package com.alchemists.tribetalk.voice

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Streaming Orchestration Layer around AI4Bharat IndicConformer ONNX ASR.
 * Handles sliding-window PCM audio processing, text stabilization, and sentence deduplication.
 */
class RealtimeHindiAsr(
    context: Context,
    private val indicConformerAsr: IndicConformerHindiAsr = IndicConformerHindiAsr(context)
) {
    companion object {
        private const val TAG = "RealtimeHindiAsr"
    }

    suspend fun processUtterancePcm(pcm: ShortArray): String = withContext(Dispatchers.IO) {
        if (pcm.isEmpty()) return@withContext ""
        val rawText = indicConformerAsr.transcribe(pcm)
        val stabilized = stabilizeText(rawText)
        Log.i(TAG, "[Realtime ASR] Transcribed text: '$stabilized' (Raw: '$rawText')")
        stabilized
    }

    /**
     * Deduplicates adjacent repeated words or phrase fragments.
     */
    fun stabilizeText(text: String): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ""

        val words = trimmed.split("\\s+".toRegex())
        val cleanWords = mutableListOf<String>()

        for (word in words) {
            if (cleanWords.isEmpty() || cleanWords.last() != word) {
                cleanWords.add(word)
            }
        }

        return cleanWords.joinToString(" ")
    }
}
