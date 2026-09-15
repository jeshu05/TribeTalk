package org.tribetalk.fln.model

/**
 * Pedagogical domain under NIPUN Bharat guidelines.
 * Completely free of emoji strings.
 */
enum class FlnDomain(
    val displayName: String,
    val santaliName: String,
    val hindiName: String,
    val iconKey: String
) {
    LITERACY_ALPHABET("Alphabet & Phonics", "ᱪᱤᱠᱤ ᱟᱲᱟᱝ", "वर्ण एवं ध्वनि", "alphabet"),
    LITERACY_VOCABULARY("Living Vocabulary", "ᱫᱤᱱᱟᱹᱢ ᱟᱹᱲᱟᱹ", "दैनिक शब्दावली", "vocabulary"),
    NUMERACY_COUNTING("Numbers & Counting", "ᱞᱮᱠᱷᱟ ᱟᱨ ᱡᱚᱲ", "संख्या एवं गणना", "numbers"),
    NUMERACY_SPATIAL("Shapes & Space", "ᱨᱩᱯ ᱟᱨ ᱴᱷᱟᱶ", "आकृतियाँ एवं स्थानिक समझ", "spatial")
}

/**
 * Content categories for high-engagement tribal curriculum exploration.
 * Completely free of emoji strings.
 */
enum class FlnCategory(
    val displayName: String,
    val santaliName: String,
    val hindiName: String,
    val iconKey: String,
    val domain: FlnDomain
) {
    ALL("All Topics", "ᱡᱚᱛᱚ", "सभी विषय", "all", FlnDomain.LITERACY_VOCABULARY),
    AKSHAR("Ol Chiki Akshar (30 Letters)", "ᱚᱞ ᱪᱤᱠᱤ (᱓᱐ ᱟᱠᱷᱚᱨ)", "ओल चिकी (३० वर्ण)", "akshar", FlnDomain.LITERACY_ALPHABET),
    NUMBERS("Numbers 1 to 20", "ᱞᱮᱠᱷᱟ (᱑-᱒᱐)", "संख्याएँ (१-२०)", "numbers", FlnDomain.NUMERACY_COUNTING),
    ANIMALS("Animals", "ᱡᱤᱵᱽ ᱡᱤᱭᱟᱹᱞᱤ", "जीव-जंतु", "animals", FlnDomain.LITERACY_VOCABULARY),
    FRUITS("Fruits & Food", "ᱡᱚ ᱟᱨ ᱡᱚᱢᱟᱜ", "फल एवं भोजन", "fruits", FlnDomain.LITERACY_VOCABULARY),
    NATURE("Nature & Forest", "ᱫᱟᱨᱮ ᱱᱟᱹᱲᱤ", "प्रकृति एवं वन", "nature", FlnDomain.LITERACY_VOCABULARY),
    SCHOOL("School & Home", "ᱤᱛᱩᱱ ᱟᱥᱲᱟ ᱟᱨ ᱚᱲᱟᱜ", "विद्यालय एवं घर", "school", FlnDomain.LITERACY_VOCABULARY),
    SPATIAL("Shapes & Comparison", "ᱨᱩᱯ ᱟᱨ ᱥᱚᱢᱟᱱ", "आकृतियाँ एवं तुलना", "spatial", FlnDomain.NUMERACY_SPATIAL),
    ARITHMETIC("Arithmetic & Math", "ᱮᱞ / ᱡᱚᱲ-ᱵᱷᱮᱜᱟᱨ", "अंकगणित (जोड़-घटाव)", "arithmetic", FlnDomain.NUMERACY_COUNTING),
    MONEY("Money & Coins", "ᱴᱟᱠᱟ ᱟᱨ ᱯᱩᱭᱥᱟᱹ", "रुपया और पैसा", "money", FlnDomain.NUMERACY_COUNTING)
}

/**
 * Interactive play modes for flashcards.
 * Completely free of emoji strings.
 */
enum class FlnPlayMode(
    val displayName: String,
    val santaliName: String,
    val hindiName: String,
    val iconKey: String
) {
    EXPLORE("Learn & Flip", "ᱪᱮᱫᱚᱜ ᱟᱨ ᱩᱞᱴᱟᱹᱣ", "पढ़ें और पलटें", "flip"),
    HANDS_ON("Hands-On Math/Trace", "ᱛᱤ ᱛᱮ ᱠᱟᱹᱢᱤ", "करके सीखें", "touch"),
    QUIZ("Quiz Challenge", "ᱠᱩᱠᱞᱤ ᱮᱱᱮᱡ", "प्रश्नोत्तरी", "quiz")
}

/**
 * Unified Foundational Literacy and Numeracy Card Model.
 * Encapsulates dual script, phonics, exemplar words, illustrations, and tactile cues.
 */
data class FlnCard(
    val id: String,
    val domain: FlnDomain,
    val category: FlnCategory,
    val santaliOlChiki: String,
    val hindiText: String,
    val englishGloss: String,
    val teacherPhoneticGuide: String,
    val santaliDevanagariPhonetic: String = teacherPhoneticGuide,
    val imageAssetPath: String? = null,
    val vectorIconType: String = "star",
    val exemplarWordSantali: String = "",
    val exemplarWordHindi: String = "",
    val exemplarPhonetic: String = "",
    val numeralValue: Int? = null,
    val countingQuantity: Int = 1,
    val countingTokenNameSantali: String = "ᱥᱟᱠᱟᱢ",
    val countingTokenNameHindi: String = "पत्ता",
    val nipunCode: String = "L-G1.1",
    val grade: FlnGrade = FlnGrade.GRADE_1,
    val fingerTracingGuide: String = "",
    val exampleSentenceSantali: String = "",
    val exampleSentenceHindi: String = "",
    val phonicsClassification: String = "" // e.g. "Puy-lu Raha Arang (First Vowel)"
)

/**
 * Grade level alignment under NIPUN Bharat FLN mission.
 */
enum class FlnGrade(val displayName: String, val hindiName: String, val ageGroup: String) {
    BALVATIKA("Balvatika (Pre-K)", "बालवाटिका", "Ages 5–6"),
    GRADE_1("Grade 1", "कक्षा 1", "Ages 6–7"),
    GRADE_2("Grade 2", "कक्षा 2", "Ages 7–8"),
    GRADE_3("Grade 3", "कक्षा 3", "Ages 8–9")
}

/**
 * Difficulty level scaffolding.
 */
enum class WorksheetDifficulty(val displayName: String, val hindiName: String) {
    EASY("Level 1 (Easy)", "स्तर 1 (सरल)"),
    MEDIUM("Level 2 (Standard)", "स्तर 2 (मध्यम)"),
    HARD("Level 3 (Challenge)", "स्तर 3 (कठिन)")
}

/**
 * 6 Standardized NIPUN Bharat bilingual worksheet formats.
 */
enum class WorksheetType(
    val displayName: String,
    val santaliName: String,
    val hindiName: String,
    val description: String,
    val nipunTargetCode: String
) {
    AKSHAR_TRACING(
        "Letter Tracing & Phonics",
        "ᱪᱤᱠᱤ ᱪᱮᱫᱚᱜ",
        "वर्ण अनुरेखण एवं ध्वनि",
        "Handwriting Ol Chiki with directional guidelines.",
        "L-BAL.1"
    ),
    COUNT_AND_MATCH(
        "Count & Match Objects",
        "ᱞᱮᱠᱷᱟ ᱟᱨ ᱡᱚᱲ",
        "गिनें और सही संख्या से मिलाएँ",
        "Count village items and match to Ol Chiki numerals.",
        "N-G1.1"
    ),
    PICTURE_WORD_MATCH(
        "Picture & Word Association",
        "ᱪᱤᱛᱟᱹᱨ ᱟᱨ ᱟᱹᱲᱟᱹ",
        "चित्र एवं शब्द मिलान",
        "Connect illustrations with bilingual Ol Chiki/Hindi words.",
        "L-G1.1"
    ),
    ADDITION_WORD_PROBLEM(
        "Bilingual Visual Addition",
        "ᱡᱚᱲᱟᱣ ᱞᱮᱠᱷᱟ",
        "चित्र सहित जोड़ अभ्यास",
        "Bilingual concrete addition word problems with objects.",
        "N-G1.2"
    ),
    NUMBER_SEQUENCE_TRAIN(
        "Number Train Sequence",
        "ᱞᱮᱠᱷᱟ ᱨᱮᱞᱜᱟᱹᱰᱤ",
        "संख्या रेलगाड़ी क्रम",
        "Fill in missing numbers in train carriages.",
        "N-G1.1"
    ),
    MISSING_AKSHAR_SPELLING(
        "Missing Akshar Spelling",
        "ᱪᱤᱠᱤ ᱯᱮᱨᱮᱡ",
        "रिक्त वर्ण भरकर शब्द पूरा करें",
        "Complete the word by identifying the missing letter.",
        "L-G2.1"
    ),
    SUBTRACTION_PROBLEM(
        "Bilingual Visual Subtraction",
        "ᱵᱷᱮᱜᱟᱨ ᱞᱮᱠᱷᱟ",
        "चित्र सहित घटाव अभ्यास",
        "Concrete subtraction with visual crossing-out.",
        "N-G1.2"
    ),
    MULTIPLICATION_GROUPS(
        "Multiplication Groups Array",
        "ᱜᱩᱬᱟᱹ ᱦᱟᱹᱴᱤᱧ",
        "समान समूह और गुणा अभ्यास",
        "Multiplication as repeated addition using visual item groups.",
        "N-G2.2"
    ),
    MONEY_COUNTING(
        "Indian Currency / Money",
        "ᱴᱟᱠᱟ ᱟᱨ ᱯᱩᱭᱥᱟᱹ",
        "रुपये-पैसे की गणना",
        "Calculate total money with Indian coins and notes.",
        "N-G2.3"
    )
}

/**
 * Configuration for on-the-spot printable bilingual worksheet generation.
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
    val includeTeacherKey: Boolean = true,
    val topicPrompt: String = ""
)

/**
 * Single problem item in a generated worksheet.
 */
data class WorksheetItem(
    val id: String,
    val promptHindi: String,
    val promptSantali: String = "",
    val iconType: String = "star",
    val imageAssetPath: String? = null,
    val quantity: Int = 1,
    val secondaryQuantity: Int = 0,
    val operationSign: String = "",
    val leftLabelHindi: String = "",
    val rightLabelSantali: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val mathAnswer: Int? = null,
    val wordWithBlank: String? = null,
    val sequenceItems: List<String> = emptyList(),
    val missingSequenceIndex: Int = -1,
    val teacherSolutionNote: String = "",
    val teacherPhoneticAnswer: String = "",
    val nipunCode: String = "",
    val activityIR: org.tribetalk.fln.pipeline.ActivityIR? = null
)

/**
 * Preview tabs for the worksheet studio.
 */
enum class WorksheetPreviewTab {
    STUDENT_SHEET,
    TEACHER_KEY
}

/**
 * Dynamic 4-choice quiz question generated for interactive quiz mode.
 */
data class QuizQuestion(
    val card: FlnCard,
    val promptHindi: String,
    val promptSantali: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val hintHindi: String
)
