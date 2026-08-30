package com.alchemists.tribetalk.translation

/**
 * Bidirectional Transliteration and G2P (Grapheme-to-Phoneme) engine for Santali (Ol Chiki, U+1C50 - U+1C7F).
 * Converts seamlessly between:
 *  1. Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ) - The authentic 30-letter alphabet of Santali
 *  2. Romanized / Latin Phonetics (e.g., "Johar", "Ceka menama?")
 *  3. Phonetic Devanagari (e.g., "जोहार", "चेका मेनामा?") for Teacher HUD pronunciation guide
 */
object OlChikiTransliterator {

    // Ol Chiki Digits (U+1C50 .. U+1C59)
    private val digitToOlChiki = mapOf(
        '0' to '᱐', '1' to '᱑', '2' to '᱒', '3' to '᱓', '4' to '᱔',
        '5' to '᱕', '6' to '᱖', '7' to '᱗', '8' to '᱘', '9' to '᱙'
    )
    private val olChikiToDigit = digitToOlChiki.entries.associate { (k, v) -> v to k }

    // Multi-character Latin digraphs to Ol Chiki (ordered by descending length for greedy matching)
    private val latinDigraphsToOlChiki = listOf(
        "aang" to "ᱟᱝ",
        "paṛhao" to "ᱯᱟᱲᱦᱟᱣ",
        "parhao" to "ᱯᱟᱲᱦᱟᱣ",
        "bujhau" to "ᱵᱩᱡᱷᱟᱹᱣ",
        "goro" to "ᱜᱚᱲᱚ",
        "dare" to "ᱫᱟᱲᱮ",
        "aam" to "ᱟᱢ",
        "bon" to "ᱵᱚᱱ",
        "dela" to "ᱫᱮᱞᱟ",
        "ceka" to "ᱪᱮᱠᱟ",
        "menama" to "ᱢᱮᱱᱟᱢᱟ",
        "johar" to "ᱡᱚᱦᱟᱨ",
        "puthi" to "ᱯᱩᱛᱷᱤ",
        "macet" to "ᱢᱟᱪᱮᱛ",
        "parhua" to "ᱯᱟᱹᱲᱦᱩᱣᱟᱹ",
        "itun" to "ᱤᱛᱩᱱ",
        "orah" to "ᱚᱲᱟᱜ",
        "mit" to "ᱢᱤᱫ",
        "bar" to "ᱵᱟᱨ",
        "pe" to "ᱯᱮ",
        "pon" to "ᱯᱩᱱ",
        "kh" to "ᱠᱷ",
        "gh" to "ᱜᱷ",
        "ch" to "ᱪᱷ",
        "jh" to "ᱡᱷ",
        "th" to "ᱛᱷ",
        "dh" to "ᱫᱷ",
        "ph" to "ᱯᱷ",
        "bh" to "ᱵᱷ",
        "ng" to "ᱝ",
        "ny" to "ᱧ",
        "rr" to "ᱲ",
        "rh" to "ᱲᱷ",
        "tt" to "ᱴ",
        "dd" to "ᱰ",
        "nn" to "ᱬ"
    )

    // Base Latin to Ol Chiki single-char mapping
    private val latinSingleToOlChiki = mapOf(
        'a' to "ᱟ",
        'o' to "ᱚ",
        'i' to "ᱤ",
        'u' to "ᱩ",
        'e' to "ᱮ",
        'k' to "ᱠ",
        'g' to "ᱜ",
        't' to "ᱛ",
        'd' to "ᱫ",
        'n' to "ᱱ",
        'p' to "ᱯ",
        'b' to "ᱵ",
        'm' to "ᱢ",
        'y' to "ᱭ",
        'r' to "ᱨ",
        'l' to "ᱞ",
        'w' to "ᱣ",
        's' to "ᱥ",
        'h' to "ᱦ",
        'c' to "ᱪ",
        'j' to "ᱡ"
    )

    // Ol Chiki letter to Latin phonetic representation
    private val olChikiToLatin = mapOf(
        '᱐' to "0", '᱑' to "1", '᱒' to "2", '᱓' to "3", '᱔' to "4",
        '᱕' to "5", '᱖' to "6", '᱗' to "7", '᱘' to "8", '᱙' to "9",
        'ᱚ' to "o",
        'ᱛ' to "t",
        'ᱜ' to "g",
        'ᱝ' to "ng",
        'ᱞ' to "l",
        'ᱟ' to "a",
        'ᱠ' to "k",
        'ᱡ' to "j",
        'ᱢ' to "m",
        'ᱣ' to "w",
        'ᱤ' to "i",
        'ᱥ' to "s",
        'ᱦ' to "h",
        'ᱧ' to "ny",
        'ᱨ' to "r",
        'ᱩ' to "u",
        'ᱪ' to "c",
        'ᱫ' to "d",
        'ᱬ' to "n",
        'ᱭ' to "y",
        'ᱮ' to "e",
        'ᱯ' to "p",
        'ᱰ' to "dd",
        'ᱱ' to "n",
        'ᱲ' to "ṛ",
        'ᱳ' to "o",
        'ᱴ' to "tt",
        'ᱵ' to "b",
        'ᱶ' to "w",
        'ᱷ' to "h",
        'ᱸ' to "n",  // Mu ttuddag (nasal)
        'ᱹ' to "",   // Gaahlaa ttuddag (baseline low dot)
        'ᱺ' to "n",  // Mu-gaahlaa
        'ᱻ' to "'",  // Ahd (checked limiter)
        'ᱼ' to "-",  // Pharka (separator)
        '᱾' to ".",  // Mucaad
        '᱿' to "."   // Double mucaad
    )

    // Ol Chiki to Devanagari phonetics (for Teacher Assist Heads-Up Display)
    private val olChikiToDevanagari = mapOf(
        '᱐' to "०", '᱑' to "१", '᱒' to "२", '᱓' to "३", '᱔' to "४",
        '᱕' to "५", '᱖' to "६", '᱗' to "७", '᱘' to "८", '᱙' to "९",
        'ᱚ' to "ऑ",
        'ᱛ' to "त",
        'ᱜ' to "ग",
        'ᱝ' to "ं",
        'ᱞ' to "ल",
        'ᱟ' to "आ",
        'ᱠ' to "क",
        'ᱡ' to "ज",
        'ᱢ' to "म",
        'ᱣ' to "व",
        'ᱤ' to "इ",
        'ᱥ' to "स",
        'ᱦ' to "ह",
        'ᱧ' to "ञ",
        'ᱨ' to "र",
        'ᱩ' to "उ",
        'ᱪ' to "च",
        'ᱫ' to "द",
        'ᱬ' to "ण",
        'ᱭ' to "य",
        'ᱮ' to "ए",
        'ᱯ' to "प",
        'ᱰ' to "ड",
        'ᱱ' to "न",
        'ᱲ' to "ड़",
        'ᱳ' to "ओ",
        'ᱴ' to "ट",
        'ᱵ' to "ब",
        'ᱶ' to "वँ",
        'ᱷ' to "्ह",
        'ᱸ' to "ँ",
        'ᱹ' to "",
        'ᱺ' to "ँ",
        'ᱻ' to "’",
        'ᱼ' to "-",
        '᱾' to "।",
        '᱿' to "॥"
    )

    // Common full phrase mappings for fast sub-millisecond conversion
    private val phraseLatinToOlChiki = mapOf(
        "johar" to "ᱡᱚᱦᱟᱨ",
        "ceka menama" to "ᱪᱮᱠᱟ ᱢᱮᱱᱟᱢᱟ",
        "ceka menama?" to "ᱪᱮᱠᱟ ᱢᱮᱱᱟᱢᱟ?",
        "dela bon parhao-a" to "ᱫᱮᱞᱟ ᱵᱚᱱ ᱯᱟᱲᱦᱟᱣ-ᱟ",
        "dela bon paṛhao-a" to "ᱫᱮᱞᱟ ᱵᱚᱱ ᱯᱟᱲᱦᱟᱣ-ᱟ",
        "puthi jhije me" to "ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ",
        "puthi jhije me" to "ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ",
        "ol eho b me" to "ᱚᱞ ᱮᱦᱚᱵ ᱢᱮ",
        "ol ehob me" to "ᱚᱞ ᱮᱦᱚᱵ ᱢᱮ",
        "mit bar pe pon" to "ᱢᱤᱫ ᱵᱟᱨ ᱯᱮ ᱯᱩᱱ",
        "mit' bar pe pon" to "ᱢᱤᱫ ᱵᱟᱨ ᱯᱮ ᱯᱩᱱ",
        "ban bujhau dareada" to "ᱵᱟᱹᱧ ᱵᱩᱡᱷᱟᱹᱣ ᱫᱟᱲᱮᱭᱟᱫᱟ",
        "bañ bujhau daṛeada" to "ᱵᱟᱹᱧ ᱵᱩᱡᱷᱟᱹᱣ ᱫᱟᱲᱮᱭᱟᱫᱟ",
        "cet aam goro dareana?" to "ᱪᱮᱫ ᱟᱢ ᱜᱚᱲᱚ ᱫᱟᱲᱮᱭᱟᱹᱧᱟ?",
        "cet' aam goṛo daṛeaña?" to "ᱪᱮᱫ ᱟᱢ ᱜᱚᱲᱚ ᱫᱟᱲᱮᱭᱟᱹᱧᱟ?",
        "macet" to "ᱢᱟᱪᱮᱛ",
        "macet'" to "ᱢᱟᱪᱮᱛ",
        "parhua" to "ᱯᱟᱹᱲᱦᱩᱣᱟᱹ",
        "paṛhua" to "ᱯᱟᱹᱲᱦᱩᱣᱟᱹ",
        "itun orah" to "ᱤᱛᱩᱱ ᱚᱲᱟᱜ",
        "itun oṛaḥ" to "ᱤᱛᱩᱱ ᱚᱲᱟᱜ",
        "dag" to "ᱫᱟᱜ",
        "daka" to "ᱫᱟᱠᱟ",
        "sarhaw" to "ᱥᱟᱨᱦᱟᱣ"
    )

    private val phraseDevanagariToOlChiki = mapOf(
        "जोहार" to "ᱡᱚᱦᱟᱨ",
        "चेका मेनामा" to "ᱪᱮᱠᱟ ᱢᱮᱱᱟᱢᱟ",
        "चेका मेनामा?" to "ᱪᱮᱠᱟ ᱢᱮᱱᱟᱢᱟ?",
        "देला बोन पढ़ाओ-आ" to "ᱫᱮᱞᱟ ᱵᱚᱱ ᱯᱟᱲᱦᱟᱣ-ᱟ",
        "देला बोन पढाओ-आ" to "ᱫᱮᱞᱟ ᱵᱚᱱ ᱯᱟᱲᱦᱟᱣ-ᱟ",
        "पुथी झिजे मे" to "ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ",
        "ओल एहोब मे" to "ᱚᱞ ᱮᱦᱚᱵ ᱢᱮ",
        "ओल एहो ब मे" to "ᱚᱞ ᱮᱦᱚᱵ ᱢᱮ",
        "मित बार पे पोन" to "ᱢᱤᱫ ᱵᱟᱨ ᱯᱮ ᱯᱩᱱ",
        "बाँ बुझाउ दड़ेआदा" to "ᱵᱟᱹᱧ ᱵᱩᱡᱷᱟᱹᱣ ᱫᱟᱲᱮᱭᱟᱫᱟ",
        "चेत आम गोड़ो दड़ेआङा?" to "ᱪᱮᱫ ᱟᱢ ᱜᱚᱲᱚ ᱫᱟᱲᱮᱭᱟᱹᱧᱟ?",
        "माचेत" to "ᱢᱟᱪᱮᱛ",
        "पढ़ुआ" to "ᱯᱟᱹᱲᱦᱩᱣᱟᱹ",
        "इतुन ओड़ाः" to "ᱤᱛᱩᱱ ᱚᱲᱟᱜ",
        "इतुन ओड़ाग" to "ᱤᱛᱩᱱ ᱚᱲᱟᱜ",
        "मेरा नाम" to "ᱤᱧᱟᱜ ᱧᱩᱛᱩᱢ",
        "मेरा" to "ᱤᱧᱟᱜ",
        "नाम" to "ᱧᱩᱛᱩᱢ",
        "मेरा नाम है" to "ᱤᱧᱟᱜ ᱧᱩᱛᱩᱢ ᱫᱚ ᱠᱟᱱᱟ",
        "दाग" to "ᱫᱟᱜ",
        "दाका" to "ᱫᱟᱠᱟ",
        "सारहाव" to "ᱥᱟᱨᱦᱟᱣ"
    )

    private val devanagariSingleToOlChiki = mapOf(
        'अ' to "ᱚ", 'आ' to "ᱟ", 'इ' to "ᱤ", 'ई' to "ᱤ", 'उ' to "ᱩ", 'ऊ' to "ᱩ", 'ए' to "ᱮ", 'ऐ' to "ᱮ", 'ओ' to "ᱚ", 'औ' to "ᱚ",
        'क' to "ᱠ", 'ख' to "ᱠᱷ", 'ग' to "ᱜ", 'घ' to "ᱜᱷ", 'ङ' to "ᱝ",
        'च' to "ᱪ", 'छ' to "ᱪᱷ", 'ज' to "ᱡ", 'झ' to "ᱡᱷ", 'ञ' to "ᱧ",
        'ट' to "ᱴ", 'ठ' to "ᱴᱷ", 'ड' to "ᱰ", 'ढ' to "ᱰᱷ", 'ण' to "ᱬ",
        'त' to "ᱛ", 'थ' to "ᱛᱷ", 'द' to "ᱫ", 'ध' to "ᱫᱷ", 'न' to "ᱱ",
        'प' to "ᱯ", 'फ' to "ᱯᱷ", 'ब' to "ᱵ", 'भ' to "ᱵᱷ", 'म' to "ᱢ",
        'य' to "ᱭ", 'र' to "ᱨ", 'ल' to "ᱞ", 'व' to "ᱣ",
        'श' to "ᱥ", 'ष' to "ᱥ", 'स' to "ᱥ", 'ह' to "ᱦ",
        'ड़' to "ᱲ", 'ढ़' to "ᱲᱷ",
        'ा' to "ᱟ", 'ि' to "ᱤ", 'ी' to "ᱤ", 'ु' to "ᱩ", 'ू' to "ᱩ",
        'े' to "ᱮ", 'ै' to "ᱮ", 'ो' to "ᱚ", 'ौ' to "ᱚ", 'ं' to "ᱸ"
    )

    /**
     * Checks if a string contains any Ol Chiki Unicode characters (U+1C50 to U+1C7F)
     */
    fun isOlChiki(text: String): Boolean {
        return text.any { it.code in 0x1C50..0x1C7F }
    }

    /**
     * Transliterates Latin or Devanagari text to Ol Chiki script.
     */
    fun toOlChiki(text: String): String {
        if (isOlChiki(text)) return text
        val trimmed = text.trim()
        val normalized = trimmed.lowercase()

        // 1. Check direct phrase cache
        phraseLatinToOlChiki[normalized]?.let { return it }
        phraseDevanagariToOlChiki[trimmed]?.let { return it }

        // Strip surrounding punctuation for lookup
        val cleanWord = normalized.replace(Regex("[?.!,]"), "").trim()
        phraseLatinToOlChiki[cleanWord]?.let {
            val suffix = if (normalized.endsWith("?")) "?" else if (normalized.endsWith("!")) "!" else ""
            return it + suffix
        }
        phraseDevanagariToOlChiki[cleanWord]?.let {
            val suffix = if (trimmed.endsWith("?")) "?" else if (trimmed.endsWith("!")) "!" else ""
            return it + suffix
        }

        // 2. Character / digraph transliteration pass
        val result = StringBuilder()
        var i = 0
        while (i < trimmed.length) {
            val char = trimmed[i]

            // Check Ol Chiki digits
            if (digitToOlChiki.containsKey(char)) {
                result.append(digitToOlChiki[char])
                i++
                continue
            }

            // Check Devanagari single character / matra
            if (devanagariSingleToOlChiki.containsKey(char)) {
                result.append(devanagariSingleToOlChiki[char])
                i++
                continue
            }

            // Check multi-character Latin digraphs
            var matchedDigraph = false
            for ((digraph, olChikiReplacement) in latinDigraphsToOlChiki) {
                if (trimmed.regionMatches(i, digraph, 0, digraph.length, ignoreCase = true)) {
                    result.append(olChikiReplacement)
                    i += digraph.length
                    matchedDigraph = true
                    break
                }
            }
            if (matchedDigraph) continue

            // Check single character
            val lowerChar = char.lowercaseChar()
            if (latinSingleToOlChiki.containsKey(lowerChar)) {
                result.append(latinSingleToOlChiki[lowerChar])
            } else {
                // Retain space, punctuation, or already existing glyph
                result.append(char)
            }
            i++
        }
        return result.toString()
    }

    /**
     * Converts Ol Chiki to Latin phonetics (e.g., ᱡᱚᱦᱟᱨ -> Johar).
     */
    fun toLatinPhonetic(olChikiText: String): String {
        if (!isOlChiki(olChikiText)) return olChikiText

        // Check reverse phrase matches
        for ((latin, olChiki) in phraseLatinToOlChiki) {
            if (olChikiText.trim() == olChiki) {
                return latin.replaceFirstChar { it.uppercase() }
            }
        }

        val sb = StringBuilder()
        for (ch in olChikiText) {
            if (olChikiToLatin.containsKey(ch)) {
                sb.append(olChikiToLatin[ch])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString().trim().replaceFirstChar { it.uppercase() }
    }

    /**
     * Converts Ol Chiki or Latin text to Phonetic Devanagari guide for the Teacher HUD.
     * e.g., ᱡᱚᱦᱟᱨ -> "जोहार (Johar)"
     */
    fun toTeacherPhoneticHUD(text: String): String {
        if (text.isBlank()) return ""

        // If it's already in Devanagari
        val hasDevanagari = text.any { it.code in 0x0900..0x097F }
        if (hasDevanagari && !isOlChiki(text)) {
            return text
        }

        val olChiki = if (isOlChiki(text)) text else toOlChiki(text)
        val latin = toLatinPhonetic(olChiki)

        val devaBuilder = StringBuilder()
        var i = 0
        while (i < olChiki.length) {
            val ch = olChiki[i]
            if (olChikiToDevanagari.containsKey(ch)) {
                devaBuilder.append(olChikiToDevanagari[ch])
            } else {
                devaBuilder.append(ch)
            }
            i++
        }

        val devaGuide = devaBuilder.toString().trim()
        return if (latin.isNotBlank() && !latin.equals(devaGuide, ignoreCase = true)) {
            "$devaGuide ($latin)"
        } else {
            devaGuide
        }
    }
}
