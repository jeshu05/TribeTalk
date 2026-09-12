package org.tribetalk.fln.model

/**
 * Grade levels aligned with the NIPUN Bharat Foundational Literacy and Numeracy framework.
 */
enum class FlnGrade(val displayName: String, val ageRange: String, val stageBadge: String) {
    BALVATIKA("Balvatika (Pre-Primary)", "Ages 5-6", "L-BAL / N-BAL"),
    GRADE_1("Class 1 (Grade 1)", "Ages 6-7", "L-G1 / N-G1"),
    GRADE_2("Class 2 (Grade 2)", "Ages 7-8", "L-G2 / N-G2")
}

/**
 * Difficulty tiers for adaptive NIPUN learning scaffolding.
 */
enum class WorksheetDifficulty(val displayName: String, val badgeColorHex: String) {
    EASY("Foundational (सरल)", "#10B981"),
    MEDIUM("Standard (मध्यम)", "#059669"),
    HARD("Challenge (चुनौती)", "#047857")
}

/**
 * Domains of learning under NIPUN Bharat Foundational Stage.
 */
enum class FlnDomain(val displayName: String, val domainCode: String) {
    LITERACY_AKSHAR("Akshar & Phonics (ᱚᱞ ᱪᱤᱠᱤ)", "FL-L1"),
    LITERACY_VOCAB("Living Vocabulary (ᱡᱤᱵᱽ ᱡᱤᱭᱟᱹᱞᱤ & ᱥᱤᱨᱡᱚᱱ)", "FL-L2"),
    LITERACY_READING("Reading with Comprehension (ᱯᱟᱲᱦᱟᱣ ᱟᱨ ᱵᱩᱡᱷᱟᱹᱣ)", "FL-L3"),
    NUMERACY_COUNTING("Number Sense & Counting (ᱞᱮᱠᱷᱟ)", "FN-N1"),
    NUMERACY_OPERATIONS("Addition & Operations (ᱡᱚᱲᱟᱣ ᱞᱮᱠᱷᱟ)", "FN-N2"),
    NUMERACY_SHAPES("Shapes & Spatial Concepts (ᱨᱩᱯ ᱟᱨ ᱡᱟᱭᱜᱟ)", "FN-N3"),
    NUMERACY_PRACTICAL("Market Math & Currency (ᱴᱟᱠᱟ ᱟᱨ ᱦᱟᱴ)", "FN-N4")
}

/**
 * An educational flashcard for bilingual Hindi <-> Santali instruction.
 * Includes the triple-representation: Hindi, authentic Ol Chiki script, and a
 * phonetic Devanagari pronunciation guide specifically designed for non-native teachers.
 */
data class FlnCard(
    val id: String,
    val domain: FlnDomain,
    val category: String,
    val nipunCode: String,
    val hindiText: String,
    val santaliOlChiki: String,
    val teacherPhoneticGuide: String,
    val englishGloss: String,
    val iconType: String,
    val numeralValue: Int? = null,
    val exampleSentenceHindi: String? = null,
    val exampleSentenceSantali: String? = null,
    val imageAssetPath: String? = "file:///android_asset/flashcards/${id}.webp",
    val isCustomUserGenerated: Boolean = false
)

/**
 * 8 High-Impact NIPUN Bharat Aligned Worksheet Types.
 */
enum class WorksheetType(
    val displayName: String,
    val santaliName: String,
    val description: String,
    val nipunTargetCode: String
) {
    COUNT_AND_MATCH(
        "Count & Match",
        "ᱞᱮᱠᱷᱟ ᱟᱨ ᱡᱚᱲᱟᱣ",
        "Count objects and connect to bilingual numerals.",
        "N-BAL.1 / N-G1.1"
    ),
    PICTURE_WORD_MATCH(
        "Word & Picture Match",
        "ᱪᱤᱛᱟᱹᱨ ᱟᱨ ᱟᱹᱲᱟᱹ",
        "Match pictures to bilingual vocabulary.",
        "L-BAL.1 / L-G1.1"
    ),
    AKSHAR_TRACING(
        "Letter Tracing & Phonics",
        "ᱪᱤᱠᱤ ᱪᱮᱫᱚᱜ",
        "Handwriting Ol Chiki with directional guides.",
        "L-BAL.1"
    ),
    ASSESSMENT_CIRCLE(
        "Circle Correct Answer",
        "ᱴᱷᱤᱠ ᱟᱹᱲᱟᱹ ᱨᱮ ᱜᱩᱞ",
        "Identify the correct translation among distractors.",
        "L-G1.1 / N-G1.1"
    ),
    ADDITION_WORD_PROBLEM(
        "Addition & Visual Math",
        "ᱡᱚᱲᱟᱣ ᱞᱮᱠᱷᱟ",
        "Bilingual visual addition stories and sums within 20.",
        "N-G1.1 / N-G2.1"
    ),
    NUMBER_SEQUENCE_TRAIN(
        "Number Train Sequence",
        "ᱞᱮᱠᱷᱟ ᱨᱮᱞᱜᱟᱹᱰᱤ",
        "Fill in missing Ol Chiki & Hindi numerals.",
        "N-G1.1 / N-G1.2"
    ),
    GREATER_LESSER_COMPARE(
        "Compare Groups ( > , < , = )",
        "ᱢᱟᱨᱟᱝ ᱟᱨ ᱦᱩᱰᱤᱧ",
        "Compare object quantities with visual balance.",
        "N-G1.2 / N-BAL.2"
    ),
    MISSING_AKSHAR_SPELLING(
        "Missing Akshar Spelling",
        "ᱪᱤᱠᱤ ᱯᱮᱨᱮᱡ",
        "Complete the word by filling the missing letter.",
        "L-G1.1 / L-G2.1"
    )
}

/**
 * Configuration options for generating a worksheet.
 */
data class WorksheetConfig(
    val title: String = "NIPUN Bharat Bilingual Worksheet",
    val type: WorksheetType = WorksheetType.COUNT_AND_MATCH,
    val grade: FlnGrade = FlnGrade.GRADE_1,
    val difficulty: WorksheetDifficulty = WorksheetDifficulty.MEDIUM,
    val questionCount: Int = 5,
    val seed: Long = System.currentTimeMillis(),
    val schoolName: String = "Prathmik Vidyalaya (प्राथमिक विद्यालय)",
    val studentName: String = "",
    val rollNumber: String = "",
    val includeTeacherKey: Boolean = true
)

/**
 * An individual problem item within a generated worksheet.
 */
data class WorksheetItem(
    val id: String,
    val prompt: String,
    val promptHindi: String = "",
    val promptSantali: String = "",
    val iconType: String = "star",
    val quantity: Int = 1,
    val secondaryQuantity: Int = 0,
    val operationSign: String = "",
    val leftLabelHindi: String = "",
    val rightLabelSantali: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val mathAnswer: Int? = null,
    val missingLetterAnswer: String? = null,
    val wordWithBlank: String? = null,
    val sequenceItems: List<String> = emptyList(),
    val missingSequenceIndex: Int = -1,
    val teacherSolutionNote: String = "",
    val teacherPhoneticAnswer: String = "",
    val nipunCode: String = ""
)

/**
 * Teacher scoring rubric level under NIPUN Bharat guidelines.
 */
enum class NipunRubricLevel(val displayName: String, val criteria: String) {
    EMERGING("Emerging (प्रारंभिक)", "Needs direct guided assistance with phonics/counting."),
    DEVELOPING("Developing (प्रगतिशील)", "Recognizes concepts with occasional teacher prompting."),
    PROFICIENT("Proficient (दक्ष)", "Independently solves and verbalizes in mother-tongue.")
}

/**
 * Neuro-Symbolic SLM Contract: Structured JSON spec exchanged with on-device SLM.
 */
data class SlmCurriculumRequest(
    val topicPrompt: String,
    val grade: FlnGrade,
    val worksheetType: WorksheetType,
    val questionCount: Int = 5
)

data class SlmCurriculumPlan(
    val theme: String,
    val nipunCode: String,
    val grade: String,
    val storyContextHindi: String,
    val problemSpecs: List<SlmProblemSpec>
)

data class SlmProblemSpec(
    val conceptHindi: String,
    val quantity1: Int,
    val quantity2: Int = 0,
    val operation: String = "NONE",
    val distractorHindi: List<String> = emptyList()
)
