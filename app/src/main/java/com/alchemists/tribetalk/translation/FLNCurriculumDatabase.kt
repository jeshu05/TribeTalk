package com.alchemists.tribetalk.translation

/**
 * High-Coverage FLN (Foundational Literacy and Numeracy) Curriculum & NIPUN Bharat Database.
 * Seeded with authentic vocabulary from JCERT MTB-MLE primary readers, BPCC, and CIIL corpora.
 *
 * Provides triple-script representation:
 *  1. Hindi (source or target for teacher)
 *  2. Santali in Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ) - Official script for students
 *  3. Phonetic Devanagari Guide (for teacher HUD pronunciation)
 *  4. Latin Phonetics (standard romanized Santali)
 */
data class FLNItem(
    val hindi: String,
    val santaliOlChiki: String,
    val phoneticDevanagari: String,
    val latinPhonetic: String,
    val category: String,
    val alternativesHindi: List<String> = emptyList(),
    val alternativesSantali: List<String> = emptyList()
)

object FLNCurriculumDatabase {

    val items: List<FLNItem> = listOf(
        // ==========================================
        // 1. GREETINGS & POLITE SOCIAL FORMULAS
        // ==========================================
        FLNItem(
            hindi = "नमस्ते",
            santaliOlChiki = "ᱡᱚᱦᱟᱨ",
            phoneticDevanagari = "जोहार (Johar)",
            latinPhonetic = "Johar",
            category = "Greetings",
            alternativesHindi = listOf("हेलो", "हैलो", "प्रणाम", "नमस्कार"),
            alternativesSantali = listOf("Johar", "जोहार")
        ),
        FLNItem(
            hindi = "मेरा नाम",
            santaliOlChiki = "ᱤᱧᱟᱜ ᱧᱩᱛᱩᱢ",
            phoneticDevanagari = "इञाग ञुतुम (Inyag nyutum)",
            latinPhonetic = "Inyag nyutum",
            category = "Greetings",
            alternativesHindi = listOf("मेरा नाम है", "मेरा नाम"),
            alternativesSantali = listOf("Inyag nyutum", "इञाग ञुतुम")
        ),
        FLNItem(
            hindi = "मेरा",
            santaliOlChiki = "ᱤᱧᱟᱜ",
            phoneticDevanagari = "इञाग (Inyag)",
            latinPhonetic = "Inyag",
            category = "Greetings",
            alternativesHindi = listOf("मेरी", "मेरे"),
            alternativesSantali = listOf("Inyag", "इञाग")
        ),
        FLNItem(
            hindi = "नाम",
            santaliOlChiki = "ᱧᱩᱛᱩᱢ",
            phoneticDevanagari = "ञुतुम (Nyutum)",
            latinPhonetic = "Nyutum",
            category = "Greetings",
            alternativesHindi = listOf("नाम"),
            alternativesSantali = listOf("Nyutum", "ञुतुम")
        ),
        FLNItem(
            hindi = "आप कैसे हैं?",
            santaliOlChiki = "ᱪᱮᱠᱟ ᱢᱮᱱᱟᱢᱟ?",
            phoneticDevanagari = "चेका मेनामा? (Ceka menama?)",
            latinPhonetic = "Ceka menama?",
            category = "Greetings",
            alternativesHindi = listOf("आप कैसे हैं", "तुम कैसे हो", "कैसा हाल है"),
            alternativesSantali = listOf("Ceka menama", "चेका मेनामा")
        ),
        FLNItem(
            hindi = "मैं ठीक हूँ",
            santaliOlChiki = "ᱤᱧ ᱵᱮ Bes ᱢᱮᱱᱟᱹᱧᱟ",
            phoneticDevanagari = "इञ बेस मेनाञा (Iny bes menanya)",
            latinPhonetic = "Iny bes menanya",
            category = "Greetings",
            alternativesHindi = listOf("मैं ठीक हूं", "हम ठीक हैं"),
            alternativesSantali = listOf("Iny bes menana", "In bes menanya")
        ),
        FLNItem(
            hindi = "आपका नाम क्या है?",
            santaliOlChiki = "ᱟᱢᱟᱜ ᱧᱩᱛᱩᱢ ᱫᱚ ᱪᱮᱫ?",
            phoneticDevanagari = "आमाग ञुतुम दो चेत? (Amag nyutum do cet?)",
            latinPhonetic = "Amag nyutum do cet?",
            category = "Greetings",
            alternativesHindi = listOf("तुम्हारा नाम क्या है", "आपका क्या नाम है"),
            alternativesSantali = listOf("Amag nyutum do cet", "Amak nutum do cet")
        ),
        FLNItem(
            hindi = "धन्यवाद",
            santaliOlChiki = "ᱥᱟᱨᱦᱟᱣ",
            phoneticDevanagari = "सारहाव (Sarhaw)",
            latinPhonetic = "Sarhaw",
            category = "Greetings",
            alternativesHindi = listOf("शुक्रिया", "बहुत धन्यवाद"),
            alternativesSantali = listOf("Sarhaw", "सारहाव")
        ),
        FLNItem(
            hindi = "फिर मिलेंगे",
            santaliOlChiki = "ᱟᱨᱦᱚᱸ ᱵᱚᱱ ᱧᱟᱯᱟᱢᱟ",
            phoneticDevanagari = "आरहो बोन ञापामा (Arho bon nyapama)",
            latinPhonetic = "Arho bon nyapama",
            category = "Greetings",
            alternativesHindi = listOf("अलविदा", "बाद में मिलेंगे"),
            alternativesSantali = listOf("Arho bon napama", "Arhon bon nyapama")
        ),

        // ==========================================
        // 2. CLASSROOM COMMANDS & PEDAGOGICAL PROMPTS
        // ==========================================
        FLNItem(
            hindi = "चलो पढ़ते हैं",
            santaliOlChiki = "ᱫᱮᱞᱟ ᱵᱚᱱ ᱯᱟᱲᱦᱟᱣ-ᱟ",
            phoneticDevanagari = "देला बोन पढ़ाओ-आ (Dela bon paṛhao-a)",
            latinPhonetic = "Dela bon paṛhao-a",
            category = "Classroom Instruction",
            alternativesHindi = listOf("चलो पढ़ते हैं", "आओ पढ़ें", "पढ़ाई शुरू करो"),
            alternativesSantali = listOf("Dela bon parhao-a", "देला बोन पढाओ-आ")
        ),
        FLNItem(
            hindi = "किताब खोलो",
            santaliOlChiki = "ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ",
            phoneticDevanagari = "पुथी झीज मे (Puthī jhije me)",
            latinPhonetic = "Puthī jhije me",
            category = "Classroom Instruction",
            alternativesHindi = listOf("पुस्तक खोलो", "अपनी किताब खोलो"),
            alternativesSantali = listOf("Puthi jhije me", "पुथी झिजे मे")
        ),
        FLNItem(
            hindi = "किताब बंद करो",
            santaliOlChiki = "ᱯᱩᱛᱷᱤ ᱵᱚᱸᱫᱽ ᱢᱮ",
            phoneticDevanagari = "पुथी बंद मे (Puthī bond me)",
            latinPhonetic = "Puthī bond me",
            category = "Classroom Instruction",
            alternativesHindi = listOf("पुस्तक बंद करो", "किताब बंद कीजिए"),
            alternativesSantali = listOf("Puthi bond me", "Puthi bondh me")
        ),
        FLNItem(
            hindi = "लिखना शुरू करो",
            santaliOlChiki = "ᱚᱞ ᱮᱦᱚᱵ ᱢᱮ",
            phoneticDevanagari = "ओल एहोब मे (Ol ehob me)",
            latinPhonetic = "Ol ehob me",
            category = "Classroom Instruction",
            alternativesHindi = listOf("लिखना शुरू कीजिए", "लिखो", "लिखना आरंभ करो"),
            alternativesSantali = listOf("Ol eho b me", "ओल एहोब मे")
        ),
        FLNItem(
            hindi = "शांत रहो",
            santaliOlChiki = "ᱛᱷᱤᱨ ᱛᱟᱦᱮᱸᱱ ᱯᱮ",
            phoneticDevanagari = "थिर ताहेन पे (Thir tahen pe)",
            latinPhonetic = "Thir tahen pe",
            category = "Classroom Instruction",
            alternativesHindi = listOf("चुप रहो", "आवाज़ मत करो", "शोर मत करो"),
            alternativesSantali = listOf("Thir tahen pe", "थिर ताहेन पे")
        ),
        FLNItem(
            hindi = "बैठ जाओ",
            santaliOlChiki = "ᱫᱩᱲᱩᱵ ᱢᱮ",
            phoneticDevanagari = "दुरुप मे (Durup me)",
            latinPhonetic = "Durup me",
            category = "Classroom Instruction",
            alternativesHindi = listOf("बैठिये", "अपनी जगह पर बैठो"),
            alternativesSantali = listOf("Durup me", "दुरुप मे")
        ),
        FLNItem(
            hindi = "खड़े हो जाओ",
            santaliOlChiki = "ᱛᱤᱸᱜᱩᱱ ᱢᱮ",
            phoneticDevanagari = "तिंगुन मे (Tingun me)",
            latinPhonetic = "Tingun me",
            category = "Classroom Instruction",
            alternativesHindi = listOf("खड़े हो जाओ", "खड़ा हो जाओ"),
            alternativesSantali = listOf("Tingun me", "तिंगुन मे")
        ),
        FLNItem(
            hindi = "यहाँ आओ",
            santaliOlChiki = "ᱱᱚᱰᱮ ᱦᱤᱡᱩᱜ ᱢᱮ",
            phoneticDevanagari = "नोडे हिजुक मे (Node hijuk me)",
            latinPhonetic = "Node hijuk me",
            category = "Classroom Instruction",
            alternativesHindi = listOf("इधर आओ", "यहाँ आइए"),
            alternativesSantali = listOf("Node hijuk me", "नोडे हिजुक मे")
        ),
        FLNItem(
            hindi = "ब्लैकबोर्ड देखो",
            santaliOlChiki = "ᱵᱚᱨᱰ ᱧᱮᱞ ᱢᱮ",
            phoneticDevanagari = "बोर्ड नेल मे (Bord nel me)",
            latinPhonetic = "Bord nel me",
            category = "Classroom Instruction",
            alternativesHindi = listOf("बोर्ड पर देखो", "श्यामपट्ट देखो"),
            alternativesSantali = listOf("Bord nel me", "Blackboard nel me")
        ),
        FLNItem(
            hindi = "ध्यान से सुनो",
            santaliOlChiki = "ᱫᱷᱮᱭᱟᱱ ᱛᱮ ᱟᱧᱡᱚᱢ ᱢᱮ",
            phoneticDevanagari = "ध्येयान ते आंजोम मे (Dheyan te anjom me)",
            latinPhonetic = "Dheyan te anjom me",
            category = "Classroom Instruction",
            alternativesHindi = listOf("गौर से सुनो", "अच्छे से सुनो"),
            alternativesSantali = listOf("Dheyan te anjom me", "Anjom me")
        ),
        FLNItem(
            hindi = "बहुत अच्छा",
            santaliOlChiki = "ᱟᱹᱰᱤ ᱱᱟᱯᱟᱭ",
            phoneticDevanagari = "आडी नापाय (Adi napay)",
            latinPhonetic = "Adi napay",
            category = "Classroom Instruction",
            alternativesHindi = listOf("शाबाश", "बढ़िया", "उत्कृष्ट"),
            alternativesSantali = listOf("Adi napay", "Sarhaw")
        ),

        // ==========================================
        // 3. STUDENT DOUBTS & DAILY NEEDS
        // ==========================================
        FLNItem(
            hindi = "मुझे समझ नहीं आया",
            santaliOlChiki = "ᱵᱟᱹᱧ ᱵᱩᱡᱷᱟᱹᱣ ᱫᱟᱲᱮᱭᱟᱫᱟ",
            phoneticDevanagari = "बाँ बुझाउ दड़ेआदा (Bañ bujhau daṛeada)",
            latinPhonetic = "Bañ bujhau daṛeada",
            category = "Student Doubt",
            alternativesHindi = listOf("समझ में नहीं आया", "मैं समझा नहीं", "हमको समझ नहीं आया"),
            alternativesSantali = listOf("Bañ bujhau daṛeada", "Ban bujhau dareada")
        ),
        FLNItem(
            hindi = "क्या आप मदद कर सकते हैं?",
            santaliOlChiki = "ᱪᱮᱫ ᱟᱢ ᱜᱚᱲᱚ ᱫᱟᱲᱮᱭᱟᱹᱧᱟ?",
            phoneticDevanagari = "चेत आम गोड़ो दड़ेआङा? (Cet aam goṛo daṛeaña?)",
            latinPhonetic = "Cet aam goṛo daṛeaña?",
            category = "Student Doubt",
            alternativesHindi = listOf("मदद कीजिए", "क्या आप सहायता कर सकते हैं?"),
            alternativesSantali = listOf("Cet aam goro dareana", "Cet' aam goṛo daṛeaña")
        ),
        FLNItem(
            hindi = "पानी पीना है",
            santaliOlChiki = "ᱫᱟᱜ ᱧᱩ ᱥᱟᱱᱟᱹᱧ ᱠᱟᱱᱟ",
            phoneticDevanagari = "दाग न्यु सानाँ काना (Dag nyu sanan kana)",
            latinPhonetic = "Dag nyu sanan kana",
            category = "Student Doubt",
            alternativesHindi = listOf("मुझे पानी पीना है", "पानी पीने जाऊं?"),
            alternativesSantali = listOf("Dag nyu sanan kana", "Dak nu sanan kana")
        ),
        FLNItem(
            hindi = "मुझे भूख लगी है",
            santaliOlChiki = "ᱤᱧ ᱨᱮᱸᱜᱮᱡ ᱠᱟᱱᱟ",
            phoneticDevanagari = "इञ रेंगेज काना (Iny rengej kana)",
            latinPhonetic = "Iny rengej kana",
            category = "Student Doubt",
            alternativesHindi = listOf("भूख लगी है", "खाना खाना है"),
            alternativesSantali = listOf("In rengej kana", "Iny rengej kana")
        ),
        FLNItem(
            hindi = "फिर से समझाइए",
            santaliOlChiki = "ᱟᱨᱦᱚᱸ ᱵᱩᱡᱷᱟᱹᱣ ᱢᱮ",
            phoneticDevanagari = "आरहो बुझाउ मे (Arho bujhau me)",
            latinPhonetic = "Arho bujhau me",
            category = "Student Doubt",
            alternativesHindi = listOf("एक बार फिर बताइए", "दोबारा समझाइए"),
            alternativesSantali = listOf("Arho bujhau me", "Arhon bujhau me")
        ),
        FLNItem(
            hindi = "मेरा काम पूरा हो गया",
            santaliOlChiki = "ᱤᱧᱟᱜ ᱠᱟᱹᱢᱤ ᱯᱩᱨᱟᱹᱣ ᱮᱱᱟ",
            phoneticDevanagari = "इञाक कामी पुराव एना (Inyak kami puraw ena)",
            latinPhonetic = "Inyak kami puraw ena",
            category = "Student Doubt",
            alternativesHindi = listOf("काम खत्म हो गया", "मैंने पूरा कर लिया"),
            alternativesSantali = listOf("Inyak kami puraw ena", "Kami puraw ena")
        ),

        // ==========================================
        // 4. NUMBERS & FOUNDATIONAL NUMERACY (FLN)
        // ==========================================
        FLNItem(
            hindi = "एक दो तीन चार",
            santaliOlChiki = "ᱢᱤᱫ ᱵᱟᱨ ᱯᱮ ᱯᱩᱱ",
            phoneticDevanagari = "मित बार पे पुन (Mit bar pe pon)",
            latinPhonetic = "Mit bar pe pon",
            category = "Numbers",
            alternativesHindi = listOf("१ २ ३ ४", "1 2 3 4"),
            alternativesSantali = listOf("Mit' bar pe pon", "Mit bar pe pun")
        ),
        FLNItem(
            hindi = "पांच छह सात आठ नौ दस",
            santaliOlChiki = "ᱢᱚᱬᱮ ᱛᱩᱨᱩᱭ ᱮᱭᱟᱭ ᱤᱨᱟᱹᱞ ᱟᱨᱮ ᱜᱮᱞ",
            phoneticDevanagari = "मोणे तुरुय एयाय इरल अरे गेल (Mone turuy eyay iral are gel)",
            latinPhonetic = "Mone turuy eyay iral are gel",
            category = "Numbers",
            alternativesHindi = listOf("५ ६ ७ ८ ९ १०", "5 6 7 8 9 10"),
            alternativesSantali = listOf("Mone turuy eyay iral are gel")
        ),
        FLNItem(
            hindi = "गिनती करो",
            santaliOlChiki = "ᱞᱮᱠᱷᱟᱭ ᱢᱮ",
            phoneticDevanagari = "लेखाय मे (Lekhay me)",
            latinPhonetic = "Lekhay me",
            category = "Numbers",
            alternativesHindi = listOf("गिनो", "संख्या गिनो"),
            alternativesSantali = listOf("Lekhay me", "Lekha me")
        ),
        FLNItem(
            hindi = "जोड़ो",
            santaliOlChiki = "ᱢᱮᱥᱟᱭ ᱢᱮ",
            phoneticDevanagari = "मेसाय मे (Mesay me)",
            latinPhonetic = "Mesay me",
            category = "Numbers",
            alternativesHindi = listOf("जोड़", "योग करो"),
            alternativesSantali = listOf("Mesay me", "Jod me")
        ),
        FLNItem(
            hindi = "घटाओ",
            santaliOlChiki = "ᱜᱷᱟᱴᱟᱣ ᱢᱮ",
            phoneticDevanagari = "घाटाव मे (Ghataw me)",
            latinPhonetic = "Ghataw me",
            category = "Numbers",
            alternativesHindi = listOf("घटाव करो", "माइनस करो"),
            alternativesSantali = listOf("Ghataw me", "Ghatao me")
        ),
        FLNItem(
            hindi = "कितने हैं?",
            santaliOlChiki = "ᱛᱤᱱᱟᱹᱜ ᱢᱮᱱᱟᱜ-ᱟ?",
            phoneticDevanagari = "तिनाग मेनाग-आ? (Tinag menag-a?)",
            latinPhonetic = "Tinag menag-a?",
            category = "Numbers",
            alternativesHindi = listOf("कितना है?", "गिनती कितनी है?"),
            alternativesSantali = listOf("Tinag menaga", "Tinag menag-a")
        ),

        // ==========================================
        // 5. VOCABULARY: ROLES, OBJECTS & NATURE
        // ==========================================
        FLNItem(
            hindi = "शिक्षक",
            santaliOlChiki = "ᱢᱟᱪᱮᱛ",
            phoneticDevanagari = "माचेत (Macet)",
            latinPhonetic = "Macet",
            category = "Basic Vocabulary",
            alternativesHindi = listOf("गुरुजी", "अध्यापक", "सर"),
            alternativesSantali = listOf("Macet", "Macet'")
        ),
        FLNItem(
            hindi = "छात्र",
            santaliOlChiki = "ᱯᱟᱹᱲᱦᱩᱣᱟᱹ",
            phoneticDevanagari = "पढ़ुआ (Parhua)",
            latinPhonetic = "Parhua",
            category = "Basic Vocabulary",
            alternativesHindi = listOf("विद्यार्थी", "बच्चे"),
            alternativesSantali = listOf("Parhua", "Paṛhua")
        ),
        FLNItem(
            hindi = "स्कूल",
            santaliOlChiki = "ᱤᱛᱩᱱ ᱚᱲᱟᱜ",
            phoneticDevanagari = "इतुन ओड़ाग (Itun orah)",
            latinPhonetic = "Itun orah",
            category = "Basic Vocabulary",
            alternativesHindi = listOf("विद्यालय", "पाठशाला"),
            alternativesSantali = listOf("Itun orah", "Itun oṛaḥ")
        ),
        FLNItem(
            hindi = "किताब",
            santaliOlChiki = "ᱯᱩᱛᱷᱤ",
            phoneticDevanagari = "पुथी (Puthi)",
            latinPhonetic = "Puthi",
            category = "Basic Vocabulary",
            alternativesHindi = listOf("पुस्तक"),
            alternativesSantali = listOf("Puthi", "Pustak")
        ),
        FLNItem(
            hindi = "कॉपी",
            santaliOlChiki = "ᱠᱷᱟᱛᱟ",
            phoneticDevanagari = "खाता (Khata)",
            latinPhonetic = "Khata",
            category = "Basic Vocabulary",
            alternativesHindi = listOf("नोटबुक", "अभ्यास पुस्तिका"),
            alternativesSantali = listOf("Khata")
        ),
        FLNItem(
            hindi = "कलम",
            santaliOlChiki = "ᱠᱚᱞᱚᱢ",
            phoneticDevanagari = "कोलोम (Kolom)",
            latinPhonetic = "Kolom",
            category = "Basic Vocabulary",
            alternativesHindi = listOf("पेन", "पेंसिल"),
            alternativesSantali = listOf("Kolom")
        ),
        FLNItem(
            hindi = "पानी",
            santaliOlChiki = "ᱫᱟᱜ",
            phoneticDevanagari = "दाग (Dag)",
            latinPhonetic = "Dag",
            category = "Basic Vocabulary",
            alternativesHindi = listOf("जल"),
            alternativesSantali = listOf("Dag", "Dak")
        ),
        FLNItem(
            hindi = "खाना",
            santaliOlChiki = "ᱫᱟᱠᱟ",
            phoneticDevanagari = "दाका (Daka)",
            latinPhonetic = "Daka",
            category = "Basic Vocabulary",
            alternativesHindi = listOf("भात", "भोजन"),
            alternativesSantali = listOf("Daka")
        ),
        FLNItem(
            hindi = "पेड़",
            santaliOlChiki = "ᱫᱟᱨᱮ",
            phoneticDevanagari = "दारे (Dare)",
            latinPhonetic = "Dare",
            category = "Basic Vocabulary",
            alternativesHindi = listOf("वृक्ष", "पौधा"),
            alternativesSantali = listOf("Dare")
        ),
        FLNItem(
            hindi = "सूरज",
            santaliOlChiki = "ᱥᱤᱸᱜᱤ",
            phoneticDevanagari = "सिंगी (Singi)",
            latinPhonetic = "Singi",
            category = "Basic Vocabulary",
            alternativesHindi = listOf("सूर्य", "धूप"),
            alternativesSantali = listOf("Singi")
        ),
        FLNItem(
            hindi = "चाँद",
            santaliOlChiki = "ᱪᱟᱸᱫᱚ",
            phoneticDevanagari = "चांदो (Chando)",
            latinPhonetic = "Chando",
            category = "Basic Vocabulary",
            alternativesHindi = listOf("चांद", "चंद्रमा"),
            alternativesSantali = listOf("Chando")
        ),
        FLNItem(
            hindi = "घर",
            santaliOlChiki = "ᱚᱲᱟᱜ",
            phoneticDevanagari = "ओड़ाग (Orah)",
            latinPhonetic = "Orah",
            category = "Basic Vocabulary",
            alternativesHindi = listOf("मकान"),
            alternativesSantali = listOf("Orah", "Orak")
        )
    )

    fun getCount(): Int = items.size

    fun findByHindi(query: String): FLNItem? {
        val normalized = query.trim().lowercase().replace(Regex("[?.!,]"), "")
        return items.firstOrNull { item ->
            item.hindi.lowercase().replace(Regex("[?.!,]"), "") == normalized ||
            item.alternativesHindi.any { it.lowercase().replace(Regex("[?.!,]"), "") == normalized }
        }
    }

    fun findBySantali(query: String): FLNItem? {
        val normalized = query.trim().lowercase().replace(Regex("[?.!,]"), "")
        return items.firstOrNull { item ->
            item.santaliOlChiki.replace(Regex("[?.!,]"), "").trim() == query.trim().replace(Regex("[?.!,]"), "") ||
            item.latinPhonetic.lowercase().replace(Regex("[?.!,]"), "") == normalized ||
            item.alternativesSantali.any { it.lowercase().replace(Regex("[?.!,]"), "") == normalized }
        }
    }
}
