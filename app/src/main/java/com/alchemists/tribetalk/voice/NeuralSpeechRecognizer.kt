package com.alchemists.tribetalk.voice

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Collections
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Neural Automatic Speech Recognition (ASR) Engine based on AI4Bharat IndicConformer ONNX INT8.
 * Records raw 16 kHz 16-bit Mono PCM audio directly via AudioRecord and transcribes offline.
 */
class NeuralSpeechRecognizer(
    private val context: Context
) : AutoCloseable {

    companion object {
        private const val TAG = "LiveHindiASR"
        const val SAMPLE_RATE = 16000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingThread: Thread? = null

    private val pcmAccumulator = Collections.synchronizedList(mutableListOf<Short>())
    private val indicConformerAsr by lazy { IndicConformerHindiAsr(context) }
    private val scope = CoroutineScope(Dispatchers.IO)
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    private var activeOnResultCallback: ((String) -> Unit)? = null
    private var recordStartTimeMs = 0L

    fun getModelFile(languageCode: String): File? {
        val modelsDir = File(context.filesDir, "models")
        if (!modelsDir.exists()) modelsDir.mkdirs()
        val modelFile = File(modelsDir, "${languageCode}_asr_int8.onnx")
        if (modelFile.exists() && modelFile.length() > 0) return modelFile

        try {
            context.assets.open("models/${languageCode}_asr_int8.onnx").use { input ->
                modelFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (modelFile.exists() && modelFile.length() > 0) return modelFile
        } catch (_: Exception) {}

        return null
    }

    fun isNeuralModelAvailable(languageCode: String): Boolean = indicConformerAsr.isAvailable()

    @SuppressLint("MissingPermission")
    fun startListening(
        languageCode: String,
        onSpeechDetected: () -> Unit = {},
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        Log.i("VOICE", "[VOICE] AUDIO_CAPTURE_INITIALIZING")
        Log.i(TAG, "[HindiASR] ASR START: Requested language=$languageCode")
        stopListening(onResult = null)

        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            val err = "Invalid audio buffer size on this device"
            Log.e(TAG, "[HindiASR] ASR ERROR: $err")
            mainHandler.post { onError(err) }
            return
        }

        val bufferSize = (minBufferSize * 2).coerceAtLeast(3200)

        try {
            var initializedRecord: AudioRecord? = null
            val audioSources = listOf(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                MediaRecorder.AudioSource.MIC,
                MediaRecorder.AudioSource.DEFAULT
            )

            for (src in audioSources) {
                try {
                    val candidate = AudioRecord(
                        src,
                        SAMPLE_RATE,
                        CHANNEL_CONFIG,
                        AUDIO_FORMAT,
                        bufferSize
                    )
                    if (candidate.state == AudioRecord.STATE_INITIALIZED) {
                        initializedRecord = candidate
                        Log.i("VOICE", "[VOICE] AUDIO_CAPTURE_STARTED (source=$src, 16kHz Mono 16-bit PCM)")
                        Log.i("VOICE", "[VOICE] ASR_LISTENING")
                        break
                    } else {
                        candidate.release()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "AudioSource $src failed: ${e.message}")
                }
            }

            audioRecord = initializedRecord

            if (audioRecord == null || audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                val err = "AudioRecord initialization failed across all audio sources"
                Log.e(TAG, "[HindiASR] ASR ERROR: $err")
                mainHandler.post { onError(err) }
                return
            }

            pcmAccumulator.clear()
            activeOnResultCallback = onResult
            recordStartTimeMs = System.currentTimeMillis()

            audioRecord?.startRecording()
            isRecording = true
            Log.i(TAG, "[HindiASR] MICROPHONE STARTED: 16kHz Mono 16-bit PCM AudioRecord active")

            val vadProcessor = AudioVADProcessor(sampleRate = SAMPLE_RATE)

            recordingThread = Thread {
                val audioBuffer = ShortArray(vadProcessor.frameSizeSamples) // 30ms frame (480 samples)
                var speechDetected = false
                var lastSpeechTimestamp = 0L

                while (isRecording) {
                    val readSamples = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0
                    if (readSamples > 0) {
                        for (i in 0 until readSamples) {
                            pcmAccumulator.add(audioBuffer[i])
                        }

                        vadProcessor.processSamples(audioBuffer) { _ ->
                            if (!speechDetected) {
                                speechDetected = true
                                Log.i(TAG, "[HindiASR] AUDIO RECEIVED: Speech activity detected by VAD")
                                mainHandler.post { onSpeechDetected() }
                            }
                            lastSpeechTimestamp = System.currentTimeMillis()
                        }

                        // Auto-endpoint: if speech was detected and 1.2s silence elapsed, or 6s total max
                        val now = System.currentTimeMillis()
                        if (speechDetected && lastSpeechTimestamp > 0 && (now - lastSpeechTimestamp > 1200L)) {
                            Log.i(TAG, "[HindiASR] Speech endpoint detected after silence")
                            stopListening()
                            break
                        } else if (now - recordStartTimeMs > 6000L) {
                            Log.i(TAG, "[HindiASR] Max speech duration reached (6s)")
                            stopListening()
                            break
                        }
                    }
                }
            }.apply { start() }

        } catch (e: Exception) {
            val err = "Neural ASR error: ${e.localizedMessage}"
            Log.e(TAG, "[HindiASR] ASR ERROR: $err", e)
            mainHandler.post { onError(err) }
        }
    }

    fun stopListening(onResult: ((String) -> Unit)? = null) {
        val t1 = System.currentTimeMillis()
        val recordingDurationMs = if (recordStartTimeMs > 0) t1 - recordStartTimeMs else 0L

        val resultCallback = onResult ?: activeOnResultCallback
        activeOnResultCallback = null
        isRecording = false

        try {
            audioRecord?.stop()
            recordingThread?.join(500)
            audioRecord?.release()
        } catch (_: Exception) {}

        audioRecord = null
        Log.i(TAG, "[HindiASR] ASR STOPPED: Recording duration=${recordingDurationMs}ms")

        val pcmArray = synchronized(pcmAccumulator) {
            val copy = pcmAccumulator.toShortArray()
            pcmAccumulator.clear()
            copy
        }

        if (pcmArray.isNotEmpty()) {
            logAudioValidationStats(pcmArray, recordingDurationMs)
            saveDebugWavFile(pcmArray)

            if (resultCallback != null) {
                scope.launch {
                    val transcribedText = indicConformerAsr.transcribe(pcmArray)
                    Log.i("VOICE", "[VOICE] HINDI_FINAL = $transcribedText")
                    Log.i(TAG, "[HindiASR] FINAL RESULT: \"$transcribedText\"")
                    mainHandler.post {
                        resultCallback.invoke(transcribedText)
                    }
                }
            }
        } else {
            Log.w(TAG, "[HindiASR] ASR WARN: Zero PCM audio samples recorded")
        }
    }

    private fun logAudioValidationStats(pcm: ShortArray, durationMs: Long) {
        var minVal = Int.MAX_VALUE
        var maxVal = Int.MIN_VALUE
        var sumAbs = 0.0
        var sumSq = 0.0
        var zeroCount = 0

        for (sample in pcm) {
            val s = sample.toInt()
            if (s < minVal) minVal = s
            if (s > maxVal) maxVal = s
            if (s == 0) zeroCount++
            val absVal = abs(s)
            sumAbs += absVal
            sumSq += absVal.toDouble() * absVal.toDouble()
        }

        val n = pcm.size.toDouble()
        val meanAbs = sumAbs / n
        val rms = sqrt(sumSq / n)

        val first20 = pcm.take(20).joinToString(", ")
        val last20 = pcm.takeLast(20).joinToString(", ")

        Log.i(TAG, "[ASR AUDIO VALIDATION]")
        Log.i(TAG, "  sampleRate=16000, channels=1, encoding=PCM_16BIT")
        Log.i(TAG, "  samples=${pcm.size}, duration=${durationMs}ms (${String.format("%.2f", pcm.size / 16000.0)} sec)")
        Log.i(TAG, "  min=$minVal, max=$maxVal, meanAbs=${String.format("%.2f", meanAbs)}, rms=${String.format("%.2f", rms)}, zeros=$zeroCount")
        Log.i(TAG, "  First 20 PCM samples: [$first20]")
        Log.i(TAG, "  Last 20 PCM samples:  [$last20]")

        if (rms < 50.0) {
            Log.w(TAG, "[ASR WARNING] Recorded microphone audio has extremely low RMS energy (${String.format("%.2f", rms)})! Check physical microphone gain or permissions.")
        }
    }

    private fun saveDebugWavFile(pcm: ShortArray) {
        try {
            val filesDir = context.filesDir ?: return
            val debugWav = File(filesDir, "debug_mic_capture.wav")
            writeWavFile(debugWav, pcm, SAMPLE_RATE)
            Log.i(TAG, "[ASR DEBUG] Saved captured microphone audio to: ${debugWav.absolutePath} (${debugWav.length()} bytes)")
        } catch (e: Exception) {
            Log.e(TAG, "[ASR DEBUG] Failed to save debug WAV file", e)
        }
    }

    private fun writeWavFile(file: File, pcm: ShortArray, sampleRate: Int) {
        val totalAudioLen = pcm.size * 2
        val totalDataLen = totalAudioLen + 36
        val channels = 1
        val byteRate = sampleRate * 2

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0 // subchunk1size = 16
        header[20] = 1; header[21] = 0 // PCM = 1
        header[22] = channels.toByte(); header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = (sampleRate shr 8 and 0xff).toByte()
        header[26] = (sampleRate shr 16 and 0xff).toByte()
        header[27] = (sampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = 2; header[33] = 0 // block align = 2
        header[34] = 16; header[35] = 0 // bits per sample = 16
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()

        FileOutputStream(file).use { out ->
            out.write(header)
            val pcmBytes = ByteBuffer.allocate(pcm.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            for (s in pcm) pcmBytes.putShort(s)
            out.write(pcmBytes.array())
        }
    }

    override fun close() {
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
            indicConformerAsr.close()
        } catch (_: Exception) {}
        audioRecord = null
    }
}
