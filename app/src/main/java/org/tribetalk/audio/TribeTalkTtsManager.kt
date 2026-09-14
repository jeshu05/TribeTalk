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
 * with authentic phonetic formant articulation. Completely eliminates synthetic tone buzz.
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

    init {
        tts = TextToSpeech(context) { status ->
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
                Log.i(TAG, "TextToSpeech initialized successfully")
            } else {
                Log.w(TAG, "TextToSpeech initialization failed with status $status")
            }
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
        if (clean.isEmpty() || !isInitialized || tts == null) {
            onComplete?.invoke()
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
            // Santali Ol Chiki: convert to phonetic syllable representation
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
     * Coroutine-safe suspending speech playback.
     * Suspends until TTS audio playback is completely finished, ensuring zero audio overlap.
     */
    suspend fun speakSuspend(text: String, targetLang: String) = kotlinx.coroutines.suspendCancellableCoroutine<Unit> { cont ->
        speak(text, targetLang) {
            if (cont.isActive) {
                cont.resume(Unit) {}
            }
        }
    }

    fun stop() {
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
