package com.alchemists.tribetalk.nlp

import android.util.Log

/**
 * Latency metrics summary data class.
 */
data class LatencyMetrics(
    val nlpDurationMs: Long,
    val translationDurationMs: Long,
    val ttsDurationMs: Long,
    val playbackDelayMs: Long,
    val totalE2eLatencyMs: Long,
    val isWithinTarget: Boolean // True if <= 3000ms (3.0s)
)

/**
 * Latency Instrumentation for Live Speech-to-Speech Translation Pipeline.
 *
 * Records timestamps across:
 * ASR_FINAL -> NLP_READY -> TRANSLATION_START -> TRANSLATION_END -> TTS_START -> TTS_RESPONSE -> PLAYBACK_START
 */
class LatencyTracker {

    companion object {
        private const val TAG = "TribeTalkLatency"
        const val TARGET_LATENCY_MS = 3000L
    }

    var tAsrFinal: Long = 0L
    var tNlpReady: Long = 0L
    var tTranslationStart: Long = 0L
    var tTranslationEnd: Long = 0L
    var tTtsStart: Long = 0L
    var tTtsResponse: Long = 0L
    var tPlaybackStart: Long = 0L

    fun markAsrFinal(timeMs: Long = System.currentTimeMillis()) {
        tAsrFinal = timeMs
        Log.d(TAG, "[TIMING] ASR_FINAL: $tAsrFinal")
    }

    fun markNlpReady(timeMs: Long = System.currentTimeMillis()) {
        tNlpReady = timeMs
        Log.d(TAG, "[TIMING] NLP_READY: $tNlpReady (+${tNlpReady - tAsrFinal}ms)")
    }

    fun markTranslationStart(timeMs: Long = System.currentTimeMillis()) {
        tTranslationStart = timeMs
        Log.d(TAG, "[TIMING] TRANSLATION_START: $tTranslationStart")
    }

    fun markTranslationEnd(timeMs: Long = System.currentTimeMillis()) {
        tTranslationEnd = timeMs
        Log.d(TAG, "[TIMING] TRANSLATION_END: $tTranslationEnd (+${tTranslationEnd - tTranslationStart}ms)")
    }

    fun markTtsStart(timeMs: Long = System.currentTimeMillis()) {
        tTtsStart = timeMs
        Log.d(TAG, "[TIMING] TTS_START: $tTtsStart")
    }

    fun markTtsResponse(timeMs: Long = System.currentTimeMillis()) {
        tTtsResponse = timeMs
        Log.d(TAG, "[TIMING] TTS_RESPONSE: $tTtsResponse (+${tTtsResponse - tTtsStart}ms)")
    }

    fun markPlaybackStart(timeMs: Long = System.currentTimeMillis()) {
        tPlaybackStart = timeMs
        val totalMs = if (tAsrFinal > 0) tPlaybackStart - tAsrFinal else 0L
        Log.d(TAG, "[TIMING] PLAYBACK_START: $tPlaybackStart -> Total E2E: ${totalMs}ms")
    }

    fun computeMetrics(): LatencyMetrics {
        val nlpMs = if (tNlpReady > 0 && tAsrFinal > 0) tNlpReady - tAsrFinal else 0L
        val transMs = if (tTranslationEnd > 0 && tTranslationStart > 0) tTranslationEnd - tTranslationStart else 0L
        val ttsMs = if (tTtsResponse > 0 && tTtsStart > 0) tTtsResponse - tTtsStart else 0L
        val playbackDelay = if (tPlaybackStart > 0 && tTtsResponse > 0) tPlaybackStart - tTtsResponse else 0L

        val totalE2e = if (tPlaybackStart > 0 && tAsrFinal > 0) {
            tPlaybackStart - tAsrFinal
        } else if (tTranslationEnd > 0 && tAsrFinal > 0) {
            tTranslationEnd - tAsrFinal
        } else {
            0L
        }

        val withinTarget = totalE2e <= TARGET_LATENCY_MS

        val metrics = LatencyMetrics(
            nlpDurationMs = nlpMs,
            translationDurationMs = transMs,
            ttsDurationMs = ttsMs,
            playbackDelayMs = playbackDelay,
            totalE2eLatencyMs = totalE2e,
            isWithinTarget = withinTarget
        )

        Log.i(TAG, "[LATENCY REPORT] E2E Total = ${metrics.totalE2eLatencyMs}ms (NLP: ${nlpMs}ms, Translation: ${transMs}ms, TTS: ${ttsMs}ms, Playback: ${playbackDelay}ms) | Within <=3s Target: ${metrics.isWithinTarget}")

        return metrics
    }
}
