package com.alchemists.tribetalk.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Health status response from the FastAPI Santali TTS server.
 */
data class TtsHealthStatus(
    val isConnected: Boolean,
    val model: String = "",
    val language: String = "",
    val device: String = "",
    val errorMessage: String? = null
)

/**
 * Real Santali TTS Provider communicating with AI4Bharat Indic Parler-TTS FastAPI server.
 *
 * Handles HTTP synthesis requests, caching audio WAV files, and sequential MediaPlayer playback.
 */
class RealSantaliTTSProvider(
    private val context: Context,
    var serverUrl: String = "http://10.0.2.2:8000"
) : AutoCloseable {

    companion object {
        private const val TAG = "RealSantaliTTS"
        private const val CONNECT_TIMEOUT_MS = 6000
        private const val READ_TIMEOUT_MS = 15000
    }

    private var mediaPlayer: MediaPlayer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isPlayingAudio = false

    /**
     * Checks server connectivity and model readiness via GET /health.
     */
    suspend fun checkHealth(): TtsHealthStatus = withContext(Dispatchers.IO) {
        val cleanUrl = serverUrl.trimEnd('/')
        try {
            val url = URL("$cleanUrl/health")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = CONNECT_TIMEOUT_MS
            }

            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonStr)
                val status = json.optString("status", "")
                val model = json.optString("model", "")
                val language = json.optString("language", "")
                val device = json.optString("device", "")

                Log.i(TAG, "[TTS HEALTH] Connected to $cleanUrl -> Model=$model, Lang=$language, Device=$device")
                TtsHealthStatus(
                    isConnected = status == "connected" || json.optBoolean("available", false),
                    model = model,
                    language = language,
                    device = device
                )
            } else {
                TtsHealthStatus(
                    isConnected = false,
                    errorMessage = "Server returned HTTP $responseCode"
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "[TTS HEALTH] Connection failed to $cleanUrl: ${e.localizedMessage}")
            TtsHealthStatus(
                isConnected = false,
                errorMessage = e.localizedMessage
            )
        }
    }

    /**
     * Sends POST /synthesize request to FastAPI server and returns binary WAV data.
     */
    suspend fun synthesize(text: String): ByteArray = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return@withContext ByteArray(0)

        val cleanUrl = serverUrl.trimEnd('/')
        val url = URL("$cleanUrl/synthesize")

        val payload = JSONObject().apply {
            put("text", trimmed)
        }.toString().toByteArray(Charsets.UTF_8)

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "audio/wav")
        }

        conn.outputStream.use { os ->
            os.write(payload)
            os.flush()
        }

        val code = conn.responseCode
        if (code == 200) {
            val wavBytes = conn.inputStream.use { it.readBytes() }
            Log.i(TAG, "[TTS SYNTHESIS] Received ${wavBytes.size} bytes WAV for: \"$trimmed\"")
            wavBytes
        } else {
            val errText = try {
                conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            } catch (_: Exception) { "" }
            throw RuntimeException("TTS Server returned HTTP $code: $errText")
        }
    }

    /**
     * Synthesizes Santali text and plays the audio waveform sequentially.
     */
    fun synthesizeAndPlay(
        text: String,
        onStart: () -> Unit = {},
        onComplete: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            onError("Text is empty")
            return
        }

        Thread {
            try {
                val cleanUrl = serverUrl.trimEnd('/')
                val url = URL("$cleanUrl/synthesize")

                val payload = JSONObject().apply {
                    put("text", trimmed)
                }.toString().toByteArray(Charsets.UTF_8)

                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = READ_TIMEOUT_MS
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "audio/wav")
                }

                conn.outputStream.use { os ->
                    os.write(payload)
                    os.flush()
                }

                val code = conn.responseCode
                if (code == 200) {
                    val wavBytes = conn.inputStream.use { it.readBytes() }
                    if (wavBytes.isNotEmpty()) {
                        mainHandler.post {
                            playWavBytes(wavBytes, onStart, onComplete, onError)
                        }
                    } else {
                        mainHandler.post { onError("Empty audio response from TTS server") }
                    }
                } else {
                    mainHandler.post { onError("TTS Server HTTP $code") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "[TTS ERROR] Synthesis failed: ${e.localizedMessage}", e)
                mainHandler.post { onError("TTS connection error: ${e.localizedMessage}") }
            }
        }.start()
    }

    /**
     * Plays raw WAV bytes using Android MediaPlayer with proper audio attributes.
     */
    private fun playWavBytes(
        wavBytes: ByteArray,
        onStart: () -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        stop()

        try {
            val tempFile = File(context.cacheDir, "temp_santali_utterance.wav")
            FileOutputStream(tempFile).use { fos ->
                fos.write(wavBytes)
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .build()
                )
                setDataSource(tempFile.absolutePath)
                setOnPreparedListener { mp ->
                    isPlayingAudio = true
                    onStart()
                    mp.start()
                    Log.i(TAG, "[TTS PLAYBACK] Audio playback started (${wavBytes.size} bytes)")
                }
                setOnCompletionListener {
                    isPlayingAudio = false
                    Log.i(TAG, "[TTS PLAYBACK] Audio playback completed")
                    stop()
                    onComplete()
                }
                setOnErrorListener { _, what, extra ->
                    isPlayingAudio = false
                    Log.e(TAG, "[TTS PLAYBACK ERROR] MediaPlayer error: what=$what, extra=$extra")
                    stop()
                    onError("MediaPlayer error ($what, $extra)")
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            isPlayingAudio = false
            Log.e(TAG, "[TTS PLAYBACK ERROR] Failed to start playback", e)
            onError("Playback error: ${e.localizedMessage}")
        }
    }

    fun isPlaying(): Boolean = isPlayingAudio

    fun stop() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.reset()
                mp.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        isPlayingAudio = false
    }

    override fun close() {
        stop()
    }
}
