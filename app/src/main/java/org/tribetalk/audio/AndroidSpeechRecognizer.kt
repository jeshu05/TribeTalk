package org.tribetalk.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * High-accuracy speech recognizer utilizing Android SpeechRecognizer.
 * Captures genuine live speech in Hindi (hi-IN) or Santali/multilingual on the device.
 */
class AndroidSpeechRecognizer(private val context: Context) {
    companion object {
        private const val TAG = "AndroidSpeechRecognizer"
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    fun startListening(
        isHindi: Boolean,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        stopListening()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w(TAG, "Speech recognition not available on device")
            onError("Speech recognition not available")
            return
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d(TAG, "Ready for speech")
                        isListening = true
                    }

                    override fun onBeginningOfSpeech() {
                        Log.d(TAG, "Speech started")
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize dB (-2 to 10 typical) to [0.0, 1.0]
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _amplitude.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        Log.d(TAG, "Speech ended")
                        _amplitude.value = 0f
                        isListening = false
                    }

                    override fun onError(error: Int) {
                        Log.w(TAG, "Speech recognition error code: $error")
                        _amplitude.value = 0f
                        isListening = false
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission denied"
                            SpeechRecognizer.ERROR_NETWORK -> "Network issue"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                            else -> "Recognition error ($error)"
                        }
                        onError(msg)
                    }

                    override fun onResults(results: Bundle?) {
                        _amplitude.value = 0f
                        isListening = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        Log.d(TAG, "Recognized text: $text")
                        if (text.isNotEmpty()) {
                            onResult(text)
                        } else {
                            onError("Empty transcription")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let {
                            Log.d(TAG, "Partial: $it")
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                if (isHindi) {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                } else {
                    // For Santali, prioritize Indic/regional multilingual
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                }
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching SpeechRecognizer", e)
            onError(e.message ?: "Failed to start listener")
        }
    }

    fun stopListening() {
        _amplitude.value = 0f
        isListening = false
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "Error destroying speechRecognizer", e)
        } finally {
            speechRecognizer = null
        }
    }
}
