package org.tribetalk.audio

import android.content.Context
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.io.File
import java.nio.FloatBuffer
import java.nio.LongBuffer

/**
 * On-device neural speech recognizer using OpenVoiceOS IndicConformer INT8 ONNX models.
 * Runs completely offline on Android without requiring system Google Speech Services.
 */
class OnnxConformerAsr(private val context: Context) {

    companion object {
        private const val TAG = "OnnxConformerAsr"
        private const val DEFAULT_BLANK_ID = 256
    }

    private var ortEnv: OrtEnvironment? = null
    private var hiSession: OrtSession? = null
    private var satSession: OrtSession? = null
    private val hiVocab = mutableMapOf<Int, String>()
    private val satVocab = mutableMapOf<Int, String>()
    private var hiBlankId = DEFAULT_BLANK_ID
    private var satBlankId = DEFAULT_BLANK_ID

    private val featureExtractor = ConformerFeatureExtractor()
    var isReady = false
        private set

    init {
        initSessions()
    }

    private fun resolveModelFile(subDir: String, fileName: String): File? {
        val candidates = listOf(
            File(context.getExternalFilesDir(null), "models/$subDir/$fileName"),
            File(context.getExternalFilesDir(null), "models/$fileName"),
            File("/sdcard/Android/data/org.tribetalk/files/models/$subDir/$fileName"),
            File("/sdcard/Android/data/org.tribetalk/files/models/$fileName"),
            File(context.filesDir, "models/$subDir/$fileName"),
            File(context.filesDir, "models/$fileName")
        )
        return candidates.firstOrNull { it.exists() && it.length() > 0 }
    }

    private fun loadVocab(vocabFile: File, targetMap: MutableMap<Int, String>): Int {
        var blankId = DEFAULT_BLANK_ID
        try {
            vocabFile.forEachLine(Charsets.UTF_8) { line ->
                val parts = line.trim().split(Regex("\\s+"))
                if (parts.size >= 2) {
                    val piece = parts[0]
                    val idx = parts[1].toIntOrNull()
                    if (idx != null) {
                        targetMap[idx] = piece
                        if (piece == "<blk>") {
                            blankId = idx
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed reading vocab file ${vocabFile.absolutePath}: ${e.message}")
        }
        return blankId
    }

    fun initSessions() {
        try {
            ortEnv = OrtEnvironment.getEnvironment()
            val opts = OrtSession.SessionOptions().apply {
                setIntraOpNumThreads(2)
            }

            // 1. Hindi Conformer
            val hiModelFile = resolveModelFile("asr", "hindi_conformer.onnx")
            val hiVocabFile = resolveModelFile("asr", "hindi_vocab.txt")
            if (hiModelFile != null && hiVocabFile != null) {
                hiBlankId = loadVocab(hiVocabFile, hiVocab)
                hiSession = ortEnv?.createSession(hiModelFile.absolutePath, opts)
                Log.i(TAG, "Hindi IndicConformer ASR session loaded successfully (${hiVocab.size} vocab tokens).")
            } else {
                Log.w(TAG, "Hindi Conformer files not found on device.")
            }

            // 2. Santali Conformer
            val satModelFile = resolveModelFile("asr", "santali_conformer.onnx")
            val satVocabFile = resolveModelFile("asr", "santali_vocab.txt")
            if (satModelFile != null && satVocabFile != null) {
                satBlankId = loadVocab(satVocabFile, satVocab)
                satSession = ortEnv?.createSession(satModelFile.absolutePath, opts)
                Log.i(TAG, "Santali IndicConformer ASR session loaded successfully (${satVocab.size} vocab tokens).")
            } else {
                Log.w(TAG, "Santali Conformer files not found on device.")
            }

            isReady = (hiSession != null || satSession != null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed initializing OnnxConformerAsr sessions", e)
        }
    }

    /**
     * Transcribes raw 16 kHz mono float32 audio samples using on-device IndicConformer ONNX model.
     *
     * @param audio Audio waveform normalized to [-1.0, 1.0].
     * @param isHindi True for Hindi speech recognition, False for Santali speech recognition.
     * @return Transcribed text in Devanagari or Ol Chiki.
     */
    fun transcribe(audio: FloatArray, isHindi: Boolean): String {
        val env = ortEnv ?: return ""
        val session = if (isHindi) hiSession else satSession
        val vocab = if (isHindi) hiVocab else satVocab
        val blankId = if (isHindi) hiBlankId else satBlankId

        if (session == null || vocab.isEmpty() || audio.isEmpty()) {
            return ""
        }

        try {
            // 1. Extract 80-channel log-mel features with per-feature normalization
            val (flatFeatures, paddedFrames) = featureExtractor.extractFeatures(audio)

            // 2. Wrap tensors
            val signalBuffer = FloatBuffer.wrap(flatFeatures)
            val lenBuffer = LongBuffer.wrap(longArrayOf(paddedFrames.toLong()))

            val signalTensor = OnnxTensor.createTensor(env, signalBuffer, longArrayOf(1, 80, paddedFrames.toLong()))
            val lenTensor = OnnxTensor.createTensor(env, lenBuffer, longArrayOf(1))

            val inputs = mapOf(
                "audio_signal" to signalTensor,
                "length" to lenTensor
            )

            // 3. Run acoustic model forward pass
            val results = session.run(inputs)
            val logprobsTensor = results[0] as? OnnxTensor

            var transcribedText = ""
            if (logprobsTensor != null) {
                val shape = logprobsTensor.info.shape // [1, time_steps, vocab_size]
                val timeSteps = shape[1].toInt()
                val vocabSize = shape[2].toInt()

                val floatBuffer = logprobsTensor.floatBuffer
                val rawPreds = IntArray(timeSteps)

                for (t in 0 until timeSteps) {
                    var maxVal = Float.NEGATIVE_INFINITY
                    var maxIdx = 0
                    for (v in 0 until vocabSize) {
                        val score = floatBuffer.get(t * vocabSize + v)
                        if (score > maxVal) {
                            maxVal = score
                            maxIdx = v
                        }
                    }
                    rawPreds[t] = maxIdx
                }

                // 4. CTC Greedy Decoding: collapse consecutive duplicates and remove blanks
                val collapsed = mutableListOf<Int>()
                var prev = -1
                for (p in rawPreds) {
                    if (p != prev) {
                        if (p != blankId) {
                            collapsed.add(p)
                        }
                        prev = p
                    }
                }

                // 5. Piece aggregation
                val sb = StringBuilder()
                for (id in collapsed) {
                    vocab[id]?.let { sb.append(it) }
                }
                transcribedText = sb.toString().replace("\u2581", " ").trim()
            }

            // Cleanup
            signalTensor.close()
            lenTensor.close()
            results.close()

            return transcribedText
        } catch (e: Exception) {
            Log.e(TAG, "Error in IndicConformer ONNX inference", e)
            return ""
        }
    }
}
