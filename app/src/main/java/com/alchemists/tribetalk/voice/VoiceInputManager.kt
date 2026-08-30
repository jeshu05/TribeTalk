package com.alchemists.tribetalk.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class VoiceInputManager(
    private val context: Context,
    private val neuralRecognizer: NeuralSpeechRecognizer? = null
) {
    companion object {
        private const val TAG = "HindiASR"
    }

    private var speechRecognizer: SpeechRecognizer? = null

    fun startListening(
        languageCode: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onStateChange: (String) -> Unit
    ) {
        Log.i(TAG, "[HindiASR] ASR START: Requested language=$languageCode")

        // 1. If offline Neural ASR (IndicConformer ONNX) model is available and initialized, use it
        if (neuralRecognizer != null && neuralRecognizer.isNeuralModelAvailable(languageCode)) {
            Log.i(TAG, "[HindiASR] ASR MODEL READY: Using offline Neural ASR engine")
            onStateChange("Listening (Offline Neural ASR)...")
            neuralRecognizer.startListening(
                languageCode = languageCode,
                onSpeechDetected = {
                    Log.i(TAG, "[HindiASR] AUDIO RECEIVED: Speech detected by Neural ASR")
                    onStateChange("Recording (Neural ASR)...")
                },
                onResult = { res ->
                    Log.i(TAG, "[HindiASR] FINAL RESULT: \"$res\"")
                    onResult(res)
                },
                onError = { err ->
                    Log.e(TAG, "[HindiASR] ASR ERROR: $err")
                    onError(err)
                }
            )
            return
        }

        // 2. Standard Android SpeechRecognizer pipeline (works offline on Android 9+ with on-device speech packs)
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            val err = "Speech recognition is not available on this device"
            Log.e(TAG, "[HindiASR] ASR ERROR: $err")
            onError(err)
            return
        }

        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.i(TAG, "[HindiASR] MICROPHONE STARTED: Android SpeechRecognizer ready")
                        onStateChange("Listening...")
                    }

                    override fun onBeginningOfSpeech() {
                        Log.i(TAG, "[HindiASR] AUDIO RECEIVED: Speech started")
                        onStateChange("Recording...")
                    }

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {
                        Log.v(TAG, "[HindiASR] AUDIO RECEIVED: buffer size=${buffer?.size ?: 0}")
                    }

                    override fun onEndOfSpeech() {
                        Log.i(TAG, "[HindiASR] Speech ended, decoding results")
                        onStateChange("Processing...")
                    }

                    override fun onError(error: Int) {
                        // Fallback to offline Neural ASR if available
                        if ((error == SpeechRecognizer.ERROR_NETWORK || error == SpeechRecognizer.ERROR_SERVER || error == SpeechRecognizer.ERROR_NO_MATCH) &&
                            neuralRecognizer?.isNeuralModelAvailable(languageCode) == true
                        ) {
                            Log.i(TAG, "[HindiASR] Falling back to offline Neural ASR after error=$error")
                            onStateChange("Listening (Offline Neural ASR)...")
                            neuralRecognizer.startListening(
                                languageCode = languageCode,
                                onSpeechDetected = { onStateChange("Recording (Neural ASR)...") },
                                onResult = { res ->
                                    Log.i(TAG, "[HindiASR] FINAL RESULT (Neural Fallback): \"$res\"")
                                    onResult(res)
                                },
                                onError = { err ->
                                    Log.e(TAG, "[HindiASR] ASR ERROR (Neural Fallback): $err")
                                    onError(err)
                                }
                            )
                            return
                        }

                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client-side error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech matched"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy"
                            SpeechRecognizer.ERROR_SERVER -> "Server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                            else -> "Unknown recognition error ($error)"
                        }
                        Log.e(TAG, "[HindiASR] ASR ERROR: code=$error ($message)")
                        onError(message)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull { it.isNotBlank() }
                        if (!text.isNullOrBlank()) {
                            Log.i(TAG, "[HindiASR] FINAL RESULT: \"$text\"")
                            onResult(text)
                        } else {
                            Log.w(TAG, "[HindiASR] ASR ERROR: No speech matched in results bundle")
                            onError("No speech matched")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partialText = partialMatches?.firstOrNull { it.isNotBlank() }
                        if (!partialText.isNullOrBlank()) {
                            Log.d(TAG, "[HindiASR] PARTIAL RESULT: \"$partialText\"")
                            onStateChange("Recognizing: $partialText")
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languageCode)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra("android.speech.extra.DICTATION_MODE", true)
                putExtra("android.speech.extra.PREFER_OFFLINE", true)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            val err = "Failed to start speech recognizer: ${e.localizedMessage}"
            Log.e(TAG, "[HindiASR] ASR ERROR: $err", e)
            onError(err)
        }
    }

    fun stopListening() {
        Log.i(TAG, "[HindiASR] ASR STOPPED")
        try {
            neuralRecognizer?.stopListening()
        } catch (_: Exception) {}

        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }

    fun destroy() {
        stopListening()
    }
}
