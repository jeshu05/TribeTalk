package com.alchemists.tribetalk.lessons

import java.util.UUID

/**
 * Data model representing a bilingual FLN lesson unit.
 */
data class Lesson(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val topic: String,
    val grade: String = "Grade 1",
    val learningDomain: String,
    val learningOutcome: String,
    val hindiIntroduction: String,
    val santaliIntroduction: String,
    val lessonScriptHindi: String,
    val lessonScriptSantali: String,
    val activityInstructionsHindi: String,
    val activityInstructionsSantali: String,
    val assessmentPromptsHindi: String,
    val assessmentPromptsSantali: String,
    val createdAt: Long = System.currentTimeMillis()
)
