package com.alchemists.tribetalk.voice

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.LongBuffer
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Standard Hindi ASR Engine Interface (Phase 11).
 */
interface HindiAsrEngine {
    suspend fun transcribe(audio: ShortArray): String
}

/**
 * Offline AI4Bharat IndicConformer Hindi ASR Engine.
 *
 * Runs 100% local, network-free ONNX Runtime inference using `indicconformer_hi_ctc_int8.onnx`.
 * Extracts 80-band NeMo Log Mel-Filterbank features and decodes Devanagari Hindi text.
 */
class IndicConformerHindiAsr(
    private val context: Context
) : HindiAsrEngine, AutoCloseable {

    companion object {
        private const val TAG = "IndicConformerHindiAsr"
    }

    private var ortEnv: OrtEnvironment? = null
    private var session: OrtSession? = null
    private var isInitialized = false

    private val vocabList = mutableListOf<String>()
    private var blankTokenId = 256

    private val melFilterbank = Array(80) { FloatArray(257) }
    private val hannWindow = FloatArray(400)
    private val cosTable = Array(257) { FloatArray(400) }
    private val sinTable = Array(257) { FloatArray(400) }

    init {
        initEngine()
        initPrecomputedTables()
        loadMelFilterbank()
    }

    private fun initEngine() {
        try {
            ortEnv = OrtEnvironment.getEnvironment()
            loadVocab()

            val opts = OrtSession.SessionOptions().apply {
                setIntraOpNumThreads(4)
                setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            }

            val modelFile = getAssetModelFile("models/int8/indicconformer_hi_ctc_int8.onnx", "indicconformer_hi_ctc_int8.onnx")
                ?: getAssetModelFile("models/indicconformer_hi_ctc_int8.onnx", "indicconformer_hi_ctc_int8.onnx")

            if (modelFile != null && modelFile.exists()) {
                session = ortEnv?.createSession(modelFile.absolutePath, opts)
                isInitialized = true
                Log.i(TAG, "[ASR] Offline IndicConformer ONNX Session initialized successfully")
                Log.i(TAG, "  Model path: ${modelFile.absolutePath} (${modelFile.length() / (1024 * 1024)} MB)")
                Log.i(TAG, "  Vocab size: ${vocabList.size}, Blank token ID: $blankTokenId")
            } else {
                Log.e(TAG, "[ASR ERROR] Failed to locate or load model asset: indicconformer_hi_ctc_int8.onnx")
                isInitialized = false
            }
        } catch (e: Throwable) {
            Log.e(TAG, "[ASR ERROR] Exception during ONNX session initialization", e)
            isInitialized = false
        }
    }

    private fun initPrecomputedTables() {
        val pi = Math.PI
        for (i in 0 until 400) {
            hannWindow[i] = (0.5 * (1.0 - cos(2.0 * pi * i / 400.0))).toFloat()
        }
        for (k in 0 until 257) {
            for (n in 0 until 400) {
                val angle = 2.0 * pi * k * n / 512.0
                cosTable[k][n] = cos(angle).toFloat()
                sinTable[k][n] = sin(angle).toFloat()
            }
        }
    }

    private fun loadMelFilterbank() {
        try {
            context.assets.open("models/mel_filterbank.bin").use { input ->
                val bytes = input.readBytes()
                val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()
                for (m in 0 until 80) {
                    for (k in 0 until 257) {
                        if (buffer.hasRemaining()) {
                            melFilterbank[m][k] = buffer.get()
                        }
                    }
                }
            }
            Log.i(TAG, "[ASR] Loaded exact 80x257 Slaney Mel Filterbank matrix from assets")
        } catch (e: Exception) {
            Log.w(TAG, "[ASR] Mel filterbank binary missing from assets, generating mathematical fallback matrix", e)
            generateMelFilterbankFallback()
        }
    }

    private fun generateMelFilterbankFallback() {
        fun hzToMel(hz: Double): Double = 2595.0 * Math.log10(1.0 + hz / 700.0)
        fun melToHz(mel: Double): Double = 700.0 * (Math.pow(10.0, mel / 2595.0) - 1.0)

        val melMin = hzToMel(0.0)
        val melMax = hzToMel(8000.0)
        val melPoints = DoubleArray(82) { i -> melMin + i * (melMax - melMin) / 81.0 }
        val hzPoints = DoubleArray(82) { i -> melToHz(melPoints[i]) }

        val fftFreqs = DoubleArray(257) { i -> i * 8000.0 / 256.0 }

        for (m in 0 until 80) {
            val left = hzPoints[m]
            val center = hzPoints[m + 1]
            val right = hzPoints[m + 2]
            val enorm = 2.0 / (right - left + 1e-5)

            for (k in 0 until 257) {
                val freq = fftFreqs[k]
                var w = 0.0
                if (freq in left..center) {
                    w = (freq - left) / (center - left + 1e-5)
                } else if (freq in center..right) {
                    w = (right - freq) / (right - center + 1e-5)
                }
                melFilterbank[m][k] = (w * enorm).toFloat()
            }
        }
    }

    private fun loadVocab() {
        try {
            vocabList.clear()
            context.assets.open("models/vocab.txt").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val token = line.trim().split("\\s+".toRegex()).firstOrNull() ?: ""
                    vocabList.add(token)
                }
            }
            if (vocabList.isNotEmpty()) {
                blankTokenId = vocabList.size - 1
            }
            Log.i(TAG, "[ASR] Loaded vocabulary from assets: ${vocabList.size} tokens (blank ID: $blankTokenId)")
        } catch (e: Exception) {
            Log.e(TAG, "[ASR ERROR] Failed to load models/vocab.txt from assets", e)
        }
    }

    private fun getAssetModelFile(assetPath: String, outputName: String): File? {
        val filesDir = context.filesDir ?: return null
        val modelsDir = File(filesDir, "indicconformer_models").apply { if (!exists()) mkdirs() }
        val candidate = File(modelsDir, outputName)
        if (candidate.exists() && candidate.length() > 0) return candidate

        return try {
            context.assets.open(assetPath).use { input ->
                candidate.outputStream().use { output -> input.copyTo(output) }
            }
            if (candidate.exists() && candidate.length() > 0) candidate else null
        } catch (_: Exception) {
            null
        }
    }

    private fun computeNeMoLogMelFeatures(pcm: ShortArray): Array<FloatArray> {
        val nSamples = pcm.size
        if (nSamples < 400) return Array(80) { FloatArray(1) }

        val floatWave = FloatArray(nSamples)

        // Pre-emphasis filter: y[i] = x[i] - 0.97 * x[i-1]
        floatWave[0] = pcm[0] / 32768.0f
        for (i in 1 until nSamples) {
            floatWave[i] = (pcm[i] - 0.97f * pcm[i - 1]) / 32768.0f
        }

        // Center padding: reflect pad 256 samples on left and right
        val pad = 256
        val paddedLen = nSamples + 2 * pad
        val padded = FloatArray(paddedLen)

        for (i in 0 until pad) {
            padded[i] = floatWave[pad - i]
        }
        for (i in 0 until nSamples) {
            padded[pad + i] = floatWave[i]
        }
        for (i in 0 until pad) {
            padded[pad + nSamples + i] = floatWave[nSamples - 2 - i]
        }

        val hopLength = 160
        val winLength = 400
        val numFrames = (paddedLen - winLength) / hopLength + 1
        if (numFrames <= 0) return Array(80) { FloatArray(1) }

        val logMel = Array(80) { FloatArray(numFrames) }
        val powerSpec = FloatArray(257)

        for (frame in 0 until numFrames) {
            val offset = frame * hopLength

            // Compute 257-bin Power Spectrogram via precomputed DFT matrix
            for (k in 0 until 257) {
                var re = 0.0f
                var im = 0.0f
                val cosK = cosTable[k]
                val sinK = sinTable[k]

                for (n in 0 until winLength) {
                    val sample = padded[offset + n] * hannWindow[n]
                    re += sample * cosK[n]
                    im -= sample * sinK[n]
                }
                powerSpec[k] = re * re + im * im
            }

            // Matrix multiply 80x257 filterbank by 257-bin Power Spectrogram
            for (m in 0 until 80) {
                var sum = 0.0f
                val filterM = melFilterbank[m]
                for (k in 0 until 257) {
                    sum += filterM[k] * powerSpec[k]
                }
                logMel[m][frame] = ln((sum + 1e-5f).toDouble()).toFloat()
            }
        }

        // Per-feature z-score normalization across frames: (x - mean) / (std + 1e-5)
        for (m in 0 until 80) {
            var sum = 0.0
            for (f in 0 until numFrames) {
                sum += logMel[m][f]
            }
            val mean = (sum / numFrames).toFloat()

            var varSum = 0.0
            for (f in 0 until numFrames) {
                val diff = logMel[m][f] - mean
                varSum += diff * diff
            }
            val std = sqrt(varSum / numFrames).toFloat()

            for (f in 0 until numFrames) {
                logMel[m][f] = (logMel[m][f] - mean) / (std + 1e-5f)
            }
        }

        return logMel
    }

    override suspend fun transcribe(audio: ShortArray): String = withContext(Dispatchers.IO) {
        if (!isInitialized || session == null || audio.isEmpty()) {
            Log.e(TAG, "[ASR ERROR] Cannot transcribe: engine is not initialized or audio array is empty (samples=${audio.size})")
            return@withContext ""
        }

        val env = ortEnv ?: return@withContext ""

        val t2 = System.currentTimeMillis()
        // Compute real NeMo 80-band Log Mel-Filterbank Features (shape: [1, 80, num_frames])
        val features = computeNeMoLogMelFeatures(audio)
        val t3 = System.currentTimeMillis()
        val prepLatencyMs = t3 - t2

        val nMels = features.size
        val numFrames = features[0].size

        // Calculate feature min, max, mean for diagnostic logging
        var fMin = Float.MAX_VALUE
        var fMax = -Float.MAX_VALUE
        var fSum = 0.0
        val totalElements = nMels * numFrames
        for (m in 0 until nMels) {
            for (f in 0 until numFrames) {
                val v = features[m][f]
                if (v < fMin) fMin = v
                if (v > fMax) fMax = v
                fSum += v
            }
        }
        val fMean = fSum / totalElements

        Log.i(TAG, "[ASR PREPROCESSING] Latency=${prepLatencyMs}ms | Shape=[1, $nMels, $numFrames] | Min=${String.format("%.2f", fMin)}, Max=${String.format("%.2f", fMax)}, Mean=${String.format("%.2f", fMean)}")

        val featureBuffer = FloatBuffer.allocate(1 * nMels * numFrames)
        for (m in 0 until nMels) {
            for (f in 0 until numFrames) {
                featureBuffer.put(features[m][f])
            }
        }
        featureBuffer.flip()

        val audioSignalShape = longArrayOf(1, nMels.toLong(), numFrames.toLong())
        val lengthShape = longArrayOf(1)

        val audioSignalTensor = OnnxTensor.createTensor(env, featureBuffer, audioSignalShape)
        val lengthTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(longArrayOf(numFrames.toLong())), lengthShape)

        val t4 = System.currentTimeMillis()
        val outputs = session?.run(
            mapOf(
                "audio_signal" to audioSignalTensor,
                "length" to lengthTensor
            )
        )
        val t5 = System.currentTimeMillis()
        val inferenceLatencyMs = t5 - t4

        val outputTensor = outputs?.get(0) as? OnnxTensor
        if (outputTensor != null) {
            val shape = outputTensor.info.shape // [1, timeSteps, 257]
            val timeSteps = shape[1].toInt()
            val vocabSize = shape[2].toInt()

            val floatBuffer = outputTensor.floatBuffer
            val rawTokens = mutableListOf<Int>()
            val collapsedTokens = mutableListOf<Int>()
            var prevToken = -1

            for (t in 0 until timeSteps) {
                var maxIdx = 0
                var maxVal = Float.NEGATIVE_INFINITY
                val offset = t * vocabSize
                for (v in 0 until vocabSize) {
                    val score = floatBuffer.get(offset + v)
                    if (score > maxVal) {
                        maxVal = score
                        maxIdx = v
                    }
                }
                rawTokens.add(maxIdx)
                if (maxIdx != prevToken) {
                    collapsedTokens.add(maxIdx)
                    prevToken = maxIdx
                }
            }

            audioSignalTensor.close()
            lengthTensor.close()
            outputs.close()

            val subwords = StringBuilder()
            val tokenNames = mutableListOf<String>()
            for (t in collapsedTokens) {
                if (t > 0 && t < blankTokenId && t < vocabList.size) {
                    val sub = vocabList[t]
                    subwords.append(sub)
                    tokenNames.add(sub)
                }
            }

            val t6 = System.currentTimeMillis()
            val decodeLatencyMs = t6 - t5
            val totalLatencyMs = t6 - t2

            val resultText = subwords.toString().replace("▁", " ").trim()

            Log.i(TAG, "[ASR INFERENCE & CTC DECODE]")
            Log.i(TAG, "  ONNX Inference Latency = ${inferenceLatencyMs}ms | CTC Decode Latency = ${decodeLatencyMs}ms | Total Engine Latency = ${totalLatencyMs}ms")
            Log.i(TAG, "  Raw argmax tokens (${rawTokens.size}): [${rawTokens.take(15).joinToString(", ")}...]")
            Log.i(TAG, "  Collapsed tokens (${collapsedTokens.size}): [${collapsedTokens.take(15).joinToString(", ")}...]")
            Log.i(TAG, "  Subwords: [${tokenNames.take(10).joinToString(", ")}]")
            Log.i(TAG, "  Final Decoded Hindi Text: \"$resultText\"")

            return@withContext resultText
        }

        audioSignalTensor.close()
        lengthTensor.close()
        outputs?.close()

        Log.e(TAG, "[ASR ERROR] ONNX Output tensor is null")
        return@withContext ""
    }

    override fun close() {
        try {
            session?.close()
            ortEnv?.close()
        } catch (_: Exception) {}
        isInitialized = false
    }
}
