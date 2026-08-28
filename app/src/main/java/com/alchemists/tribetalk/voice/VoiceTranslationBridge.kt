package com.alchemists.tribetalk.voice

import com.alchemists.tribetalk.translation.Language
import com.alchemists.tribetalk.translation.TranslationEngine
import com.alchemists.tribetalk.translation.TranslationResult

class VoiceTranslationBridge(
    private val translationEngine: TranslationEngine,
    private val speechOutputManager: SpeechOutputManager
) {
    enum class State {
        Idle,
        Listening,
        Recognized,
        Translating,
        TranslationComplete,
        Speaking,
        Error
    }

    fun translateAndSpeak(
        recognizedText: String,
        sourceLanguage: Language,
        targetLanguage: Language,
        isVoiceBridgeEnabled: Boolean,
        onStateChange: (State, String) -> Unit,
        onResult: (TranslationResult) -> Unit
    ) {
        if (recognizedText.trim().isEmpty()) {
            onStateChange(State.Idle, "")
            return
        }

        onStateChange(State.Translating, "Translating...")

        try {
            val result = translationEngine.translate(recognizedText, sourceLanguage, targetLanguage)
            onResult(result)
            onStateChange(State.TranslationComplete, "Translation Complete")

            if (isVoiceBridgeEnabled) {
                val targetLocaleCode = when (targetLanguage) {
                    Language.HINDI -> "hi"
                    Language.SANTALI -> "sat"
                }

                val isAvailable = speechOutputManager.isLanguageAvailable(targetLocaleCode)
                if (isAvailable) {
                    onStateChange(State.Speaking, "Playing audio...")
                    speechOutputManager.speak(
                        text = result.translatedText,
                        languageCode = targetLocaleCode,
                        onStart = {
                            onStateChange(State.Speaking, "Speaking...")
                        },
                        onDone = {
                            onStateChange(State.TranslationComplete, "Translation Complete")
                        },
                        onError = { err ->
                            onStateChange(State.Error, "Audio: $err")
                        }
                    )
                } else {
                    val label = if (targetLanguage == Language.SANTALI) "Santali" else "Hindi"
                    onStateChange(State.Error, "$label voice unavailable on this device")
                }
            }
        } catch (e: Exception) {
            onStateChange(State.Error, "Error: ${e.localizedMessage}")
        }
    }
}
