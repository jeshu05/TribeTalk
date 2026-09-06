package com.alchemists.tribetalk.flashcards

/**
 * NIPUN Bharat / FLN Learning Domain descriptor.
 */
data class LearningDomain(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val flnGoal: String,
    val skills: List<String>
)

/**
 * Vocabulary template item for quick set generation from existing validated curriculum.
 */
data class PresetCardTemplate(
    val hindi: String,
    val santaliLatin: String,
    val santaliOlChiki: String,
    val phoneticDevanagari: String,
    val emoji: String,
    val exampleSentenceHindi: String,
    val exampleSentenceSantali: String
)

/**
 * Curated NIPUN Bharat / FLN Learning Outcomes and Topics framework.
 *
 * Grounded in the project pedagogy architecture:
 * 1. Health & Well-being (हमारा परिवेश एवं स्वास्थ्य)
 * 2. Effective Communicators (बुनियादी साक्षरता एवं मौखिक भाषा)
 * 3. Involved Learners (प्रारंभिक संख्या ज्ञान एवं गणित)
 */
object NIPUNLearningFramework {

    val domains: List<LearningDomain> = listOf(
        LearningDomain(
            id = "literacy_oral_language",
            title = "Foundational Literacy & Oral Language",
            hindiTitle = "मौखिक भाषा एवं बुनियादी साक्षरता",
            flnGoal = "FLN Goal 2 (Effective Communicators)",
            skills = listOf(
                "Oral Vocabulary & Word Meaning (मौखिक शब्दावली)",
                "Word-Picture Association (शब्द-चित्र संबंध)",
                "Phonological & Ol Chiki Script Awareness (ध्वनि एवं लिपि समझ)",
                "Classroom Interaction & Dialogue (कक्षा संवाद एवं निर्देश)"
            )
        ),
        LearningDomain(
            id = "numeracy_mathematics",
            title = "Foundational Numeracy & Mathematical Thinking",
            hindiTitle = "प्रारंभिक गणित एवं संख्या ज्ञान",
            flnGoal = "FLN Goal 3 (Involved Learners)",
            skills = listOf(
                "Number Recognition & Counting (संख्या पहचान एवं गिनती)",
                "Quantities & Shapes (मात्रा एवं आकार समझ)",
                "Mathematical Vocabulary (गणितीय शब्दावली)"
            )
        ),
        LearningDomain(
            id = "environmental_social",
            title = "Physical & Environmental Context",
            hindiTitle = "हमारा परिवेश एवं प्रकृति",
            flnGoal = "FLN Goal 1 (Health & Well-being)",
            skills = listOf(
                "Animals & Living Beings (पशु-पक्षी एवं जीव)",
                "Nature & Surroundings (प्रकृति एवं पर्यावरण)",
                "Daily Expressions & Needs (दैनिक आवश्यकताएं)"
            )
        )
    )

    val presetTopicTemplates: Map<String, List<PresetCardTemplate>> = mapOf(
        "Animals (पशु-पक्षी)" to listOf(
            PresetCardTemplate("गाय", "Gại", "ᱜᱟᱹᱭ", "गाई", "🐄", "यह गाय है।", "ᱱᱚᱶᱟ ᱫᱚ ᱜᱟᱹᱭ ᱠᱟᱱᱟᱭ।"),
            PresetCardTemplate("कुत्ता", "Seta", "ᱥᱮᱛᱟ", "सेता", "🐕", "कुत्ता वफादार होता है।", "ᱥᱮᱛᱟ ᱫᱚ ᱵᱤᱥᱣᱟᱥᱤ ᱠᱟᱱᱟᱭ।"),
            PresetCardTemplate("मछली", "Haku", "ᱦᱟᱹᱠᱩ", "हाकू", "🐟", "मछली पानी में तैरती है।", "ᱦᱟᱹᱠᱩ ᱫᱟᱜ ᱨᱮ ᱯᱟᱭᱨᱟᱜ-ᱟ।"),
            PresetCardTemplate("चिड़िया", "Ceṛẽ", "ᱪᱮᱬᱮ", "चेणे", "🐦", "चिड़िया आकाश में उड़ती है।", "ᱪᱮᱬᱮ ᱥᱮᱨᱢᱟ ᱨᱮ ᱩᱰᱟᱹᱣᱜ-ᱟ।"),
            PresetCardTemplate("बाघ", "Tạrub", "ᱛᱟᱹᱨᱩᱵ", "तारुब", "🐅", "बाघ जंगल में रहता है।", "ᱛᱟᱹᱨᱩᱵ ᱵᱤᱨ ᱨᱮ ᱛᱟᱦᱮᱸᱱᱟᱭ।")
        ),
        "Classroom (कक्षा)" to listOf(
            PresetCardTemplate("किताब", "Puthī", "ᱯᱩᱛᱷᱤ", "पुथी", "📖", "अपनी किताब खोलो।", "ᱟᱢᱟᱜ ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ।"),
            PresetCardTemplate("कलम", "Kalam", "ᱠᱚᱞᱚᱢ", "कोलम", "✏️", "कलम से लिखो।", "ᱠᱚᱞᱚᱢ ᱛᱮ ᱚᱞ ᱢᱮ।"),
            PresetCardTemplate("स्कूल", "Itun oṛak'", "ᱤᱛᱩᱱ ᱚᱲᱟᱜ", "इतुन ओड़ाक", "🏫", "हम स्कूल जाते हैं।", "ᱟᱞᱮ ᱤᱛᱩᱱ ᱚᱲᱟᱜ ᱪᱟᱞᱟᱜ-ᱟᱞᱮ।"),
            PresetCardTemplate("शिक्षक", "Mācet'", "ᱢᱟᱪᱮᱛ", "माचेत", "👨‍🏫", "शिक्षक पढ़ाते हैं।", "ᱢᱟᱪᱮᱛ ᱯᱟᱲᱦᱟᱣᱮᱫ-ᱟᱭ।"),
            PresetCardTemplate("छात्र", "Pạṛhuạ", "ᱯᱟᱹᱲᱦᱩᱣᱟᱹ", "पाड़हुवा", "🧑‍🎓", "छात्र सीखते हैं।", "ᱯᱟᱹᱲᱦᱩᱣᱟᱹ ᱠᱚ ᱪᱮᱫᱚᱜ-ᱟ।")
        ),
        "Nature (प्रकृति एवं परिवेश)" to listOf(
            PresetCardTemplate("पेड़", "Dare", "ᱫᱟᱨᱮ", "दारे", "🌳", "पेड़ फल देता है।", "ᱫᱟᱨᱮ ᱡᱚ ᱮᱢᱚᱜ-ᱟ।"),
            PresetCardTemplate("पानी", "Dak'", "ᱫᱟᱜ", "दाक", "💧", "पानी पियो।", "ᱫᱟᱜ ᱧᱩᱭ ᱢᱮ।"),
            PresetCardTemplate("सूरज", "Siṅgi", "ᱥᱤᱸᱜᱤ", "सिंगी", "☀️", "सूरज चमकता है।", "ᱥᱤᱸᱜᱤ ᱡᱩᱞᱩᱜ ᱠᱟᱱᱟᱭ।"),
            PresetCardTemplate("चाँद", "Cāndo", "ᱪᱟᱸᱫᱚ", "चांदो", "🌙", "चाँद रात में दिखता है।", "ᱪᱟᱸᱫᱚ ᱧᱤᱫᱟᱹ ᱧᱮᱞᱚᱜ-ᱟᱭ।"),
            PresetCardTemplate("घर", "Oṛak'", "ᱚᱲᱟᱜ", "ओड़ाक", "🏠", "यह मेरा घर है।", "ᱱᱚᱶᱟ ᱫᱚ ᱤᱧᱟᱜ ᱚᱲᱟᱜ ᱠᱟᱱᱟ।")
        ),
        "Numbers (संख्या ज्ञान)" to listOf(
            PresetCardTemplate("एक", "Mit'", "ᱢᱤᱫ", "मित", "1️⃣", "एक सेब।", "ᱢᱤᱫᱴᱟᱝ ᱥᱮᱣ।"),
            PresetCardTemplate("दो", "Bār", "ᱵᱟᱨ", "बार", "2️⃣", "दो आँखें।", "ᱵᱟᱨᱭᱟ ᱢᱮᱫ।"),
            PresetCardTemplate("तीन", "Pɛ", "ᱯᱮ", "पे", "3️⃣", "तीन पत्ते।", "ᱯᱮᱭᱟ ᱥᱟᱠᱟᱢ।"),
            PresetCardTemplate("चार", "Pōn", "ᱯᱩᱱ", "पुन", "4️⃣", "चार पैर।", "ᱯᱩᱱᱭᱟ ᱡᱟᱝᱜᱟ।"),
            PresetCardTemplate("पांच", "Mōṛẽ", "ᱢᱚᱬᱮ", "मोणे", "5️⃣", "पांच उंगलियां।", "ᱢᱚᱬᱮ ᱜᱚᱴᱟᱝ ᱠᱟᱹᱴᱩᱵ।")
        )
    )
}
