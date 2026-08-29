package com.alchemists.tribetalk.translation

class OfflineFLNTranslationEngine(
    val translationMemory: TranslationMemory
) : TranslationEngine {

    private val curatedDataset = listOf(
        // Hindi -> Santali
        TranslationEntry("नमस्ते", "Johar (जोहार)", Language.HINDI, Language.SANTALI, "Greeting", alternativePhrasing = listOf("हैलो", "हेलो")),
        TranslationEntry("आप कैसे हैं?", "Ceka menama? (चेका मेनामा?)", Language.HINDI, Language.SANTALI, "Greeting", alternativePhrasing = listOf("आप कैसे हैं")),
        TranslationEntry("चलो पढ़ते हैं", "Dela bon paṛhao-a (देला बोन पढ़ाओ-आ)", Language.HINDI, Language.SANTALI, "Classroom Instruction", alternativePhrasing = listOf("चलो पढ़ते हैं")),
        TranslationEntry("किताब खोलो", "Puthī jhije me (पुथी झिजे मे)", Language.HINDI, Language.SANTALI, "Classroom Instruction"),
        TranslationEntry("लिखना शुरू करो", "Ol eho b me (ओल एहो ब मे)", Language.HINDI, Language.SANTALI, "Classroom Instruction"),
        TranslationEntry("एक दो तीन चार", "Mit’ bar pe pon (मित’ बार पे पोन)", Language.HINDI, Language.SANTALI, "Numbers"),
        TranslationEntry("मुझे समझ नहीं आया", "Bañ bujhau daṛeada (बाँ बुझाउ दड़ेआदा)", Language.HINDI, Language.SANTALI, "Student Doubt"),
        TranslationEntry("क्या आप मदद कर सकते हैं?", "Cet’ aam goṛo daṛeaña? (चेत’ आम गोड़ो दड़ेआङा?)", Language.HINDI, Language.SANTALI, "Student Doubt", alternativePhrasing = listOf("क्या आप मदद कर सकते हैं")),
        TranslationEntry("शिक्षक", "Macet’ (माचेत’)", Language.HINDI, Language.SANTALI, "Basic Vocabulary"),
        TranslationEntry("छात्र", "Paṛhua (पढ़ुआ)", Language.HINDI, Language.SANTALI, "Basic Vocabulary"),
        TranslationEntry("स्कूल", "Itun oṛaḥ (इतुन ओड़ाः)", Language.HINDI, Language.SANTALI, "Basic Vocabulary"),

        // Santali -> Hindi
        TranslationEntry("Johar", "नमस्ते (Namaste)", Language.SANTALI, Language.HINDI, "Greeting", alternativePhrasing = listOf("जोहार")),
        TranslationEntry("Ceka menama?", "आप कैसे हैं? (Aap kaise hain?)", Language.SANTALI, Language.HINDI, "Greeting", alternativePhrasing = listOf("Ceka menama")),
        TranslationEntry("Dela bon paṛhao-a", "चलो पढ़ते हैं (Chalo padhte hain)", Language.SANTALI, Language.HINDI, "Classroom Instruction"),
        TranslationEntry("Puthī jhije me", "किताब खोलो (Kitab kholo)", Language.SANTALI, Language.HINDI, "Classroom Instruction"),
        TranslationEntry("Ol eho b me", "लिखना शुरू करो (Likhna shuru karo)", Language.SANTALI, Language.HINDI, "Classroom Instruction"),
        TranslationEntry("Mit’ bar pe pon", "एक दो तीन चार (Ek do teen chaar)", Language.SANTALI, Language.HINDI, "Numbers"),
        TranslationEntry("Bañ bujhau daṛeada", "मुझे समझ नहीं आया (Mujhe samajh nahi aaya)", Language.SANTALI, Language.HINDI, "Student Doubt"),
        TranslationEntry("Cet’ aam goṛo daṛeaña?", "क्या आप मदद कर सकते हैं? (Kya aap madad kar sakte hain?)", Language.SANTALI, Language.HINDI, "Student Doubt", alternativePhrasing = listOf("Cet’ aam goṛo daṛeaña")),
        TranslationEntry("Macet’", "शिक्षक (Shikshak)", Language.SANTALI, Language.HINDI, "Basic Vocabulary"),
        TranslationEntry("Paṛhua", "छात्र (Chhatra)", Language.SANTALI, Language.HINDI, "Basic Vocabulary"),
        TranslationEntry("Itun oṛaḥ", "स्कूल (School)", Language.SANTALI, Language.HINDI, "Basic Vocabulary")
    )

    fun getCuratedDatasetCount(): Int = curatedDataset.size

    fun normalizeText(text: String): String {
        return text.trim()
            .replace(Regex("\\s+"), " ")
            .lowercase()
            .replace(Regex("[?.!,]"), "")
            .trim()
    }

    override fun translate(text: String, source: Language, target: Language): TranslationResult {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return TranslationResult("", "No match", matched = false, requiresReview = false, matchType = "none")
        }

        if (source == target) {
            return TranslationResult(trimmed, "High (Same Language)", matched = true, requiresReview = false, matchType = "same_language")
        }

        // Priority 1: Check Teacher-validated translation memory
        val correction = translationMemory.findCorrection(trimmed, source, target)
        if (correction != null) {
            return TranslationResult(
                translatedText = correction.targetText,
                confidence = "High (Teacher Validated)",
                matched = true,
                requiresReview = false,
                matchType = "exact"
            )
        }

        val normalizedInput = normalizeText(trimmed)

        // Priority 2: Check Exact Curated FLN Database Matches
        val exactEntry = curatedDataset.firstOrNull { entry ->
            entry.sourceLang == source && entry.targetLang == target &&
                    (normalizeText(entry.sourceText) == normalizedInput ||
                     entry.alternativePhrasing.any { normalizeText(it) == normalizedInput })
        }

        if (exactEntry != null) {
            return TranslationResult(
                translatedText = exactEntry.targetText,
                confidence = "High (Offline Match)",
                matched = true,
                requiresReview = false,
                matchType = "exact"
            )
        }

        // Priority 3: Token/Vocabulary Fallback (where safe)
        val words = trimmed.split(Regex("\\s+"))
        if (words.size in 2..3) {
            val translatedWords = words.map { word ->
                val wordNormalized = normalizeText(word)
                val match = curatedDataset.firstOrNull { entry ->
                    entry.sourceLang == source && entry.targetLang == target &&
                            (normalizeText(entry.sourceText) == wordNormalized ||
                             entry.alternativePhrasing.any { normalizeText(it) == wordNormalized })
                }
                match?.targetText ?: ""
            }

            if (translatedWords.all { it.isNotEmpty() }) {
                val joinedText = translatedWords.joinToString(" ")
                return TranslationResult(
                    translatedText = joinedText,
                    confidence = "Low (Word-level fallback)",
                    matched = true,
                    requiresReview = true,
                    matchType = "fallback"
                )
            }
        }

        // Priority 4: No match
        return TranslationResult(
            translatedText = "Translation not available offline",
            confidence = "No match",
            matched = false,
            requiresReview = true,
            matchType = "none"
        )
    }

    fun addCorrection(entry: TranslationEntry) {
        translationMemory.addCorrection(entry)
    }
}
