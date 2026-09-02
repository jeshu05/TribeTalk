package com.alchemists.tribetalk.nlp

import java.text.Normalizer

/**
 * Deterministic Offline Hindi Text Normalizer.
 * Handles Unicode normalization, Devanagari whitespace cleanup, ASR artifact deduplication,
 * and sentence-ending punctuation without external neural models.
 */
object HindiNormalizer {

    private val LEGITIMATE_REPETITIONS = setOf(
        "धीरे धीरे", "बार बार", "साथ साथ", "तरह तरह", "अलग अलग",
        "छोटे छोटे", "बड़े बड़े", "साफ साफ", "जल्दी जल्दी", "एक एक", "पास पास"
    )

    private val QUESTION_WORDS = setOf(
        "क्या", "कितने", "कितनी", "कौन", "कहाँ", "कैसे", "क्यों", "किस"
    )

    fun normalize(text: String): String {
        if (text.isBlank()) return ""

        // 1. Unicode NFC Normalization
        val nfcNormalized = Normalizer.normalize(text, Normalizer.Form.NFC)

        // 2. Whitespace Collapse & Trim
        var cleaned = nfcNormalized.replace("\\s+".toRegex(), " ").trim()

        // 3. ASR Artifact Repetition Cleanup
        cleaned = cleanupAsrRepetitions(cleaned)

        // 4. Punctuation Normalization
        cleaned = normalizePunctuation(cleaned)

        return cleaned
    }

    private fun cleanupAsrRepetitions(input: String): String {
        val words = input.split(" ")
        if (words.size <= 1) return input

        val result = mutableListOf<String>()
        var i = 0

        while (i < words.size) {
            val currentWord = words[i]
            if (i < words.size - 1) {
                val nextWord = words[i + 1]
                val pair = "$currentWord $nextWord"

                if (currentWord == nextWord) {
                    if (pair in LEGITIMATE_REPETITIONS) {
                        result.add(currentWord)
                        result.add(nextWord)
                        i += 2
                        continue
                    } else {
                        // Skip duplicate ASR artifact
                        result.add(currentWord)
                        while (i < words.size && words[i] == currentWord) {
                            i++
                        }
                        continue
                    }
                }
            }
            result.add(currentWord)
            i++
        }

        return result.joinToString(" ")
    }

    private fun normalizePunctuation(text: String): String {
        var trimmed = text.trim()

        // Remove trailing commas or dashes
        trimmed = trimmed.replace("[,\\-–—]+$".toRegex(), "").trim()

        // Check if sentence already ends with valid punctuation (।, ?, !)
        if (trimmed.endsWith("।") || trimmed.endsWith("?") || trimmed.endsWith("!")) {
            return trimmed
        }

        // Determine whether sentence is interrogative
        val words = trimmed.split("\\s+".toRegex())
        val isQuestion = words.any { it in QUESTION_WORDS }

        return if (isQuestion) {
            "$trimmed?"
        } else {
            "$trimmed।"
        }
    }
}
