package org.tribetalk.worksheet.curriculum

import org.tribetalk.worksheet.QuestionType
import org.tribetalk.worksheet.SantaliVerificationStatus

/**
 * Authoritative Central Curriculum Registry for TribeTalk.
 * Grounded in NCF-FS 2022, NIPUN Bharat Guidelines, and NCERT Vidya Pravesh.
 *
 * Covers all 6 stages of the Foundational Stage Continuum:
 * - Pre-School 1 (Age 3–4)
 * - Pre-School 2 (Age 4–5)
 * - Pre-School 3 / Balvatika (Age 5–6)
 * - Grade 1 (Age 6–7)
 * - Grade 2 (Age 7–8)
 * - Grade 3 (Age 8–9)
 *
 * Strictly models:
 * Stage -> Domain (Pancha Kosha + Habits) -> Curricular Goal (CG-1 to CG-13) ->
 * Competency (C-1.1 to C-13.1) -> Learning Outcome -> Worksheet Activity.
 */
object NipunCurriculumRegistry {

    private val items: List<CurriculumItem> = listOf(
        // =========================================================================
        // PRE-SCHOOL 1 (Ages 3–4)
        // =========================================================================
        CurriculumItem(
            id = "PS1-LANG-CG8-C81-01",
            stage = FoundationalStage.PRE_SCHOOL_1,
            domain = CurriculumDomain.LANGUAGE_LITERACY,
            curricularGoalId = "CG-8",
            curricularGoalTitle = "Effective Oral Communication in Everyday Language",
            competencyId = "C-8.1",
            competencyTitle = "Listens attentively to familiar sounds, rhymes, and short stories",
            learningOutcomeId = "LO-PS1-LANG-01",
            learningOutcomeText = "Responds to familiar animal sounds and nature rhymes through gestures and imitation",
            suitability = WorksheetSuitability.ACTIVITY_BASED,
            supportedActivityTypes = emptyList(),
            difficultyBand = "Foundational Early",
            culturalTheme = "Animals",
            verificationStatus = SantaliVerificationStatus.VERIFIED
        ),
        CurriculumItem(
            id = "PS1-LANG-CG9-C91-01",
            stage = FoundationalStage.PRE_SCHOOL_1,
            domain = CurriculumDomain.LANGUAGE_LITERACY,
            curricularGoalId = "CG-9",
            curricularGoalTitle = "Early Emergent Literacy & Script Meaning Association",
            competencyId = "C-9.1",
            competencyTitle = "Associates visual images with spoken words for common objects",
            learningOutcomeId = "LO-PS1-LANG-02",
            learningOutcomeText = "Identifies familiar living creatures from pictures and names them in mother tongue / Hindi",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.PICTURE_IDENTIFICATION, QuestionType.MATCHING),
            difficultyBand = "Foundational Early",
            hindiPrompt = "चित्र देखकर पहचानो: गाय (Cow)",
            santaliOlChiki = "ᱜᱟᱹᱭ (Gại)",
            phoneticGuide = "गाई",
            englishGloss = "Cow",
            options = listOf("गाय", "बकरी", "चिड़िया"),
            correctAnswer = "गाय (ᱜᱟᱹᱭ)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Animals",
            visualAssetRef = "cow"
        ),
        CurriculumItem(
            id = "PS1-NUM-CG7-C71-01",
            stage = FoundationalStage.PRE_SCHOOL_1,
            domain = CurriculumDomain.NUMERACY,
            curricularGoalId = "CG-7",
            curricularGoalTitle = "Number Sense & Pre-Number Concepts",
            competencyId = "C-7.1",
            competencyTitle = "Observes, compares, and distinguishes physical sizes and quantities",
            learningOutcomeId = "LO-PS1-NUM-01",
            learningOutcomeText = "Distinguishes between big and small objects in concrete visual displays",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.PICTURE_IDENTIFICATION, QuestionType.MATCHING),
            difficultyBand = "Foundational Early",
            hindiPrompt = "बड़े चित्र पर गोला लगाओ (Circle the bigger tree)",
            santaliOlChiki = "ᱢᱟᱨᱟᱝ ᱫᱟᱨᱮ ᱨᱮ ᱜᱩᱞᱟᱹᱭ ᱢᱮ",
            phoneticGuide = "मारांग दारे रे गुलाय मे",
            englishGloss = "Big vs Small Tree",
            options = listOf("बड़ा पेड़ (ᱢᱟᱨᱟᱝ ᱫᱟᱨᱮ)", "छोटा पेड़ (ᱦᱩᱰᱤᱧ ᱫᱟᱨᱮ)"),
            correctAnswer = "बड़ा पेड़",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Nature",
            visualAssetRef = "tree"
        ),
        CurriculumItem(
            id = "PS1-NUM-CG7-C72-01",
            stage = FoundationalStage.PRE_SCHOOL_1,
            domain = CurriculumDomain.NUMERACY,
            curricularGoalId = "CG-7",
            curricularGoalTitle = "Number Sense & Pre-Number Concepts",
            competencyId = "C-7.2",
            competencyTitle = "Recites number names and recognizes concrete quantity up to 3",
            learningOutcomeId = "LO-PS1-NUM-02",
            learningOutcomeText = "Counts objects up to 3 using one-to-one correspondence",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.COUNT_AND_WRITE, QuestionType.MATCHING),
            difficultyBand = "Foundational Early",
            hindiPrompt = "गिनकर संख्या लिखो: दो मछलियां (Count 2 fishes)",
            santaliOlChiki = "ᱞᱮᱠᱷᱟ ᱠᱟᱛᱮ ᱚᱞ ᱢᱮ: ᱵᱟᱨᱭᱟ ᱦᱟᱹᱠᱩ (Bār-ya haku)",
            phoneticGuide = "बारया हाकू",
            englishGloss = "Count 2 Fishes",
            options = listOf("1", "2", "3"),
            correctAnswer = "2 (ᱵᱟᱨ)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Animals",
            visualAssetRef = "fish"
        ),
        CurriculumItem(
            id = "PS1-COG-CG4-C41-01",
            stage = FoundationalStage.PRE_SCHOOL_1,
            domain = CurriculumDomain.COGNITIVE,
            curricularGoalId = "CG-4",
            curricularGoalTitle = "Sensory Exploration and Natural Observation",
            competencyId = "C-4.1",
            competencyTitle = "Recognizes and matches primary colors and natural textures",
            learningOutcomeId = "LO-PS1-COG-01",
            learningOutcomeText = "Matches identical fruits and vegetables based on visual appearance",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.MATCHING, QuestionType.CLASSIFICATION),
            difficultyBand = "Foundational Early",
            hindiPrompt = "एक जैसे फल मिलाओ (Match identical fruits)",
            santaliOlChiki = "ᱢᱤᱫ ᱞᱮᱠᱟᱱ ᱡᱚ ᱠᱚ ᱡᱚᱲᱟᱣ ᱢᱮ",
            phoneticGuide = "मिद लेकान जो को जोड़ाव मे",
            englishGloss = "Match Identical Fruits",
            options = listOf("आम ➔ आम", "केला ➔ केला"),
            correctAnswer = "समान फल जोड़ी",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Nature"
        ),
        CurriculumItem(
            id = "PS1-PHY-CG1-C12-01",
            stage = FoundationalStage.PRE_SCHOOL_1,
            domain = CurriculumDomain.PHYSICAL,
            curricularGoalId = "CG-1",
            curricularGoalTitle = "Gross and Fine Motor Development",
            competencyId = "C-1.2",
            competencyTitle = "Develops fine motor control, pincer grip, and hand-eye coordination",
            learningOutcomeId = "LO-PS1-PHY-01",
            learningOutcomeText = "Draws pre-writing scribbles and vertical lines along dotted guides",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.TRACE_OR_WRITE),
            difficultyBand = "Foundational Early",
            hindiPrompt = "बिन्दुओं को मिलाकर सीधी रेखा खींचो (Trace straight line)",
            santaliOlChiki = "ᱴᱩᱰᱟᱹᱜ ᱠᱚ ᱡᱚᱲᱟᱣ ᱠᱟᱛᱮ ᱥᱚᱡᱷᱮ ᱜᱟᱨ ᱴᱟᱱᱟᱣ ᱢᱮ",
            phoneticGuide = "टुडाग को जोड़ाव काते सोज्हे गार तानाव मे",
            englishGloss = "Trace Vertical Line",
            options = emptyList(),
            correctAnswer = "सीधी रेखा (Trace line)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "General"
        ),
        CurriculumItem(
            id = "PS1-SE-CG3-C31-01",
            stage = FoundationalStage.PRE_SCHOOL_1,
            domain = CurriculumDomain.SOCIO_EMOTIONAL,
            curricularGoalId = "CG-3",
            curricularGoalTitle = "Emotional Well-being & Social Interaction",
            competencyId = "C-3.1",
            competencyTitle = "Expresses basic emotions and interacts cordially with peers",
            learningOutcomeId = "LO-PS1-SE-01",
            learningOutcomeText = "Shares toys, greets peers (Johar/Namaste), and demonstrates joyful play",
            suitability = WorksheetSuitability.OBSERVATION_BASED,
            supportedActivityTypes = emptyList(),
            difficultyBand = "Foundational Early",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Village"
        ),

        // =========================================================================
        // PRE-SCHOOL 2 (Ages 4–5)
        // =========================================================================
        CurriculumItem(
            id = "PS2-LANG-CG9-C91-01",
            stage = FoundationalStage.PRE_SCHOOL_2,
            domain = CurriculumDomain.LANGUAGE_LITERACY,
            curricularGoalId = "CG-9",
            curricularGoalTitle = "Early Emergent Literacy & Script Meaning Association",
            competencyId = "C-9.1",
            competencyTitle = "Identifies initial speech sounds and associates them with symbols / letters",
            learningOutcomeId = "LO-PS2-LANG-01",
            learningOutcomeText = "Recognizes starting sound of common words and identifies corresponding letter symbol",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.PICTURE_IDENTIFICATION, QuestionType.MATCHING, QuestionType.TRACE_OR_WRITE),
            difficultyBand = "Foundational Mid",
            hindiPrompt = "चित्र का पहला अक्षर पहचानो: क से कलम (Pen)",
            santaliOlChiki = "ᱪᱤᱛᱟᱹᱨ ᱧᱮᱞ ᱠᱟᱛᱮ ᱯᱩᱭᱞᱩ ᱟᱠᱷᱚᱨ ᱚᱞ ᱢᱮ: ᱠ (Ol)",
            phoneticGuide = "कोलम - ᱠ",
            englishGloss = "Initial Sound: Pen",
            options = listOf("क", "म", "न"),
            correctAnswer = "क (ᱠ)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "School",
            visualAssetRef = "pen"
        ),
        CurriculumItem(
            id = "PS2-LANG-CG8-C82-01",
            stage = FoundationalStage.PRE_SCHOOL_2,
            domain = CurriculumDomain.LANGUAGE_LITERACY,
            curricularGoalId = "CG-8",
            curricularGoalTitle = "Effective Oral Communication in Everyday Language",
            competencyId = "C-8.2",
            competencyTitle = "Expands conversational vocabulary through stories and picture talk",
            learningOutcomeId = "LO-PS2-LANG-02",
            learningOutcomeText = "Explains classroom tools and personal belonging names in bilingual dialogue",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.WORD_MEANING, QuestionType.MATCHING),
            difficultyBand = "Foundational Mid",
            hindiPrompt = "शब्द का संथाली अर्थ लिखो: किताब (Book)",
            santaliOlChiki = "ᱯᱩᱛᱷᱤ (Puthī)",
            phoneticGuide = "पुथी",
            englishGloss = "Book",
            options = listOf("ᱯᱩᱛᱷᱤ", "ᱠᱚᱞᱚᱢ", "ᱫᱟᱨᱮ"),
            correctAnswer = "ᱯᱩᱛᱷᱤ (पुथी)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "School",
            visualAssetRef = "book"
        ),
        CurriculumItem(
            id = "PS2-NUM-CG7-C72-01",
            stage = FoundationalStage.PRE_SCHOOL_2,
            domain = CurriculumDomain.NUMERACY,
            curricularGoalId = "CG-7",
            curricularGoalTitle = "Number Sense & Pre-Number Concepts",
            competencyId = "C-7.2",
            competencyTitle = "Counts objects up to 5 and recognizes corresponding numerals",
            learningOutcomeId = "LO-PS2-NUM-01",
            learningOutcomeText = "Matches groups of 1 to 5 familiar objects with numeral symbols",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.COUNT_AND_WRITE, QuestionType.MATCHING, QuestionType.ORDERING),
            difficultyBand = "Foundational Mid",
            hindiPrompt = "वस्तुओं को गिनकर सही संख्या लिखो: 4 चिड़ियाँ (4 Birds)",
            santaliOlChiki = "ᱪᱮᱬᱮ ᱠᱚ ᱞᱮᱠᱷᱟ ᱠᱟᱛᱮ ᱮᱞ ᱚᱞ ᱢᱮ: ᱯᱩᱱ (Pōn)",
            phoneticGuide = "पुनया चेणे",
            englishGloss = "Count 4 Birds",
            options = listOf("3", "4", "5"),
            correctAnswer = "4 (ᱯᱩᱱ)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Animals",
            visualAssetRef = "bird"
        ),
        CurriculumItem(
            id = "PS2-NUM-CG7-C73-01",
            stage = FoundationalStage.PRE_SCHOOL_2,
            domain = CurriculumDomain.NUMERACY,
            curricularGoalId = "CG-7",
            curricularGoalTitle = "Number Sense & Pre-Number Concepts",
            competencyId = "C-7.3",
            competencyTitle = "Recognizes basic 2D geometric shapes in the environment",
            learningOutcomeId = "LO-PS2-NUM-02",
            learningOutcomeText = "Identifies circle, triangle, and rectangle in familiar surroundings",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.MATCHING, QuestionType.CLASSIFICATION),
            difficultyBand = "Foundational Mid",
            hindiPrompt = "गोल आकार (वृत्त) वाले चित्र पर सही का निशान लगाओ",
            santaliOlChiki = "ᱜᱩᱞᱟᱹᱭ ᱪᱤᱛᱟᱹᱨ ᱨᱮ ᱴᱷᱤᱠ ᱪᱤᱱᱦᱟᱹ ᱮᱢ ᱢᱮ",
            phoneticGuide = "गुलाय चितार रे ठीक चिन्ह एम मे",
            englishGloss = "Circle Shape Identification",
            options = listOf("गेंद (गोल)", "डिब्बा (चौकोर)", "ईंट"),
            correctAnswer = "गेंद (गोल)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "General"
        ),
        CurriculumItem(
            id = "PS2-COG-CG5-C51-01",
            stage = FoundationalStage.PRE_SCHOOL_2,
            domain = CurriculumDomain.COGNITIVE,
            curricularGoalId = "CG-5",
            curricularGoalTitle = "Classification and Pattern Recognition",
            competencyId = "C-5.1",
            competencyTitle = "Classifies objects based on a single observable characteristic",
            learningOutcomeId = "LO-PS2-COG-01",
            learningOutcomeText = "Sorts domestic animals and wild animals into appropriate groups",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.CLASSIFICATION, QuestionType.MATCHING),
            difficultyBand = "Foundational Mid",
            hindiPrompt = "पालतू जानवर और जंगली जानवर का वर्गीकरण करो",
            santaliOlChiki = "ᱚᱲᱟᱜ ᱡᱤᱭᱟᱹᱞᱤ ᱟᱨ ᱵᱤᱨ ᱡᱤᱭᱟᱹᱞᱤ ᱵᱷᱮᱜᱟᱨ ᱢᱮ",
            phoneticGuide = "ओड़ाक जीयाली आर बीर जीयाली भेगार मे",
            englishGloss = "Sort Domestic vs Wild Animals",
            options = listOf("गाय ➔ पालतू", "बाघ ➔ जंगली", "कुत्ता ➔ पालतू"),
            correctAnswer = "गाय: पालतू, बाघ: जंगली",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Animals"
        ),
        CurriculumItem(
            id = "PS2-HAB-CG13-C131-01",
            stage = FoundationalStage.PRE_SCHOOL_2,
            domain = CurriculumDomain.LEARNING_HABITS,
            curricularGoalId = "CG-13",
            curricularGoalTitle = "Development of Positive Learning Habits & Focus",
            competencyId = "C-13.1",
            competencyTitle = "Follows two-step directions and returns learning materials to designated places",
            learningOutcomeId = "LO-PS2-HAB-01",
            learningOutcomeText = "Listens to multi-step instructions and keeps worksheet supplies organized",
            suitability = WorksheetSuitability.TEACHER_LED,
            supportedActivityTypes = emptyList(),
            difficultyBand = "Foundational Mid",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "School"
        ),

        // =========================================================================
        // PRE-SCHOOL 3 / BALVATIKA (Ages 5–6)
        // =========================================================================
        CurriculumItem(
            id = "BAL-LANG-CG9-C92-01",
            stage = FoundationalStage.BALVATIKA,
            domain = CurriculumDomain.LANGUAGE_LITERACY,
            curricularGoalId = "CG-9",
            curricularGoalTitle = "Early Emergent Literacy & Script Meaning Association",
            competencyId = "C-9.2",
            competencyTitle = "Recognizes letter-sound correspondence (Akshara / Ol Chiki)",
            learningOutcomeId = "LO-BAL-LANG-01",
            learningOutcomeText = "Reads and traces foundational letters and associates them with familiar words",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.TRACE_OR_WRITE, QuestionType.MATCHING, QuestionType.PICTURE_IDENTIFICATION),
            difficultyBand = "Foundational Prep",
            hindiPrompt = "अक्षर से शब्द मिलाओ: अ से अनार, प से पानी",
            santaliOlChiki = "ᱟᱠᱷᱚᱨ ᱠᱷᱚᱱ ᱟᱹᱲᱟᱹ ᱢᱮᱞᱟᱣ ᱢᱮ: ᱫ -> ᱫᱟᱜ (Dak')",
            phoneticGuide = "दाग (Dak') - पानी",
            englishGloss = "Letter-Word Match: Water",
            options = listOf("ᱫ ➔ ᱫᱟᱜ", "ᱚ ➔ ᱚᱲᱟᱜ", "ᱢ ➔ ᱢᱟᱪᱮᱛ"),
            correctAnswer = "ᱫ ➔ ᱫᱟᱜ (Water)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Nature",
            visualAssetRef = "water"
        ),
        CurriculumItem(
            id = "BAL-LANG-CG10-C101-01",
            stage = FoundationalStage.BALVATIKA,
            domain = CurriculumDomain.LANGUAGE_LITERACY,
            curricularGoalId = "CG-10",
            curricularGoalTitle = "Reading for Meaning and Emergent Comprehension",
            competencyId = "C-10.1",
            competencyTitle = "Reads 2-letter simple familiar words with phonetic awareness",
            learningOutcomeId = "LO-BAL-LANG-02",
            learningOutcomeText = "Reads simple two-syllable sight words in mother tongue / Hindi",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.FILL_IN_THE_BLANK, QuestionType.WORD_MEANING, QuestionType.MULTIPLE_CHOICE),
            difficultyBand = "Foundational Prep",
            hindiPrompt = "खाली जगह भरकर शब्द पूरा करो: घ + ___ = घर",
            santaliOlChiki = "ᱠᱷᱟᱹᱞᱤ ᱴᱷᱟᱶ ᱯᱮᱨᱮᱡᱽ ᱢᱮ: ᱚᱲᱟᱜ (Oṛak')",
            phoneticGuide = "ओड़ाक (घर)",
            englishGloss = "Complete Word: House",
            options = listOf("र", "ल", "म"),
            correctAnswer = "र (घर)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Village",
            visualAssetRef = "house"
        ),
        CurriculumItem(
            id = "BAL-NUM-CG7-C72-01",
            stage = FoundationalStage.BALVATIKA,
            domain = CurriculumDomain.NUMERACY,
            curricularGoalId = "CG-7",
            curricularGoalTitle = "Number Sense & Pre-Number Concepts",
            competencyId = "C-7.2",
            competencyTitle = "Counts objects up to 10 with accurate one-to-one correspondence",
            learningOutcomeId = "LO-BAL-NUM-01",
            learningOutcomeText = "Counts sets up to 10 and writes the corresponding numeral",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.COUNT_AND_WRITE, QuestionType.ORDERING, QuestionType.MATCHING),
            difficultyBand = "Foundational Prep",
            hindiPrompt = "पेड़ पर लगे सेब गिनकर संख्या लिखो: 5 सेब",
            santaliOlChiki = "ᱫᱟᱨᱮ ᱨᱮᱭᱟᱜ ᱥᱮᱣ ᱞᱮᱠᱷᱟ ᱠᱟᱛᱮ ᱚᱞ ᱢᱮ: ᱢᱚᱬᱮ (Mōṛẽ)",
            phoneticGuide = "मोणे गोटांग सेव",
            englishGloss = "Count 5 Apples",
            options = listOf("3", "4", "5", "6"),
            correctAnswer = "5 (ᱢᱚᱬᱮ)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Nature"
        ),
        CurriculumItem(
            id = "BAL-NUM-CG8-C81-01",
            stage = FoundationalStage.BALVATIKA,
            domain = CurriculumDomain.NUMERACY,
            curricularGoalId = "CG-8",
            curricularGoalTitle = "Early Operations and Mathematical Logic",
            competencyId = "C-8.1",
            competencyTitle = "Solves concrete addition (combining) situations up to 5 using pictures",
            learningOutcomeId = "LO-BAL-NUM-02",
            learningOutcomeText = "Combines two visual sets of objects to find total quantity up to 5",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.SOLVE, QuestionType.COUNT_AND_WRITE),
            difficultyBand = "Foundational Prep",
            hindiPrompt = "मिलाकर कुल संख्या बताओ: 2 फूल + 1 फूल = ___ फूल",
            santaliOlChiki = "ᱡᱚᱲᱟᱣ ᱠᱟᱛᱮ ᱞᱮᱠᱷᱟ ᱢᱮ: ᱒ ᱵᱟᱦᱟ + ᱑ ᱵᱟᱦᱟ = ___ ᱵᱟᱦᱟ",
            phoneticGuide = "बारया बाहा + मिदटांग बाहा = पेया बाहा",
            englishGloss = "2 Flowers + 1 Flower = 3 Flowers",
            options = listOf("2", "3", "4"),
            correctAnswer = "3 (ᱯᱮ)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Nature"
        ),
        CurriculumItem(
            id = "BAL-COG-CG5-C52-01",
            stage = FoundationalStage.BALVATIKA,
            domain = CurriculumDomain.COGNITIVE,
            curricularGoalId = "CG-5",
            curricularGoalTitle = "Classification and Pattern Recognition",
            competencyId = "C-5.2",
            competencyTitle = "Identifies and extends repeating AB and AAB patterns",
            learningOutcomeId = "LO-BAL-COG-01",
            learningOutcomeText = "Completes repeating shape and picture patterns (Circle, Square, Circle...)",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.COMPLETE_PATTERN, QuestionType.MULTIPLE_CHOICE),
            difficultyBand = "Foundational Prep",
            hindiPrompt = "पैटर्न को आगे बढ़ाओ: ⚪ ⬛ ⚪ ⬛ ___",
            santaliOlChiki = "ᱯᱮᱴᱟᱨᱱ ᱞᱟᱦᱟ ᱤᱫᱤ ᱢᱮ: ⚪ ⬛ ⚪ ⬛ ___",
            phoneticGuide = "पैटर्न लाहा इदी मे",
            englishGloss = "Complete Pattern ABAB",
            options = listOf("⚪", "⬛", "🔺"),
            correctAnswer = "⚪ (Circle)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "General"
        ),
        CurriculumItem(
            id = "BAL-AES-CG12-C121-01",
            stage = FoundationalStage.BALVATIKA,
            domain = CurriculumDomain.AESTHETIC,
            curricularGoalId = "CG-12",
            curricularGoalTitle = "Aesthetic Sensibility and Creative Expression",
            competencyId = "C-12.1",
            competencyTitle = "Explores colors, free-hand drawing, and traditional tribal art forms",
            learningOutcomeId = "LO-BAL-AES-01",
            learningOutcomeText = "Creates decorative patterns inspired by local village murals (Sohrai / Kohver)",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.TRACE_OR_WRITE),
            difficultyBand = "Foundational Prep",
            hindiPrompt = "पारंपरिक सोहराई भित्ति चित्र के बॉर्डर को पूरा करो",
            santaliOlChiki = "ᱥᱚᱦᱨᱟᱭ ᱪᱤᱛᱟᱹᱨ ᱨᱮᱭᱟᱜ ᱵᱚᱨᱰᱟᱨ ᱯᱩᱨᱟᱹᱣ ᱢᱮ",
            phoneticGuide = "सोहराय चितार रेयाग बोरडार पुराव मे",
            englishGloss = "Trace Traditional Sohrai Border",
            options = emptyList(),
            correctAnswer = "सचित्र पैटर्न",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Village"
        ),

        // =========================================================================
        // GRADE 1 (Ages 6–7)
        // =========================================================================
        CurriculumItem(
            id = "G1-LANG-CG10-C102-01",
            stage = FoundationalStage.GRADE_1,
            domain = CurriculumDomain.LANGUAGE_LITERACY,
            curricularGoalId = "CG-10",
            curricularGoalTitle = "Reading for Meaning and Emergent Comprehension",
            competencyId = "C-10.2",
            competencyTitle = "Reads simple 3-4 word sentences with comprehension",
            learningOutcomeId = "LO-G1-LANG-01",
            learningOutcomeText = "Reads simple grade-level sentences and answers factual who/what questions",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.READ_AND_ANSWER, QuestionType.FILL_IN_THE_BLANK, QuestionType.MULTIPLE_CHOICE),
            difficultyBand = "Grade 1 Standard",
            hindiPrompt = "पढ़ो और उत्तर लिखो: सूरज चमकता है। सूरज कब चमकता है?",
            santaliOlChiki = "ᱯᱟᱲᱦᱟᱣ ᱢᱮ ᱟᱨ ᱛᱮᱞᱟ ᱚᱞ ᱢᱮ: ᱥᱤᱸᱜᱤ ᱡᱩᱞᱩᱜ ᱠᱟᱱᱟᱭ। ᱥᱤᱸᱜᱤ ᱛᱤᱥ ᱧᱮᱞᱚᱜ-ᱟᱭ?",
            phoneticGuide = "सिंगी जुलुग कानाय (सूरज चमकता है)",
            englishGloss = "Sun shines in the day",
            options = listOf("दिन में (ᱥᱤᱧ ᱵᱮᱲᱟ)", "रात में (ᱧᱤᱫᱟᱹ)"),
            correctAnswer = "दिन में (ᱥᱤᱧ ᱵᱮᱲᱟ)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Nature",
            visualAssetRef = "sun"
        ),
        CurriculumItem(
            id = "G1-LANG-CG11-C111-01",
            stage = FoundationalStage.GRADE_1,
            domain = CurriculumDomain.LANGUAGE_LITERACY,
            curricularGoalId = "CG-11",
            curricularGoalTitle = "Writing for Expression and Purpose",
            competencyId = "C-11.1",
            competencyTitle = "Writes familiar words and labels pictures correctly",
            learningOutcomeId = "LO-G1-LANG-02",
            learningOutcomeText = "Writes names of domestic animals and everyday classroom articles",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.WORD_MEANING, QuestionType.MATCHING, QuestionType.FILL_IN_THE_BLANK),
            difficultyBand = "Grade 1 Standard",
            hindiPrompt = "संथाली में अनुवाद करो: कुत्ता वफादार होता है।",
            santaliOlChiki = "ᱥᱮᱛᱟ ᱫᱚ ᱵᱤᱥᱣᱟᱥᱤ ᱠᱟᱱᱟᱭ। (Seta do biswasi kanay.)",
            phoneticGuide = "सेता दो बिस्वाशी कानाय",
            englishGloss = "Dog is loyal",
            options = emptyList(),
            correctAnswer = "ᱥᱮᱛᱟ ᱫᱚ ᱵᱤᱥᱣᱟᱥᱤ ᱠᱟᱱᱟᱭ।",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Animals",
            visualAssetRef = "dog"
        ),
        CurriculumItem(
            id = "G1-NUM-CG7-C74-01",
            stage = FoundationalStage.GRADE_1,
            domain = CurriculumDomain.NUMERACY,
            curricularGoalId = "CG-7",
            curricularGoalTitle = "Number Sense & Place Value Foundations",
            competencyId = "C-7.4",
            competencyTitle = "Understands numbers up to 20 and groups into tens and ones",
            learningOutcomeId = "LO-G1-NUM-01",
            learningOutcomeText = "Compares numbers up to 20 using greater than / less than concepts",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.ORDERING, QuestionType.SOLVE, QuestionType.MULTIPLE_CHOICE),
            difficultyBand = "Grade 1 Standard",
            hindiPrompt = "संख्याओं को बढ़ते क्रम में लिखो: 7, 3, 9, 5",
            santaliOlChiki = "ᱮᱞ ᱠᱚ ᱞᱟᱦᱟᱱᱛᱤ ᱦᱚᱨᱟ ᱛᱮ ᱥᱟᱡᱟᱣ ᱢᱮ: ᱗, ᱓, ᱙, ᱕",
            phoneticGuide = "एल को लाहान्ती होरा ते साजाव मे",
            englishGloss = "Ascending Order: 3, 5, 7, 9",
            options = listOf("3, 5, 7, 9", "9, 7, 5, 3", "5, 3, 7, 9"),
            correctAnswer = "3, 5, 7, 9",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "School"
        ),
        CurriculumItem(
            id = "G1-NUM-CG8-C82-01",
            stage = FoundationalStage.GRADE_1,
            domain = CurriculumDomain.NUMERACY,
            curricularGoalId = "CG-8",
            curricularGoalTitle = "Foundational Addition & Subtraction",
            competencyId = "C-8.2",
            competencyTitle = "Performs addition and subtraction of numbers up to 9",
            learningOutcomeId = "LO-G1-NUM-02",
            learningOutcomeText = "Solves single-digit addition and subtraction word problems",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.SOLVE, QuestionType.SHORT_ANSWER),
            difficultyBand = "Grade 1 Standard",
            hindiPrompt = "जोड़ो: 4 + 3 = ___",
            santaliOlChiki = "ᱢᱮᱥᱟᱭ ᱢᱮ: ᱔ + ᱓ = ___",
            phoneticGuide = "पुन + पे = सात (ᱮᱭᱟᱭ)",
            englishGloss = "4 + 3 = 7",
            options = listOf("6", "7", "8"),
            correctAnswer = "7 (ᱮᱭᱟᱭ)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "School"
        ),
        CurriculumItem(
            id = "G1-COG-CG6-C61-01",
            stage = FoundationalStage.GRADE_1,
            domain = CurriculumDomain.COGNITIVE,
            curricularGoalId = "CG-6",
            curricularGoalTitle = "Environmental Understanding and Community Living",
            competencyId = "C-6.1",
            competencyTitle = "Identifies community helpers and their essential tools",
            learningOutcomeId = "LO-G1-COG-01",
            learningOutcomeText = "Matches community roles (teacher, farmer, potter) with their functions",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.MATCHING, QuestionType.WORD_MEANING),
            difficultyBand = "Grade 1 Standard",
            hindiPrompt = "सही जोड़ी मिलाओ: शिक्षक ➔ पढ़ाते हैं, किसान ➔ फसल उगाते हैं",
            santaliOlChiki = "ᱥᱟᱹᱨᱤ ᱡᱚᱲ ᱢᱮᱞᱟᱣ ᱢᱮ: ᱢᱟᱪᱮᱛ -> ᱯᱟᱲᱦᱟᱣᱮᱫ-ᱟᱭ",
            phoneticGuide = "माचेत -> पाड़हावेदाय",
            englishGloss = "Teacher -> Teaches",
            options = listOf("शिक्षक ➔ पढ़ाते हैं", "किसान ➔ फसल उगाते हैं"),
            correctAnswer = "शिक्षक: पढ़ाते हैं (ᱢᱟᱪᱮᱛ)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Village",
            visualAssetRef = "teacher"
        ),

        // =========================================================================
        // GRADE 2 (Ages 7–8)
        // =========================================================================
        CurriculumItem(
            id = "G2-LANG-CG10-C103-01",
            stage = FoundationalStage.GRADE_2,
            domain = CurriculumDomain.LANGUAGE_LITERACY,
            curricularGoalId = "CG-10",
            curricularGoalTitle = "Fluent Reading with Meaning (NIPUN Target: 45–60 WPM)",
            competencyId = "C-10.3",
            competencyTitle = "Reads short age-appropriate passages fluently with comprehension",
            learningOutcomeId = "LO-G2-LANG-01",
            learningOutcomeText = "Reads a 4-line story and identifies the main idea and cause-effect",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.READ_AND_ANSWER, QuestionType.TRUE_FALSE, QuestionType.SHORT_ANSWER),
            difficultyBand = "Grade 2 Standard",
            hindiPrompt = "गद्यांश पढ़कर उत्तर दो: 'पेड़ हमें फल और छाया देते हैं। हमें पेड़ लगाने चाहिए।'",
            santaliOlChiki = "ᱯᱟᱲᱦᱟᱣ ᱠᱟᱛᱮ ᱛᱮᱞᱟ ᱮᱢ ᱢᱮ: 'ᱫᱟᱨᱮ ᱡᱚ ᱟᱨ ᱩᱢᱩᱞ ᱮᱢᱚᱜ-ᱟ। ᱟᱵᱚ ᱫᱟᱨᱮ ᱨᱚᱦᱚᱭ ᱞᱟᱹᱠᱛᱤ ᱠᱟᱱᱟ।'",
            phoneticGuide = "दारे जो आर उमुल एमोगा (पेड़ फल और छाया देते हैं)",
            englishGloss = "Trees give us fruit and shade",
            options = emptyList(),
            correctAnswer = "पेड़ हमें फल और छाया देते हैं। (ᱫᱟᱨᱮ ᱡᱚ ᱟᱨ ᱩᱢᱩᱞ ᱮᱢᱚᱜ-ᱟ)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Nature",
            visualAssetRef = "tree"
        ),
        CurriculumItem(
            id = "G2-LANG-CG8-C83-01",
            stage = FoundationalStage.GRADE_2,
            domain = CurriculumDomain.LANGUAGE_LITERACY,
            curricularGoalId = "CG-8",
            curricularGoalTitle = "Vocabulary Depth and Grammatical Awareness",
            competencyId = "C-8.3",
            competencyTitle = "Understands opposite words and compound vocabulary",
            learningOutcomeId = "LO-G2-LANG-02",
            learningOutcomeText = "Matches antonym pairs in bilingual Hindi and Santali text",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.MATCHING, QuestionType.WORD_MEANING),
            difficultyBand = "Grade 2 Standard",
            hindiPrompt = "विलोम शब्द मिलाओ: दिन ➔ रात, बड़ा ➔ छोटा",
            santaliOlChiki = "ᱩᱞᱴᱟᱹ ᱟᱹᱲᱟᱹ ᱢᱮᱞᱟᱣ ᱢᱮ: ᱥᱤᱧ ➔ ᱧᱤᱫᱟᱹ, ᱢᱟᱨᱟᱝ ➔ ᱦᱩᱰᱤᱧ",
            phoneticGuide = "सिंञ -> ञिदा, मारांग -> हुडिंग",
            englishGloss = "Antonyms: Day/Night, Big/Small",
            options = listOf("दिन ➔ रात", "बड़ा ➔ छोटा", "आगे ➔ पीछे"),
            correctAnswer = "दिन ➔ रात (ᱥᱤᱧ ➔ ᱧᱤᱫᱟᱹ)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "General"
        ),
        CurriculumItem(
            id = "G2-NUM-CG7-C75-01",
            stage = FoundationalStage.GRADE_2,
            domain = CurriculumDomain.NUMERACY,
            curricularGoalId = "CG-7",
            curricularGoalTitle = "Two-Digit Place Value & Number Sense (NIPUN Target: up to 99)",
            competencyId = "C-7.5",
            competencyTitle = "Reads, writes, and expands 2-digit numbers using tens and ones",
            learningOutcomeId = "LO-G2-NUM-01",
            learningOutcomeText = "Breaks 2-digit numbers into tens and ones (e.g., 45 = 4 tens + 5 ones)",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.SOLVE, QuestionType.FILL_IN_THE_BLANK),
            difficultyBand = "Grade 2 Standard",
            hindiPrompt = "दहाई और इकाई अलग करो: 36 = ___ दहाई + ___ इकाई",
            santaliOlChiki = "ᱜᱮᱞ ᱟᱨ ᱢᱤᱫ ᱵᱷᱮᱜᱟᱨ ᱢᱮ: ᱓᱖ = ___ ᱜᱮᱞ + ___ ᱢᱤᱫ",
            phoneticGuide = "गेल आर मिद भेगार मे",
            englishGloss = "36 = 3 Tens + 6 Ones",
            options = listOf("3 दहाई और 6 इकाई", "6 दहाई और 3 इकाई"),
            correctAnswer = "3 दहाई + 6 इकाई",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "School"
        ),
        CurriculumItem(
            id = "G2-NUM-CG8-C83-01",
            stage = FoundationalStage.GRADE_2,
            domain = CurriculumDomain.NUMERACY,
            curricularGoalId = "CG-8",
            curricularGoalTitle = "2-Digit Operations and Practical Arithmetic",
            competencyId = "C-8.3",
            competencyTitle = "Adds and subtracts 2-digit numbers up to 99",
            learningOutcomeId = "LO-G2-NUM-02",
            learningOutcomeText = "Solves real-life contextual addition problems up to 99 without regrouping",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.SOLVE, QuestionType.SHORT_ANSWER),
            difficultyBand = "Grade 2 Standard",
            hindiPrompt = "हल करो: 24 + 15 = ___",
            santaliOlChiki = "ᱥᱚᱞᱦᱮ ᱢᱮ: ᱒᱔ + ᱑᱕ = ___",
            phoneticGuide = "सोलहे मे: २४ + १५",
            englishGloss = "24 + 15 = 39",
            options = listOf("37", "39", "40"),
            correctAnswer = "39",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "School"
        ),
        CurriculumItem(
            id = "G2-COG-CG4-C42-01",
            stage = FoundationalStage.GRADE_2,
            domain = CurriculumDomain.COGNITIVE,
            curricularGoalId = "CG-4",
            curricularGoalTitle = "Water, Hygiene and Natural Resources",
            competencyId = "C-4.2",
            competencyTitle = "Identifies sources of clean water and daily conservation practices",
            learningOutcomeId = "LO-G2-COG-01",
            learningOutcomeText = "Identifies natural vs artificial water sources and evaluates hygiene statements",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.TRUE_FALSE, QuestionType.CLASSIFICATION, QuestionType.READ_AND_ANSWER),
            difficultyBand = "Grade 2 Standard",
            hindiPrompt = "सही या गलत बताओ: हमें पीने का पानी हमेशा ढककर रखना चाहिए।",
            santaliOlChiki = "ᱥᱟᱹᱨᱤ ᱥᱮ ᱵᱟᱹᱲᱤᱡ: ᱟᱵᱚ ᱫᱚ ᱧᱩ ᱫᱟᱜ ᱡᱟᱣᱜᱮ ᱯᱚᱴᱚᱢ ᱠᱟᱛᱮ ᱫᱚᱦᱚ ᱞᱟᱹᱠᱛᱤ ᱠᱟᱱᱟ।",
            phoneticGuide = "सारि से बाड़िज (True or False)",
            englishGloss = "Drinking water should always be covered",
            options = listOf("सही (True)", "गलत (False)"),
            correctAnswer = "सही (True / ᱥᱟᱹᱨᱤ)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Village",
            visualAssetRef = "water"
        ),

        // =========================================================================
        // GRADE 3 (Ages 8–9)
        // =========================================================================
        CurriculumItem(
            id = "G3-LANG-CG10-C104-01",
            stage = FoundationalStage.GRADE_3,
            domain = CurriculumDomain.LANGUAGE_LITERACY,
            curricularGoalId = "CG-10",
            curricularGoalTitle = "Independent Reading Comprehension (NIPUN Target: at least 60 WPM)",
            competencyId = "C-10.4",
            competencyTitle = "Reads unfamiliar texts with understanding and answers inferential questions",
            learningOutcomeId = "LO-G3-LANG-01",
            learningOutcomeText = "Draws conclusions and infers character feelings from short bilingual passages",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.READ_AND_ANSWER, QuestionType.SHORT_ANSWER, QuestionType.MULTIPLE_CHOICE),
            difficultyBand = "Grade 3 Advanced",
            hindiPrompt = "कहानी पढ़कर उत्तर लिखो: 'बाघ जंगल में रहता है। वह शक्तिशाली और फुर्तीला होता है। बाघ कहाँ रहता है?'",
            santaliOlChiki = "ᱠᱟᱹᱦᱱᱤ ᱯᱟᱲᱦᱟᱣ ᱠᱟᱛᱮ ᱛᱮᱞᱟ ᱚᱞ ᱢᱮ: 'ᱛᱟᱹᱨᱩᱵ ᱵᱤᱨ ᱨᱮ ᱛᱟᱦᱮᱸᱱᱟᱭ। ᱛᱟᱹᱨᱩᱵ ᱚᱠᱟᱨᱮ ᱛᱟᱦᱮᱸᱱᱟᱭ?'",
            phoneticGuide = "तारुब बीर रे ताहेनाय (बाघ जंगल में रहता है)",
            englishGloss = "Tiger lives in forest",
            options = listOf("जंगल में (ᱵᱤᱨ ᱨᱮ)", "घर में (ᱚᱲᱟᱜ ᱨᱮ)", "पानी में (ᱫᱟᱜ ᱨᱮ)"),
            correctAnswer = "जंगल में (ᱵᱤᱨ ᱨᱮ)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Animals",
            visualAssetRef = "tiger"
        ),
        CurriculumItem(
            id = "G3-LANG-CG11-C112-01",
            stage = FoundationalStage.GRADE_3,
            domain = CurriculumDomain.LANGUAGE_LITERACY,
            curricularGoalId = "CG-11",
            curricularGoalTitle = "Creative and Structured Sentence Writing",
            competencyId = "C-11.2",
            competencyTitle = "Frames complete grammatically correct sentences on given keywords",
            learningOutcomeId = "LO-G3-LANG-02",
            learningOutcomeText = "Constructs 3 coherent sentences describing school or village life",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.FILL_IN_THE_BLANK, QuestionType.SHORT_ANSWER),
            difficultyBand = "Grade 3 Advanced",
            hindiPrompt = "दिए गए शब्द से वाक्य बनाओ: स्कूल (School)",
            santaliOlChiki = "ᱮᱢ ᱟᱠᱟᱱ ᱟᱹᱲᱟᱹ ᱛᱮ ᱟᱹᱭᱟᱹᱛ ᱵᱮᱱᱟᱣ ᱢᱮ: ᱤᱛᱩᱱ ᱚᱲᱟᱜ (Itun oṛak')",
            phoneticGuide = "आले इतुन ओड़ाक चालागाले (हम स्कूल जाते हैं)",
            englishGloss = "Sentence framing: School",
            options = emptyList(),
            correctAnswer = "हम स्कूल जाते हैं। (ᱟᱞᱮ ᱤᱛᱩᱱ ᱚᱲᱟᱜ ᱪᱟᱞᱟᱜ-ᱟᱞᱮ।)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "School",
            visualAssetRef = "school"
        ),
        CurriculumItem(
            id = "G3-NUM-CG7-C76-01",
            stage = FoundationalStage.GRADE_3,
            domain = CurriculumDomain.NUMERACY,
            curricularGoalId = "CG-7",
            curricularGoalTitle = "Numbers up to 999 & Place Value",
            competencyId = "C-7.6",
            competencyTitle = "Reads, writes, and compares 3-digit numbers up to 999",
            learningOutcomeId = "LO-G3-NUM-01",
            learningOutcomeText = "Arranges 3-digit numbers in ascending and descending sequences",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.ORDERING, QuestionType.SOLVE),
            difficultyBand = "Grade 3 Advanced",
            hindiPrompt = "संख्याओं को घटते क्रम में लिखो: 320, 450, 210, 500",
            santaliOlChiki = "ᱮᱞ ᱠᱚ ᱟᱬᱜᱚᱱ ᱦᱚᱨᱟ ᱛᱮ ᱥᱟᱡᱟᱣ ᱢᱮ: ᱓᱒᱐, ᱔᱕᱐, ᱒᱑᱐, ᱕᱐᱐",
            phoneticGuide = "एल को आणगोन होरा ते साजाव मे",
            englishGloss = "Descending Order: 500, 450, 320, 210",
            options = listOf("500, 450, 320, 210", "210, 320, 450, 500"),
            correctAnswer = "500, 450, 320, 210",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "School"
        ),
        CurriculumItem(
            id = "G3-NUM-CG8-C84-01",
            stage = FoundationalStage.GRADE_3,
            domain = CurriculumDomain.NUMERACY,
            curricularGoalId = "CG-8",
            curricularGoalTitle = "Multiplication as Repeated Addition & Division",
            competencyId = "C-8.4",
            competencyTitle = "Multiplies numbers up to 10 x 10 and understands division as equal sharing",
            learningOutcomeId = "LO-G3-NUM-02",
            learningOutcomeText = "Solves real-world multiplication problems and equal distribution situations",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.SOLVE, QuestionType.SHORT_ANSWER, QuestionType.MULTIPLE_CHOICE),
            difficultyBand = "Grade 3 Advanced",
            hindiPrompt = "हल करो: 6 बच्चों को 4-4 केले दिए गए। कुल कितने केले दिए गए? (6 x 4 = ___)",
            santaliOlChiki = "ᱥᱚᱞᱦᱮ ᱢᱮ: ᱖ ᱜᱤᱫᱽᱨᱟᱹ ᱔-᱔ ᱠᱟᱭᱨᱟ ᱮᱢ ᱦᱩᱭ ᱮᱱᱟ। ᱞᱮᱠᱷᱟ ᱛᱤᱱᱟᱹᱜ? (᱖ x ᱔ = ___)",
            phoneticGuide = "६ x ४ = २४ (Bar-gel pun)",
            englishGloss = "6 x 4 = 24",
            options = listOf("20", "24", "28"),
            correctAnswer = "24 (ᱵᱟᱨ ᱜᱮᱞ ᱯᱩᱱ)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "School"
        ),
        CurriculumItem(
            id = "G3-NUM-CG8-C85-01",
            stage = FoundationalStage.GRADE_3,
            domain = CurriculumDomain.NUMERACY,
            curricularGoalId = "CG-8",
            curricularGoalTitle = "Fraction Foundations & Equal Parts",
            competencyId = "C-8.5",
            competencyTitle = "Recognizes half (1/2) and quarter (1/4) of a whole visual object",
            learningOutcomeId = "LO-G3-NUM-03",
            learningOutcomeText = "Shades or identifies 1/2 and 1/4 portions of geometric figures",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.PICTURE_IDENTIFICATION, QuestionType.SOLVE),
            difficultyBand = "Grade 3 Advanced",
            hindiPrompt = "चित्र का आधा (1/2) भाग पहचानो",
            santaliOlChiki = "ᱪᱤᱛᱟᱹᱨ ᱨᱮᱭᱟᱜ ᱛᱟᱞᱟ ᱦᱟᱹᱴᱤᱧ (᱑/᱒) ᱪᱤᱱᱦᱟᱹᱣ ᱢᱮ",
            phoneticGuide = "ताला हाटिंग (आधा / 1/2)",
            englishGloss = "Identify One-Half (1/2)",
            options = listOf("आधा (1/2)", "एक-चौथाई (1/4)", "पूरा (1)"),
            correctAnswer = "आधा (1/2 / ᱛᱟᱞᱟ ᱦᱟᱹᱴᱤᱧ)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "General"
        ),
        CurriculumItem(
            id = "G3-COG-CG6-C62-01",
            stage = FoundationalStage.GRADE_3,
            domain = CurriculumDomain.COGNITIVE,
            curricularGoalId = "CG-6",
            curricularGoalTitle = "Local Ecosystems, Forest Products, and Heritage",
            competencyId = "C-6.2",
            competencyTitle = "Appreciates local flora, fauna, and sustainable community living",
            learningOutcomeId = "LO-G3-COG-01",
            learningOutcomeText = "Identifies indigenous tribal trees (Sal, Mahua, Kendu) and their seasonal gifts",
            suitability = WorksheetSuitability.WORKSHEET_SUITABLE,
            supportedActivityTypes = listOf(QuestionType.MATCHING, QuestionType.READ_AND_ANSWER, QuestionType.TRUE_FALSE),
            difficultyBand = "Grade 3 Advanced",
            hindiPrompt = "सही जोड़ी मिलाओ: सखुआ/साल ➔ सरहुल पर्व, महुआ ➔ मीठे फूल",
            santaliOlChiki = "ᱥᱟᱹᱨᱤ ᱡᱚᱲ ᱢᱮᱞᱟᱣ ᱢᱮ: ᱥᱟᱨᱡᱚᱢ ➔ ᱵᱟᱦᱟ ᱯᱚᱨᱚᱵᱽ, ᱢᱟᱹᱛᱠᱚᱢ ➔ ᱦᱮᱲᱮᱢ ᱵᱟᱦᱟ",
            phoneticGuide = "सारजोम (साल) -> बाहा परोब",
            englishGloss = "Sal tree associated with Baha / Sarhul festival",
            options = listOf("साल (ᱥᱟᱨᱡᱚᱢ) ➔ सरहुल पर्व", "महुआ (ᱢᱟᱹᱛᱠᱚᱢ) ➔ मीठे फूल"),
            correctAnswer = "साल: सरहुल पर्व (ᱥᱟᱨᱡᱚᱢ ➔ ᱵᱟᱦᱟ ᱯᱚᱨᱚᱵᱽ)",
            verificationStatus = SantaliVerificationStatus.VERIFIED,
            culturalTheme = "Nature",
            visualAssetRef = "tree"
        )
    )

    /**
     * Returns all registered curriculum items.
     */
    fun getAllItems(): List<CurriculumItem> = items

    /**
     * Filters items by stage.
     */
    fun getItemsForStage(stage: FoundationalStage): List<CurriculumItem> =
        items.filter { it.stage == stage }

    /**
     * Filters items by domain.
     */
    fun getItemsForDomain(domain: CurriculumDomain): List<CurriculumItem> =
        items.filter { it.domain == domain }

    /**
     * Filters items by stage, domain, and optional suitability.
     */
    fun getItems(
        stage: FoundationalStage? = null,
        domain: CurriculumDomain? = null,
        suitability: WorksheetSuitability? = null
    ): List<CurriculumItem> {
        return items.filter { item ->
            (stage == null || item.stage == stage) &&
            (domain == null || item.domain == domain) &&
            (suitability == null || item.suitability == suitability)
        }
    }

    /**
     * Returns only worksheet-suitable items for practical classroom generation.
     */
    fun getWorksheetSuitableItems(
        stage: FoundationalStage? = null,
        domain: CurriculumDomain? = null
    ): List<CurriculumItem> {
        return getItems(stage, domain, WorksheetSuitability.WORKSHEET_SUITABLE)
    }

    /**
     * Lookup an item by unique identifier.
     */
    fun getItemById(id: String): CurriculumItem? =
        items.firstOrNull { it.id.equals(id, ignoreCase = true) }

    /**
     * Validates data integrity of the registry.
     * Returns an empty list if valid, or a list of descriptive errors if invalid.
     */
    fun validateRegistry(): List<String> {
        val errors = mutableListOf<String>()

        // 1. Check for duplicate IDs
        val idCounts = items.groupBy { it.id }
        for ((id, group) in idCounts) {
            if (group.size > 1) {
                errors.add("Duplicate curriculum ID detected: '$id' (${group.size} occurrences)")
            }
        }

        // 2. Validate all stages have entries
        val coveredStages = items.map { it.stage }.toSet()
        for (stage in FoundationalStage.values()) {
            if (!coveredStages.contains(stage)) {
                errors.add("Stage has no curriculum entries: ${stage.displayName}")
            }
        }

        // 3. Validate every item has required NCF-FS mapping fields
        for (item in items) {
            if (item.curricularGoalId.isBlank() || !item.curricularGoalId.startsWith("CG-")) {
                errors.add("Item ${item.id} has invalid Curricular Goal ID: '${item.curricularGoalId}'")
            }
            if (item.competencyId.isBlank() || !item.competencyId.startsWith("C-")) {
                errors.add("Item ${item.id} has invalid Competency ID: '${item.competencyId}'")
            }
            if (item.learningOutcomeId.isBlank() || !item.learningOutcomeId.startsWith("LO-")) {
                errors.add("Item ${item.id} has invalid Learning Outcome ID: '${item.learningOutcomeId}'")
            }
            if (item.learningOutcomeText.isBlank()) {
                errors.add("Item ${item.id} has empty Learning Outcome description")
            }

            // 4. Validate Worksheet Suitable items have activity types
            if (item.suitability == WorksheetSuitability.WORKSHEET_SUITABLE && item.supportedActivityTypes.isEmpty()) {
                errors.add("Worksheet suitable item ${item.id} defines no supported activity types")
            }

            // 5. Validate Non-Worksheet items do not define paper question types
            if (item.suitability != WorksheetSuitability.WORKSHEET_SUITABLE && item.supportedActivityTypes.isNotEmpty()) {
                errors.add("Non-worksheet item ${item.id} (${item.suitability}) improperly defines worksheet activity types")
            }
        }

        return errors
    }
}
