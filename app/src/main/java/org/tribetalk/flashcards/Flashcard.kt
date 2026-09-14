package org.tribetalk.flashcards

import java.util.UUID

/**
 * Data model representing a single bilingual visual flashcard.
 */
data class Flashcard(
    val id: String = UUID.randomUUID().toString(),
    val setId: String = "",
    val topic: String = "",
    val skill: String = "",
    val hindiText: String,
    val santaliText: String,
    val santaliOlChiki: String? = null,
    val phoneticGuide: String? = null,
    val imageUri: String? = null,
    val imageEmoji: String? = null,
    val exampleSentenceHindi: String? = null,
    val exampleSentenceSantali: String? = null,
    val order: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    // FLN Curriculum Metadata
    val domain: String = "",
    val grade: String = "Grade 1-3",
    val difficulty: String = "Foundational",
    val englishGloss: String = "",
    val iconType: String = "",
    // Local Progress Tracking
    val seenCount: Int = 0,
    val revealedCount: Int = 0,
    val heardCount: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val lastPracticedAt: Long? = null
)
