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
    val createdAt: Long = System.currentTimeMillis()
)
