package org.tribetalk.fln.repository

import org.tribetalk.fln.model.*

/**
 * Curated curriculum repository providing NIPUN Bharat aligned bilingual datasets
 * for Foundational Literacy and Numeracy in Hindi and Santali (Ol Chiki).
 * Contains 120+ rich foundational cards across 10 essential early childhood domains.
 */
object FlnCurriculumRepository {

    const val CATEGORY_ALL = "All Topics"
    const val CATEGORY_AKSHAR = "Akshar (ᱚᱞ ᱪᱤᱠᱤ)"
    const val CATEGORY_NUMBERS = "Numbers (ᱞᱮᱠᱷᱟ)"
    const val CATEGORY_ANIMALS = "Animals (ᱡᱤᱵᱽ ᱡᱤᱭᱟᱹᱞᱤ)"
    const val CATEGORY_NATURE = "Nature (ᱥᱤᱨᱡᱚᱱ)"
    const val CATEGORY_FRUITS = "Fruits & Food (ᱡᱚ ᱟᱨ ᱡᱚᱢᱟᱜ)"
    const val CATEGORY_COLORS = "Colors (ᱨᱚᱝ)"
    const val CATEGORY_SCHOOL_FAMILY = "School & Family (ᱟᱥᱲᱟ ᱟᱨ ᱜᱷᱟᱨᱚᱸᱡᱽ)"
    const val CATEGORY_SHAPES = "Shapes & Space (ᱨᱩᱯ ᱟᱨ ᱡᱟᱭᱜᱟ)"
    const val CATEGORY_CUSTOM = "Custom Teacher Topics (ᱢᱟᱪᱮᱛ ᱥᱟᱛᱟᱢ)"

    private val CARDS = listOf(
        // =====================================================================
        // 1. COMPLETE OL CHIKI ALPHABET (All 30 Core Letters) - NIPUN L1.1
        // =====================================================================
        // Row 1: La Series (ᱚ ᱛ ᱜ ᱝ ᱞ)
        FlnCard("ak_01", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "अ (ध्वनि /a/)", "ᱚ", "अ (La / Ah)", "Vowel /a/", "akshar", exampleSentenceHindi = "पहला अक्षर 'अ' है।", exampleSentenceSantali = "ᱯᱩᱭᱞᱩ ᱪᱤᱠᱤ ᱫᱚ 'ᱚ' ᱠᱟᱱᱟ ᱾"),
        FlnCard("ak_02", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "त (ध्वनि /at/)", "ᱛ", "अत् (At)", "Consonant /t/", "akshar"),
        FlnCard("ak_03", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "ग (ध्वनि /ag/)", "ᱜ", "अग् (Ag)", "Consonant /g/", "akshar"),
        FlnCard("ak_04", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "ङ (ध्वनि /ang/)", "ᱝ", "अं (Ang)", "Nasal /ng/", "akshar"),
        FlnCard("ak_05", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "ल (ध्वनि /al/)", "ᱞ", "अल् (Al)", "Liquid /l/", "akshar"),

        // Row 2: Aak Series (ᱟ ᱠ ᱡ ᱢ ᱣ)
        FlnCard("ak_06", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "आ (ध्वनि /aa/)", "ᱟ", "आ (Aak)", "Vowel /aa/", "akshar"),
        FlnCard("ak_07", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "क (ध्वनि /aak/)", "ᱠ", "आक् (Ak)", "Consonant /k/", "akshar"),
        FlnCard("ak_08", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "ज (ध्वनि /aaj/)", "ᱡ", "आज् (Aj)", "Consonant /j/", "akshar"),
        FlnCard("ak_09", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "म (ध्वनि /aam/)", "ᱢ", "आम् (Am)", "Consonant /m/", "akshar"),
        FlnCard("ak_10", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "व (ध्वनि /aaw/)", "ᱣ", "आव् (Aw)", "Semivowel /w/", "akshar"),

        // Row 3: Is Series (ᱤ ᱥ ᱦ ᱧ ᱨ)
        FlnCard("ak_11", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "इ (ध्वनि /i/)", "ᱤ", "इ (Is / I)", "Vowel /i/", "akshar"),
        FlnCard("ak_12", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "स (ध्वनि /is/)", "ᱥ", "इस् (Is)", "Sibilant /s/", "akshar"),
        FlnCard("ak_13", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "ह (ध्वनि /ih/)", "ᱦ", "इह् (Ih)", "Aspirate /h/", "akshar"),
        FlnCard("ak_14", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "ञ (ध्वनि /iny/)", "ᱧ", "इञ् (Iny)", "Nasal /ny/", "akshar"),
        FlnCard("ak_15", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "र (ध्वनि /ir/)", "ᱨ", "इर् (Ir)", "Liquid /r/", "akshar"),

        // Row 4: Uch Series (ᱩ ᱪ ᱫ ᱬ ᱭ)
        FlnCard("ak_16", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "उ (ध्वनि /u/)", "ᱩ", "उ (Uch / U)", "Vowel /u/", "akshar"),
        FlnCard("ak_17", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "च (ध्वनि /uch/)", "ᱪ", "उच् (Uch)", "Consonant /c/", "akshar"),
        FlnCard("ak_18", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "द (ध्वनि /ud/)", "ᱫ", "उद् (Ud)", "Consonant /d/", "akshar"),
        FlnCard("ak_19", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "ण (ध्वनि /unn/)", "ᱬ", "उण् (Unn)", "Retroflex /nn/", "akshar"),
        FlnCard("ak_20", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "य (ध्वनि /uy/)", "ᱭ", "उय् (Uy)", "Semivowel /y/", "akshar"),

        // Row 5: Ep Series (ᱮ ᱯ ᱰ ᱱ ᱲ)
        FlnCard("ak_21", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "ए (ध्वनि /e/)", "ᱮ", "ए (Ep / E)", "Vowel /e/", "akshar"),
        FlnCard("ak_22", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "प (ध्वनि /ep/)", "ᱯ", "एप् (Ep)", "Consonant /p/", "akshar"),
        FlnCard("ak_23", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "ड (ध्वनि /edd/)", "ᱰ", "एड (Edd)", "Retroflex /dd/", "akshar"),
        FlnCard("ak_24", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "न (ध्वनि /en/)", "ᱱ", "एन् (En)", "Nasal /n/", "akshar"),
        FlnCard("ak_25", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "ड़ (ध्वनि /err/)", "ᱲ", "एड़ (Err)", "Retroflex flap /rr/", "akshar"),

        // Row 6: Ot Series (ᱳ ᱴ ᱵ ᱶ ᱷ)
        FlnCard("ak_26", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "ओ (ध्वनि /o/)", "ᱳ", "ओ (Ot / O)", "Vowel /o/", "akshar"),
        FlnCard("ak_27", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "ट (ध्वनि /ot/)", "ᱴ", "ओट् (Ot)", "Retroflex /tt/", "akshar"),
        FlnCard("ak_28", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "ब (ध्वनि /ob/)", "ᱵ", "ओब् (Ob)", "Consonant /b/", "akshar"),
        FlnCard("ak_29", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "ँ (ध्वनि /ov/)", "ᱶ", "ओंव (Ov)", "Nasalized /w~/", "akshar"),
        FlnCard("ak_30", FlnDomain.LITERACY_AKSHAR, CATEGORY_AKSHAR, "L1.1", "ह (ध्वनि /oh/)", "ᱷ", "ओह् (Oh)", "Aspirate /h/", "akshar"),

        // =====================================================================
        // 2. FOUNDATIONAL NUMERACY (1 to 20) - NIPUN N1.1 / N1.2
        // =====================================================================
        FlnCard("num_01", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.1", "एक (१)", "ᱢᱤᱫ (᱑)", "मिद (Mid)", "One (1)", "number_counter", numeralValue = 1, exampleSentenceHindi = "यह एक सेब है।", exampleSentenceSantali = "ᱱᱚᱣᱟ ᱫᱚ ᱢᱤᱫᱴᱟᱝ ᱥᱮᱣ ᱠᱟᱱᱟ ᱾"),
        FlnCard("num_02", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.1", "दो (२)", "ᱵᱟᱨ (᱒)", "बार (Bar)", "Two (2)", "number_counter", numeralValue = 2, exampleSentenceHindi = "दो पक्षी उड़ रहे हैं।", exampleSentenceSantali = "ᱵᱟᱨᱭᱟ ᱪᱮᱬᱮ ᱠᱤᱱ ᱩᱰᱟᱹᱣᱜ ᱠᱟᱱᱟ ᱾"),
        FlnCard("num_03", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.1", "तीन (३)", "ᱯᱮ (᱓)", "पे (Pe)", "Three (3)", "number_counter", numeralValue = 3),
        FlnCard("num_04", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.1", "चार (४)", "ᱯᱩᱱ (᱔)", "पुन (Pun)", "Four (4)", "number_counter", numeralValue = 4),
        FlnCard("num_05", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.1", "पाँच (५)", "ᱢᱚᱬᱮ (᱕)", "मोण़े (Mone)", "Five (5)", "number_counter", numeralValue = 5),
        FlnCard("num_06", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.2", "छह (६)", "ᱛᱩᱨᱩᱭ (᱖)", "तुरुय (Turuy)", "Six (6)", "number_counter", numeralValue = 6),
        FlnCard("num_07", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.2", "सात (७)", "ᱮᱭᱟᱭ (᱗)", "एयाय (Eyay)", "Seven (7)", "number_counter", numeralValue = 7),
        FlnCard("num_08", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.2", "आठ (८)", "ᱤᱨᱟᱹᱞ (᱘)", "इरल (Iral)", "Eight (8)", "number_counter", numeralValue = 8),
        FlnCard("num_09", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.2", "नौ (९)", "ᱟᱨᱮ (᱙)", "आरे (Are)", "Nine (9)", "number_counter", numeralValue = 9),
        FlnCard("num_10", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.2", "दस (१०)", "ᱜᱮᱞ (᱑᱐)", "गेल (Gel)", "Ten (10)", "number_counter", numeralValue = 10),
        FlnCard("num_11", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.2", "ग्यारह (११)", "ᱜᱮᱞ ᱢᱤᱫ (᱑᱑)", "गेल मिद (Gel Mid)", "Eleven (11)", "number_counter", numeralValue = 11),
        FlnCard("num_12", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.2", "बारह (१२)", "ᱜᱮᱞ ᱵᱟᱨ (᱑᱒)", "गेल बार (Gel Bar)", "Twelve (12)", "number_counter", numeralValue = 12),
        FlnCard("num_13", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.2", "तेरह (१३)", "ᱜᱮᱞ ᱯᱮ (᱑᱓)", "गेल पे (Gel Pe)", "Thirteen (13)", "number_counter", numeralValue = 13),
        FlnCard("num_14", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.2", "चौदह (१४)", "ᱜᱮᱞ ᱯᱩᱱ (᱑᱔)", "गेल पुन (Gel Pun)", "Fourteen (14)", "number_counter", numeralValue = 14),
        FlnCard("num_15", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.2", "पंद्रह (१५)", "ᱜᱮᱞ ᱢᱚᱬᱮ (᱑᱕)", "गेल मोण़े (Gel Mone)", "Fifteen (15)", "number_counter", numeralValue = 15),
        FlnCard("num_16", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.2", "सोलह (१६)", "ᱜᱮᱞ ᱛᱩᱨᱩᱭ (᱑᱖)", "गेल तुरुय (Gel Turuy)", "Sixteen (16)", "number_counter", numeralValue = 16),
        FlnCard("num_17", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.2", "सत्रह (१७)", "ᱜᱮᱞ ᱮᱭᱟᱭ (᱑᱗)", "गेल एयाय (Gel Eyay)", "Seventeen (17)", "number_counter", numeralValue = 17),
        FlnCard("num_18", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.2", "अठारह (१८)", "ᱜᱮᱞ ᱤᱨᱟᱹᱞ (᱑᱘)", "गेल इरल (Gel Iral)", "Eighteen (18)", "number_counter", numeralValue = 18),
        FlnCard("num_19", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.2", "उन्नीस (१९)", "ᱜᱮᱞ ᱟᱨᱮ (᱑᱙)", "गेल आरे (Gel Are)", "Nineteen (19)", "number_counter", numeralValue = 19),
        FlnCard("num_20", FlnDomain.NUMERACY_COUNTING, CATEGORY_NUMBERS, "N1.2", "बीस (२०)", "ᱵᱟᱨ ᱜᱮᱞ (᱒᱐)", "बार गेल (Bar Gel)", "Twenty (20)", "number_counter", numeralValue = 20),

        // =====================================================================
        // 3. ANIMALS & LIVING WORLD (15 Cards) - NIPUN L2.1
        // =====================================================================
        FlnCard("an_01", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "कुत्ता", "ᱥᱮᱛᱟ", "सेता (Seta)", "Dog", "dog", exampleSentenceHindi = "कुत्ता भौंकता है।", exampleSentenceSantali = "ᱥᱮᱛᱟ ᱫᱚᱭ ᱵᱷᱩᱜᱟᱹᱜ-ᱟ ᱾"),
        FlnCard("an_02", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "गाय", "ᱜᱟᱹᱭ", "गय (Gai)", "Cow", "cow", exampleSentenceHindi = "गाय दूध देती है।", exampleSentenceSantali = "ᱜᱟᱹᱭ ᱫᱚ ᱛᱚᱣᱟᱭ ᱮᱢᱟ ᱾"),
        FlnCard("an_03", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "बकरी", "ᱢᱮᱨᱚᱢ", "मेरोम (Merom)", "Goat", "cow"),
        FlnCard("an_04", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "बिल्ली", "ᱯᱩᱥᱤ", "पुसी (Pusi)", "Cat", "cat"),
        FlnCard("an_05", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "हाथी", "ᱦᱟᱛᱤ", "हाती (Hati)", "Elephant", "elephant"),
        FlnCard("an_06", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "पक्षी (चिड़िया)", "ᱪᱮᱬᱮ", "चेण़े (Chene)", "Bird", "bird"),
        FlnCard("an_07", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "मछली", "ᱦᱟᱹᱠᱩ", "हाकु (Haku)", "Fish", "fish"),
        FlnCard("an_08", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "बाघ", "ᱛᱟᱹᱨᱩᱵ", "तारुब (Tarub)", "Tiger", "cat"),
        FlnCard("an_09", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "घोड़ा", "ᱥᱟᱫᱚᱢ", "सादोम (Sadom)", "Horse", "cow"),
        FlnCard("an_10", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "बंदर", "ᱜᱟᱹᱲᱤ", "गाड़ी (Gari)", "Monkey", "cat"),
        FlnCard("an_11", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "मोर", "ᱢᱟᱨᱟᱜ", "माराग (Marag)", "Peacock", "bird"),
        FlnCard("an_12", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "बतख", "ᱜᱮᱰᱮ", "गेडे (Gede)", "Duck", "bird"),
        FlnCard("an_13", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "मेंढक", "ᱪᱮᱨᱚ", "चेरो (Chero)", "Frog", "fish"),
        FlnCard("an_14", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "हिरण", "ᱡᱷᱤᱞ", "झिल (Jhil)", "Deer", "cow"),
        FlnCard("an_15", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "भेड़", "ᱵᱷᱤᱰᱤ", "भिडी (Bhidi)", "Sheep", "cow"),

        // =====================================================================
        // 4. NATURE, SKY & ENVIRONMENT (15 Cards) - NIPUN L2.2
        // =====================================================================
        FlnCard("na_01", FlnDomain.LITERACY_VOCAB, CATEGORY_NATURE, "L2.2", "पेड़", "ᱫᱟᱨᱮ", "दारे (Dare)", "Tree", "tree", exampleSentenceHindi = "यह एक बड़ा पेड़ है।", exampleSentenceSantali = "ᱱᱚᱣᱟ ᱫᱚ ᱢᱤᱫᱴᱟᱝ ᱢᱟᱨᱟᱝ ᱫᱟᱨᱮ ᱠᱟᱱᱟ ᱾"),
        FlnCard("na_02", FlnDomain.LITERACY_VOCAB, CATEGORY_NATURE, "L2.2", "पानी", "ᱫᱟᱜ", "दाग (Dag)", "Water", "water"),
        FlnCard("na_03", FlnDomain.LITERACY_VOCAB, CATEGORY_NATURE, "L2.2", "नदी", "ᱜᱟᱰᱟ", "गाडा (Gada)", "River", "river"),
        FlnCard("na_04", FlnDomain.LITERACY_VOCAB, CATEGORY_NATURE, "L2.2", "पहाड़", "ᱵᱩᱨᱩ", "बुरु (Buru)", "Mountain", "mountain"),
        FlnCard("na_05", FlnDomain.LITERACY_VOCAB, CATEGORY_NATURE, "L2.2", "सूरज", "ᱥᱤᱧ ᱪᱟᱸᱫᱚ", "सिञ चाँद (Sin Chando)", "Sun", "sun"),
        FlnCard("na_06", FlnDomain.LITERACY_VOCAB, CATEGORY_NATURE, "L2.2", "चाँद", "ᱧᱤᱫᱟᱹ ᱪᱟᱸᱫᱚ", "निदा चाँद (Nida Chando)", "Moon", "star"),
        FlnCard("na_07", FlnDomain.LITERACY_VOCAB, CATEGORY_NATURE, "L2.2", "तारा", "ᱤᱯᱤᱞ", "इपिल (Ipil)", "Star", "star"),
        FlnCard("na_08", FlnDomain.LITERACY_VOCAB, CATEGORY_NATURE, "L2.2", "फूल", "ᱵᱟᱦᱟ", "बाहा (Baha)", "Flower", "flower"),
        FlnCard("na_09", FlnDomain.LITERACY_VOCAB, CATEGORY_NATURE, "L2.2", "जंगल", "ᱵᱤᱨ", "बीर (Bir)", "Forest", "forest"),
        FlnCard("na_10", FlnDomain.LITERACY_VOCAB, CATEGORY_NATURE, "L2.2", "पत्ता", "ᱥᱟᱠᱟᱢ", "साकाम (Sakam)", "Leaf", "tree"),
        FlnCard("na_11", FlnDomain.LITERACY_VOCAB, CATEGORY_NATURE, "L2.2", "बादल", "ᱨᱤᱢᱤᱞ", "रिमिल (Rimil)", "Cloud", "water"),
        FlnCard("na_12", FlnDomain.LITERACY_VOCAB, CATEGORY_NATURE, "L2.2", "बारिश", "ᱫᱟᱜ ᱡᱟᱹᱲᱤ", "दाग जाड़ी (Dag Jari)", "Rain", "water"),
        FlnCard("na_13", FlnDomain.LITERACY_VOCAB, CATEGORY_NATURE, "L2.2", "आग", "ᱥᱮᱸᱜᱮᱞ", "सेंगेल (Sengel)", "Fire", "sun"),
        FlnCard("na_14", FlnDomain.LITERACY_VOCAB, CATEGORY_NATURE, "L2.2", "हवा", "ᱦᱚᱭ", "होय (Hoy)", "Wind", "water"),
        FlnCard("na_15", FlnDomain.LITERACY_VOCAB, CATEGORY_NATURE, "L2.2", "मिट्टी / भूमि", "ᱦᱟᱥᱟ", "हासा (Hasa)", "Soil / Earth", "mountain"),

        // =====================================================================
        // 5. FRUITS, CROPS & FOOD (12 Cards) - NIPUN L2.2
        // =====================================================================
        FlnCard("fr_01", FlnDomain.LITERACY_VOCAB, CATEGORY_FRUITS, "L2.2", "आम", "ᱩᱞ", "उल (Ul)", "Mango", "mango", exampleSentenceHindi = "मीठा आम खाओ।", exampleSentenceSantali = "ᱦᱮᱲᱮᱢ ᱩᱞ ᱡᱚᱢ ᱢᱮ ᱾"),
        FlnCard("fr_02", FlnDomain.LITERACY_VOCAB, CATEGORY_FRUITS, "L2.2", "सेब", "ᱥᱮᱣ", "सेव (Sew)", "Apple", "apple"),
        FlnCard("fr_03", FlnDomain.LITERACY_VOCAB, CATEGORY_FRUITS, "L2.2", "केला", "ᱠᱟᱭᱨᱟ", "कायरा (Kayra)", "Banana", "fruit"),
        FlnCard("fr_04", FlnDomain.LITERACY_VOCAB, CATEGORY_FRUITS, "L2.2", "अमरूद", "ᱟᱢᱨᱩᱫᱽ", "अमरुद (Amrud)", "Guava", "apple"),
        FlnCard("fr_05", FlnDomain.LITERACY_VOCAB, CATEGORY_FRUITS, "L2.2", "पपीता", "ᱯᱚᱯᱮ", "पोपे (Pope)", "Papaya", "mango"),
        FlnCard("fr_06", FlnDomain.LITERACY_VOCAB, CATEGORY_FRUITS, "L2.2", "संतरा", "ᱠᱚᱢᱞᱟ", "कोमला (Komla)", "Orange", "mango"),
        FlnCard("fr_07", FlnDomain.LITERACY_VOCAB, CATEGORY_FRUITS, "L2.2", "तरबूज", "ᱛᱟᱨᱵᱩᱡᱽ", "तरबुज (Tarbuj)", "Watermelon", "fruit"),
        FlnCard("fr_08", FlnDomain.LITERACY_VOCAB, CATEGORY_FRUITS, "L2.2", "चावल (भात)", "ᱫᱟᱠᱟ", "दाका (Daka)", "Rice / Food", "fruit"),
        FlnCard("fr_09", FlnDomain.LITERACY_VOCAB, CATEGORY_FRUITS, "L2.2", "रोटी / पीठा", "ᱯᱤᱴᱷᱟᱹ", "पीठा (Pitha)", "Bread / Cake", "fruit"),
        FlnCard("fr_10", FlnDomain.LITERACY_VOCAB, CATEGORY_FRUITS, "L2.2", "दूध", "ᱛᱚᱣᱟ", "तोवा (Towa)", "Milk", "water"),
        FlnCard("fr_11", FlnDomain.LITERACY_VOCAB, CATEGORY_FRUITS, "L2.2", "नमक", "ᱵᱩᱞᱩᱝ", "बुलुंग (Bulung)", "Salt", "star"),
        FlnCard("fr_12", FlnDomain.LITERACY_VOCAB, CATEGORY_FRUITS, "L2.2", "मीठा / शहद", "ᱦᱮᱲᱮᱢ", "हेड़ेम (Herem)", "Sweet / Honey", "flower"),

        // =====================================================================
        // 6. COLORS (7 Cards) - NIPUN L2.1
        // =====================================================================
        FlnCard("col_01", FlnDomain.LITERACY_VOCAB, CATEGORY_COLORS, "L2.1", "लाल", "ᱟᱨᱟᱜ", "आराग (Arag)", "Red", "flower", exampleSentenceHindi = "लाल फूल सुंदर है।", exampleSentenceSantali = "ᱟᱨᱟᱜ ᱵᱟᱦᱟ ᱫᱚ ᱪᱚᱨᱚᱠ ᱜᱮᱭᱟ ᱾"),
        FlnCard("col_02", FlnDomain.LITERACY_VOCAB, CATEGORY_COLORS, "L2.1", "हरा", "ᱦᱟᱹᱨᱭᱟᱹᱲ", "हरियाड़ (Hariyar)", "Green", "tree"),
        FlnCard("col_03", FlnDomain.LITERACY_VOCAB, CATEGORY_COLORS, "L2.1", "नीला", "ᱞᱤᱞ", "लिल (Lil)", "Blue", "water"),
        FlnCard("col_04", FlnDomain.LITERACY_VOCAB, CATEGORY_COLORS, "L2.1", "पीला", "ᱥᱟᱥᱟᱝ", "सासांग (Sasang)", "Yellow", "sun"),
        FlnCard("col_05", FlnDomain.LITERACY_VOCAB, CATEGORY_COLORS, "L2.1", "सफेद", "ᱯᱩᱸᱰ", "पुंड (Pund)", "White", "star"),
        FlnCard("col_06", FlnDomain.LITERACY_VOCAB, CATEGORY_COLORS, "L2.1", "काला", "ᱦᱮᱸᱫᱮ", "हेंदे (Hende)", "Black", "star"),
        FlnCard("col_07", FlnDomain.LITERACY_VOCAB, CATEGORY_COLORS, "L2.1", "नारंगी (गेरुआ)", "ᱜᱮᱨᱩᱣᱟ", "गेरुआ (Gerua)", "Orange Color", "mango"),

        // =====================================================================
        // 7. SCHOOL, FAMILY & VILLAGE LIFE (15 Cards) - NIPUN L2.3
        // =====================================================================
        FlnCard("sf_01", FlnDomain.LITERACY_VOCAB, CATEGORY_SCHOOL_FAMILY, "L2.3", "किताब (पुस्तक)", "ᱯᱩᱛᱷᱤ", "पुथी (Puthi)", "Book", "book"),
        FlnCard("sf_02", FlnDomain.LITERACY_VOCAB, CATEGORY_SCHOOL_FAMILY, "L2.3", "कलम (पेंसिल)", "ᱠᱚᱞᱚᱢ", "कोलम (Kolom)", "Pen / Pencil", "pencil"),
        FlnCard("sf_03", FlnDomain.LITERACY_VOCAB, CATEGORY_SCHOOL_FAMILY, "L2.3", "स्कूल (विद्यालय)", "ᱤᱛᱩᱱ ᱟᱥᱲᱟ", "इतुन आसड़ा (Itun Asra)", "School", "school"),
        FlnCard("sf_04", FlnDomain.LITERACY_VOCAB, CATEGORY_SCHOOL_FAMILY, "L2.3", "माँ (माता)", "ᱟᱭᱳ", "आयो (Ayo)", "Mother", "school"),
        FlnCard("sf_05", FlnDomain.LITERACY_VOCAB, CATEGORY_SCHOOL_FAMILY, "L2.3", "पिताजी (बापू)", "ᱵᱟᱵᱟ", "बाबा (Baba)", "Father", "school"),
        FlnCard("sf_06", FlnDomain.LITERACY_VOCAB, CATEGORY_SCHOOL_FAMILY, "L2.3", "भाई", "ᱵᱚᱭᱦᱟ", "बोयहा (Boyha)", "Brother", "school"),
        FlnCard("sf_07", FlnDomain.LITERACY_VOCAB, CATEGORY_SCHOOL_FAMILY, "L2.3", "बहन", "ᱢᱤᱥᱤ", "मिसि (Misi)", "Sister", "school"),
        FlnCard("sf_08", FlnDomain.LITERACY_VOCAB, CATEGORY_SCHOOL_FAMILY, "L2.3", "मित्र (दोस्त)", "ᱜᱟᱛᱮ", "गाते (Gate)", "Friend", "school"),
        FlnCard("sf_09", FlnDomain.LITERACY_VOCAB, CATEGORY_SCHOOL_FAMILY, "L2.3", "शिक्षक (गुरुजी)", "ᱢᱟᱪᱮᱛ", "माचेत (Machet)", "Teacher", "school"),
        FlnCard("sf_10", FlnDomain.LITERACY_VOCAB, CATEGORY_SCHOOL_FAMILY, "L2.3", "घर", "ᱚᱲᱟᱜ", "ओड़ाग (Orag)", "House / Home", "school"),
        FlnCard("sf_11", FlnDomain.LITERACY_VOCAB, CATEGORY_SCHOOL_FAMILY, "L2.3", "गाँव", "ᱟᱹᱛᱩ", "आतु (Atu)", "Village", "school"),
        FlnCard("sf_12", FlnDomain.LITERACY_VOCAB, CATEGORY_SCHOOL_FAMILY, "L2.3", "रास्ता (सड़क)", "ᱦᱚᱨ", "होर (Hor)", "Road / Path", "mountain"),
        FlnCard("sf_13", FlnDomain.LITERACY_VOCAB, CATEGORY_SCHOOL_FAMILY, "L2.3", "नगाड़ा (मांदर)", "ᱴᱟᱢᱟᱠ", "टामाक (Tamak)", "Tribal Drum", "coin"),
        FlnCard("sf_14", FlnDomain.LITERACY_VOCAB, CATEGORY_SCHOOL_FAMILY, "L2.3", "रुपया", "ᱴᱟᱠᱟ", "टाका (Taka)", "Rupee", "coin"),
        FlnCard("sf_15", FlnDomain.LITERACY_VOCAB, CATEGORY_SCHOOL_FAMILY, "L2.3", "सिक्का (पैसा)", "ᱯᱩᱭᱥᱟᱹ", "पुयसा (Puysa)", "Coin", "coin"),

        // =====================================================================
        // 8. SHAPES, SPACE & COMPARISONS (8 Cards) - NIPUN N2.1 / N2.2
        // =====================================================================
        FlnCard("sh_01", FlnDomain.NUMERACY_SHAPES, CATEGORY_SHAPES, "N2.1", "गोल (वृत्त)", "ᱜᱳᱞ", "गोल (Gol)", "Circle", "shape_circle"),
        FlnCard("sh_02", FlnDomain.NUMERACY_SHAPES, CATEGORY_SHAPES, "N2.1", "चौकोर (वर्ग)", "ᱪᱟᱹᱣᱠᱟᱹ", "चौका (Chowka)", "Square", "shape_square"),
        FlnCard("sh_03", FlnDomain.NUMERACY_SHAPES, CATEGORY_SHAPES, "N2.1", "त्रिभुज", "ᱯᱮ ᱠᱳᱬ", "पे कोण (Pe Kon)", "Triangle", "shape_triangle"),
        FlnCard("sh_04", FlnDomain.NUMERACY_SHAPES, CATEGORY_SHAPES, "N2.2", "बड़ा", "ᱢᱟᱨᱟᱝ", "मारांग (Marang)", "Big", "concept_big"),
        FlnCard("sh_05", FlnDomain.NUMERACY_SHAPES, CATEGORY_SHAPES, "N2.2", "छोटा", "ᱦᱩᱰᱤᱧ", "हुडिञ (Hudin)", "Small", "concept_small"),
        FlnCard("sh_06", FlnDomain.NUMERACY_SHAPES, CATEGORY_SHAPES, "N2.2", "ऊपर", "ᱪᱮᱛᱟᱱ", "चेतान (Chetan)", "Up", "concept_up"),
        FlnCard("sh_07", FlnDomain.NUMERACY_SHAPES, CATEGORY_SHAPES, "N2.2", "नीचे", "ᱞᱟᱛᱟᱨ", "लातार (Latar)", "Down", "concept_down"),
        FlnCard("sh_08", FlnDomain.NUMERACY_SHAPES, CATEGORY_SHAPES, "N2.2", "समान (बराबर)", "ᱥᱚᱢᱟᱱ", "सोमान (Soman)", "Equal", "coin")
    )

    private val customCards = mutableListOf<FlnCard>()

    val AI_STORYBOOK_CARDS = listOf(
        FlnCard("an_05", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "हाथी", "ᱦᱟᱹᱛᱤ", "हाति (Hati)", "Elephant", "elephant", exampleSentenceSantali = "ᱦᱟᱹᱛᱤ ᱫᱚ ᱵᱤᱨ ᱨᱤᱱᱤᱡ ᱢᱟᱨᱟᱝ ᱡᱤᱵᱽ ᱠᱟᱱᱟᱭ ᱾", exampleSentenceHindi = "हाथी जंगल का सबसे बड़ा और समझदार जानवर है।"),
        FlnCard("an_02", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "गाय", "ᱜᱟᱹᱭ", "गय (Gai)", "Cow", "cow", exampleSentenceSantali = "ᱜᱟᱹᱭ ᱫᱚ ᱟᱵᱚ ᱢᱤଠᱟᱹ ᱛᱚᱣᱟᱭ ᱮᱢᱟᱵᱚᱱᱟ ᱾", exampleSentenceHindi = "गाय हमें मीठा और पौष्टिक दूध देती है।"),
        FlnCard("an_01", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "कुत्ता", "ᱥᱮᱛᱟ", "सेता (Seta)", "Dog", "dog", exampleSentenceSantali = "ᱥᱮᱛᱟ ᱫᱚ ᱟᱹᱰᱤ ᱵᱩᱫᱷᱤᱢᱟᱱ ᱟᱨ ᱜᱟᱛᱮ ᱡᱤᱵᱽ ᱠᱟᱱᱟᱭ ᱾", exampleSentenceHindi = "कुत्ता बहुत वफ़ादार और सच्चा मित्र होता है।"),
        FlnCard("an_04", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "बिल्ली", "ᱯᱩᱥᱤ", "पुसी (Pusi)", "Cat", "cat", exampleSentenceSantali = "ᱯᱩᱥᱤ ᱫᱚ ᱛᱚᱣᱟ ᱧᱩ ᱟᱹᱰᱤ ᱠᱩᱥᱤᱭᱟᱜ-ᱟ ᱾", exampleSentenceHindi = "बिल्ली दूध पीना और खेलना बहुत पसंद करती है।"),
        FlnCard("an_06", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "पक्षी (चिड़िया)", "ᱪᱮᱬᱮ", "चेण़े (Chene)", "Bird", "bird", exampleSentenceSantali = "ᱪᱮᱬᱮ ᱫᱚ ᱥᱮᱨᱢᱟ ᱨᱮ ᱪᱚᱨᱚᱠ ᱮ ᱩᱰᱟᱹᱣᱜ-ᱟ ᱾", exampleSentenceHindi = "चिड़िया नीले आकाश में सुंदर उड़ती है।"),
        FlnCard("an_07", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "मछली", "ᱦᱟᱹᱠᱩ", "हाकु (Haku)", "Fish", "fish", exampleSentenceSantali = "ᱦᱟᱹᱠᱩ ᱫᱚ ᱱᱤᱨᱢᱚᱲ ᱫᱟᱜ ᱨᱮᱠᱚ ᱯᱟᱭᱨᱟᱜ-ᱟ ᱾", exampleSentenceHindi = "मछली साफ़ और ठंडे पानी में तैरती है।"),
        FlnCard("an_11", FlnDomain.LITERACY_VOCAB, CATEGORY_ANIMALS, "L2.1", "मोर", "ᱢᱟᱨᱟᱜ", "माराग (Marag)", "Peacock", "bird", exampleSentenceSantali = "ᱢᱟᱨᱟᱜ ᱫᱚ ᱫᱟᱜ ᱡᱟᱹᱲᱤ ᱨᱮ ᱪᱚᱨᱚᱠ ᱮ ᱮᱱᱮᱡ-ᱟ ᱾", exampleSentenceHindi = "मोर बारिश में सुंदर पंख फैलाकर नाचता है।"),
        FlnCard("fr_01", FlnDomain.LITERACY_VOCAB, CATEGORY_FRUITS, "L2.2", "आम", "ᱩᱞ", "उल (Ul)", "Mango", "mango", exampleSentenceSantali = "ᱦᱮᱲᱮᱢ ᱩᱞ ᱡᱚᱢ ᱛᱮ ᱟᱹᱰᱤ ᱨᱟᱹᱥᱠᱟᱹ ᱾", exampleSentenceHindi = "मीठा पका आम खाने में बहुत स्वादिष्ट लगता है।"),
        FlnCard("na_01", FlnDomain.LITERACY_VOCAB, CATEGORY_NATURE, "L2.2", "पेड़", "ᱫᱟᱨᱮ", "दारे (Dare)", "Tree", "tree", exampleSentenceSantali = "ᱫᱟᱨᱮ ᱫᱚ ᱟᱵᱚ ᱪᱷᱟᱸᱭ ᱟᱨ ᱦᱚᱭ ᱮᱢᱟᱵᱚᱱᱟ ᱾", exampleSentenceHindi = "पेड़ हमें शीतल छाया और ताज़ी हवा देता है।"),
        FlnCard("na_05", FlnDomain.LITERACY_VOCAB, CATEGORY_NATURE, "L2.2", "सूरज", "ᱥᱤᱧ ᱪᱟᱸᱫᱚ", "सिञ चाँद (Sin Chando)", "Sun", "sun", exampleSentenceSantali = "ᱥᱤᱧ ᱪᱟᱸᱫᱚ ᱫᱚ ᱫᱷᱟᱹᱨᱛᱤ ᱨᱮ ᱢᱟᱨᱥᱟᱞ ᱮ ᱮᱢᱟ ᱾", exampleSentenceHindi = "सूरज पूरी धरती को सवेरे रोशनी और गरमी देता है।"),
        FlnCard("na_08", FlnDomain.LITERACY_VOCAB, CATEGORY_NATURE, "L2.2", "फूल", "ᱵᱟᱦᱟ", "बाहा (Baha)", "Flower", "flower", exampleSentenceSantali = "ᱵᱟᱦᱟ ᱨᱮᱱᱟᱜ ᱥᱚ ᱟᱹᱰᱤ ᱪᱚᱨᱚᱠ ᱜᱮᱭᱟ ᱾", exampleSentenceHindi = "फूलों की खुशबू और रंग मन को मोह लेते हैं।"),
        FlnCard("sf_03", FlnDomain.LITERACY_VOCAB, CATEGORY_SCHOOL_FAMILY, "L2.3", "स्कूल (विद्यालय)", "ᱤᱛᱩᱱ ᱟᱥᱲᱟ", "इतुन आसड़ा (Itun Asra)", "School", "school", exampleSentenceSantali = "ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ ᱤᱛᱩᱱ ᱟᱥᱲᱟ ᱨᱮ ᱯᱟᱲᱦᱟᱣ ᱠᱚ ᱪᱟᱞᱟᱜ-ᱟ ᱾", exampleSentenceHindi = "बच्चे विद्यालय में पढ़ने और नए दोस्त बनाने जाते हैं।"),
        FlnCard("sf_13", FlnDomain.LITERACY_VOCAB, CATEGORY_SCHOOL_FAMILY, "L2.3", "नगाड़ा (मांदर/ढोल)", "ᱴᱟᱢᱟᱠ", "टामाक (Tamak)", "Tribal Drum", "coin", exampleSentenceSantali = "ᱴᱟᱢᱟᱠ ᱨᱩ ᱟᱸᱡᱚᱢ ᱛᱮ ᱥᱟᱱᱟᱢ ᱦᱚᱲ ᱮᱱᱮᱡ ᱠᱚ ᱮᱦᱚᱵ-ᱟ ᱾", exampleSentenceHindi = "टामाक की ताल सुनते ही सब खुशी से नाच उठते हैं।")
    )

    fun getAllCards(): List<FlnCard> = CARDS + customCards

    fun getFlashcards(): List<FlnCard> = AI_STORYBOOK_CARDS + customCards

    fun getCategories(): List<String> = listOf(
        CATEGORY_ALL,
        CATEGORY_AKSHAR,
        CATEGORY_NUMBERS,
        CATEGORY_ANIMALS,
        CATEGORY_NATURE,
        CATEGORY_FRUITS,
        CATEGORY_COLORS,
        CATEGORY_SCHOOL_FAMILY,
        CATEGORY_SHAPES,
        CATEGORY_CUSTOM
    )

    fun getFlashcardCategories(): List<String> = listOf(
        CATEGORY_ALL,
        CATEGORY_ANIMALS,
        CATEGORY_NATURE,
        CATEGORY_FRUITS,
        CATEGORY_SCHOOL_FAMILY,
        CATEGORY_CUSTOM
    )

    fun getCardsByCategory(category: String): List<FlnCard> {
        val all = getAllCards()
        return if (category == CATEGORY_ALL) {
            all
        } else {
            all.filter { it.category == category }
        }
    }

    fun getFlashcardsByCategory(category: String): List<FlnCard> {
        val all = getFlashcards()
        return if (category == CATEGORY_ALL) {
            all
        } else {
            all.filter { it.category == category }
        }
    }

    fun addCustomCard(card: FlnCard) {
        customCards.add(0, card)
    }

    /**
     * Algorithmic generation of bilingual worksheet problem sets via ProceduralCurriculumGenerator.
     */
    fun generateWorksheet(config: WorksheetConfig): List<WorksheetItem> {
        return org.tribetalk.fln.generator.ProceduralCurriculumGenerator.generate(config)
    }
}
