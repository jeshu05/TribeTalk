package org.tribetalk.ui.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tribetalk.audio.AudioRecorder
import org.tribetalk.audio.TribeTalkTtsManager
import org.tribetalk.core.NativePipeline
import org.tribetalk.core.TranslationExchange
import org.tribetalk.core.TribeTalkTranslator
import org.tribetalk.nlp.LiveUtteranceProcessor
import org.tribetalk.nlp.LiveUtteranceQueue
import org.tribetalk.nlp.QueuedUtterance
import org.tribetalk.ui.components.PipelineUiState
import org.tribetalk.voice.LiveVoiceInputManager

/**
 * ViewModel orchestrating continuous live voice classroom translation.
 *
 * Pipeline:
 * Continuous Hindi Microphone -> Live ASR -> Utterance Queue -> TribeTalkTranslator -> Santali TTS Queue -> Audio Playback
 *
 * Guarantees:
 * 1. Sequential end-to-end processing with internal sequence IDs (Utterance 1 -> Utterance 2).
 * 2. Zero overlapping audio output.
 * 3. Continuous microphone listening with feedback suppression during TTS playback.
 * 4. Immediate on-screen display of recognized Hindi text and Santali Ol Chiki translation.
 */
class TranslationViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "TranslationViewModel"
    }

    private val audioRecorder = AudioRecorder()
    private val liveVoiceInputManager = LiveVoiceInputManager(application)
    private val liveProcessor = LiveUtteranceProcessor()
    private val liveUtteranceQueue = LiveUtteranceQueue { item ->
        processSequentialLiveUtterance(item)
    }
    private val ttsManager = TribeTalkTtsManager(application)

    val amplitude = audioRecorder.amplitude
    val isPlayingAudio = ttsManager.isSpeaking

    private val _uiState = MutableStateFlow(PipelineUiState.IDLE)
    val uiState: StateFlow<PipelineUiState> = _uiState.asStateFlow()

    private val _isHindiToSantali = MutableStateFlow(true)
    val isHindiToSantali: StateFlow<Boolean> = _isHindiToSantali.asStateFlow()

    private val _conversations = MutableStateFlow<List<TranslationExchange>>(emptyList())
    val conversations: StateFlow<List<TranslationExchange>> = _conversations.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _textInput = MutableStateFlow("")
    val textInput: StateFlow<String> = _textInput.asStateFlow()

    // Live Voice Streaming States
    private val _currentPartialHindi = MutableStateFlow("")
    val currentPartialHindi: StateFlow<String> = _currentPartialHindi.asStateFlow()

    private val _liveStatusLabel = MutableStateFlow("Idle")
    val liveStatusLabel: StateFlow<String> = _liveStatusLabel.asStateFlow()

    private val _activeUtteranceSequence = MutableStateFlow(0)
    val activeUtteranceSequence: StateFlow<Int> = _activeUtteranceSequence.asStateFlow()

    init {
        // Initialize on-device AI translator
        TribeTalkTranslator.initialize(application)

        // Initialize native pipeline with app storage path
        val assetDir = application.filesDir.absolutePath
        viewModelScope.launch(Dispatchers.IO) {
            NativePipeline.init(assetDir)
        }

        // Start live sequential utterance processing worker
        liveUtteranceQueue.start(viewModelScope)
    }

    fun swapDirection() {
        ttsManager.stop()
        _isHindiToSantali.value = !_isHindiToSantali.value
    }

    fun onTextInputChanged(newText: String) {
        _textInput.value = newText
    }

    /**
     * Toggles the continuous Live Classroom Voice Translation session.
     */
    fun toggleRecording() {
        if (_isRecording.value) {
            stopLiveVoiceSession()
        } else {
            startLiveVoiceSession()
        }
    }

    /**
     * Starts continuous live classroom voice translation.
     */
    fun startLiveVoiceSession() {
        ttsManager.stop()
        _isRecording.value = true
        _uiState.value = PipelineUiState.LISTENING
        _liveStatusLabel.value = "LIVE LISTENING"
        _currentPartialHindi.value = ""
        liveProcessor.reset()
        liveUtteranceQueue.resetSequence()

        val langCode = if (_isHindiToSantali.value) "hi-IN" else "hi-IN"

        liveVoiceInputManager.startContinuousListening(
            languageCode = langCode,
            onPartial = { partialText ->
                _currentPartialHindi.value = partialText
                _liveStatusLabel.value = "Recognizing: $partialText"
                Log.d(TAG, "[LIVE ASR PARTIAL] \"$partialText\"")
            },
            onFinal = { finalRawText ->
                _currentPartialHindi.value = ""
                Log.i(TAG, "[LIVE ASR FINAL] \"$finalRawText\"")
                val processed = liveProcessor.processFinal(finalRawText)
                if (processed != null) {
                    val enqueued = liveUtteranceQueue.enqueue(processed)
                    if (enqueued) {
                        Log.i(TAG, "[LIVE QUEUE] Successfully enqueued utterance: \"$processed\"")
                    }
                } else {
                    Log.w(TAG, "[LIVE ASR] Utterance filtered by NLP confidence check: \"$finalRawText\"")
                }
            },
            onError = { errorMsg ->
                Log.w(TAG, "[LIVE ASR ERROR] $errorMsg")
                _liveStatusLabel.value = errorMsg
            },
            onStateChange = { status ->
                _liveStatusLabel.value = status
            }
        )
    }

    /**
     * Stops continuous live classroom voice translation.
     */
    fun stopLiveVoiceSession() {
        _isRecording.value = false
        _currentPartialHindi.value = ""
        _liveStatusLabel.value = "Idle"
        _uiState.value = PipelineUiState.IDLE
        liveVoiceInputManager.stopContinuousListening()
    }

    /**
     * Sequential utterance worker: executes translation and synchronous TTS playback.
     */
    private suspend fun processSequentialLiveUtterance(item: QueuedUtterance) {
        val t0 = System.currentTimeMillis()
        _activeUtteranceSequence.value = item.sequenceId
        Log.i(TAG, "[LIVE PIPELINE] Utterance #${item.sequenceId} HINDI_FINAL = \"${item.hindiText}\"")

        withContext(Dispatchers.Main) {
            _uiState.value = PipelineUiState.TRANSLATING
            _liveStatusLabel.value = "Translating #${item.sequenceId}..."
        }

        val isHiToSat = _isHindiToSantali.value
        val srcLang = if (isHiToSat) "hi" else "sat"
        val tgtLang = if (isHiToSat) "sat" else "hi"

        // 1. Translate using stable-talk's verified translator
        val translatedText = try {
            TribeTalkTranslator.translate(item.hindiText, isHiToSat)
        } catch (e: Exception) {
            Log.e(TAG, "[LIVE TRANSLATION ERROR] Failed for #${item.sequenceId}: ${e.localizedMessage}", e)
            "Translation unavailable"
        }
        val latency = (System.currentTimeMillis() - t0).toFloat().coerceAtLeast(15f)
        Log.i(TAG, "[LIVE PIPELINE] Utterance #${item.sequenceId} TRANSLATION_COMPLETED (Santali=\"$translatedText\", latency=${latency}ms)")

        val exchange = TranslationExchange(
            sourceText = item.hindiText,
            targetText = translatedText,
            sourceLanguage = srcLang,
            targetLanguage = tgtLang,
            audioSamples = FloatArray(0),
            totalLatencyMs = latency,
            success = translatedText != "Translation unavailable"
        )

        // 2. Display on screen immediately
        withContext(Dispatchers.Main) {
            _conversations.value = listOf(exchange) + _conversations.value
            _uiState.value = PipelineUiState.PLAYING
            _liveStatusLabel.value = "Playing Santali voice (#${item.sequenceId})..."
        }

        // 3. Sequential audio playback with microphone feedback suppression
        if (exchange.success && translatedText.isNotBlank()) {
            liveVoiceInputManager.pauseForPlayback()
            Log.i(TAG, "[LIVE PIPELINE] Utterance #${item.sequenceId} TTS_STARTED")
            try {
                ttsManager.speakSuspend(translatedText, tgtLang)
                Log.i(TAG, "[LIVE PIPELINE] Utterance #${item.sequenceId} TTS_COMPLETED")
            } catch (e: Exception) {
                Log.e(TAG, "[LIVE TTS ERROR] Failed for #${item.sequenceId}: ${e.localizedMessage}", e)
            } finally {
                liveVoiceInputManager.resumeAfterPlayback()
            }
        }

        withContext(Dispatchers.Main) {
            if (_isRecording.value) {
                _uiState.value = PipelineUiState.LISTENING
                _liveStatusLabel.value = "LIVE LISTENING"
            } else {
                _uiState.value = PipelineUiState.IDLE
                _liveStatusLabel.value = "Idle"
            }
        }
    }

    fun translateManualText() {
        val text = _textInput.value.trim()
        if (text.isEmpty()) return

        _textInput.value = ""
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PipelineUiState.TRANSLATING
            val t0 = System.currentTimeMillis()

            val isHiToSat = _isHindiToSantali.value
            val srcLang = if (isHiToSat) "hi" else "sat"
            val tgtLang = if (isHiToSat) "sat" else "hi"

            val translatedText = TribeTalkTranslator.translate(text, isHiToSat)
            val latency = (System.currentTimeMillis() - t0).toFloat().coerceAtLeast(120f)

            val exchange = TranslationExchange(
                sourceText = text,
                targetText = translatedText,
                sourceLanguage = srcLang,
                targetLanguage = tgtLang,
                audioSamples = FloatArray(0),
                totalLatencyMs = latency,
                success = true
            )

            withContext(Dispatchers.Main) {
                _conversations.value = listOf(exchange) + _conversations.value
                _uiState.value = PipelineUiState.PLAYING

                ttsManager.speak(translatedText, tgtLang) {
                    _uiState.value = PipelineUiState.IDLE
                }
            }
        }
    }

    /**
     * Instantly translates a direct prompt (e.g. from classroom command chips) with voice playback.
     */
    fun translateDirect(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PipelineUiState.TRANSLATING
            val t0 = System.currentTimeMillis()

            val isHiToSat = _isHindiToSantali.value
            val srcLang = if (isHiToSat) "hi" else "sat"
            val tgtLang = if (isHiToSat) "sat" else "hi"

            val translatedText = TribeTalkTranslator.translate(trimmed, isHiToSat)
            val latency = (System.currentTimeMillis() - t0).toFloat().coerceAtLeast(10f)

            val exchange = TranslationExchange(
                sourceText = trimmed,
                targetText = translatedText,
                sourceLanguage = srcLang,
                targetLanguage = tgtLang,
                audioSamples = FloatArray(0),
                totalLatencyMs = latency,
                success = true
            )

            withContext(Dispatchers.Main) {
                _conversations.value = listOf(exchange) + _conversations.value
                _uiState.value = PipelineUiState.PLAYING

                ttsManager.speak(translatedText, tgtLang) {
                    _uiState.value = PipelineUiState.IDLE
                }
            }
        }
    }

    fun playAudio(exchange: TranslationExchange) {
        _uiState.value = PipelineUiState.PLAYING
        ttsManager.speak(exchange.targetText, exchange.targetLanguage) {
            _uiState.value = PipelineUiState.IDLE
        }
    }

    fun clearHistory() {
        ttsManager.stop()
        liveVoiceInputManager.stopContinuousListening()
        _conversations.value = emptyList()
        NativePipeline.trimMemory()
    }

    override fun onCleared() {
        super.onCleared()
        liveVoiceInputManager.destroy()
        liveUtteranceQueue.stop()
        ttsManager.shutdown()
        NativePipeline.releaseAll()
    }
}
