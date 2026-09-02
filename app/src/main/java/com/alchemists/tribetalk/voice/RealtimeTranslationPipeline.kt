package com.alchemists.tribetalk.voice

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.alchemists.tribetalk.translation.Language
import com.alchemists.tribetalk.translation.TranslationCache
import com.alchemists.tribetalk.translation.TranslationEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.atomic.AtomicBoolean

data class PipelineLatency(
    val asrMs: Long = 0L,
    val nmtMs: Long = 0L,
    val ttsMs: Long = 0L,
    val endToEndMs: Long = 0L
)

sealed class PipelineState {
    object Idle : PipelineState()
    object Listening : PipelineState()
    object ProcessingAsr : PipelineState()
    object Translating : PipelineState()
    object Speaking : PipelineState()
    data class Error(val message: String) : PipelineState()
}

/**
 * Asynchronous Real-Time Hindi -> Santali Translation Pipeline.
 * Connects continuous audio recording, VAD endpoint detection, chunked ASR,
 * IndicTrans2 NMT, and SPRING_F5 Santali TTS in a non-blocking coroutine producer/consumer queue.
 */
class RealtimeTranslationPipeline(
    private val context: Context,
    private val translationEngine: TranslationEngine,
    private val neuralSynthesizer: NeuralSpeechSynthesizer? = null
) : AutoCloseable {

    companion object {
        private const val TAG = "RealtimeTranslationPipeline"
    }

    private val audioRecorder = RealtimeAudioRecorder()
    private val vad = VoiceActivityDetector()
    private val realtimeAsr by lazy { RealtimeHindiAsr(context) }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val isRunning = AtomicBoolean(false)

    private val utteranceChannel = Channel<UtteranceJob>(Channel.UNLIMITED)

    private data class UtteranceJob(
        val pcm: ShortArray,
        val endpointTimeMs: Long
    )

    fun startPipeline(
        onStateChange: (PipelineState) -> Unit,
        onHindiText: (String) -> Unit,
        onSantaliText: (String) -> Unit,
        onLatencyMeasured: (PipelineLatency) -> Unit,
        onError: (String) -> Unit
    ) {
        if (isRunning.getAndSet(true)) return

        Log.i(TAG, "[Pipeline] Starting continuous real-time voice translation pipeline")
        onStateChange(PipelineState.Listening)

        // Launch Consumer Loop processing utterances asynchronously
        scope.launch {
            for (job in utteranceChannel) {
                processUtteranceJob(
                    job = job,
                    onStateChange = onStateChange,
                    onHindiText = onHindiText,
                    onSantaliText = onSantaliText,
                    onLatencyMeasured = onLatencyMeasured,
                    onError = onError
                )
            }
        }

        // Launch Producer Loop collecting microphone PCM audio frames
        scope.launch {
            if (!audioRecorder.startRecording(onError = { err ->
                onStateChange(PipelineState.Error(err))
                onError(err)
            })) {
                isRunning.set(false)
                return@launch
            }

            audioRecorder.audioFlow.collectLatest { pcmFrame ->
                if (!isRunning.get()) return@collectLatest

                vad.processFrame(pcmFrame) { event ->
                    when (event) {
                        is VadEvent.SpeechStarted -> {
                            onStateChange(PipelineState.Listening)
                        }
                        is VadEvent.SpeechContinuing -> {
                            onStateChange(PipelineState.Listening)
                        }
                        is VadEvent.EndpointDetected -> {
                            val endpointTime = SystemClock.elapsedRealtime()
                            Log.i(TAG, "[Pipeline] Endpoint detected. Queueing utterance (${event.utterancePcm.size} samples)")
                            utteranceChannel.trySend(UtteranceJob(event.utterancePcm, endpointTime))
                        }
                    }
                }
            }
        }
    }

    private suspend fun processUtteranceJob(
        job: UtteranceJob,
        onStateChange: (PipelineState) -> Unit,
        onHindiText: (String) -> Unit,
        onSantaliText: (String) -> Unit,
        onLatencyMeasured: (PipelineLatency) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val t0 = job.endpointTimeMs

            // Step 1: ASR Transcribe Hindi
            onStateChange(PipelineState.ProcessingAsr)
            val rawHindiText = realtimeAsr.processUtterancePcm(job.pcm)
            val t1 = SystemClock.elapsedRealtime()
            val asrLatency = t1 - t0

            if (rawHindiText.isBlank()) {
                Log.w(TAG, "[Pipeline] ASR output empty or quiet speech")
                onStateChange(PipelineState.Listening)
                return
            }

            // Step 1b: Offline NLP Middleware Processing (Normalization, De-duplication, Intent, Number Extraction)
            val nlpResult = com.alchemists.tribetalk.nlp.OfflineNlpProcessor.process(rawHindiText)
            val normalizedHindi = nlpResult.normalizedText

            if (!nlpResult.isTranslationReady || normalizedHindi.isBlank()) {
                Log.w(TAG, "[Pipeline NLP] Text not ready for translation: '${nlpResult.reason}'")
                onStateChange(PipelineState.Listening)
                return
            }

            onHindiText(normalizedHindi)

            // Step 2: NMT Translate Hindi -> Santali
            onStateChange(PipelineState.Translating)
            val cachedTranslation = TranslationCache.getTranslation(normalizedHindi, "hin_Deva", "sat_Olck")
            val rawSantali = if (cachedTranslation != null) {
                cachedTranslation
            } else {
                val res = translationEngine.translate(normalizedHindi, Language.HINDI, Language.SANTALI)
                val translated = res.translatedText
                if (translated.isNotBlank()) {
                    TranslationCache.putTranslation(normalizedHindi, "hin_Deva", "sat_Olck", translated)
                }
                translated
            }

            // Step 2b: Santali Post-Processing (Ol Chiki Unicode & Whitespace Normalization)
            val santaliText = com.alchemists.tribetalk.nlp.SantaliPostProcessor.postProcess(rawSantali)

            val t2 = SystemClock.elapsedRealtime()
            val nmtLatency = t2 - t1

            if (santaliText.isBlank()) {
                val err = "Translation unavailable for '$normalizedHindi'"
                Log.w(TAG, "[Pipeline] $err")
                onError(err)
                onStateChange(PipelineState.Listening)
                return
            }

            onSantaliText(santaliText)

            // Step 3: TTS Synthesize & Speak Santali Audio
            onStateChange(PipelineState.Speaking)
            val t3Start = SystemClock.elapsedRealtime()

            if (neuralSynthesizer != null && neuralSynthesizer.isNeuralModelAvailable("sat")) {
                val completer = CompletableDeferred<Unit>()
                neuralSynthesizer.speak(
                    text = santaliText,
                    languageCode = "sat",
                    onStart = {
                        val t4 = SystemClock.elapsedRealtime()
                        val ttsLatency = t4 - t3Start
                        val endToEnd = t4 - t0
                        onLatencyMeasured(PipelineLatency(asrMs = asrLatency, nmtMs = nmtLatency, ttsMs = ttsLatency, endToEndMs = endToEnd))
                    },
                    onDone = {
                        completer.complete(Unit)
                    },
                    onError = { err ->
                        Log.e(TAG, "[Pipeline TTS Error] $err")
                        completer.complete(Unit)
                    }
                )
                completer.await()
            } else {
                val t4 = SystemClock.elapsedRealtime()
                val endToEnd = t4 - t0
                onLatencyMeasured(PipelineLatency(asrMs = asrLatency, nmtMs = nmtLatency, ttsMs = 0L, endToEndMs = endToEnd))
            }

            onStateChange(PipelineState.Listening)

        } catch (e: Exception) {
            val err = "Pipeline processing exception: ${e.message}"
            Log.e(TAG, err, e)
            onError(err)
            onStateChange(PipelineState.Listening)
        }
    }

    fun stopPipeline() {
        if (!isRunning.getAndSet(false)) return
        Log.i(TAG, "[Pipeline] Stopping real-time translation pipeline")
        audioRecorder.stopRecording()
        vad.reset()
    }

    override fun close() {
        stopPipeline()
        audioRecorder.close()
        scope.cancel()
    }
}
