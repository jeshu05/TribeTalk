package org.tribetalk.fln

import org.junit.Assert.*
import org.junit.Test
import org.tribetalk.fln.generator.ProceduralCurriculumGenerator
import org.tribetalk.fln.model.*
import org.tribetalk.fln.repository.FlnCurriculumRepository

class FlnCurriculumRepositoryTest {

    @Test
    fun testAllCardsAreValidAndWellFormed() {
        val cards = FlnCurriculumRepository.getAllCards()
        assertTrue("Curriculum should have at least 25 cards", cards.size >= 25)

        for (card in cards) {
            assertFalse("Card ID must not be blank", card.id.isBlank())
            assertFalse("Hindi text must not be blank", card.hindiText.isBlank())
            assertFalse("Santali Ol Chiki text must not be blank", card.santaliOlChiki.isBlank())
            assertFalse("Teacher phonetic guide must not be blank", card.teacherPhoneticGuide.isBlank())
            assertFalse("English gloss must not be blank", card.englishGloss.isBlank())
            assertFalse("NIPUN code must not be blank", card.nipunCode.isBlank())
        }
    }

    @Test
    fun testCategoryFiltering() {
        val categories = FlnCurriculumRepository.getCategories()
        assertTrue("Categories list must include All Topics", categories.contains(FlnCurriculumRepository.CATEGORY_ALL))
        assertTrue("Categories must include Akshar", categories.contains(FlnCurriculumRepository.CATEGORY_AKSHAR))
        assertTrue("Categories must include Numbers", categories.contains(FlnCurriculumRepository.CATEGORY_NUMBERS))
        assertTrue("Categories must include Animals", categories.contains(FlnCurriculumRepository.CATEGORY_ANIMALS))

        val animalCards = FlnCurriculumRepository.getCardsByCategory(FlnCurriculumRepository.CATEGORY_ANIMALS)
        assertTrue("Animal cards must not be empty", animalCards.isNotEmpty())
        for (card in animalCards) {
            assertEquals(FlnCurriculumRepository.CATEGORY_ANIMALS, card.category)
        }
    }

    @Test
    fun testNumeracyCardsHaveValues() {
        val numberCards = FlnCurriculumRepository.getCardsByCategory(FlnCurriculumRepository.CATEGORY_NUMBERS)
        assertTrue("Number cards must exist", numberCards.isNotEmpty())
        for (card in numberCards) {
            assertEquals(FlnDomain.NUMERACY_COUNTING, card.domain)
            assertNotNull("Number card should have numeralValue", card.numeralValue)
            assertTrue("Numeral value must be >= 1", card.numeralValue!! >= 1)
        }
    }

    @Test
    fun testWorksheetGenerationCountAndMatch() {
        val config = WorksheetConfig(
            title = "Test Count & Match",
            type = WorksheetType.COUNT_AND_MATCH,
            grade = FlnGrade.GRADE_1,
            difficulty = WorksheetDifficulty.EASY,
            questionCount = 5
        )
        val items = FlnCurriculumRepository.generateWorksheet(config)
        assertEquals(5, items.size)
        for (item in items) {
            assertTrue("Quantity must be >= 1", item.quantity >= 1)
            assertFalse("Right Santali label must not be blank", item.rightLabelSantali.isBlank())
            assertFalse("Left Hindi label must not be blank", item.leftLabelHindi.isBlank())
            assertFalse("Teacher phonetic guide must not be blank", item.teacherPhoneticAnswer.isBlank())
        }
    }

    @Test
    fun testWorksheetGenerationPictureWordMatch() {
        val config = WorksheetConfig(
            title = "Test Picture Word",
            type = WorksheetType.PICTURE_WORD_MATCH,
            grade = FlnGrade.BALVATIKA,
            questionCount = 4
        )
        val items = FlnCurriculumRepository.generateWorksheet(config)
        assertEquals(4, items.size)
        for (item in items) {
            assertFalse("Left Hindi label must not be blank", item.leftLabelHindi.isBlank())
            assertFalse("Right Santali label must not be blank", item.rightLabelSantali.isBlank())
            assertFalse("Teacher phonetic guide must not be blank", item.teacherPhoneticAnswer.isBlank())
        }
    }

    @Test
    fun testWorksheetGenerationAdditionWordProblem() {
        val config = WorksheetConfig(
            title = "Test Addition Math",
            type = WorksheetType.ADDITION_WORD_PROBLEM,
            grade = FlnGrade.GRADE_1,
            difficulty = WorksheetDifficulty.MEDIUM,
            questionCount = 4
        )
        val items = FlnCurriculumRepository.generateWorksheet(config)
        assertEquals(4, items.size)
        for (item in items) {
            assertEquals("+", item.operationSign)
            assertTrue("Quantity 1 must be >= 1", item.quantity >= 1)
            assertTrue("Quantity 2 must be >= 1", item.secondaryQuantity >= 1)
            assertNotNull("Math answer must not be null", item.mathAnswer)
            assertEquals("Math equation must be correct", item.quantity + item.secondaryQuantity, item.mathAnswer)
            assertFalse("Hindi story prompt must not be blank", item.promptHindi.isBlank())
            assertFalse("Santali story prompt must not be blank", item.promptSantali.isBlank())
        }
    }

    @Test
    fun testWorksheetGenerationNumberSequenceTrain() {
        val config = WorksheetConfig(
            title = "Test Number Train",
            type = WorksheetType.NUMBER_SEQUENCE_TRAIN,
            grade = FlnGrade.GRADE_1,
            questionCount = 3
        )
        val items = FlnCurriculumRepository.generateWorksheet(config)
        assertEquals(3, items.size)
        for (item in items) {
            assertEquals(5, item.sequenceItems.size)
            assertTrue("Missing sequence index must be in 1..3", item.missingSequenceIndex in 1..3)
            assertEquals("__", item.sequenceItems[item.missingSequenceIndex])
            assertNotNull("Math answer must be present", item.mathAnswer)
        }
    }

    @Test
    fun testWorksheetGenerationGreaterLesserCompare() {
        val config = WorksheetConfig(
            title = "Test Compare Groups",
            type = WorksheetType.GREATER_LESSER_COMPARE,
            grade = FlnGrade.BALVATIKA,
            questionCount = 4
        )
        val items = FlnCurriculumRepository.generateWorksheet(config)
        assertEquals(4, items.size)
        for (item in items) {
            val expectedSign = when {
                item.quantity > item.secondaryQuantity -> ">"
                item.quantity < item.secondaryQuantity -> "<"
                else -> "="
            }
            assertEquals(expectedSign, item.operationSign)
        }
    }

    @Test
    fun testWorksheetGenerationMissingAksharSpelling() {
        val config = WorksheetConfig(
            title = "Test Missing Akshar",
            type = WorksheetType.MISSING_AKSHAR_SPELLING,
            grade = FlnGrade.GRADE_2,
            questionCount = 4
        )
        val items = FlnCurriculumRepository.generateWorksheet(config)
        assertEquals(4, items.size)
        for (item in items) {
            assertNotNull("Word with blank must not be null", item.wordWithBlank)
            assertTrue("Word must contain blank placeholder", item.wordWithBlank!!.contains("[ _ ]"))
            assertNotNull("Missing letter answer must not be null", item.missingLetterAnswer)
            assertEquals("Must have 4 option choices", 4, item.options.size)
            assertTrue("Options must contain the correct missing letter", item.options.contains(item.missingLetterAnswer))
            assertEquals(item.missingLetterAnswer, item.options[item.correctIndex])
        }
    }

    @Test
    fun testDeterministicSeedReproducibility() {
        val seed = 123456789L
        val config1 = WorksheetConfig(
            type = WorksheetType.ADDITION_WORD_PROBLEM,
            questionCount = 5,
            seed = seed
        )
        val config2 = WorksheetConfig(
            type = WorksheetType.ADDITION_WORD_PROBLEM,
            questionCount = 5,
            seed = seed
        )

        val items1 = FlnCurriculumRepository.generateWorksheet(config1)
        val items2 = FlnCurriculumRepository.generateWorksheet(config2)

        assertEquals(items1.size, items2.size)
        for (i in items1.indices) {
            assertEquals(items1[i].quantity, items2[i].quantity)
            assertEquals(items1[i].secondaryQuantity, items2[i].secondaryQuantity)
            assertEquals(items1[i].mathAnswer, items2[i].mathAnswer)
            assertEquals(items1[i].promptHindi, items2[i].promptHindi)
        }
    }

    @Test
    fun testCustomFlashcardSynthesis() {
        val card = ProceduralCurriculumGenerator.synthesizeCard("नदी (Water stream)")
        assertNotNull(card)
        assertTrue(card.isCustomUserGenerated)
        assertFalse(card.santaliOlChiki.isBlank())
        assertFalse(card.teacherPhoneticGuide.isBlank())

        FlnCurriculumRepository.addCustomCard(card)
        val customCards = FlnCurriculumRepository.getCardsByCategory(FlnCurriculumRepository.CATEGORY_CUSTOM)
        assertTrue(customCards.any { it.id == card.id })
    }
}
