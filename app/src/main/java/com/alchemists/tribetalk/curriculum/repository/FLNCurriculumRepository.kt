package com.alchemists.tribetalk.curriculum.repository

import com.alchemists.tribetalk.curriculum.generator.TemplateContentGenerator
import com.alchemists.tribetalk.curriculum.models.*

object FLNCurriculumRepository {

    val lessons: List<Lesson> = listOf(
        // 1. Counting 1-10
        Lesson(
            id = "fln_num_01",
            grade = "Balvatika (Grade 1)",
            domain = FLNDomain.NUMERACY,
            topic = "Counting 1-10",
            titleHindi = "1 से 10 तक गिनती सीखें",
            titleSantali = "1 ᱠᱷᱚᱱ 10 ᱦᱟᱹᱵᱤᱡ ᱞᱮᱠᱷᱟ",
            titlePhonetic = "1 खोन 10 हाबिज लेखा",
            learningOutcome = LearningOutcome(
                id = "lo_num_01",
                nipunCode = "NIPUN-NUM-M1",
                descriptionHindi = "छात्र 1 से 10 तक वस्तुओं को सही क्रम में गिन सकते हैं।",
                descriptionSantali = "ᱯᱟᱹᱴᱷᱩᱣᱟᱹ ᱠᱚ 1 ᱠᱷᱚᱱ 10 ᱦᱟᱹᱵᱤᱡ ᱡᱤᱱᱤᱥ ᱴᱷᱤᱠ ᱞᱮᱠᱟᱛᱮ ᱠᱚ ᱞᱮᱠᱷᱟ ᱫᱟᱲᱮᱭᱟᱜᱼᱟ᱾",
                descriptionPhonetic = "पाठुआ को 1 खोन 10 हाबिज जिनिस ठीक लेकाते को लेखा दाड़ेयाआ।"
            ),
            instructions = listOf(
                LessonInstruction(
                    id = "inst_num_01_1",
                    stepNumber = 1,
                    teacherPromptHindi = "बच्चों, आज हम 1 से 5 तक गिनती सीखेंगे। अपने हाथ उठाएं।",
                    santaliTranslation = "ᱵᱟᱹᱵᱩ ᱛᱮᱦᱮᱧ ᱟᱵᱚ 1 ᱠᱷᱚᱱ 5 ᱦᱟᱹᱵᱤᱡ ᱞᱮᱠᱷᱟ ᱵᱚᱱ ᱪᱮᱫᱟ᱾ ᱛᱤ ᱛᱩᱞ ᱯᱮ᱾",
                    phoneticDevanagari = "बाबू तेहेञ आबो 1 खोन 5 हाबिज लेखा बोन चेदा। ती तुल पे।"
                ),
                LessonInstruction(
                    id = "inst_num_01_2",
                    stepNumber = 2,
                    teacherPromptHindi = "एक, दो, तीन, चार, पाँच! मेरे साथ बोलिए।",
                    santaliTranslation = "ᱢᱤᱫ, ᱵᱟᱨ, ᱯᱮ, ᱯᱩᱱ, ᱢᱚᱬᱮ! ᱤᱧ ᱥᱟᱶᱛᱮ ᱢᱮᱱ ᱯᱮ᱾",
                    phoneticDevanagari = "मिद, बार, पे, पुन, मोणे! इञ सावते मेन पे।"
                )
            ),
            activities = listOf(
                Activity(
                    id = "act_num_01",
                    learningOutcomeId = "lo_num_01",
                    titleHindi = "कंकड़ गिनती खेल",
                    titleSantali = " ढुंढा ᱞᱮᱠᱷᱟ ᱮᱱᱮᱡ",
                    type = ActivityType.COUNTING_GAME,
                    descriptionHindi = "सामने रखे कंकड़ों को एक-एक करके गिनें।",
                    descriptionSantali = "ᱥᱟᱢᱟᱝ ᱨᱮ ᱫᱚᱦᱚ ᱟᱠᱟᱱ  ढुंढा ᱢᱤᱫ ᱢᱤᱫ ᱛᱮ ᱞᱮᱠᱷᱟᱭ ᱯᱮ᱾",
                    interactiveItems = listOf("1 (ᱢᱤᱫ)", "2 (ᱵᱟᱨ)", "3 (ᱯᱮ)", "4 (ᱯᱩᱱ)", "5 (ᱢᱚᱬᱮ)"),
                    difficulty = DifficultyLevel.BEGINNER
                )
            ),
            assessmentQuestions = listOf(
                AssessmentQuestion(
                    id = "q_num_01",
                    learningOutcomeId = "lo_num_01",
                    questionHindi = "तीन के बाद कौन सी संख्या आती है?",
                    questionSantali = "ᱯᱮ ᱛᱟᱭᱚᱢ ᱚᱠᱟ ᱮᱞ ᱦᱤᱡᱩᱜᱼᱟ?",
                    questionPhonetic = "पे तायोम ओका एल हिजुआ?",
                    optionsHindi = listOf("दो", "चार", "पाँच"),
                    optionsSantali = listOf("ᱵᱟᱨ", "ᱯᱩᱱ", "ᱢᱚᱬᱮ"),
                    correctOptionIndex = 1,
                    explanationHindi = "गिनती में तीन (ᱯᱮ) के बाद चार (ᱯᱩᱱ) आता है。",
                    difficulty = DifficultyLevel.BEGINNER
                )
            )
        ),

        // 2. Number Recognition
        Lesson(
            id = "fln_num_02",
            grade = "Grade 1",
            domain = FLNDomain.NUMERACY,
            topic = "Number Recognition",
            titleHindi = "संख्या पहचान (1-10)",
            titleSantali = "ᱮᱞ ᱪᱤᱱᱦᱟᱹᱣ (1-10)",
            titlePhonetic = "एल चिन्हौ (1-10)",
            learningOutcome = LearningOutcome(
                id = "lo_num_02",
                nipunCode = "NIPUN-NUM-M2",
                descriptionHindi = "छात्र लिखित अंकों (1-10) को देखकर पहचान सकते हैं।",
                descriptionSantali = "ᱯᱟᱹᱴᱷᱩᱣᱟᱹ ᱠᱚ ᱚᱞ ᱟᱠᱟᱱ ᱮᱞ (1-10) ᱧᱮᱞ ᱠᱟᱛᱮ ᱠᱚ ᱪᱤᱱᱦᱟᱹᱣ ᱫᱟᱲᱮᱭᱟᱜᱼᱟ᱾",
                descriptionPhonetic = "पाठुआ को ओल आकान एल ᱧेल काते को चिन्हौ दाड़ेयाआ।"
            ),
            instructions = listOf(
                LessonInstruction(
                    id = "inst_num_02_1",
                    stepNumber = 1,
                    teacherPromptHindi = "ब्लैकबोर्ड पर लिखी संख्या 5 को ध्यान से देखिए।",
                    santaliTranslation = "ᱵᱽᱞᱮᱠᱵᱚᱨᱰ ᱨᱮ ᱚᱞ ᱟᱠᱟᱱ 5 ᱮᱞ ᱴᱷᱤᱠ ᱞᱮᱠᱟᱛᱮ ᱧᱮᱞ ᱯᱮ᱾",
                    phoneticDevanagari = "ब्लैकबोर्ड रे ओल आकान 5 एल ठीक लेकाते ᱧेल पे।"
                )
            ),
            activities = listOf(
                Activity(
                    id = "act_num_02",
                    learningOutcomeId = "lo_num_02",
                    titleHindi = "अंक फ्लैशकार्ड पहचान",
                    titleSantali = "ᱮᱞ ᱯᱷᱞᱮᱥᱠᱟᱨᱰ ᱪᱤᱱᱦᱟᱹᱣ",
                    type = ActivityType.FLASHCARD_RECOGNITION,
                    descriptionHindi = "दिखाए गए फ्लैशकार्ड का सही अंक चुनें।",
                    descriptionSantali = "ᱩᱫᱩᱜ ᱟᱠᱟᱱ ᱯᱷᱞᱮᱥᱠᱟᱨᱰ ᱨᱮᱱᱟᱜ ᱴᱷᱤᱠ ᱮᱞ ᱪᱤᱱᱦᱟᱹᱣ ᱯᱮ᱾",
                    interactiveItems = listOf("3 (ᱯᱮ)", "5 (ᱢᱚᱬᱮ)", "7 (ᱮᱭᱟᱭ)"),
                    difficulty = DifficultyLevel.BEGINNER
                )
            ),
            assessmentQuestions = listOf(
                AssessmentQuestion(
                    id = "q_num_02",
                    learningOutcomeId = "lo_num_02",
                    questionHindi = "अंक '5' को संथाली में क्या कहते हैं?",
                    questionSantali = "5 ᱮᱞ ᱥᱟᱱᱛᱟᱲᱤ ᱛᱮ ᱪᱮᱫ ᱠᱚ ᱢᱮᱛᱟᱜᱼᱟ?",
                    questionPhonetic = "5 एल संताड़ी ते चेद को मेताआ?",
                    optionsHindi = listOf("चार (ᱯᱩᱱ)", "पाँच (ᱢᱚᱬᱮ)", "छह (ᱛᱩᱨᱩᱭ)"),
                    optionsSantali = listOf("ᱯᱩᱱ", "ᱢᱚᱬᱮ", "ᱛᱩᱨᱩᱭ"),
                    correctOptionIndex = 1,
                    explanationHindi = "5 को संथाली में ᱢᱚᱬᱮ (मोणे) कहते हैं。",
                    difficulty = DifficultyLevel.BEGINNER
                )
            )
        ),

        // 3. Addition
        Lesson(
            id = "fln_num_03",
            grade = "Grade 1",
            domain = FLNDomain.NUMERACY,
            topic = "Addition (1-5)",
            titleHindi = "सरल जोड़ (1-5)",
            titleSantali = "ᱥᱟᱫᱷᱟᱨᱚᱱ ᱢᱮᱥᱟ (1-5)",
            titlePhonetic = "साधारण मेसा (1-5)",
            learningOutcome = LearningOutcome(
                id = "lo_num_03",
                nipunCode = "NIPUN-NUM-M3",
                descriptionHindi = "छात्र 1 से 5 तक की वस्तुओं का जोड़ कर सकते हैं।",
                descriptionSantali = "ᱯᱟᱹᱴᱷᱩᱣᱟᱹ ᱠᱚ 1 ᱠᱷᱚᱱ 5 ᱦᱟᱹᱵᱤᱡ ᱡᱤᱱᱤᱥ ᱠᱚ ᱢᱮᱥᱟ ᱫᱟᱲᱮᱭᱟᱜᱼᱟ᱾",
                descriptionPhonetic = "पाठुआ को 1 खोन 5 हाबिज जिनिस को मेसा दाड़ेयाआ।"
            ),
            instructions = listOf(
                LessonInstruction(
                    id = "inst_num_03_1",
                    stepNumber = 1,
                    teacherPromptHindi = "2 आम और 1 आम मिलकर कितने आम बनते हैं?",
                    santaliTranslation = "2 ᱩᱞ ᱟᱨ 1 ᱩᱞ ᱢᱮᱥᱟ ᱠᱟᱛᱮ ᱛᱤᱱᱟᱹᱜ ᱩᱞ ᱦᱩᱭᱩᱜᱼᱟ?",
                    phoneticDevanagari = "2 उल आर 1 उल मेसा काते तिनाअ उल हुयुआ?"
                )
            ),
            activities = listOf(
                Activity(
                    id = "act_num_03",
                    learningOutcomeId = "lo_num_03",
                    titleHindi = "वस्तु मिलाओ खेल",
                    titleSantali = "ᱡᱤᱱᱤᱥ ᱢᱮᱥᱟ ᱮᱱᱮᱡ",
                    type = ActivityType.PATTERN_MATCHING,
                    descriptionHindi = "दो समूहों की वस्तुओं को मिलाकर कुल संख्या बताएं।",
                    descriptionSantali = "ᱵᱟᱨᱭᱟ ᱜᱩᱴ ᱨᱮᱱᱟᱜ ᱡᱤᱱᱤᱥ ᱢᱮᱥᱟ ᱠᱟᱛᱮ ᱞᱮᱠᱷᱟᱭ ᱯᱮ᱾",
                    interactiveItems = listOf("2 + 1 = 3", "3 + 2 = 5"),
                    difficulty = DifficultyLevel.BEGINNER
                )
            ),
            assessmentQuestions = listOf(
                AssessmentQuestion(
                    id = "q_num_03",
                    learningOutcomeId = "lo_num_03",
                    questionHindi = "2 + 1 कितना होता है?",
                    questionSantali = "2 + 1 ᱛᱤᱱᱟᱹᱜ ᱦᱩᱭᱩᱜᱼᱟ?",
                    questionPhonetic = "2 + 1 तिनाअ हुयुआ?",
                    optionsHindi = listOf("2 (ᱵᱟᱨ)", "3 (ᱯᱮ)", "4 (ᱯᱩᱱ)"),
                    optionsSantali = listOf("ᱵᱟᱨ", "ᱯᱮ", "ᱯᱩᱱ"),
                    correctOptionIndex = 1,
                    explanationHindi = "2 और 1 को जोड़ने पर 3 (ᱯᱮ) मिलता है。",
                    difficulty = DifficultyLevel.BEGINNER
                )
            )
        ),

        // 4. Subtraction
        Lesson(
            id = "fln_num_04",
            grade = "Grade 1",
            domain = FLNDomain.NUMERACY,
            topic = "Subtraction (1-5)",
            titleHindi = "सरल घटाव (1-5)",
            titleSantali = "ᱥᱟᱫᱷᱟᱨᱚᱱ ᱵᱮᱜᱟᱨ (1-5)",
            titlePhonetic = "साधारण बेगार (1-5)",
            learningOutcome = LearningOutcome(
                id = "lo_num_04",
                nipunCode = "NIPUN-NUM-M4",
                descriptionHindi = "छात्र 1 से 5 तक की वस्तुओं में से घटाव समझ सकते हैं।",
                descriptionSantali = "ᱯᱟᱹᱴᱷᱩᱣᱟᱹ ᱠᱚ 1 ᱠᱷᱚᱱ 5 ᱦᱟᱹᱵᱤᱡ ᱡᱤᱱᱤᱥ ᱵᱮᱜᱟᱨ ᱠᱚ ᱵᱩᱡᱷᱟᱹᱣ ᱫᱟᱲᱮᱭᱟᱜᱼᱟ᱾",
                descriptionPhonetic = "पाठुआ को 1 खोन 5 हाबिज जिनिस बेगार को बुझौ दाड़ेयाआ।"
            ),
            instructions = listOf(
                LessonInstruction(
                    id = "inst_num_04_1",
                    stepNumber = 1,
                    teacherPromptHindi = "5 पक्षियों में से 2 पक्षी उड़ गए, कितने पक्षी बचे?",
                    santaliTranslation = "5 ᱪᱮᱬᱮ ᱠᱷᱚᱱ 2 ᱪᱮᱬᱮ ᱩᱰᱟᱹᱣ ᱮᱱᱟ, ᱛᱤᱱᱟᱹᱜ ᱪᱮᱬᱮ ᱥᱟᱨᱮᱡ ᱮᱱᱟ ᱠᱚ?",
                    phoneticDevanagari = "5 चेणे खोन 2 चेणे उडाव एना, तिनाअ चेणे सारेज एना को?"
                )
            ),
            activities = listOf(
                Activity(
                    id = "act_num_04",
                    learningOutcomeId = "lo_num_04",
                    titleHindi = "वस्तु हटाओ खेल",
                    titleSantali = "ᱡᱤᱱᱤᱥ ᱚᱪᱚᱜ ᱮᱱᱮᱡ",
                    type = ActivityType.COUNTING_GAME,
                    descriptionHindi = "कुल वस्तुओं में से कुछ वस्तुएं हटाकर शेष गिनें।",
                    descriptionSantali = "ᱡᱚᱛᱚ ᱡᱤᱱᱤᱥ ᱠᱷᱚᱱ ᱠᱤᱪᱷᱩ ᱚᱪᱚᱜ ᱠᱟᱛᱮ ᱥᱟᱨᱮᱡ ᱞᱮᱠᱷᱟᱭ ᱯᱮ᱾",
                    interactiveItems = listOf("5 - 2 = 3", "4 - 1 = 3"),
                    difficulty = DifficultyLevel.BEGINNER
                )
            ),
            assessmentQuestions = listOf(
                AssessmentQuestion(
                    id = "q_num_04",
                    learningOutcomeId = "lo_num_04",
                    questionHindi = "5 - 2 का उत्तर क्या है?",
                    questionSantali = "5 - 2 ᱨᱮᱱᱟᱜ ᱛᱮᱞᱟ ᱪᱮᱫ ᱠᱟᱱᱟ?",
                    questionPhonetic = "5 - 2 रेनाअ तेला चेद काना?",
                    optionsHindi = listOf("2 (ᱵᱟᱨ)", "3 (ᱯᱮ)", "4 (ᱯᱩᱱ)"),
                    optionsSantali = listOf("ᱵᱟᱨ", "ᱯᱮ", "ᱯᱩᱱ"),
                    correctOptionIndex = 1,
                    explanationHindi = "5 में से 2 घटाने पर 3 (ᱯᱮ) बचता है。",
                    difficulty = DifficultyLevel.BEGINNER
                )
            )
        ),

        // 5. Shapes
        Lesson(
            id = "fln_num_05",
            grade = "Balvatika",
            domain = FLNDomain.NUMERACY,
            topic = "Basic Shapes",
            titleHindi = "मूल आकृतियाँ (वृत्त, वर्ग)",
            titleSantali = "ᱢᱩᱞ ᱨᱩᱯ (ᱜᱳᱞ, ᱪᱚᱠᱟ)",
            titlePhonetic = "मुल रुप (गोल, चोका)",
            learningOutcome = LearningOutcome(
                id = "lo_num_05",
                nipunCode = "NIPUN-NUM-M5",
                descriptionHindi = "छात्र वृत्त और वर्ग जैसी आकृतियों को पहचानते हैं।",
                descriptionSantali = "ᱯᱟᱹᱴᱷᱩᱣᱟᱹ ᱠᱚ ᱜᱳᱞ ᱟᱨ ᱪᱚᱠᱟ ᱨᱩᱯ ᱠᱚ ᱪᱤᱱᱦᱟᱹᱣᱼᱟ᱾",
                descriptionPhonetic = "पाठुआ को गोल आर चोका रुप को चिन्हौआ।"
            ),
            instructions = listOf(
                LessonInstruction(
                    id = "inst_num_05_1",
                    stepNumber = 1,
                    teacherPromptHindi = "गेंद जैसी गोल आकृति को वृत्त कहते हैं।",
                    santaliTranslation = "ᱜᱮᱸᱫᱽ ᱞᱮᱠᱟᱱ ᱜᱳᱞ ᱨᱩᱯ ᱫᱚ ᱜᱳᱞ ᱠᱚ ᱢᱮᱛᱟᱜᱼᱟ᱾",
                    phoneticDevanagari = "गेंद लेकान गोल रुप दो गोल को मेताआ।"
                )
            ),
            activities = listOf(
                Activity(
                    id = "act_num_05",
                    learningOutcomeId = "lo_num_05",
                    titleHindi = "आकृति मिलाओ खेल",
                    titleSantali = "ᱨᱩᱯ ᱢᱤᱞᱟᱹᱣ ᱮᱱᱮᱡ",
                    type = ActivityType.PATTERN_MATCHING,
                    descriptionHindi = "दिए गए चित्रों में से वृत्त (गोल) आकृति खोजें।",
                    descriptionSantali = "ᱮᱢ ᱟᱠᱟᱱ ᱪᱤᱛᱟᱹᱨ ᱠᱷᱚᱱ ᱜᱳᱞ ᱨᱩᱯ ᱯᱟᱸᱡᱟᱭ ᱯᱮ᱾",
                    interactiveItems = listOf("वृत्त / ᱜᱳᱞ ⭕", "वर्ग / ᱪᱚᱠᱟ ⏹️"),
                    difficulty = DifficultyLevel.BEGINNER
                )
            ),
            assessmentQuestions = listOf(
                AssessmentQuestion(
                    id = "q_num_05",
                    learningOutcomeId = "lo_num_05",
                    questionHindi = "गेंद की आकृति कैसी होती है?",
                    questionSantali = "ᱜᱮᱸᱫᱽ ᱨᱮᱱᱟᱜ ᱨᱩᱯ ᱪᱮᱫ ᱞᱮᱠᱟᱱᱟ?",
                    questionPhonetic = "गेंद रेनाअ रुप चेद लेकाना?",
                    optionsHindi = listOf("वृत्त/गोल (ᱜᱳᱞ)", "वर्ग (ᱪᱚᱠᱟ)", "त्रिकोण"),
                    optionsSantali = listOf("ᱜᱳᱞ", "ᱪᱚᱠᱟ", "ᱛᱮᱠᱳᱬ"),
                    correctOptionIndex = 0,
                    explanationHindi = "गेंद गोल (ᱜᱳᱞ) आकृति की होती है。",
                    difficulty = DifficultyLevel.BEGINNER
                )
            )
        ),

        // 6. Colours
        Lesson(
            id = "fln_lit_01",
            grade = "Balvatika",
            domain = FLNDomain.LITERACY,
            topic = "Colour Recognition",
            titleHindi = "रंगों की पहचान",
            titleSantali = "ᱨᱚᱝ ᱪᱤᱱᱦᱟᱹᱣ",
            titlePhonetic = "रंग चिन्हौ",
            learningOutcome = LearningOutcome(
                id = "lo_lit_01",
                nipunCode = "NIPUN-LIT-L1",
                descriptionHindi = "छात्र प्राथमिक रंगों (लाल, हरा, नीला) को पहचानते हैं।",
                descriptionSantali = "ᱯᱟᱹᱴᱷᱩᱣᱟᱹ ᱠᱚ ᱢᱩᱞ ᱨᱚᱝ (ᱟᱨᱟ platform, ᱦᱟᱹᱨᱤᱭᱟᱹᱲ, ᱞᱤᱞ) ᱠᱚ ᱪᱤᱱᱦᱟᱹᱣᱼᱟ᱾",
                descriptionPhonetic = "पाठुआ को मुल रंग (आरा, हरियाड़, लिल) को चिन्हौआ।"
            ),
            instructions = listOf(
                LessonInstruction(
                    id = "inst_lit_01_1",
                    stepNumber = 1,
                    teacherPromptHindi = "सेब का रंग लाल (ᱟᱨᱟ platform) होता है।",
                    santaliTranslation = "ᱥᱮᱣ ᱨᱮᱱᱟᱜ ᱨᱚᱝ ᱫᱚ ᱟᱨᱟ platform ᱦᱩᱭᱩᱜᱼᱟ᱾",
                    phoneticDevanagari = "सेव रेनाअ रंग दो आरा हुयुआ।"
                )
            ),
            activities = listOf(
                Activity(
                    id = "act_lit_01",
                    learningOutcomeId = "lo_lit_01",
                    titleHindi = "रंग पहचान फ्लैशकार्ड",
                    titleSantali = "ᱨᱚᱝ ᱪᱤᱱᱦᱟᱹᱣ ᱯᱷᱞᱮᱥᱠᱟᱨᱰ",
                    type = ActivityType.FLASHCARD_RECOGNITION,
                    descriptionHindi = "लाल रंग का कार्ड चुनें।",
                    descriptionSantali = "ᱟᱨᱟ platform ᱨᱚᱝ ᱨᱮᱱᱟᱜ ᱠᱟᱨᱰ ᱪᱤᱱᱦᱟᱹᱣ ᱯᱮ᱾",
                    interactiveItems = listOf("🔴 लाल / ᱟᱨᱟ", "🟢 हरा / ᱦᱟᱹᱨᱤᱭᱟᱹᱲ", "🔵 नीला / ᱞᱤᱞ"),
                    difficulty = DifficultyLevel.BEGINNER
                )
            ),
            assessmentQuestions = listOf(
                AssessmentQuestion(
                    id = "q_lit_01",
                    learningOutcomeId = "lo_lit_01",
                    questionHindi = "पत्तियों का रंग कैसा होता है?",
                    questionSantali = "ᱥᱟᱠᱟᱢ ᱨᱮᱱᱟᱜ ᱨᱚᱝ ᱪᱮᱫ ᱞᱮᱠᱟᱱᱟ?",
                    questionPhonetic = "साकाम रेनाअ रंग चेद लेकाना?",
                    optionsHindi = listOf("लाल (ᱟᱨᱟ)", "हरा (ᱦᱟᱹᱨᱤᱭᱟᱹᱲ)", "पीला"),
                    optionsSantali = listOf("ᱟᱨᱟ", "ᱦᱟᱹᱨᱤᱭᱟᱹᱲ", "ᱥᱟᱥᱟᱝ"),
                    correctOptionIndex = 1,
                    explanationHindi = "पेड़ की पत्तियाँ हरी (ᱦᱟᱹᱨᱤᱭᱟᱹᱲ) होती हैं。",
                    difficulty = DifficultyLevel.BEGINNER
                )
            )
        ),

        // 7. Basic Alphabet/Letter Recognition
        Lesson(
            id = "fln_lit_02",
            grade = "Grade 1",
            domain = FLNDomain.LITERACY,
            topic = "Letter Recognition",
            titleHindi = "अक्षर पहचान (Ol Chiki / ᱚᱞ ᱪᱤᱠᱤ)",
            titleSantali = "ᱚᱞ ᱪᱤᱠᱤ ᱟᱠᱷᱚᱨ ᱪᱤᱱᱦᱟᱹᱣ",
            titlePhonetic = "ओल चिकी आखोर चिन्हौ",
            learningOutcome = LearningOutcome(
                id = "lo_lit_02",
                nipunCode = "NIPUN-LIT-L2",
                descriptionHindi = "छात्र ओल चिकी अक्षरों (ᱚ, ᱛ, ᱜ, ᱝ) की आकृति और ध्वनि पहचानते हैं।",
                descriptionSantali = "ᱯᱟᱹᱴᱷᱩᱣᱟᱹ ᱠᱚ ᱚᱞ ᱪᱤᱠᱤ ᱟᱠᱷᱚᱨ (ᱚ, ᱛ, ᱜ, ᱝ) ᱨᱮᱱᱟᱜ ᱨᱩᱯ ᱟᱨ ᱥᱟᱲᱮ ᱠᱚ ᱪᱤᱱᱦᱟᱹᱣᱼᱟ᱾",
                descriptionPhonetic = "पाठुआ को ओल चिकी आखोर रेनाअ रुप आर साड़े को चिन्हौआ।"
            ),
            instructions = listOf(
                LessonInstruction(
                    id = "inst_lit_02_1",
                    stepNumber = 1,
                    teacherPromptHindi = "यह पहला अक्षर 'ᱚ' (ऑ) है। मेरे साथ बोलिए।",
                    santaliTranslation = "ᱱᱚᱣᱟ ᱫᱚ ᱯᱩᱭᱞᱩ ᱟᱠᱷᱚᱨ 'ᱚ' ᱠᱟᱱᱟ᱾ ᱤᱧ ᱥᱟᱶᱛᱮ ᱢᱮᱱ ᱯᱮ᱾",
                    phoneticDevanagari = "नोवा दो पुयलु आखोर 'ᱚ' काना। इञ सावते मेन पे।"
                )
            ),
            activities = listOf(
                Activity(
                    id = "act_lit_02",
                    learningOutcomeId = "lo_lit_02",
                    titleHindi = "अक्षर ध्वनि उच्चारण",
                    titleSantali = "ᱟᱠᱷᱚᱨ ᱥᱟᱲᱮ ᱨᱚᱲ",
                    type = ActivityType.VOICE_REPEAT,
                    descriptionHindi = "अक्षर ᱚ को देखकर उच्चारण करें।",
                    descriptionSantali = "ᱟᱠᱷᱚᱨ ᱚ ᱧᱮᱞ ᱠᱟᱛᱮ ᱨᱚᱲ ᱯᱮ᱾",
                    interactiveItems = listOf("ᱚ (LA)", "ᱛ (AT)", "ᱜ (AG)", "ᱝ (ANG)"),
                    difficulty = DifficultyLevel.BEGINNER
                )
            ),
            assessmentQuestions = listOf(
                AssessmentQuestion(
                    id = "q_lit_02",
                    learningOutcomeId = "lo_lit_02",
                    questionHindi = "संथाली लिपि ओल चिकी का पहला अक्षर कौन सा है?",
                    questionSantali = "ᱥᱟᱱᱛᱟᱲᱤ ᱚᱞ ᱪᱤᱠᱤ ᱨᱮᱱᱟᱜ ᱯᱩᱭᱞᱩ ᱟᱠᱷᱚᱨ ᱚᱠᱟ ᱠᱟᱱᱟ?",
                    questionPhonetic = "संताड़ी ओल चिकी रेनाअ पुयलु आखोर ओका काना?",
                    optionsHindi = listOf("ᱚ (LA)", "ᱛ (AT)", "ᱜ (AG)"),
                    optionsSantali = listOf("ᱚ", "ᱛ", "ᱜ"),
                    correctOptionIndex = 0,
                    explanationHindi = "ओल चिकी का पहला अक्षर ᱚ (LA) होता है。",
                    difficulty = DifficultyLevel.BEGINNER
                )
            )
        ),

        // 8. Simple Vocabulary
        Lesson(
            id = "fln_lit_03",
            grade = "Grade 1",
            domain = FLNDomain.LITERACY,
            topic = "Classroom Vocabulary",
            titleHindi = "दैनिक कक्षा शब्द ज्ञान",
            titleSantali = "ᱫᱤᱱᱟᱹᱢ ᱦᱤᱞᱳᱜᱟᱜ ᱠᱞᱟᱥ ᱥᱟᱵᱟᱫᱽ",
            titlePhonetic = "दिनाम हिलोगआग क्लास साबाद",
            learningOutcome = LearningOutcome(
                id = "lo_lit_03",
                nipunCode = "NIPUN-LIT-L3",
                descriptionHindi = "छात्र कक्षा के दैनिक उपयोग के शब्दों (पानी, किताब, फल) का संथाली अनुवाद सीखते हैं।",
                descriptionSantali = "ᱯᱟᱹᱴᱷᱩᱣᱟᱹ ᱠᱚ ᱠᱞᱟᱥ ᱨᱮᱱᱟᱜ ᱫᱤᱱᱟᱹᱢ ᱥᱟᱵᱟᱫᱽ (ᱫᱟᱜ, ᱯᱩᱛᱷᱤ, ᱡᱚ) ᱥᱟᱱᱛᱟᱲᱤ ᱛᱮ ᱠᱚ ᱪᱮᱫᱼᱟ᱾",
                descriptionPhonetic = "पाठुआ को क्लास रेनाअ दिनाम साबाद (दाग, पुथि, जो) संताड़ी ते को चेदा।"
            ),
            instructions = listOf(
                LessonInstruction(
                    id = "inst_lit_03_1",
                    stepNumber = 1,
                    teacherPromptHindi = "पानी को संथाली में 'ᱫᱟᱜ' (दाग) कहते हैं।",
                    santaliTranslation = "ᱯᱟᱱᱤ ᱫᱚ ᱥᱟᱱᱛᱟᱲᱤ ᱛᱮ 'ᱫᱟᱜ' ᱠᱚ ᱢᱮᱛᱟᱜᱼᱟ᱾",
                    phoneticDevanagari = "पानी दो संताड़ी ते 'दाग' को मेताआ।"
                )
            ),
            activities = listOf(
                Activity(
                    id = "act_lit_03",
                    learningOutcomeId = "lo_lit_03",
                    titleHindi = "शब्द-चित्र मिलान",
                    titleSantali = "ᱥᱟᱵᱟᱫᱽ-ᱪᱤᱛᱟᱹᱨ ᱢᱤᱞᱟᱹᱣ",
                    type = ActivityType.PATTERN_MATCHING,
                    descriptionHindi = "चित्र देखकर सही संथाली शब्द चुनें।",
                    descriptionSantali = "ᱪᱤᱛᱟᱹᱨ ᱧᱮᱞ ᱠᱟᱛᱮ ᱴᱷᱤᱠ ᱥᱟᱱᱛᱟᱲᱤ ᱥᱟᱵᱟᱫᱽ ᱪᱤᱱᱦᱟᱹᱣ ᱯᱮ᱾",
                    interactiveItems = listOf("💧 पानी ➜ ᱫᱟᱜ (दाग)", "📖 किताब ➜ ᱯᱩᱛᱷᱤ (पुथि)", "🍎 फल ➜ ᱡᱚ (जो)"),
                    difficulty = DifficultyLevel.BEGINNER
                )
            ),
            assessmentQuestions = listOf(
                AssessmentQuestion(
                    id = "q_lit_03",
                    learningOutcomeId = "lo_lit_03",
                    questionHindi = "किताब (Book) को संथाली में क्या कहते हैं?",
                    questionSantali = "किताब ᱫᱚ ᱥᱟᱱᱛᱟᱲᱤ ᱛᱮ ᱪᱮᱫ ᱠᱚ ᱢᱮᱛᱟᱜᱼᱟ?",
                    questionPhonetic = "किताब दो संताड़ी ते चेद को मेताआ?",
                    optionsHindi = listOf("ᱫᱟᱜ (पानी)", "ᱯᱩᱛᱷᱤ (किताब)", "ᱡᱚ (फल)"),
                    optionsSantali = listOf("ᱫᱟᱜ", "ᱯᱩᱛᱷᱤ", "ᱡᱚ"),
                    correctOptionIndex = 1,
                    explanationHindi = "किताब को संथाली में ᱯᱩᱛᱷᱤ (पुथि) कहते हैं。",
                    difficulty = DifficultyLevel.BEGINNER
                )
            )
        )
    )

    fun getLessonById(id: String): Lesson? = lessons.find { it.id == id }

    fun getLessonsByDomain(domain: FLNDomain): List<Lesson> = lessons.filter { it.domain == domain }

    fun generateWorksheetForLesson(lessonId: String, difficulty: DifficultyLevel = DifficultyLevel.BEGINNER): Worksheet? {
        val lesson = getLessonById(lessonId) ?: return null
        return TemplateContentGenerator.generateWorksheet(
            outcome = lesson.learningOutcome,
            grade = lesson.grade,
            domain = lesson.domain,
            numQuestions = 4,
            difficulty = difficulty
        )
    }
}
