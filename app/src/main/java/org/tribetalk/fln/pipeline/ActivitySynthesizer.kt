package org.tribetalk.fln.pipeline

import org.json.JSONObject
import org.tribetalk.fln.model.FlnGrade
import org.tribetalk.fln.model.WorksheetDifficulty
import java.util.regex.Pattern

/**
 * Structured Curriculum Activity Synthesizer.
 * Synthesizes curriculum-aligned Activity IR instances from teacher topics or JSON plans,
 * enforcing NIPUN Bharat pedagogical constraints, localized Ol Chiki/Hindi vocabulary,
 * and deterministic validation.
 */
object ActivitySynthesizer {

    private const val TAG = "ActivitySynthesizer"

    /**
     * Parses raw Activity JSON input, validates it through ActivityValidator, and auto-repairs any defects.
     */
    fun parsePlanFromJson(rawText: String, fallbackGrade: FlnGrade = FlnGrade.GRADE_1): ValidationResult {
        return try {
            val jsonPattern = Pattern.compile("\\{[\\s\\S]*\\}")
            val matcher = jsonPattern.matcher(rawText)
            if (matcher.find()) {
                val jsonStr = matcher.group()
                val json = JSONObject(jsonStr)

                val id = json.optString("id", "act_${System.currentTimeMillis()}")
                val nipunCode = json.optString("nipunCompetencyCode", "N-G1.1")
                val actionTypeStr = json.optString("actionType", ActivityActionType.COUNT_AND_SELECT.name)
                val actionType = try {
                    ActivityActionType.valueOf(actionTypeStr)
                } catch (e: Exception) {
                    ActivityActionType.COUNT_AND_SELECT
                }

                val gradeStr = json.optString("grade", fallbackGrade.name)
                val grade = try {
                    FlnGrade.valueOf(gradeStr)
                } catch (e: Exception) {
                    fallbackGrade
                }

                val diffStr = json.optString("difficulty", WorksheetDifficulty.EASY.name)
                val diff = try {
                    WorksheetDifficulty.valueOf(diffStr)
                } catch (e: Exception) {
                    WorksheetDifficulty.EASY
                }

                val tierStr = json.optString("linguisticTier", LinguisticTier.CORE_VOCABULARY.name)
                val tier = try {
                    LinguisticTier.valueOf(tierStr)
                } catch (e: Exception) {
                    LinguisticTier.CORE_VOCABULARY
                }

                var primaryKey = json.optString("primaryObjectKey", "mango").lowercase().trim()
                if (SvgCorpusRegistry.get(primaryKey) == null) {
                    primaryKey = SvgCorpusRegistry.findMatchingKey(primaryKey) ?: "mango"
                }

                val secondaryKey = json.optString("secondaryObjectKey", "").ifBlank { null }?.lowercase()?.trim()
                val quantity = json.optInt("quantity", 3)
                val secondaryQuantity = json.optInt("secondaryQuantity", 0)
                val correctValue = json.optString("correctValue", quantity.toString())

                val options = mutableListOf<String>()
                val optsArr = json.optJSONArray("distractorOptions")
                if (optsArr != null) {
                    for (i in 0 until optsArr.length()) {
                        options.add(optsArr.getString(i))
                    }
                }

                val santaliWord = json.optString("santaliWord", "ᱩᱞ")
                val hindiWord = json.optString("hindiWord", "आम")
                val englishWord = json.optString("englishWord", "Mango")
                val phraseSantali = json.optString("bilingualPhraseSantali", "ᱦᱮᱲᱮᱢ ᱩᱞ")
                val phraseHindi = json.optString("bilingualPhraseHindi", "मीठा आम")
                val phraseEnglish = json.optString("phraseEnglishGloss", "Sweet Mango")
                val instrSantali = json.optString("instructionSantali", "ᱥᱟᱹᱦᱤ ᱛᱮᱞᱟ ᱵᱟᱪᱷᱟᱣ ᱢᱮ:")
                val instrHindi = json.optString("instructionHindi", "सही उत्तर चुनें:")
                val note = json.optString("teacherSolutionNote", "Answer: $correctValue")
                val phonics = json.optString("phonicsGuide", "")

                val candidateIR = ActivityIR(
                    id = id,
                    nipunCompetencyCode = nipunCode,
                    actionType = actionType,
                    grade = grade,
                    difficulty = diff,
                    linguisticTier = tier,
                    primaryObjectKey = primaryKey,
                    secondaryObjectKey = secondaryKey,
                    quantity = quantity,
                    secondaryQuantity = secondaryQuantity,
                    correctValue = correctValue,
                    distractorOptions = options,
                    santaliWord = santaliWord,
                    hindiWord = hindiWord,
                    englishWord = englishWord,
                    bilingualPhraseSantali = phraseSantali,
                    bilingualPhraseHindi = phraseHindi,
                    phraseEnglishGloss = phraseEnglish,
                    instructionSantali = instrSantali,
                    instructionHindi = instrHindi,
                    teacherSolutionNote = note,
                    phonicsGuide = phonics
                )

                ActivityValidator.validateAndRepair(candidateIR)
            } else {
                ValidationResult(
                    isValid = false,
                    repairedIR = synthesizeFromTopic(rawText, fallbackGrade),
                    issuesFound = listOf("No JSON structure detected in input. Used deterministic fallback.")
                )
            }
        } catch (e: Exception) {
            ValidationResult(
                isValid = false,
                repairedIR = synthesizeFromTopic(rawText, fallbackGrade),
                issuesFound = listOf("JSON parsing failed: ${e.message}. Used deterministic fallback.")
            )
        }
    }

    /**
     * Zero-RAM, high-speed deterministic synthesizer (< 5ms).
     * Maps teacher topic keywords to curriculum-aligned ActivityIRs instantly.
     */
    fun synthesizeFromTopic(
        topic: String,
        grade: FlnGrade = FlnGrade.GRADE_1
    ): ActivityIR {
        val clean = topic.lowercase().trim()

        // 1. Resolve Semantic Object Key
        val objectKey = when {
            clean.contains("आम") || clean.contains("mango") || clean.contains("ᱩᱞ") -> "mango"
            clean.contains("केल") || clean.contains("banana") || clean.contains("ᱠᱟᱭᱨᱟ") -> "banana"
            clean.contains("सेब") || clean.contains("apple") || clean.contains("ᱟᱯᱮᱞ") -> "apple"
            clean.contains("पत्त") || clean.contains("leaf") || clean.contains("ᱥᱟᱠᱟᱢ") || clean.contains("साल") -> "sal_leaf"
            clean.contains("महुआ") || clean.contains("फूल") || clean.contains("flower") || clean.contains("ᱵᱟᱦᱟ") || clean.contains("ᱢᱟᱹᱛᱠᱚᱢ") -> "mahua_flower"
            clean.contains("घड़") || clean.contains("मटका") || clean.contains("pot") || clean.contains("ᱴᱩᱠᱩᱡ") -> "clay_pot"
            clean.contains("ढोल") || clean.contains("मांदर") || clean.contains("drum") || clean.contains("ᱛᱩᱢᱫᱟᱜ") -> "tumdak_drum"
            clean.contains("मछल") || clean.contains("fish") || clean.contains("ᱦᱟᱹᱠᱩ") -> "fish"
            clean.contains("मोर") || clean.contains("peacock") || clean.contains("ᱢᱟᱨᱟᱜ") -> "peacock"
            clean.contains("गाय") || clean.contains("गौ") || clean.contains("cow") || clean.contains("ᱜᱟᱹᱭ") -> "cow"
            clean.contains("हाथी") || clean.contains("elephant") || clean.contains("ᱦᱟᱹᱛᱤ") -> "elephant"
            clean.contains("कुत्त") || clean.contains("dog") || clean.contains("ᱥᱮᱛᱟ") -> "dog"
            clean.contains("तार") || clean.contains("star") || clean.contains("ᱤᱯᱤᱞ") -> "star"
            clean.contains("पत्थर") || clean.contains("कंकड़") || clean.contains("pebble") || clean.contains("ᱫᱷᱤᱨᱤ") -> "pebble"
            clean.contains("गोल") || clean.contains("circle") || clean.contains("ᱪᱟᱸᱫᱚ") -> "circle"
            clean.contains("चौकोर") || clean.contains("square") -> "square"
            clean.contains("त्रिभुज") || clean.contains("triangle") -> "triangle"
            else -> SvgCorpusRegistry.findMatchingKey(clean) ?: "mango"
        }

        // 2. Resolve Action Type & Competency Code
        val isAddition = clean.contains("जोड़") || clean.contains("add") || clean.contains("प्लस") || clean.contains("+")
        val isSequence = clean.contains("रेल") || clean.contains("क्रम") || clean.contains("sequence") || clean.contains("train")
        val isTracing = clean.contains("अक्षर") || clean.contains("वर्ण") || clean.contains("लिख") || clean.contains("trace") || clean.contains("ध्वनि")
        val isClassification = clean.contains("छाँट") || clean.contains("वर्गीकरण") || clean.contains("classify") || clean.contains("category")

        val actionType = when {
            isAddition -> ActivityActionType.ADDITION_CONCRETE
            isSequence -> ActivityActionType.SEQUENCE_TRAIN
            isTracing -> ActivityActionType.AKSHAR_PHONICS_TRACE
            isClassification -> ActivityActionType.OBJECT_CLASSIFY
            clean.contains("गिन") || clean.contains("count") || clean.contains("संख्या") -> ActivityActionType.COUNT_AND_SELECT
            else -> ActivityActionType.PICTURE_WORD_MATCH
        }

        val nipunCode = when (actionType) {
            ActivityActionType.COUNT_AND_SELECT -> if (grade == FlnGrade.BALVATIKA) "N-BAL.1" else "N-G1.1"
            ActivityActionType.ADDITION_CONCRETE -> "N-G1.2"
            ActivityActionType.SEQUENCE_TRAIN -> "N-G1.3"
            ActivityActionType.AKSHAR_PHONICS_TRACE -> "L-BAL.1"
            ActivityActionType.PICTURE_WORD_MATCH -> "L-G1.1"
            ActivityActionType.OBJECT_CLASSIFY -> "N-BAL.2"
            else -> "L-G1.1"
        }

        // 3. Resolve Content from Catalog
        val meta = SvgCorpusRegistry.get(objectKey)
        val wordSantali = meta?.santaliName ?: "ᱩᱞ"
        val wordHindi = meta?.hindiName ?: "आम"
        val wordEnglish = meta?.englishName ?: "Mango"

        val progression = LearnerProgressionEngine.getContent(objectKey)
        val phraseSantali = progression?.phraseSantali ?: "$wordSantali ᱡᱚ"
        val phraseHindi = progression?.phraseHindi ?: "मीठा $wordHindi"
        val phraseEnglish = progression?.phraseEnglish ?: "Sweet $wordEnglish"

        val qty = when (grade) {
            FlnGrade.BALVATIKA -> 5
            FlnGrade.GRADE_1 -> 7
            FlnGrade.GRADE_2 -> 12
            FlnGrade.GRADE_3 -> 25
        }
        val secQty = if (actionType == ActivityActionType.ADDITION_CONCRETE) 3 else 0
        val correctVal = if (actionType == ActivityActionType.ADDITION_CONCRETE) {
            (qty + secQty).toString()
        } else if (actionType == ActivityActionType.COUNT_AND_SELECT) {
            qty.toString()
        } else {
            wordSantali
        }

        val distractors = if (actionType == ActivityActionType.ADDITION_CONCRETE || actionType == ActivityActionType.COUNT_AND_SELECT) {
            val num = correctVal.toIntOrNull() ?: qty
            listOf(num.toString(), (num + 1).toString(), (num - 1).coerceAtLeast(1).toString(), (num + 2).toString()).distinct().shuffled()
        } else {
            listOf(wordSantali, "ᱫᱟᱨᱮ", "ᱜᱟᱰᱟ", "ᱦᱟᱹᱠᱩ").distinct().shuffled()
        }

        val rawIR = ActivityIR(
            id = "synth_${objectKey}_${System.currentTimeMillis()}",
            nipunCompetencyCode = nipunCode,
            actionType = actionType,
            grade = grade,
            difficulty = WorksheetDifficulty.EASY,
            linguisticTier = if (isTracing) LinguisticTier.ISOLATED_AKSHAR else LinguisticTier.CORE_VOCABULARY,
            primaryObjectKey = objectKey,
            secondaryObjectKey = null,
            quantity = qty,
            secondaryQuantity = secQty,
            correctValue = correctVal,
            distractorOptions = distractors,
            santaliWord = wordSantali,
            hindiWord = wordHindi,
            englishWord = wordEnglish,
            bilingualPhraseSantali = phraseSantali,
            bilingualPhraseHindi = phraseHindi,
            phraseEnglishGloss = phraseEnglish,
            instructionSantali = if (actionType == ActivityActionType.ADDITION_CONCRETE) "$wordSantali ᱡᱚᱲᱟᱣ ᱢᱮ:" else "$wordSantali ᱞᱮᱠᱷᱟᱭ ᱢᱮ ᱟᱨ ᱵᱟᱪᱷᱟᱣ ᱢᱮ:",
            instructionHindi = if (actionType == ActivityActionType.ADDITION_CONCRETE) "$wordHindi जोड़ें और कुल संख्या लिखें:" else "$wordHindi गिनें और सही संख्या चुनें:",
            teacherSolutionNote = "Correct: $correctVal ($wordSantali / $wordHindi)",
            phonicsGuide = "$wordEnglish ($wordSantali)"
        )

        return ActivityValidator.validateAndRepair(rawIR).repairedIR
    }
}
