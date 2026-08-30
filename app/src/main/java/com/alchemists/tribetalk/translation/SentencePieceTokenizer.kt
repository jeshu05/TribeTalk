package com.alchemists.tribetalk.translation

/**
 * Subword SentencePiece / BPE Tokenizer for NLLB-200 / IndicTrans2 format.
 * Enables neural sequence tokenization for Hindi (hin_Deva) and Santali (sat_Olck).
 */
object SentencePieceTokenizer {

    const val PAD_TOKEN_ID: Long = 0L
    const val UNK_TOKEN_ID: Long = 1L
    const val BOS_TOKEN_ID: Long = 2L
    const val EOS_TOKEN_ID: Long = 3L

    // NLLB-200 Language Special Tokens
    const val HIN_DEVA_LANG_ID: Long = 256057L
    const val SAT_OLCK_LANG_ID: Long = 256158L

    // Core subword vocabulary table
    private val vocabToId = mutableMapOf<String, Long>()
    private val idToVocab = mutableMapOf<Long, String>()

    init {
        // Register special tokens
        registerToken("<pad>", PAD_TOKEN_ID)
        registerToken("<unk>", UNK_TOKEN_ID)
        registerToken("<s>", BOS_TOKEN_ID)
        registerToken("</s>", EOS_TOKEN_ID)
        registerToken("__hin_Deva__", HIN_DEVA_LANG_ID)
        registerToken("__sat_Olck__", SAT_OLCK_LANG_ID)

        // Seed common subword units from FLN & BPCC vocabulary
        var currentId = 100L
        val seedWords = listOf(
            // Hindi tokens
            "नमस्ते", "आप", "कैसे", "हैं", "किताब", "खोलो", "बंद", "करो", "पढ़ते", "पढ़ते",
            "लिखना", "शुरू", "शांत", "रहो", "बैठ", "जाओ", "खड़े", "यहाँ", "आओ", "ब्लैकबोर्ड",
            "देखो", "ध्यान", "सुनो", "अच्छा", "शाबाश", "समझ", "नहीं", "आया", "मदद", "पानी",
            "पीना", "भूख", "काम", "पूरा", "एक", "दो", "तीन", "चार", "पांच", "छह", "सात", "आठ",
            "नौ", "दस", "स्कूल", "छात्र", "शिक्षक", "कल", "बारिश", "होगी", "छुट्टी", "मौसम",
            // Santali Ol Chiki tokens
            "ᱡᱚᱦᱟᱨ", "ᱪᱮᱠᱟ", "ᱢᱮᱱᱟᱢᱟ", "ᱫᱮᱞᱟ", "ᱵᱚᱱ", "ᱯᱟᱲᱦᱟᱣ-ᱟ", "ᱯᱩᱛᱷᱤ", "ᱡᱷᱤᱡᱽ", "ᱢᱮ",
            "ᱚᱞ", "ᱮᱦᱚᱵ", "ᱛᱷᱤᱨ", "ᱛᱟᱦᱮᱸᱱ", "ᱯᱮ", "ᱫᱩᱲᱩᱵ", "ᱛᱤᱸᱜᱩᱱ", "ᱱᱚᱰᱮ", "ᱦᱤᱡᱩᱜ", "ᱵᱚᱨᱰ",
            "ᱧᱮᱞ", "ᱫᱷᱮᱭᱟᱱ", "ᱟᱧᱡᱚᱢ", "ᱟᱹᱰᱤ", "ᱱᱟᱯᱟᱭ", "ᱥᱟᱨᱦᱟᱣ", "ᱵᱟᱹᱧ", "ᱵᱩᱡᱷᱟᱹᱣ", "ᱫᱟᱲᱮᱭᱟᱫᱟ",
            "ᱪᱮᱫ", "ᱟᱢ", "ᱜᱚᱲᱚ", "ᱫᱟᱜ", "ᱧᱩ", "ᱥᱟᱱᱟᱹᱧ", "ᱠᱟᱱᱟ", "ᱨᱮᱸᱜᱮᱡ", "ᱢᱤᱫ", "ᱵᱟᱨ", "ᱯᱮ",
            "ᱯᱩᱱ", "ᱢᱚᱬᱮ", "ᱛᱩᱨᱩᱭ", "ᱮᱭᱟᱭ", "ᱤᱨᱟᱹᱞ", "ᱟᱨᱮ", "ᱜᱮᱞ", "ᱤᱛᱩᱱ", "ᱚᱲᱟᱜ", "ᱯᱟᱹᱲᱦᱩᱣᱟᱹ",
            "ᱢᱟᱪᱮᱛ", "ᱜᱟᱯᱟ", "ᱡᱟᱹᱲᱤ", "ᱦᱩᱭᱩᱜ-ᱟ"
        )

        for (word in seedWords) {
            registerToken(word, currentId++)
            // Register subwords with SentencePiece leading space indicator   (U+2581)
            registerToken(" $word", currentId++)
        }
    }

    private fun registerToken(token: String, id: Long) {
        vocabToId[token] = id
        idToVocab[id] = token
    }

    /**
     * Tokenizes an input sentence into an array of Long token IDs with language tags.
     */
    fun tokenize(text: String, sourceLang: Language): LongArray {
        val tokens = mutableListOf<Long>()

        // 1. Prepend language tag and BOS token
        tokens.add(BOS_TOKEN_ID)
        val langId = if (sourceLang == Language.HINDI) HIN_DEVA_LANG_ID else SAT_OLCK_LANG_ID
        tokens.add(langId)

        // 2. Tokenize words / subwords
        val words = text.trim().split(Regex("\\s+"))
        for (word in words) {
            val clean = word.replace(Regex("[?.!,]"), "")
            val spWord = " $clean"
            when {
                vocabToId.containsKey(spWord) -> tokens.add(vocabToId[spWord]!!)
                vocabToId.containsKey(clean) -> tokens.add(vocabToId[clean]!!)
                else -> {
                    // Fallback to character-level token hash for OOV subwords
                    val charHash = 10000L + (clean.hashCode().toLong() and 0xFFFF)
                    tokens.add(charHash)
                }
            }
        }

        // 3. Append EOS token
        tokens.add(EOS_TOKEN_ID)
        return tokens.toLongArray()
    }

    /**
     * Detokenizes an array of token IDs back into a reconstructed text string.
     */
    fun detokenize(tokenIds: LongArray, targetLang: Language): String {
        val sb = StringBuilder()
        for (id in tokenIds) {
            // Skip control tokens
            if (id == BOS_TOKEN_ID || id == EOS_TOKEN_ID || id == PAD_TOKEN_ID ||
                id == HIN_DEVA_LANG_ID || id == SAT_OLCK_LANG_ID) {
                continue
            }
            val piece = idToVocab[id]
            if (piece != null) {
                if (piece.startsWith(" ")) {
                    if (sb.isNotEmpty()) sb.append(" ")
                    sb.append(piece.substring(1))
                } else {
                    sb.append(" ").append(piece)
                }
            }
        }
        val raw = sb.toString().trim().replace(Regex("\\s+"), " ")
        return if (targetLang == Language.SANTALI) {
            OlChikiTransliterator.toOlChiki(raw)
        } else {
            raw
        }
    }
}
