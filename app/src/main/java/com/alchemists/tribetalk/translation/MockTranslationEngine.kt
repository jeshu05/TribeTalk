package com.alchemists.tribetalk.translation

class MockTranslationEngine : TranslationEngine {

    private val hindiToSantali = mapOf(
        "नमस्ते" to "Johar (जोहार)",
        "हैलो" to "Johar (जोहार)",
        "आप कैसे हैं?" to "Ceka menama? (चेका मेनामा?)",
        "आप कैसे हैं" to "Ceka menama? (चेका मेनामा?)",
        "चलो पढ़ते हैं" to "Dela bon paṛhao-a (देला बोन पढ़ाओ-आ)",
        "चलो पढ़ते हैं" to "Dela bon paṛhao-a (देला बोन पढ़ाओ-आ)",
        "किताब खोलो" to "Puthī jhije me (पुथी झिजे मे)",
        "एक दो तीन चार" to "Mit’ bar pe pon (मित’ बार पे पोन)",
        "लिखना शुरू करो" to "Ol eho b me (ओल एहो ब मे)",
        "मुझे समझ नहीं आया" to "Bañ bujhau daṛeada (बाँ बुझाउ दड़ेआदा)",
        "क्या आप मदद कर सकते हैं?" to "Cet’ aam goṛo daṛeaña? (चेत’ आम गोड़ो दड़ेआङा?)",
        "क्या आप मदद कर सकते हैं" to "Cet’ aam goṛo daṛeaña? (चेत’ आम गोड़ो दड़ेआङा?)"
    )

    private val santaliToHindi = mapOf(
        "johar" to "नमस्ते (Namaste)",
        "जोहार" to "नमस्ते (Namaste)",
        "ceka menama?" to "आप कैसे हैं? (Aap kaise hain?)",
        "ceka menama" to "आप कैसे हैं? (Aap kaise hain?)",
        "चेका मेनामा" to "आप कैसे हैं? (Aap kaise hain?)",
        "चेका मेनामा?" to "आप कैसे हैं? (Aap kaise hain?)",
        "dela bon paṛhao-a" to "चलो पढ़ते हैं (Chalo padhte hain)",
        "देला बोन पढ़ाओ-आ" to "चलो पढ़ते हैं (Chalo padhte hain)",
        "puthī jhije me" to "किताब खोलो (Kitab kholo)",
        "पुथी झिजे मे" to "किताब खोलो (Kitab kholo)",
        "mit’ bar pe pon" to "एक दो तीन चार (Ek do teen chaar)",
        "मित’ बार पे पोन" to "एक दो तीन चार (Ek do teen chaar)",
        "ol eho b me" to "लिखना शुरू करो (Likhna shuru karo)",
        "ओल एहो ब मे" to "लिखना शुरू करो (Likhna shuru karo)",
        "bañ bujhau daṛeada" to "मुझे समझ नहीं आया (Mujhe samajh nahi aaya)",
        "बाँ बुझाउ दड़ेआदा" to "मुझे समझ नहीं आया (Mujhe samajh nahi aaya)",
        "cet’ aam goṛo daṛeaña?" to "क्या आप मदद कर सकते हैं? (Kya aap madad kar sakte hain?)",
        "cet’ aam goṛo daṛeaña" to "क्या आप मदद कर सकते हैं? (Kya aap madad kar sakte hain?)",
        "चेत’ आम गोड़ो दड़ेआङा?" to "क्या आप मदद कर सकते हैं? (Kya aap madad kar sakte hain?)",
        "चेत’ आम गोड़ो दड़ेआङा" to "क्या आप मदद कर सकते हैं? (Kya aap madad kar sakte hain?)"
    )

    override fun translate(text: String, source: Language, target: Language): String {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty()) return ""

        if (source == target) return trimmedText

        val normalizedText = trimmedText.lowercase().replace(Regex("[?.!,]"), "").trim()

        return if (source == Language.HINDI && target == Language.SANTALI) {
            hindiToSantali[trimmedText] 
                ?: hindiToSantali[normalizedText]
                ?: hindiToSantali.entries.firstOrNull { it.key.lowercase().replace(Regex("[?.!,]"), "").trim() == normalizedText }?.value
                ?: "[Demo Translation: Placeholder for '$trimmedText']"
        } else if (source == Language.SANTALI && target == Language.HINDI) {
            santaliToHindi[trimmedText]
                ?: santaliToHindi[normalizedText]
                ?: santaliToHindi.entries.firstOrNull { it.key.lowercase().replace(Regex("[?.!,]"), "").trim() == normalizedText }?.value
                ?: "[Demo Translation: Placeholder for '$trimmedText']"
        } else {
            "[Unsupported Translation]"
        }
    }
}
