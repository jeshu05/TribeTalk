package org.tribetalk.fln

import org.junit.Assert.*
import org.junit.Test
import org.tribetalk.fln.model.FlnGrade
import org.tribetalk.fln.model.SlmCurriculumRequest
import org.tribetalk.fln.model.WorksheetType
import org.tribetalk.fln.slm.SlmCurriculumEngine

class SlmCurriculumEngineTest {

    @Test
    fun testBuildSystemPromptIncludesNipunTargetsAndConstraints() {
        val request = SlmCurriculumRequest(
            topicPrompt = "हाट बाज़ार में फल",
            grade = FlnGrade.GRADE_1,
            worksheetType = WorksheetType.ADDITION_WORD_PROBLEM,
            questionCount = 5
        )

        val prompt = SlmCurriculumEngine.buildSystemPrompt(request)

        assertTrue("Prompt must contain grade", prompt.contains(request.grade.displayName))
        assertTrue("Prompt must contain worksheet type", prompt.contains(request.worksheetType.displayName))
        assertTrue("Prompt must contain NIPUN target code", prompt.contains(request.worksheetType.nipunTargetCode))
        assertTrue("Prompt must contain topic", prompt.contains("हाट बाज़ार में फल"))
        assertTrue("Prompt must mandate JSON output", prompt.contains("Return ONLY a strict JSON object"))
    }

    @Test
    fun testParseJsonPlanValidStructure() {
        val sampleJson = """
            {
              "theme": "हाट बाज़ार और फल",
              "nipunCode": "N-G1.2",
              "grade": "GRADE_1",
              "storyContextHindi": "मीरा और रोहन गाँव के हाट बाज़ार में गए।",
              "problemSpecs": [
                {
                  "conceptHindi": "सेब",
                  "quantity1": 3,
                  "quantity2": 2,
                  "operation": "ADD",
                  "distractorHindi": ["आम", "केला", "जामुन"]
                }
              ]
            }
        """.trimIndent()

        val plan = SlmCurriculumEngine.parseJsonPlan(sampleJson)

        assertNotNull("Parsed plan should not be null", plan)
        assertEquals("हाट बाज़ार और फल", plan!!.theme)
        assertEquals("N-G1.2", plan.nipunCode)
        assertEquals("GRADE_1", plan.grade)
        assertTrue(plan.storyContextHindi.contains("हाट बाज़ार"))
        assertTrue(plan.problemSpecs.isNotEmpty())
    }

    @Test
    fun testSymbolicReasoningFallbackWorksInstantly() {
        val request = SlmCurriculumRequest(
            topicPrompt = "गाँव के पालतू जानवर",
            grade = FlnGrade.GRADE_1,
            worksheetType = WorksheetType.COUNT_AND_MATCH,
            questionCount = 4
        )

        val plan = SlmCurriculumEngine.runSymbolicReasoning(request)

        assertNotNull(plan)
        assertEquals(4, plan.problemSpecs.size)
        assertEquals(request.worksheetType.nipunTargetCode, plan.nipunCode)
        for (spec in plan.problemSpecs) {
            assertTrue("Quantity 1 must be > 0", spec.quantity1 > 0)
            assertTrue("Quantity 2 must be > 0", spec.quantity2 > 0)
            assertFalse("Concept Hindi must not be blank", spec.conceptHindi.isBlank())
            assertEquals(3, spec.distractorHindi.size)
        }
    }

    @Test
    fun testSymbolicReasoningUnderstandsTopicThemes() {
        // Theme 1: Fruits
        val fruitReq = SlmCurriculumRequest(
            topicPrompt = "हाट बाज़ार में ताज़े फल",
            grade = FlnGrade.GRADE_1,
            worksheetType = WorksheetType.ADDITION_WORD_PROBLEM,
            questionCount = 3
        )
        val fruitPlan = SlmCurriculumEngine.runSymbolicReasoning(fruitReq)
        assertTrue(fruitPlan.storyContextHindi.contains("बाज़ार") || fruitPlan.storyContextHindi.contains("फल"))
        val fruitConcepts = fruitPlan.problemSpecs.map { it.conceptHindi }
        assertTrue(fruitConcepts.any { it.contains("सेब") || it.contains("आम") })

        // Theme 2: Forest
        val forestReq = SlmCurriculumRequest(
            topicPrompt = "जंगल के पेड़ और प्रकृति",
            grade = FlnGrade.GRADE_1,
            worksheetType = WorksheetType.PICTURE_WORD_MATCH,
            questionCount = 3
        )
        val forestPlan = SlmCurriculumEngine.runSymbolicReasoning(forestReq)
        val forestConcepts = forestPlan.problemSpecs.map { it.conceptHindi }
        assertTrue(forestConcepts.any { it.contains("पेड़") || it.contains("पक्षी") })
    }

    @Test
    fun testPlanBindingGeneratesValidWorksheetItems() {
        val request = SlmCurriculumRequest(
            topicPrompt = "नदी और मछलियाँ",
            grade = FlnGrade.GRADE_1,
            worksheetType = WorksheetType.ADDITION_WORD_PROBLEM,
            questionCount = 3
        )

        val (plan, items) = SlmCurriculumEngine.generateCurriculumPlan(request)

        assertEquals(3, items.size)
        for (item in items) {
            assertNotNull(item.mathAnswer)
            assertEquals("+", item.operationSign)
            assertEquals(item.quantity + item.secondaryQuantity, item.mathAnswer)
            assertFalse("Prompt Hindi must not be empty", item.promptHindi.isBlank())
            assertFalse("Teacher solution must not be empty", item.teacherSolutionNote.isBlank())
            assertFalse("Teacher phonetics must not be empty", item.teacherPhoneticAnswer.isBlank())
            assertEquals(plan.nipunCode, item.nipunCode)
        }
    }

    @Test
    fun testFlashcardDeckGenerationProducesValidCards() {
        val cards = SlmCurriculumEngine.generateFlashcardDeck("गाँव के पेड़ और फल")

        assertEquals(5, cards.size)
        for (card in cards) {
            assertTrue(card.isCustomUserGenerated)
            assertFalse("Hindi text should not be blank", card.hindiText.isBlank())
            assertFalse("Ol Chiki text should not be blank", card.santaliOlChiki.isBlank())
            assertFalse("Teacher phonetic guide should not be blank", card.teacherPhoneticGuide.isBlank())
        }
    }
}
