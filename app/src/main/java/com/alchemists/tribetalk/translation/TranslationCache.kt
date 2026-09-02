package com.alchemists.tribetalk.translation

import java.util.Collections

/**
 * Bounded In-Memory Cache for Translations and TTS Audio.
 * Prevents redundant ONNX model inferences on common classroom phrases.
 */
object TranslationCache {

    private const val MAX_ENTRIES = 50

    private val textCache = Collections.synchronizedMap(
        object : LinkedHashMap<String, String>(MAX_ENTRIES, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
                return size > MAX_ENTRIES
            }
        }
    )

    private val ttsCache = Collections.synchronizedMap(
        object : LinkedHashMap<String, ShortArray>(MAX_ENTRIES, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ShortArray>?): Boolean {
                return size > MAX_ENTRIES
            }
        }
    )

    fun getTranslation(sourceText: String, sourceLang: String, targetLang: String): String? {
        val key = "$sourceLang:$targetLang:${sourceText.trim().lowercase()}"
        return textCache[key]
    }

    fun putTranslation(sourceText: String, sourceLang: String, targetLang: String, translatedText: String) {
        val key = "$sourceLang:$targetLang:${sourceText.trim().lowercase()}"
        textCache[key] = translatedText
    }

    fun getTtsAudio(text: String, lang: String): ShortArray? {
        val key = "$lang:${text.trim().lowercase()}"
        return ttsCache[key]
    }

    fun putTtsAudio(text: String, lang: String, pcmAudio: ShortArray) {
        val key = "$lang:${text.trim().lowercase()}"
        ttsCache[key] = pcmAudio
    }

    fun clear() {
        textCache.clear()
        ttsCache.clear()
    }
}
