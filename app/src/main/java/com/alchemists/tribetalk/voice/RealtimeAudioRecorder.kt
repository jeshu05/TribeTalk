package com.alchemists.tribetalk.voice

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Continuous Real-Time Audio Recorder.
 * Captures 16 kHz 16-bit Mono PCM audio in real-time without blocking the main UI thread.
 * Streams ShortArray frames to a shared Coroutine Flow.
 */
class RealtimeAudioRecorder(
    private val sampleRate: Int = 16000,
    private val frameSizeSamples: Int = 480 // 30ms @ 16kHz
) : AutoCloseable {

    companion object {
        private const val TAG = "RealtimeAudioRecorder"
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioRecord: AudioRecord? = null
    private val isRecording = AtomicBoolean(false)
    private var recordingThread: Thread? = null

    private val _audioFlow = MutableSharedFlow<ShortArray>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val audioFlow: SharedFlow<ShortArray> = _audioFlow.asSharedFlow()

    @SuppressLint("MissingPermission")
    fun startRecording(onError: (String) -> Unit = {}): Boolean {
        if (isRecording.get()) return true

        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, CHANNEL_CONFIG, AUDIO_FORMAT)
        if (minBufferSize <= 0) {
            val err = "Invalid AudioRecord buffer size ($minBufferSize)"
            Log.e(TAG, err)
            onError(err)
            return false
        }

        val bufferSize = (minBufferSize * 2).coerceAtLeast(3200)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                val err = "Failed to initialize AudioRecord"
                Log.e(TAG, err)
                onError(err)
                return false
            }

            audioRecord?.startRecording()
            isRecording.set(true)
            Log.i(TAG, "[Mic] Continuous recording started (16kHz Mono 16-bit PCM, frameSize=$frameSizeSamples)")

            recordingThread = Thread {
                val buffer = ShortArray(frameSizeSamples)
                while (isRecording.get()) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        val chunk = buffer.copyOf(read)
                        _audioFlow.tryEmit(chunk)
                    }
                }
            }.apply {
                name = "RealtimeAudioRecorderThread"
                start()
            }

            return true
        } catch (e: Exception) {
            val err = "Error starting AudioRecord: ${e.message}"
            Log.e(TAG, err, e)
            onError(err)
            return false
        }
    }

    fun stopRecording() {
        if (!isRecording.getAndSet(false)) return
        Log.i(TAG, "[Mic] Stopping continuous microphone recording")
        try {
            audioRecord?.stop()
            recordingThread?.join(300)
            audioRecord?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Exception during AudioRecord release", e)
        }
        audioRecord = null
    }

    override fun close() {
        stopRecording()
    }
}
