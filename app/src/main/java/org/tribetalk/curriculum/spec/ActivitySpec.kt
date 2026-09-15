package org.tribetalk.curriculum.spec

import org.json.JSONArray
import org.json.JSONObject
import org.tribetalk.curriculum.ontology.LearningObjective
import org.tribetalk.fln.model.FlnCard
import org.tribetalk.fln.model.FlnCategory
import org.tribetalk.fln.model.FlnDomain
import org.tribetalk.fln.model.WorksheetDifficulty
import org.tribetalk.fln.model.WorksheetItem
import org.tribetalk.fln.pipeline.ActivityActionType
import org.tribetalk.fln.pipeline.ActivityIR
import org.tribetalk.fln.pipeline.SvgCorpusRegistry

enum class ActivityType(val displayName: String, val actionType: ActivityActionType) {
    COUNT("Count Objects", ActivityActionType.COUNT_AND_SELECT),
    COUNT_AND_MATCH("Count & Match", ActivityActionType.COUNT_AND_MATCH),
    CIRCLE_CORRECT("Circle Correct Answer", ActivityActionType.CIRCLE_THE_ANSWER),
    COMPARE_QUANTITIES("Compare Quantities", ActivityActionType.COMPARE_QUANTITY),
    MISSING_NUMBER("Missing Number", ActivityActionType.SEQUENCE_TRAIN),
    SEQUENCE("Sequence", ActivityActionType.SEQUENCE_TRAIN),
    SIMPLE_ADDITION("Concrete Addition", ActivityActionType.ADDITION_CONCRETE),
    CLASSIFY("Object Classification", ActivityActionType.OBJECT_CLASSIFY),
    IDENTIFY_LETTER("Identify Letter", ActivityActionType.AKSHAR_PHONICS_TRACE),
    LETTER_TO_OBJECT("Letter to Object", ActivityActionType.PICTURE_WORD_MATCH),
    MATCH("Match", ActivityActionType.PICTURE_WORD_MATCH),
    SOUND_IDENTIFICATION("Sound Identification", ActivityActionType.AKSHAR_PHONICS_TRACE),
    VOCABULARY_RECOGNITION("Vocabulary Recognition", ActivityActionType.PICTURE_WORD_MATCH)
}

enum class VisualTheme {
    GARDEN, FARM, FOREST, CLASSROOM, VILLAGE, NATURE
}

enum class VisualLayout {
    HERO, SPLIT, GRID, COMPARISON, SEQUENCE, MATCHING, QUESTION, REVEAL
}

data class ActivityItemSpec(
    val id: String,
    val visualAsset: String,
    val secondaryVisualAsset: String? = null,
    val quantity: Int = 1,
    val secondaryQuantity: Int = 0,
    val options: List<String> = emptyList(),
    val correctAnswer: String = "1",
    val explanationHindi: String = "",
    val explanationSantali: String = ""
) {
    fun toJson(): JSONObject {
        val j = JSONObject()
        j.put("id", id)
        j.put("visualAsset", visualAsset)
        if (secondaryVisualAsset != null) j.put("secondaryVisualAsset", secondaryVisualAsset)
        j.put("quantity", quantity)
        j.put("secondaryQuantity", secondaryQuantity)
        val opts = JSONArray()
        options.forEach { opts.put(it) }
        j.put("options", opts)
        j.put("correctAnswer", correctAnswer)
        j.put("explanationHindi", explanationHindi)
        j.put("explanationSantali", explanationSantali)
        return j
    }

    companion object {
        fun fromJson(j: JSONObject): ActivityItemSpec {
            val opts = mutableListOf<String>()
            val arr = j.optJSONArray("options")
            if (arr != null) {
                for (i in 0 until arr.length()) opts.add(arr.getString(i))
            }
            return ActivityItemSpec(
                id = j.optString("id", "item_1"),
                visualAsset = j.optString("visualAsset", "mango"),
                secondaryVisualAsset = if (j.has("secondaryVisualAsset")) j.getString("secondaryVisualAsset") else null,
                quantity = j.optInt("quantity", 1),
                secondaryQuantity = j.optInt("secondaryQuantity", 0),
                options = opts,
                correctAnswer = j.optString("correctAnswer", "1"),
                explanationHindi = j.optString("explanationHindi", ""),
                explanationSantali = j.optString("explanationSantali", "")
            )
        }
    }
}

data class VisualSpec(
    val theme: VisualTheme = VisualTheme.GARDEN,
    val layout: VisualLayout = VisualLayout.GRID,
    val primaryAssetKey: String = "mango",
    val secondaryAssetKey: String? = null,
    val supportsMonochrome: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("theme", theme.name)
        put("layout", layout.name)
        put("primaryAssetKey", primaryAssetKey)
        if (secondaryAssetKey != null) put("secondaryAssetKey", secondaryAssetKey)
        put("supportsMonochrome", supportsMonochrome)
    }

    companion object {
        fun fromJson(j: JSONObject): VisualSpec = VisualSpec(
            theme = try { VisualTheme.valueOf(j.optString("theme", "GARDEN")) } catch (e: Exception) { VisualTheme.GARDEN },
            layout = try { VisualLayout.valueOf(j.optString("layout", "GRID")) } catch (e: Exception) { VisualLayout.GRID },
            primaryAssetKey = j.optString("primaryAssetKey", "mango"),
            secondaryAssetKey = if (j.has("secondaryAssetKey")) j.getString("secondaryAssetKey") else null,
            supportsMonochrome = j.optBoolean("supportsMonochrome", true)
        )
    }
}

data class LanguageSpec(
    val primaryWord: String = "",
    val targetWord: String = "",
    val englishWord: String = "",
    val instructionHindi: String = "",
    val instructionSantali: String = "",
    val phoneticGuide: String = "",
    val phraseHindi: String = "",
    val phraseSantali: String = "",
    val phraseEnglish: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("primaryWord", primaryWord)
        put("targetWord", targetWord)
        put("englishWord", englishWord)
        put("instructionHindi", instructionHindi)
        put("instructionSantali", instructionSantali)
        put("phoneticGuide", phoneticGuide)
        put("phraseHindi", phraseHindi)
        put("phraseSantali", phraseSantali)
        put("phraseEnglish", phraseEnglish)
    }

    companion object {
        fun fromJson(j: JSONObject): LanguageSpec = LanguageSpec(
            primaryWord = j.optString("primaryWord", ""),
            targetWord = j.optString("targetWord", ""),
            englishWord = j.optString("englishWord", ""),
            instructionHindi = j.optString("instructionHindi", ""),
            instructionSantali = j.optString("instructionSantali", ""),
            phoneticGuide = j.optString("phoneticGuide", ""),
            phraseHindi = j.optString("phraseHindi", ""),
            phraseSantali = j.optString("phraseSantali", ""),
            phraseEnglish = j.optString("phraseEnglish", "")
        )
    }
}

data class AnswerSpec(
    val correctValue: String,
    val numericValue: Int? = null,
    val distractors: List<String> = emptyList(),
    val teacherNote: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("correctValue", correctValue)
        if (numericValue != null) put("numericValue", numericValue)
        val arr = JSONArray()
        distractors.forEach { arr.put(it) }
        put("distractors", arr)
        put("teacherNote", teacherNote)
    }

    companion object {
        fun fromJson(j: JSONObject): AnswerSpec {
            val dist = mutableListOf<String>()
            val arr = j.optJSONArray("distractors")
            if (arr != null) {
                for (i in 0 until arr.length()) dist.add(arr.getString(i))
            }
            return AnswerSpec(
                correctValue = j.optString("correctValue", "1"),
                numericValue = if (j.has("numericValue")) j.getInt("numericValue") else null,
                distractors = dist,
                teacherNote = j.optString("teacherNote", "")
            )
        }
    }
}

data class ActivityMetadata(
    val worksheetId: String,
    val activityId: String,
    val generationSeed: Long,
    val modelVersion: String = "Qwen-0.5B-v2.5",
    val curriculumVersion: String = "NIPUN-2026.1",
    val rendererVersion: String = "TT-SVG-v1",
    val assetVersion: String = "1.0",
    val languageVersion: String = "sat-hi-v1",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("worksheetId", worksheetId)
        put("activityId", activityId)
        put("generationSeed", generationSeed)
        put("modelVersion", modelVersion)
        put("curriculumVersion", curriculumVersion)
        put("rendererVersion", rendererVersion)
        put("assetVersion", assetVersion)
        put("languageVersion", languageVersion)
        put("timestamp", timestamp)
    }

    companion object {
        fun fromJson(j: JSONObject): ActivityMetadata = ActivityMetadata(
            worksheetId = j.optString("worksheetId", ""),
            activityId = j.optString("activityId", ""),
            generationSeed = j.optLong("generationSeed", 0L),
            modelVersion = j.optString("modelVersion", "Qwen-0.5B-v2.5"),
            curriculumVersion = j.optString("curriculumVersion", "NIPUN-2026.1"),
            rendererVersion = j.optString("rendererVersion", "TT-SVG-v1"),
            assetVersion = j.optString("assetVersion", "1.0"),
            languageVersion = j.optString("languageVersion", "sat-hi-v1"),
            timestamp = j.optLong("timestamp", System.currentTimeMillis())
        )
    }
}

/**
 * Universal Activity Specification contract.
 * Pure, renderer-independent, serializable, and independently testable.
 */
data class ActivitySpec(
    val id: String,
    val objectiveId: String,
    val activityType: ActivityType,
    val difficulty: WorksheetDifficulty = WorksheetDifficulty.EASY,
    val items: List<ActivityItemSpec>,
    val visualSpec: VisualSpec,
    val languageSpec: LanguageSpec,
    val answerSpec: AnswerSpec,
    val metadata: ActivityMetadata
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("objectiveId", objectiveId)
        put("activityType", activityType.name)
        put("difficulty", difficulty.name)
        val itemsArr = JSONArray()
        items.forEach { itemsArr.put(it.toJson()) }
        put("items", itemsArr)
        put("visualSpec", visualSpec.toJson())
        put("languageSpec", languageSpec.toJson())
        put("answerSpec", answerSpec.toJson())
        put("metadata", metadata.toJson())
    }

    companion object {
        fun fromJson(j: JSONObject): ActivitySpec {
            val itemsList = mutableListOf<ActivityItemSpec>()
            val arr = j.optJSONArray("items")
            if (arr != null) {
                for (i in 0 until arr.length()) itemsList.add(ActivityItemSpec.fromJson(arr.getJSONObject(i)))
            }
            return ActivitySpec(
                id = j.optString("id", "act_${System.currentTimeMillis()}"),
                objectiveId = j.optString("objectiveId", "NIPUN_G1_NUM_COUNT_1_10"),
                activityType = try { ActivityType.valueOf(j.optString("activityType", "COUNT")) } catch (e: Exception) { ActivityType.COUNT },
                difficulty = try { WorksheetDifficulty.valueOf(j.optString("difficulty", "EASY")) } catch (e: Exception) { WorksheetDifficulty.EASY },
                items = itemsList,
                visualSpec = if (j.has("visualSpec")) VisualSpec.fromJson(j.getJSONObject("visualSpec")) else VisualSpec(),
                languageSpec = if (j.has("languageSpec")) LanguageSpec.fromJson(j.getJSONObject("languageSpec")) else LanguageSpec(),
                answerSpec = if (j.has("answerSpec")) AnswerSpec.fromJson(j.getJSONObject("answerSpec")) else AnswerSpec("1"),
                metadata = if (j.has("metadata")) ActivityMetadata.fromJson(j.getJSONObject("metadata")) else ActivityMetadata("ws_1", "act_1", 42L)
            )
        }

        /**
         * Synthesizes an ActivitySpec from Qwen structured JSON output.
         */
        fun fromPlannerOutput(
            plannerJson: JSONObject,
            objective: LearningObjective,
            seed: Long
        ): ActivitySpec {
            val actTypeStr = plannerJson.optString("activityType", "COUNT").uppercase()
            val actType = ActivityType.values().firstOrNull {
                it.name == actTypeStr || it.displayName.uppercase() == actTypeStr
            } ?: objective.allowedActivityTypes.firstOrNull() ?: ActivityType.COUNT

            val qty = when {
                plannerJson.has("quantity") -> plannerJson.optInt("quantity", 7)
                plannerJson.has("targetQuantity") -> plannerJson.optInt("targetQuantity", 7)
                plannerJson.has("objectCount") -> plannerJson.optInt("objectCount", 7)
                else -> 7
            }.coerceIn(objective.numberRange.first, objective.numberRange.last)

            var assetKey = when {
                plannerJson.has("visualAsset") -> plannerJson.optString("visualAsset", "mango")
                plannerJson.has("primaryObject") -> plannerJson.optString("primaryObject", "mango")
                plannerJson.has("assetType") -> plannerJson.optString("assetType", "mango")
                else -> "mango"
            }.lowercase().trim()

            if (SvgCorpusRegistry.get(assetKey) == null) {
                assetKey = SvgCorpusRegistry.findMatchingKey(assetKey) ?: "mango"
            }

            val ansStr = when {
                plannerJson.has("correctAnswer") -> plannerJson.optString("correctAnswer", qty.toString())
                plannerJson.has("targetNumber") -> plannerJson.optString("targetNumber", qty.toString())
                else -> qty.toString()
            }

            val opts = mutableListOf<String>()
            val optArr = plannerJson.optJSONArray("distractorOptions") 
                ?: plannerJson.optJSONArray("distractors")
                ?: plannerJson.optJSONArray("options")
            if (optArr != null) {
                for (i in 0 until optArr.length()) opts.add(optArr.getString(i))
            }
            if (!opts.contains(ansStr)) opts.add(ansStr)

            val meta = SvgCorpusRegistry.get(assetKey)
            val hiName = meta?.hindiName ?: "आम"
            val satName = meta?.santaliName ?: "ᱩᱞ"
            val engName = meta?.englishName ?: "Mango"

            val item = ActivityItemSpec(
                id = "item_${seed}_1",
                visualAsset = assetKey,
                quantity = qty,
                options = opts.distinct().shuffled(),
                correctAnswer = ansStr,
                explanationHindi = "$qty $hiName",
                explanationSantali = "$qty $satName"
            )

            val itemsList = mutableListOf<ActivityItemSpec>()
            val itemsArr = plannerJson.optJSONArray("items") ?: plannerJson.optJSONArray("questions")
            if (itemsArr != null && itemsArr.length() > 0) {
                for (i in 0 until itemsArr.length()) {
                    val itmJson = itemsArr.optJSONObject(i) ?: continue
                    val itmQty = itmJson.optInt("quantity", qty).coerceIn(objective.numberRange.first, objective.numberRange.last)
                    var itmAsset = itmJson.optString("visualAsset", assetKey).lowercase().trim()
                    if (SvgCorpusRegistry.get(itmAsset) == null) {
                        itmAsset = SvgCorpusRegistry.findMatchingKey(itmAsset) ?: assetKey
                    }
                    val itmAns = itmJson.optString("correctAnswer", itmQty.toString())
                    val itmOpts = mutableListOf<String>()
                    val itmOptArr = itmJson.optJSONArray("distractorOptions") ?: itmJson.optJSONArray("distractors") ?: itmJson.optJSONArray("options")
                    if (itmOptArr != null) {
                        for (o in 0 until itmOptArr.length()) itmOpts.add(itmOptArr.getString(o))
                    }
                    if (!itmOpts.contains(itmAns)) itmOpts.add(itmAns)
                    val itmMeta = SvgCorpusRegistry.get(itmAsset)
                    val itmHi = itmMeta?.hindiName ?: hiName
                    val itmSat = itmMeta?.santaliName ?: satName
                    itemsList.add(
                        ActivityItemSpec(
                            id = "item_${seed}_${i + 1}",
                            visualAsset = itmAsset,
                            quantity = itmQty,
                            options = itmOpts.distinct().shuffled(),
                            correctAnswer = itmAns,
                            explanationHindi = "$itmQty $itmHi",
                            explanationSantali = "$itmQty $itmSat"
                        )
                    )
                }
            }
            if (itemsList.isEmpty()) {
                itemsList.add(item)
            }

            val themeStr = plannerJson.optString("visualTheme", "GARDEN").uppercase()
            val theme = try { VisualTheme.valueOf(themeStr) } catch (e: Exception) { VisualTheme.GARDEN }

            val layoutStr = plannerJson.optString("visualLayout", "GRID").uppercase()
            val layout = try { VisualLayout.valueOf(layoutStr) } catch (e: Exception) { VisualLayout.GRID }

            return ActivitySpec(
                id = "spec_${objective.id}_${seed}",
                objectiveId = objective.id,
                activityType = actType,
                difficulty = WorksheetDifficulty.EASY,
                items = itemsList,
                visualSpec = VisualSpec(
                    theme = theme,
                    layout = layout,
                    primaryAssetKey = assetKey
                ),
                languageSpec = LanguageSpec(
                    primaryWord = hiName,
                    targetWord = satName,
                    englishWord = engName,
                    instructionHindi = "$hiName गिनें और सही संख्या चुनें:",
                    instructionSantali = "$satName ᱞᱮᱠᱷᱟᱭ ᱢᱮ ᱟᱨ ᱥᱟᱹᱦᱤ ᱮᱞ ᱵᱟᱪᱷᱟᱣ ᱢᱮ:",
                    phoneticGuide = "$engName ($satName)",
                    phraseHindi = "मीठा $hiName",
                    phraseSantali = "$satName ᱡᱚ",
                    phraseEnglish = "Sweet $engName"
                ),
                answerSpec = AnswerSpec(
                    correctValue = ansStr,
                    numericValue = ansStr.toIntOrNull() ?: qty,
                    distractors = opts.filter { it != ansStr },
                    teacherNote = "Answer: $ansStr ($hiName / $satName)"
                ),
                metadata = ActivityMetadata(
                    worksheetId = "ws_${objective.id}_${seed}",
                    activityId = "act_${objective.id}_${seed}",
                    generationSeed = seed,
                    modelVersion = "Qwen-0.5B-v2.5",
                    curriculumVersion = "NIPUN-2026.1"
                )
            )
        }

        fun fromActivityIR(ir: ActivityIR, objective: LearningObjective): ActivitySpec {
            val meta = SvgCorpusRegistry.get(ir.primaryObjectKey)
            val hiName = ir.hindiWord.ifBlank { meta?.hindiName ?: "आम" }
            val satName = ir.santaliWord.ifBlank { meta?.santaliName ?: "ᱩᱞ" }
            val engName = ir.englishWord.ifBlank { meta?.englishName ?: "Mango" }
            val item = ActivityItemSpec(
                id = ir.id,
                visualAsset = ir.primaryObjectKey,
                secondaryVisualAsset = ir.secondaryObjectKey,
                quantity = ir.quantity,
                secondaryQuantity = ir.secondaryQuantity,
                options = ir.distractorOptions,
                correctAnswer = ir.correctValue,
                explanationHindi = ir.instructionHindi,
                explanationSantali = ir.instructionSantali
            )
            val actType = ActivityType.values().firstOrNull { it.actionType == ir.actionType } ?: ActivityType.COUNT
            return ActivitySpec(
                id = ir.id,
                objectiveId = objective.id,
                activityType = actType,
                difficulty = ir.difficulty,
                items = listOf(item),
                visualSpec = VisualSpec(primaryAssetKey = ir.primaryObjectKey, secondaryAssetKey = ir.secondaryObjectKey),
                languageSpec = LanguageSpec(
                    primaryWord = hiName,
                    targetWord = satName,
                    englishWord = engName,
                    instructionHindi = ir.instructionHindi,
                    instructionSantali = ir.instructionSantali,
                    phoneticGuide = ir.phonicsGuide,
                    phraseHindi = ir.bilingualPhraseHindi,
                    phraseSantali = ir.bilingualPhraseSantali,
                    phraseEnglish = ir.phraseEnglishGloss
                ),
                answerSpec = AnswerSpec(
                    correctValue = ir.correctValue,
                    numericValue = ir.correctValue.toIntOrNull(),
                    distractors = ir.distractorOptions.filter { it != ir.correctValue },
                    teacherNote = ir.teacherSolutionNote
                ),
                metadata = ActivityMetadata(worksheetId = "ws_${ir.id}", activityId = ir.id, generationSeed = 42L)
            )
        }

        fun toActivityIR(spec: ActivitySpec): ActivityIR {
            val primaryItem = spec.items.firstOrNull() ?: ActivityItemSpec("default", "mango")
            return ActivityIR(
                id = spec.id,
                nipunCompetencyCode = spec.objectiveId,
                actionType = spec.activityType.actionType,
                grade = org.tribetalk.fln.model.FlnGrade.GRADE_1,
                difficulty = spec.difficulty,
                primaryObjectKey = primaryItem.visualAsset,
                secondaryObjectKey = primaryItem.secondaryVisualAsset,
                quantity = primaryItem.quantity,
                secondaryQuantity = primaryItem.secondaryQuantity,
                correctValue = primaryItem.correctAnswer,
                distractorOptions = primaryItem.options,
                santaliWord = spec.languageSpec.targetWord,
                hindiWord = spec.languageSpec.primaryWord,
                englishWord = spec.languageSpec.englishWord,
                bilingualPhraseSantali = spec.languageSpec.phraseSantali,
                bilingualPhraseHindi = spec.languageSpec.phraseHindi,
                phraseEnglishGloss = spec.languageSpec.phraseEnglish,
                instructionSantali = spec.languageSpec.instructionSantali,
                instructionHindi = spec.languageSpec.instructionHindi,
                teacherSolutionNote = spec.answerSpec.teacherNote,
                phonicsGuide = spec.languageSpec.phoneticGuide
            )
        }

        fun toWorksheetItem(spec: ActivitySpec): WorksheetItem {
            val ir = toActivityIR(spec)
            val meta = SvgCorpusRegistry.get(ir.primaryObjectKey)
            val assetPath = meta?.relativeFilePath ?: "fln_svg_corpus/plants/mango.svg"

            return WorksheetItem(
                id = spec.id,
                promptHindi = spec.languageSpec.instructionHindi.ifBlank { "गिनें और सही संख्या लिखें:" },
                promptSantali = spec.languageSpec.instructionSantali.ifBlank { "ᱞᱮᱠᱷᱟᱭ ᱢᱮ ᱟᱨ ᱥᱟᱹᱦᱤ ᱮᱞ ᱚᱞ ᱢᱮ:" },
                iconType = spec.visualSpec.primaryAssetKey,
                imageAssetPath = assetPath,
                quantity = spec.items.firstOrNull()?.quantity ?: 1,
                secondaryQuantity = spec.items.firstOrNull()?.secondaryQuantity ?: 0,
                leftLabelHindi = spec.answerSpec.correctValue,
                rightLabelSantali = spec.languageSpec.targetWord,
                options = spec.items.firstOrNull()?.options ?: emptyList(),
                correctIndex = spec.items.firstOrNull()?.options?.indexOf(spec.answerSpec.correctValue)?.coerceAtLeast(0) ?: 0,
                mathAnswer = spec.answerSpec.numericValue,
                teacherSolutionNote = spec.answerSpec.teacherNote,
                teacherPhoneticAnswer = spec.languageSpec.phoneticGuide,
                nipunCode = spec.objectiveId,
                activityIR = ir
            )
        }

        fun toFlnCard(spec: ActivitySpec): FlnCard {
            val item = spec.items.firstOrNull() ?: ActivityItemSpec("default", "mango")
            val meta = SvgCorpusRegistry.get(item.visualAsset)
            val assetPath = meta?.relativeFilePath ?: "fln_svg_corpus/plants/mango.svg"

            return FlnCard(
                id = spec.id,
                domain = FlnDomain.NUMERACY_COUNTING,
                category = FlnCategory.NUMBERS,
                santaliOlChiki = spec.languageSpec.targetWord,
                hindiText = spec.languageSpec.primaryWord,
                englishGloss = spec.languageSpec.englishWord,
                teacherPhoneticGuide = spec.languageSpec.phoneticGuide,
                santaliDevanagariPhonetic = spec.languageSpec.phoneticGuide,
                imageAssetPath = assetPath,
                vectorIconType = item.visualAsset,
                exemplarWordSantali = spec.languageSpec.phraseSantali,
                exemplarWordHindi = spec.languageSpec.phraseHindi,
                numeralValue = item.quantity,
                countingQuantity = item.quantity,
                countingTokenNameSantali = spec.languageSpec.targetWord,
                countingTokenNameHindi = spec.languageSpec.primaryWord,
                nipunCode = spec.objectiveId
            )
        }
    }
}
