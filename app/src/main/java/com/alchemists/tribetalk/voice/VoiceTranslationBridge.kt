package com.alchemists.tribetalk.voice

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.alchemists.tribetalk.nlp.HindiNlpProcessor
import com.alchemists.tribetalk.nlp.LatencyMetrics
import com.alchemists.tribetalk.nlp.LatencyTracker
import com.alchemists.tribetalk.nlp.LiveUtteranceProcessor
import com.alchemists.tribetalk.nlp.LiveUtteranceQueue
import com.alchemists.tribetalk.nlp.QueuedUtterance
import com.alchemists.tribetalk.translation.Language
import com.alchemists.tribetalk.translation.TranslationEngine
import com.alchemists.tribetalk.translation.TranslationResult
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class VoiceTranslationBridge(
    private val translationEngine: TranslationEngine,
    private val speechOutputManager: SpeechOutputManager,
    private val neuralSynthesizer: NeuralSpeechSynthesizer? = null,
    var realSantaliTTSProvider: RealSantaliTTSProvider? = null,
    var voiceInputManager: VoiceInputManager? = null
) {
    companion object {
        private const val TAG = "VoiceTranslationBridge"
    }

    enum class State {
        Idle,
        Listening,
        Recognized,
        Recognizing,
        Processing,
        Translating,
        GeneratingSantaliSpeech,
        Speaking,
        TranslationComplete,
        Error
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val liveProcessor = LiveUtteranceProcessor()
    private var liveQueue: LiveUtteranceQueue? = null
    private var liveScope: CoroutineScope? = null
    private var isLiveSessionActive = false

    var lastMeasuredLatency: LatencyMetrics? = null
        private set

    /**
     * Starts continuous Live Voice session with sequential utterance queue processing.
     */
    fun startLiveVoiceSession(
        onStateChange: (State, String) -> Unit,
        onUtteranceResult: (String, TranslationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        stopLiveVoiceSession()
        isLiveSessionActive = true
        liveProcessor.reset()

        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        liveScope = scope

        liveQueue = LiveUtteranceQueue { item ->
            processSequentialUtterance(
                queuedItem = item,
                onStateChange = onStateChange,
                onUtteranceResult = onUtteranceResult,
                onError = onError
            )
        }.apply {
            start(scope)
        }

        onStateChange(State.Listening, "LIVE LISTENING")
        Log.i(TAG, "[LIVE SESSION] Live Voice Bridge session started")
    }

    /**
     * Enqueues an incoming live utterance recognized from continuous microphone stream.
     */
    fun enqueueLiveUtterance(rawAsrText: String) {
        val processed = liveProcessor.processFinal(rawAsrText)
        if (processed != null) {
            liveQueue?.enqueue(processed)
        }
    }

    /**
     * Handles streaming partial results for boundary detection without waiting for full silence.
     */
    fun handleLivePartial(partialAsrText: String) {
        val segmented = liveProcessor.processPartial(partialAsrText)
        if (segmented != null) {
            liveQueue?.enqueue(segmented)
        }
    }

    /**
     * Process an utterance strictly sequentially (Translate -> Synthesize -> Play audio).
     * Suspends until the audio has completed playing before accepting the next queued item.
     */
    private suspend fun processSequentialUtterance(
        queuedItem: QueuedUtterance,
        onStateChange: (State, String) -> Unit,
        onUtteranceResult: (String, TranslationResult) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.Main) {
        val tracker = LatencyTracker().apply {
            markAsrFinal(queuedItem.timestampMs)
        }

        val rawText = queuedItem.hindiText
        Log.i(TAG, "[LIVE PIPELINE START] Processing: \"$rawText\"")

        // 1. NLP Processing
        onStateChange(State.Processing, "Processing...")
        val nlpResult = HindiNlpProcessor.process(rawText)
        tracker.markNlpReady()

        if (!nlpResult.isValid) {
            onStateChange(State.Error, nlpResult.feedbackMessage ?: "कृपया पुनः बोलें")
            if (isLiveSessionActive) {
                delay(800)
                onStateChange(State.Listening, "LIVE LISTENING")
            }
            return@withContext
        }

        // 2. Translation
        tracker.markTranslationStart()
        onStateChange(State.Translating, "Translating...")
        val translationResult = try {
            withContext(Dispatchers.IO) {
                translationEngine.translate(nlpResult.normalizedText, Language.HINDI, Language.SANTALI)
            }
        } catch (e: Exception) {
            Log.e(TAG, "[TRANSLATION ERROR] Failed: ${e.localizedMessage}")
            onStateChange(State.Error, "Translation unavailable")
            if (isLiveSessionActive) {
                delay(800)
                onStateChange(State.Listening, "LIVE LISTENING")
            }
            return@withContext
        }

        tracker.markTranslationEnd()
        onUtteranceResult(nlpResult.normalizedText, translationResult)

        // 3. TTS Speech Synthesis & Sequential Playback
        tracker.markTtsStart()
        onStateChange(State.GeneratingSantaliSpeech, "Generating Santali voice...")

        // Mute mic during speaker output to prevent acoustic feedback loop
        voiceInputManager?.pauseForPlayback()

        try {
            suspendCoroutine<Unit> { continuation ->
                val realTts = realSantaliTTSProvider
                if (realTts != null) {
                    realTts.synthesizeAndPlay(
                        text = translationResult.translatedText,
                        onStart = {
                            tracker.markTtsResponse()
                            tracker.markPlaybackStart()
                            lastMeasuredLatency = tracker.computeMetrics()
                            onStateChange(State.Speaking, "Playing Santali")
                        },
                        onComplete = {
                            voiceInputManager?.resumeAfterPlayback()
                            onStateChange(State.TranslationComplete, "Translation Complete")
                            continuation.resume(Unit)
                        },
                        onError = { err ->
                            Log.w(TAG, "[TTS FALLBACK] Real TTS error: $err, falling back to on-device engine")
                            // Fallback to neural synthesizer
                            neuralSynthesizer?.speak(
                                text = translationResult.translatedText,
                                languageCode = "sat",
                                onStart = {
                                    tracker.markTtsResponse()
                                    tracker.markPlaybackStart()
                                    lastMeasuredLatency = tracker.computeMetrics()
                                    onStateChange(State.Speaking, "Playing Santali (Neural)")
                                },
                                onDone = {
                                    voiceInputManager?.resumeAfterPlayback()
                                    continuation.resume(Unit)
                                },
                                onError = {
                                    voiceInputManager?.resumeAfterPlayback()
                                    continuation.resume(Unit)
                                }
                            ) ?: run {
                                voiceInputManager?.resumeAfterPlayback()
                                continuation.resume(Unit)
                            }
                        }
                    )
                } else if (neuralSynthesizer != null) {
                    neuralSynthesizer.speak(
                        text = translationResult.translatedText,
                        languageCode = "sat",
                        onStart = {
                            tracker.markTtsResponse()
                            tracker.markPlaybackStart()
                            lastMeasuredLatency = tracker.computeMetrics()
                            onStateChange(State.Speaking, "Playing Santali")
                        },
                        onDone = {
                            voiceInputManager?.resumeAfterPlayback()
                            continuation.resume(Unit)
                        },
                        onError = {
                            voiceInputManager?.resumeAfterPlayback()
                            continuation.resume(Unit)
                        }
                    )
                } else {
                    voiceInputManager?.resumeAfterPlayback()
                    tracker.computeMetrics()
                    continuation.resume(Unit)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "[AUDIO ERROR] Playback failed: ${e.localizedMessage}")
            voiceInputManager?.resumeAfterPlayback()
        }

        // 4. Return to Live Listening automatically
        if (isLiveSessionActive) {
            delay(150)
            onStateChange(State.Listening, "LIVE LISTENING")
        }
    }

    /**
     * Single-shot translation method for manual triggers / standard HUD operations.
     */
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

        // 1. NLP Processing Layer
        val textToTranslate: String
        if (sourceLanguage == Language.HINDI) {
            val nlpResult = HindiNlpProcessor.process(recognizedText)
            latencyTracker.markNlpReady()

            if (!nlpResult.isValid) {
                onStateChange(State.Error, nlpResult.feedbackMessage ?: "कृपया पुनः बोलें")
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

            // 3. TTS Layer
            if (isVoiceBridgeEnabled) {
                val targetLocaleCode = when (targetLanguage) {
                    Language.HINDI -> "hi"
                    Language.SANTALI -> "sat"
                }

                latencyTracker.markTtsStart()
                if (targetLanguage == Language.SANTALI && realSantaliTTSProvider != null) {
                    onStateChange(State.GeneratingSantaliSpeech, "Generating Santali voice...")
                    voiceInputManager?.pauseForPlayback()
                    realSantaliTTSProvider?.synthesizeAndPlay(
                        text = result.translatedText,
                        onStart = {
                            latencyTracker.markTtsResponse()
                            latencyTracker.markPlaybackStart()
                            lastMeasuredLatency = latencyTracker.computeMetrics()
                            onStateChange(State.Speaking, "Playing Santali")
                        },
                        onComplete = {
                            voiceInputManager?.resumeAfterPlayback()
                            onStateChange(State.TranslationComplete, "Translation Complete")
                        },
                        onError = {
                            voiceInputManager?.resumeAfterPlayback()
                            onStateChange(State.TranslationComplete, "Translation Complete (HUD Ready)")
                        }
                    )
                } else if (speechOutputManager.isLanguageAvailable(targetLocaleCode)) {
                    voiceInputManager?.pauseForPlayback()
                    speechOutputManager.speak(
                        text = result.translatedText,
                        languageCode = targetLocaleCode,
                        onStart = {
                            latencyTracker.markTtsResponse()
                            latencyTracker.markPlaybackStart()
                            lastMeasuredLatency = latencyTracker.computeMetrics()
                            onStateChange(State.Speaking, "Playing audio...")
                        },
                        onDone = {
                            voiceInputManager?.resumeAfterPlayback()
                            onStateChange(State.TranslationComplete, "Translation Complete")
                        },
                        onError = {
                            voiceInputManager?.resumeAfterPlayback()
                            onStateChange(State.TranslationComplete, "Translation Complete (HUD Ready)")
                        }
                    )
                } else if (neuralSynthesizer != null) {
                    voiceInputManager?.pauseForPlayback()
                    neuralSynthesizer.speak(
                        text = result.translatedText,
                        languageCode = targetLocaleCode,
                        onStart = {
                            latencyTracker.markTtsResponse()
                            latencyTracker.markPlaybackStart()
                            lastMeasuredLatency = latencyTracker.computeMetrics()
                            onStateChange(State.Speaking, "Playing Santali (Neural)")
                        },
                        onDone = {
                            voiceInputManager?.resumeAfterPlayback()
                            onStateChange(State.TranslationComplete, "Translation Complete")
                        },
                        onError = {
                            voiceInputManager?.resumeAfterPlayback()
                            onStateChange(State.TranslationComplete, "Translation Complete (HUD Ready)")
                        }
                    )
                } else {
                    latencyTracker.computeMetrics()
                    onStateChange(State.TranslationComplete, "Translation Complete (HUD Ready)")
                }
            } else {
                lastMeasuredLatency = latencyTracker.computeMetrics()
            }
        } catch (e: Exception) {
            onStateChange(State.Error, "Error: ${e.localizedMessage}")
        }
    }

    /**
     * Cleanly stops live voice session, terminates worker coroutines, and resets audio.
     */
    fun stopLiveVoiceSession() {
        Log.i(TAG, "[LIVE SESSION] Stopping Live Voice Bridge session")
        isLiveSessionActive = false
        voiceInputManager?.stopContinuousListening()
        voiceInputManager?.resumeAfterPlayback()
        realSantaliTTSProvider?.stop()
        neuralSynthesizer?.stop()
        speechOutputManager.stop()
        liveQueue?.stop()
        liveQueue = null
        liveScope?.cancel()
        liveScope = null
        liveProcessor.reset()
    }

    fun isLiveActive(): Boolean = isLiveSessionActive
}
