package org.tribetalk.curriculum.cache

import org.tribetalk.curriculum.context.TeachingContext
import org.tribetalk.curriculum.ontology.LearningObjective
import org.tribetalk.curriculum.spec.ActivitySpec
import org.tribetalk.fln.model.FlnCard
import org.tribetalk.fln.model.WorksheetItem
import java.security.MessageDigest

/**
 * High-performance, versioned multi-tier caching system for TribeTalk.
 * Distinct caches for Activities, Flashcards, and Worksheets.
 */
object CurriculumCacheManager {

    private class LruCache<K, V>(private val maxEntries: Int) : LinkedHashMap<K, V>(maxEntries, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
            return size > maxEntries
        }
    }

    private val activityCache = LruCache<String, ActivitySpec>(64)
    private val flashcardCache = LruCache<String, List<FlnCard>>(32)
    private val worksheetCache = LruCache<String, List<WorksheetItem>>(32)

    private val lock = Any()

    fun buildCacheKey(
        context: TeachingContext,
        objective: LearningObjective,
        seed: Long,
        modelVersion: String = "Qwen-0.5B-v2.5",
        curriculumVersion: String = "NIPUN-2026.1",
        rendererVersion: String = "TT-SVG-v1"
    ): String {
        val raw = "${context.toContextKey()}_${objective.id}_${seed}_${modelVersion}_${curriculumVersion}_${rendererVersion}"
        return sha256(raw)
    }

    fun getCachedActivity(key: String): ActivitySpec? {
        synchronized(lock) {
            return activityCache[key]
        }
    }

    fun putActivity(key: String, spec: ActivitySpec) {
        synchronized(lock) {
            activityCache[key] = spec
        }
    }

    fun getCachedFlashcards(key: String): List<FlnCard>? {
        synchronized(lock) {
            return flashcardCache[key]
        }
    }

    fun putFlashcards(key: String, cards: List<FlnCard>) {
        synchronized(lock) {
            flashcardCache[key] = cards
        }
    }

    fun getCachedWorksheet(key: String): List<WorksheetItem>? {
        synchronized(lock) {
            return worksheetCache[key]
        }
    }

    fun putWorksheet(key: String, items: List<WorksheetItem>) {
        synchronized(lock) {
            worksheetCache[key] = items
        }
    }

    fun clearAll() {
        synchronized(lock) {
            activityCache.clear()
            flashcardCache.clear()
            worksheetCache.clear()
        }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
