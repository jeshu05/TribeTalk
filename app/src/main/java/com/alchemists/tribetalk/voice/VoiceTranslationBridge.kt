package com.alchemists.tribetalk.voice

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.alchemists.tribetalk.nlp.HindiNlpProcessor
import com.alchemists.tribetalk.nlp.LatencyMetrics
import com.alchemists.tribetalk.nlp.LatencyTracker
import com.alchemists.tribetalk.nlp.LiveUtteranceProcessor
import com.alchemists.tribetalk.nlp.LiveUtteranceQueue
import com.alchemists.tribetalk.nlp.NlpProcessedUtterance
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
    var voiceInputManager: VoiceInputManager? = null,
    private val context: Context? = null
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
    private var santaliAudioQueue: SantaliAudioQueue? = null
    private var liveScope: CoroutineScope? = null
    private var isLiveSessionActive = false

    var lastMeasuredLatency: LatencyMetrics? = null
        private set

    /**
     * Starts continuous Live Voice session with pipelined utterance & audio queue processing.
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

        val ctx = context
        if (ctx != null) {
            santaliAudioQueue = SantaliAudioQueue(ctx).apply {
                start(scope)
            }
        }

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
     * Process an utterance: NLP Normalize -> Translate -> Synthesize TTS -> Enqueue to Audio Queue.
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
        Log.i("VOICE_PIPELINE", "[VOICE_PIPELINE] HINDI_FINAL = \"$rawText\"")
        Log.i("VoiceIntegration", "HINDI_RESULT = \"$rawText\"")

        // 1. NLP Processing
        onStateChange(State.Processing, "Processing...")
        val nlpResult = HindiNlpProcessor.process(rawText)
        tracker.markNlpReady()

        if (!nlpResult.isValid) {
            onStateChange(State.Error, nlpResult.feedbackMessage ?: "कृपया पुनः बोलें")
            if (isLiveSessionActive) {
                delay(800)
                Log.i("VoiceIntegration", "RETURNING_TO_LISTENING")
                onStateChange(State.Listening, "LIVE LISTENING")
            }
            return@withContext
        }

        // 2. Translation
        tracker.markTranslationStart()
        Log.i("VOICE_PIPELINE", "[VOICE_PIPELINE] TRANSLATION_STARTED (text=\"${nlpResult.normalizedText}\")")
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
                Log.i("VoiceIntegration", "RETURNING_TO_LISTENING")
                onStateChange(State.Listening, "LIVE LISTENING")
            }
            return@withContext
        }

        tracker.markTranslationEnd()
        Log.i("VOICE_PIPELINE", "[VOICE_PIPELINE] SANTALI_RESULT = \"${translationResult.translatedText}\" (${translationResult.latinPhonetic})")
        Log.i("VoiceIntegration", "TRANSLATION_RESULT = \"${translationResult.translatedText}\" (${translationResult.latinPhonetic})")

        // 3. TTS Speech Synthesis via SpeechOutputManager (matching Play Button path)
        tracker.markTtsStart()
        Log.i(TAG, "BRIDGE_TTS_PROVIDER=SpeechOutputManager")
        Log.i(TAG, "BRIDGE_TTS_LANGUAGE=sat")
        Log.i(TAG, "BRIDGE_TTS_REQUEST=${translationResult.translatedText}")
        Log.i("VoiceIntegration", "BRIDGE_TTS_PROVIDER=SpeechOutputManager")
        Log.i("VoiceIntegration", "BRIDGE_TTS_LANGUAGE=sat")
        Log.i("VoiceIntegration", "BRIDGE_TTS_REQUEST=${translationResult.translatedText}")
        Log.i("VoiceIntegration", "SANTALI_TTS_REQUEST = \"${translationResult.translatedText}\"")

        val isSupported = speechOutputManager.isLanguageAvailable("sat")
        if (!isSupported) {
            val errMsg = "Santali voice unavailable on this device"
            Log.e(TAG, "BRIDGE_TTS_ERROR=$errMsg")
            Log.e("VoiceIntegration", "BRIDGE_TTS_ERROR=$errMsg")
            onStateChange(State.Error, errMsg)
            if (isLiveSessionActive) {
                delay(800)
                Log.i("VoiceIntegration", "RETURNING_TO_LISTENING")
                onStateChange(State.Listening, "LIVE LISTENING")
            }
            return@withContext
        }

        onStateChange(State.GeneratingSantaliSpeech, "Generating Santali voice...")

        suspendCancellableCoroutine<Unit> { cont ->
            cont.invokeOnCancellation {
                speechOutputManager.stop()
                voiceInputManager?.resumeAfterPlayback()
            }

            speechOutputManager.speak(
                text = translationResult.translatedText,
                languageCode = "sat",
                onStart = {
                    tracker.markTtsResponse()
                    tracker.markPlaybackStart()
                    lastMeasuredLatency = tracker.computeMetrics()
                    tracker.logLiveLatency(nlpResult.normalizedText)
                    Log.i(TAG, "BRIDGE_TTS_PLAYBACK_START")
                    Log.i("VoiceIntegration", "BRIDGE_TTS_PLAYBACK_START")
                    Log.i("VoiceIntegration", "TTS_AUDIO_RECEIVED")
                    Log.i("VoiceIntegration", "PLAYBACK_STARTED")
                    voiceInputManager?.pauseForPlayback()
                    onStateChange(State.Speaking, "Playing Santali")
                    onUtteranceResult(nlpResult.normalizedText, translationResult)
                },
                onDone = {
                    Log.i(TAG, "BRIDGE_TTS_PLAYBACK_COMPLETE")
                    Log.i("VoiceIntegration", "BRIDGE_TTS_PLAYBACK_COMPLETE")
                    Log.i("VoiceIntegration", "PLAYBACK_COMPLETED")
                    voiceInputManager?.resumeAfterPlayback()
                    onStateChange(State.TranslationComplete, "Translation Complete")
                    if (isLiveSessionActive) {
                        mainHandler.postDelayed({
                            if (isLiveSessionActive) {
                                Log.i("VoiceIntegration", "RETURNING_TO_LISTENING")
                                onStateChange(State.Listening, "LIVE LISTENING")
                            }
                        }, 250)
                    }
                    if (cont.isActive) {
                        cont.resume(Unit)
                    }
                },
                onError = { err ->
                    Log.e(TAG, "BRIDGE_TTS_ERROR=$err")
                    Log.e("VoiceIntegration", "BRIDGE_TTS_ERROR=$err")
                    voiceInputManager?.resumeAfterPlayback()
                    onStateChange(State.Error, "Audio: $err")
                    if (isLiveSessionActive) {
                        mainHandler.postDelayed({
                            if (isLiveSessionActive) {
                                Log.i("VoiceIntegration", "RETURNING_TO_LISTENING")
                                onStateChange(State.Listening, "LIVE LISTENING")
                            }
                        }, 500)
                    }
                    if (cont.isActive) {
                        cont.resume(Unit)
                    }
                }
            )
        }
    }

    fun stopLiveVoiceSession() {
        isLiveSessionActive = false
        speechOutputManager.stop()
        voiceInputManager?.resumeAfterPlayback()
        liveQueue?.stop()
        liveQueue = null
        santaliAudioQueue?.stop()
        santaliAudioQueue = null
        liveScope?.cancel()
        liveScope = null
        liveProcessor.reset()
        Log.i(TAG, "[LIVE SESSION] Live Voice Bridge session stopped")
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
            textToTranslate = recognizedText.trim()
            latencyTracker.markNlpReady()
        }

        onStateChange(State.Translating, "Translating...")
        latencyTracker.markTranslationStart()

        val translationResult = try {
            translationEngine.translate(textToTranslate, sourceLanguage, targetLanguage)
        } catch (e: Exception) {
            Log.e(TAG, "[TRANSLATION ERROR] Failed: ${e.localizedMessage}")
            onStateChange(State.Error, "Translation unavailable")
            return
        }

        latencyTracker.markTranslationEnd()
        onResult(translationResult)

        if (!isVoiceBridgeEnabled) {
            lastMeasuredLatency = latencyTracker.computeMetrics()
            latencyTracker.logLiveLatency(textToTranslate)
            onStateChange(State.TranslationComplete, "Translation complete")
            return
        }

        // 2. TTS Voice Output
        latencyTracker.markTtsStart()
        if (targetLanguage == Language.SANTALI && realSantaliTTSProvider != null) {
            onStateChange(State.GeneratingSantaliSpeech, "Generating Santali voice...")
            realSantaliTTSProvider?.synthesizeAndPlay(
                text = translationResult.translatedText,
                onStart = {
                    latencyTracker.markTtsResponse()
                    latencyTracker.markPlaybackStart()
                    lastMeasuredLatency = latencyTracker.computeMetrics()
                    latencyTracker.logLiveLatency(textToTranslate)
                    onStateChange(State.Speaking, "Playing Santali")
                },
                onComplete = {
                    onStateChange(State.TranslationComplete, "Translation complete")
                },
                onError = { err ->
                    Log.w(TAG, "[TTS FALLBACK] Real TTS error: $err, falling back to on-device engine")
                    speakViaNeuralSynthesizer(translationResult, targetLanguage, latencyTracker, textToTranslate, onStateChange)
                }
            )
        } else {
            speakViaNeuralSynthesizer(translationResult, targetLanguage, latencyTracker, textToTranslate, onStateChange)
        }
    }

    private fun speakViaNeuralSynthesizer(
        translationResult: TranslationResult,
        targetLanguage: Language,
        latencyTracker: LatencyTracker,
        textToTranslate: String,
        onStateChange: (State, String) -> Unit
    ) {
        val targetCode = if (targetLanguage == Language.HINDI) "hi" else "sat"
        if (neuralSynthesizer != null) {
            neuralSynthesizer.speak(
                text = translationResult.translatedText,
                languageCode = targetCode,
                onStart = {
                    latencyTracker.markTtsResponse()
                    latencyTracker.markPlaybackStart()
                    lastMeasuredLatency = latencyTracker.computeMetrics()
                    latencyTracker.logLiveLatency(textToTranslate)
                    onStateChange(State.Speaking, "Playing Santali (Neural)")
                },
                onDone = {
                    onStateChange(State.TranslationComplete, "Translation complete")
                },
                onError = {
                    speechOutputManager.speak(
                        text = translationResult.translatedText,
                        languageCode = targetCode,
                        onStart = {
                            latencyTracker.markTtsResponse()
                            latencyTracker.markPlaybackStart()
                            lastMeasuredLatency = latencyTracker.computeMetrics()
                            latencyTracker.logLiveLatency(textToTranslate)
                            onStateChange(State.Speaking, "Speaking...")
                        },
                        onDone = {
                            onStateChange(State.TranslationComplete, "Translation complete")
                        },
                        onError = {
                            onStateChange(State.Error, "Speech output failed")
                        }
                    )
                }
            )
        } else {
            speechOutputManager.speak(
                text = translationResult.translatedText,
                languageCode = targetCode,
                onStart = {
                    latencyTracker.markTtsResponse()
                    latencyTracker.markPlaybackStart()
                    lastMeasuredLatency = latencyTracker.computeMetrics()
                    latencyTracker.logLiveLatency(textToTranslate)
                    onStateChange(State.Speaking, "Speaking...")
                },
                onDone = {
                    onStateChange(State.TranslationComplete, "Translation complete")
                },
                onError = {
                    onStateChange(State.Error, "Speech output failed")
                }
            )
        }
    }
}
