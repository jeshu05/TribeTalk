package org.tribetalk.fln.repository

import org.tribetalk.fln.model.*

/**
 * Curated, verified repository of NIPUN Bharat Foundational Literacy and Numeracy (FLN) cards.
 * 130+ structured cards covering all 30 Ol Chiki letters, Numbers 1 to 20,
 * and high-frequency tribal village vocabulary with 100% vector SVG illustrations.
 * Zero emojis. Authentically bilingual (Santali Ol Chiki + Devanagari Hindi).
 */
object FlnCurriculumRepository {

    // -------------------------------------------------------------------------
    // 1. All 30 Ol Chiki Alphabet Letters (Literacy)
    // -------------------------------------------------------------------------
    private val aksharCards = listOf(
        FlnCard(
            id = "ak_01",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱚ",
            hindiText = "अ (ओ)",
            englishGloss = "Letter O (First Vowel)",
            teacherPhoneticGuide = "OH (Puy-lu Raha Arang)",
            imageAssetPath = "fln_svg_corpus/nature/earth.svg",
            vectorIconType = "earth",
            exemplarWordSantali = "ᱚᱛ",
            exemplarWordHindi = "ओत् (धरती)",
            exemplarPhonetic = "OT",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Trace left oval curve downward, connect to top right line.",
            exampleSentenceSantali = "ᱱᱚᱣᱟ ᱫᱚ ᱟᱞᱮᱭᱟᱜ ᱚᱛ ᱠᱟᱱᱟ᱾",
            exampleSentenceHindi = "यह हमारी धरती है।",
            phonicsClassification = "Puy-lu Raha Arang (First Vowel)"
        ),
        FlnCard(
            id = "ak_02",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱛ",
            hindiText = "त (अत्)",
            englishGloss = "Letter T",
            teacherPhoneticGuide = "AT (Taras Chiki)",
            imageAssetPath = "fln_svg_corpus/animals/tiger.svg",
            vectorIconType = "tiger",
            exemplarWordSantali = "ᱛᱟᱹᱨᱩᱵ",
            exemplarWordHindi = "तारुब (बाघ)",
            exemplarPhonetic = "TAA-RUB",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Draw top horizontal line, then curve down like a hook.",
            exampleSentenceSantali = "ᱛᱟᱹᱨᱩᱵ ᱵᱤᱨ ᱨᱮ ᱢᱮᱱᱟᱭᱟ᱾",
            exampleSentenceHindi = "बाघ जंगल में रहता है।",
            phonicsClassification = "Taras Chiki (Initial Consonant)"
        ),
        FlnCard(
            id = "ak_03",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱜ",
            hindiText = "ग (अग्)",
            englishGloss = "Letter G",
            teacherPhoneticGuide = "AG (Tapug Chiki)",
            imageAssetPath = "fln_svg_corpus/animals/cow.svg",
            vectorIconType = "cow",
            exemplarWordSantali = "ᱜᱟᱹᱭ",
            exemplarWordHindi = "गाई (गाय)",
            exemplarPhonetic = "GAA-I",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Loop circle on top, draw vertical stem down.",
            exampleSentenceSantali = "ᱜᱟᱹᱭ ᱛᱚᱣᱟᱭ ᱮᱢᱚᱜᱼᱟ᱾",
            exampleSentenceHindi = "गाय दूध देती है।",
            phonicsClassification = "Tapug Chiki (Checked Consonant)"
        ),
        FlnCard(
            id = "ak_04",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱝ",
            hindiText = "ङ (अं)",
            englishGloss = "Letter Ng",
            teacherPhoneticGuide = "ANG (Rarang Chiki)",
            imageAssetPath = "fln_svg_corpus/animals/bull.svg",
            vectorIconType = "bull",
            exemplarWordSantali = "ᱰᱟᱝᱜᱽᱨᱟ",
            exemplarWordHindi = "डांगरा (बैल)",
            exemplarPhonetic = "DAANG-RAA",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Small upper loop with a straight vertical tail.",
            exampleSentenceSantali = "ᱰᱟᱝᱜᱽᱨᱟ ᱠᱷᱮᱛ ᱮ ᱥᱤᱭᱟ᱾",
            exampleSentenceHindi = "बैल खेत जोतता है।",
            phonicsClassification = "Rarang Chiki (Nasal Consonant)"
        ),
        FlnCard(
            id = "ak_05",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱞ",
            hindiText = "ल (अल्)",
            englishGloss = "Letter L",
            teacherPhoneticGuide = "AL (Larang Chiki)",
            imageAssetPath = "fln_svg_corpus/plants/sal_leaf.svg",
            vectorIconType = "sal_leaf",
            exemplarWordSantali = "ᱥᱟᱨᱡᱚᱢ ᱥᱟᱠᱟᱢ",
            exemplarWordHindi = "साल का पत्ता",
            exemplarPhonetic = "SAAR-JOM SAA-KAAM",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Smooth wave curve from bottom left to top right.",
            exampleSentenceSantali = "ᱥᱟᱨᱡᱚᱢ ᱥᱟᱠᱟᱢ ᱦᱟᱹᱨᱭᱟᱹᱲ ᱜᱮᱭᱟ᱾",
            exampleSentenceHindi = "साल का पत्ता हरा होता है।",
            phonicsClassification = "Larang Chiki (Liquid Consonant)"
        ),
        FlnCard(
            id = "ak_06",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱟ",
            hindiText = "आ (आ)",
            englishGloss = "Letter AA (Second Vowel)",
            teacherPhoneticGuide = "AA (Dosar Raha)",
            imageAssetPath = "fln_svg_corpus/village_life/bow_arrow.svg",
            vectorIconType = "bow_arrow",
            exemplarWordSantali = "ᱟᱜ",
            exemplarWordHindi = "आग (धनुष)",
            exemplarPhonetic = "AAG",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Draw circle on left, extend horizontal bridge to vertical line.",
            exampleSentenceSantali = "ᱟᱜ ᱛᱮ ᱥᱟᱨ ᱛᱩᱧ ᱢᱮ᱾",
            exampleSentenceHindi = "धनुष से तीर चलाओ।",
            phonicsClassification = "Dosar Raha (Second Vowel)"
        ),
        FlnCard(
            id = "ak_07",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱠ",
            hindiText = "क (आक्)",
            englishGloss = "Letter K",
            teacherPhoneticGuide = "AAK (Taras Chiki)",
            imageAssetPath = "fln_svg_corpus/plants/banana.svg",
            vectorIconType = "banana",
            exemplarWordSantali = "ᱠᱟᱭᱨᱟ",
            exemplarWordHindi = "कायरा (केला)",
            exemplarPhonetic = "KAI-RAA",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Vertical back spine, add two forward wings.",
            exampleSentenceSantali = "ᱵᱤᱞᱤ ᱠᱟᱭᱨᱟ ᱦᱮᱲᱮᱢᱟ᱾",
            exampleSentenceHindi = "पका केला मीठा होता है।",
            phonicsClassification = "Taras Chiki (Consonant)"
        ),
        FlnCard(
            id = "ak_08",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱡ",
            hindiText = "ज (आज्)",
            englishGloss = "Letter J",
            teacherPhoneticGuide = "AAJ (Tapug Chiki)",
            imageAssetPath = "fln_svg_corpus/animals/deer.svg",
            vectorIconType = "deer",
            exemplarWordSantali = "ᱡᱤᱞ",
            exemplarWordHindi = "जिल (हिरण)",
            exemplarPhonetic = "JEEL",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Horizontal base line, gentle upward slope to loop.",
            exampleSentenceSantali = "ᱡᱤᱞ ᱵᱤᱨ ᱨᱮ ᱫᱟᱹᱲᱟᱭ᱾",
            exampleSentenceHindi = "हिरण जंगल में दौड़ता है।",
            phonicsClassification = "Tapug Chiki (Checked Consonant)"
        ),
        FlnCard(
            id = "ak_09",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱢ",
            hindiText = "म (आम्)",
            englishGloss = "Letter M",
            teacherPhoneticGuide = "AAM (Rarang Chiki)",
            imageAssetPath = "fln_svg_corpus/animals/goat.svg",
            vectorIconType = "goat",
            exemplarWordSantali = "ᱢᱮᱨᱚᱢ",
            exemplarWordHindi = "मेरम (बकरी)",
            exemplarPhonetic = "ME-ROM",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Top bar, diagonal slant down and back up.",
            exampleSentenceSantali = "ᱢᱮᱨᱚᱢ ᱜᱷᱟᱸᱥ ᱮ ᱡᱚᱢᱟ᱾",
            exampleSentenceHindi = "बकरी घास खाती है।",
            phonicsClassification = "Rarang Chiki (Nasal Consonant)"
        ),
        FlnCard(
            id = "ak_10",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱣ",
            hindiText = "व (आव्)",
            englishGloss = "Letter W",
            teacherPhoneticGuide = "AAW (Larang Chiki)",
            imageAssetPath = "fln_svg_corpus/village_life/bow_arrow.svg",
            vectorIconType = "bow_arrow",
            exemplarWordSantali = "ᱣᱟᱜ",
            exemplarWordHindi = "वाग (धनुष/तीर)",
            exemplarPhonetic = "WAAG",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Curved arc like an archer's bow.",
            exampleSentenceSantali = "ᱥᱟᱨ ᱟᱨ ᱣᱟᱜ ᱛᱤ ᱛᱮ ᱥᱟᱵ ᱢᱮ᱾",
            exampleSentenceHindi = "तीर और धनुष हाथ में पकड़ो।",
            phonicsClassification = "Larang Chiki (Semi-vowel)"
        ),
        FlnCard(
            id = "ak_11",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱤ",
            hindiText = "इ (इ)",
            englishGloss = "Letter I (Third Vowel)",
            teacherPhoneticGuide = "EE (Tesar Raha)",
            imageAssetPath = "fln_svg_corpus/tokens/star.svg",
            vectorIconType = "star",
            exemplarWordSantali = "ᱤᱯᱤᱞ",
            exemplarWordHindi = "इपिल (तारा)",
            exemplarPhonetic = "EE-PIL",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Small needle-like hook bending downward.",
            exampleSentenceSantali = "ᱧᱤᱫᱟᱹ ᱤᱯᱤᱞ ᱡᱩᱞᱩᱜᱼᱟ᱾",
            exampleSentenceHindi = "रात में तारा चमकता है।",
            phonicsClassification = "Tesar Raha (Third Vowel)"
        ),
        FlnCard(
            id = "ak_12",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱥ",
            hindiText = "स (इस्)",
            englishGloss = "Letter S",
            teacherPhoneticGuide = "EES (Taras Chiki)",
            imageAssetPath = "fln_svg_corpus/animals/dog.svg",
            vectorIconType = "dog",
            exemplarWordSantali = "ᱥᱮᱛᱟ",
            exemplarWordHindi = "सेता (कुत्ता)",
            exemplarPhonetic = "SE-TAA",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "S-curve with balanced upper and lower bellies.",
            exampleSentenceSantali = "ᱥᱮᱛᱟ ᱚᱲᱟᱜ ᱮ ᱨᱩᱠᱷᱤᱭᱟᱹᱭᱟ᱾",
            exampleSentenceHindi = "कुत्ता घर की रखवाली करता है।",
            phonicsClassification = "Taras Chiki (Consonant)"
        ),
        FlnCard(
            id = "ak_13",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱦ",
            hindiText = "ह (इह्)",
            englishGloss = "Letter H",
            teacherPhoneticGuide = "EEH (Tapug Chiki)",
            imageAssetPath = "fln_svg_corpus/animals/elephant.svg",
            vectorIconType = "elephant",
            exemplarWordSantali = "ᱦᱟᱹᱛᱤ",
            exemplarWordHindi = "हाती (हाथी)",
            exemplarPhonetic = "HAA-TI",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Arch over the top and curve down right.",
            exampleSentenceSantali = "ᱦᱟᱹᱛᱤ ᱢᱟᱨᱟᱝ ᱡᱤᱭᱟᱹᱞᱤ ᱠᱟᱱᱟᱭ᱾",
            exampleSentenceHindi = "हाथी बड़ा जानवर है।",
            phonicsClassification = "Tapug Chiki (Aspirate Consonant)"
        ),
        FlnCard(
            id = "ak_14",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱧ",
            hindiText = "ञ (इञ्)",
            englishGloss = "Letter Ny",
            teacherPhoneticGuide = "EENY (Rarang Chiki)",
            imageAssetPath = "fln_svg_corpus/nature/moon.svg",
            vectorIconType = "moon",
            exemplarWordSantali = "ᱧᱤᱫᱟᱹ",
            exemplarWordHindi = "ञिदा (रात/चाँद)",
            exemplarPhonetic = "NYI-DAA",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Loop inward like an earring hook.",
            exampleSentenceSantali = "ᱧᱤᱫᱟᱹ ᱪᱟᱸᱫᱚ ᱢᱟᱨᱥᱟᱞ ᱮ ᱮᱢᱟ᱾",
            exampleSentenceHindi = "रात में चाँद रोशनी देता है।",
            phonicsClassification = "Rarang Chiki (Palatal Nasal)"
        ),
        FlnCard(
            id = "ak_15",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱨ",
            hindiText = "र (इर्)",
            englishGloss = "Letter R",
            teacherPhoneticGuide = "EER (Larang Chiki)",
            imageAssetPath = "fln_svg_corpus/nature/cloud.svg",
            vectorIconType = "cloud",
            exemplarWordSantali = "ᱨᱤᱢᱤᱞ",
            exemplarWordHindi = "रिमिल (बादल)",
            exemplarPhonetic = "RI-MIL",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Gentle wave from top to bottom.",
            exampleSentenceSantali = "ᱥᱮᱨᱢᱟ ᱨᱮ ᱦᱮᱸᱫᱮ ᱨᱤᱢᱤᱞ ᱢᱮᱱᱟᱜᱼᱟ᱾",
            exampleSentenceHindi = "आसमान में काले बादल हैं।",
            phonicsClassification = "Larang Chiki (Flap Consonant)"
        ),
        FlnCard(
            id = "ak_16",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱩ",
            hindiText = "उ (उ)",
            englishGloss = "Letter U (Fourth Vowel)",
            teacherPhoneticGuide = "OO (Pun-a Raha)",
            imageAssetPath = "fln_svg_corpus/plants/mango.svg",
            vectorIconType = "mango",
            exemplarWordSantali = "ᱩᱞ",
            exemplarWordHindi = "उल (आम)",
            exemplarPhonetic = "OOL",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Deep cup curving downward and sweeping back up.",
            exampleSentenceSantali = "ᱵᱤᱞᱤ ᱩᱞ ᱟᱹᱰᱤ ᱥᱤᱵᱤᱞᱟ᱾",
            exampleSentenceHindi = "पका आम बहुत मीठा होता है।",
            phonicsClassification = "Pun-a Raha (Fourth Vowel)"
        ),
        FlnCard(
            id = "ak_17",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱪ",
            hindiText = "च (उच्)",
            englishGloss = "Letter Ch",
            teacherPhoneticGuide = "UCH (Taras Chiki)",
            imageAssetPath = "fln_svg_corpus/animals/bird.svg",
            vectorIconType = "bird",
            exemplarWordSantali = "ᱪᱮᱬᱮ",
            exemplarWordHindi = "चेणे (चिड़िया)",
            exemplarPhonetic = "CHE-NE",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Curved beak opening to the right.",
            exampleSentenceSantali = "ᱪᱮᱬᱮ ᱩᱰᱟᱹᱣᱜ ᱠᱟᱱᱟᱭ᱾",
            exampleSentenceHindi = "चिड़िया उड़ रही है।",
            phonicsClassification = "Taras Chiki (Consonant)"
        ),
        FlnCard(
            id = "ak_18",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱫ",
            hindiText = "द (उद्)",
            englishGloss = "Letter D",
            teacherPhoneticGuide = "UD (Tapug Chiki)",
            imageAssetPath = "fln_svg_corpus/plants/tree.svg",
            vectorIconType = "tree",
            exemplarWordSantali = "ᱫᱟᱨᱮ",
            exemplarWordHindi = "दारे (पेड़)",
            exemplarPhonetic = "DAA-RE",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Vertical stem with sturdy right loop.",
            exampleSentenceSantali = "ᱫᱟᱨᱮ ᱨᱮ ᱩᱢᱩᱞ ᱢᱮᱱᱟᱜᱼᱟ᱾",
            exampleSentenceHindi = "पेड़ की छाँव होती है।",
            phonicsClassification = "Tapug Chiki (Checked Dental)"
        ),
        FlnCard(
            id = "ak_19",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱬ",
            hindiText = "ण (उण्)",
            englishGloss = "Letter Nn (Retroflex)",
            teacherPhoneticGuide = "UNN (Rarang Chiki)",
            imageAssetPath = "fln_svg_corpus/village_life/flute.svg",
            vectorIconType = "flute",
            exemplarWordSantali = "ᱛᱤᱨᱤᱭᱟᱹᱣ",
            exemplarWordHindi = "तिरियौ (बांसुरी)",
            exemplarPhonetic = "TI-RI-YAW",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Upper wave with deep retroflex curve.",
            exampleSentenceSantali = "ᱛᱤᱨᱤᱭᱟᱹᱣ ᱨᱟᱦᱟ ᱟᱹᱰᱤ ᱱᱟᱯᱟᱭᱟ᱾",
            exampleSentenceHindi = "बांसुरी की धुन बहुत प्यारी है।",
            phonicsClassification = "Rarang Chiki (Retroflex Nasal)"
        ),
        FlnCard(
            id = "ak_20",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱭ",
            hindiText = "य (उय्)",
            englishGloss = "Letter Y",
            teacherPhoneticGuide = "UY (Larang Chiki)",
            imageAssetPath = "fln_svg_corpus/tokens/flower.svg",
            vectorIconType = "flower",
            exemplarWordSantali = "ᱜᱟᱛᱮ",
            exemplarWordHindi = "गाते (मित्र/दोस्त)",
            exemplarPhonetic = "GAA-TE",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Two flowing branches joining into one stem.",
            exampleSentenceSantali = "ᱜᱟᱛᱮ ᱥᱟᱶ ᱠᱷᱮᱞ ᱢᱮ᱾",
            exampleSentenceHindi = "दोस्त के साथ खेलो।",
            phonicsClassification = "Larang Chiki (Semi-vowel)"
        ),
        FlnCard(
            id = "ak_21",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱮ",
            hindiText = "ए (ए)",
            englishGloss = "Letter E (Fifth Vowel)",
            teacherPhoneticGuide = "EH (Mone-a Raha)",
            imageAssetPath = "fln_svg_corpus/animals/parrot.svg",
            vectorIconType = "parrot",
            exemplarWordSantali = "ᱮᱨᱮ",
            exemplarWordHindi = "एरे (तोता)",
            exemplarPhonetic = "E-RE",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Horizontal base with smooth ascending arch.",
            exampleSentenceSantali = "ᱮᱨᱮ ᱪᱮᱬᱮ ᱨᱚᱲᱟᱭ᱾",
            exampleSentenceHindi = "तोता बोलता है।",
            phonicsClassification = "Mone-a Raha (Fifth Vowel)"
        ),
        FlnCard(
            id = "ak_22",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱯ",
            hindiText = "प (एप्)",
            englishGloss = "Letter P",
            teacherPhoneticGuide = "EP (Taras Chiki)",
            imageAssetPath = "fln_svg_corpus/school_and_play/book.svg",
            vectorIconType = "book",
            exemplarWordSantali = "ᱯᱚᱛᱚᱵ",
            exemplarWordHindi = "पोतोब (किताब)",
            exemplarPhonetic = "PO-TOB",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Tall vertical stem with closed upper oval loop.",
            exampleSentenceSantali = "ᱯᱚᱛᱚᱵ ᱯᱟᱲᱦᱟᱣ ᱢᱮ᱾",
            exampleSentenceHindi = "किताब पढ़ो।",
            phonicsClassification = "Taras Chiki (Consonant)"
        ),
        FlnCard(
            id = "ak_23",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱰ",
            hindiText = "ड (एड्)",
            englishGloss = "Letter Dd (Retroflex)",
            teacherPhoneticGuide = "EDD (Tapug Chiki)",
            imageAssetPath = "fln_svg_corpus/animals/frog.svg",
            vectorIconType = "frog",
            exemplarWordSantali = "ᱰᱟᱹᱰᱤ",
            exemplarWordHindi = "डाडी (मेंढक/कुआँ)",
            exemplarPhonetic = "DAA-DI",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Top bar and double curve downward.",
            exampleSentenceSantali = "ᱫᱟᱜ ᱨᱮ ᱨᱚᱴᱮ ᱰᱚᱸᱠᱟᱭᱟ᱾",
            exampleSentenceHindi = "पानी में मेंढक कूदता है।",
            phonicsClassification = "Tapug Chiki (Retroflex Stop)"
        ),
        FlnCard(
            id = "ak_24",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱱ",
            hindiText = "न (एन्)",
            englishGloss = "Letter N",
            teacherPhoneticGuide = "EN (Rarang Chiki)",
            imageAssetPath = "fln_svg_corpus/nature/river.svg",
            vectorIconType = "river",
            exemplarWordSantali = "ᱱᱟᱹᱭ",
            exemplarWordHindi = "नई (नदी)",
            exemplarPhonetic = "NAA-EE",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Arch over and draw clean downward post.",
            exampleSentenceSantali = "ᱱᱟᱹᱭ ᱨᱮ ᱫᱟᱜ ᱞᱤᱸᱜᱤᱱ ᱠᱟᱱᱟ᱾",
            exampleSentenceHindi = "नदी में पानी बह रहा है।",
            phonicsClassification = "Rarang Chiki (Dental Nasal)"
        ),
        FlnCard(
            id = "ak_25",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱲ",
            hindiText = "ड़ (एढ़्)",
            englishGloss = "Letter Rr (Flap)",
            teacherPhoneticGuide = "ERR (Larang Chiki)",
            imageAssetPath = "fln_svg_corpus/animals/sheep.svg",
            vectorIconType = "sheep",
            exemplarWordSantali = "ᱵᱷᱮᱰᱟ",
            exemplarWordHindi = "भेड़ा (भेड़)",
            exemplarPhonetic = "BHE-DAA",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Curved loop with lower foot tick.",
            exampleSentenceSantali = "ᱵᱷᱮᱰᱟ ᱩᱵ ᱛᱮ ᱞᱩᱜᱽᱲᱤ ᱵᱮᱱᱟᱣᱜᱼᱟ᱾",
            exampleSentenceHindi = "भेड़ की ऊन से कपड़ा बनता है।",
            phonicsClassification = "Larang Chiki (Retroflex Flap)"
        ),
        FlnCard(
            id = "ak_26",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱳ",
            hindiText = "ओ (ओ)",
            englishGloss = "Letter O (Sixth Vowel)",
            teacherPhoneticGuide = "OH (Turui-a Raha)",
            imageAssetPath = "fln_svg_corpus/nature/sun.svg",
            vectorIconType = "sun",
            exemplarWordSantali = "ᱪᱟᱸᱫᱚ",
            exemplarWordHindi = "चांदो (सूरज)",
            exemplarPhonetic = "CHAAN-DO",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Round full circle like the rising sun.",
            exampleSentenceSantali = "ᱪᱟᱸᱫᱚᱭ ᱨᱟᱠᱟᱵ ᱮᱱᱟ᱾",
            exampleSentenceHindi = "सूरज उग आया।",
            phonicsClassification = "Turui-a Raha (Sixth Vowel)"
        ),
        FlnCard(
            id = "ak_27",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱴ",
            hindiText = "ट (ओट्)",
            englishGloss = "Letter Tt (Retroflex)",
            teacherPhoneticGuide = "OTT (Taras Chiki)",
            imageAssetPath = "fln_svg_corpus/village_life/clay_pot.svg",
            vectorIconType = "clay_pot",
            exemplarWordSantali = "ᱴᱩᱠᱩᱡ",
            exemplarWordHindi = "टुकुज (घड़ा/मटका)",
            exemplarPhonetic = "TU-KUJ",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Top horizontal bar with centered vertical stroke.",
            exampleSentenceSantali = "ᱴᱩᱠᱩᱡ ᱨᱮ ᱫᱟᱜ ᱫᱚᱦᱚᱭ ᱢᱮ᱾",
            exampleSentenceHindi = "मटके में पानी रखो।",
            phonicsClassification = "Taras Chiki (Retroflex Consonant)"
        ),
        FlnCard(
            id = "ak_28",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱵ",
            hindiText = "ब (ओब्)",
            englishGloss = "Letter B",
            teacherPhoneticGuide = "OB (Tapug Chiki)",
            imageAssetPath = "fln_svg_corpus/tokens/flower.svg",
            vectorIconType = "flower",
            exemplarWordSantali = "ᱵᱟᱦᱟ",
            exemplarWordHindi = "बाहा (फूल)",
            exemplarPhonetic = "BAA-HAA",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Vertical stem with two stacked curved loops.",
            exampleSentenceSantali = "ᱵᱟᱦᱟ ᱟᱹᱰᱤ ᱪᱮᱦᱨᱟ ᱜᱮᱭᱟ᱾",
            exampleSentenceHindi = "फूल बहुत सुंदर है।",
            phonicsClassification = "Tapug Chiki (Bilabial Consonant)"
        ),
        FlnCard(
            id = "ak_29",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱶ",
            hindiText = "ँ (ओंव्)",
            englishGloss = "Letter Vw (Nasal)",
            teacherPhoneticGuide = "OVW (Rarang Chiki)",
            imageAssetPath = "fln_svg_corpus/village_life/tumdak_drum.svg",
            vectorIconType = "tumdak_drum",
            exemplarWordSantali = "ᱛᱩᱢᱫᱟᱜ",
            exemplarWordHindi = "तुम्दाग (मांदर/ढोल)",
            exemplarPhonetic = "TUM-DAAG",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Curved pot mouth with lower rounded belly.",
            exampleSentenceSantali = "ᱛᱩᱢᱫᱟᱜ ᱨᱩᱭ ᱢᱮ ᱮᱱᱮᱡ ᱞᱟᱹᱜᱤᱫ᱾",
            exampleSentenceHindi = "नाचने के लिए मांदर बजाओ।",
            phonicsClassification = "Rarang Chiki (Nasal Vowel Glide)"
        ),
        FlnCard(
            id = "ak_30",
            domain = FlnDomain.LITERACY_ALPHABET,
            category = FlnCategory.AKSHAR,
            santaliOlChiki = "ᱷ",
            hindiText = "ह (ओह्)",
            englishGloss = "Letter H (Aspirate Modifier)",
            teacherPhoneticGuide = "OHH (Aspiration)",
            imageAssetPath = "fln_svg_corpus/animals/bear.svg",
            vectorIconType = "bear",
            exemplarWordSantali = "ᱵᱟᱱᱟ",
            exemplarWordHindi = "बाना (भालू)",
            exemplarPhonetic = "BAA-NAA",
            nipunCode = "L-BAL.1",
            fingerTracingGuide = "Small breathing plume rising upward.",
            exampleSentenceSantali = "ᱵᱟᱱᱟ ᱵᱤᱨ ᱨᱮ ᱢᱮᱱᱟᱭᱟ᱾",
            exampleSentenceHindi = "भालू जंगल में रहता है।",
            phonicsClassification = "Larang Chiki (Aspiration Sign)"
        )
    )

    // -------------------------------------------------------------------------
    // 2. Numbers 1 to 20 (Numeracy & Progressive Counting Primitives)
    // -------------------------------------------------------------------------
    fun toOlChikiDigits(num: Int): String {
        val olChikiDigits = charArrayOf('᱐', '᱑', '᱒', '᱓', '᱔', '᱕', '᱖', '᱗', '᱘', '᱙')
        val s = num.toString()
        val sb = StringBuilder()
        for (ch in s) {
            if (ch in '0'..'9') {
                sb.append(olChikiDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    private val numberNamesSantali = listOf(
        "ᱢᱤᱫ", "ᱵᱟᱨ", "ᱯᱮ", "ᱯᱩᱱ", "ᱢᱚᱬᱮ",
        "ᱛᱩᱨᱩᱭ", "ᱮᱭᱟᱭ", "ᱤᱨᱟᱹᱞ", "ᱟᱨᱮ", "ᱜᱮᱞ",
        "ᱜᱮᱞ ᱢᱤᱫ", "ᱜᱮᱞ ᱵᱟᱨ", "ᱜᱮᱞ ᱯᱮ", "ᱜᱮᱞ ᱯᱩᱱ", "ᱜᱮᱞ ᱢᱚᱬᱮ",
        "ᱜᱮᱞ ᱛᱩᱨᱩᱭ", "ᱜᱮᱞ ᱮᱭᱟᱭ", "ᱜᱮᱞ ᱤᱨᱟᱹᱞ", "ᱜᱮᱞ ᱟᱨᱮ", "ᱤᱥᱤ"
    )

    private val numberNamesHindi = listOf(
        "एक", "दो", "तीन", "चार", "पाँच",
        "छह", "सात", "आठ", "नौ", "दस",
        "ग्यारह", "बारह", "तेरह", "चौदह", "पंद्रह",
        "सोलह", "सत्रह", "अठारह", "उन्नीस", "बीस"
    )

    private val numberPhonetics = listOf(
        "MID", "BAAR", "PE", "PUN", "MON-EH",
        "TUR-OY", "EY-AY", "EER-AL", "AA-RE", "GEL",
        "GEL-MID", "GEL-BAAR", "GEL-PE", "GEL-PUN", "GEL-MON-EH",
        "GEL-TUR-OY", "GEL-EY-AY", "GEL-EER-AL", "GEL-AA-RE", "EE-SI"
    )

    // Dedicated, exact matching counting primitives for 1..20
    private val numberSvgPaths = listOf(
        "fln_svg_corpus/tokens/star.svg" to "star",
        "fln_svg_corpus/plants/apple.svg" to "apple",
        "fln_svg_corpus/animals/fish.svg" to "fish",
        "fln_svg_corpus/animals/bird.svg" to "bird",
        "fln_svg_corpus/plants/mango.svg" to "mango",
        "fln_svg_corpus/tokens/flower.svg" to "flower",
        "fln_svg_corpus/plants/banana.svg" to "banana",
        "fln_svg_corpus/plants/sal_leaf.svg" to "sal_leaf",
        "fln_svg_corpus/animals/duck.svg" to "duck",
        "fln_svg_corpus/village_life/clay_pot.svg" to "clay_pot",
        "fln_svg_corpus/tokens/egg.svg" to "egg",
        "fln_svg_corpus/plants/orange.svg" to "orange",
        "fln_svg_corpus/tokens/bowl_rice.svg" to "bowl_rice",
        "fln_svg_corpus/plants/tree.svg" to "tree",
        "fln_svg_corpus/math/coin_1.svg" to "coin_1",
        "fln_svg_corpus/animals/butterfly.svg" to "butterfly",
        "fln_svg_corpus/animals/frog.svg" to "frog",
        "fln_svg_corpus/school_and_play/book.svg" to "book",
        "fln_svg_corpus/school_and_play/pencil.svg" to "pencil",
        "fln_svg_corpus/school_and_play/kite.svg" to "kite"
    )

    private val numberCards = (1..20).map { num ->
        val olDigit = toOlChikiDigits(num)
        val sanWord = numberNamesSantali[num - 1]
        val hinWord = numberNamesHindi[num - 1]
        val phon = numberPhonetics[num - 1]
        val (svgPath, icon) = numberSvgPaths[num - 1]

        FlnCard(
            id = "num_%02d".format(num),
            domain = FlnDomain.NUMERACY_COUNTING,
            category = FlnCategory.NUMBERS,
            santaliOlChiki = "$olDigit ($sanWord)",
            hindiText = "$num ($hinWord)",
            englishGloss = "Number $num",
            teacherPhoneticGuide = "$phon ($olDigit)",
            imageAssetPath = svgPath,
            vectorIconType = icon,
            numeralValue = num,
            countingQuantity = num,
            nipunCode = if (num <= 10) "N-BAL.1" else "N-G1.1",
            grade = if (num <= 5) FlnGrade.BALVATIKA else if (num <= 10) FlnGrade.GRADE_1 else FlnGrade.GRADE_2,
            fingerTracingGuide = "Trace the numeral $olDigit and tap each item to count $num.",
            exampleSentenceSantali = "$olDigit ᱴᱤ ᱡᱚ ᱢᱮᱱᱟᱜᱼᱟ᱾",
            exampleSentenceHindi = "यहाँ $num फल हैं।"
        )
    }

    // -------------------------------------------------------------------------
    // 3. Dedicated Animals Deck (Clean Segregation)
    // -------------------------------------------------------------------------
    private val animalCards = listOf(
        FlnCard(
            id = "voc_an_01",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.ANIMALS,
            santaliOlChiki = "ᱥᱮᱛᱟ",
            hindiText = "सेता (कुत्ता)",
            englishGloss = "Dog",
            teacherPhoneticGuide = "SE-TAA",
            imageAssetPath = "fln_svg_corpus/animals/dog.svg",
            vectorIconType = "dog",
            nipunCode = "L-BAL.2",
            exampleSentenceSantali = "ᱥᱮᱛᱟ ᱚᱲᱟᱜ ᱮ ᱨᱩᱠᱷᱤᱭᱟᱹᱭᱟ᱾",
            exampleSentenceHindi = "कुत्ता घर की रखवाली करता है।"
        ),
        FlnCard(
            id = "voc_an_02",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.ANIMALS,
            santaliOlChiki = "ᱜᱟᱹᱭ",
            hindiText = "गाई (गाय)",
            englishGloss = "Cow",
            teacherPhoneticGuide = "GAA-I",
            imageAssetPath = "fln_svg_corpus/animals/cow.svg",
            vectorIconType = "cow",
            nipunCode = "L-BAL.2",
            exampleSentenceSantali = "ᱜᱟᱹᱭ ᱛᱚᱣᱟᱭ ᱮᱢᱚᱜᱼᱟ᱾",
            exampleSentenceHindi = "गाय दूध देती है।"
        ),
        FlnCard(
            id = "voc_an_03",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.ANIMALS,
            santaliOlChiki = "ᱵᱤᱞᱟᱹᱭ",
            hindiText = "बिलाई (बिल्ली)",
            englishGloss = "Cat",
            teacherPhoneticGuide = "BI-LAA-I",
            imageAssetPath = "fln_svg_corpus/animals/cat.svg",
            vectorIconType = "cat",
            nipunCode = "L-BAL.2",
            exampleSentenceSantali = "ᱵᱤᱞᱟᱹᱭ ᱛᱚᱣᱟᱭ ᱧᱩᱭᱟ᱾",
            exampleSentenceHindi = "बिल्ली दूध पीती है।"
        ),
        FlnCard(
            id = "voc_an_04",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.ANIMALS,
            santaliOlChiki = "ᱰᱟᱝᱜᱽᱨᱟ",
            hindiText = "डांगरा (बैल)",
            englishGloss = "Bull / Ox",
            teacherPhoneticGuide = "DAANG-RAA",
            imageAssetPath = "fln_svg_corpus/animals/bull.svg",
            vectorIconType = "bull",
            nipunCode = "L-G1.1",
            exampleSentenceSantali = "ᱰᱟᱝᱜᱽᱨᱟ ᱠᱷᱮᱛ ᱮ ᱥᱤᱭᱟ᱾",
            exampleSentenceHindi = "बैल खेत जोतता है।"
        ),
        FlnCard(
            id = "voc_an_05",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.ANIMALS,
            santaliOlChiki = "ᱦᱟᱹᱛᱤ",
            hindiText = "हाती (हाथी)",
            englishGloss = "Elephant",
            teacherPhoneticGuide = "HAA-TI",
            imageAssetPath = "fln_svg_corpus/animals/elephant.svg",
            vectorIconType = "elephant",
            nipunCode = "L-BAL.2",
            exampleSentenceSantali = "ᱦᱟᱹᱛᱤ ᱟᱹᱰᱤ ᱢᱟᱨᱟᱝ ᱜᱮᱭᱟᱭ᱾",
            exampleSentenceHindi = "हाथी बहुत बड़ा होता है।"
        ),
        FlnCard(
            id = "voc_an_06",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.ANIMALS,
            santaliOlChiki = "ᱪᱮᱬᱮ",
            hindiText = "चेणे (चिड़िया)",
            englishGloss = "Bird",
            teacherPhoneticGuide = "CHE-NE",
            imageAssetPath = "fln_svg_corpus/animals/bird.svg",
            vectorIconType = "bird",
            nipunCode = "L-BAL.2",
            exampleSentenceSantali = "ᱪᱮᱬᱮ ᱫᱟᱨᱮ ᱨᱮ ᱛᱩᱠᱟᱹᱭ ᱵᱮᱱᱟᱣᱟ᱾",
            exampleSentenceHindi = "चिड़िया पेड़ पर घोंसला बनाती है।"
        ),
        FlnCard(
            id = "voc_an_07",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.ANIMALS,
            santaliOlChiki = "ᱮᱨᱮ",
            hindiText = "एरे (तोता)",
            englishGloss = "Parrot",
            teacherPhoneticGuide = "E-RE",
            imageAssetPath = "fln_svg_corpus/animals/parrot.svg",
            vectorIconType = "parrot",
            nipunCode = "L-G1.1",
            exampleSentenceSantali = "ᱮᱨᱮ ᱪᱮᱬᱮ ᱦᱟᱹᱨᱭᱟᱹᱲ ᱜᱮᱭᱟᱭ᱾",
            exampleSentenceHindi = "तोता हरे रंग का होता है।"
        ),
        FlnCard(
            id = "voc_an_08",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.ANIMALS,
            santaliOlChiki = "ᱛᱟᱹᱨᱩᱵ",
            hindiText = "तारुब (बाघ)",
            englishGloss = "Tiger",
            teacherPhoneticGuide = "TAA-RUB",
            imageAssetPath = "fln_svg_corpus/animals/tiger.svg",
            vectorIconType = "tiger",
            nipunCode = "L-G1.1",
            exampleSentenceSantali = "ᱛᱟᱹᱨᱩᱵ ᱵᱤᱨ ᱨᱮᱱ ᱨᱟᱡᱟ ᱠᱟᱱᱟᱭ᱾",
            exampleSentenceHindi = "बाघ जंगल का राजा है।"
        ),
        FlnCard(
            id = "voc_an_09",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.ANIMALS,
            santaliOlChiki = "ᱥᱟᱫᱚᱢ",
            hindiText = "सादोम (घोड़ा)",
            englishGloss = "Horse",
            teacherPhoneticGuide = "SAA-DOM",
            imageAssetPath = "fln_svg_corpus/animals/horse.svg",
            vectorIconType = "horse",
            nipunCode = "L-G1.1",
            exampleSentenceSantali = "ᱥᱟᱫᱚᱢ ᱞᱚᱜᱚᱱ ᱮ ᱫᱟᱹᱲᱟ᱾",
            exampleSentenceHindi = "घोड़ा तेज़ दौड़ता है।"
        ),
        FlnCard(
            id = "voc_an_10",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.ANIMALS,
            santaliOlChiki = "ᱢᱟᱨᱟᱜ",
            hindiText = "माराग (मोर)",
            englishGloss = "Peacock",
            teacherPhoneticGuide = "MAA-RAAG",
            imageAssetPath = "fln_svg_corpus/animals/peacock.svg",
            vectorIconType = "peacock",
            nipunCode = "L-G1.1",
            exampleSentenceSantali = "ᱢᱟᱨᱟᱜ ᱫᱟᱜ ᱡᱟᱹᱲᱤ ᱨᱮ ᱮᱱᱮᱡᱟᱭ᱾",
            exampleSentenceHindi = "मोर बारिश में नाचता है।"
        ),
        FlnCard(
            id = "voc_an_11",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.ANIMALS,
            santaliOlChiki = "ᱵᱟᱱᱟ",
            hindiText = "बाना (भालू)",
            englishGloss = "Bear",
            teacherPhoneticGuide = "BAA-NAA",
            imageAssetPath = "fln_svg_corpus/animals/bear.svg",
            vectorIconType = "bear",
            nipunCode = "L-G1.1",
            exampleSentenceSantali = "ᱵᱟᱱᱟ ᱵᱤᱨ ᱨᱮ ᱧᱮᱞᱚᱜᱼᱟᱭ᱾",
            exampleSentenceHindi = "भालू जंगल में दिखाई देता है।"
        ),
        FlnCard(
            id = "voc_an_12",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.ANIMALS,
            santaliOlChiki = "ᱜᱟᱹᱰᱤ",
            hindiText = "गाड़ी (बंदर)",
            englishGloss = "Monkey",
            teacherPhoneticGuide = "GAA-DI",
            imageAssetPath = "fln_svg_corpus/animals/monkey.svg",
            vectorIconType = "monkey",
            nipunCode = "L-G1.1",
            exampleSentenceSantali = "ᱜᱟᱹᱰᱤ ᱫᱟᱨᱮ ᱨᱮ ᱫᱚᱱᱟᱭ᱾",
            exampleSentenceHindi = "बंदर पेड़ पर कूदता है।"
        ),
        FlnCard(
            id = "voc_an_13",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.ANIMALS,
            santaliOlChiki = "ᱠᱩᱞᱟᱹᱭ",
            hindiText = "कुलाई (खरगोश)",
            englishGloss = "Rabbit",
            teacherPhoneticGuide = "KU-LAA-I",
            imageAssetPath = "fln_svg_corpus/animals/rabbit.svg",
            vectorIconType = "rabbit",
            nipunCode = "L-BAL.2",
            exampleSentenceSantali = "ᱠᱩᱞᱟᱹᱭ ᱞᱚᱜᱚᱱ ᱮ ᱫᱚᱱᱟ᱾",
            exampleSentenceHindi = "खरगोश तेज़ी से छलांग लगाता है।"
        ),
        FlnCard(
            id = "voc_an_14",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.ANIMALS,
            santaliOlChiki = "ᱢᱮᱨᱚᱢ",
            hindiText = "मेरम (बकरी)",
            englishGloss = "Goat",
            teacherPhoneticGuide = "ME-ROM",
            imageAssetPath = "fln_svg_corpus/animals/goat.svg",
            vectorIconType = "goat",
            nipunCode = "L-BAL.2",
            exampleSentenceSantali = "ᱢᱮᱨᱚᱢ ᱜᱷᱟᱸᱥ ᱮ ᱡᱚᱢᱟ᱾",
            exampleSentenceHindi = "बकरी घास चरती है।"
        ),
        FlnCard(
            id = "voc_an_15",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.ANIMALS,
            santaliOlChiki = "ᱜᱮᱰᱮ",
            hindiText = "गेडे (बतख)",
            englishGloss = "Duck",
            teacherPhoneticGuide = "GE-DE",
            imageAssetPath = "fln_svg_corpus/animals/duck.svg",
            vectorIconType = "duck",
            nipunCode = "L-BAL.2",
            exampleSentenceSantali = "ᱜᱮᱰᱮ ᱯᱩᱠᱷᱨᱤ ᱨᱮ ᱯᱟᱭᱨᱟᱜᱼᱟᱭ᱾",
            exampleSentenceHindi = "बतख तालाब में तैरती है।"
        ),
        FlnCard(
            id = "voc_an_16",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.ANIMALS,
            santaliOlChiki = "ᱦᱟᱹᱠᱩ",
            hindiText = "हाकू (मछली)",
            englishGloss = "Fish",
            teacherPhoneticGuide = "HAA-KU",
            imageAssetPath = "fln_svg_corpus/animals/fish.svg",
            vectorIconType = "fish",
            nipunCode = "L-G1.1",
            exampleSentenceSantali = "ᱦᱟᱹᱠᱩ ᱜᱟᱰᱟ ᱨᱮ ᱯᱟᱭᱨᱟᱜᱼᱟᱭ᱾",
            exampleSentenceHindi = "मछली नदी में तैरती है।"
        ),
        FlnCard(
            id = "voc_an_17",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.ANIMALS,
            santaliOlChiki = "ᱥᱤᱢ",
            hindiText = "सिम (मुर्गी)",
            englishGloss = "Hen",
            teacherPhoneticGuide = "SEEM",
            imageAssetPath = "fln_svg_corpus/animals/hen.svg",
            vectorIconType = "hen",
            nipunCode = "L-BAL.2",
            exampleSentenceSantali = "ᱥᱤᱢ ᱵᱤᱞᱤ ᱮ ᱮᱢᱟ᱾",
            exampleSentenceHindi = "मुर्गी अंडा देती है।"
        ),
        FlnCard(
            id = "voc_an_18",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.ANIMALS,
            santaliOlChiki = "ᱯᱤᱯᱤᱲᱤᱭᱟᱹᱝ",
            hindiText = "पिपिड़ियांग (तितली)",
            englishGloss = "Butterfly",
            teacherPhoneticGuide = "PI-PI-RI-YAANG",
            imageAssetPath = "fln_svg_corpus/animals/butterfly.svg",
            vectorIconType = "butterfly",
            nipunCode = "L-BAL.2",
            exampleSentenceSantali = "ᱯᱤᱯᱤᱲᱤᱭᱟᱹᱝ ᱵᱟᱦᱟ ᱨᱮ ᱟᱬᱜᱚᱭᱟ᱾",
            exampleSentenceHindi = "तितली फूल पर बैठती है।"
        )
    )

    // -------------------------------------------------------------------------
    // 4. Dedicated Fruits & Food Deck (Clean Segregation)
    // -------------------------------------------------------------------------
    private val fruitCards = listOf(
        FlnCard(
            id = "voc_fr_01",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.FRUITS,
            santaliOlChiki = "ᱩᱞ",
            hindiText = "उल (आम)",
            englishGloss = "Mango",
            teacherPhoneticGuide = "OOL",
            imageAssetPath = "fln_svg_corpus/plants/mango.svg",
            vectorIconType = "mango",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱩᱞ ᱡᱚᱛᱚ ᱠᱷᱚᱱ ᱥᱤᱵᱤᱞ ᱡᱚ ᱠᱟᱱᱟ᱾",
            exampleSentenceHindi = "आम सबसे मीठा फल है।"
        ),
        FlnCard(
            id = "voc_fr_02",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.FRUITS,
            santaliOlChiki = "ᱠᱟᱭᱨᱟ",
            hindiText = "कायरा (केला)",
            englishGloss = "Banana",
            teacherPhoneticGuide = "KAI-RAA",
            imageAssetPath = "fln_svg_corpus/plants/banana.svg",
            vectorIconType = "banana",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱵᱤᱞᱤ ᱠᱟᱭᱨᱟ ᱦᱮᱲᱮᱢᱟ᱾",
            exampleSentenceHindi = "पका केला मीठा होता है।"
        ),
        FlnCard(
            id = "voc_fr_03",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.FRUITS,
            santaliOlChiki = "ᱟᱯᱮᱞ",
            hindiText = "आपेल (सेब)",
            englishGloss = "Apple",
            teacherPhoneticGuide = "AA-PEL",
            imageAssetPath = "fln_svg_corpus/plants/apple.svg",
            vectorIconType = "apple",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱟᱨᱟᱜ ᱟᱯᱮᱞ ᱡᱚᱢ ᱢᱮ᱾",
            exampleSentenceHindi = "लाल सेब खाओ।"
        ),
        FlnCard(
            id = "voc_fr_04",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.FRUITS,
            santaliOlChiki = "ᱠᱚᱢᱞᱟ",
            hindiText = "कोमला (संतरा)",
            englishGloss = "Orange",
            teacherPhoneticGuide = "KOM-LAA",
            imageAssetPath = "fln_svg_corpus/plants/orange.svg",
            vectorIconType = "orange",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱠᱚᱢᱞᱟ ᱨᱟᱥᱟ ᱟᱹᱰᱤ ᱱᱟᱯᱟᱭᱟ᱾",
            exampleSentenceHindi = "संतरे का रस बहुत अच्छा है।"
        ),
        FlnCard(
            id = "voc_fr_05",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.FRUITS,
            santaliOlChiki = "ᱟᱝᱜᱩᱨ",
            hindiText = "अंगूर (अंगूर)",
            englishGloss = "Grapes",
            teacherPhoneticGuide = "ANG-GUR",
            imageAssetPath = "fln_svg_corpus/plants/grapes.svg",
            vectorIconType = "grapes",
            nipunCode = "L-G1.1",
            exampleSentenceSantali = "ᱦᱟᱹᱨᱭᱟᱹᱲ ᱟᱝᱜᱩᱨ ᱥᱤᱵᱤᱞᱟ᱾",
            exampleSentenceHindi = "हरे अंगूर स्वादिष्ट हैं।"
        ),
        FlnCard(
            id = "voc_fr_06",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.FRUITS,
            santaliOlChiki = "ᱛᱚᱨᱵᱩᱡᱽ",
            hindiText = "तोरबुज (तरबूज)",
            englishGloss = "Watermelon",
            teacherPhoneticGuide = "TOR-BUJ",
            imageAssetPath = "fln_svg_corpus/plants/watermelon.svg",
            vectorIconType = "watermelon",
            nipunCode = "L-G1.1",
            exampleSentenceSantali = "ᱛᱚᱨᱵᱩᱡᱽ ᱨᱮ ᱟᱹᱰᱤ ᱫᱟᱜ ᱛᱟᱦᱮᱸᱱᱟ᱾",
            exampleSentenceHindi = "तरबूज में बहुत पानी होता है।"
        ),
        FlnCard(
            id = "voc_fr_07",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.FRUITS,
            santaliOlChiki = "ᱫᱟᱠᱟ",
            hindiText = "दाका (भात/चावल)",
            englishGloss = "Rice Bowl",
            teacherPhoneticGuide = "DAA-KAA",
            imageAssetPath = "fln_svg_corpus/tokens/bowl_rice.svg",
            vectorIconType = "bowl_rice",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱫᱟᱠᱟ ᱟᱨ ᱩᱛᱩ ᱡᱚᱢ ᱢᱮ᱾",
            exampleSentenceHindi = "चावल और सब्ज़ी खाओ।"
        ),
        FlnCard(
            id = "voc_fr_08",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.FRUITS,
            santaliOlChiki = "ᱵᱤᱞᱤ",
            hindiText = "बिली (अंडा)",
            englishGloss = "Egg",
            teacherPhoneticGuide = "BI-LI",
            imageAssetPath = "fln_svg_corpus/tokens/egg.svg",
            vectorIconType = "egg",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱥᱤᱢ ᱵᱤᱞᱤ ᱱᱤᱨᱚᱜ ᱜᱮᱭᱟ᱾",
            exampleSentenceHindi = "अंडा सेहत के लिए अच्छा है।"
        ),
        FlnCard(
            id = "voc_fr_09",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.FRUITS,
            santaliOlChiki = "ᱨᱩᱴᱤ",
            hindiText = "रुटी (रोटी)",
            englishGloss = "Roti / Bread",
            teacherPhoneticGuide = "RU-TI",
            imageAssetPath = "fln_svg_corpus/tokens/roti.svg",
            vectorIconType = "roti",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱞᱚᱞᱚ ᱨᱩᱴᱤ ᱡᱚᱢ ᱢᱮ᱾",
            exampleSentenceHindi = "गर्म रोटी खाओ।"
        ),
        FlnCard(
            id = "voc_fr_10",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.FRUITS,
            santaliOlChiki = "ᱛᱚᱣᱟ",
            hindiText = "तोवा (दूध)",
            englishGloss = "Milk",
            teacherPhoneticGuide = "TO-WAA",
            imageAssetPath = "fln_svg_corpus/tokens/milk.svg",
            vectorIconType = "milk",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱜᱟᱹᱭ ᱛᱚᱣᱟ ᱧᱩᱭ ᱢᱮ᱾",
            exampleSentenceHindi = "गाय का दूध पियो।"
        )
    )

    // -------------------------------------------------------------------------
    // 5. Dedicated Nature & Sky Deck (Clean Segregation)
    // -------------------------------------------------------------------------
    private val natureCards = listOf(
        FlnCard(
            id = "voc_na_01",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.NATURE,
            santaliOlChiki = "ᱫᱟᱨᱮ",
            hindiText = "दारे (पेड़)",
            englishGloss = "Tree",
            teacherPhoneticGuide = "DAA-RE",
            imageAssetPath = "fln_svg_corpus/plants/tree.svg",
            vectorIconType = "tree",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱫᱟᱨᱮ ᱨᱮ ᱦᱟᱹᱨᱭᱟᱹᱲ ᱥᱟᱠᱟᱢ ᱢᱮᱱᱟᱜᱼᱟ᱾",
            exampleSentenceHindi = "पेड़ पर हरी पत्तियाँ हैं।"
        ),
        FlnCard(
            id = "voc_na_02",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.NATURE,
            santaliOlChiki = "ᱵᱟᱦᱟ",
            hindiText = "बाहा (फूल)",
            englishGloss = "Flower",
            teacherPhoneticGuide = "BAA-HAA",
            imageAssetPath = "fln_svg_corpus/tokens/flower.svg",
            vectorIconType = "flower",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱵᱟᱦᱟ ᱨᱚᱝ ᱟᱹᱰᱤ ᱪᱮᱦᱨᱟ᱾",
            exampleSentenceHindi = "फूल का रंग बहुत सुंदर है।"
        ),
        FlnCard(
            id = "voc_na_03",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.NATURE,
            santaliOlChiki = "ᱥᱟᱨᱡᱚᱢ ᱥᱟᱠᱟᱢ",
            hindiText = "साल का पत्ता",
            englishGloss = "Sal Leaf",
            teacherPhoneticGuide = "SAAR-JOM SAA-KAAM",
            imageAssetPath = "fln_svg_corpus/plants/sal_leaf.svg",
            vectorIconType = "sal_leaf",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱥᱟᱨᱡᱚᱢ ᱫᱟᱨᱮ ᱵᱤᱨ ᱨᱮ ᱢᱮᱱᱟᱜᱼᱟ᱾",
            exampleSentenceHindi = "साल का पेड़ जंगल में होता है।"
        ),
        FlnCard(
            id = "voc_na_04",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.NATURE,
            santaliOlChiki = "ᱵᱩᱨᱩ",
            hindiText = "बुरु (पहाड़)",
            englishGloss = "Mountain",
            teacherPhoneticGuide = "BU-RU",
            imageAssetPath = "fln_svg_corpus/nature/mountain.svg",
            vectorIconType = "mountain",
            nipunCode = "L-G1.1",
            exampleSentenceSantali = "ᱵᱩᱨᱩ ᱟᱹᱰᱤ ᱩᱥᱩᱞ ᱜᱮᱭᱟ᱾",
            exampleSentenceHindi = "पहाड़ बहुत ऊँचा है।"
        ),
        FlnCard(
            id = "voc_na_05",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.NATURE,
            santaliOlChiki = "ᱜᱟᱰᱟ",
            hindiText = "गाडा (नदी)",
            englishGloss = "River",
            teacherPhoneticGuide = "GAA-DAA",
            imageAssetPath = "fln_svg_corpus/nature/river.svg",
            vectorIconType = "river",
            nipunCode = "L-G1.1",
            exampleSentenceSantali = "ᱜᱟᱰᱟ ᱫᱟᱜ ᱞᱤᱸᱜᱤᱱ ᱠᱟᱱᱟ᱾",
            exampleSentenceHindi = "नदी का पानी बह रहा है।"
        ),
        FlnCard(
            id = "voc_na_06",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.NATURE,
            santaliOlChiki = "ᱫᱟᱜ",
            hindiText = "दाग (पानी)",
            englishGloss = "Water",
            teacherPhoneticGuide = "DAAG",
            imageAssetPath = "fln_svg_corpus/nature/water.svg",
            vectorIconType = "water",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱥᱟᱯᱷᱟ ᱫᱟᱜ ᱧᱩᱭ ᱢᱮ᱾",
            exampleSentenceHindi = "साफ पानी पियो।"
        ),
        FlnCard(
            id = "voc_na_07",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.NATURE,
            santaliOlChiki = "ᱨᱤᱢᱤᱞ",
            hindiText = "रिमिल (बादल)",
            englishGloss = "Cloud",
            teacherPhoneticGuide = "RI-MIL",
            imageAssetPath = "fln_svg_corpus/nature/cloud.svg",
            vectorIconType = "cloud",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱨᱤᱢᱤᱞ ᱫᱟᱜ ᱮ ᱡᱟᱹᱲᱤᱭᱟ᱾",
            exampleSentenceHindi = "बादल से बारिश होती है।"
        ),
        FlnCard(
            id = "voc_na_08",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.NATURE,
            santaliOlChiki = "ᱥᱤᱧ ᱪᱟᱸᱫᱚ",
            hindiText = "सूरज (सूर्य)",
            englishGloss = "Sun",
            teacherPhoneticGuide = "SING CHAAN-DO",
            imageAssetPath = "fln_svg_corpus/nature/sun.svg",
            vectorIconType = "sun",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱥᱤᱧ ᱪᱟᱸᱫᱚ ᱢᱟᱨᱥᱟᱞ ᱮ ᱮᱢᱟ᱾",
            exampleSentenceHindi = "सूरज रोशनी देता है।"
        ),
        FlnCard(
            id = "voc_na_09",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.NATURE,
            santaliOlChiki = "ᱧᱤᱫᱟᱹ ᱪᱟᱸᱫᱚ",
            hindiText = "चाँद (चंद्रमा)",
            englishGloss = "Moon",
            teacherPhoneticGuide = "NYI-DAA CHAAN-DO",
            imageAssetPath = "fln_svg_corpus/nature/moon.svg",
            vectorIconType = "moon",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱧᱤᱫᱟᱹ ᱪᱟᱸᱫᱚ ᱨᱮᱭᱟᱲ ᱢᱟᱨᱥᱟᱞ ᱮ ᱮᱢᱟ᱾",
            exampleSentenceHindi = "चाँद शीतल चाँदनी देता है।"
        ),
        FlnCard(
            id = "voc_na_10",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.NATURE,
            santaliOlChiki = "ᱥᱮᱸᱜᱮᱞ",
            hindiText = "सेंगेल (आग)",
            englishGloss = "Fire",
            teacherPhoneticGuide = "SEN-GEL",
            imageAssetPath = "fln_svg_corpus/nature/fire.svg",
            vectorIconType = "fire",
            nipunCode = "L-G1.1",
            exampleSentenceSantali = "ᱥᱮᱸᱜᱮᱞ ᱛᱮ ᱫᱟᱠᱟ ᱤᱥᱤᱱᱚᱜᱼᱟ᱾",
            exampleSentenceHindi = "आग से खाना पकता है।"
        )
    )

    // -------------------------------------------------------------------------
    // 6. Dedicated School & Village Life Deck (Clean Segregation)
    // -------------------------------------------------------------------------
    private val schoolCards = listOf(
        FlnCard(
            id = "voc_sc_01",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.SCHOOL,
            santaliOlChiki = "ᱤᱛᱩᱱ ᱟᱥᱲᱟ",
            hindiText = "इतून आशड़ा (विद्यालय)",
            englishGloss = "School",
            teacherPhoneticGuide = "EE-TUN AAS-RRAA",
            imageAssetPath = "fln_svg_corpus/school_and_play/school.svg",
            vectorIconType = "school",
            nipunCode = "L-G1.1",
            exampleSentenceSantali = "ᱫᱮᱞᱟ ᱤᱛᱩᱱ ᱟᱥᱲᱟ ᱛᱮ ᱪᱟᱞᱟᱜᱼᱟ᱾",
            exampleSentenceHindi = "चलो स्कूल चलते हैं।"
        ),
        FlnCard(
            id = "voc_sc_02",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.SCHOOL,
            santaliOlChiki = "ᱯᱚᱛᱚᱵ",
            hindiText = "पोतोब (किताब)",
            englishGloss = "Book",
            teacherPhoneticGuide = "PO-TOB",
            imageAssetPath = "fln_svg_corpus/school_and_play/book.svg",
            vectorIconType = "book",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱯᱚᱛᱚᱵ ᱯᱟᱲᱦᱟᱣ ᱢᱮ᱾",
            exampleSentenceHindi = "किताब पढ़ो।"
        ),
        FlnCard(
            id = "voc_sc_03",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.SCHOOL,
            santaliOlChiki = "ᱯᱮᱱᱥᱤᱞ",
            hindiText = "पेंसिल (पेंसिल)",
            englishGloss = "Pencil",
            teacherPhoneticGuide = "PEN-SIL",
            imageAssetPath = "fln_svg_corpus/school_and_play/pencil.svg",
            vectorIconType = "pencil",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱯᱮᱱᱥᱤᱞ ᱛᱮ ᱚᱞ ᱢᱮ᱾",
            exampleSentenceHindi = "पेंसिल से लिखो।"
        ),
        FlnCard(
            id = "voc_sc_04",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.SCHOOL,
            santaliOlChiki = "ᱥᱞᱮᱴ",
            hindiText = "स्लेट (तख्ती)",
            englishGloss = "Slate",
            teacherPhoneticGuide = "S-LE-TT",
            imageAssetPath = "fln_svg_corpus/school_and_play/slate.svg",
            vectorIconType = "slate",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱥᱞᱮᱴ ᱨᱮ ᱚᱞ ᱪᱤᱠᱤ ᱚᱞ ᱢᱮ᱾",
            exampleSentenceHindi = "स्लेट पर ओल चिकी लिखो।"
        ),
        FlnCard(
            id = "voc_sc_05",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.SCHOOL,
            santaliOlChiki = "ᱛᱷᱟᱹᱞᱤ",
            hindiText = "थाली (बस्ता/झोला)",
            englishGloss = "School Bag",
            teacherPhoneticGuide = "THAA-LI",
            imageAssetPath = "fln_svg_corpus/school_and_play/bag.svg",
            vectorIconType = "bag",
            nipunCode = "L-G1.1",
            exampleSentenceSantali = "ᱛᱷᱟᱹᱞᱤ ᱨᱮ ᱯᱚᱛᱚᱵ ᱫᱚᱦᱚᱭ ᱢᱮ᱾",
            exampleSentenceHindi = "बस्ते में किताबें रखो।"
        ),
        FlnCard(
            id = "voc_sc_06",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.SCHOOL,
            santaliOlChiki = "ᱵᱚᱞ",
            hindiText = "बोल (गेंद)",
            englishGloss = "Play Ball",
            teacherPhoneticGuide = "BOL",
            imageAssetPath = "fln_svg_corpus/school_and_play/ball.svg",
            vectorIconType = "ball",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱜᱟᱛᱮ ᱥᱟᱶ ᱵᱚᱞ ᱮᱱᱮᱡ ᱢᱮ᱾",
            exampleSentenceHindi = "दोस्त के साथ गेंद खेलो।"
        ),
        FlnCard(
            id = "voc_sc_07",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.SCHOOL,
            santaliOlChiki = "ᱜᱩᱰᱤ",
            hindiText = "गुडी (पतंग)",
            englishGloss = "Kite",
            teacherPhoneticGuide = "GU-DI",
            imageAssetPath = "fln_svg_corpus/school_and_play/kite.svg",
            vectorIconType = "kite",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱥᱮᱨᱢᱟ ᱨᱮ ᱜᱩᱰᱤ ᱩᱰᱟᱹᱣ ᱢᱮ᱾",
            exampleSentenceHindi = "आसमान में पतंग उड़ाओ।"
        ),
        FlnCard(
            id = "voc_sc_08",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.SCHOOL,
            santaliOlChiki = "ᱜᱷᱟᱹᱱᱴᱤ",
            hindiText = "घंटी (घंटी)",
            englishGloss = "School Bell",
            teacherPhoneticGuide = "GHAN-TI",
            imageAssetPath = "fln_svg_corpus/school_and_play/bell.svg",
            vectorIconType = "bell",
            nipunCode = "L-G1.1",
            exampleSentenceSantali = "ᱟᱥᱲᱟ ᱜᱷᱟᱹᱱᱴᱤ ᱥᱟᱰᱮᱭᱮᱱᱟ᱾",
            exampleSentenceHindi = "स्कूल की घंटी बज गई।"
        ),
        FlnCard(
            id = "voc_sc_09",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.SCHOOL,
            santaliOlChiki = "ᱚᱲᱟᱜ",
            hindiText = "ओड़ाग् (घर)",
            englishGloss = "House / Hut",
            teacherPhoneticGuide = "O-RRAAG",
            imageAssetPath = "fln_svg_corpus/village_life/straw_hut.svg",
            vectorIconType = "straw_hut",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱱᱚᱣᱟ ᱫᱚ ᱟᱞᱮᱭᱟᱜ ᱚᱲᱟᱜ ᱠᱟᱱᱟ᱾",
            exampleSentenceHindi = "यह हमारा घर है।"
        ),
        FlnCard(
            id = "voc_sc_10",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.SCHOOL,
            santaliOlChiki = "ᱴᱩᱠᱩᱡ",
            hindiText = "टुकुज (घड़ा)",
            englishGloss = "Clay Pot",
            teacherPhoneticGuide = "TU-KUJ",
            imageAssetPath = "fln_svg_corpus/village_life/clay_pot.svg",
            vectorIconType = "clay_pot",
            nipunCode = "L-BAL.1",
            exampleSentenceSantali = "ᱴᱩᱠᱩᱡ ᱨᱮ ᱨᱮᱭᱟᱲ ᱫᱟᱜ ᱛᱟᱦᱮᱸᱱᱟ᱾",
            exampleSentenceHindi = "मटके में ठंडा पानी रहता है।"
        ),
        FlnCard(
            id = "voc_sc_11",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.SCHOOL,
            santaliOlChiki = "ᱛᱩᱢᱫᱟᱜ",
            hindiText = "तुम्दाग (मांदर)",
            englishGloss = "Tumdak Drum",
            teacherPhoneticGuide = "TUM-DAAG",
            imageAssetPath = "fln_svg_corpus/village_life/tumdak_drum.svg",
            vectorIconType = "tumdak_drum",
            nipunCode = "L-G1.1",
            exampleSentenceSantali = "ᱥᱚᱦᱨᱟᱭ ᱨᱮ ᱛᱩᱢᱫᱟᱜ ᱨᱩᱭᱟᱠᱚ᱾",
            exampleSentenceHindi = "सोहराय में मांदर बजाते हैं।"
        ),
        FlnCard(
            id = "voc_sc_12",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.SCHOOL,
            santaliOlChiki = "ᱛᱤᱨᱤᱭᱟᱹᱣ",
            hindiText = "तिरियौ (बांसुरी)",
            englishGloss = "Tribal Flute",
            teacherPhoneticGuide = "TI-RI-YAW",
            imageAssetPath = "fln_svg_corpus/village_life/flute.svg",
            vectorIconType = "flute",
            nipunCode = "L-G1.1",
            exampleSentenceSantali = "ᱛᱤᱨᱤᱭᱟᱹᱣ ᱨᱟᱦᱟ ᱟᱹᱰᱤ ᱱᱟᱯᱟᱭᱟ᱾",
            exampleSentenceHindi = "बांसुरी की धुन बहुत सुरीली है।"
        ),
        FlnCard(
            id = "voc_sc_13",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.SCHOOL,
            santaliOlChiki = "ᱰᱟᱹᱞᱤ",
            hindiText = "डाली (टोकरी)",
            englishGloss = "Bamboo Basket",
            teacherPhoneticGuide = "DAA-LI",
            imageAssetPath = "fln_svg_corpus/village_life/basket.svg",
            vectorIconType = "basket",
            nipunCode = "L-BAL.2",
            exampleSentenceSantali = "ᱰᱟᱹᱞᱤ ᱨᱮ ᱡᱚ ᱫᱚᱦᱚᱭ ᱢᱮ᱾",
            exampleSentenceHindi = "टोकरी में फल रखो।"
        ),
        FlnCard(
            id = "voc_sc_14",
            domain = FlnDomain.LITERACY_VOCABULARY,
            category = FlnCategory.SCHOOL,
            santaliOlChiki = "ᱰᱟᱹᱰᱤ",
            hindiText = "डाडी (कुआँ)",
            englishGloss = "Village Well",
            teacherPhoneticGuide = "DAA-DI",
            imageAssetPath = "fln_svg_corpus/village_life/well.svg",
            vectorIconType = "well",
            nipunCode = "L-BAL.2",
            exampleSentenceSantali = "ᱰᱟᱹᱰᱤ ᱠᱷᱚᱱ ᱫᱟᱜ ᱞᱩᱭ ᱢᱮ᱾",
            exampleSentenceHindi = "कुएँ से पानी भरो।"
        )
    )

    // -------------------------------------------------------------------------
    // 7. Spatial Geometry & Shapes Deck (Clean Segregation)
    // -------------------------------------------------------------------------
    private val spatialCards = listOf(
        FlnCard(
            id = "sp_01",
            domain = FlnDomain.NUMERACY_SPATIAL,
            category = FlnCategory.SPATIAL,
            santaliOlChiki = "ᱜᱩᱞᱟᱹᱴ",
            hindiText = "गोल (वृत्त)",
            englishGloss = "Circle",
            teacherPhoneticGuide = "GU-LAAT",
            imageAssetPath = "fln_svg_corpus/tokens/circle.svg",
            vectorIconType = "circle",
            nipunCode = "N-G1.2",
            exampleSentenceSantali = "ᱪᱟᱸᱫᱚ ᱫᱚ ᱜᱩᱞᱟᱹᱴ ᱜᱮᱭᱟ᱾",
            exampleSentenceHindi = "सूरज गोल होता है।"
        ),
        FlnCard(
            id = "sp_02",
            domain = FlnDomain.NUMERACY_SPATIAL,
            category = FlnCategory.SPATIAL,
            santaliOlChiki = "ᱪᱟᱹᱣᱠᱟᱹ",
            hindiText = "चौकोर (वर्ग)",
            englishGloss = "Square",
            teacherPhoneticGuide = "CHAU-KAA",
            imageAssetPath = "fln_svg_corpus/tokens/square.svg",
            vectorIconType = "square",
            nipunCode = "N-G1.2",
            exampleSentenceSantali = "ᱯᱚᱛᱚᱵ ᱫᱚ ᱪᱟᱹᱣᱠᱟᱹ ᱜᱮᱭᱟ᱾",
            exampleSentenceHindi = "किताब चौकोर होती है।"
        ),
        FlnCard(
            id = "sp_03",
            domain = FlnDomain.NUMERACY_SPATIAL,
            category = FlnCategory.SPATIAL,
            santaliOlChiki = "ᱯᱮᱠᱳᱬ",
            hindiText = "त्रिकोण (त्रिभुज)",
            englishGloss = "Triangle",
            teacherPhoneticGuide = "PE-KON",
            imageAssetPath = "fln_svg_corpus/tokens/triangle.svg",
            vectorIconType = "triangle",
            nipunCode = "N-G1.2",
            exampleSentenceSantali = "ᱵᱩᱨᱩ ᱪᱩᱲᱟᱹ ᱫᱚ ᱯᱮᱠᱳᱬ ᱞᱮᱠᱟ᱾",
            exampleSentenceHindi = "पहाड़ की चोटी त्रिकोण जैसी है।"
        ),
        FlnCard(
            id = "sp_04",
            domain = FlnDomain.NUMERACY_SPATIAL,
            category = FlnCategory.SPATIAL,
            santaliOlChiki = "ᱟᱭᱛᱟᱠᱟᱨ",
            hindiText = "आयत (दीर्घचतुरस्र)",
            englishGloss = "Rectangle",
            teacherPhoneticGuide = "AY-TAA-KAAR",
            imageAssetPath = "fln_svg_corpus/math/rectangle.svg",
            vectorIconType = "rectangle",
            nipunCode = "N-G1.2",
            exampleSentenceSantali = "ᱫᱩᱣᱟᱹᱨ ᱫᱚ ᱟᱭᱛᱟᱠᱟᱨ ᱜᱮᱭᱟ᱾",
            exampleSentenceHindi = "दरवाज़ा आयताकार है।"
        ),
        FlnCard(
            id = "sp_05",
            domain = FlnDomain.NUMERACY_SPATIAL,
            category = FlnCategory.SPATIAL,
            santaliOlChiki = "ᱢᱟᱨᱟᱝ / ᱦᱩᱰᱤᱧ",
            hindiText = "बड़ा / छोटा (आकार तुलना)",
            englishGloss = "Big vs Small",
            teacherPhoneticGuide = "MAA-RAANG / HU-DING",
            imageAssetPath = "fln_svg_corpus/tokens/circle.svg",
            vectorIconType = "circle",
            nipunCode = "N-BAL.2",
            exampleSentenceSantali = "ᱦᱟᱹᱛᱤ ᱫᱚ ᱢᱟᱨᱟᱝ, ᱵᱤᱞᱟᱹᱭ ᱫᱚ ᱦᱩᱰᱤᱧ᱾",
            exampleSentenceHindi = "हाथी बड़ा है, बिल्ली छोटी है।"
        ),
        FlnCard(
            id = "sp_06",
            domain = FlnDomain.NUMERACY_SPATIAL,
            category = FlnCategory.SPATIAL,
            santaliOlChiki = "ᱪᱮᱛᱟᱱ / ᱞᱟᱛᱟᱨ",
            hindiText = "ऊपर / नीचे (स्थानिक समझ)",
            englishGloss = "Up vs Down",
            teacherPhoneticGuide = "CHE-TAAN / LAA-TAAR",
            imageAssetPath = "fln_svg_corpus/tokens/star.svg",
            vectorIconType = "star",
            nipunCode = "N-BAL.2",
            exampleSentenceSantali = "ᱪᱮᱬᱮ ᱪᱮᱛᱟᱱ ᱨᱮ, ᱦᱟᱹᱠᱩ ᱞᱟᱛᱟᱨ ᱨᱮ᱾",
            exampleSentenceHindi = "चिड़िया ऊपर है, मछली नीचे है।"
        )
    )

    // -------------------------------------------------------------------------
    // 8. Visual Arithmetic Operations (Balvatika to Grade 3)
    // -------------------------------------------------------------------------
    private val arithmeticCards = listOf(
        FlnCard(
            id = "math_add_01",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = FlnCategory.ARITHMETIC,
            santaliOlChiki = "᱑ + ᱒ = ᱓",
            hindiText = "१ + २ = ३ (जोड़)",
            englishGloss = "1 + 2 = 3 (Visual Addition)",
            teacherPhoneticGuide = "MID + BAAR = PE (JOD)",
            imageAssetPath = "fln_svg_corpus/math/plus.svg",
            vectorIconType = "plus",
            numeralValue = 3,
            countingQuantity = 1,
            nipunCode = "N-G1.2",
            grade = FlnGrade.GRADE_1,
            fingerTracingGuide = "Combine 1 apple and 2 apples to count 3.",
            exampleSentenceSantali = "ᱢᱤᱫᱴᱟᱝ ᱩᱞ ᱟᱨ ᱵᱟᱨᱭᱟ ᱩᱞ ᱢᱮᱥᱟ ᱠᱟᱛᱮ ᱯᱮᱭᱟ ᱦᱩᱭᱩᱜᱼᱟ᱾",
            exampleSentenceHindi = "१ आम और २ आम मिलकर ३ आम होते हैं।"
        ),
        FlnCard(
            id = "math_add_02",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = FlnCategory.ARITHMETIC,
            santaliOlChiki = "᱒ + ᱓ = ᱕",
            hindiText = "२ + ३ = ५ (जोड़)",
            englishGloss = "2 + 3 = 5 (Addition to 5)",
            teacherPhoneticGuide = "BAAR + PE = MON-EH",
            imageAssetPath = "fln_svg_corpus/plants/mango.svg",
            vectorIconType = "mango",
            numeralValue = 5,
            countingQuantity = 2,
            nipunCode = "N-G1.2",
            grade = FlnGrade.GRADE_1,
            fingerTracingGuide = "Add 2 mangoes and 3 mangoes to make 5.",
            exampleSentenceSantali = "ᱵᱟᱨᱭᱟ ᱩᱞ ᱟᱨ ᱯᱮᱭᱟ ᱩᱞ ᱡᱚᱲᱟᱣ ᱠᱟᱛᱮ ᱢᱚᱬᱮ ᱦᱩᱭᱩᱜᱼᱟ᱾",
            exampleSentenceHindi = "२ आम और ३ आम जोड़कर ५ होते हैं।"
        ),
        FlnCard(
            id = "math_add_03",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = FlnCategory.ARITHMETIC,
            santaliOlChiki = "᱕ + ᱕ = ᱑᱐",
            hindiText = "५ + ५ = १० (दस का ढाँचा)",
            englishGloss = "5 + 5 = 10 (Ten-Frame)",
            teacherPhoneticGuide = "MON-EH + MON-EH = GEL",
            imageAssetPath = "fln_svg_corpus/math/dot.svg",
            vectorIconType = "dot",
            numeralValue = 10,
            countingQuantity = 5,
            nipunCode = "N-G1.2",
            grade = FlnGrade.GRADE_1,
            fingerTracingGuide = "Fill the top 5 and bottom 5 of the ten-frame to reach 10.",
            exampleSentenceSantali = "ᱢᱚᱬᱮ ᱟᱨ ᱢᱚᱬᱮ ᱢᱮᱥᱟ ᱠᱟᱛᱮ ᱜᱮᱞ ᱦᱩᱭᱩᱜᱼᱟ᱾",
            exampleSentenceHindi = "पाँच और पाँच मिलकर दस बनते हैं।"
        ),
        FlnCard(
            id = "math_sub_01",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = FlnCategory.ARITHMETIC,
            santaliOlChiki = "᱕ - ᱒ = ᱓",
            hindiText = "५ - २ = ३ (घटाव)",
            englishGloss = "5 - 2 = 3 (Visual Subtraction)",
            teacherPhoneticGuide = "MON-EH - BAAR = PE (BHEGAR)",
            imageAssetPath = "fln_svg_corpus/math/minus.svg",
            vectorIconType = "minus",
            numeralValue = 3,
            countingQuantity = 5,
            nipunCode = "N-G1.2",
            grade = FlnGrade.GRADE_1,
            fingerTracingGuide = "Take away 2 items from 5 items to leave 3.",
            exampleSentenceSantali = "ᱢᱚᱬᱮ ᱠᱷᱚᱱ ᱵᱟᱨ ᱵᱷᱮᱜᱟᱨ ᱞᱮᱠᱷᱟᱱ ᱯᱮ ᱥᱟᱨᱮᱲᱚᱜᱼᱟ᱾",
            exampleSentenceHindi = "५ में से २ घटाने पर ३ बचते हैं।"
        ),
        FlnCard(
            id = "math_sub_02",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = FlnCategory.ARITHMETIC,
            santaliOlChiki = "᱘ - ᱓ = ᱕",
            hindiText = "८ - ३ = ५ (घटाव)",
            englishGloss = "8 - 3 = 5 (Subtraction with Fish)",
            teacherPhoneticGuide = "EER-AL - PE = MON-EH",
            imageAssetPath = "fln_svg_corpus/animals/fish.svg",
            vectorIconType = "fish",
            numeralValue = 5,
            countingQuantity = 8,
            nipunCode = "N-G1.2",
            grade = FlnGrade.GRADE_1,
            fingerTracingGuide = "From 8 swimming fish, 3 swim away. 5 remain.",
            exampleSentenceSantali = "ᱤᱨᱟᱹᱞ ᱦᱟᱹᱠᱩ ᱠᱷᱚᱱ ᱯᱮ ᱦᱟᱹᱠᱩ ᱪᱟᱞᱟᱣ ᱮᱱᱟ, ᱢᱚᱬᱮ ᱥᱟᱨᱮᱲ ᱮᱱᱟ᱾",
            exampleSentenceHindi = "८ मछलियों में से ३ चली गईं, ५ बचीं।"
        ),
        FlnCard(
            id = "math_mul_01",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = FlnCategory.ARITHMETIC,
            santaliOlChiki = "᱒ × ᱓ = ᱖",
            hindiText = "२ × ३ = ६ (समान समूह / गुणा)",
            englishGloss = "2 × 3 = 6 (2 Groups of 3)",
            teacherPhoneticGuide = "BAAR x PE = TUR-OY (GUNAA)",
            imageAssetPath = "fln_svg_corpus/math/multiply.svg",
            vectorIconType = "multiply",
            numeralValue = 6,
            countingQuantity = 2,
            nipunCode = "N-G2.2",
            grade = FlnGrade.GRADE_2,
            fingerTracingGuide = "Count 2 baskets with 3 eggs each to find 6.",
            exampleSentenceSantali = "ᱵᱟᱨᱭᱟ ᱰᱟᱹᱞᱤ ᱨᱮ ᱯᱮ-ᱯᱮ ᱵᱤᱞᱤ, ᱢᱩᱴᱷ ᱛᱩᱨᱩᱭ ᱵᱤᱞᱤ᱾",
            exampleSentenceHindi = "२ टोकरियों में ३-३ अंडे, कुल ६ अंडे।"
        ),
        FlnCard(
            id = "math_mul_02",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = FlnCategory.ARITHMETIC,
            santaliOlChiki = "᱓ × ᱔ = ᱑᱒",
            hindiText = "३ × ४ = १२ (गुणा तालिका)",
            englishGloss = "3 × 4 = 12 (3 Groups of 4)",
            teacherPhoneticGuide = "PE x PUN = GEL-BAAR",
            imageAssetPath = "fln_svg_corpus/village_life/basket.svg",
            vectorIconType = "basket",
            numeralValue = 12,
            countingQuantity = 3,
            nipunCode = "N-G2.2",
            grade = FlnGrade.GRADE_2,
            fingerTracingGuide = "3 rows of 4 mangoes equals 12 mangoes.",
            exampleSentenceSantali = "ᱯᱮ ᱫᱚᱞ ᱨᱮ ᱯᱩᱱ-ᱯᱩᱱ ᱩᱞ, ᱡᱚᱛᱚ ᱛᱮ ᱜᱮᱞ ᱵᱟᱨ ᱩᱞ᱾",
            exampleSentenceHindi = "३ समूहों में ४-४ आम, कुल १२ आम।"
        ),
        FlnCard(
            id = "math_div_01",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = FlnCategory.ARITHMETIC,
            santaliOlChiki = "᱖ ÷ ᱒ = ᱓",
            hindiText = "६ ÷ २ = ३ (समान बँटवारा / भाग)",
            englishGloss = "6 ÷ 2 = 3 (Equal Sharing)",
            teacherPhoneticGuide = "TUR-OY / BAAR = PE (HATING)",
            imageAssetPath = "fln_svg_corpus/math/divide.svg",
            vectorIconType = "divide",
            numeralValue = 3,
            countingQuantity = 6,
            nipunCode = "N-G2.2",
            grade = FlnGrade.GRADE_2,
            fingerTracingGuide = "Divide 6 apples equally between 2 children. Each gets 3.",
            exampleSentenceSantali = "ᱛᱩᱨᱩᱭ ᱩᱞ ᱵᱟᱨ ᱦᱚᱲ ᱨᱮ ᱦᱟᱹᱴᱤᱧ ᱞᱮᱠᱷᱟᱱ ᱯᱮ-ᱯᱮ ᱧᱟᱢᱚᱜᱼᱟ᱾",
            exampleSentenceHindi = "६ आम दो लोगों में बराबर बाँटने पर ३-३ मिलते हैं।"
        )
    )

    // -------------------------------------------------------------------------
    // 9. Indian Currency & Money Deck (Clean Segregation)
    // -------------------------------------------------------------------------
    private val moneyCards = listOf(
        FlnCard(
            id = "math_coin_01",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = FlnCategory.MONEY,
            santaliOlChiki = "ᱢᱤᱫ ᱴᱟᱠᱟ (₹᱑)",
            hindiText = "१ रुपया सिक्का (₹1)",
            englishGloss = "1 Rupee Coin",
            teacherPhoneticGuide = "MID TAKA",
            imageAssetPath = "fln_svg_corpus/math/coin_1.svg",
            vectorIconType = "coin_1",
            numeralValue = 1,
            nipunCode = "N-G2.3",
            grade = FlnGrade.GRADE_1,
            fingerTracingGuide = "Feel the round coin edge and numeral 1.",
            exampleSentenceSantali = "ᱱᱚᱣᱟ ᱫᱚ ᱢᱤᱫ ᱴᱟᱠᱟ ᱨᱮᱱᱟᱜ ᱠᱚᱭᱤᱱ ᱠᱟᱱᱟ᱾",
            exampleSentenceHindi = "यह एक रुपये का सिक्का है।"
        ),
        FlnCard(
            id = "math_coin_02",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = FlnCategory.MONEY,
            santaliOlChiki = "ᱵᱟᱨ ᱴᱟᱠᱟ (₹᱒)",
            hindiText = "२ रुपये का सिक्का (₹2)",
            englishGloss = "2 Rupee Coin",
            teacherPhoneticGuide = "BAAR TAKA",
            imageAssetPath = "fln_svg_corpus/math/coin_2.svg",
            vectorIconType = "coin_2",
            numeralValue = 2,
            nipunCode = "N-G2.3",
            grade = FlnGrade.GRADE_1,
            fingerTracingGuide = "1 coin of ₹2 equals two ₹1 coins.",
            exampleSentenceSantali = "ᱵᱟᱨ ᱴᱟᱠᱟ ᱛᱮ ᱢᱤᱫᱴᱟᱝ ᱯᱮᱱᱥᱤᱞ ᱧᱟᱢᱚᱜᱼᱟ᱾",
            exampleSentenceHindi = "दो रुपये में एक पेंसिल मिलती है।"
        ),
        FlnCard(
            id = "math_coin_05",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = FlnCategory.MONEY,
            santaliOlChiki = "ᱢᱚᱬᱮ ᱴᱟᱠᱟ (₹᱕)",
            hindiText = "५ रुपये का सिक्का (₹5)",
            englishGloss = "5 Rupee Coin",
            teacherPhoneticGuide = "MON-EH TAKA",
            imageAssetPath = "fln_svg_corpus/math/coin_5.svg",
            vectorIconType = "coin_5",
            numeralValue = 5,
            nipunCode = "N-G2.3",
            grade = FlnGrade.GRADE_2,
            fingerTracingGuide = "Golden brass 5 rupee coin.",
            exampleSentenceSantali = "ᱢᱚᱬᱮ ᱴᱟᱠᱟ ᱛᱮ ᱢᱤᱫᱴᱟᱝ ᱥᱞᱮᱴ ᱠᱤᱨᱤᱧ ᱢᱮ᱾",
            exampleSentenceHindi = "पाँच रुपये का सिक्का सुनहरी होता है।"
        ),
        FlnCard(
            id = "math_coin_10",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = FlnCategory.MONEY,
            santaliOlChiki = "ᱜᱮᱞ ᱴᱟᱠᱟ (₹᱑᱐)",
            hindiText = "१० रुपये का सिक्का (₹10)",
            englishGloss = "10 Rupee Coin",
            teacherPhoneticGuide = "GEL TAKA",
            imageAssetPath = "fln_svg_corpus/math/coin_10.svg",
            vectorIconType = "coin_10",
            numeralValue = 10,
            nipunCode = "N-G2.3",
            grade = FlnGrade.GRADE_2,
            fingerTracingGuide = "Two 5 rupee coins make one 10 rupee coin.",
            exampleSentenceSantali = "ᱜᱮᱞ ᱴᱟᱠᱟ ᱛᱮ ᱯᱩᱛᱷᱤ ᱠᱤᱨᱤᱧ ᱢᱮ᱾",
            exampleSentenceHindi = "दस रुपये में एक अच्छी किताब मिलती है।"
        ),
        FlnCard(
            id = "math_note_10",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = FlnCategory.MONEY,
            santaliOlChiki = "ᱜᱮᱞ ᱴᱟᱠᱟ ᱱᱳᱴ (₹᱑᱐)",
            hindiText = "१० रुपये का नोट (₹10)",
            englishGloss = "10 Rupee Note",
            teacherPhoneticGuide = "GEL TAKA NOTE",
            imageAssetPath = "fln_svg_corpus/math/rupee_note.svg",
            vectorIconType = "rupee_note",
            numeralValue = 10,
            nipunCode = "N-G2.3",
            grade = FlnGrade.GRADE_2,
            fingerTracingGuide = "Chocolate brown 10 rupee currency note with Konark Sun Temple wheel.",
            exampleSentenceSantali = "ᱜᱮᱞ ᱴᱟᱠᱟ ᱱᱳᱴ ᱛᱮ ᱠᱷᱟᱛᱟ ᱠᱤᱨᱤᱧ ᱢᱮ᱾",
            exampleSentenceHindi = "दस रुपये के नोट से कॉपी खरीदो।"
        ),
        FlnCard(
            id = "math_note_20",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = FlnCategory.MONEY,
            santaliOlChiki = "ᱤᱥᱤ ᱴᱟᱠᱟ ᱱᱳᱴ (₹᱒᱐)",
            hindiText = "२० रुपये का नोट (₹20)",
            englishGloss = "20 Rupee Note",
            teacherPhoneticGuide = "ISI TAKA NOTE",
            imageAssetPath = "fln_svg_corpus/math/rupee_note.svg",
            vectorIconType = "rupee_note",
            numeralValue = 20,
            nipunCode = "N-G2.3",
            grade = FlnGrade.GRADE_2,
            fingerTracingGuide = "Greenish yellow 20 rupee currency note with Ellora Caves.",
            exampleSentenceSantali = "ᱤᱥᱤ ᱴᱟᱠᱟ ᱛᱮ ᱵᱟᱨᱭᱟ ᱯᱚᱛᱚᱵ ᱧᱟᱢᱚᱜᱼᱟ᱾",
            exampleSentenceHindi = "बीस रुपये में दो किताबें मिलती हैं।"
        )
    )

    // Master Deck Order: High-interest vocabulary first, followed by arithmetic, numbers, and akshar
    private val masterDeck: List<FlnCard> = animalCards + fruitCards + natureCards + schoolCards + spatialCards + arithmeticCards + moneyCards + numberCards + aksharCards

    // -------------------------------------------------------------------------
    // Query & Filtering APIs
    // -------------------------------------------------------------------------

    fun getAllCards(): List<FlnCard> = masterDeck

    fun getCardsByDomain(domain: FlnDomain): List<FlnCard> {
        return masterDeck.filter { it.domain == domain }
    }

    fun getCardsByCategory(category: FlnCategory): List<FlnCard> {
        if (category == FlnCategory.ALL) return masterDeck
        return masterDeck.filter { it.category == category }
    }

    fun getCardById(id: String): FlnCard? {
        return masterDeck.firstOrNull { it.id == id }
    }

    fun getFlashcardCategories(): List<FlnCategory> {
        return FlnCategory.entries
    }

    fun searchCards(query: String): List<FlnCard> {
        if (query.isBlank()) return masterDeck
        val clean = query.trim().lowercase()
        return masterDeck.filter { card ->
            card.hindiText.lowercase().contains(clean) ||
            card.santaliOlChiki.lowercase().contains(clean) ||
            card.englishGloss.lowercase().contains(clean) ||
            card.teacherPhoneticGuide.lowercase().contains(clean) ||
            card.exemplarWordHindi.lowercase().contains(clean) ||
            card.exemplarWordSantali.lowercase().contains(clean)
        }
    }

    /**
     * Generates plausible, distinct, category-aligned distractors for any FLN card.
     * Guaranteed to return distinct items that do NOT include the card's target answer.
     */
    fun generateDistractorsForCard(card: FlnCard, count: Int = 3): List<String> {
        val targetAnswer = card.numeralValue?.toString() ?: card.santaliOlChiki

        if (card.numeralValue != null) {
            val v = card.numeralValue
            // Generate nearby distinct numerical options
            val candidates = mutableListOf<Int>()
            for (offset in listOf(1, -1, 2, -2, 3, -3, 4, -4)) {
                val candidate = v + offset
                if (candidate in 1..20 && candidate != v && !candidates.contains(candidate)) {
                    candidates.add(candidate)
                }
                if (candidates.size >= count) break
            }
            var fallback = 1
            while (candidates.size < count && fallback <= 10) {
                if (fallback != v && !candidates.contains(fallback)) {
                    candidates.add(fallback)
                }
                fallback++
            }
            return candidates.take(count).map { it.toString() }
        }

        if (card.category == FlnCategory.AKSHAR) {
            val otherAkshars = getCardsByCategory(FlnCategory.AKSHAR)
                .filter { it.id != card.id && it.santaliOlChiki != targetAnswer }
                .map { it.santaliOlChiki }
                .distinct()
                .shuffled()

            val selected = otherAkshars.take(count).toMutableList()
            val defaultAkshars = listOf("ᱚ", "ᱛ", "ᱜ", "ᱝ", "ᱞ", "ᱟ", "ᱠ", "ᱡ", "ᱢ", "ᱣ", "ᱤ", "ᱥ", "ᱦ", "ᱧ", "ᱨ", "ᱩ", "ᱪ", "ᱫ", "ᱬ", "ᱭ", "ᱮ", "ᱯ", "ᱰ", "ᱱ", "ᱲ", "ᱳ", "ᱴ", "ᱵ", "ᱶ", "ᱦ")
            for (ch in defaultAkshars) {
                if (selected.size >= count) break
                if (ch != targetAnswer && !selected.contains(ch)) {
                    selected.add(ch)
                }
            }
            return selected.take(count)
        }

        // Vocabulary cards: sample from the same category first, then all cards
        val sameCategoryWords = getCardsByCategory(card.category)
            .filter { it.id != card.id && it.santaliOlChiki != targetAnswer }
            .map { it.santaliOlChiki }
            .distinct()
            .shuffled()

        val selected = sameCategoryWords.take(count).toMutableList()

        if (selected.size < count) {
            val otherWords = getAllCards()
                .filter { it.category != FlnCategory.NUMBERS && it.id != card.id && it.santaliOlChiki != targetAnswer && !selected.contains(it.santaliOlChiki) }
                .map { it.santaliOlChiki }
                .distinct()
                .shuffled()
            selected.addAll(otherWords.take(count - selected.size))
        }

        val defaultWords = listOf("ᱫᱟᱨᱮ", "ᱜᱟᱰᱟ", "ᱵᱟᱦᱟ", "ᱩᱞ", "ᱦᱟᱹᱠᱩ", "ᱥᱮᱛᱟ", "ᱚᱲᱟᱜ", "ᱤᱯᱤᱞ")
        for (w in defaultWords) {
            if (selected.size >= count) break
            if (w != targetAnswer && !selected.contains(w)) {
                selected.add(w)
            }
        }

        return selected.take(count)
    }
}
