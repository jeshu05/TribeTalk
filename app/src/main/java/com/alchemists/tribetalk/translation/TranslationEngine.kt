package com.alchemists.tribetalk.translation

interface TranslationEngine {
    fun translate(text: String, source: Language, target: Language): String
}
