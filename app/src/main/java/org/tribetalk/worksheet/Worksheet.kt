package org.tribetalk.worksheet

import java.util.UUID

/**
 * Represents a complete NIPUN Bharat & NCF-FS bilingual worksheet.
 */
data class Worksheet(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val topic: String,
    val grade: String? = null,
    val stageId: String? = null,
    val domainId: String? = null,
    val curricularGoalId: String? = null,
    val competencyId: String? = null,
    val learningOutcomeText: String? = null,
    val instructions: String = "Read each activity carefully. Complete both Hindi and Santali sections.",
    val santaliInstructions: String = "ᱡᱚᱛᱚ ᱠᱟᱹᱢᱤ ᱱᱟᱯᱟᱭ ᱛᱮ ᱯᱟᱲᱦᱟᱣ ᱢᱮ ᱾ ᱦᱤᱱᱫᱤ ᱟᱨ ᱥᱟᱱᱛᱟᱲᱤ ᱵᱟᱱᱟᱨ ᱦᱟᱹᱴᱤᱧ ᱯᱩᱨᱟᱹᱣ ᱢᱮ ᱾",
    val questions: List<WorksheetQuestion> = emptyList(),
    val showTeacherAlignment: Boolean = false,
    val includeAnswerKey: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

