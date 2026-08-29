package com.alchemists.tribetalk.translation

class MockTranslationEngine : TranslationEngine {
    override fun translate(text: String, source: Language, target: Language): TranslationResult {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return TranslationResult("", "No match", matched = false, requiresReview = false, matchType = "none")
        }
        return TranslationResult(
            translatedText = "[Mock Translation: $trimmed]",
            confidence = "High (Mock)",
            matched = true,
            requiresReview = false,
            matchType = "exact"
        )
    }
}
