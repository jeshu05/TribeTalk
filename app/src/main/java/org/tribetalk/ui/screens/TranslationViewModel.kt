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
import org.tribetalk.audio.AndroidSpeechRecognizer
import org.tribetalk.audio.AudioRecorder
import org.tribetalk.audio.OnnxConformerAsr
import org.tribetalk.audio.TribeTalkTtsManager
import org.tribetalk.core.NativePipeline
import org.tribetalk.core.TranslationExchange
import org.tribetalk.core.TribeTalkTranslator
import org.tribetalk.ui.components.PipelineUiState

class TranslationViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "TranslationViewModel"
    }

    private val audioRecorder = AudioRecorder()
    private val onnxConformerAsr = OnnxConformerAsr(application)
    private val speechRecognizer = AndroidSpeechRecognizer(application)
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

    init {
        // Initialize on-device AI translator
        TribeTalkTranslator.initialize(application)

        // Initialize native pipeline with app storage path
        val assetDir = application.filesDir.absolutePath
        viewModelScope.launch(Dispatchers.IO) {
            NativePipeline.init(assetDir)
        }
    }

    fun swapDirection() {
        ttsManager.stop()
        _isHindiToSantali.value = !_isHindiToSantali.value
    }

    fun onTextInputChanged(newText: String) {
        _textInput.value = newText
    }

    fun toggleRecording() {
        if (_isRecording.value) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        ttsManager.stop()
        _isRecording.value = true
        _uiState.value = PipelineUiState.LISTENING

        val started = audioRecorder.start { audioSamples: FloatArray ->
            _isRecording.value = false
            _uiState.value = PipelineUiState.TRANSCRIBING

            viewModelScope.launch(Dispatchers.IO) {
                val isHi = _isHindiToSantali.value
                val recognized = onnxConformerAsr.transcribe(audioSamples, isHi)
                if (recognized.isNotBlank()) {
                    processRecognizedSpeech(recognized)
                } else {
                    Log.i(TAG, "Audio recorded: ${audioSamples.size} samples, transcribed silence or empty.")
                    withContext(Dispatchers.Main) {
                        _uiState.value = PipelineUiState.IDLE
                    }
                }
            }
        }

        if (!started) {
            _isRecording.value = false
            _uiState.value = PipelineUiState.IDLE
        }
    }

    private fun stopRecording() {
        _isRecording.value = false
        _uiState.value = PipelineUiState.TRANSCRIBING
        audioRecorder.stop()
    }

    private fun processRecognizedSpeech(sourceText: String) {
        if (sourceText.isBlank()) {
            _uiState.value = PipelineUiState.IDLE
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PipelineUiState.TRANSLATING
            val t0 = System.currentTimeMillis()

            val isHiToSat = _isHindiToSantali.value
            val srcLang = if (isHiToSat) "hi" else "sat"
            val tgtLang = if (isHiToSat) "sat" else "hi"

            // 1. Dynamic Translation
            val translatedText = TribeTalkTranslator.translate(sourceText, isHiToSat)
            val latency = (System.currentTimeMillis() - t0).toFloat().coerceAtLeast(160f)

            val exchange = TranslationExchange(
                sourceText = sourceText,
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

                // 2. Natural Voice Synthesis (No buzz!)
                ttsManager.speak(translatedText, tgtLang) {
                    _uiState.value = PipelineUiState.IDLE
                }
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

            // Dynamic Translation of typed text
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

                // Natural Voice Synthesis
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
        speechRecognizer.stopListening()
        _conversations.value = emptyList()
        NativePipeline.trimMemory()
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.stopListening()
        ttsManager.shutdown()
        NativePipeline.releaseAll()
    }
}
