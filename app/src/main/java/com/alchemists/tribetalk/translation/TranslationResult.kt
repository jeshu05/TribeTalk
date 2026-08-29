package com.alchemists.tribetalk.translation

data class TranslationResult(
    val translatedText: String,
    val confidence: String,
    val matched: Boolean,
    val requiresReview: Boolean,
    val matchType: String
)
