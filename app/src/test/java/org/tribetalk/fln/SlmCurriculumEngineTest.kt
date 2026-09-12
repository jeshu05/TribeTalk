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
            assertFalse("Icon type should not be blank", card.iconType.isBlank())
        }
    }

    @Test
    fun testPlanBindingSupportsMultipleWorksheetFormats() {
        // 1. Number Sequence Train
        val trainReq = SlmCurriculumRequest("संख्या रेलगाड़ी", FlnGrade.GRADE_1, WorksheetType.NUMBER_SEQUENCE_TRAIN, 3)
        val (_, trainItems) = SlmCurriculumEngine.generateCurriculumPlan(trainReq)
        assertEquals(3, trainItems.size)
        for (item in trainItems) {
            assertEquals("train", item.iconType)
            assertEquals(5, item.sequenceItems.size)
            assertTrue("Sequence must contain missing blank", item.sequenceItems.contains("__"))
            assertNotNull("Math answer must be set", item.mathAnswer)
        }

        // 2. Count & Match
        val countReq = SlmCurriculumRequest("हाट के फल", FlnGrade.BALVATIKA, WorksheetType.COUNT_AND_MATCH, 4)
        val (_, countItems) = SlmCurriculumEngine.generateCurriculumPlan(countReq)
        assertEquals(4, countItems.size)
        for (item in countItems) {
            assertTrue("Quantity must be > 0", item.quantity > 0)
            assertTrue(item.rightLabelSantali.contains("[") && item.rightLabelSantali.contains("]"))
        }

        // 3. Greater Lesser Compare
        val cmpReq = SlmCurriculumRequest("गाँव के पशु", FlnGrade.GRADE_1, WorksheetType.GREATER_LESSER_COMPARE, 3)
        val (_, cmpItems) = SlmCurriculumEngine.generateCurriculumPlan(cmpReq)
        assertEquals(3, cmpItems.size)
        for (item in cmpItems) {
            assertTrue(listOf(">", "<", "=").contains(item.operationSign))
        }

        // 4. Missing Akshar Spelling
        val spellReq = SlmCurriculumRequest("जंगल और प्रकृति", FlnGrade.GRADE_1, WorksheetType.MISSING_AKSHAR_SPELLING, 3)
        val (_, spellItems) = SlmCurriculumEngine.generateCurriculumPlan(spellReq)
        assertEquals(3, spellItems.size)
        for (item in spellItems) {
            assertNotNull(item.wordWithBlank)
            assertTrue("Must contain blank slot", item.wordWithBlank!!.contains("[ _ ]"))
            assertNotNull(item.missingLetterAnswer)
            assertFalse("Missing letter answer must not be empty", item.missingLetterAnswer!!.isBlank())
            assertEquals(4, item.options.size)
            assertTrue("Options must contain missing letter", item.options.contains(item.missingLetterAnswer))
        }

        // 5. Assessment Circle MCQ
        val mcqReq = SlmCurriculumRequest("पाठशाला", FlnGrade.GRADE_2, WorksheetType.ASSESSMENT_CIRCLE, 3)
        val (_, mcqItems) = SlmCurriculumEngine.generateCurriculumPlan(mcqReq)
        assertEquals(3, mcqItems.size)
        for (item in mcqItems) {
            assertEquals(4, item.options.size)
            assertTrue("Correct index must be in bounds", item.correctIndex in 0..3)
        }
    }

    @Test
    fun testMultiDomainReasoningExpandsBeyondBasics() {
        val birdReq = SlmCurriculumRequest("आकाश के सुंदर पंछी", FlnGrade.GRADE_1, WorksheetType.PICTURE_WORD_MATCH, 3)
        val birdPlan = SlmCurriculumEngine.runSymbolicReasoning(birdReq)
        assertTrue(birdPlan.theme.contains("पंछी") || birdPlan.theme.contains("Birds"))

        val vegReq = SlmCurriculumRequest("खेत की ताज़ी सब्जियाँ", FlnGrade.GRADE_1, WorksheetType.PICTURE_WORD_MATCH, 3)
        val vegPlan = SlmCurriculumEngine.runSymbolicReasoning(vegReq)
        assertTrue(vegPlan.theme.contains("सब्जियाँ") || vegPlan.theme.contains("Vegetables"))

        val colorReq = SlmCurriculumRequest("रंग-बिरंगे फूल", FlnGrade.GRADE_1, WorksheetType.PICTURE_WORD_MATCH, 3)
        val colorPlan = SlmCurriculumEngine.runSymbolicReasoning(colorReq)
        assertTrue(colorPlan.theme.contains("रंग") || colorPlan.theme.contains("Colors"))
    }
}
