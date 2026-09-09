package org.tribetalk.fln.model

/**
 * Grade levels aligned with the NIPUN Bharat Foundational Literacy and Numeracy framework.
 */
enum class FlnGrade(val displayName: String, val ageRange: String) {
    BALVATIKA("Balvatika (Pre-Primary)", "Ages 5-6"),
    GRADE_1("Class 1 (Grade 1)", "Ages 6-7"),
    GRADE_2("Class 2 (Grade 2)", "Ages 7-8")
}

/**
 * Domain of learning under NIPUN Bharat.
 */
enum class FlnDomain(val displayName: String) {
    LITERACY_AKSHAR("Akshar & Phonics (ᱚᱞ ᱪᱤᱠᱤ)"),
    LITERACY_VOCAB("Living Vocabulary (ᱡᱤᱵᱽ ᱡᱤᱭᱟᱹᱞᱤ & ᱥᱤᱨᱡᱚᱱ)"),
    NUMERACY_COUNTING("Number Sense & Counting (ᱞᱮᱠᱷᱟ)"),
    NUMERACY_SHAPES("Shapes & Spatial Concepts (ᱨᱩᱯ ᱟᱨ ᱡᱟᱭᱜᱟ)")
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
    val exampleSentenceSantali: String? = null
)

/**
 * Types of auto-generated bilingual worksheets.
 */
enum class WorksheetType(val displayName: String, val description: String) {
    COUNT_AND_MATCH("Count and Match (ᱞᱮᱠᱷᱟ ᱟᱨ ᱡᱚᱲᱟᱣ)", "Count the objects and connect to the Santali & Hindi numbers."),
    PICTURE_WORD_MATCH("Word & Picture Match (ᱪᱤᱛᱟᱹᱨ ᱟᱨ ᱟᱹᱲᱟᱹ)", "Match pictures to the correct bilingual words."),
    AKSHAR_TRACING("Letter Tracing (ᱪᱤᱠᱤ ᱪᱮᱫᱚᱜ)", "Practice handwriting Ol Chiki alphabets with phonetic guides."),
    ASSESSMENT_CIRCLE("Circle the Correct Answer (ᱴᱷᱤᱠ ᱟᱹᱲᱟᱹ ᱨᱮ ᱜᱩᱞ)", "Identify the correct Santali word for each concept.")
}

/**
 * Configuration options for generating a worksheet.
 */
data class WorksheetConfig(
    val title: String,
    val type: WorksheetType,
    val grade: FlnGrade,
    val questionCount: Int = 5,
    val seed: Long = System.currentTimeMillis()
)

/**
 * An individual problem item within a generated worksheet.
 */
data class WorksheetItem(
    val id: String,
    val prompt: String,
    val iconType: String,
    val quantity: Int = 1,
    val leftLabelHindi: String = "",
    val rightLabelSantali: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0
)
