package org.tribetalk.fln

import org.junit.Assert.*
import org.junit.Test
import org.tribetalk.fln.model.FlnDomain
import org.tribetalk.fln.model.FlnGrade
import org.tribetalk.fln.model.WorksheetConfig
import org.tribetalk.fln.model.WorksheetType
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
            questionCount = 5
        )
        val items = FlnCurriculumRepository.generateWorksheet(config)
        assertEquals(5, items.size)
        for (item in items) {
            assertTrue("Quantity must be >= 1", item.quantity >= 1)
            assertFalse("Right Santali label must not be blank", item.rightLabelSantali.isBlank())
            assertFalse("Left Hindi label must not be blank", item.leftLabelHindi.isBlank())
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
        }
    }

    @Test
    fun testWorksheetGenerationAssessmentCircle() {
        val config = WorksheetConfig(
            title = "Test Multiple Choice Assessment",
            type = WorksheetType.ASSESSMENT_CIRCLE,
            grade = FlnGrade.GRADE_2,
            questionCount = 4
        )
        val items = FlnCurriculumRepository.generateWorksheet(config)
        assertEquals(4, items.size)
        for (item in items) {
            assertEquals("Must have 3 multiple choice options", 3, item.options.size)
            assertTrue("Correct index must be in 0..2", item.correctIndex in 0..2)
        }
    }
}
