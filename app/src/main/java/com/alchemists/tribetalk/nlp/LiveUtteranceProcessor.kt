package com.alchemists.tribetalk.nlp

import android.util.Log

/**
 * Live Utterance Processor for Continuous ASR Stream.
 *
 * Buffers streaming partial recognition results and emits complete, normalized,
 * translatable utterances when grammatical sentence boundaries (।, ?, !) or final ASR tokens occur.
 */
class LiveUtteranceProcessor {

    companion object {
        private const val TAG = "LiveUtteranceProcessor"
    }

    private var accumulatedText = ""
    private var lastEmittedText = ""

    /**
     * Handles streaming partial ASR results.
     * Evaluates if a complete sentence boundary has formed without waiting for full microphone stop.
     */
    fun processPartial(partialText: String): String? {
        val trimmed = partialText.trim()
        if (trimmed.isEmpty() || trimmed == lastEmittedText) return null

        // Check if a complete sentence delimiter is present
        if (trimmed.contains("[।?!\\n]".toRegex())) {
            val segments = HindiNlpProcessor.segmentUtterances(trimmed)
            if (segments.isNotEmpty()) {
                val completeSentence = segments.first().trim()
                if (completeSentence.isNotEmpty() && completeSentence != lastEmittedText) {
                    val normalized = HindiNlpProcessor.normalizeHindiText(completeSentence)
                    val (isValid, _) = HindiNlpProcessor.evaluateConfidence(normalized)
                    if (isValid) {
                        lastEmittedText = completeSentence
                        Log.i(TAG, "[LIVE NLP] Emitting segmented utterance from stream: \"$normalized\"")
                        return normalized
                    }
                }
            }
        }
        return null
    }

    /**
     * Handles final ASR recognition results.
     * Normalizes text, deduplicates stutter, filters noise, and returns clean utterance.
     */
    fun processFinal(finalText: String): String? {
        val trimmed = finalText.trim()
        if (trimmed.isEmpty()) return null

        val normalized = HindiNlpProcessor.normalizeHindiText(trimmed)
        val (isValid, _) = HindiNlpProcessor.evaluateConfidence(normalized)

        if (isValid) {
            lastEmittedText = normalized
            Log.i(TAG, "[LIVE NLP] Finalized complete utterance: \"$normalized\"")
            return normalized
        } else {
            Log.w(TAG, "[LIVE NLP] Filtered out sub-word noise / low-confidence text: \"$finalText\"")
            return null
        }
    }

    fun reset() {
        accumulatedText = ""
        lastEmittedText = ""
    }
}
