package com.alchemists.tribetalk.lessons

import com.alchemists.tribetalk.translation.FLNCurriculumDatabase
import com.alchemists.tribetalk.translation.Language
import com.alchemists.tribetalk.translation.TranslationEngine

/**
 * Repository providing FLN curriculum demonstration lessons and section re-translation.
 */
object LessonRepository {

    private val lessons: MutableList<Lesson> = mutableListOf(
        // ========================================================
        // 1. FOUNDATIONAL LITERACY & ORAL LANGUAGE (FLN Goal 2)
        // ========================================================
        Lesson(
            id = "lesson_greetings",
            title = "Greetings and Introductions (नमस्ते एवं परिचय)",
            topic = "Greetings",
            grade = "Grade 1",
            learningDomain = "Foundational Literacy & Oral Language",
            learningOutcome = "Oral Vocabulary & Social Interaction (मौखिक संवाद)",
            hindiIntroduction = "कक्षा में एक-दूसरे का संथाली और हिन्दी में अभिवादन करना सीखें।",
            santaliIntroduction = "ᱠᱞᱟᱥ ᱨᱮ ᱡᱚᱦᱟᱨ ᱟᱨ ᱟᱯᱱᱟᱨ ᱧᱩᱛᱩᱢ ᱞᱟᱹᱭ ᱪᱮᱫᱚᱜ ᱢᱮ। (Learn to greet in Santali and state your name.)",
            lessonScriptHindi = "नमस्ते बच्चों! मेरा नाम शिक्षक है। आप कैसे हैं? अपनी किताब खोलो।",
            lessonScriptSantali = "ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ! ᱤᱧᱟᱜ ᱧᱩᱛᱩᱢ ᱢᱟᱪᱮᱛ। ᱪᱮᱠᱟ ᱢᱮᱱᱟᱢᱟ? ᱟᱢᱟᱜ ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ।",
            activityInstructionsHindi = "शिक्षक के साथ 'जोहार' बोलें और अपना नाम संथाली में बताएं।",
            activityInstructionsSantali = "ᱢᱟᱪᱮᱛ ᱥᱟᱶ 'ᱡᱚᱦᱟᱨ' ᱢᱮᱱ ᱢᱮ ᱟᱨ ᱟᱢᱟᱜ ᱧᱩᱛᱩᱢ ᱥᱟᱱᱛᱟᱲᱤ ᱛᱮ ᱞᱟᱹᱭ ᱢᱮ।",
            assessmentPromptsHindi = "प्रत्येक छात्र अपना नाम संथाली में 'इञाग ञुतुम...' के साथ बोले।",
            assessmentPromptsSantali = "ᱡᱚᱛᱚ ᱯᱟᱹᱲᱦᱩᱣᱟᱹ ᱟᱠᱚᱣᱟᱜ ᱧᱩᱛᱩᱢ 'ᱤᱧᱟᱜ ᱧᱩᱛᱩᱢ...' ᱛᱮ ᱠᱚ ᱞᱟᱹᱭᱟ।"
        ),
        Lesson(
            id = "lesson_vocab",
            title = "Basic Vocabulary & Daily Objects (शब्दावली एवं दैनिक वस्तुएं)",
            topic = "Classroom Objects",
            grade = "Grade 1",
            learningDomain = "Foundational Literacy & Oral Language",
            learningOutcome = "Word-Picture Association (शब्द-चित्र संबंध)",
            hindiIntroduction = "कक्षा की मुख्य वस्तुओं (किताब, कलम, पानी, घर) को पहचानें।",
            santaliIntroduction = "ᱠᱞᱟᱥ ᱨᱮᱭᱟᱜ ᱡᱤᱱᱤᱥ (ᱯᱩᱛᱷᱤ, ᱠᱚᱞᱚᱢ, ᱫᱟᱜ, ᱚᱲᱟᱜ) ᱪᱤᱱᱦᱟᱹᱣ ᱢᱮ।",
            lessonScriptHindi = "यह किताब है। यह कलम है। पानी पियो। हम स्कूल में हैं।",
            lessonScriptSantali = "ᱱᱚᱶᱟ ᱫᱚ ᱯᱩᱛᱷᱤ ᱠᱟᱱᱟ। ᱱᱚᱶᱟ ᱫᱚ ᱠᱚᱞᱚᱢ ᱠᱟᱱᱟ। ᱫᱟᱜ ᱧᱩᱭ ᱢᱮ। ᱟᱞᱮ ᱤᱛᱩᱱ ᱚᱲᱟᱜ ᱨᱮ ᱢᱮᱱᱟᱜ ᱞᱮᱭᱟ।",
            activityInstructionsHindi = "वस्तु को देखकर उसका संथाली नाम दोहराएं।",
            activityInstructionsSantali = "ᱡᱤᱱᱤᱥ ᱧᱮᱞ ᱠᱟᱛᱮ ᱥᱟᱱᱛᱟᱲᱤ ᱧᱩᱛᱩᱢ ᱫᱚᱦᱲᱟᱭ ᱢᱮ।",
            assessmentPromptsHindi = "'कलम' और 'किताब' का संथाली में सही नाम बताएं।",
            assessmentPromptsSantali = "'ᱠᱚᱞᱚᱢ' ᱟᱨ 'ᱯᱩᱛᱷᱤ' ᱨᱮᱭᱟᱜ ᱥᱟᱹᱨᱤ ᱧᱩᱛᱩᱢ ᱞᱟᱹᱭ ᱢᱮ।"
        ),
        Lesson(
            id = "lesson_sound_awareness",
            title = "Letter & Sound Awareness (ध्वनि एवं Ol Chiki लिपि समझ)",
            topic = "Phonetics & Script",
            grade = "Grade 2",
            learningDomain = "Foundational Literacy & Oral Language",
            learningOutcome = "Phonological & Ol Chiki Awareness (ध्वनि एवं लिपि समझ)",
            hindiIntroduction = "संथाली की ओल चिकी लिपि और प्रमुख ध्वनियों का अभ्यास।",
            santaliIntroduction = "ᱥᱟᱱᱛᱟᱲᱤ ᱚᱞ ᱪᱤᱠᱤ ᱟᱨ ᱟᱲᱟᱝ ᱠᱚ ᱯᱟᱲᱦᱟᱣ ᱢᱮ।",
            lessonScriptHindi = "ध्यान से सुनो। लिखना शुरू करो। बहुत अच्छा काम।",
            lessonScriptSantali = "ᱫᱷᱮᱭᱟᱱ ᱛᱮ ᱟᱧᱡᱚᱢ ᱢᱮ। ᱚᱞ ᱮᱦᱚᱵ ᱢᱮ। ᱟᱹᱰᱤ ᱱᱟᱯᱟᱭ ᱠᱟᱹᱢᱤ।",
            activityInstructionsHindi = "ओल चिकी वर्णमाला को अपनी कॉपी में लिखें।",
            activityInstructionsSantali = "ᱚᱞ ᱪᱤᱠᱤ ᱟᱠᱷᱚᱨ ᱠᱚ ᱟᱢᱟᱜ ᱠᱷᱟᱛᱟ ᱨᱮ ᱚᱞ ᱢᱮ।",
            assessmentPromptsHindi = "बोले गए शब्द का पहला अक्षर पहचानें।",
            assessmentPromptsSantali = "ᱨᱚᱲ ᱟᱠᱟᱱ ᱟᱹᱲᱟᱹ ᱨᱮᱭᱟᱜ ᱯᱩᱭᱞᱩ ᱟᱠᱷᱚᱨ ᱪᱤᱱᱦᱟᱹᱣ ᱢᱮ।"
        ),
        Lesson(
            id = "lesson_sentences",
            title = "Simple Sentences (सरल वाक्य रचना)",
            topic = "Classroom Commands",
            grade = "Grade 2",
            learningDomain = "Foundational Literacy & Oral Language",
            learningOutcome = "Classroom Dialogue & Comprehension (कक्षा संवाद)",
            hindiIntroduction = "कक्षा में सामान्य निर्देशों को समझकर उनका पालन करना।",
            santaliIntroduction = "ᱠᱞᱟᱥ ᱨᱮᱭᱟᱜ ᱟᱫᱮᱥ ᱵᱩᱡᱷᱟᱹᱣ ᱠᱟᱛᱮ ᱠᱟᱹᱢᱤ ᱢᱮ।",
            lessonScriptHindi = "किताब खोलो और पढ़ो। अपना काम पूरा करो। फिर से समझाइए।",
            lessonScriptSantali = "ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ ᱟᱨ ᱯᱟᱲᱦᱟᱣ ᱢᱮ। ᱟᱢᱟᱜ ᱠᱟᱹᱢᱤ ᱯᱩᱨᱟᱹᱣ ᱢᱮ। ᱟᱨᱦᱚᱸ ᱵᱩᱡᱷᱟᱹᱣ ᱢᱮ।",
            activityInstructionsHindi = "शिक्षक द्वारा दिए गए निर्देश का पालन करें।",
            activityInstructionsSantali = "ᱢᱟᱪᱮᱛ ᱮᱢ ᱟᱠᱟᱫ ᱦᱩᱠᱩᱢ ᱢᱟᱱᱟᱣ ᱢᱮ।",
            assessmentPromptsHindi = "'किताब खोलो' का संथाली अनुवाद बोलकर दिखाएं।",
            assessmentPromptsSantali = "'ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ' ᱥᱟᱱᱛᱟᱲᱤ ᱛᱮ ᱨᱚᱲ ᱢᱮ।"
        ),

        // ========================================================
        // 2. FOUNDATIONAL NUMERACY & MATHEMATICS (FLN Goal 3)
        // ========================================================
        Lesson(
            id = "lesson_counting",
            title = "Counting 1–10 (संख्या ज्ञान एवं १-१० गिनती)",
            topic = "Numbers 1-10",
            grade = "Grade 1",
            learningDomain = "Foundational Numeracy & Mathematical Thinking",
            learningOutcome = "Number Recognition & Counting (संख्या पहचान एवं गिनती)",
            hindiIntroduction = "एक से दस तक की गिनती संथाली और हिन्दी में सीखें।",
            santaliIntroduction = "ᱢᱤᱫ ᱠᱷᱚᱱ ᱜᱮᱞ ᱫᱷᱟᱹᱵᱤᱡ ᱞᱮᱠᱷᱟ ᱥᱟᱱᱛᱟᱲᱤ ᱟᱨ ᱦᱤᱱᱫᱤ ᱛᱮ ᱪᱮᱫᱚᱜ ᱢᱮ।",
            lessonScriptHindi = "गिनती करो: एक, दो, तीन, चार, पांच। कितने हैं? पांच हैं।",
            lessonScriptSantali = "ᱞᱮᱠᱷᱟᱭ ᱢᱮ: ᱢᱤᱫ, ᱵᱟᱨ, ᱯᱮ, ᱯᱩᱱ, ᱢᱚᱬᱮ। ᱛᱤᱱᱟᱹᱜ ᱢᱮᱱᱟᱜ-ᱟ? ᱢᱚᱬᱮ ᱢᱮᱱᱟᱜ-ᱟ।",
            activityInstructionsHindi = "उंगलियों पर संथाली में १ से ५ तक गिनें।",
            activityInstructionsSantali = "ᱠᱟᱹᱴᱩᱵ ᱛᱮ ᱥᱟᱱᱛᱟᱲᱤ ᱛᱮ ᱑ ᱠᱷᱚᱱ ᱕ ᱫᱷᱟᱹᱵᱤᱡ ᱞᱮᱠᱷᱟᱭ ᱢᱮ।",
            assessmentPromptsHindi = "कक्षा में रखी ५ पेंसिलों को संथाली में गिनकर बताएं।",
            assessmentPromptsSantali = "ᱠᱞᱟᱥ ᱨᱮ ᱢᱮᱱᱟᱜ ᱕ ᱜᱚᱴᱟᱝ ᱯᱮᱱᱥᱤᱞ ᱥᱟᱱᱛᱟᱲᱤ ᱛᱮ ᱞᱮᱠᱷᱟ ᱠᱟᱛᱮ ᱞᱟᱹᱭ ᱢᱮ।"
        ),
        Lesson(
            id = "lesson_addition",
            title = "Number Recognition & Addition (संख्या पहचान एवं जोड़)",
            topic = "Basic Math",
            grade = "Grade 2",
            learningDomain = "Foundational Numeracy & Mathematical Thinking",
            learningOutcome = "Basic Quantities & Addition (मात्रा एवं जोड़ समझ)",
            hindiIntroduction = "सरल वस्तुओं को जोड़ना और संथाली में उत्तर देना।",
            santaliIntroduction = "ᱡᱤᱱᱤᱥ ᱠᱚ ᱢᱮᱥᱟ ᱠᱟᱛᱮ ᱥᱟᱱᱛᱟᱲᱤ ᱛᱮ ᱛᱮᱞᱟ ᱮᱢ ᱢᱮ।",
            lessonScriptHindi = "जोड़ो: दो और दो कितने हैं? दो और दो चार हैं।",
            lessonScriptSantali = "ᱢᱮᱥᱟᱭ ᱢᱮ: ᱵᱟᱨ ᱟᱨ ᱵᱟᱨ ᱛᱤᱱᱟᱹᱜ ᱠᱟᱱᱟ? ᱵᱟᱨ ᱟᱨ ᱵᱟᱨ ᱯᱩᱱ ᱠᱟᱱᱟ।",
            activityInstructionsHindi = "पत्थरों या कंकड़ों को गिनकर जोड़ें।",
            activityInstructionsSantali = "ᱫᱷᱤᱨᱤ ᱞᱮᱠᱷᱟ ᱠᱟᱛᱮ ᱢᱮᱥᱟᱭ ᱢᱮ।",
            assessmentPromptsHindi = "'जोड़ो' का संथाली अर्थ बताएं।",
            assessmentPromptsSantali = "'ᱢᱮᱥᱟᱭ ᱢᱮ' ᱨᱮᱭᱟᱜ ᱦᱤᱱᱫᱤ ᱢᱮᱱᱮᱛ ᱞᱟᱹᱭ ᱢᱮ।"
        ),
        Lesson(
            id = "lesson_shapes",
            title = "Shapes Around Us (हमारे आस-पास के आकार)",
            topic = "Geometry & Shapes",
            grade = "Grade 1",
            learningDomain = "Foundational Numeracy & Mathematical Thinking",
            learningOutcome = "Shapes & Geometric Concepts (आकार समझ)",
            hindiIntroduction = "गोल, चौकोर और अन्य आकारों की पहचान करना।",
            santaliIntroduction = "ᱜᱩᱞᱟᱹᱭ ᱟᱨ ᱪᱟᱹᱣᱠᱟᱹ ᱨᱩᱯ ᱪᱤᱱᱦᱟᱹᱣ ᱢᱮ।",
            lessonScriptHindi = "सूरज गोल है। किताब चौकोर है। पेड़ बड़ा है।",
            lessonScriptSantali = "ᱥᱤᱸᱜᱤ ᱫᱚ ᱜᱩᱞᱟᱹᱭ ᱜᱮᱭᱟ। ᱯᱩᱛᱷᱤ ᱫᱚ ᱪᱟᱹᱣᱠᱟᱹ ᱜᱮᱭᱟ। ᱫᱟᱨᱮ ᱫᱚ ᱢᱟᱨᱟᱝ ᱜᱮᱭᱟ।",
            activityInstructionsHindi = "कक्षा में मौजूद गोल वस्तुएं खोजें।",
            activityInstructionsSantali = "ᱠᱞᱟᱥ ᱨᱮ ᱢᱮᱱᱟᱜ ᱜᱩᱞᱟᱹᱭ ᱡᱤᱱᱤᱥ ᱯᱟᱱᱛᱮ ᱧᱟᱢ ᱢᱮ।",
            assessmentPromptsHindi = "सूरज और चाँद का आकार कैसा होता है?",
            assessmentPromptsSantali = "ᱥᱤᱸᱜᱤ ᱟᱨ ᱪᱟᱸᱫᱚ ᱨᱮᱭᱟᱜ ᱨᱩᱯ ᱪᱮᱛ ᱞᱮᱠᱟᱱᱟ?"
        )
    )

    fun getAllLessons(): List<Lesson> = lessons.toList()

    fun getLessonById(id: String): Lesson? = lessons.firstOrNull { it.id == id }

    fun searchLessons(query: String): List<Lesson> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return getAllLessons()
        return lessons.filter {
            it.title.lowercase().contains(trimmed) ||
            it.topic.lowercase().contains(trimmed) ||
            it.learningDomain.lowercase().contains(trimmed) ||
            it.learningOutcome.lowercase().contains(trimmed) ||
            it.lessonScriptHindi.lowercase().contains(trimmed)
        }
    }

    fun updateLesson(updated: Lesson) {
        val index = lessons.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            lessons[index] = updated
        } else {
            lessons.add(updated)
        }
    }

    /**
     * Translates a teacher-edited Hindi section using the existing TranslationEngine.
     */
    fun translateSection(hindiText: String, translationEngine: TranslationEngine): String {
        val trimmed = hindiText.trim()
        if (trimmed.isEmpty()) return ""

        val flnItem = FLNCurriculumDatabase.findByHindi(trimmed)
        if (flnItem != null) {
            return "${flnItem.santaliOlChiki} (${flnItem.latinPhonetic})"
        }

        try {
            val result = translationEngine.translate(trimmed, Language.HINDI, Language.SANTALI)
            if (result.matched && result.translatedText.isNotBlank() && !result.translatedText.contains("not available", ignoreCase = true)) {
                return result.translatedText
            }
        } catch (_: Exception) {
            // Graceful fallback
        }

        return "Translation unavailable"
    }
}
