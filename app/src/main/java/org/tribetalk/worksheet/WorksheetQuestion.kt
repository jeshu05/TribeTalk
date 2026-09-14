package org.tribetalk.worksheet

import java.util.UUID

/**
 * Question types supported in NIPUN Bharat & NCF-FS bilingual worksheets.
 */
enum class QuestionType(val displayName: String) {
    MATCHING("Match the Following (जोड़ी मिलाओ)"),
    FILL_IN_THE_BLANK("Fill in the Blank (रिक्त स्थान)"),
    MULTIPLE_CHOICE("Multiple Choice (बहुविकल्पी)"),
    WORD_MEANING("Word Meaning (शब्द अर्थ)"),
    READ_AND_ANSWER("Read and Answer (पढ़ो और उत्तर दो)"),
    PICTURE_IDENTIFICATION("Picture Identification (चित्र पहचान)"),
    COUNT_AND_WRITE("Count & Write (गिनो और लिखो)"),
    ORDERING("Ordering (क्रमबद्ध करो)"),
    CLASSIFICATION("Classification & Sorting (वर्गीकरण)"),
    COMPLETE_PATTERN("Complete the Pattern (पैटर्न पूरा करो)"),
    TRACE_OR_WRITE("Tracing & Writing (अनुरेखण एवं लेखन)"),
    TRUE_FALSE("True or False (सही या गलत)"),
    SOLVE("Solve (हल करो)"),
    SHORT_ANSWER("Short Answer (संक्षिप्त उत्तर)"),
    SEQUENCING("Sequencing (घटनाक्रम)")
}

/**
 * Verification state for bilingual Santali content.
 */
enum class SantaliVerificationStatus(val displayName: String) {
    VERIFIED("Verified"),
    TEACHER_VERIFIED("Teacher Verified"),
    NEEDS_REVIEW("Needs Review"),
    UNAVAILABLE("Unavailable")
}

/**
 * Represents a single bilingual activity/question in a worksheet, with full NIPUN curriculum traceability.
 */
data class WorksheetQuestion(
    val id: String = UUID.randomUUID().toString(),
    val type: QuestionType,
    val hindiText: String,
    val santaliText: String,
    val options: List<String> = emptyList(),
    val answer: String = "",
    val editable: Boolean = true,
    // Curriculum Traceability
    val curriculumItemId: String = "",
    val stage: String = "",
    val domain: String = "",
    val competencyId: String = "",
    val learningOutcomeId: String = "",
    val verificationStatus: SantaliVerificationStatus = SantaliVerificationStatus.VERIFIED,
    val visualAssetRef: String? = null
)
