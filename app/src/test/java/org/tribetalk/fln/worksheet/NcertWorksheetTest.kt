package org.tribetalk.fln.worksheet

import org.junit.Assert.*
import org.junit.Test
import org.tribetalk.curriculum.ontology.CurriculumRegistry
import org.tribetalk.curriculum.spec.ActivityItemSpec
import org.tribetalk.curriculum.spec.ActivitySpec
import org.tribetalk.curriculum.spec.ActivityType
import org.tribetalk.curriculum.spec.VisualSpec
import org.tribetalk.fln.model.FlnGrade
import org.tribetalk.fln.model.WorksheetConfig
import org.tribetalk.fln.model.WorksheetType

class NcertWorksheetTest {

    @Test
    fun testNcertBilingualInstructionsPresence() {
        for (type in WorksheetType.entries) {
            val (hiInstr, satInstr) = WorksheetPdfExporter.getBilingualInstruction(type)
            assertNotNull("Hindi instruction must not be null for $type", hiInstr)
            assertNotNull("Santhali instruction must not be null for $type", satInstr)
            assertTrue("Hindi instruction must contain निर्देश", hiInstr.contains("निर्देश"))
            assertTrue("Santhali instruction must contain ᱫᱤᱥᱟᱹ-ᱩᱫᱩᱜ", satInstr.contains("ᱫᱤᱥᱟᱹ-ᱩᱫᱩᱜ"))
        }
    }

    @Test
    fun testNcertBilingualLearningOutcomesPresence() {
        for (type in WorksheetType.entries) {
            val (hiOutcome, satOutcome) = WorksheetPdfExporter.getBilingualLearningOutcome(type, FlnGrade.GRADE_1)
            assertNotNull("Hindi outcome must not be null for $type", hiOutcome)
            assertNotNull("Santhali outcome must not be null for $type", satOutcome)
            assertTrue("Hindi outcome must not be blank for $type", hiOutcome.isNotBlank())
            assertTrue("Santhali outcome must not be blank for $type", satOutcome.isNotBlank())
        }
    }

    @Test
    fun testWorksheetGenerationFromMultiItemActivitySpec() {
        val objective = CurriculumRegistry.getDefaultObjective()
        val items = listOf(
            ActivityItemSpec(id = "item_1", visualAsset = "apple", quantity = 3, correctAnswer = "3", options = listOf("2", "3", "4")),
            ActivityItemSpec(id = "item_2", visualAsset = "mango", quantity = 5, correctAnswer = "5", options = listOf("4", "5", "6")),
            ActivityItemSpec(id = "item_3", visualAsset = "fish", quantity = 2, correctAnswer = "2", options = listOf("1", "2", "3"))
        )

        val spec = ActivitySpec(
            id = "spec_test_multi",
            objectiveId = objective.id,
            activityType = ActivityType.COUNT_AND_MATCH,
            items = items,
            visualSpec = VisualSpec(primaryAssetKey = "apple"),
            languageSpec = org.tribetalk.curriculum.spec.LanguageSpec(),
            answerSpec = org.tribetalk.curriculum.spec.AnswerSpec(correctValue = "3"),
            metadata = org.tribetalk.curriculum.spec.ActivityMetadata("ws_1", "act_1", 101L)
        )

        val config = WorksheetConfig(
            type = WorksheetType.COUNT_AND_MATCH,
            grade = FlnGrade.GRADE_1,
            seed = 101L
        )

        val wsItems = WorksheetGenerator.generateFromActivitySpec(spec, config)
        assertEquals("Multi-item spec should preserve all 3 items", 3, wsItems.size)
        assertEquals("apple", wsItems[0].iconType)
        assertEquals("mango", wsItems[1].iconType)
        assertEquals("fish", wsItems[2].iconType)
        assertEquals(3, wsItems[0].quantity)
        assertEquals(5, wsItems[1].quantity)
        assertEquals(2, wsItems[2].quantity)
    }

    @Test
    fun testProceduralWorksheetGeneratesValidItemsForAllTypes() {
        for (type in WorksheetType.entries) {
            val config = WorksheetConfig(
                type = type,
                grade = FlnGrade.GRADE_1,
                questionCount = 4,
                seed = 42L
            )
            val items = WorksheetGenerator.generateWorksheet(config)
            assertTrue("Worksheet $type must generate non-empty item list", items.isNotEmpty())
            for (item in items) {
                assertTrue("Prompt Hindi must not be blank for $type", item.promptHindi.isNotBlank())
                assertTrue("Prompt Santali must not be blank for $type", item.promptSantali.isNotBlank())
                assertNotNull("Teacher solution note must not be null for $type", item.teacherSolutionNote)
            }
        }
    }
}
