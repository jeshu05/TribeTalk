package org.tribetalk.fln.repository

import org.tribetalk.fln.model.*
import kotlin.random.Random

/**
 * Curated curriculum repository providing NIPUN Bharat aligned bilingual datasets
 * for Foundational Literacy and Numeracy in Hindi and Santali (Ol Chiki).
 */
object FlnCurriculumRepository {

    val CATEGORY_ALL = "All Topics"
    val CATEGORY_AKSHAR = "Akshar (ᱚᱞ ᱪᱤᱠᱤ)"
    val CATEGORY_NUMBERS = "Numbers (ᱞᱮᱠᱷᱟ)"
    val CATEGORY_ANIMALS = "Animals (ᱡᱤᱵᱽ ᱡᱤᱭᱟᱹᱞᱤ)"
    val CATEGORY_NATURE = "Nature (ᱥᱤᱨᱡᱚᱱ)"
    val CATEGORY_SCHOOL_FAMILY = "School & Family (ᱟᱥᱲᱟ ᱟᱨ ᱜᱷᱟᱨᱚᱸᱡᱽ)"
    val CATEGORY_SHAPES = "Shapes & Space (ᱨᱩᱯ ᱟᱨ ᱡᱟᱭᱜᱟ)"

    private val CARDS = listOf(
        // ---------------------------------------------------------------------
        // 1. Akshar / Ol Chiki Alphabets (NIPUN L1: Phonological Awareness)
        // ---------------------------------------------------------------------
        FlnCard(
            id = "ak_01",
            domain = FlnDomain.LITERACY_AKSHAR,
            category = CATEGORY_AKSHAR,
            nipunCode = "L1.1",
            hindiText = "अ (ध्वनि /a/)",
            santaliOlChiki = "ᱚ",
            teacherPhoneticGuide = "अ (La / Ah)",
            englishGloss = "Vowel /a/",
            iconType = "akshar",
            exampleSentenceHindi = "पहला अक्षर 'अ' है।",
            exampleSentenceSantali = "ᱯᱩᱭᱞᱩ ᱪᱤᱠᱤ ᱫᱚ 'ᱚ' ᱠᱟᱱᱟ ᱾"
        ),
        FlnCard(
            id = "ak_02",
            domain = FlnDomain.LITERACY_AKSHAR,
            category = CATEGORY_AKSHAR,
            nipunCode = "L1.1",
            hindiText = "त (ध्वनि /at/)",
            santaliOlChiki = "ᱛ",
            teacherPhoneticGuide = "अत् (At)",
            englishGloss = "Consonant /t/",
            iconType = "akshar"
        ),
        FlnCard(
            id = "ak_03",
            domain = FlnDomain.LITERACY_AKSHAR,
            category = CATEGORY_AKSHAR,
            nipunCode = "L1.1",
            hindiText = "ग (ध्वनि /ag/)",
            santaliOlChiki = "ᱜ",
            teacherPhoneticGuide = "अग् (Ag)",
            englishGloss = "Consonant /g/",
            iconType = "akshar"
        ),
        FlnCard(
            id = "ak_04",
            domain = FlnDomain.LITERACY_AKSHAR,
            category = CATEGORY_AKSHAR,
            nipunCode = "L1.1",
            hindiText = "ङ (ध्वनि /ang/)",
            santaliOlChiki = "ᱝ",
            teacherPhoneticGuide = "अं (Ang)",
            englishGloss = "Nasal /ng/",
            iconType = "akshar"
        ),
        FlnCard(
            id = "ak_05",
            domain = FlnDomain.LITERACY_AKSHAR,
            category = CATEGORY_AKSHAR,
            nipunCode = "L1.1",
            hindiText = "ल (ध्वनि /al/)",
            santaliOlChiki = "ᱞ",
            teacherPhoneticGuide = "अल् (Al)",
            englishGloss = "Liquid /l/",
            iconType = "akshar"
        ),
        FlnCard(
            id = "ak_06",
            domain = FlnDomain.LITERACY_AKSHAR,
            category = CATEGORY_AKSHAR,
            nipunCode = "L1.1",
            hindiText = "आ (ध्वनि /aa/)",
            santaliOlChiki = "ᱟ",
            teacherPhoneticGuide = "आ (Aak)",
            englishGloss = "Vowel /aa/",
            iconType = "akshar"
        ),
        FlnCard(
            id = "ak_07",
            domain = FlnDomain.LITERACY_AKSHAR,
            category = CATEGORY_AKSHAR,
            nipunCode = "L1.1",
            hindiText = "क (ध्वनि /aak/)",
            santaliOlChiki = "ᱠ",
            teacherPhoneticGuide = "आक् (Ak)",
            englishGloss = "Consonant /k/",
            iconType = "akshar"
        ),
        FlnCard(
            id = "ak_08",
            domain = FlnDomain.LITERACY_AKSHAR,
            category = CATEGORY_AKSHAR,
            nipunCode = "L1.1",
            hindiText = "ज (ध्वनि /aaj/)",
            santaliOlChiki = "ᱡ",
            teacherPhoneticGuide = "आज् (Aj)",
            englishGloss = "Consonant /j/",
            iconType = "akshar"
        ),
        FlnCard(
            id = "ak_09",
            domain = FlnDomain.LITERACY_AKSHAR,
            category = CATEGORY_AKSHAR,
            nipunCode = "L1.1",
            hindiText = "म (ध्वनि /aam/)",
            santaliOlChiki = "ᱢ",
            teacherPhoneticGuide = "आम् (Am)",
            englishGloss = "Consonant /m/",
            iconType = "akshar"
        ),
        FlnCard(
            id = "ak_10",
            domain = FlnDomain.LITERACY_AKSHAR,
            category = CATEGORY_AKSHAR,
            nipunCode = "L1.1",
            hindiText = "स (ध्वनि /is/)",
            santaliOlChiki = "ᱥ",
            teacherPhoneticGuide = "इस् (Is)",
            englishGloss = "Sibilant /s/",
            iconType = "akshar"
        ),

        // ---------------------------------------------------------------------
        // 2. Foundational Numeracy (1–10) (NIPUN N1: Number Sense)
        // ---------------------------------------------------------------------
        FlnCard(
            id = "num_01",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = CATEGORY_NUMBERS,
            nipunCode = "N1.1",
            hindiText = "एक (१)",
            santaliOlChiki = "ᱢᱤᱫ (᱑)",
            teacherPhoneticGuide = "मिद (Mid)",
            englishGloss = "One (1)",
            iconType = "number_counter",
            numeralValue = 1,
            exampleSentenceHindi = "यह एक सेब है।",
            exampleSentenceSantali = "ᱱᱚᱣᱟ ᱫᱚ ᱢᱤᱫᱴᱟᱝ ᱥᱮᱣ ᱠᱟᱱᱟ ᱾"
        ),
        FlnCard(
            id = "num_02",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = CATEGORY_NUMBERS,
            nipunCode = "N1.1",
            hindiText = "दो (२)",
            santaliOlChiki = "ᱵᱟᱨ (᱒)",
            teacherPhoneticGuide = "बार (Bar)",
            englishGloss = "Two (2)",
            iconType = "number_counter",
            numeralValue = 2,
            exampleSentenceHindi = "दो पक्षी उड़ रहे हैं।",
            exampleSentenceSantali = "ᱵᱟᱨᱭᱟ ᱪᱮᱬᱮ ᱠᱤᱱ ᱩᱰᱟᱹᱣᱜ ᱠᱟᱱᱟ ᱾"
        ),
        FlnCard(
            id = "num_03",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = CATEGORY_NUMBERS,
            nipunCode = "N1.1",
            hindiText = "तीन (३)",
            santaliOlChiki = "ᱯᱮ (᱓)",
            teacherPhoneticGuide = "पे (Pe)",
            englishGloss = "Three (3)",
            iconType = "number_counter",
            numeralValue = 3
        ),
        FlnCard(
            id = "num_04",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = CATEGORY_NUMBERS,
            nipunCode = "N1.1",
            hindiText = "चार (४)",
            santaliOlChiki = "ᱯᱩᱱ (᱔)",
            teacherPhoneticGuide = "पुन (Pun)",
            englishGloss = "Four (4)",
            iconType = "number_counter",
            numeralValue = 4
        ),
        FlnCard(
            id = "num_05",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = CATEGORY_NUMBERS,
            nipunCode = "N1.1",
            hindiText = "पाँच (५)",
            santaliOlChiki = "ᱢᱚᱬᱮ (᱕)",
            teacherPhoneticGuide = "मोण़े (Mone)",
            englishGloss = "Five (5)",
            iconType = "number_counter",
            numeralValue = 5
        ),
        FlnCard(
            id = "num_06",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = CATEGORY_NUMBERS,
            nipunCode = "N1.2",
            hindiText = "छह (६)",
            santaliOlChiki = "ᱛᱩᱨᱩᱭ (᱖)",
            teacherPhoneticGuide = "तुरुय (Turuy)",
            englishGloss = "Six (6)",
            iconType = "number_counter",
            numeralValue = 6
        ),
        FlnCard(
            id = "num_07",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = CATEGORY_NUMBERS,
            nipunCode = "N1.2",
            hindiText = "सात (७)",
            santaliOlChiki = "ᱮᱭᱟᱭ (᱗)",
            teacherPhoneticGuide = "एयाय (Eyay)",
            englishGloss = "Seven (7)",
            iconType = "number_counter",
            numeralValue = 7
        ),
        FlnCard(
            id = "num_08",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = CATEGORY_NUMBERS,
            nipunCode = "N1.2",
            hindiText = "आठ (८)",
            santaliOlChiki = "ᱤᱨᱟᱹᱞ (᱘)",
            teacherPhoneticGuide = "इरल (Iral)",
            englishGloss = "Eight (8)",
            iconType = "number_counter",
            numeralValue = 8
        ),
        FlnCard(
            id = "num_09",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = CATEGORY_NUMBERS,
            nipunCode = "N1.2",
            hindiText = "नौ (९)",
            santaliOlChiki = "ᱟᱨᱮ (᱙)",
            teacherPhoneticGuide = "आरे (Are)",
            englishGloss = "Nine (9)",
            iconType = "number_counter",
            numeralValue = 9
        ),
        FlnCard(
            id = "num_10",
            domain = FlnDomain.NUMERACY_COUNTING,
            category = CATEGORY_NUMBERS,
            nipunCode = "N1.2",
            hindiText = "दस (१०)",
            santaliOlChiki = "ᱜᱮᱞ (᱑᱐)",
            teacherPhoneticGuide = "गेल (Gel)",
            englishGloss = "Ten (10)",
            iconType = "number_counter",
            numeralValue = 10
        ),

        // ---------------------------------------------------------------------
        // 3. Animals & Living World (NIPUN L2: Vocabulary Development)
        // ---------------------------------------------------------------------
        FlnCard(
            id = "an_01",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_ANIMALS,
            nipunCode = "L2.1",
            hindiText = "कुत्ता",
            santaliOlChiki = "ᱥᱮᱛᱟ",
            teacherPhoneticGuide = "सेता (Seta)",
            englishGloss = "Dog",
            iconType = "dog",
            exampleSentenceHindi = "कुत्ता भौंकता है।",
            exampleSentenceSantali = "ᱥᱮᱛᱟ ᱫᱚᱭ ᱵᱷᱩᱜᱟᱹᱜ-ᱟ ᱾"
        ),
        FlnCard(
            id = "an_02",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_ANIMALS,
            nipunCode = "L2.1",
            hindiText = "गाय",
            santaliOlChiki = "ᱜᱟᱹᱭ",
            teacherPhoneticGuide = "गय (Gai)",
            englishGloss = "Cow",
            iconType = "cow",
            exampleSentenceHindi = "गाय दूध देती है।",
            exampleSentenceSantali = "ᱜᱟᱹᱭ ᱫᱚ ᱛᱚᱣᱟᱭ ᱮᱢᱟ ᱾"
        ),
        FlnCard(
            id = "an_03",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_ANIMALS,
            nipunCode = "L2.1",
            hindiText = "बकरी",
            santaliOlChiki = "ᱢᱮᱨᱚᱢ",
            teacherPhoneticGuide = "मेरोम (Merom)",
            englishGloss = "Goat",
            iconType = "goat"
        ),
        FlnCard(
            id = "an_04",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_ANIMALS,
            nipunCode = "L2.1",
            hindiText = "पक्षी (चिड़िया)",
            santaliOlChiki = "ᱪᱮᱬᱮ",
            teacherPhoneticGuide = "चेण़े (Chene)",
            englishGloss = "Bird",
            iconType = "bird"
        ),
        FlnCard(
            id = "an_05",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_ANIMALS,
            nipunCode = "L2.1",
            hindiText = "मछली",
            santaliOlChiki = "ᱦᱟᱹᱠᱩ",
            teacherPhoneticGuide = "हाकु (Haku)",
            englishGloss = "Fish",
            iconType = "fish"
        ),
        FlnCard(
            id = "an_06",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_ANIMALS,
            nipunCode = "L2.1",
            hindiText = "बिल्ली",
            santaliOlChiki = "ᱯᱩᱥᱤ",
            teacherPhoneticGuide = "पुसी (Pusi)",
            englishGloss = "Cat",
            iconType = "cat"
        ),
        FlnCard(
            id = "an_07",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_ANIMALS,
            nipunCode = "L2.1",
            hindiText = "हाथी",
            santaliOlChiki = "ᱦᱟᱛᱤ",
            teacherPhoneticGuide = "हाती (Hati)",
            englishGloss = "Elephant",
            iconType = "elephant"
        ),

        // ---------------------------------------------------------------------
        // 4. Nature & Environment (NIPUN L2: Vocabulary)
        // ---------------------------------------------------------------------
        FlnCard(
            id = "na_01",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_NATURE,
            nipunCode = "L2.2",
            hindiText = "पेड़",
            santaliOlChiki = "ᱫᱟᱨᱮ",
            teacherPhoneticGuide = "दारे (Dare)",
            englishGloss = "Tree",
            iconType = "tree",
            exampleSentenceHindi = "यह एक बड़ा पेड़ है।",
            exampleSentenceSantali = "ᱱᱚᱣᱟ ᱫᱚ ᱢᱤᱫᱴᱟᱝ ᱢᱟᱨᱟᱝ ᱫᱟᱨᱮ ᱠᱟᱱᱟ ᱾"
        ),
        FlnCard(
            id = "na_02",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_NATURE,
            nipunCode = "L2.2",
            hindiText = "पानी",
            santaliOlChiki = "ᱫᱟᱜ",
            teacherPhoneticGuide = "दाग (Dag)",
            englishGloss = "Water",
            iconType = "water"
        ),
        FlnCard(
            id = "na_03",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_NATURE,
            nipunCode = "L2.2",
            hindiText = "नदी",
            santaliOlChiki = "ᱜᱟᱰᱟ",
            teacherPhoneticGuide = "गाडा (Gada)",
            englishGloss = "River",
            iconType = "river"
        ),
        FlnCard(
            id = "na_04",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_NATURE,
            nipunCode = "L2.2",
            hindiText = "पहाड़",
            santaliOlChiki = "ᱵᱩᱨᱩ",
            teacherPhoneticGuide = "बुरु (Buru)",
            englishGloss = "Mountain",
            iconType = "mountain"
        ),
        FlnCard(
            id = "na_05",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_NATURE,
            nipunCode = "L2.2",
            hindiText = "सूरज",
            santaliOlChiki = "ᱥᱤᱧ ᱪᱟᱸᱫᱚ",
            teacherPhoneticGuide = "सिञ चाँद (Sin Chando)",
            englishGloss = "Sun",
            iconType = "sun"
        ),
        FlnCard(
            id = "na_06",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_NATURE,
            nipunCode = "L2.2",
            hindiText = "फूल",
            santaliOlChiki = "ᱵᱟᱦᱟ",
            teacherPhoneticGuide = "बाहा (Baha)",
            englishGloss = "Flower",
            iconType = "flower"
        ),
        FlnCard(
            id = "na_07",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_NATURE,
            nipunCode = "L2.2",
            hindiText = "जंगल",
            santaliOlChiki = "ᱵᱤᱨ",
            teacherPhoneticGuide = "बीर (Bir)",
            englishGloss = "Forest",
            iconType = "forest"
        ),

        // ---------------------------------------------------------------------
        // 5. School & Family (NIPUN L2: Daily Context)
        // ---------------------------------------------------------------------
        FlnCard(
            id = "sf_01",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_SCHOOL_FAMILY,
            nipunCode = "L2.3",
            hindiText = "किताब (पुस्तक)",
            santaliOlChiki = "ᱯᱩᱛᱷᱤ",
            teacherPhoneticGuide = "पुथी (Puthi)",
            englishGloss = "Book",
            iconType = "book"
        ),
        FlnCard(
            id = "sf_02",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_SCHOOL_FAMILY,
            nipunCode = "L2.3",
            hindiText = "कलम",
            santaliOlChiki = "ᱠᱚᱞᱚᱢ",
            teacherPhoneticGuide = "कोलम (Kolom)",
            englishGloss = "Pen",
            iconType = "pencil"
        ),
        FlnCard(
            id = "sf_03",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_SCHOOL_FAMILY,
            nipunCode = "L2.3",
            hindiText = "स्कूल (विद्यालय)",
            santaliOlChiki = "ᱤᱛᱩᱱ ᱟᱥᱲᱟ",
            teacherPhoneticGuide = "इतुन आसड़ा (Itun Asra)",
            englishGloss = "School",
            iconType = "school"
        ),
        FlnCard(
            id = "sf_04",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_SCHOOL_FAMILY,
            nipunCode = "L2.3",
            hindiText = "माँ (माता)",
            santaliOlChiki = "ᱟᱭᱳ",
            teacherPhoneticGuide = "आयो (Ayo)",
            englishGloss = "Mother",
            iconType = "mother"
        ),
        FlnCard(
            id = "sf_05",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_SCHOOL_FAMILY,
            nipunCode = "L2.3",
            hindiText = "पिताजी (बापू)",
            santaliOlChiki = "ᱵᱟᱵᱟ",
            teacherPhoneticGuide = "बाबा (Baba)",
            englishGloss = "Father",
            iconType = "father"
        ),
        FlnCard(
            id = "sf_06",
            domain = FlnDomain.LITERACY_VOCAB,
            category = CATEGORY_SCHOOL_FAMILY,
            nipunCode = "L2.3",
            hindiText = "मित्र (दोस्त)",
            santaliOlChiki = "ᱜᱟᱛᱮ",
            teacherPhoneticGuide = "गाते (Gate)",
            englishGloss = "Friend",
            iconType = "friend"
        ),

        // ---------------------------------------------------------------------
        // 6. Shapes & Spatial Concepts (NIPUN N2: Spatial Understanding)
        // ---------------------------------------------------------------------
        FlnCard(
            id = "sh_01",
            domain = FlnDomain.NUMERACY_SHAPES,
            category = CATEGORY_SHAPES,
            nipunCode = "N2.1",
            hindiText = "गोल (वृत्त)",
            santaliOlChiki = "ᱜᱳᱞ",
            teacherPhoneticGuide = "गोल (Gol)",
            englishGloss = "Circle",
            iconType = "shape_circle"
        ),
        FlnCard(
            id = "sh_02",
            domain = FlnDomain.NUMERACY_SHAPES,
            category = CATEGORY_SHAPES,
            nipunCode = "N2.1",
            hindiText = "त्रिभुज",
            santaliOlChiki = "ᱯᱮ ᱠᱳᱬ",
            teacherPhoneticGuide = "पे कोण (Pe Kon)",
            englishGloss = "Triangle",
            iconType = "shape_triangle"
        ),
        FlnCard(
            id = "sh_03",
            domain = FlnDomain.NUMERACY_SHAPES,
            category = CATEGORY_SHAPES,
            nipunCode = "N2.2",
            hindiText = "बड़ा",
            santaliOlChiki = "ᱢᱟᱨᱟᱝ",
            teacherPhoneticGuide = "मारांग (Marang)",
            englishGloss = "Big",
            iconType = "concept_big"
        ),
        FlnCard(
            id = "sh_04",
            domain = FlnDomain.NUMERACY_SHAPES,
            category = CATEGORY_SHAPES,
            nipunCode = "N2.2",
            hindiText = "छोटा",
            santaliOlChiki = "ᱦᱩᱰᱤᱧ",
            teacherPhoneticGuide = "हुडिञ (Hudin)",
            englishGloss = "Small",
            iconType = "concept_small"
        ),
        FlnCard(
            id = "sh_05",
            domain = FlnDomain.NUMERACY_SHAPES,
            category = CATEGORY_SHAPES,
            nipunCode = "N2.2",
            hindiText = "ऊपर",
            santaliOlChiki = "ᱪᱮᱛᱟᱱ",
            teacherPhoneticGuide = "चेतान (Chetan)",
            englishGloss = "Up",
            iconType = "concept_up"
        ),
        FlnCard(
            id = "sh_06",
            domain = FlnDomain.NUMERACY_SHAPES,
            category = CATEGORY_SHAPES,
            nipunCode = "N2.2",
            hindiText = "नीचे",
            santaliOlChiki = "ᱞᱟᱛᱟᱨ",
            teacherPhoneticGuide = "लातार (Latar)",
            englishGloss = "Down",
            iconType = "concept_down"
        )
    )

    fun getAllCards(): List<FlnCard> = CARDS

    fun getCategories(): List<String> = listOf(
        CATEGORY_ALL,
        CATEGORY_AKSHAR,
        CATEGORY_NUMBERS,
        CATEGORY_ANIMALS,
        CATEGORY_NATURE,
        CATEGORY_SCHOOL_FAMILY,
        CATEGORY_SHAPES
    )

    fun getCardsByCategory(category: String): List<FlnCard> {
        return if (category == CATEGORY_ALL) {
            CARDS
        } else {
            CARDS.filter { it.category == category }
        }
    }

    /**
     * Algorithmic generation of bilingual worksheet problem sets.
     */
    fun generateWorksheet(config: WorksheetConfig): List<WorksheetItem> {
        val rand = Random(config.seed)
        val count = config.questionCount.coerceIn(3, 8)

        return when (config.type) {
            WorksheetType.COUNT_AND_MATCH -> {
                val numberCards = CARDS.filter { it.domain == FlnDomain.NUMERACY_COUNTING && it.numeralValue != null }
                    .shuffled(rand)
                    .take(count)

                numberCards.mapIndexed { index, card ->
                    WorksheetItem(
                        id = "q_$index",
                        prompt = "Count the objects and connect to the correct Santali & Hindi number:",
                        iconType = listOf("star", "circle", "flower", "apple").random(rand),
                        quantity = card.numeralValue ?: (index + 1),
                        leftLabelHindi = card.hindiText,
                        rightLabelSantali = card.santaliOlChiki
                    )
                }
            }

            WorksheetType.PICTURE_WORD_MATCH -> {
                val vocabCards = CARDS.filter {
                    it.category == CATEGORY_ANIMALS || it.category == CATEGORY_NATURE || it.category == CATEGORY_SCHOOL_FAMILY
                }.shuffled(rand).take(count)

                vocabCards.mapIndexed { index, card ->
                    WorksheetItem(
                        id = "q_$index",
                        prompt = "Match the picture with its Santali word and Hindi translation:",
                        iconType = card.iconType,
                        quantity = 1,
                        leftLabelHindi = card.hindiText,
                        rightLabelSantali = "${card.santaliOlChiki} [${card.teacherPhoneticGuide}]"
                    )
                }
            }

            WorksheetType.AKSHAR_TRACING -> {
                val aksharCards = CARDS.filter { it.domain == FlnDomain.LITERACY_AKSHAR }
                    .shuffled(rand)
                    .take(count)

                aksharCards.mapIndexed { index, card ->
                    WorksheetItem(
                        id = "q_$index",
                        prompt = "Trace the Ol Chiki letter and practice its pronunciation:",
                        iconType = "akshar",
                        quantity = 1,
                        leftLabelHindi = "${card.santaliOlChiki} ( ${card.teacherPhoneticGuide} )",
                        rightLabelSantali = ". . .  . . .  . . ."
                    )
                }
            }

            WorksheetType.ASSESSMENT_CIRCLE -> {
                val candidateCards = CARDS.filter { it.domain != FlnDomain.LITERACY_AKSHAR }
                    .shuffled(rand)
                    .take(count)

                candidateCards.mapIndexed { index, card ->
                    val distractors = CARDS.filter { it.id != card.id && it.category == card.category }
                        .shuffled(rand)
                        .take(2)
                        .map { it.santaliOlChiki }

                    val allOptions = (distractors + card.santaliOlChiki).shuffled(rand)
                    val correctIdx = allOptions.indexOf(card.santaliOlChiki)

                    WorksheetItem(
                        id = "q_$index",
                        prompt = "What is '${card.hindiText}' in Santali (Ol Chiki)?",
                        iconType = card.iconType,
                        quantity = 1,
                        leftLabelHindi = card.hindiText,
                        options = allOptions,
                        correctIndex = correctIdx
                    )
                }
            }
        }
    }
}
