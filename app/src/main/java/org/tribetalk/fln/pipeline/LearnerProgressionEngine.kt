package org.tribetalk.fln.pipeline

import org.tribetalk.fln.model.FlnGrade
import org.tribetalk.fln.model.WorksheetDifficulty

/**
 * Trilingual phrase and sentence expansion for a given semantic object.
 */
data class LinguisticProgressionContent(
    val objectKey: String,
    val wordSantali: String,
    val wordHindi: String,
    val wordEnglish: String,
    
    // Tier 3: Collocation / Phrase
    val phraseSantali: String,
    val phraseHindi: String,
    val phraseEnglish: String,
    
    // Tier 4: Fluency Sentence (NIPUN Grade 1)
    val sentenceSantali: String,
    val sentenceHindi: String,
    val sentenceEnglish: String
)

/**
 * Engine that manages continuous learning trajectories.
 * Progresses the child from:
 * Isolated Akshar -> Core Word -> Descriptive Phrase -> Fluency Sentence.
 */
object LearnerProgressionEngine {

    // Curated progression dictionary for common village and nature items
    private val progressionCatalog = mapOf(
        "mango" to LinguisticProgressionContent(
            objectKey = "mango",
            wordSantali = "ᱩᱞ",
            wordHindi = "आम",
            wordEnglish = "Mango",
            phraseSantali = "ᱦᱮᱲᱮᱢ ᱩᱞ",
            phraseHindi = "मीठा आम",
            phraseEnglish = "Sweet Mango",
            sentenceSantali = "ᱩᱞ ᱫᱚ ᱟᱹᱰᱤ ᱦᱮᱲᱮᱢ ᱜᱮᱭᱟ᱾",
            sentenceHindi = "आम बहुत मीठा है।",
            sentenceEnglish = "The mango is very sweet."
        ),
        "banana" to LinguisticProgressionContent(
            objectKey = "banana",
            wordSantali = "ᱠᱟᱭᱨᱟ",
            wordHindi = "केला",
            wordEnglish = "Banana",
            phraseSantali = "ᱵᱮᱞᱮ ᱠᱟᱭᱨᱟ",
            phraseHindi = "पका केला",
            phraseEnglish = "Ripe Banana",
            sentenceSantali = "ᱦᱟᱹᱺᱬᱩ ᱫᱚ ᱠᱟᱭᱨᱟᱭ ᱡᱚᱢᱟ᱾",
            sentenceHindi = "बंदर केला खाता है।",
            sentenceEnglish = "The monkey eats banana."
        ),
        "dog" to LinguisticProgressionContent(
            objectKey = "dog",
            wordSantali = "ᱥᱮᱛᱟ",
            wordHindi = "कुत्ता",
            wordEnglish = "Dog",
            phraseSantali = "ᱦᱮᱸᱫᱮ ᱥᱮᱛᱟ",
            phraseHindi = "काला कुत्ता",
            phraseEnglish = "Black Dog",
            sentenceSantali = "ᱥᱮᱛᱟ ᱫᱚ ᱚᱲᱟᱜ ᱮ ᱨᱩᱠᱷᱤᱭᱟᱹᱭᱟ᱾",
            sentenceHindi = "कुत्ता घर की रखवाली करता है।",
            sentenceEnglish = "The dog guards the house."
        ),
        "elephant" to LinguisticProgressionContent(
            objectKey = "elephant",
            wordSantali = "ᱦᱟᱹᱛᱤ",
            wordHindi = "हाथी",
            wordEnglish = "Elephant",
            phraseSantali = "ᱢᱟᱨᱟᱝ ᱦᱟᱹᱛᱤ",
            phraseHindi = "बड़ा हाथी",
            phraseEnglish = "Big Elephant",
            sentenceSantali = "ᱦᱟᱹᱛᱤ ᱫᱚ ᱵᱤᱨ ᱨᱮ ᱛᱟᱦᱮᱸᱱᱟᱭ᱾",
            sentenceHindi = "हाथी जंगल में रहता है।",
            sentenceEnglish = "The elephant lives in the forest."
        ),
        "clay_pot" to LinguisticProgressionContent(
            objectKey = "clay_pot",
            wordSantali = "ᱴᱩᱠᱩᱡ",
            wordHindi = "घड़ा",
            wordEnglish = "Clay Pot",
            phraseSantali = "ᱫᱟᱜ ᱴᱩᱠᱩᱡ",
            phraseHindi = "पानी का घड़ा",
            phraseEnglish = "Water Pot",
            sentenceSantali = "ᱴᱩᱠᱩᱡ ᱨᱮ ᱨᱮᱭᱟᱲ ᱫᱟᱜ ᱢᱮᱱᱟᱜᱼᱟ᱾",
            sentenceHindi = "घड़े में ठंडा पानी है।",
            sentenceEnglish = "There is cold water in the pot."
        ),
        "sal_leaf" to LinguisticProgressionContent(
            objectKey = "sal_leaf",
            wordSantali = "ᱥᱟᱨᱡᱚᱢ ᱥᱟᱠᱟᱢ",
            wordHindi = "साल का पत्ता",
            wordEnglish = "Sal Leaf",
            phraseSantali = "ᱦᱟᱹᱨᱭᱟᱹᱲ ᱥᱟᱠᱟᱢ",
            phraseHindi = "हरा पत्ता",
            phraseEnglish = "Green Leaf",
            sentenceSantali = "ᱥᱟᱨᱡᱚᱢ ᱥᱟᱠᱟᱢ ᱛᱮ ᱠᱷᱟᱹᱞᱟᱹ ᱵᱮᱱᱟᱣᱜᱼᱟ᱾",
            sentenceHindi = "साल के पत्तों से पत्तल बनती है।",
            sentenceEnglish = "Plates are made from sal leaves."
        ),
        "peacock" to LinguisticProgressionContent(
            objectKey = "peacock",
            wordSantali = "ᱢᱟᱨᱟᱜ",
            wordHindi = "मोर",
            wordEnglish = "Peacock",
            phraseSantali = "ᱪᱮᱦᱨᱟ ᱢᱟᱨᱟᱜ",
            phraseHindi = "सुंदर मोर",
            phraseEnglish = "Beautiful Peacock",
            sentenceSantali = "ᱢᱟᱨᱟᱜ ᱫᱚ ᱫᱟᱜ ᱫᱤᱱ ᱨᱮ ᱮᱱᱮᱡᱼᱟᱭ᱾",
            sentenceHindi = "मोर बारिश में नाचता है।",
            sentenceEnglish = "The peacock dances in the rain."
        ),
        "tumdak_drum" to LinguisticProgressionContent(
            objectKey = "tumdak_drum",
            wordSantali = "ᱛᱩᱢᱫᱟᱜ",
            wordHindi = "मांदर (ढोल)",
            wordEnglish = "Tumdak Drum",
            phraseSantali = "ᱨᱩ ᱛᱩᱢᱫᱟᱜ",
            phraseHindi = "बजता मांदर",
            phraseEnglish = "Beating Drum",
            sentenceSantali = "ᱥᱚᱦᱨᱟᱭ ᱯᱚᱨᱚᱵᱽ ᱨᱮ ᱛᱩᱢᱫᱟᱜ ᱨᱩᱭᱟᱹᱠᱚ᱾",
            sentenceHindi = "सोहराय पर्व में मांदर बजाते हैं।",
            sentenceEnglish = "They beat the tumdak drum during Sohrai festival."
        ),
        "fish" to LinguisticProgressionContent(
            objectKey = "fish",
            wordSantali = "ᱦᱟᱹᱠᱩ",
            wordHindi = "मछली",
            wordEnglish = "Fish",
            phraseSantali = "ᱜᱟᱰᱟ ᱦᱟᱹᱠᱩ",
            phraseHindi = "नदी की मछली",
            phraseEnglish = "River Fish",
            sentenceSantali = "ᱦᱟᱹᱠᱩ ᱫᱚ ᱫᱟᱜ ᱨᱮᱠᱚ ᱯᱟᱭᱨᱟᱜᱼᱟ᱾",
            sentenceHindi = "मछली पानी में तैरती है।",
            sentenceEnglish = "Fish swim in the water."
        ),
        "cow" to LinguisticProgressionContent(
            objectKey = "cow",
            wordSantali = "ᱜᱟᱹᱭ",
            wordHindi = "गाय",
            wordEnglish = "Cow",
            phraseSantali = "ᱯᱩᱸᱰ ᱜᱟᱹᱭ",
            phraseHindi = "सफेद गाय",
            phraseEnglish = "White Cow",
            sentenceSantali = "ᱜᱟᱹᱭ ᱫᱚ ᱛᱚᱣᱟᱭ ᱮᱢᱚᱜᱼᱟ᱾",
            sentenceHindi = "गाय दूध देती है।",
            sentenceEnglish = "The cow gives milk."
        ),
        "apple" to LinguisticProgressionContent(
            objectKey = "apple",
            wordSantali = "ᱟᱯᱮᱞ",
            wordHindi = "सेब",
            wordEnglish = "Apple",
            phraseSantali = "ᱟᱨᱟᱜ ᱟᱯᱮᱞ",
            phraseHindi = "लाल सेब",
            phraseEnglish = "Red Apple",
            sentenceSantali = "ᱟᱯᱮᱞ ᱫᱚ ᱦᱚᱲᱢᱚ ᱞᱟᱹᱜᱤᱫ ᱵᱮᱥ ᱜᱮᱭᱟ᱾",
            sentenceHindi = "सेब सेहत के लिए अच्छा है।",
            sentenceEnglish = "The apple is good for health."
        ),
        "mahua_flower" to LinguisticProgressionContent(
            objectKey = "mahua_flower",
            wordSantali = "ᱢᱟᱹᱛᱠᱚᱢ",
            wordHindi = "महुआ का फूल",
            wordEnglish = "Mahua Flower",
            phraseSantali = "ᱵᱤᱨ ᱢᱟᱹᱛᱠᱚᱢ",
            phraseHindi = "जंगल का महुआ",
            phraseEnglish = "Forest Mahua",
            sentenceSantali = "ᱪᱮᱛ ᱵᱚᱸᱜᱟ ᱨᱮ ᱢᱟᱹᱛᱠᱚᱢ ᱧᱩᱨᱩᱜᱼᱟ᱾",
            sentenceHindi = "चैत के महीने में महुआ टपकता है।",
            sentenceEnglish = "Mahua blossoms fall in the spring."
        ),
        "straw_hut" to LinguisticProgressionContent(
            objectKey = "straw_hut",
            wordSantali = "ᱚᱲᱟᱜ",
            wordHindi = "झोपड़ी (घर)",
            wordEnglish = "Village Hut",
            phraseSantali = "ᱥᱟᱹᱣᱲᱤ ᱚᱲᱟᱜ",
            phraseHindi = "फूस का घर",
            phraseEnglish = "Thatched House",
            sentenceSantali = "ᱟᱞᱮ ᱚᱲᱟᱜ ᱫᱚ ᱵᱤᱨ ᱥᱩᱨ ᱨᱮ ᱢᱮᱱᱟᱜᱼᱟ᱾",
            sentenceHindi = "हमारा घर जंगल के पास है।",
            sentenceEnglish = "Our house is near the forest."
        ),
        "sickle" to LinguisticProgressionContent(
            objectKey = "sickle",
            wordSantali = "ᱫᱟᱛᱨᱚᱢ",
            wordHindi = "हंसिया",
            wordEnglish = "Sickle",
            phraseSantali = "ᱞᱟᱥᱮᱨ ᱫᱟᱛᱨᱚᱢ",
            phraseHindi = "धारदार हंसिया",
            phraseEnglish = "Sharp Sickle",
            sentenceSantali = "ᱫᱟᱛᱨᱚᱢ ᱛᱮ ᱤᱨᱚᱜ ᱦᱳᱲᱳ ᱤᱨᱟᱹᱠᱚ᱾",
            sentenceHindi = "हंसिया से पकी धान काटते हैं।",
            sentenceEnglish = "They harvest ripe paddy with a sickle."
        ),
        "pebble" to LinguisticProgressionContent(
            objectKey = "pebble",
            wordSantali = "ᱫᱷᱤᱨᱤ",
            wordHindi = "पत्थर (कंकड़)",
            wordEnglish = "Pebble",
            phraseSantali = "ᱜᱩᱞᱟᱹᱴ ᱫᱷᱤᱨᱤ",
            phraseHindi = "गोल कंकड़",
            phraseEnglish = "Round Pebble",
            sentenceSantali = "ᱜᱤᱫᱽᱨᱟᱹ ᱫᱷᱤᱨᱤ ᱛᱮᱠᱚ ᱞᱮᱠᱷᱟᱭᱟ᱾",
            sentenceHindi = "बच्चे कंकड़ों से गिनती सीखते हैं।",
            sentenceEnglish = "Children count using pebbles."
        ),
        "star" to LinguisticProgressionContent(
            objectKey = "star",
            wordSantali = "ᱤᱯᱤᱞ",
            wordHindi = "तारा",
            wordEnglish = "Star",
            phraseSantali = "ᱡᱩᱞᱩᱜ ᱤᱯᱤᱞ",
            phraseHindi = "चमकता तारा",
            phraseEnglish = "Shining Star",
            sentenceSantali = "ᱧᱤᱫᱟᱹ ᱥᱮᱨᱢᱟ ᱨᱮ ᱤᱯᱤᱞ ᱠᱚ ᱡᱩᱞᱩᱜᱼᱟ᱾",
            sentenceHindi = "रात में आकाश में तारे चमकते हैं।",
            sentenceEnglish = "Stars shine in the night sky."
        ),
        "circle" to LinguisticProgressionContent(
            objectKey = "circle",
            wordSantali = "ᱜᱩᱞᱟᱹᱴ",
            wordHindi = "वृत्त (गोला)",
            wordEnglish = "Circle",
            phraseSantali = "ᱜᱩᱞᱟᱹᱴ ᱪᱟᱸᱫᱚ",
            phraseHindi = "गोल चाँद",
            phraseEnglish = "Round Moon",
            sentenceSantali = "ᱪᱟᱸᱫᱚ ᱫᱚ ᱜᱩᱞᱟᱹᱴ ᱜᱮ ᱧᱮᱞᱚᱜᱼᱟᱭ᱾",
            sentenceHindi = "चाँद गोल दिखाई देता है।",
            sentenceEnglish = "The moon looks round."
        ),
        "square" to LinguisticProgressionContent(
            objectKey = "square",
            wordSantali = "ᱪᱟᱹᱣᱠᱟᱹ",
            wordHindi = "वर्ग (चौकोर)",
            wordEnglish = "Square",
            phraseSantali = "ᱪᱟᱹᱣᱠᱟᱹ ᱥᱞᱮᱴ",
            phraseHindi = "चौकोर स्लेट",
            phraseEnglish = "Square Slate",
            sentenceSantali = "ᱤᱧᱟᱹᱜ ᱥᱞᱮᱴ ᱫᱚ ᱪᱟᱹᱣᱠᱟᱹ ᱜᱮᱭᱟ᱾",
            sentenceHindi = "मेरी स्लेट चौकोर है।",
            sentenceEnglish = "My slate is square."
        ),
        "triangle" to LinguisticProgressionContent(
            objectKey = "triangle",
            wordSantali = "ᱯᱮᱠᱳᱬ",
            wordHindi = "त्रिभुज",
            wordEnglish = "Triangle",
            phraseSantali = "ᱯᱮᱠᱳᱬ ᱪᱤᱱᱦᱟᱹ",
            phraseHindi = "तीन कोनों का चिह्न",
            phraseEnglish = "Triangle Sign",
            sentenceSantali = "ᱯᱮᱠᱳᱬ ᱨᱮ ᱯᱮᱭᱟ ᱠᱳᱬ ᱛᱟᱦᱮᱸᱱᱟ᱾",
            sentenceHindi = "त्रिभुज में तीन कोने होते हैं।",
            sentenceEnglish = "A triangle has three corners."
        )
    )

    /**
     * Looks up progression content for an object.
     */
    fun getContent(objectKey: String): LinguisticProgressionContent? {
        return progressionCatalog[objectKey.lowercase()]
    }

    /**
     * Evaluates learner interaction and returns updated state.
     * Automatically triggers tier advancement when streak thresholds are reached.
     */
    fun recordActivityCompletion(
        currentState: LearnerState,
        activity: ActivityIR,
        isSuccess: Boolean
    ): LearnerState {
        if (!isSuccess) {
            return currentState.copy(consecutiveSuccessStreak = 0)
        }

        val newStreak = currentState.consecutiveSuccessStreak + 1
        val newTotal = currentState.totalActivitiesCompleted + 1
        val updatedMasteredKeys = currentState.masteredObjectKeys + activity.primaryObjectKey

        // Advance tier on consistent streak (every 2 consecutive successes)
        val nextTier = if (newStreak >= 2) {
            when (currentState.currentTier) {
                LinguisticTier.ISOLATED_AKSHAR -> LinguisticTier.CORE_VOCABULARY
                LinguisticTier.CORE_VOCABULARY -> LinguisticTier.DESCRIPTIVE_PHRASE
                LinguisticTier.DESCRIPTIVE_PHRASE -> LinguisticTier.FLUENCY_SENTENCE
                LinguisticTier.FLUENCY_SENTENCE -> LinguisticTier.CONNECTED_STORY
                LinguisticTier.CONNECTED_STORY -> LinguisticTier.CONNECTED_STORY
            }
        } else {
            currentState.currentTier
        }

        return currentState.copy(
            currentTier = nextTier,
            consecutiveSuccessStreak = if (nextTier != currentState.currentTier) 0 else newStreak,
            totalActivitiesCompleted = newTotal,
            masteredObjectKeys = updatedMasteredKeys
        )
    }

    /**
     * Generates a "Next Phrase Challenge" ActivityIR expanding from a mastered word.
     */
    fun generateNextPhraseActivity(
        objectKey: String,
        targetTier: LinguisticTier = LinguisticTier.DESCRIPTIVE_PHRASE,
        grade: FlnGrade = FlnGrade.GRADE_1
    ): ActivityIR {
        val content = getContent(objectKey) ?: LinguisticProgressionContent(
            objectKey = objectKey,
            wordSantali = objectKey,
            wordHindi = objectKey,
            wordEnglish = objectKey,
            phraseSantali = "$objectKey (ᱥᱟᱱᱛᱟᱲᱤ)",
            phraseHindi = "$objectKey (हिंदी)",
            phraseEnglish = "$objectKey phrase",
            sentenceSantali = "$objectKey ᱫᱚ ᱵᱮᱥ ᱜᱮᱭᱟ᱾",
            sentenceHindi = "$objectKey अच्छा है।",
            sentenceEnglish = "The $objectKey is good."
        )

        return when (targetTier) {
            LinguisticTier.DESCRIPTIVE_PHRASE -> {
                ActivityIR(
                    id = "prog_phrase_${objectKey}_${System.currentTimeMillis()}",
                    nipunCompetencyCode = "L-G1.2",
                    actionType = ActivityActionType.PICTURE_WORD_MATCH,
                    grade = grade,
                    difficulty = WorksheetDifficulty.EASY,
                    linguisticTier = LinguisticTier.DESCRIPTIVE_PHRASE,
                    primaryObjectKey = objectKey,
                    correctValue = content.phraseSantali,
                    distractorOptions = listOf(
                        content.phraseSantali,
                        "ᱦᱮᱸᱫᱮ ᱫᱟᱨᱮ", // Distractor: Black tree
                        "ᱨᱮᱭᱟᱲ ᱫᱟᱜ"   // Distractor: Cold water
                    ).shuffled(),
                    santaliWord = content.wordSantali,
                    hindiWord = content.wordHindi,
                    englishWord = content.wordEnglish,
                    bilingualPhraseSantali = content.phraseSantali,
                    bilingualPhraseHindi = content.phraseHindi,
                    phraseEnglishGloss = content.phraseEnglish,
                    instructionSantali = "ᱪᱤᱛᱟᱹᱨ ᱧᱮᱞ ᱠᱟᱛᱮ ᱥᱟᱹᱦᱤ ᱟᱹᱲᱟᱹ ᱡᱚᱲ (Phrase) ᱵᱟᱪᱷᱟᱣ ᱢᱮ:",
                    instructionHindi = "चित्र देखकर सही पदबंध (Phrase) चुनें:",
                    teacherSolutionNote = "Phrase: ${content.phraseSantali} (${content.phraseHindi})"
                )
            }

            LinguisticTier.FLUENCY_SENTENCE -> {
                ActivityIR(
                    id = "prog_sent_${objectKey}_${System.currentTimeMillis()}",
                    nipunCompetencyCode = "L-G1.4",
                    actionType = ActivityActionType.PICTURE_WORD_MATCH,
                    grade = grade,
                    difficulty = WorksheetDifficulty.MEDIUM,
                    linguisticTier = LinguisticTier.FLUENCY_SENTENCE,
                    primaryObjectKey = objectKey,
                    correctValue = content.sentenceSantali,
                    distractorOptions = listOf(
                        content.sentenceSantali,
                        "ᱥᱮᱛᱟ ᱫᱚ ᱜᱟᱰᱟ ᱨᱮ ᱯᱟᱭᱨᱟᱜᱼᱟᱭ᱾",
                        "ᱫᱟᱨᱮ ᱨᱮ ᱡᱚ ᱵᱟᱹᱱᱩᱜᱼᱟ᱾"
                    ).shuffled(),
                    santaliWord = content.wordSantali,
                    hindiWord = content.wordHindi,
                    englishWord = content.wordEnglish,
                    sentenceSantali = content.sentenceSantali,
                    sentenceHindi = content.sentenceHindi,
                    bilingualPhraseSantali = content.phraseSantali,
                    bilingualPhraseHindi = content.phraseHindi,
                    phraseEnglishGloss = content.sentenceEnglish,
                    instructionSantali = "ᱱᱚᱣᱟ ᱪᱤᱛᱟᱹᱨ ᱞᱟᱹᱜᱤᱫ ᱥᱟᱹᱦᱤ ᱟᱹᱭᱟᱹᱛ (Sentence) ᱵᱟᱪᱷᱟᱣ ᱢᱮ:",
                    instructionHindi = "इस चित्र के लिए सही वाक्य (Sentence) चुनें:",
                    teacherSolutionNote = "Sentence: ${content.sentenceSantali} (${content.sentenceHindi})"
                )
            }

            else -> {
                // Default word level
                ActivityIR(
                    id = "prog_word_${objectKey}_${System.currentTimeMillis()}",
                    nipunCompetencyCode = "L-BAL.1",
                    actionType = ActivityActionType.PICTURE_WORD_MATCH,
                    grade = grade,
                    difficulty = WorksheetDifficulty.EASY,
                    linguisticTier = LinguisticTier.CORE_VOCABULARY,
                    primaryObjectKey = objectKey,
                    correctValue = content.wordSantali,
                    distractorOptions = listOf(content.wordSantali, "ᱫᱟᱨᱮ", "ᱜᱟᱰᱟ").shuffled(),
                    santaliWord = content.wordSantali,
                    hindiWord = content.wordHindi,
                    englishWord = content.wordEnglish,
                    bilingualPhraseSantali = content.phraseSantali,
                    bilingualPhraseHindi = content.phraseHindi,
                    instructionSantali = "ᱥᱟᱹᱦᱤ ᱟᱹᱲᱟᱹ ᱵᱟᱪᱷᱟᱣ ᱢᱮ:",
                    instructionHindi = "सही शब्द चुनें:"
                )
            }
        }
    }
}
