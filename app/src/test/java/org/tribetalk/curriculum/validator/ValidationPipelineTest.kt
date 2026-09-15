package org.tribetalk.curriculum.validator

import org.junit.Assert.*
import org.junit.Test
import org.tribetalk.curriculum.ontology.CurriculumRegistry
import org.tribetalk.curriculum.spec.*
import org.tribetalk.fln.model.WorksheetDifficulty

class ValidationPipelineTest {

    private val objective = CurriculumRegistry.getDefaultObjective() // NIPUN_G1_NUM_COUNT_1_10

    private fun createValidSpec(): ActivitySpec {
        val item = ActivityItemSpec(
            id = "item_1",
            visualAsset = "mango",
            quantity = 5,
            options = listOf("4", "5", "6"),
            correctAnswer = "5",
            explanationHindi = "यहाँ 5 आम हैं।",
            explanationSantali = "ᱱᱚᱸᱰᱮ ᱕ ᱩᱞ ᱢᱮᱱᱟᱜᱼᱟ ᱾"
        )
        return ActivitySpec(
            id = "act_valid_1",
            objectiveId = objective.id,
            activityType = ActivityType.COUNT,
            difficulty = WorksheetDifficulty.EASY,
            items = listOf(item),
            visualSpec = VisualSpec(primaryAssetKey = "mango"),
            languageSpec = LanguageSpec(
                primaryWord = "आम",
                targetWord = "ᱩᱞ",
                englishWord = "Mango",
                instructionHindi = "आम गिनें और सही संख्या चुनें:",
                instructionSantali = "ᱩᱞ ᱞᱮᱠᱷᱟᱭ ᱢᱮ ᱟᱨ ᱥᱟᱹᱦᱤ ᱮᱞ ᱵᱟᱪᱷᱟᱣ ᱢᱮ:"
            ),
            answerSpec = AnswerSpec(correctValue = "5"),
            metadata = ActivityMetadata("ws_1", "act_1", 100L)
        )
    }

    @Test
    fun testValidSpecPassesAllValidatorsWithoutIssues() {
        val spec = createValidSpec()
        val result = ActivityValidationPipeline.validate(spec, objective)
        assertTrue("Valid spec should pass validation", result.isValid)
        assertTrue("No issues should be reported for valid spec", result.allIssues.isEmpty())
        assertFalse("Spec should not have been repaired", result.wasRepaired)
    }

    @Test
    fun testMathematicalValidatorCatchesAndRepairsCountMismatch() {
        val spec = createValidSpec()
        // Intentional math mismatch: 5 mangoes, but correctAnswer says "2"
        val buggyItem = spec.items[0].copy(quantity = 5, correctAnswer = "2")
        val buggySpec = spec.copy(items = listOf(buggyItem))

        val mathResult = MathematicalValidator.validate(buggySpec)
        assertFalse("Buggy math should not be valid", mathResult.isValid)
        assertTrue("Issues must note the math repair", mathResult.issues.any { it.contains("Math error") })
        assertEquals("Correct answer must be repaired to 5", "5", mathResult.repairedSpec.items[0].correctAnswer)
        assertTrue("Options must contain the repaired answer", mathResult.repairedSpec.items[0].options.contains("5"))
    }

    @Test
    fun testCurriculumValidatorClampsQuantityExceedingRange() {
        val spec = createValidSpec()
        // Objective range is 1..10. Put quantity = 18.
        val outOfRangeItem = spec.items[0].copy(quantity = 18, correctAnswer = "18", options = listOf("16", "18", "20"))
        val outOfRangeSpec = spec.copy(items = listOf(outOfRangeItem))

        val currResult = CurriculumValidator.validate(outOfRangeSpec, objective)
        assertFalse("Out of range quantity should be flagged", currResult.isValid)
        assertTrue("Issues must mention clamping", currResult.issues.any { it.contains("Clamped to 10") })
        assertEquals("Quantity must be clamped to max of range (10)", 10, currResult.repairedSpec.items[0].quantity)
    }

    @Test
    fun testCurriculumValidatorReplacesDisallowedActivityType() {
        val spec = createValidSpec()
        // Disallowed activity type for Numeracy: IDENTIFY_LETTER
        val wrongTypeSpec = spec.copy(activityType = ActivityType.IDENTIFY_LETTER)

        val currResult = CurriculumValidator.validate(wrongTypeSpec, objective)
        assertFalse(currResult.isValid)
        assertTrue(currResult.issues.any { it.contains("ActivityType") })
        assertTrue("Must be repaired to an allowed activity type", objective.allowedActivityTypes.contains(currResult.repairedSpec.activityType))
    }

    @Test
    fun testAssetValidatorReplacesUnknownAsset() {
        val spec = createValidSpec()
        val badAssetItem = spec.items[0].copy(visualAsset = "non_existent_spaceship_xyz")
        val badAssetSpec = spec.copy(
            items = listOf(badAssetItem),
            visualSpec = spec.visualSpec.copy(primaryAssetKey = "non_existent_spaceship_xyz")
        )

        val assetResult = AssetValidator.validate(badAssetSpec)
        assertFalse("Unknown asset must be flagged", assetResult.isValid)
        assertTrue(assetResult.issues.any { it.contains("not found in SVG corpus") })
        assertNotEquals("non_existent_spaceship_xyz", assetResult.repairedSpec.items[0].visualAsset)
    }

    @Test
    fun testLanguageValidatorSynthesizesBlankInstructions() {
        val spec = createValidSpec().copy(
            languageSpec = LanguageSpec(
                primaryWord = "",
                targetWord = "",
                instructionHindi = "",
                instructionSantali = ""
            )
        )

        val langResult = LanguageValidator.validate(spec)
        assertFalse(langResult.isValid)
        assertTrue(langResult.issues.any { it.contains("Hindi instruction was blank") })
        assertTrue(langResult.issues.any { it.contains("Santali instruction was blank") })
        assertFalse(langResult.repairedSpec.languageSpec.instructionHindi.isBlank())
        assertFalse(langResult.repairedSpec.languageSpec.instructionSantali.isBlank())
    }

    @Test
    fun testFullPipelineEndToEndRepair() {
        // Spec with multiple compound issues: wrong objective ID, out of bounds count, bad math, unknown asset, blank instructions
        val compoundBuggySpec = ActivitySpec(
            id = "",
            objectiveId = "WRONG_ID",
            activityType = ActivityType.IDENTIFY_LETTER,
            difficulty = WorksheetDifficulty.EASY,
            items = listOf(
                ActivityItemSpec(
                    id = "item_buggy",
                    visualAsset = "completely_unknown_asset",
                    quantity = 25, // exceeds 1..10
                    options = listOf("1", "2"), // doesn't contain answer
                    correctAnswer = "99" // wrong math
                )
            ),
            visualSpec = VisualSpec(primaryAssetKey = "completely_unknown_asset"),
            languageSpec = LanguageSpec(instructionHindi = "", instructionSantali = ""),
            answerSpec = AnswerSpec("99"),
            metadata = ActivityMetadata("ws_1", "act_1", 1L)
        )

        val pipelineResult = ActivityValidationPipeline.validate(compoundBuggySpec, objective)
        assertFalse("Initial spec was invalid", pipelineResult.isValid)
        assertTrue("Pipeline must have repaired the spec", pipelineResult.wasRepaired)
        assertTrue("Issues should be catalogued", pipelineResult.allIssues.isNotEmpty())

        val repaired = pipelineResult.spec
        assertEquals(objective.id, repaired.objectiveId)
        assertTrue(objective.allowedActivityTypes.contains(repaired.activityType))
        assertTrue(repaired.items[0].quantity in 1..10)
        assertEquals(repaired.items[0].quantity.toString(), repaired.items[0].correctAnswer)
        assertTrue(repaired.items[0].options.contains(repaired.items[0].correctAnswer))
        assertFalse(repaired.languageSpec.instructionHindi.isBlank())
        assertFalse(repaired.languageSpec.instructionSantali.isBlank())
    }
}
