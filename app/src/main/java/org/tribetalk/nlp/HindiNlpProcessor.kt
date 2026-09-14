package org.tribetalk.nlp

import android.util.Log

/**
 * Result data class produced by the Hindi NLP Processing Layer.
 */
data class NlpProcessedUtterance(
    val rawText: String,
    val normalizedText: String,
    val segments: List<String>,
    val isValid: Boolean,
    val confidence: Float,
    val feedbackMessage: String? = null,
    val contextHistory: List<String> = emptyList()
)

/**
 * NLP Processing Layer for Live Hindi Speech-to-Speech Translation.
 *
 * Implements:
 * 1. Token deduplication (removes ASR repetition/stutter).
 * 2. Devanagari whitespace & punctuation normalization.
 * 3. Utterance/Sentence segmentation.
 * 4. Confidence & sub-word noise evaluation.
 * 5. Classroom conversational context retention.
 */
object HindiNlpProcessor {

    private const val TAG = "HindiNLP"
    private const val MAX_CONTEXT_ITEMS = 5
    private val conversationHistory = mutableListOf<String>()

    /**
     * Normalizes raw Hindi text:
     * - Trims and unifies whitespace
     * - Deduplicates consecutive repeated words (e.g. "नमस्ते नमस्ते" -> "नमस्ते")
     * - Standardizes Devanagari punctuation (danda '।', question mark '?', exclamation '!')
     * - Cleans up stray non-word characters while preserving Hindi meaning.
     */
    fun normalizeHindiText(text: String): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ""

        // 1. Unify multiple whitespaces into a single space
        var normalized = trimmed.replace("\\s+".toRegex(), " ")

        // 2. Standardize punctuation: convert '.' to '।', clean duplicate dandas
        normalized = normalized
            .replace("\\.{2,}".toRegex(), "।")
            .replace("\\.+".toRegex(), "।")
            .replace("।{2,}".toRegex(), "।")
            .replace("!{2,}".toRegex(), "!")
            .replace("\\?{2,}".toRegex(), "?")

        // 3. Deduplicate consecutive identical words (common ASR artifact)
        val words = normalized.split(" ")
        val deduplicatedWords = mutableListOf<String>()
        var prevWord = ""

        for (word in words) {
            val cleanWord = word.trim()
            if (cleanWord.isNotEmpty()) {
                // Strip trailing punctuation for comparison
                val baseWord = cleanWord.replace("[।!?,]".toRegex(), "")
                val prevBase = prevWord.replace("[।!?,]".toRegex(), "")

                if (baseWord.isNotEmpty() && baseWord.equals(prevBase, ignoreCase = true)) {
                    // Duplicate detected - preserve any punctuation attached to the second instance
                    if (cleanWord.contains("[।!?,]".toRegex()) && deduplicatedWords.isNotEmpty()) {
                        val lastIdx = deduplicatedWords.size - 1
                        deduplicatedWords[lastIdx] = deduplicatedWords[lastIdx] + cleanWord.filter { it in "।!?, " }
                    }
                    Log.d(TAG, "[NLP] Deduplicated repeated token: \"$cleanWord\"")
                } else {
                    deduplicatedWords.add(cleanWord)
                    prevWord = cleanWord
                }
            }
        }

        return deduplicatedWords.joinToString(" ").trim()
    }

    /**
     * Segments an utterance into meaningful grammatical clauses / sentences
     * using standard sentence delimiters (।, ?, !, \n).
     */
    fun segmentUtterances(text: String): List<String> {
        val normalized = normalizeHindiText(text)
        if (normalized.isEmpty()) return emptyList()

        // Split on sentence boundaries: ।, ?, !, \n
        val rawSegments = normalized.split("(?<=[।?!\\n])".toRegex())
        val segments = mutableListOf<String>()

        for (seg in rawSegments) {
            val trimmedSeg = seg.trim()
            if (trimmedSeg.length >= 2) {
                segments.add(trimmedSeg)
            }
        }

        return if (segments.isNotEmpty()) segments else listOf(normalized)
    }

    /**
     * Evaluates whether the ASR output represents a valid linguistic utterance
     * or transient noise / single-character artifact.
     */
    fun evaluateConfidence(text: String): Pair<Boolean, String?> {
        val trimmed = text.trim()
        val textWithoutPunct = trimmed.replace("[।॥!?,.:;\"'()\\[\\]\\s-]".toRegex(), "")

        // Reject empty or single-character noise
        if (textWithoutPunct.length < 2) {
            return Pair(false, "कृपया पुनः बोलें (Please repeat)")
        }

        // Check if string contains at least one valid letter character
        val hasLinguisticContent = textWithoutPunct.any {
            it.isLetter() || (it.code in 0x0904..0x0939) || (it.code in 0x0958..0x0961) || (it.code in 0x1C5A..0x1C77)
        }
        if (!hasLinguisticContent) {
            return Pair(false, "कृपया पुनः बोलें (No clear speech detected)")
        }

        return Pair(true, null)
    }

    /**
     * Appends a confirmed utterance to the classroom sliding context history.
     */
    @Synchronized
    fun recordContext(utterance: String) {
        val trimmed = utterance.trim()
        if (trimmed.isNotEmpty()) {
            conversationHistory.add(trimmed)
            if (conversationHistory.size > MAX_CONTEXT_ITEMS) {
                conversationHistory.removeAt(0)
            }
        }
    }

    /**
     * Returns the sliding context history of recent classroom utterances.
     */
    @Synchronized
    fun getRecentContext(): List<String> {
        return conversationHistory.toList()
    }

    /**
     * Clears conversational context history (e.g. on new classroom session).
     */
    @Synchronized
    fun clearContext() {
        conversationHistory.clear()
    }

    /**
     * Full NLP processing pipeline for a single raw ASR text output.
     */
    fun process(rawText: String): NlpProcessedUtterance {
        val normalized = normalizeHindiText(rawText)
        val (isValid, feedback) = evaluateConfidence(normalized)
        val segments = if (isValid) segmentUtterances(normalized) else emptyList()
        val confidence = if (isValid) 0.95f else 0.20f

        if (isValid) {
            recordContext(normalized)
        }

        return NlpProcessedUtterance(
            rawText = rawText,
            normalizedText = normalized,
            segments = segments,
            isValid = isValid,
            confidence = confidence,
            feedbackMessage = feedback,
            contextHistory = getRecentContext()
        )
    }
}
