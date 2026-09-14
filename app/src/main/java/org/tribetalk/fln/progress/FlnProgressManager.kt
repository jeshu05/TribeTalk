package org.tribetalk.fln.progress

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.ConcurrentHashMap

/**
 * Data class holding offline progress metrics for an individual flashcard.
 */
data class CardProgress(
    val cardId: String,
    val seenCount: Int = 0,
    val revealedCount: Int = 0,
    val heardCount: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val lastPracticedAt: Long? = null
) {
    val totalAttempts: Int get() = correctCount + incorrectCount
    val accuracyPercent: Int get() = if (totalAttempts > 0) (correctCount * 100) / totalAttempts else 0
    val isMastered: Boolean get() = correctCount >= 3 && accuracyPercent >= 75
}

/**
 * Lightweight, 100% offline progress tracker for NIPUN Bharat FLN flashcards.
 * Uses an in-memory cache synchronized with Android SharedPreferences. Zero cloud overhead.
 */
class FlnProgressManager(context: Context? = null) {

    companion object {
        private const val PREFS_NAME = "tribetalk_fln_progress"
        private const val KEY_SEEN = "_seen"
        private const val KEY_REVEALED = "_revealed"
        private const val KEY_HEARD = "_heard"
        private const val KEY_CORRECT = "_correct"
        private const val KEY_INCORRECT = "_incorrect"
        private const val KEY_LAST_PRACTICED = "_last_practiced"

        @Volatile
        private var instance: FlnProgressManager? = null

        fun getInstance(context: Context? = null): FlnProgressManager {
            return instance ?: synchronized(this) {
                instance ?: FlnProgressManager(context?.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val memoryCache = ConcurrentHashMap<String, CardProgress>()

    init {
        loadFromPrefs()
    }

    private fun loadFromPrefs() {
        if (prefs == null) return
        val allEntries = prefs.all
        val cardIds = allEntries.keys.mapNotNull { key ->
            when {
                key.endsWith(KEY_SEEN) -> key.removeSuffix(KEY_SEEN)
                key.endsWith(KEY_REVEALED) -> key.removeSuffix(KEY_REVEALED)
                key.endsWith(KEY_HEARD) -> key.removeSuffix(KEY_HEARD)
                key.endsWith(KEY_CORRECT) -> key.removeSuffix(KEY_CORRECT)
                key.endsWith(KEY_INCORRECT) -> key.removeSuffix(KEY_INCORRECT)
                key.endsWith(KEY_LAST_PRACTICED) -> key.removeSuffix(KEY_LAST_PRACTICED)
                else -> null
            }
        }.distinct()

        for (id in cardIds) {
            val progress = CardProgress(
                cardId = id,
                seenCount = prefs.getInt(id + KEY_SEEN, 0),
                revealedCount = prefs.getInt(id + KEY_REVEALED, 0),
                heardCount = prefs.getInt(id + KEY_HEARD, 0),
                correctCount = prefs.getInt(id + KEY_CORRECT, 0),
                incorrectCount = prefs.getInt(id + KEY_INCORRECT, 0),
                lastPracticedAt = if (prefs.contains(id + KEY_LAST_PRACTICED)) prefs.getLong(id + KEY_LAST_PRACTICED, 0L) else null
            )
            memoryCache[id] = progress
        }
    }

    fun getCardProgress(cardId: String): CardProgress {
        return memoryCache[cardId] ?: CardProgress(cardId = cardId)
    }

    @Synchronized
    fun recordCardSeen(cardId: String) {
        val current = getCardProgress(cardId)
        val now = System.currentTimeMillis()
        val updated = current.copy(
            seenCount = current.seenCount + 1,
            lastPracticedAt = now
        )
        memoryCache[cardId] = updated
        prefs?.edit()
            ?.putInt(cardId + KEY_SEEN, updated.seenCount)
            ?.putLong(cardId + KEY_LAST_PRACTICED, now)
            ?.apply()
    }

    @Synchronized
    fun recordCardRevealed(cardId: String) {
        val current = getCardProgress(cardId)
        val now = System.currentTimeMillis()
        val updated = current.copy(
            revealedCount = current.revealedCount + 1,
            lastPracticedAt = now
        )
        memoryCache[cardId] = updated
        prefs?.edit()
            ?.putInt(cardId + KEY_REVEALED, updated.revealedCount)
            ?.putLong(cardId + KEY_LAST_PRACTICED, now)
            ?.apply()
    }

    @Synchronized
    fun recordCardHeard(cardId: String) {
        val current = getCardProgress(cardId)
        val now = System.currentTimeMillis()
        val updated = current.copy(
            heardCount = current.heardCount + 1,
            lastPracticedAt = now
        )
        memoryCache[cardId] = updated
        prefs?.edit()
            ?.putInt(cardId + KEY_HEARD, updated.heardCount)
            ?.putLong(cardId + KEY_LAST_PRACTICED, now)
            ?.apply()
    }

    @Synchronized
    fun recordQuizResult(cardId: String, isCorrect: Boolean) {
        val current = getCardProgress(cardId)
        val now = System.currentTimeMillis()
        val updated = if (isCorrect) {
            current.copy(
                correctCount = current.correctCount + 1,
                lastPracticedAt = now
            )
        } else {
            current.copy(
                incorrectCount = current.incorrectCount + 1,
                lastPracticedAt = now
            )
        }
        memoryCache[cardId] = updated
        prefs?.edit()
            ?.putInt(cardId + KEY_CORRECT, updated.correctCount)
            ?.putInt(cardId + KEY_INCORRECT, updated.incorrectCount)
            ?.putLong(cardId + KEY_LAST_PRACTICED, now)
            ?.apply()
    }

    @Synchronized
    fun recordMatchingResult(cardId: String, isCorrect: Boolean) {
        recordQuizResult(cardId, isCorrect)
    }

    fun getTotalCardsPracticed(): Int {
        return memoryCache.values.count { it.seenCount > 0 || it.totalAttempts > 0 }
    }

    fun getTotalCorrectAnswers(): Int {
        return memoryCache.values.sumOf { it.correctCount }
    }

    fun getMasteredCardCount(): Int {
        return memoryCache.values.count { it.isMastered }
    }

    @Synchronized
    fun resetProgress() {
        memoryCache.clear()
        prefs?.edit()?.clear()?.apply()
    }
}
