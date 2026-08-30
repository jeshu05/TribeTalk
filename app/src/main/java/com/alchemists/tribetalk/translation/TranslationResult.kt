package com.alchemists.tribetalk.translation

/**
 * Enriched Translation Result supporting dual-screen & Teacher HUD projection.
 *
 * @param translatedText The primary translated text string.
 * @param confidence Confidence descriptor (e.g. "High (Teacher Validated)", "High (Edge AI / FLN Match)").
 * @param matched Whether a valid offline translation or subword match was identified.
 * @param requiresReview True if fallback or low confidence translation requires teacher verification.
 * @param matchType Categorical match classification ("exact", "fallback", "memory", "neural", "none").
 * @param olChikiText Authentic Santali Ol Chiki glyphs (U+1C50-U+1C7F) projected on the student-facing display.
 * @param phoneticDevanagari Phonetic Devanagari guide with stress marks for the Teacher Assist Heads-Up Display (HUD).
 * @param latinPhonetic Standard romanized Santali phonetic spelling.
 * @param engineTier The pipeline tier that resolved the translation.
 */
data class TranslationResult(
    val translatedText: String,
    val confidence: String,
    val matched: Boolean,
    val requiresReview: Boolean,
    val matchType: String,
    val olChikiText: String = "",
    val phoneticDevanagari: String = "",
    val latinPhonetic: String = "",
    val engineTier: String = "CURATED_FLN"
)
