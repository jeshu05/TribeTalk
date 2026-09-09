package org.tribetalk.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * High-performance 16 kHz audio player using Android AudioTrack.
 * Plays 1D float32 PCM waveforms synthesized by TribeTalk's TTS engines directly from RAM.
 */
class AudioPlayer {
    companion object {
        private const val TAG = "AudioPlayer"
        const val SAMPLE_RATE = 16000
    }

    private var audioTrack: AudioTrack? = null
    private val isPlaying = AtomicBoolean(false)
    private var playbackJob: Job? = null
    private val playbackScope = CoroutineScope(Dispatchers.IO)

    private val _isPlayingState = MutableStateFlow(false)
    val isPlayingState: StateFlow<Boolean> = _isPlayingState.asStateFlow()

    fun play(samples: FloatArray, onFinished: (() -> Unit)? = null) {
        stop()

        if (samples.isEmpty()) {
            onFinished?.invoke()
            return
        }

        val minBuffer = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_FLOAT
        )
        val bufferSize = if (minBuffer > 0) {
            minBuffer.coerceAtLeast(samples.size * 4)
        } else {
            samples.size * 4
        }

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
            isPlaying.set(true)
            _isPlayingState.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AudioTrack", e)
            stop()
            onFinished?.invoke()
            return
        }

        playbackJob = playbackScope.launch {
            var completedNormally = false
            try {
                audioTrack?.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
                // Small sleep to ensure the hardware finishes draining the buffer
                val durationMs = (samples.size.toFloat() / SAMPLE_RATE * 1000f).toLong()
                kotlinx.coroutines.delay(durationMs.coerceAtLeast(100L))
                completedNormally = true
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.w(TAG, "Exception during audio playback", e)
                }
            } finally {
                stop()
                if (completedNormally) {
                    onFinished?.invoke()
                }
            }
        }
    }

    fun stop() {
        isPlaying.set(false)
        _isPlayingState.value = false
        val currentJob = playbackJob
        playbackJob = null
        currentJob?.cancel()

        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping AudioTrack", e)
        } finally {
            audioTrack = null
        }
    }
}
