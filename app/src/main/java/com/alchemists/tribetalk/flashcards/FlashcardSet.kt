package com.alchemists.tribetalk.flashcards

import java.util.UUID

/**
 * Data model representing a set/deck of bilingual flashcards aligned to an FLN learning domain.
 */
data class FlashcardSet(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val topic: String,
    val domain: String,
    val learningOutcome: String,
    val grade: String? = "Grade 1-3",
    val cards: List<Flashcard> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
