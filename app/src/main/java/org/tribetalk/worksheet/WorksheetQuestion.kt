package org.tribetalk.worksheet

import java.util.UUID

/**
 * Question types supported in Phase 9 bilingual FLN worksheets.
 */
enum class QuestionType(val displayName: String) {
    MATCHING("Match the Following"),
    FILL_IN_THE_BLANK("Fill in the Blank"),
    MULTIPLE_CHOICE("Multiple Choice Question"),
    WORD_MEANING("Word Meaning / Vocabulary"),
    READ_AND_ANSWER("Read and Answer")
}

/**
 * Represents a single bilingual activity/question in a worksheet.
 */
data class WorksheetQuestion(
    val id: String = UUID.randomUUID().toString(),
    val type: QuestionType,
    val hindiText: String,
    val santaliText: String,
    val options: List<String> = emptyList(),
    val answer: String = "",
    val editable: Boolean = true
)
