package com.alchemists.tribetalk.translation

/**
 * Hybrid Edge AI Translation Engine for TribeTalk.
 * Integrates:
 *  1. Active Teacher Translation Memory (Priority 1)
 *  2. High-Coverage NIPUN FLN & JCERT Curriculum Database (Priority 2)
 *  3. Agglutinative & Subword Morphological Matcher (Priority 3)
 *  4. Edge AI ONNX Runtime Mobile Neural Engine (Priority 4)
 *  5. G2P Ol Chiki Dual-Script Transliteration (Priority 5)
 */
class HybridEdgeAITranslationEngine(
    val translationMemory: TranslationMemory,
    val onnxEngine: OnnxTranslationEngine? = null
) : TranslationEngine {

    private val neuralEngine = NeuralNMTTranslationEngine(onnxEngine)

    fun normalizeText(text: String): String {
        return text.trim()
            .replace(Regex("\\s+"), " ")
            .lowercase()
            .replace(Regex("[?.!,।॥]"), "")
            .trim()
    }

    override fun translate(text: String, source: Language, target: Language): TranslationResult {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return TranslationResult("", "No match", matched = false, requiresReview = false, matchType = "none")
        }

        if (source == target) {
            val olChiki = if (source == Language.SANTALI) OlChikiTransliterator.toOlChiki(trimmed) else ""
            val devaGuide = if (source == Language.SANTALI) OlChikiTransliterator.toTeacherPhoneticHUD(trimmed) else ""
            val latinGuide = if (source == Language.SANTALI) OlChikiTransliterator.toLatinPhonetic(trimmed) else ""
            return TranslationResult(
                translatedText = trimmed,
                confidence = "High (Same Language)",
                matched = true,
                requiresReview = false,
                matchType = "same_language",
                olChikiText = olChiki,
                phoneticDevanagari = devaGuide,
                latinPhonetic = latinGuide,
                engineTier = "SAME_LANGUAGE"
            )
        }

        // =========================================================================
        // PRIORITY 1: Teacher-Validated Translation Memory (0ms active memory hit)
        // =========================================================================
        val memoryHit = translationMemory.findCorrection(trimmed, source, target)
        if (memoryHit != null) {
            val targetText = memoryHit.targetText
            val olChiki = if (target == Language.SANTALI) OlChikiTransliterator.toOlChiki(targetText) else ""
            val devaGuide = if (target == Language.SANTALI) OlChikiTransliterator.toTeacherPhoneticHUD(targetText) else ""
            val latinGuide = if (target == Language.SANTALI) OlChikiTransliterator.toLatinPhonetic(targetText) else ""
            return TranslationResult(
                translatedText = targetText,
                confidence = "High (Teacher Validated)",
                matched = true,
                requiresReview = false,
                matchType = "exact",
                olChikiText = olChiki,
                phoneticDevanagari = devaGuide,
                latinPhonetic = latinGuide,
                engineTier = "TEACHER_MEMORY"
            )
        }

        val normalizedInput = normalizeText(trimmed)

        // =========================================================================
        // PRIORITY 2: Curated NIPUN FLN & Pedagogical Curriculum Base
        // =========================================================================
        if (source == Language.HINDI && target == Language.SANTALI) {
            val flnMatch = FLNCurriculumDatabase.findByHindi(trimmed)
            if (flnMatch != null) {
                return TranslationResult(
                    translatedText = flnMatch.santaliOlChiki,
                    confidence = "High (Curated FLN Match)",
                    matched = true,
                    requiresReview = false,
                    matchType = "exact",
                    olChikiText = flnMatch.santaliOlChiki,
                    phoneticDevanagari = flnMatch.phoneticDevanagari,
                    latinPhonetic = flnMatch.latinPhonetic,
                    engineTier = "CURATED_FLN"
                )
            }
        } else if (source == Language.SANTALI && target == Language.HINDI) {
            val flnMatch = FLNCurriculumDatabase.findBySantali(trimmed)
            if (flnMatch != null) {
                return TranslationResult(
                    translatedText = flnMatch.hindi,
                    confidence = "High (Curated FLN Match)",
                    matched = true,
                    requiresReview = false,
                    matchType = "exact",
                    olChikiText = flnMatch.santaliOlChiki,
                    phoneticDevanagari = flnMatch.phoneticDevanagari,
                    latinPhonetic = flnMatch.latinPhonetic,
                    engineTier = "CURATED_FLN"
                )
            }
        }

        // =========================================================================
        // PRIORITY 3: Subword & Agglutinative Morphological Matcher
        // =========================================================================
        val words = trimmed.split(Regex("\\s+"))
        if (words.size in 2..5) {
            if (source == Language.HINDI && target == Language.SANTALI) {
                val translatedWords = words.mapNotNull { word ->
                    FLNCurriculumDatabase.findByHindi(word)
                }
                if (translatedWords.size == words.size) {
                    val olChikiJoined = translatedWords.joinToString(" ") { it.santaliOlChiki }
                    val latinJoined = translatedWords.joinToString(" ") { it.latinPhonetic }
                    val devaJoined = translatedWords.joinToString(" ") { it.phoneticDevanagari.split(" ")[0] }
                    return TranslationResult(
                        translatedText = olChikiJoined,
                        confidence = "Moderate (Morphological Subword)",
                        matched = true,
                        requiresReview = true,
                        matchType = "fallback",
                        olChikiText = olChikiJoined,
                        phoneticDevanagari = "$devaJoined ($latinJoined)",
                        latinPhonetic = latinJoined,
                        engineTier = "MORPHOLOGICAL_SUBWORD"
                    )
                }
            } else if (source == Language.SANTALI && target == Language.HINDI) {
                val translatedWords = words.mapNotNull { word ->
                    FLNCurriculumDatabase.findBySantali(word)
                }
                if (translatedWords.size == words.size) {
                    val hindiJoined = translatedWords.joinToString(" ") { it.hindi }
                    return TranslationResult(
                        translatedText = hindiJoined,
                        confidence = "Moderate (Morphological Subword)",
                        matched = true,
                        requiresReview = true,
                        matchType = "fallback",
                        engineTier = "MORPHOLOGICAL_SUBWORD"
                    )
                }
            }
        }

        // =========================================================================
        // PRIORITY 4: Ol Chiki Dual-Script Transliteration
        // =========================================================================
        if (source == Language.SANTALI && target == Language.HINDI) {
            if (OlChikiTransliterator.isOlChiki(trimmed)) {
                // If student entered or spoke Ol Chiki not in FLN dictionary, provide phonetic transcription
                val latinPhonetic = OlChikiTransliterator.toLatinPhonetic(trimmed)
                val devaGuide = OlChikiTransliterator.toTeacherPhoneticHUD(trimmed)
                return TranslationResult(
                    translatedText = devaGuide,
                    confidence = "Phonetic Transliteration",
                    matched = true,
                    requiresReview = true,
                    matchType = "transliteration",
                    olChikiText = trimmed,
                    phoneticDevanagari = devaGuide,
                    latinPhonetic = latinPhonetic,
                    engineTier = "TRANSLITERATION_FALLBACK"
                )
            }
        }

        // =========================================================================
        // PRIORITY 5: 8 GB RAM Neural Machine Translation (NMT) for Arbitrary Sentences
        // =========================================================================
        val neuralResult = neuralEngine.translate(trimmed, source, target)
        if (neuralResult.matched) {
            return neuralResult
        }

        // =========================================================================
        // PRIORITY 6: No Match (Requires Teacher Review & Training Addition)
        // =========================================================================
        return TranslationResult(
            translatedText = "Translation not available offline",
            confidence = "No match",
            matched = false,
            requiresReview = true,
            matchType = "none",
            engineTier = "NONE"
        )
    }

    fun addCorrection(entry: TranslationEntry) {
        translationMemory.addCorrection(entry)
    }
}
