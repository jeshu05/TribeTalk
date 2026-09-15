package org.tribetalk.curriculum.ai

import org.junit.Assert.*
import org.junit.Test

class JsonRepairEngineTest {

    @Test
    fun testDirectCleanJsonParsing() {
        val raw = """{"activityType": "COUNT", "quantity": 7, "visualAsset": "apple", "correctAnswer": "7", "distractorOptions": ["5", "6", "7", "8"]}"""
        val parsed = JsonRepairEngine.repairAndParse(raw)

        assertNotNull("Should parse clean JSON", parsed)
        assertEquals("COUNT", parsed?.optString("activityType"))
        assertEquals(7, parsed?.optInt("quantity"))
        assertEquals("apple", parsed?.optString("visualAsset"))
        assertEquals("7", parsed?.optString("correctAnswer"))
    }

    @Test
    fun testMarkdownCodeFencesExtraction() {
        val raw = """
            Here is the planned activity:
            ```json
            {
                "activityType": "COUNT_AND_MATCH",
                "quantity": 5,
                "visualAsset": "mango",
                "correctAnswer": "5",
                "distractorOptions": ["3", "4", "5", "6"]
            }
            ```
            Hope this helps!
        """.trimIndent()

        val parsed = JsonRepairEngine.repairAndParse(raw)
        assertNotNull("Should extract and parse JSON from markdown code fence", parsed)
        assertEquals("COUNT_AND_MATCH", parsed?.optString("activityType"))
        assertEquals(5, parsed?.optInt("quantity"))
        assertEquals("mango", parsed?.optString("visualAsset"))
    }

    @Test
    fun testTrailingCommasRemoval() {
        val raw = """{"activityType": "COUNT", "quantity": 4, "visualAsset": "fish", "distractorOptions": ["2", "3", "4",],}"""
        val parsed = JsonRepairEngine.repairAndParse(raw)

        assertNotNull("Should sanitize trailing commas", parsed)
        assertEquals("COUNT", parsed?.optString("activityType"))
        assertEquals(4, parsed?.optInt("quantity"))
        assertEquals("fish", parsed?.optString("visualAsset"))
        val opts = parsed?.optJSONArray("distractorOptions")
        assertEquals(3, opts?.length())
    }

    @Test
    fun testTruncatedJsonRecovery() {
        // Model output cut off mid-array before closing brackets
        val raw = """{"activityType": "COUNT", "quantity": 6, "visualAsset": "tree", "distractorOptions": ["4", "5", "6""""
        val parsed = JsonRepairEngine.repairAndParse(raw)

        assertNotNull("Should recover from premature token truncation", parsed)
        assertEquals("COUNT", parsed?.optString("activityType"))
        assertEquals(6, parsed?.optInt("quantity"))
        assertEquals("tree", parsed?.optString("visualAsset"))
    }

    @Test
    fun testKeyAliasNormalization() {
        val raw = """
            {
                "type": "COUNT",
                "target_quantity": 8,
                "primary_asset": "flower",
                "correct_answer": "8",
                "choices": ["6", "7", "8", "9"]
            }
        """.trimIndent()

        val parsed = JsonRepairEngine.repairAndParse(raw)
        assertNotNull("Should normalize aliases", parsed)
        assertEquals("COUNT", parsed?.optString("activityType"))
        assertEquals(8, parsed?.optInt("quantity"))
        assertEquals("flower", parsed?.optString("visualAsset"))
        assertEquals("8", parsed?.optString("correctAnswer"))
        val opts = parsed?.optJSONArray("distractorOptions")
        assertNotNull(opts)
        assertEquals(4, opts?.length())
    }

    @Test
    fun testHeuristicExtractionFallback() {
        val raw = """The activity is activityType: COUNT with quantity: 9 and visualAsset: peacock and answer: 9"""
        val parsed = JsonRepairEngine.repairAndParse(raw)

        assertNotNull("Should perform heuristic key extraction on conversational text", parsed)
        assertEquals("COUNT", parsed?.optString("activityType"))
        assertEquals(9, parsed?.optInt("quantity"))
        assertEquals("peacock", parsed?.optString("visualAsset"))
    }
}
