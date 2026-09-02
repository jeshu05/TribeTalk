package com.alchemists.tribetalk.curriculum

import com.alchemists.tribetalk.curriculum.assets.LocalVisualAssetLibrary
import com.alchemists.tribetalk.curriculum.generator.FLNActivityGenerator
import com.alchemists.tribetalk.curriculum.generator.FLNFlashcardGenerator
import com.alchemists.tribetalk.curriculum.generator.FLNWorksheetGenerator
import com.alchemists.tribetalk.curriculum.models.*
import com.alchemists.tribetalk.curriculum.repository.FLNCurriculumRepository
import com.alchemists.tribetalk.curriculum.validation.ActivityValidator
import org.junit.Assert.*
import org.junit.Test

class FLNStage2Test {

    private val sampleOutcome = LearningOutcome(
        id = "lo_num_01",
        nipunCode = "NIPUN-NUM-M1",
        descriptionHindi = "छात्र 1 से 10 तक वस्तुओं को सही क्रम में गिन सकते हैं।",
        descriptionSantali = "ᱯᱟᱹᱴᱷᱩᱣᱟᱹ ᱠᱚ 1 ᱠᱷᱚᱱ 10 ᱦᱟᱹᱵᱤᱡ ᱡᱤᱱᱤᱥ ᱴᱷᱤᱠ ᱞᱮᱠᱟᱛᱮ ᱠᱚ ᱞᱮᱠᱷᱟ ᱫᱟᱲᱮᱭᱟᱜᱼᱟ᱾",
        descriptionPhonetic = "पाठुआ को 1 खोन 10 हाबिज जिनिस ठीक लेकाते को लेखा दाड़ेयाआ।"
    )

    @Test
    fun testAllTenActivityTypesGeneration() {
        val types = listOf(
            ActivityType.COUNT_OBJECTS,
            ActivityType.NUMBER_RECOGNITION,
            ActivityType.NUMBER_SELECTION,
            ActivityType.ADDITION,
            ActivityType.SUBTRACTION,
            ActivityType.IDENTIFY_SHAPE,
            ActivityType.IDENTIFY_COLOUR,
            ActivityType.PICTURE_WORD_MATCH,
            ActivityType.MATCHING,
            ActivityType.SIMPLE_PATTERN
        )

        types.forEachIndexed { idx, type ->
            val item = FLNActivityGenerator.generateActivity(
                outcome = sampleOutcome,
                type = type,
                difficulty = DifficultyLevel.BEGINNER,
                seed = 1000L + idx * 42L
            )

            assertNotNull("Generated item for $type should not be null", item)
            assertEquals("Learning outcome ID must be preserved", sampleOutcome.id, item.learningOutcomeId)
            assertEquals("NIPUN code must be preserved", sampleOutcome.nipunCode, item.nipunCode)
            assertTrue("Hindi question must be non-empty", item.questionHindi.isNotBlank())
            assertTrue("Santali question must be non-empty", item.questionSantali.isNotBlank())
            assertTrue("Options Hindi must be non-empty", item.optionsHindi.isNotEmpty())

            val validation = ActivityValidator.validate(item)
            assertTrue("Generated item for $type must pass validation: ${validation.errorMessage}", validation.isValid)
        }
    }

    @Test
    fun testMathValidationAndCorrectness() {
        // Addition Activity Math Verification
        val addActivity = FLNActivityGenerator.generateActivity(
            outcome = sampleOutcome,
            type = ActivityType.ADDITION,
            difficulty = DifficultyLevel.BEGINNER,
            seed = 12345L
        )

        val addMatch = Regex("""(\d+)\s*\+\s*(\d+)""").find(addActivity.questionHindi)
        assertNotNull("Addition question should contain op1 + op2 pattern", addMatch)
        val op1 = addMatch!!.groupValues[1].toInt()
        val op2 = addMatch.groupValues[2].toInt()
        val expectedSum = op1 + op2
        val actualAnsVal = addActivity.correctAnswerValue.filter { it.isDigit() }.toInt()

        assertEquals("Addition math op1 + op2 must equal correct answer value", expectedSum, actualAnsVal)

        // Subtraction Activity Math Verification
        val subActivity = FLNActivityGenerator.generateActivity(
            outcome = sampleOutcome,
            type = ActivityType.SUBTRACTION,
            difficulty = DifficultyLevel.BEGINNER,
            seed = 54321L
        )

        val subMatch = Regex("""(\d+)\s*-\s*(\d+)""").find(subActivity.questionHindi)
        assertNotNull("Subtraction question should contain op1 - op2 pattern", subMatch)
        val subOp1 = subMatch!!.groupValues[1].toInt()
        val subOp2 = subMatch.groupValues[2].toInt()
        val expectedDiff = subOp1 - subOp2
        val actualDiffVal = subActivity.correctAnswerValue.filter { it.isDigit() }.toInt()

        assertEquals("Subtraction math op1 - op2 must equal correct answer value", expectedDiff, actualDiffVal)
    }

    @Test
    fun testActivityValidatorRejection() {
        val invalidItem = GeneratedActivityItem(
            questionId = "invalid_01",
            learningOutcomeId = "lo_num_01",
            nipunCode = "NIPUN-NUM-M1",
            grade = "Grade 1",
            subject = "Numeracy",
            domain = FLNDomain.NUMERACY,
            topic = "Addition",
            difficulty = DifficultyLevel.BEGINNER,
            activityType = ActivityType.ADDITION,
            stimulus = null,
            questionHindi = "2 + 2 कितना होता है?",
            questionSantali = "2 + 2 ᱛᱤᱱᱟᱹᱜ ᱦᱩᱭᱩᱜᱼᱟ?",
            questionPhonetic = "2 + 2",
            optionsHindi = listOf("5", "4", "3"),
            optionsSantali = listOf("5", "4", "3"),
            correctAnswerIndex = 0, // Wrong! 2+2=4 is at index 1
            correctAnswerValue = "5",
            explanationHindi = "Wrong math"
        )

        val result = ActivityValidator.validate(invalidItem)
        assertFalse("ActivityValidator should reject item with incorrect addition math", result.isValid)
        assertTrue("Error message should mention addition math error", result.errorMessage?.contains("Addition math error") == true)
    }

    @Test
    fun testControlledRandomizationAndRegeneration() {
        val seed1 = 1111L
        val seed2 = 9999L

        val activity1 = FLNActivityGenerator.generateActivity(sampleOutcome, ActivityType.COUNT_OBJECTS, seed = seed1)
        val activity2 = FLNActivityGenerator.generateActivity(sampleOutcome, ActivityType.COUNT_OBJECTS, seed = seed2)

        assertNotEquals("Question IDs for different seeds should be distinct", activity1.questionId, activity2.questionId)
        assertNotEquals("Visual stimuli counts or items should differ", activity1.correctAnswerValue, activity2.correctAnswerValue)
        assertEquals("Learning outcome ID must remain identical", activity1.learningOutcomeId, activity2.learningOutcomeId)
    }

    @Test
    fun testWorksheetAndFlashcardGenerator() {
        val worksheet = FLNWorksheetGenerator.generateWorksheet(
            outcome = sampleOutcome,
            grade = "Grade 1",
            domain = FLNDomain.NUMERACY,
            numQuestions = 4,
            difficulty = DifficultyLevel.BEGINNER,
            seed = 7777L
        )

        assertNotNull("Generated worksheet should not be null", worksheet)
        assertEquals("Worksheet should contain 4 items", 4, worksheet.items.size)
        assertEquals("Worksheet NIPUN code must match outcome", sampleOutcome.nipunCode, worksheet.nipunCode)

        val flashcards = FLNFlashcardGenerator.generateFlashcards(sampleOutcome, count = 6, seed = 7777L)
        assertNotNull("Generated flashcards should not be null", flashcards)
        assertEquals("Flashcard deck size should be 6", 6, flashcards.size)
        flashcards.forEach { fc ->
            assertEquals("Flashcard outcome ID must match", sampleOutcome.id, fc.learningOutcomeId)
            assertTrue("Flashcard Ol Chiki text must be non-empty", fc.textSantali.isNotBlank())
        }
    }

    @Test
    fun testLocalVisualAssetLibrary() {
        assertTrue("LocalVisualAssetLibrary fruits catalog must be non-empty", LocalVisualAssetLibrary.fruits.isNotEmpty())
        assertTrue("LocalVisualAssetLibrary shapes catalog must be non-empty", LocalVisualAssetLibrary.shapes.isNotEmpty())
        assertTrue("LocalVisualAssetLibrary colours catalog must be non-empty", LocalVisualAssetLibrary.colours.isNotEmpty())

        val obj = LocalVisualAssetLibrary.getRandomObject(100L)
        assertNotNull("Retrieved asset should not be null", obj)
        assertTrue("Asset icon emoji should be non-empty", obj.iconEmoji.isNotBlank())
    }
}
