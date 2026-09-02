package com.alchemists.tribetalk.nlp

/**
 * Hindi Number Word and Range Extractor.
 * Extracts numeric words, Devanagari digits, and range pairs (e.g. "एक से दस तक" -> 1..10).
 */
object HindiNumberExtractor {

    private val HINDI_NUMBER_MAP = mapOf(
        "एक" to 1, "दो" to 2, "तीन" to 3, "चार" to 4, "पाँच" to 5, "पांच" to 5,
        "छह" to 6, "छः" to 6, "सात" to 7, "आठ" to 8, "नौ" to 9, "दस" to 10,
        "ग्यारह" to 11, "बारह" to 12, "तेरह" to 13, "चौदह" to 14, "पंद्रह" to 15,
        "सोलह" to 16, "सत्रह" to 17, "अठारह" to 18, "उन्नीस" to 19, "बीस" to 20,
        "१" to 1, "२" to 2, "३" to 3, "४" to 4, "५" to 5,
        "६" to 6, "७" to 7, "८" to 8, "९" to 9, "१०" to 10
    )

    fun extractNumbers(text: String): List<String> {
        val found = mutableListOf<String>()
        val words = text.split("\\s+".toRegex())

        for (word in words) {
            val cleanWord = word.replace("[,\\.\\?।]+$".toRegex(), "")
            if (cleanWord in HINDI_NUMBER_MAP) {
                found.add(cleanWord)
            } else if (cleanWord.matches("\\d+".toRegex())) {
                found.add(cleanWord)
            }
        }
        return found
    }

    fun extractNumberRange(text: String): Pair<Int, Int>? {
        val numbers = extractNumbers(text)
        if (numbers.size >= 2 && text.contains("से")) {
            val startVal = parseNumberValue(numbers[0])
            val endVal = parseNumberValue(numbers[1])
            if (startVal != null && endVal != null && startVal < endVal) {
                return Pair(startVal, endVal)
            }
        }
        return null
    }

    fun parseNumberValue(word: String): Int? {
        val clean = word.replace("[,\\.\\?।]+$".toRegex(), "")
        HINDI_NUMBER_MAP[clean]?.let { return it }
        return clean.toIntOrNull()
    }
}
