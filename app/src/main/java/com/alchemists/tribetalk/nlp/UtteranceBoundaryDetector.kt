package com.alchemists.tribetalk.nlp

import android.util.Log

/**
 * Utterance Boundary & Completion Detector.
 * Determines when an ASR hypothesis has formed a complete, meaningful Hindi classroom phrase.
 */
object UtteranceBoundaryDetector {

    private const val TAG = "UtteranceBoundaryDetector"

    private val SENTENCE_ENDING_WORDS = setOf(
        "सिखेंगे", "सीखेंगे", "पढ़िए", "बताइए", "खोलिए", "करिए", "करें",
        "है", "हैं", "हो", "गा", "गी", "गे", "था", "थी", "थे", "चाहिए"
    )

    fun isUtteranceComplete(text: String, silenceDurationMs: Long = 0L): Boolean {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return false

        val words = trimmed.split("\\s+".toRegex())
        if (words.size < 2) return false

        val lastWord = words.last().replace("[।,?!]+$".toRegex(), "")

        // Signal 1: Explicit sentence punctuation
        if (trimmed.endsWith("।") || trimmed.endsWith("?") || trimmed.endsWith("!")) {
            return true
        }

        // Signal 2: Sentence-ending Hindi auxiliary/verb
        if (lastWord in SENTENCE_ENDING_WORDS) {
            return true
        }

        // Signal 3: Silence timeout (> 500 ms) with sufficient phrase length
        if (silenceDurationMs >= 500L && words.size >= 3) {
            return true
        }

        return false
    }
}
