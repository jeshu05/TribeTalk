package com.alchemists.tribetalk.voice

import android.util.Log
import kotlin.math.sqrt

sealed class VadEvent {
    object SpeechStarted : VadEvent()
    object SpeechContinuing : VadEvent()
    data class EndpointDetected(val utterancePcm: ShortArray, val durationMs: Long) : VadEvent()
}

/**
 * Local Lightweight Voice Activity & Endpoint Detector.
 * Runs 100% offline using RMS energy calculation with adaptive noise floor and hysteresis.
 */
class VoiceActivityDetector(
    private val sampleRate: Int = 16000,
    var minSpeechDurationMs: Long = 300L,
    var minSilenceDurationMs: Long = 600L,
    var maxUtteranceDurationMs: Long = 10000L,
    var initialThresholdRms: Float = 150.0f
) {
    companion object {
        private const val TAG = "VoiceActivityDetector"
    }

    private var noiseFloorRms = initialThresholdRms
    private var isSpeechActive = false
    private var speechStartTimeMs = 0L
    private var silenceStartTimeMs = 0L

    private val utteranceBuffer = mutableListOf<Short>()

    fun processFrame(frame: ShortArray, onEvent: (VadEvent) -> Unit) {
        val rms = calculateRms(frame)
        updateAdaptiveNoiseFloor(rms)

        val speechThreshold = (noiseFloorRms * 2.2f).coerceAtLeast(initialThresholdRms)
        val silenceThreshold = (noiseFloorRms * 1.4f).coerceAtLeast(initialThresholdRms * 0.7f)

        val now = System.currentTimeMillis()

        if (!isSpeechActive) {
            // Check for speech start
            if (rms > speechThreshold) {
                isSpeechActive = true
                speechStartTimeMs = now
                silenceStartTimeMs = 0L
                utteranceBuffer.clear()
                for (s in frame) utteranceBuffer.add(s)
                onEvent(VadEvent.SpeechStarted)
                try {
                    Log.d(TAG, "[VAD] Speech started (RMS=%.1f, Threshold=%.1f)".format(rms, speechThreshold))
                } catch (_: Throwable) {}
            }
        } else {
            // Speech is active
            for (s in frame) utteranceBuffer.add(s)
            val currentSpeechDuration = now - speechStartTimeMs

            if (rms < silenceThreshold) {
                if (silenceStartTimeMs == 0L) {
                    silenceStartTimeMs = now
                }
                val silenceDuration = now - silenceStartTimeMs

                if (silenceDuration >= minSilenceDurationMs && currentSpeechDuration >= minSpeechDurationMs) {
                    // Endpoint detected!
                    triggerEndpoint(now, onEvent)
                } else {
                    onEvent(VadEvent.SpeechContinuing)
                }
            } else {
                silenceStartTimeMs = 0L
                if (currentSpeechDuration >= maxUtteranceDurationMs) {
                    // Max utterance duration reached!
                    triggerEndpoint(now, onEvent)
                } else {
                    onEvent(VadEvent.SpeechContinuing)
                }
            }
        }
    }

    private fun triggerEndpoint(nowMs: Long, onEvent: (VadEvent) -> Unit) {
        val durationMs = nowMs - speechStartTimeMs
        val utterancePcm = synchronized(utteranceBuffer) {
            val arr = utteranceBuffer.toShortArray()
            utteranceBuffer.clear()
            arr
        }
        isSpeechActive = false
        speechStartTimeMs = 0L
        silenceStartTimeMs = 0L

        if (utterancePcm.isNotEmpty()) {
            try {
                Log.i(TAG, "[VAD] Endpoint detected! Utterance samples: ${utterancePcm.size} (${durationMs}ms)")
            } catch (_: Throwable) {}
            onEvent(VadEvent.EndpointDetected(utterancePcm, durationMs))
        }
    }

    fun reset() {
        isSpeechActive = false
        speechStartTimeMs = 0L
        silenceStartTimeMs = 0L
        utteranceBuffer.clear()
    }

    private fun calculateRms(frame: ShortArray): Float {
        if (frame.isEmpty()) return 0.0f
        var sumSq = 0.0
        for (s in frame) {
            val v = s.toDouble()
            sumSq += v * v
        }
        return sqrt(sumSq / frame.size).toFloat()
    }

    private fun updateAdaptiveNoiseFloor(currentRms: Float) {
        if (!isSpeechActive && currentRms < noiseFloorRms * 2.0f) {
            noiseFloorRms = 0.95f * noiseFloorRms + 0.05f * currentRms
        }
    }
}
