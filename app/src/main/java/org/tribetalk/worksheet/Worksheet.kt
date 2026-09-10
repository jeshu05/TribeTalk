package org.tribetalk.worksheet

import java.util.UUID

/**
 * Represents a complete FLN bilingual worksheet.
 */
data class Worksheet(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val topic: String,
    val grade: String? = null,
    val instructions: String = "Read each activity carefully. Complete both Hindi and Santali sections.",
    val questions: List<WorksheetQuestion> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
