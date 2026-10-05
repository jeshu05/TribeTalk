package org.tribetalk.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.sqrt

/**
 * Robust 16 kHz audio capture engine using Android AudioRecord.
 * Produces 1D float32 PCM normalized to [-1.0, 1.0] matching TribeTalk's 16 kHz pipeline.
 */
class AudioRecorder {
    companion object {
        private const val TAG = "AudioRecorder"
        const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioRecord: AudioRecord? = null
    private val isRecording = AtomicBoolean(false)
    private var recordingJob: Job? = null
    private val recordingScope = CoroutineScope(Dispatchers.IO)

    // Current normalized RMS amplitude for live UI waveform visualizer [0.0, 1.0]
    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    @SuppressLint("MissingPermission")
    fun start(onComplete: (FloatArray) -> Unit): Boolean {
        if (isRecording.get()) {
            Log.w(TAG, "Recording already in progress")
            return false
        }

        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        val bufferSize = maxOf(minBufferSize, SAMPLE_RATE * 2)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord failed to initialize")
                audioRecord?.release()
                audioRecord = null
                return false
            }

            audioRecord?.startRecording()
            isRecording.set(true)
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting AudioRecord", e)
            audioRecord?.release()
            audioRecord = null
            return false
        }

        recordingJob = recordingScope.launch {
            val shortBuffer = ShortArray(1024)
            val recordedShorts = ArrayList<Short>(SAMPLE_RATE * 5)

            while (isActive && isRecording.get()) {
                val record = audioRecord ?: break
                val readCount = record.read(shortBuffer, 0, shortBuffer.size)
                if (readCount > 0) {
                    var sumSquare = 0.0
                    for (i in 0 until readCount) {
                        val s = shortBuffer[i]
                        recordedShorts.add(s)
                        sumSquare += s * s
                    }
                    val rms = sqrt(sumSquare / readCount).toFloat()
                    val normalizedAmp = (rms / 32768f).coerceIn(0f, 1f)
                    _amplitude.value = normalizedAmp
                }
            }

            // Drain remaining buffered samples so the final spoken syllable/word is never lost
            val record = audioRecord
            if (record != null) {
                var drainCount: Int
                var drainAttempts = 0
                while (drainAttempts < 5) {
                    drainCount = record.read(shortBuffer, 0, shortBuffer.size, AudioRecord.READ_NON_BLOCKING)
                    if (drainCount > 0) {
                        for (i in 0 until drainCount) {
                            recordedShorts.add(shortBuffer[i])
                        }
                    } else {
                        break
                    }
                    drainAttempts++
                }
            }

            try {
                audioRecord?.stop()
                audioRecord?.release()
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping AudioRecord", e)
            } finally {
                audioRecord = null
            }

            // Comfort padding: Append 400ms of silence so the Conformer ASR acoustic model
            // has complete right-context for CTC decoding of the final word.
            val trailingPadding = (SAMPLE_RATE * 0.4f).toInt()
            val totalSamples = recordedShorts.size + trailingPadding
            val floatArray = FloatArray(totalSamples)
            var maxAbs = 0f
            for (i in recordedShorts.indices) {
                val f = recordedShorts[i] / 32768f
                floatArray[i] = f
                val absVal = kotlin.math.abs(f)
                if (absVal > maxAbs) maxAbs = absVal
            }

            // Gentle digital gain boost for low-sensitivity tablet microphones
            if (maxAbs in 0.005f..0.25f) {
                val gain = (0.5f / maxAbs).coerceAtMost(5.0f)
                for (i in 0 until recordedShorts.size) {
                    floatArray[i] = (floatArray[i] * gain).coerceIn(-1f, 1f)
                }
            }

            _amplitude.value = 0f
            onComplete(floatArray)
        }

        return true
    }

    fun stop() {
        isRecording.set(false)
    }
}
