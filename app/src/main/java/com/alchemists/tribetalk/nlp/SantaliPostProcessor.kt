package com.alchemists.tribetalk.nlp

import java.text.Normalizer

/**
 * Santali Translation Post-Processor.
 * Normalizes IndicTrans2 Santali Ol Chiki output, cleans whitespace, preserves punctuation,
 * and validates Ol Chiki Unicode script integrity (U+1C50..U+1C7F).
 */
object SantaliPostProcessor {

    fun postProcess(translatedText: String): String {
        if (translatedText.isBlank()) return ""

        // 1. NFC Normalization
        val nfc = Normalizer.normalize(translatedText, Normalizer.Form.NFC)

        // 2. Whitespace collapse
        var cleaned = nfc.replace("\\s+".toRegex(), " ").trim()

        // 3. Ensure sentence ending punctuation if absent
        if (!cleaned.endsWith("᱾") && !cleaned.endsWith("?") && !cleaned.endsWith("!") && !cleaned.endsWith("।")) {
            cleaned += " ᱾"
        }

        return cleaned
    }

    fun containsOlChiki(text: String): Boolean {
        for (char in text) {
            val codePoint = char.code
            if (codePoint in 0x1C50..0x1C7F) {
                return true
            }
        }
        return false
    }
}
