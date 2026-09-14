package org.tribetalk.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

/**
 * Continuous Hindi Speech Recognition Manager for Live Classroom Voice Translation.
 *
 * Implements:
 * 1. Continuous listening with automatic restart loop upon speech endpoints or silence timeouts.
 * 2. Partial streaming speech callbacks for real-time Devanagari text display on screen.
 * 3. Audio feedback suppression (pause during Santali TTS playback, auto-resume after playback).
 * 4. Error recovery that recycles the recognizer without dropping the session.
 */
class LiveVoiceInputManager(private val context: Context) {

    companion object {
        private const val TAG = "LiveVoiceInputManager"
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    var isContinuousSession: Boolean = false
        private set

    @Volatile
    var isPlaybackActive: Boolean = false
        private set

    private var activeLanguageCode: String = "hi-IN"
    private var onPartialCallback: ((String) -> Unit)? = null
    private var onFinalCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null
    private var onStateChangeCallback: ((String) -> Unit)? = null

    fun startContinuousListening(
        languageCode: String = "hi-IN",
        onPartial: (String) -> Unit = {},
        onFinal: (String) -> Unit,
        onError: (String) -> Unit,
        onStateChange: (String) -> Unit
    ) {
        Log.i(TAG, "Starting continuous voice listening session (language=$languageCode)")
        isContinuousSession = true
        isPlaybackActive = false
        activeLanguageCode = languageCode
        onPartialCallback = onPartial
        onFinalCallback = onFinal
        onErrorCallback = onError
        onStateChangeCallback = onStateChange

        startListeningInternal()
    }

    fun pauseForPlayback() {
        isPlaybackActive = true
        Log.d(TAG, "Microphone paused during TTS playback to avoid audio feedback")
    }

    fun resumeAfterPlayback() {
        isPlaybackActive = false
        Log.d(TAG, "Microphone unmuted after TTS playback finished")
        if (isContinuousSession) {
            mainHandler.postDelayed({
                if (isContinuousSession && !isPlaybackActive) {
                    startListeningInternal()
                }
            }, 250)
        }
    }

    private fun startListeningInternal() {
        if (!isContinuousSession || isPlaybackActive) return

        mainHandler.post {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                val err = "Speech recognition service is not available on this device"
                Log.e(TAG, err)
                onErrorCallback?.invoke(err)
                return@post
            }

            stopListeningInternal()

            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            Log.d(TAG, "ASR ready for speech")
                            onStateChangeCallback?.invoke("LIVE LISTENING")
                        }

                        override fun onBeginningOfSpeech() {
                            if (!isPlaybackActive) {
                                Log.d(TAG, "Speech detected by microphone")
                                onStateChangeCallback?.invoke("Listening...")
                            }
                        }

                        override fun onRmsChanged(rmsdB: Float) {}

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            Log.d(TAG, "Speech ended, decoding Hindi text")
                            onStateChangeCallback?.invoke("Processing...")
                        }

                        override fun onError(error: Int) {
                            val isTimeoutOrNoMatch = (error == SpeechRecognizer.ERROR_NO_MATCH ||
                                    error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)

                            if (isContinuousSession && !isPlaybackActive) {
                                Log.d(TAG, "ASR silence/timeout (code=$error), cycling microphone...")
                                mainHandler.postDelayed({
                                    if (isContinuousSession && !isPlaybackActive) {
                                        startListeningInternal()
                                    }
                                }, 300)
                                return
                            }

                            val message = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                                SpeechRecognizer.ERROR_CLIENT -> "Client error"
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                                SpeechRecognizer.ERROR_SERVER -> "Server error"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                                else -> "Recognition error ($error)"
                            }
                            Log.w(TAG, "ASR Error: $message (code=$error)")
                            onErrorCallback?.invoke(message)
                        }

                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull { it.isNotBlank() }
                            if (!isPlaybackActive && !text.isNullOrBlank()) {
                                Log.i(TAG, "ASR Final Result: \"$text\"")
                                onFinalCallback?.invoke(text)
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
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val partialText = matches?.firstOrNull { it.isNotBlank() }
                            if (!partialText.isNullOrBlank()) {
                                Log.d(TAG, "ASR Partial Result: \"$partialText\"")
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
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start speech recognizer: ${e.localizedMessage}", e)
                onErrorCallback?.invoke(e.localizedMessage ?: "Failed to start speech recognition")
            }
        }
    }

    private fun stopListeningInternal() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }

    fun stopContinuousListening() {
        Log.i(TAG, "Stopping continuous voice listening session")
        isContinuousSession = false
        isPlaybackActive = false
        stopListeningInternal()
        onStateChangeCallback?.invoke("Idle")
    }

    fun destroy() {
        stopContinuousListening()
    }
}
