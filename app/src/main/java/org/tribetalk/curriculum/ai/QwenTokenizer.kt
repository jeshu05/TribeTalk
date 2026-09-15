package org.tribetalk.curriculum.ai

import android.util.Log
import org.json.JSONObject
import java.io.File

/**
 * On-device BPE Tokenizer for Qwen 2.5 0.5B.
 * Encodes prompts into token IDs and detokenizes generated output IDs back into text.
 */
class QwenTokenizer {

    companion object {
        private const val TAG = "QwenTokenizer"

        const val IM_START_TOKEN_ID = 151644L
        const val IM_END_TOKEN_ID = 151645L
        const val ENDOFTEXT_TOKEN_ID = 151643L

        const val IM_START_STR = "<|im_start|>"
        const val IM_END_STR = "<|im_end|>"
    }

    private val tokenToId = HashMap<String, Long>(152000)
    private val idToToken = HashMap<Long, String>(152000)
    private var isInitialized = false

    /**
     * Loads vocabulary from vocab.json or tokenizer.json.
     */
    fun loadVocab(vocabFile: File): Boolean {
        if (!vocabFile.exists() || vocabFile.length() == 0L) {
            Log.w(TAG, "Vocab file not found: ${vocabFile.absolutePath}")
            populateBaseVocab()
            return false
        }

        return try {
            val content = vocabFile.readText(Charsets.UTF_8)
            val json = JSONObject(content)
            val keys = json.keys()
            while (keys.hasNext()) {
                val token = keys.next()
                val id = json.getLong(token)
                tokenToId[token] = id
                idToToken[id] = token
            }
            // Ensure special tokens
            tokenToId[IM_START_STR] = IM_START_TOKEN_ID
            idToToken[IM_START_TOKEN_ID] = IM_START_STR
            tokenToId[IM_END_STR] = IM_END_TOKEN_ID
            idToToken[IM_END_TOKEN_ID] = IM_END_STR
            tokenToId["<|endoftext|>"] = ENDOFTEXT_TOKEN_ID
            idToToken[ENDOFTEXT_TOKEN_ID] = "<|endoftext|>"

            isInitialized = true
            Log.i(TAG, "Loaded ${tokenToId.size} tokens from ${vocabFile.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed loading vocab: ${e.message}", e)
            populateBaseVocab()
            false
        }
    }

    private fun populateBaseVocab() {
        tokenToId[IM_START_STR] = IM_START_TOKEN_ID
        idToToken[IM_START_TOKEN_ID] = IM_START_STR
        tokenToId[IM_END_STR] = IM_END_TOKEN_ID
        idToToken[IM_END_TOKEN_ID] = IM_END_STR
        tokenToId["<|endoftext|>"] = ENDOFTEXT_TOKEN_ID
        idToToken[ENDOFTEXT_TOKEN_ID] = "<|endoftext|>"

        // Core ASCII single characters
        for (i in 0..127) {
            val s = i.toChar().toString()
            tokenToId[s] = i.toLong()
            idToToken[i.toLong()] = s
        }
        isInitialized = true
    }

    /**
     * Encodes raw text into a sequence of token IDs.
     */
    fun encode(text: String): LongArray {
        if (!isInitialized) populateBaseVocab()
        if (text.isEmpty()) return LongArray(0)

        val tokens = mutableListOf<Long>()
        var remaining = text

        while (remaining.isNotEmpty()) {
            if (remaining.startsWith(IM_START_STR)) {
                tokens.add(IM_START_TOKEN_ID)
                remaining = remaining.substring(IM_START_STR.length)
                continue
            }
            if (remaining.startsWith(IM_END_STR)) {
                tokens.add(IM_END_TOKEN_ID)
                remaining = remaining.substring(IM_END_STR.length)
                continue
            }

            // BPE leading space/newline substitution
            val bpeRemaining = when (remaining[0]) {
                ' ' -> "\u0120" + remaining.substring(1)
                '\n' -> "\u010a" + remaining.substring(1)
                else -> null
            }

            var matched = false
            if (bpeRemaining != null) {
                val maxCheck = bpeRemaining.length.coerceAtMost(24)
                for (len in maxCheck downTo 1) {
                    val sub = bpeRemaining.substring(0, len)
                    val id = tokenToId[sub]
                    if (id != null) {
                        tokens.add(id)
                        remaining = remaining.substring(len)
                        matched = true
                        break
                    }
                }
            }

            if (!matched) {
                val maxCheck = remaining.length.coerceAtMost(24)
                for (len in maxCheck downTo 1) {
                    val sub = remaining.substring(0, len)
                    val id = tokenToId[sub]
                    if (id != null) {
                        tokens.add(id)
                        remaining = remaining.substring(len)
                        matched = true
                        break
                    }
                }
            }

            if (!matched) {
                // Byte fallback
                val firstChar = remaining[0]
                val id = when (firstChar) {
                    ' ' -> 220L
                    '\n' -> 198L
                    else -> tokenToId[firstChar.toString()] ?: (firstChar.code.toLong() % 151643L)
                }
                tokens.add(id)
                remaining = remaining.substring(1)
            }
        }

        return tokens.toLongArray()
    }

    /**
     * Decodes token IDs back into string text.
     */
    fun decode(tokenIds: LongArray): String = decode(tokenIds.toList())

    fun decode(tokenIds: List<Long>): String {
        val sb = StringBuilder()
        for (id in tokenIds) {
            if (id == IM_START_TOKEN_ID || id == IM_END_TOKEN_ID || id == ENDOFTEXT_TOKEN_ID) {
                continue
            }
            val piece = idToToken[id]
            if (piece != null) {
                // Qwen BPE uses 'Ġ' (\u0120) for leading space
                val clean = piece.replace("Ġ", " ").replace("Ċ", "\n")
                sb.append(clean)
            } else if (id in 0..255) {
                sb.append(id.toInt().toChar())
            }
        }
        return sb.toString()
    }

    fun isSpecialToken(id: Long): Boolean {
        return id == IM_START_TOKEN_ID || id == IM_END_TOKEN_ID || id == ENDOFTEXT_TOKEN_ID
    }
}
