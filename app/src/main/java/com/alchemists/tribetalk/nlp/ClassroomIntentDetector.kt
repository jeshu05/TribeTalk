package com.alchemists.tribetalk.nlp

import com.alchemists.tribetalk.curriculum.models.FLNDomain
import com.alchemists.tribetalk.nlp.models.ClassroomIntent

/**
 * Lightweight Rule-Based FLN Domain & Intent Detector.
 * Matches classroom keywords to classify FLNDomain (NUMERACY / LITERACY) and Intent.
 */
object ClassroomIntentDetector {

    private val NUMERACY_KEYWORDS = setOf(
        "गिनती", "संख्या", "जोड़", "घटाना", "आकार", "गिनो", "बराबर", "संख्याएँ", "संख्याओं", "अंक", "गिनिए"
    )

    private val LITERACY_KEYWORDS = setOf(
        "पढ़ो", "कहानी", "अक्षर", "शब्द", "लिखो", "सुनो", "कविता", "वाक्य", "वर्ण", "वर्णमाला", "पढ़िए"
    )

    fun detectDomain(text: String): FLNDomain? {
        var numCount = 0
        var litCount = 0
        val words = text.split("\\s+".toRegex())

        for (w in words) {
            val clean = w.replace("[,\\.\\?।]+$".toRegex(), "")
            if (clean in NUMERACY_KEYWORDS) numCount++
            if (clean in LITERACY_KEYWORDS) litCount++
        }

        return when {
            numCount > litCount -> FLNDomain.NUMERACY
            litCount > numCount -> FLNDomain.LITERACY
            else -> null
        }
    }

    fun detectTopic(text: String): String? {
        return when {
            text.contains("गिनती") || text.contains("गिनिए") || text.contains("गिनो") -> "COUNTING"
            text.contains("जोड़") -> "ADDITION"
            text.contains("घटाना") || text.contains("घटाव") -> "SUBTRACTION"
            text.contains("आकार") || text.contains("गोल") || text.contains("चौकोर") -> "SHAPES"
            text.contains("अक्षर") || text.contains("वर्ण") -> "ALPHABET_RECOGNITION"
            text.contains("कहानी") || text.contains("कविता") -> "STORY_RHYME"
            else -> null
        }
    }

    fun detectIntent(text: String): ClassroomIntent {
        return when {
            text.contains("नमस्ते") || text.contains("जोहार") || text.contains("सुप्रभात") -> ClassroomIntent.GREETING
            text.contains("?") || text.contains("कितने") || text.contains("कितनी") || text.contains("क्या") -> ClassroomIntent.ASSESSMENT_QUESTION
            text.contains("सीखेंगे") || text.contains("खोलिए") || text.contains("पढ़िए") || text.contains("करिए") -> ClassroomIntent.TEACHING_INSTRUCTION
            text.contains("क्योंकि") || text.contains("अर्थात") || text.contains("मतलब") -> ClassroomIntent.EXPLANATION
            else -> ClassroomIntent.GENERAL_STATEMENT
        }
    }
}
