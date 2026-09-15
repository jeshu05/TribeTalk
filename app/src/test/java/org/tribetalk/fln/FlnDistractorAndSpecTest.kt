package org.tribetalk.fln

import org.junit.Assert.*
import org.junit.Test
import org.tribetalk.curriculum.spec.*
import org.tribetalk.fln.model.*
import org.tribetalk.fln.repository.FlnCurriculumRepository
import org.tribetalk.fln.worksheet.WorksheetGenerator

class FlnDistractorAndSpecTest {

    @Test
    fun testDistractorGenerationForNumbers() {
        val numberCard = FlnCurriculumRepository.getAllCards().first { it.category == FlnCategory.NUMBERS && it.numeralValue != null }
        val distractors = FlnCurriculumRepository.generateDistractorsForCard(numberCard, 3)

        assertEquals("Must return exactly 3 distractors", 3, distractors.size)
        assertFalse("Distractors must not contain the target number", distractors.contains(numberCard.numeralValue.toString()))
        assertEquals("Distractors must be unique", 3, distractors.distinct().size)

        // Verify distractors are valid integers
        distractors.forEach { distractor ->
            assertTrue("Distractor must be a valid number: $distractor", distractor.toIntOrNull() != null)
        }
    }

    @Test
    fun testDistractorGenerationForAkshar() {
        val aksharCard = FlnCurriculumRepository.getAllCards().first { it.category == FlnCategory.AKSHAR }
        val distractors = FlnCurriculumRepository.generateDistractorsForCard(aksharCard, 3)

        assertEquals("Must return exactly 3 distractors", 3, distractors.size)
        assertFalse("Distractors must not contain target letter", distractors.contains(aksharCard.santaliOlChiki))
        assertEquals("Distractors must be unique", 3, distractors.distinct().size)

        // All distractors should be valid Ol Chiki akshars
        val allAksharLetters = FlnCurriculumRepository.getCardsByCategory(FlnCategory.AKSHAR).map { it.santaliOlChiki }.toSet()
        distractors.forEach { distractor ->
            assertTrue("Distractor '$distractor' must belong to Ol Chiki alphabet", allAksharLetters.contains(distractor))
        }
    }

    @Test
    fun testDistractorGenerationForVocabularyWords() {
        val animalCard = FlnCurriculumRepository.getAllCards().first { it.category == FlnCategory.ANIMALS }
        val distractors = FlnCurriculumRepository.generateDistractorsForCard(animalCard, 3)

        assertEquals("Must return exactly 3 distractors", 3, distractors.size)
        assertFalse("Distractors must not contain target word", distractors.contains(animalCard.santaliOlChiki))
        assertEquals("Distractors must be unique", 3, distractors.distinct().size)

        // Ensure options combined with target produce exactly 4 distinct choices
        val combined = (listOf(animalCard.santaliOlChiki) + distractors).distinct()
        assertEquals("Target plus 3 distractors must produce 4 distinct options", 4, combined.size)
    }

    @Test
    fun testWorksheetGeneratorFromActivitySpecAddition() {
        val spec = ActivitySpec(
            id = "test_add_spec",
            objectiveId = "NIPUN_G1_MATH_ADD",
            activityType = ActivityType.SIMPLE_ADDITION,
            items = listOf(
                ActivityItemSpec(
                    id = "item_add_1",
                    visualAsset = "mango",
                    quantity = 4,
                    secondaryQuantity = 3,
                    options = listOf("5", "6", "7", "8"),
                    correctAnswer = "7"
                )
            ),
            visualSpec = VisualSpec(primaryAssetKey = "mango"),
            languageSpec = LanguageSpec(instructionHindi = "कुल जोड़ें", instructionSantali = "ᱡᱚᱛᱚ ᱡᱚᱢᱟ ᱢᱮ"),
            answerSpec = AnswerSpec(correctValue = "7", numericValue = 7),
            metadata = ActivityMetadata("ws_1", "act_1", 123L)
        )

        val config = WorksheetConfig(
            type = WorksheetType.ADDITION_WORD_PROBLEM,
            questionCount = 4
        )
        val items = WorksheetGenerator.generateFromActivitySpec(spec, config)
        assertEquals("Should generate 4 items", 4, items.size)
        for (item in items) {
            assertNotNull("mathAnswer must be populated", item.mathAnswer)
            assertTrue("mathAnswer must be > 0", item.mathAnswer!! > 0)
            assertEquals("mango", item.iconType)
            assertEquals("+", item.operationSign)
            assertTrue("options must contain answer", item.options.contains(item.mathAnswer.toString()))
        }
    }

    @Test
    fun testWorksheetGeneratorFromActivitySpecSubtraction() {
        val spec = ActivitySpec(
            id = "test_sub_spec",
            objectiveId = "NIPUN_G1_MATH_SUB",
            activityType = ActivityType.COMPARE_QUANTITIES,
            items = listOf(
                ActivityItemSpec(
                    id = "item_sub_1",
                    visualAsset = "tree",
                    quantity = 6,
                    secondaryQuantity = 2,
                    options = listOf("3", "4", "5", "6"),
                    correctAnswer = "4"
                )
            ),
            visualSpec = VisualSpec(primaryAssetKey = "tree"),
            languageSpec = LanguageSpec(instructionHindi = "घटाएं", instructionSantali = "ᱵᱷᱮᱜᱟᱨ ᱢᱮ"),
            answerSpec = AnswerSpec(correctValue = "4", numericValue = 4),
            metadata = ActivityMetadata("ws_1", "act_2", 124L)
        )

        val config = WorksheetConfig(
            type = WorksheetType.SUBTRACTION_PROBLEM,
            questionCount = 3
        )
        val items = WorksheetGenerator.generateFromActivitySpec(spec, config)
        assertEquals(3, items.size)
        for (item in items) {
            assertNotNull("mathAnswer must be populated", item.mathAnswer)
            assertTrue(item.mathAnswer!! >= 1)
            assertEquals("tree", item.iconType)
            assertEquals("-", item.operationSign)
            assertTrue("options must contain answer", item.options.contains(item.mathAnswer.toString()))
        }
    }

    @Test
    fun testWorksheetGeneratorFromActivitySpecMultiplication() {
        val spec = ActivitySpec(
            id = "test_mul_spec",
            objectiveId = "NIPUN_G2_MATH_MULT",
            activityType = ActivityType.SIMPLE_ADDITION,
            items = listOf(
                ActivityItemSpec(
                    id = "item_mul_1",
                    visualAsset = "star",
                    quantity = 3,
                    secondaryQuantity = 4,
                    options = listOf("10", "12", "14", "16"),
                    correctAnswer = "12"
                )
            ),
            visualSpec = VisualSpec(primaryAssetKey = "star"),
            languageSpec = LanguageSpec(instructionHindi = "समूह गिनें", instructionSantali = "ᱜᱩᱴ ᱞᱮᱠᱷᱟᱭ ᱢᱮ"),
            answerSpec = AnswerSpec(correctValue = "12", numericValue = 12),
            metadata = ActivityMetadata("ws_1", "act_3", 125L)
        )

        val config = WorksheetConfig(
            type = WorksheetType.MULTIPLICATION_GROUPS,
            questionCount = 3
        )
        val items = WorksheetGenerator.generateFromActivitySpec(spec, config)
        assertEquals(3, items.size)
        for (item in items) {
            assertNotNull("mathAnswer must be populated", item.mathAnswer)
            assertTrue(item.mathAnswer!! >= 4)
            assertEquals("star", item.iconType)
            assertTrue("options must contain answer", item.options.contains(item.mathAnswer.toString()))
        }
    }

    @Test
    fun testNumberCardsHaveValidNumeralAndCountingQuantity() {
        val numberCards = FlnCurriculumRepository.getCardsByCategory(FlnCategory.NUMBERS)
        assertEquals("Must have 20 progressive number cards", 20, numberCards.size)

        for (card in numberCards) {
            assertNotNull("Numeral value must not be null for ${card.id}", card.numeralValue)
            assertEquals("numeralValue must match countingQuantity for ${card.id}", card.numeralValue, card.countingQuantity)
            assertTrue("Quantity must be in 1..20", card.countingQuantity in 1..20)
            val olDigit = FlnCurriculumRepository.toOlChikiDigits(card.numeralValue!!)
            assertTrue("Santali Ol Chiki text must contain Ol Chiki digit $olDigit", card.santaliOlChiki.contains(olDigit))
        }
    }

    @Test
    fun testNumberCardsBilingualRealization() {
        assertEquals("0 in Ol Chiki", "᱐", FlnCurriculumRepository.toOlChikiDigits(0))
        assertEquals("1 in Ol Chiki", "᱑", FlnCurriculumRepository.toOlChikiDigits(1))
        assertEquals("5 in Ol Chiki", "᱕", FlnCurriculumRepository.toOlChikiDigits(5))
        assertEquals("10 in Ol Chiki", "᱑᱐", FlnCurriculumRepository.toOlChikiDigits(10))
        assertEquals("20 in Ol Chiki", "᱒᱐", FlnCurriculumRepository.toOlChikiDigits(20))

        val bilingualLabel = "${FlnCurriculumRepository.toOlChikiDigits(5)} (5)"
        assertEquals("᱕ (5)", bilingualLabel)
    }

    @Test
    fun testCountAndMatchWorksheetGenerationFidelity() {
        val config = WorksheetConfig(
            type = WorksheetType.COUNT_AND_MATCH,
            grade = FlnGrade.BALVATIKA,
            questionCount = 6
        )
        val worksheetItems = WorksheetGenerator.generateWorksheet(config)
        assertEquals(6, worksheetItems.size)

        for (item in worksheetItems) {
            assertTrue("Item quantity must be between 1 and 12", item.quantity in 1..12)
            assertTrue("Icon type must be present", item.iconType.isNotBlank())
            assertTrue("Options must contain the item quantity", item.options.contains(item.quantity.toString()))
            assertEquals("Expected answer must match quantity", item.quantity.toString(), item.options[item.correctIndex])
        }
    }
}
