package com.alchemists.tribetalk.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.File

/**
 * Single queued audio payload ready for playback.
 */
data class QueuedAudio(
    val utteranceId: String,
    val wavBytes: ByteArray,
    val onStart: () -> Unit = {},
    val onComplete: () -> Unit = {},
    val onError: (String) -> Unit = {}
)

/**
 * Thread-safe & coroutine-safe FIFO Audio Playback Queue for Live Santali Speech.
 *
 * Decouples TTS synthesis from audio playback:
 * - Plays audio items sequentially without overlap (A -> B -> C).
 * - Allows the translation/TTS pipeline to pre-synthesize Utterance B while Utterance A is playing.
 * - Automatically deletes temporary WAV files upon playback completion.
 */
class SantaliAudioQueue(
    private val context: Context
) : AutoCloseable {

    companion object {
        private const val TAG = "SantaliAudioQueue"
        private const val MAX_AUDIO_CAPACITY = 10
    }

    private var audioChannel = Channel<QueuedAudio>(MAX_AUDIO_CAPACITY)
    private var playbackJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    var isAudioPlaying: Boolean = false

    fun start(scope: CoroutineScope) {
        stop()
        audioChannel = Channel(MAX_AUDIO_CAPACITY)
        playbackJob = scope.launch(Dispatchers.IO) {
            Log.i(TAG, "[AUDIO QUEUE] Worker started")
            try {
                for (item in audioChannel) {
                    playAudioItemSequentially(item)
                }
            } catch (_: CancellationException) {
                Log.i(TAG, "[AUDIO QUEUE] Worker cancelled")
            }
        }
    }

    suspend fun enqueueAudio(item: QueuedAudio) {
        audioChannel.send(item)
    }

    private suspend fun playAudioItemSequentially(item: QueuedAudio) = suspendCancellableCoroutine<Unit> { cont ->
        mainHandler.post {
            try {
                val tempFile = File(context.cacheDir, "live_audio_${item.utteranceId}.wav")
                tempFile.writeBytes(item.wavBytes)

                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(tempFile.absolutePath)
                    setOnPreparedListener { player ->
                        this@SantaliAudioQueue.isAudioPlaying = true
                        Log.i(TAG, "[AUDIO QUEUE] Started playback for #${item.utteranceId} (${item.wavBytes.size} bytes)")
                        item.onStart()
                        player.start()
                    }
                    setOnCompletionListener { player ->
                        this@SantaliAudioQueue.isAudioPlaying = false
                        Log.i(TAG, "[AUDIO QUEUE] Completed playback for #${item.utteranceId}")
                        try {
                            player.reset()
                            player.release()
                        } catch (_: Exception) {}
                        tempFile.delete()
                        item.onComplete()
                        if (cont.isActive) cont.resume(Unit) {}
                    }
                    setOnErrorListener { player, what, extra ->
                        this@SantaliAudioQueue.isAudioPlaying = false
                        Log.e(TAG, "[AUDIO QUEUE ERROR] Error playing #${item.utteranceId}: what=$what, extra=$extra")
                        try {
                            player.reset()
                            player.release()
                        } catch (_: Exception) {}
                        tempFile.delete()
                        item.onError("MediaPlayer error ($what, $extra)")
                        if (cont.isActive) cont.resume(Unit) {}
                        true
                    }
                    prepareAsync()
                }
                mediaPlayer = mp
            } catch (e: Exception) {
                this@SantaliAudioQueue.isAudioPlaying = false
                Log.e(TAG, "[AUDIO QUEUE ERROR] Exception for #${item.utteranceId}: ${e.localizedMessage}", e)
                item.onError(e.localizedMessage ?: "Playback error")
                if (cont.isActive) cont.resume(Unit) {}
            }
        }
    }

    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        mainHandler.post {
            try {
                mediaPlayer?.let {
                    if (it.isPlaying) it.stop()
                    it.reset()
                    it.release()
                }
            } catch (_: Exception) {}
            mediaPlayer = null
            isAudioPlaying = false
        }
    }

    override fun close() {
        stop()
    }
}
