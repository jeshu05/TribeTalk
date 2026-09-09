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
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
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
                val readCount = audioRecord?.read(shortBuffer, 0, shortBuffer.size) ?: 0
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

            // Convert captured 16-bit PCM shorts to normalized FloatArray [-1.0, 1.0]
            val floatArray = FloatArray(recordedShorts.size)
            for (i in recordedShorts.indices) {
                floatArray[i] = (recordedShorts[i] / 32768f).coerceIn(-1f, 1f)
            }

            _amplitude.value = 0f
            onComplete(floatArray)
        }

        return true
    }

    fun stop() {
        if (!isRecording.getAndSet(false)) {
            return
        }

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping AudioRecord", e)
        } finally {
            audioRecord = null
        }
    }
}
