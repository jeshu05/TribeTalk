package org.tribetalk.curriculum

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.tribetalk.curriculum.ontology.CurriculumRegistry
import org.tribetalk.curriculum.spec.*
import org.tribetalk.fln.model.WorksheetDifficulty

class ActivitySpecTest {

    private fun createSampleActivitySpec(): ActivitySpec {
        val items = listOf(
            ActivityItemSpec(
                id = "item_1",
                visualAsset = "mango",
                quantity = 4,
                options = listOf("2", "4", "5"),
                correctAnswer = "4",
                explanationHindi = "यहाँ 4 आम हैं।",
                explanationSantali = "ᱱᱚᱸᱰᱮ ᱔ ᱩᱞ ᱢᱮᱱᱟᱜᱼᱟ ᱾"
            ),
            ActivityItemSpec(
                id = "item_2",
                visualAsset = "flower",
                quantity = 7,
                options = listOf("6", "7", "8"),
                correctAnswer = "7",
                explanationHindi = "यहाँ 7 फूल हैं।",
                explanationSantali = "ᱱᱚᱸᱰᱮ ᱗ ᱵᱟᱦᱟ ᱢᱮᱱᱟᱜᱼᱟ ᱾"
            )
        )

        val visualSpec = VisualSpec(
            theme = VisualTheme.GARDEN,
            layout = VisualLayout.GRID,
            primaryAssetKey = "mango",
            supportsMonochrome = true
        )

        val languageSpec = LanguageSpec(
            primaryWord = "आम",
            targetWord = "ᱩᱞ",
            englishWord = "Mango",
            instructionHindi = "वस्तुओं को गिनें और सही संख्या चुनें।",
            instructionSantali = "ᱡᱤᱱᱤᱥ ᱠᱚ ᱞᱮᱠᱷᱟᱭ ᱯᱮ ᱟᱨ ᱥᱟᱹᱨᱤ ᱮᱞ ᱵᱟᱪᱷᱟᱣ ᱯᱮ ᱾",
            phoneticGuide = "Jinis ko lekhay pe ar sari el bachhaw pe."
        )

        val answerSpec = AnswerSpec(
            correctValue = "4",
            numericValue = 4
        )

        val metadata = ActivityMetadata(
            worksheetId = "ws_test_1",
            activityId = "act_test_1",
            generationSeed = 12345L,
            modelVersion = "Qwen-0.5B-v2.5"
        )

        return ActivitySpec(
            id = "act_test_1",
            objectiveId = "NIPUN_G1_NUM_COUNT_1_10",
            activityType = ActivityType.COUNT,
            difficulty = WorksheetDifficulty.EASY,
            items = items,
            visualSpec = visualSpec,
            languageSpec = languageSpec,
            answerSpec = answerSpec,
            metadata = metadata
        )
    }

    @Test
    fun testActivitySpecJsonSerializationRoundTrip() {
        val original = createSampleActivitySpec()
        val json = original.toJson()
        assertNotNull(json)
        assertEquals("act_test_1", json.getString("id"))
        assertEquals("NIPUN_G1_NUM_COUNT_1_10", json.getString("objectiveId"))
        assertEquals("COUNT", json.getString("activityType"))
        assertEquals("EASY", json.getString("difficulty"))

        val restored = ActivitySpec.fromJson(json)
        assertEquals(original.id, restored.id)
        assertEquals(original.objectiveId, restored.objectiveId)
        assertEquals(original.activityType, restored.activityType)
        assertEquals(original.difficulty, restored.difficulty)
        assertEquals(original.items.size, restored.items.size)
        assertEquals(original.items[0].visualAsset, restored.items[0].visualAsset)
        assertEquals(original.items[0].quantity, restored.items[0].quantity)
        assertEquals(original.items[0].correctAnswer, restored.items[0].correctAnswer)
        assertEquals(original.visualSpec.theme, restored.visualSpec.theme)
        assertEquals(original.languageSpec.instructionHindi, restored.languageSpec.instructionHindi)
        assertEquals(original.answerSpec.correctValue, restored.answerSpec.correctValue)
        assertEquals(original.metadata.generationSeed, restored.metadata.generationSeed)
    }

    @Test
    fun testConversionToActivityIR() {
        val spec = createSampleActivitySpec()
        val ir = ActivitySpec.toActivityIR(spec)
        assertNotNull(ir)
        assertEquals(spec.id, ir.id)
        assertEquals(spec.activityType.actionType, ir.actionType)
        assertEquals(spec.items[0].quantity, ir.quantity)
        assertEquals(spec.items[0].correctAnswer, ir.correctValue)
        assertEquals(spec.languageSpec.instructionHindi, ir.instructionHindi)
        assertEquals(spec.languageSpec.instructionSantali, ir.instructionSantali)
    }

    @Test
    fun testConversionToWorksheetItem() {
        val spec = createSampleActivitySpec()
        val wsItem = ActivitySpec.toWorksheetItem(spec)
        assertNotNull(wsItem)
        assertEquals(spec.id, wsItem.id)
        assertEquals(spec.items[0].quantity, wsItem.quantity)
        assertEquals(spec.answerSpec.correctValue, wsItem.leftLabelHindi)
        assertTrue(wsItem.options.contains(spec.answerSpec.correctValue))
    }

    @Test
    fun testConversionToFlnCard() {
        val spec = createSampleActivitySpec()
        val card = ActivitySpec.toFlnCard(spec)
        assertNotNull(card)
        assertEquals(spec.id, card.id)
        assertEquals(spec.languageSpec.targetWord, card.santaliOlChiki)
        assertEquals(spec.items[0].visualAsset, card.vectorIconType)
    }

    @Test
    fun testFromPlannerOutputSynthesizesValidSpec() {
        val defaultObj = CurriculumRegistry.getDefaultObjective()
        val qwenResponseJson = JSONObject().apply {
            put("activityType", "COUNT")
            put("targetQuantity", 5)
            put("primaryObject", "tree")
            put("instructionEn", "Count the trees in the picture.")
            put("instructionHi", "चित्र में पेड़ों को गिनें।")
            put("distractors", org.json.JSONArray(listOf("3", "4", "6")))
        }

        val synthesized = ActivitySpec.fromPlannerOutput(qwenResponseJson, defaultObj, 999L)
        assertNotNull(synthesized)
        assertEquals(ActivityType.COUNT, synthesized.activityType)
        assertEquals(defaultObj.id, synthesized.objectiveId)
        assertEquals(1, synthesized.items.size)
        assertEquals(5, synthesized.items[0].quantity)
        assertEquals("tree", synthesized.items[0].visualAsset)
        assertEquals("5", synthesized.items[0].correctAnswer)
        assertTrue(synthesized.items[0].options.contains("5"))
        assertTrue(synthesized.items[0].options.contains("3"))
    }
}
