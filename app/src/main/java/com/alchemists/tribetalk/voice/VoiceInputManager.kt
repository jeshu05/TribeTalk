package com.alchemists.tribetalk.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class VoiceInputManager(
    private val context: Context,
    private val neuralRecognizer: NeuralSpeechRecognizer? = null
) {
    companion object {
        private const val TAG = "LiveHindiASR"
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    var isContinuousSession = false
        private set

    @Volatile
    var isPlaybackActive = false
        private set

    private var activeLanguageCode: String = "hi-IN"
    private var onPartialCallback: ((String) -> Unit)? = null
    private var onFinalCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null
    private var onStateChangeCallback: ((String) -> Unit)? = null

    /**
     * Starts continuous live microphone listening session.
     * Automatically restarts listening on speech end or silence timeout without requiring button presses.
     */
    fun startContinuousListening(
        languageCode: String = "hi-IN",
        onPartial: (String) -> Unit = {},
        onFinal: (String) -> Unit,
        onError: (String) -> Unit,
        onStateChange: (String) -> Unit
    ) {
        Log.i(TAG, "ASR INITIALIZATION STARTED")
        Log.i(TAG, "ASR START REQUESTED: language=$languageCode (Continuous=true)")
        isContinuousSession = true
        activeLanguageCode = languageCode
        onPartialCallback = onPartial
        onFinalCallback = onFinal
        onErrorCallback = onError
        onStateChangeCallback = onStateChange

        startListeningInternal()
    }

    /**
     * Pauses microphone listening during TTS audio output to prevent audio feedback loop.
     */
    fun pauseForPlayback() {
        isPlaybackActive = true
        Log.d(TAG, "ASR MUTED FOR PLAYBACK")
    }

    /**
     * Resumes microphone listening immediately after TTS audio playback finishes.
     */
    fun resumeAfterPlayback() {
        isPlaybackActive = false
        Log.d(TAG, "ASR UNMUTED AFTER PLAYBACK")
        if (isContinuousSession) {
            mainHandler.postDelayed({
                if (isContinuousSession && !isPlaybackActive) {
                    startListeningInternal()
                }
            }, 300)
        }
    }

    fun startListening(
        languageCode: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onStateChange: (String) -> Unit
    ) {
        Log.i(TAG, "ASR INITIALIZATION STARTED (Single-shot)")
        Log.i(TAG, "ASR START REQUESTED: language=$languageCode (Continuous=false)")
        isContinuousSession = false
        activeLanguageCode = languageCode
        onPartialCallback = null
        onFinalCallback = onResult
        onErrorCallback = onError
        onStateChangeCallback = onStateChange

        startListeningInternal()
    }

    private fun startListeningInternal() {
        if (!isContinuousSession && isPlaybackActive) return

        mainHandler.post {
            // 1. If offline Neural ASR (IndicConformer ONNX) is available, use it
            if (neuralRecognizer != null && neuralRecognizer.isNeuralModelAvailable(activeLanguageCode)) {
                Log.i(TAG, "ASR MODEL READY: Using offline Neural ASR engine (IndicConformer ONNX)")
                Log.i(TAG, "ASR LISTENING (Offline Neural ASR)")
                onStateChangeCallback?.invoke(if (isContinuousSession) "LIVE LISTENING" else "Listening (Offline Neural ASR)...")
                
                neuralRecognizer.startListening(
                    languageCode = activeLanguageCode,
                    onSpeechDetected = {
                        if (!isPlaybackActive) {
                            Log.i(TAG, "AUDIO RECEIVED: Speech detected by Neural ASR")
                            onStateChangeCallback?.invoke("Recording (Neural ASR)...")
                        }
                    },
                    onResult = { res ->
                        if (!isPlaybackActive && res.isNotBlank()) {
                            Log.i(TAG, "ASR FINAL RESULT: \"$res\"")
                            onFinalCallback?.invoke(res)
                        }
                        if (isContinuousSession && !isPlaybackActive) {
                            mainHandler.postDelayed({ startListeningInternal() }, 200)
                        }
                    },
                    onError = { err ->
                        Log.e(TAG, "ASR ERROR: $err (Falling back to native recognizer if continuous)")
                        if (!isContinuousSession) {
                            onErrorCallback?.invoke(err)
                        } else {
                            // In continuous mode, fall back to native speech recognizer
                            startNativeSpeechRecognizer()
                        }
                    }
                )
                return@post
            }

            // 2. Standard Android SpeechRecognizer pipeline
            startNativeSpeechRecognizer()
        }
    }

    private fun startNativeSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            val err = "Speech recognition is not available on this device"
            Log.e(TAG, "ASR ERROR: $err")
            onErrorCallback?.invoke(err)
            return
        }

        stopListeningInternal()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.i(TAG, "ASR LISTENING")
                        onStateChangeCallback?.invoke(if (isContinuousSession) "LIVE LISTENING" else "Listening...")
                    }

                    override fun onBeginningOfSpeech() {
                        if (!isPlaybackActive) {
                            Log.i(TAG, "AUDIO RECEIVED: Speech started")
                            onStateChangeCallback?.invoke("Recording...")
                        }
                    }

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {
                        Log.v(TAG, "AUDIO RECEIVED: buffer size=${buffer?.size ?: 0}")
                    }

                    override fun onEndOfSpeech() {
                        Log.i(TAG, "AUDIO RECEIVED: Speech ended, decoding results")
                        onStateChangeCallback?.invoke("Processing...")
                    }

                    override fun onError(error: Int) {
                        val isSilenceOrNoMatch = (error == SpeechRecognizer.ERROR_NO_MATCH ||
                                error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)

                        if (isContinuousSession && !isPlaybackActive) {
                            Log.d(TAG, "ASR PAUSE/TIMEOUT in continuous mode (code=$error), cycling microphone...")
                            mainHandler.postDelayed({
                                if (isContinuousSession && !isPlaybackActive) {
                                    startListeningInternal()
                                }
                            }, 300)
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
                        Log.e(TAG, "ASR ERROR: code=$error ($message)")
                        onErrorCallback?.invoke(message)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull { it.isNotBlank() }
                        if (!isPlaybackActive && !text.isNullOrBlank()) {
                            Log.i(TAG, "ASR FINAL RESULT: \"$text\"")
                            onFinalCallback?.invoke(text)
                        } else if (!isContinuousSession) {
                            Log.w(TAG, "ASR ERROR: No speech matched in results bundle")
                            onErrorCallback?.invoke("No speech matched")
                        }

                        if (isContinuousSession && !isPlaybackActive) {
                            mainHandler.postDelayed({
                                if (isContinuousSession && !isPlaybackActive) {
                                    startListeningInternal()
                                }
                            }, 250)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        if (isPlaybackActive) return
                        val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partialText = partialMatches?.firstOrNull { it.isNotBlank() }
                        if (!partialText.isNullOrBlank()) {
                            Log.d(TAG, "ASR PARTIAL RESULT: \"$partialText\"")
                            onPartialCallback?.invoke(partialText)
                            onStateChangeCallback?.invoke("Recognizing: $partialText")
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, activeLanguageCode)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, activeLanguageCode)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, activeLanguageCode)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra("android.speech.extra.DICTATION_MODE", true)
                putExtra("android.speech.extra.PREFER_OFFLINE", true)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            val err = "Failed to start speech recognizer: ${e.localizedMessage}"
            Log.e(TAG, "ASR ERROR: $err", e)
            onErrorCallback?.invoke(err)
        }
    }

    private fun stopListeningInternal() {
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

    fun stopContinuousListening() {
        Log.i(TAG, "ASR STOPPED (Continuous session ended)")
        isContinuousSession = false
        isPlaybackActive = false
        stopListeningInternal()
        onStateChangeCallback?.invoke("Idle")
    }

    fun stopListening() {
        Log.i(TAG, "ASR STOPPED")
        isContinuousSession = false
        isPlaybackActive = false
        stopListeningInternal()
    }

    fun destroy() {
        stopContinuousListening()
    }
}
