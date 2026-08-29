package com.alchemists.tribetalk.translation

data class TranslationEntry(
    val sourceText: String,
    val targetText: String,
    val sourceLang: Language,
    val targetLang: Language,
    val category: String,
    val confidence: String = "High (Offline Match)",
    val alternativePhrasing: List<String> = emptyList()
)
