package com.alchemists.tribetalk.insights

/**
 * Single activity log item for local classroom monitoring.
 */
data class ActivityRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val detail: String,
    val domain: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Summary metrics of classroom activities.
 */
data class ClassroomMetrics(
    val lessonsUsedCount: Int,
    val flashcardSessionsCount: Int,
    val worksheetsCreatedCount: Int,
    val voiceTranslationsCount: Int,
    val literacyProgress: Float,
    val numeracyProgress: Float,
    val environmentalProgress: Float
)

/**
 * Lightweight local Classroom Analytics Manager.
 *
 * Tracks local teacher activities (lessons, flashcards, worksheets, voice sessions)
 * without cloud transmission or heavy AI analytics.
 */
object ClassroomAnalyticsManager {

    private var lessonsCount = 3
    private var flashcardsCount = 2
    private var worksheetsCount = 1
    private var voiceTranslationsCount = 12

    private val activityLog = mutableListOf(
        ActivityRecord(
            title = "Greetings lesson opened",
            detail = "Grade 1 • Foundational Literacy",
            domain = "Foundational Literacy"
        ),
        ActivityRecord(
            title = "Animals flashcards used",
            detail = "5 bilingual visual cards • Oral Vocabulary",
            domain = "Foundational Literacy"
        ),
        ActivityRecord(
            title = "Numbers worksheet created",
            detail = "6 activities • Counting 1-10",
            domain = "Foundational Numeracy"
        ),
        ActivityRecord(
            title = "Live voice translation session",
            detail = "12 Hindi ➔ Santali phrases translated",
            domain = "Foundational Literacy"
        )
    )

    fun logLessonOpened(title: String, domain: String) {
        lessonsCount++
        activityLog.add(
            0,
            ActivityRecord(
                title = "$title lesson opened",
                detail = domain,
                domain = domain
            )
        )
    }

    fun logFlashcardSession(topic: String, cardCount: Int) {
        flashcardsCount++
        activityLog.add(
            0,
            ActivityRecord(
                title = "$topic flashcard session",
                detail = "$cardCount visual cards used in Study Mode",
                domain = "Foundational Literacy"
            )
        )
    }

    fun logWorksheetCreated(topic: String, questionCount: Int) {
        worksheetsCount++
        activityLog.add(
            0,
            ActivityRecord(
                title = "$topic worksheet created",
                detail = "$questionCount bilingual activities",
                domain = "Foundational Numeracy"
            )
        )
    }

    fun logVoiceTranslationSession(phraseCount: Int) {
        voiceTranslationsCount += phraseCount
        activityLog.add(
            0,
            ActivityRecord(
                title = "Live Classroom voice session",
                detail = "$phraseCount phrases translated with speech",
                domain = "Foundational Literacy"
            )
        )
    }

    fun getMetrics(): ClassroomMetrics {
        return ClassroomMetrics(
            lessonsUsedCount = lessonsCount,
            flashcardSessionsCount = flashcardsCount,
            worksheetsCreatedCount = worksheetsCount,
            voiceTranslationsCount = voiceTranslationsCount,
            literacyProgress = 0.80f,
            numeracyProgress = 0.60f,
            environmentalProgress = 0.50f
        )
    }

    fun getRecentActivities(): List<ActivityRecord> = activityLog.take(10)

    fun clearActivity() {
        lessonsCount = 0
        flashcardsCount = 0
        worksheetsCount = 0
        voiceTranslationsCount = 0
        activityLog.clear()
    }
}
