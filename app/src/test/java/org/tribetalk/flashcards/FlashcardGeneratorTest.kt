package org.tribetalk.flashcards

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FlashcardGeneratorTest {

    private lateinit var generator: FlashcardGenerator

    @Before
    fun setUp() {
        generator = FlashcardGenerator()
    }

    @Test
    fun testFlashcardModelCreation() {
        val card = Flashcard(
            setId = "set-123",
            topic = "Animals",
            skill = "Word-Picture Association",
            hindiText = "गाय",
            santaliText = "Gại",
            santaliOlChiki = "ᱜᱟᱹᱭ",
            phoneticGuide = "गाई",
            imageEmoji = "🐄",
            order = 1
        )

        assertEquals("set-123", card.setId)
        assertEquals("Animals", card.topic)
        assertEquals("गाय", card.hindiText)
        assertEquals("Gại", card.santaliText)
        assertEquals("ᱜᱟᱹᱭ", card.santaliOlChiki)
        assertEquals("गाई", card.phoneticGuide)
        assertEquals("🐄", card.imageEmoji)
        assertEquals(1, card.order)
        assertTrue(card.createdAt > 0)
    }

    @Test
    fun testFlashcardSetCreation() {
        val set = FlashcardSet(
            title = "Animals Around Us",
            topic = "Animals",
            domain = "Foundational Literacy & Oral Language",
            learningOutcome = "Word-Picture Association (शब्द-चित्र संबंध)",
            grade = "Grade 1-3",
            cards = listOf(
                Flashcard(hindiText = "गाय", santaliText = "Gại", order = 1),
                Flashcard(hindiText = "कुत्ता", santaliText = "Seta", order = 2)
            )
        )

        assertEquals("Animals Around Us", set.title)
        assertEquals("Animals", set.topic)
        assertEquals("Foundational Literacy & Oral Language", set.domain)
        assertEquals("Grade 1-3", set.grade)
        assertEquals(2, set.cards.size)
        assertEquals(1, set.cards[0].order)
        assertEquals(2, set.cards[1].order)
    }

    @Test
    fun testPresetTopicGenerationFromCuratedVocabulary() {
        val set = generator.createPresetSet(
            topic = "Animals (पशु-पक्षी)",
            domain = "Foundational Literacy & Oral Language",
            skill = "Oral Vocabulary & Word Meaning",
            grade = "Grade 2"
        )

        assertNotNull(set)
        assertEquals("Animals (पशु-पक्षी) Flashcards", set.title)
        assertEquals(5, set.cards.size)

        // Verify content of first card (Cow / गाय)
        val firstCard = set.cards.first()
        assertEquals("गाय", firstCard.hindiText)
        assertEquals("Gại", firstCard.santaliText)
        assertEquals("ᱜᱟᱹᱭ", firstCard.santaliOlChiki)
        assertEquals("गाई", firstCard.phoneticGuide)
        assertEquals("🐄", firstCard.imageEmoji)
        assertNotNull(firstCard.exampleSentenceHindi)
        assertNotNull(firstCard.exampleSentenceSantali)
    }

    @Test
    fun testCurriculumDatabaseIntegrationAcrossCategories() {
        val categories = listOf(
            "Animals (पशु-पक्षी)",
            "Classroom (कक्षा)",
            "Nature (प्रकृति एवं परिवेश)",
            "Numbers (संख्या ज्ञान)"
        )

        for (category in categories) {
            val set = generator.createPresetSet(category)
            assertTrue("Set for $category should have 5 cards", set.cards.size >= 5)
            for (card in set.cards) {
                assertTrue("Hindi text should not be blank", card.hindiText.isNotBlank())
                assertTrue("Santali text should not be blank", card.santaliText.isNotBlank())
                assertTrue("Ol Chiki should not be blank", card.santaliOlChiki?.isNotBlank() == true)
                assertNotNull("Emoji should be provided", card.imageEmoji)
            }
        }
    }

    @Test
    fun testMissingTranslationFallbackDoesNotInventSantali() {
        val unknownWord = "अज्ञातविचित्रशब्द९९९"
        val translated = generator.retranslateCard(unknownWord)

        assertEquals("Translation unavailable", translated.santaliText)
        assertNull(translated.santaliOlChiki)
        assertNull(translated.phoneticGuide)
    }

    @Test
    fun testHindiEditTriggersSantaliRetranslation() {
        val editedWord = "किताब"
        val result = generator.retranslateCard(editedWord)

        assertTrue("Should return Santali translation", result.santaliText.isNotBlank())
        assertNotEquals("Translation unavailable", result.santaliText)
        assertTrue(result.santaliText.contains("Puth", ignoreCase = true) || result.santaliOlChiki?.contains("ᱯᱩᱛᱷᱤ") == true)
    }

    @Test
    fun testTeacherManualSantaliEditPreservation() {
        val originalCard = Flashcard(
            hindiText = "गाय",
            santaliText = "Gại",
            santaliOlChiki = "ᱜᱟᱹᱭ",
            order = 1
        )

        // Teacher manually overrides Santali text
        val editedCard = originalCard.copy(
            santaliText = "Custom Gai Text",
            santaliOlChiki = "ᱜᱟᱹᱭ (ᱥᱟᱹᱨᱤ)"
        )

        assertEquals("गाय", editedCard.hindiText)
        assertEquals("Custom Gai Text", editedCard.santaliText)
        assertEquals("ᱜᱟᱹᱭ (ᱥᱟᱹᱨᱤ)", editedCard.santaliOlChiki)
    }

    @Test
    fun testCustomSetCreationWithWordList() {
        val customWords = listOf("गाय", "किताब", "पानी", "सूरज")
        val customSet = generator.createCustomSet(
            title = "My Custom Deck",
            topic = "Daily Objects",
            domain = "Foundational Literacy",
            skill = "Word Recognition",
            grade = "Grade 1",
            hindiWords = customWords
        )

        assertEquals("My Custom Deck", customSet.title)
        assertEquals("Daily Objects", customSet.topic)
        assertEquals(4, customSet.cards.size)

        assertEquals("गाय", customSet.cards[0].hindiText)
        assertEquals("किताब", customSet.cards[1].hindiText)
        assertEquals("पानी", customSet.cards[2].hindiText)
        assertEquals("सूरज", customSet.cards[3].hindiText)

        for (i in 0 until 4) {
            assertEquals(i + 1, customSet.cards[i].order)
            assertTrue(customSet.cards[i].santaliText.isNotBlank())
        }
    }

    @Test
    fun testEmptyInputValidation() {
        val emptyResult = generator.retranslateCard("   ")
        assertEquals("", emptyResult.santaliText)
        assertNull(emptyResult.santaliOlChiki)
        assertNull(emptyResult.phoneticGuide)
    }
}
