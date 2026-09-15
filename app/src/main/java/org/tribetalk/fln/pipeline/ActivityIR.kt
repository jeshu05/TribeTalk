package org.tribetalk.fln.pipeline

import org.json.JSONArray
import org.json.JSONObject
import org.tribetalk.fln.model.FlnGrade
import org.tribetalk.fln.model.WorksheetDifficulty

/**
 * Standardized NIPUN Pedagogical Activity Types.
 */
enum class ActivityActionType(val displayName: String, val santaliName: String, val hindiName: String) {
    COUNT_AND_SELECT("Count & Select", "ᱞᱮᱠᱷᱟ ᱟᱨ ᱵᱟᱪᱷᱟᱣ", "गिनें और चुनें"),
    COUNT_AND_MATCH("Count & Match", "ᱞᱮᱠᱷᱟ ᱟᱨ ᱡᱚᱲ", "गिनें और मिलाएँ"),
    ADDITION_CONCRETE("Concrete Addition", "ᱡᱚᱲ (ᱞᱮᱠᱷᱟ)", "वस्तुओं का जोड़"),
    SUBTRACTION_CONCRETE("Concrete Subtraction", "ᱵᱷᱮᱜᱟᱨ (ᱞᱮᱠᱷᱟ)", "वस्तुओं का घटाव"),
    SEQUENCE_TRAIN("Number Sequence", "ᱞᱮᱠᱷᱟ ᱛᱷᱟᱨ", "संख्या क्रम"),
    OBJECT_CLASSIFY("Object Classification", "ᱦᱟᱹᱴᱤᱧ", "वर्गीकरण"),
    COMPARE_QUANTITY("Compare Quantity", "ᱠᱚᱢ/ᱰᱷᱮᱨ ᱛᱩᱞᱟᱹ", "कम या अधिक तुलना"),
    AKSHAR_PHONICS_TRACE("Akshar Tracing & Phonics", "ᱪᱤᱠᱤ ᱟᱲᱟᱝ ᱟᱨ ᱚᱞ", "अक्षर ध्वनि एवं लेखन"),
    PICTURE_WORD_MATCH("Picture-Word Match", "ᱪᱤᱛᱟᱹᱨ ᱟᱹᱲᱟᱹ ᱡᱚᱲ", "चित्र-शब्द मिलान"),
    FILL_MISSING_AKSHAR("Missing Akshar", "ᱟᱫ ᱪᱤᱠᱤ ᱯᱮᱨᱮᱡ", "छूटा अक्षर भरें"),
    CIRCLE_THE_ANSWER("Circle the Answer", "ᱜᱩᱞᱟᱹᱴ ᱜᱷᱮᱨᱟᱣ", "सही उत्तर पर गोला लगाएँ"),
    PATTERN_RECOGNITION("Pattern Recognition", "ᱨᱩᱯ ᱛᱷᱟᱨ", "पैटर्न पहचान"),
    MULTIPLICATION_GROUPS("Multiplication Groups", "ᱜᱩᱬᱟᱹ (ᱦᱟᱹᱴᱤᱧ)", "गुणा (समान समूह)"),
    MONEY_CALCULATION("Money Counting", "ᱴᱟᱠᱟ ᱟᱨ ᱯᱩᱭᱥᱟᱹ", "रुपया और पैसा"),
    SHAPE_RECOGNITION("Shape Recognition", "ᱨᱩᱯ ᱪᱤᱱᱦᱟᱹᱣ", "आकार पहचान")
}

/**
 * Progressive Linguistic Depth Tiers for continuous NIPUN learning outcomes.
 */
enum class LinguisticTier(val displayName: String, val levelIndex: Int) {
    ISOLATED_AKSHAR("Tier 1: Isolated Akshar & Sound", 1),
    CORE_VOCABULARY("Tier 2: Core Vocabulary Word", 2),
    DESCRIPTIVE_PHRASE("Tier 3: Descriptive Phrase / Pair", 3),
    FLUENCY_SENTENCE("Tier 4: Fluency Sentence (NIPUN Grade 1)", 4),
    CONNECTED_STORY("Tier 5: Connected Micro-Story / Riddle", 5)
}

/**
 * Strict, validated Activity Intermediate Representation (IR).
 * Decouples pedagogical planning from graphics rendering and layout coordinates.
 */
data class ActivityIR(
    val id: String,
    val nipunCompetencyCode: String,
    val actionType: ActivityActionType,
    val grade: FlnGrade = FlnGrade.GRADE_1,
    val difficulty: WorksheetDifficulty = WorksheetDifficulty.EASY,
    val linguisticTier: LinguisticTier = LinguisticTier.CORE_VOCABULARY,
    
    // Semantic Content Keys (mapped to SVG Corpus)
    val primaryObjectKey: String,
    val secondaryObjectKey: String? = null,
    val quantity: Int = 1,
    val secondaryQuantity: Int = 0,
    
    // Distractors & Solution
    val correctValue: String,
    val distractorOptions: List<String> = emptyList(),
    
    // Linguistic & Phonetic Data
    val targetAkshar: String = "",
    val santaliWord: String = "",
    val hindiWord: String = "",
    val englishWord: String = "",
    
    // Progression: Phrases and Sentences
    val bilingualPhraseSantali: String = "",
    val bilingualPhraseHindi: String = "",
    val phraseEnglishGloss: String = "",
    val sentenceSantali: String = "",
    val sentenceHindi: String = "",
    
    // Teacher Directives & Phonics
    val instructionSantali: String = "",
    val instructionHindi: String = "",
    val teacherSolutionNote: String = "",
    val phonicsGuide: String = ""
) {
    /**
     * Serializes this ActivityIR into a clean JSON structure.
     */
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("nipunCompetencyCode", nipunCompetencyCode)
        json.put("actionType", actionType.name)
        json.put("grade", grade.name)
        json.put("difficulty", difficulty.name)
        json.put("linguisticTier", linguisticTier.name)
        
        json.put("primaryObjectKey", primaryObjectKey)
        if (secondaryObjectKey != null) {
            json.put("secondaryObjectKey", secondaryObjectKey)
        }
        json.put("quantity", quantity)
        json.put("secondaryQuantity", secondaryQuantity)
        
        json.put("correctValue", correctValue)
        val optsArray = JSONArray()
        distractorOptions.forEach { optsArray.put(it) }
        json.put("distractorOptions", optsArray)
        
        json.put("targetAkshar", targetAkshar)
        json.put("santaliWord", santaliWord)
        json.put("hindiWord", hindiWord)
        json.put("englishWord", englishWord)
        
        json.put("bilingualPhraseSantali", bilingualPhraseSantali)
        json.put("bilingualPhraseHindi", bilingualPhraseHindi)
        json.put("phraseEnglishGloss", phraseEnglishGloss)
        json.put("sentenceSantali", sentenceSantali)
        json.put("sentenceHindi", sentenceHindi)
        
        json.put("instructionSantali", instructionSantali)
        json.put("instructionHindi", instructionHindi)
        json.put("teacherSolutionNote", teacherSolutionNote)
        json.put("phonicsGuide", phonicsGuide)
        return json
    }

    companion object {
        /**
         * Safely parses JSON into a validated ActivityIR.
         */
        fun fromJson(json: JSONObject): ActivityIR {
            val opts = mutableListOf<String>()
            val arr = json.optJSONArray("distractorOptions")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    opts.add(arr.getString(i))
                }
            }

            return ActivityIR(
                id = json.optString("id", "act_${System.currentTimeMillis()}"),
                nipunCompetencyCode = json.optString("nipunCompetencyCode", "N-BAL.1"),
                actionType = try {
                    ActivityActionType.valueOf(json.optString("actionType", "COUNT_AND_SELECT"))
                } catch (e: Exception) {
                    ActivityActionType.COUNT_AND_SELECT
                },
                grade = try {
                    FlnGrade.valueOf(json.optString("grade", "GRADE_1"))
                } catch (e: Exception) {
                    FlnGrade.GRADE_1
                },
                difficulty = try {
                    WorksheetDifficulty.valueOf(json.optString("difficulty", "EASY"))
                } catch (e: Exception) {
                    WorksheetDifficulty.EASY
                },
                linguisticTier = try {
                    LinguisticTier.valueOf(json.optString("linguisticTier", "CORE_VOCABULARY"))
                } catch (e: Exception) {
                    LinguisticTier.CORE_VOCABULARY
                },
                primaryObjectKey = json.optString("primaryObjectKey", "mango"),
                secondaryObjectKey = if (json.has("secondaryObjectKey")) json.getString("secondaryObjectKey") else null,
                quantity = json.optInt("quantity", 1),
                secondaryQuantity = json.optInt("secondaryQuantity", 0),
                correctValue = json.optString("correctValue", "1"),
                distractorOptions = opts,
                targetAkshar = json.optString("targetAkshar", ""),
                santaliWord = json.optString("santaliWord", ""),
                hindiWord = json.optString("hindiWord", ""),
                englishWord = json.optString("englishWord", ""),
                bilingualPhraseSantali = json.optString("bilingualPhraseSantali", ""),
                bilingualPhraseHindi = json.optString("bilingualPhraseHindi", ""),
                phraseEnglishGloss = json.optString("phraseEnglishGloss", ""),
                sentenceSantali = json.optString("sentenceSantali", ""),
                sentenceHindi = json.optString("sentenceHindi", ""),
                instructionSantali = json.optString("instructionSantali", ""),
                instructionHindi = json.optString("instructionHindi", ""),
                teacherSolutionNote = json.optString("teacherSolutionNote", ""),
                phonicsGuide = json.optString("phonicsGuide", "")
            )
        }
    }
}

/**
 * Tracks the child's learning trajectory and scaffolds progression from Words -> Phrases -> Sentences.
 */
data class LearnerState(
    val learnerId: String = "default_child",
    val grade: FlnGrade = FlnGrade.GRADE_1,
    val currentTier: LinguisticTier = LinguisticTier.CORE_VOCABULARY,
    val consecutiveSuccessStreak: Int = 0,
    val totalActivitiesCompleted: Int = 0,
    val masteredObjectKeys: Set<String> = emptySet(),
    val masteredAkshars: Set<String> = emptySet()
)
