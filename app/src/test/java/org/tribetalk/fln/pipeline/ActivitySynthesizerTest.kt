package org.tribetalk.fln.pipeline

import org.junit.Assert.*
import org.junit.Test
import org.tribetalk.fln.model.FlnGrade

class ActivitySynthesizerTest {

    @Test
    fun testParseValidJsonPlan() {
        val validJson = """
            {
              "id": "act_test_01",
              "nipunCompetencyCode": "N-BAL.1",
              "actionType": "COUNT_AND_SELECT",
              "grade": "BALVATIKA",
              "difficulty": "EASY",
              "linguisticTier": "CORE_VOCABULARY",
              "primaryObjectKey": "mango",
              "quantity": 5,
              "secondaryQuantity": 0,
              "correctValue": "5",
              "distractorOptions": ["3", "5", "6"],
              "santaliWord": "ᱩᱞ",
              "hindiWord": "आम",
              "englishWord": "Mango",
              "bilingualPhraseSantali": "ᱦᱮᱲᱮᱢ ᱩᱞ",
              "bilingualPhraseHindi": "मीठा आम",
              "phraseEnglishGloss": "Sweet Mango",
              "instructionSantali": "ᱩᱞ ᱞᱮᱠᱷᱟᱭ ᱢᱮ:",
              "instructionHindi": "आम गिनें और संख्या चुनें:",
              "teacherSolutionNote": "5 Mangoes"
            }
        """.trimIndent()

        val result = ActivitySynthesizer.parsePlanFromJson(validJson, FlnGrade.BALVATIKA)
        assertTrue("Valid JSON should produce a valid result", result.isValid)
        val ir = result.repairedIR
        assertEquals("act_test_01", ir.id)
        assertEquals(ActivityActionType.COUNT_AND_SELECT, ir.actionType)
        assertEquals("mango", ir.primaryObjectKey)
        assertEquals(5, ir.quantity)
        assertEquals("5", ir.correctValue)
        assertTrue(ir.distractorOptions.contains("5"))
    }

    @Test
    fun testParseMalformedJsonAutoRepairs() {
        // Quantity exceeds Balvatika limit (18 > 10) and correctValue is not in options
        val malformedJson = """
            {
              "id": "act_bad",
              "actionType": "COUNT_AND_SELECT",
              "grade": "BALVATIKA",
              "primaryObjectKey": "mango",
              "quantity": 18,
              "correctValue": "18",
              "distractorOptions": ["1", "2", "3"]
            }
        """.trimIndent()

        val result = ActivitySynthesizer.parsePlanFromJson(malformedJson, FlnGrade.BALVATIKA)
        // ActivityValidator auto-repairs quantity and distractors
        val repaired = result.repairedIR
        assertEquals("Quantity should be clamped to 10 for Balvatika", 10, repaired.quantity)
        assertEquals("Correct value should be clamped", "10", repaired.correctValue)
        assertTrue("Options must contain the repaired correct answer", repaired.distractorOptions.contains("10"))
    }

    @Test
    fun testDeterministicSynthesis() {
        // Counting Mangoes
        val irCount = ActivitySynthesizer.synthesizeFromTopic("आम गिनना", FlnGrade.GRADE_1)
        assertEquals("mango", irCount.primaryObjectKey)
        assertEquals(ActivityActionType.COUNT_AND_SELECT, irCount.actionType)
        assertEquals(7, irCount.quantity)
        assertTrue(irCount.distractorOptions.contains(irCount.correctValue))

        // Addition Clay Pots
        val irAdd = ActivitySynthesizer.synthesizeFromTopic("घड़े जोड़ना", FlnGrade.GRADE_1)
        assertEquals("clay_pot", irAdd.primaryObjectKey)
        assertEquals(ActivityActionType.ADDITION_CONCRETE, irAdd.actionType)
        assertEquals("10", irAdd.correctValue) // 7 + 3
        assertTrue(irAdd.distractorOptions.contains("10"))

        // Sal Leaf Tracing
        val irTrace = ActivitySynthesizer.synthesizeFromTopic("अक्षर लिखना साल का पत्ता", FlnGrade.BALVATIKA)
        assertEquals("sal_leaf", irTrace.primaryObjectKey)
        assertEquals(ActivityActionType.AKSHAR_PHONICS_TRACE, irTrace.actionType)
        assertEquals(FlnGrade.BALVATIKA, irTrace.grade)
    }
}
