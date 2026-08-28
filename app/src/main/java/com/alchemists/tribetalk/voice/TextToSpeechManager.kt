package com.alchemists.tribetalk.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TextToSpeechManager(context: Context, private val onInitComplete: (Boolean) -> Unit) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                onInitComplete(true)
            } else {
                onInitComplete(false)
            }
        }
    }

    fun isLanguageSupported(languageCode: String): Boolean {
        if (!isInitialized) return false
        val locale = Locale(languageCode)
        val result = tts?.setLanguage(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
        return !(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
    }

    fun speak(
        text: String,
        languageCode: String,
        onStart: () -> Unit,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isInitialized) {
            onError("TTS engine is not initialized yet")
            return
        }

        val locale = Locale(languageCode)
        val checkLang = tts?.setLanguage(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED

        if (checkLang == TextToSpeech.LANG_MISSING_DATA || checkLang == TextToSpeech.LANG_NOT_SUPPORTED) {
            onError("Language voice unavailable on this device")
            return
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onStart()
            }

            override fun onDone(utteranceId: String?) {
                onDone()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onError("TTS playback error")
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                onError("TTS error code: $errorCode")
            }
        })

        val params = android.os.Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "TribeTalkTTS")
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "TribeTalkTTS")
    }

    fun stop() {
        tts?.stop()
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
