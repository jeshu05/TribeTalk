package org.tribetalk.core

import android.content.Context

/**
 * High-performance on-device AI Translator facade for TribeTalk.
 * Enhances NLP performance with dual-tier LRU caching, thread safety,
 * and zero-latency retrieval for recurring classroom dialogues and curriculum prompts.
 *
 * Preserves the underlying TribeTalkNeuralTranslator intact.
 */
object TribeTalkTranslator {

    private var neuralTranslator: TribeTalkNeuralTranslator? = null

// High-performance thread-safe LRU caches for sub-millisecond repeated translations and phonetics
    private class SimpleLruCache<K, V>(private val maxEntries: Int) : LinkedHashMap<K, V>(maxEntries, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
            return size > maxEntries
        }
    }

    private val translationCache = SimpleLruCache<String, String>(512)
    private val phoneticsCache = SimpleLruCache<String, String>(512)

    private val lock = Any()

    @Volatile
    var cacheHits: Long = 0L
        private set

    @Volatile
    var cacheMisses: Long = 0L
        private set

    fun initialize(context: Context) {
        if (neuralTranslator == null) {
            synchronized(lock) {
                if (neuralTranslator == null) {
                    neuralTranslator = TribeTalkNeuralTranslator(context.applicationContext)
                }
            }
        }
    }

    /**
     * Translates input text bidirectionally with LRU caching for maximum throughput.
     */
    fun translate(input: String, isHindiToSantali: Boolean): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""

        val cacheKey = "$isHindiToSantali:$trimmed"
        synchronized(lock) {
            val cached = translationCache.get(cacheKey)
            if (cached != null) {
                cacheHits++
                return cached
            }
        }

        cacheMisses++
        val result = neuralTranslator?.translate(trimmed, isHindiToSantali) ?: ""

        if (result.isNotEmpty()) {
            synchronized(lock) {
                translationCache.put(cacheKey, result)
            }
        }
        return result
    }

    /**
     * Converts Ol Chiki text into pronounceable Indic syllables for teachers & TTS with caching.
     */
    fun olChikiToSpeechPhonetics(olChikiText: String): String {
        val trimmed = olChikiText.trim()
        if (trimmed.isEmpty()) return ""

        synchronized(lock) {
            val cached = phoneticsCache.get(trimmed)
            if (cached != null) return cached
        }

        val result = neuralTranslator?.olChikiToSpeechPhonetics(trimmed) ?: trimmed

        synchronized(lock) {
            phoneticsCache.put(trimmed, result)
        }
        return result
    }

    fun clearCache() {
        synchronized(lock) {
            translationCache.clear()
            phoneticsCache.clear()
            cacheHits = 0L
            cacheMisses = 0L
        }
    }
}
