package com.alchemists.tribetalk.flashcards

import com.alchemists.tribetalk.translation.FLNCurriculumDatabase
import com.alchemists.tribetalk.translation.Language
import com.alchemists.tribetalk.translation.TranslationEngine
import java.util.UUID

/**
 * Result data class for card re-translation.
 */
data class TranslatedCardData(
    val santaliText: String,
    val santaliOlChiki: String?,
    val phoneticGuide: String?
)

/**
 * Flashcard Generator for NIPUN Bharat / FLN bilingual card sets.
 *
 * Integrates the existing offline TranslationEngine and FLNCurriculumDatabase.
 */
class FlashcardGenerator(
    private val translationEngine: TranslationEngine
) {

    /**
     * Creates a quick flashcard set from curated FLN vocabulary templates.
     */
    fun createPresetSet(
        topic: String,
        domain: String = "Foundational Literacy & Oral Language",
        skill: String = "Word-Picture Association (शब्द-चित्र संबंध)",
        grade: String? = "Grade 1-3"
    ): FlashcardSet {
        val setId = UUID.randomUUID().toString()
        val templateList = NIPUNLearningFramework.presetTopicTemplates[topic]
            ?: NIPUNLearningFramework.presetTopicTemplates.values.first()

        val cards = templateList.mapIndexed { index, template ->
            Flashcard(
                id = UUID.randomUUID().toString(),
                setId = setId,
                topic = topic,
                skill = skill,
                hindiText = template.hindi,
                santaliText = template.santaliLatin,
                santaliOlChiki = template.santaliOlChiki,
                phoneticGuide = template.phoneticDevanagari,
                imageEmoji = template.emoji,
                exampleSentenceHindi = template.exampleSentenceHindi,
                exampleSentenceSantali = template.exampleSentenceSantali,
                order = index + 1
            )
        }

        return FlashcardSet(
            id = setId,
            title = "$topic Flashcards",
            topic = topic,
            domain = domain,
            learningOutcome = skill,
            grade = grade,
            cards = cards
        )
    }

    /**
     * Creates a flashcard set from a teacher's custom list of Hindi words/concepts.
     */
    fun createCustomSet(
        title: String,
        topic: String,
        domain: String,
        skill: String,
        grade: String?,
        hindiWords: List<String>
    ): FlashcardSet {
        val setId = UUID.randomUUID().toString()
        val effectiveTitle = if (title.isNotBlank()) title.trim() else "$topic Flashcard Set"
        val effectiveTopic = if (topic.isNotBlank()) topic.trim() else "FLN Topic"

        val cards = hindiWords.filter { it.isNotBlank() }.mapIndexed { index, hindiWord ->
            val translated = retranslateCard(hindiWord.trim())
            val emoji = getEmojiForWord(hindiWord.trim())

            Flashcard(
                id = UUID.randomUUID().toString(),
                setId = setId,
                topic = effectiveTopic,
                skill = skill,
                hindiText = hindiWord.trim(),
                santaliText = translated.santaliText,
                santaliOlChiki = translated.santaliOlChiki,
                phoneticGuide = translated.phoneticGuide,
                imageEmoji = emoji,
                order = index + 1
            )
        }

        return FlashcardSet(
            id = setId,
            title = effectiveTitle,
            topic = effectiveTopic,
            domain = domain,
            learningOutcome = skill,
            grade = grade,
            cards = cards
        )
    }

    /**
     * Translates a single Hindi text into Santali and resolves Ol Chiki / Phonetic guide.
     */
    fun retranslateCard(hindiText: String): TranslatedCardData {
        val trimmed = hindiText.trim()
        if (trimmed.isEmpty()) {
            return TranslatedCardData("", null, null)
        }

        // 1. Direct FLN Curriculum match
        val flnItem = FLNCurriculumDatabase.findByHindi(trimmed)
        if (flnItem != null) {
            return TranslatedCardData(
                santaliText = flnItem.latinPhonetic,
                santaliOlChiki = flnItem.santaliOlChiki,
                phoneticGuide = flnItem.phoneticDevanagari
            )
        }

        // 2. Query TranslationEngine
        try {
            val result = translationEngine.translate(trimmed, Language.HINDI, Language.SANTALI)
            if (result.matched && result.translatedText.isNotBlank() && !result.translatedText.contains("not available", ignoreCase = true)) {
                // If the translation engine returns Ol Chiki characters
                val isOlChiki = result.translatedText.any { it in '\u1C50'..'\u1C7F' }
                return if (isOlChiki) {
                    TranslatedCardData(
                        santaliText = result.translatedText,
                        santaliOlChiki = result.translatedText,
                        phoneticGuide = null
                    )
                } else {
                    TranslatedCardData(
                        santaliText = result.translatedText,
                        santaliOlChiki = null,
                        phoneticGuide = null
                    )
                }
            }
        } catch (_: Exception) {
            // Graceful fallback
        }

        return TranslatedCardData(
            santaliText = "Translation unavailable",
            santaliOlChiki = null,
            phoneticGuide = null
        )
    }

    private fun getEmojiForWord(word: String): String {
        return when {
            word.contains("गाय") -> "🐄"
            word.contains("कुत्ता") -> "🐕"
            word.contains("मछली") -> "🐟"
            word.contains("चिड़िया") || word.contains("पक्षी") -> "🐦"
            word.contains("बाघ") || word.contains("शेर") -> "🐅"
            word.contains("किताब") || word.contains("पुस्तक") -> "📖"
            word.contains("कलम") || word.contains("पेन") -> "✏️"
            word.contains("स्कूल") || word.contains("विद्यालय") -> "🏫"
            word.contains("शिक्षक") || word.contains("अध्यापक") -> "👨‍🏫"
            word.contains("छात्र") || word.contains("विद्यार्थी") -> "🧑‍🎓"
            word.contains("पेड़") || word.contains("वृक्ष") -> "🌳"
            word.contains("पानी") || word.contains("जल") -> "💧"
            word.contains("सूरज") || word.contains("सूर्य") -> "☀️"
            word.contains("चाँद") || word.contains("चंद्रमा") -> "🌙"
            word.contains("घर") || word.contains("मकान") -> "🏠"
            word.contains("एक") -> "1️⃣"
            word.contains("दो") -> "2️⃣"
            word.contains("तीन") -> "3️⃣"
            word.contains("चार") -> "4️⃣"
            word.contains("पांच") -> "5️⃣"
            else -> "🎴"
        }
    }
}
