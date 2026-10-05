package org.tribetalk.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.tribetalk.core.TribeTalkTranslator
import java.util.Locale

/**
 * Natural voice speech synthesis manager for TribeTalk.
 * Speaks translated Hindi natively via Android TTS and pronounces Santali (Ol Chiki)
 * with authentic phonetic formant articulation via Google Hindi TTS.
 * Restores the high-quality pipeline from e49119f6085f38516408ad69a6e1543fe4dfc0c6.
 */
class TribeTalkTtsManager(context: Context) {
    companion object {
        private const val TAG = "TribeTalkTtsManager"
    }

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var currentCallback: (() -> Unit)? = null
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    private var pendingRequest: PendingSpeechRequest? = null

    private data class PendingSpeechRequest(
        val text: String,
        val targetLang: String,
        val onComplete: (() -> Unit)?
    )

    init {
        initTts(context.applicationContext)
    }

    private fun initTts(appContext: Context) {
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.setPitch(1.0f)
                tts?.setSpeechRate(0.95f)

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        mainHandler.post {
                            currentCallback?.invoke()
                            currentCallback = null
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        mainHandler.post {
                            currentCallback?.invoke()
                            currentCallback = null
                        }
                    }
                })
                Log.i(TAG, "TextToSpeech initialized successfully (high quality pipeline)")

                // Play any speech queued during startup
                mainHandler.post {
                    pendingRequest?.let { req ->
                        pendingRequest = null
                        speak(req.text, req.targetLang, req.onComplete)
                    }
                }
            } else {
                Log.w(TAG, "TextToSpeech initialization returned status $status")
            }
        }

        // Try Google TTS engine first for high quality neural voice; fallback to default
        tts = try {
            TextToSpeech(appContext, listener, "com.google.android.tts")
        } catch (_: Throwable) {
            TextToSpeech(appContext, listener)
        }
    }

    /**
     * Speaks out translated text.
     * @param text Vernacular text (Devanagari or Ol Chiki).
     * @param targetLang "hi" or "sat".
     * @param onComplete Callback invoked when speech finishes.
     */
    fun speak(text: String, targetLang: String, onComplete: (() -> Unit)? = null) {
        val clean = text.trim()
        if (clean.isEmpty()) {
            onComplete?.invoke()
            return
        }

        if (!isInitialized || tts == null) {
            Log.i(TAG, "TTS initializing; queuing utterance: ${clean.take(25)}")
            pendingRequest = PendingSpeechRequest(clean, targetLang, onComplete)
            return
        }

        currentCallback = onComplete
        val utteranceId = "TribeTalk_${System.currentTimeMillis()}"

        if (targetLang == "hi") {
            val result = tts?.setLanguage(Locale("hi", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            _isSpeaking.value = true
            tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            // Santali Ol Chiki: convert to authentic phonetic syllable representation
            val phoneticText = TribeTalkTranslator.olChikiToSpeechPhonetics(clean)
            val result = tts?.setLanguage(Locale("hi", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            _isSpeaking.value = true
            tts?.speak(phoneticText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    /**
     * Speaks Santali phonetic text with authentic pronunciation.
     */
    fun speakSantaliPhonetic(phoneticText: String, onComplete: (() -> Unit)? = null) {
        speak(phoneticText, "sat", onComplete)
    }

    fun stop() {
        pendingRequest = null
        _isSpeaking.value = false
        mainHandler.post {
            currentCallback?.invoke()
            currentCallback = null
        }
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
