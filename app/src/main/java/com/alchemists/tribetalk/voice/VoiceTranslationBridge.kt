package com.alchemists.tribetalk.voice

import com.alchemists.tribetalk.nlp.HindiNlpProcessor
import com.alchemists.tribetalk.nlp.LatencyTracker
import com.alchemists.tribetalk.translation.Language
import com.alchemists.tribetalk.translation.TranslationEngine
import com.alchemists.tribetalk.translation.TranslationResult

class VoiceTranslationBridge(
    private val translationEngine: TranslationEngine,
    private val speechOutputManager: SpeechOutputManager,
    private val neuralSynthesizer: NeuralSpeechSynthesizer? = null
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
        val latencyTracker = LatencyTracker().apply {
            markAsrFinal()
        }

        if (recognizedText.trim().isEmpty()) {
            onStateChange(State.Idle, "")
            return
        }

        // 1. NLP Processing Layer (Deduplication, Normalization, Segmentation, Confidence)
        val textToTranslate: String
        if (sourceLanguage == Language.HINDI) {
            val nlpResult = HindiNlpProcessor.process(recognizedText)
            latencyTracker.markNlpReady()

            if (!nlpResult.isValid) {
                onStateChange(State.Error, nlpResult.feedbackMessage ?: "कृपया पुनः बोलें (Please repeat)")
                return
            }
            textToTranslate = nlpResult.normalizedText
        } else {
            textToTranslate = recognizedText.trim().replace("\\s+".toRegex(), " ")
            latencyTracker.markNlpReady()
        }

        // 2. Translation Layer
        latencyTracker.markTranslationStart()
        onStateChange(State.Translating, "Translating...")

        try {
            val result = translationEngine.translate(textToTranslate, sourceLanguage, targetLanguage)
            latencyTracker.markTranslationEnd()
            onResult(result)
            onStateChange(State.TranslationComplete, "Translation Complete")

            // 3. TTS Synthesis & Playback Layer
            if (isVoiceBridgeEnabled) {
                val targetLocaleCode = when (targetLanguage) {
                    Language.HINDI -> "hi"
                    Language.SANTALI -> "sat"
                }

                latencyTracker.markTtsStart()
                val isAvailable = speechOutputManager.isLanguageAvailable(targetLocaleCode)
                if (isAvailable) {
                    onStateChange(State.Speaking, "Playing audio...")
                    speechOutputManager.speak(
                        text = result.translatedText,
                        languageCode = targetLocaleCode,
                        onStart = {
                            latencyTracker.markTtsResponse()
                            latencyTracker.markPlaybackStart()
                            latencyTracker.computeMetrics()
                            onStateChange(State.Speaking, "Speaking...")
                        },
                        onDone = {
                            onStateChange(State.TranslationComplete, "Translation Complete")
                        },
                        onError = {
                            // Fallback to neural synthesizer
                            if (neuralSynthesizer != null) {
                                neuralSynthesizer.speak(
                                    text = result.translatedText,
                                    languageCode = targetLocaleCode,
                                    onStart = {
                                        latencyTracker.markTtsResponse()
                                        latencyTracker.markPlaybackStart()
                                        latencyTracker.computeMetrics()
                                        onStateChange(State.Speaking, "Speaking (Neural TTS)...")
                                    },
                                    onDone = { onStateChange(State.TranslationComplete, "Translation Complete") },
                                    onError = { onStateChange(State.TranslationComplete, "Translation Complete (HUD Ready)") }
                                )
                            } else {
                                latencyTracker.computeMetrics()
                                onStateChange(State.TranslationComplete, "Translation Complete (HUD Ready)")
                            }
                        }
                    )
                } else if (neuralSynthesizer != null) {
                    onStateChange(State.Speaking, "Synthesizing audio (Neural TTS)...")
                    neuralSynthesizer.speak(
                        text = result.translatedText,
                        languageCode = targetLocaleCode,
                        onStart = {
                            latencyTracker.markTtsResponse()
                            latencyTracker.markPlaybackStart()
                            latencyTracker.computeMetrics()
                            onStateChange(State.Speaking, "Speaking (Neural TTS)...")
                        },
                        onDone = {
                            onStateChange(State.TranslationComplete, "Translation Complete")
                        },
                        onError = {
                            latencyTracker.computeMetrics()
                            onStateChange(State.TranslationComplete, "Translation Complete (HUD Ready)")
                        }
                    )
                } else {
                    latencyTracker.computeMetrics()
                    onStateChange(State.TranslationComplete, "Translation Complete (HUD Ready)")
                }
            } else {
                latencyTracker.computeMetrics()
            }
        } catch (e: Exception) {
            onStateChange(State.Error, "Error: ${e.localizedMessage}")
        }
    }
}
