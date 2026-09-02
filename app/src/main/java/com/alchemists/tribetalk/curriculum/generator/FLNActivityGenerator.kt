package com.alchemists.tribetalk.curriculum.generator

import com.alchemists.tribetalk.curriculum.assets.LocalVisualAssetLibrary
import com.alchemists.tribetalk.curriculum.models.*
import com.alchemists.tribetalk.curriculum.validation.ActivityValidator
import com.alchemists.tribetalk.translation.OlChikiTransliterator
import java.util.UUID

object FLNActivityGenerator {

    private val numberNamesSantali = mapOf(
        1 to Pair("ᱢᱤᱫ", "मिद"),
        2 to Pair("ᱵᱟᱨ", "बार"),
        3 to Pair("ᱯᱮ", "पे"),
        4 to Pair("ᱯᱩᱱ", "पुन"),
        5 to Pair("ᱢᱚᱬᱮ", "मोणे"),
        6 to Pair("ᱛᱩᱨᱩᱭ", "तुरुय"),
        7 to Pair("ᱮᱭᱟᱭ", "एयाय"),
        8 to Pair("ᱤᱨᱟᱹᱞ", "इरल"),
        9 to Pair("ᱟᱨᱮ", "आरे"),
        10 to Pair("ᱜᱮᱞ", "गेल")
    )

    fun generateActivity(
        outcome: LearningOutcome,
        type: ActivityType,
        difficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
        seed: Long? = null
    ): GeneratedActivityItem {
        val effectiveSeed = seed ?: System.currentTimeMillis()

        val item = when (type) {
            ActivityType.COUNT_OBJECTS -> generateCountObjects(outcome, difficulty, effectiveSeed)
            ActivityType.NUMBER_RECOGNITION -> generateNumberRecognition(outcome, difficulty, effectiveSeed)
            ActivityType.NUMBER_SELECTION -> generateNumberSelection(outcome, difficulty, effectiveSeed)
            ActivityType.ADDITION -> generateAddition(outcome, difficulty, effectiveSeed)
            ActivityType.SUBTRACTION -> generateSubtraction(outcome, difficulty, effectiveSeed)
            ActivityType.IDENTIFY_SHAPE -> generateIdentifyShape(outcome, difficulty, effectiveSeed)
            ActivityType.IDENTIFY_COLOUR -> generateIdentifyColour(outcome, difficulty, effectiveSeed)
            ActivityType.PICTURE_WORD_MATCH -> generatePictureWordMatch(outcome, difficulty, effectiveSeed)
            ActivityType.MATCHING -> generateMatching(outcome, difficulty, effectiveSeed)
            ActivityType.SIMPLE_PATTERN -> generateSimplePattern(outcome, difficulty, effectiveSeed)
            else -> generateCountObjects(outcome, difficulty, effectiveSeed)
        }

        val valResult = ActivityValidator.validate(item)
        if (!valResult.isValid) {
            throw IllegalStateException("Generated activity item failed validation: ${valResult.errorMessage}")
        }
        return item
    }

    private fun generateCountObjects(
        outcome: LearningOutcome,
        difficulty: DifficultyLevel,
        seed: Long
    ): GeneratedActivityItem {
        val maxCount = when (difficulty) {
            DifficultyLevel.BEGINNER -> 5
            DifficultyLevel.INTERMEDIATE -> 8
            DifficultyLevel.ADVANCED -> 10
        }
        val targetCount = ((seed % maxCount).toInt() + 1).coerceIn(1, maxCount)
        val objAsset = LocalVisualAssetLibrary.getRandomObject(seed)

        val (nameSantali, namePhonetic) = numberNamesSantali[targetCount] ?: Pair("$targetCount", "$targetCount")
        val questionHindi = "चित्र में कितने ${objAsset.objectNameHindi} हैं?"
        val questionSantali = "ᱪᱤᱛᱟᱹᱨ ᱨᱮ ᱛᱤᱱᱟᱹᱜ ${objAsset.objectNameSantali} ᱢᱮᱱᱟᱜᱼᱟ?"
        val questionPhonetic = OlChikiTransliterator.toTeacherPhoneticHUD(questionSantali)

        val wrong1 = if (targetCount > 1) targetCount - 1 else targetCount + 1
        val wrong2 = if (targetCount < maxCount) targetCount + 1 else targetCount - 2
        val (wrong1Santali, _) = numberNamesSantali[wrong1] ?: Pair("$wrong1", "$wrong1")
        val (wrong2Santali, _) = numberNamesSantali[wrong2] ?: Pair("$wrong2", "$wrong2")

        val optionsHindi = listOf("$targetCount ($nameSantali)", "$wrong1 ($wrong1Santali)", "$wrong2 ($wrong2Santali)")
        val optionsSantali = listOf(nameSantali, wrong1Santali, wrong2Santali)

        return GeneratedActivityItem(
            questionId = "act_cnt_${outcome.id}_$seed",
            learningOutcomeId = outcome.id,
            nipunCode = outcome.nipunCode,
            grade = "Grade 1",
            subject = "Numeracy",
            domain = FLNDomain.NUMERACY,
            topic = "Counting Objects",
            difficulty = difficulty,
            activityType = ActivityType.COUNT_OBJECTS,
            stimulus = objAsset.copy(count = targetCount),
            questionHindi = questionHindi,
            questionSantali = questionSantali,
            questionPhonetic = questionPhonetic,
            optionsHindi = optionsHindi,
            optionsSantali = optionsSantali,
            correctAnswerIndex = 0,
            correctAnswerValue = "$targetCount ($nameSantali)",
            explanationHindi = "चित्र में $targetCount ${objAsset.objectNameHindi} हैं। $targetCount को संथाली में $nameSantali ($namePhonetic) कहते हैं।",
            assetReferences = listOf(objAsset.iconEmoji)
        )
    }

    private fun generateNumberRecognition(
        outcome: LearningOutcome,
        difficulty: DifficultyLevel,
        seed: Long
    ): GeneratedActivityItem {
        val targetNum = ((seed % 10).toInt() + 1).coerceIn(1, 10)
        val (nameSantali, namePhonetic) = numberNamesSantali[targetNum] ?: Pair("$targetNum", "$targetNum")

        val questionHindi = "संख्या '$targetNum' का सही संथाली नाम चुनें।"
        val questionSantali = "$targetNum ᱮᱞ ᱨᱮᱱᱟᱜ ᱴᱷᱤᱠ ᱥᱟᱱᱛᱟᱲᱤ ᱧᱩᱛᱩᱢ ᱪᱤᱱᱦᱟᱹᱣ ᱯᱮ᱾"

        val wrong1 = if (targetNum > 1) targetNum - 1 else targetNum + 2
        val wrong2 = if (targetNum < 10) targetNum + 1 else targetNum - 3
        val (wrong1Santali, _) = numberNamesSantali[wrong1] ?: Pair("$wrong1", "$wrong1")
        val (wrong2Santali, _) = numberNamesSantali[wrong2] ?: Pair("$wrong2", "$wrong2")

        return GeneratedActivityItem(
            questionId = "act_rec_${outcome.id}_$seed",
            learningOutcomeId = outcome.id,
            nipunCode = outcome.nipunCode,
            grade = "Grade 1",
            subject = "Numeracy",
            domain = FLNDomain.NUMERACY,
            topic = "Number Recognition",
            difficulty = difficulty,
            activityType = ActivityType.NUMBER_RECOGNITION,
            stimulus = VisualStimulus("संख्या $targetNum", "$targetNum ᱮᱞ", "🔢", count = targetNum),
            questionHindi = questionHindi,
            questionSantali = questionSantali,
            questionPhonetic = OlChikiTransliterator.toTeacherPhoneticHUD(questionSantali),
            optionsHindi = listOf(nameSantali, wrong1Santali, wrong2Santali),
            optionsSantali = listOf(nameSantali, wrong1Santali, wrong2Santali),
            correctAnswerIndex = 0,
            correctAnswerValue = nameSantali,
            explanationHindi = "संख्या $targetNum को संथाली में $nameSantali ($namePhonetic) कहते हैं।"
        )
    }

    private fun generateNumberSelection(
        outcome: LearningOutcome,
        difficulty: DifficultyLevel,
        seed: Long
    ): GeneratedActivityItem {
        val targetNum = ((seed % 10).toInt() + 1).coerceIn(1, 10)
        val (nameSantali, namePhonetic) = numberNamesSantali[targetNum] ?: Pair("$targetNum", "$targetNum")

        val questionHindi = "संथाली शब्द '$nameSantali' ($namePhonetic) के लिए सही अंक चुनें।"
        val questionSantali = "'$nameSantali' ᱥᱟᱱᱛᱟᱲᱤ ᱥᱟᱵᱟᱫᱽ ᱞᱟᱹᱜᱤᱫ ᱴᱷᱤᱠ ᱮᱞ ᱪᱤᱱᱦᱟᱹᱣ ᱯᱮ᱾"

        val wrong1 = if (targetNum > 1) targetNum - 1 else targetNum + 1
        val wrong2 = if (targetNum < 10) targetNum + 1 else targetNum - 2

        return GeneratedActivityItem(
            questionId = "act_sel_${outcome.id}_$seed",
            learningOutcomeId = outcome.id,
            nipunCode = outcome.nipunCode,
            grade = "Grade 1",
            subject = "Numeracy",
            domain = FLNDomain.NUMERACY,
            topic = "Number Selection",
            difficulty = difficulty,
            activityType = ActivityType.NUMBER_SELECTION,
            stimulus = VisualStimulus(nameSantali, nameSantali, "🔢", count = targetNum),
            questionHindi = questionHindi,
            questionSantali = questionSantali,
            questionPhonetic = OlChikiTransliterator.toTeacherPhoneticHUD(questionSantali),
            optionsHindi = listOf("$targetNum", "$wrong1", "$wrong2"),
            optionsSantali = listOf(OlChikiTransliterator.toOlChiki("$targetNum"), OlChikiTransliterator.toOlChiki("$wrong1"), OlChikiTransliterator.toOlChiki("$wrong2")),
            correctAnswerIndex = 0,
            correctAnswerValue = "$targetNum",
            explanationHindi = "'$nameSantali' का अर्थ अंक $targetNum होता है।"
        )
    }

    private fun generateAddition(
        outcome: LearningOutcome,
        difficulty: DifficultyLevel,
        seed: Long
    ): GeneratedActivityItem {
        val maxOp = when (difficulty) {
            DifficultyLevel.BEGINNER -> 3
            DifficultyLevel.INTERMEDIATE -> 5
            DifficultyLevel.ADVANCED -> 7
        }
        val op1 = ((seed % maxOp).toInt() + 1).coerceIn(1, maxOp)
        val op2 = (((seed / 2) % maxOp).toInt() + 1).coerceIn(1, maxOp)
        val sum = op1 + op2
        val (sumSantali, sumPhonetic) = numberNamesSantali[sum] ?: Pair("$sum", "$sum")

        val questionHindi = "$op1 + $op2 कितना होता है?"
        val questionSantali = "$op1 + $op2 ᱛᱤᱱᱟᱹᱜ ᱦᱩᱭᱩᱜᱼᱟ?"

        val wrong1 = sum + 1
        val wrong2 = if (sum > 2) sum - 1 else sum + 2
        val (w1Santali, _) = numberNamesSantali[wrong1] ?: Pair("$wrong1", "$wrong1")
        val (w2Santali, _) = numberNamesSantali[wrong2] ?: Pair("$wrong2", "$wrong2")

        return GeneratedActivityItem(
            questionId = "act_add_${outcome.id}_$seed",
            learningOutcomeId = outcome.id,
            nipunCode = outcome.nipunCode,
            grade = "Grade 1",
            subject = "Numeracy",
            domain = FLNDomain.NUMERACY,
            topic = "Addition",
            difficulty = difficulty,
            activityType = ActivityType.ADDITION,
            stimulus = VisualStimulus("$op1 + $op2", "$op1 + $op2", "➕", count = sum),
            questionHindi = questionHindi,
            questionSantali = questionSantali,
            questionPhonetic = OlChikiTransliterator.toTeacherPhoneticHUD(questionSantali),
            optionsHindi = listOf("$sum ($sumSantali)", "$wrong1 ($w1Santali)", "$wrong2 ($w2Santali)"),
            optionsSantali = listOf(sumSantali, w1Santali, w2Santali),
            correctAnswerIndex = 0,
            correctAnswerValue = "$sum ($sumSantali)",
            explanationHindi = "$op1 और $op2 को जोड़ने पर $sum ($sumSantali / $sumPhonetic) प्राप्त होता है।"
        )
    }

    private fun generateSubtraction(
        outcome: LearningOutcome,
        difficulty: DifficultyLevel,
        seed: Long
    ): GeneratedActivityItem {
        val op1 = ((seed % 5).toInt() + 3).coerceIn(3, 8)
        val op2 = ((seed % (op1 - 1)).toInt() + 1).coerceIn(1, op1 - 1)
        val diff = op1 - op2
        val (diffSantali, diffPhonetic) = numberNamesSantali[diff] ?: Pair("$diff", "$diff")

        val questionHindi = "$op1 - $op2 का मान क्या है?"
        val questionSantali = "$op1 - $op2 ᱨᱮᱱᱟᱜ ᱛᱮᱞᱟ ᱪᱮᱫ ᱠᱟᱱᱟ?"

        val wrong1 = diff + 1
        val wrong2 = if (diff > 1) diff - 1 else diff + 2
        val (w1Santali, _) = numberNamesSantali[wrong1] ?: Pair("$wrong1", "$wrong1")
        val (w2Santali, _) = numberNamesSantali[wrong2] ?: Pair("$wrong2", "$wrong2")

        return GeneratedActivityItem(
            questionId = "act_sub_${outcome.id}_$seed",
            learningOutcomeId = outcome.id,
            nipunCode = outcome.nipunCode,
            grade = "Grade 1",
            subject = "Numeracy",
            domain = FLNDomain.NUMERACY,
            topic = "Subtraction",
            difficulty = difficulty,
            activityType = ActivityType.SUBTRACTION,
            stimulus = VisualStimulus("$op1 - $op2", "$op1 - $op2", "➖", count = diff),
            questionHindi = questionHindi,
            questionSantali = questionSantali,
            questionPhonetic = OlChikiTransliterator.toTeacherPhoneticHUD(questionSantali),
            optionsHindi = listOf("$diff ($diffSantali)", "$wrong1 ($w1Santali)", "$wrong2 ($w2Santali)"),
            optionsSantali = listOf(diffSantali, w1Santali, w2Santali),
            correctAnswerIndex = 0,
            correctAnswerValue = "$diff ($diffSantali)",
            explanationHindi = "$op1 में से $op2 घटाने पर $diff ($diffSantali / $diffPhonetic) बचता है।"
        )
    }

    private fun generateIdentifyShape(
        outcome: LearningOutcome,
        difficulty: DifficultyLevel,
        seed: Long
    ): GeneratedActivityItem {
        val shape = LocalVisualAssetLibrary.getRandomShape(seed)
        val otherShapes = LocalVisualAssetLibrary.shapes.filter { it.objectNameHindi != shape.objectNameHindi }

        val questionHindi = "चित्र में दिखाई गई आकृति का नाम क्या है?"
        val questionSantali = "ᱪᱤᱛᱟᱹᱨ ᱨᱮ ᱧᱮᱞᱚᱜ ᱠᱟᱱ ᱨᱩᱯ ᱨᱮᱱᱟᱜ ᱧᱩᱛᱩᱢ ᱪᱮᱫ ᱠᱟᱱᱟ?"

        val optionsHindi = listOf(shape.objectNameHindi, otherShapes[0].objectNameHindi, otherShapes[1].objectNameHindi)
        val optionsSantali = listOf(shape.objectNameSantali, otherShapes[0].objectNameSantali, otherShapes[1].objectNameSantali)

        return GeneratedActivityItem(
            questionId = "act_shp_${outcome.id}_$seed",
            learningOutcomeId = outcome.id,
            nipunCode = outcome.nipunCode,
            grade = "Balvatika",
            subject = "Numeracy",
            domain = FLNDomain.NUMERACY,
            topic = "Shapes",
            difficulty = difficulty,
            activityType = ActivityType.IDENTIFY_SHAPE,
            stimulus = shape,
            questionHindi = questionHindi,
            questionSantali = questionSantali,
            questionPhonetic = OlChikiTransliterator.toTeacherPhoneticHUD(questionSantali),
            optionsHindi = optionsHindi,
            optionsSantali = optionsSantali,
            correctAnswerIndex = 0,
            correctAnswerValue = shape.objectNameHindi,
            explanationHindi = "${shape.iconEmoji} आकृति को संथाली में ${shape.objectNameSantali} कहते हैं।",
            assetReferences = listOf(shape.iconEmoji)
        )
    }

    private fun generateIdentifyColour(
        outcome: LearningOutcome,
        difficulty: DifficultyLevel,
        seed: Long
    ): GeneratedActivityItem {
        val colour = LocalVisualAssetLibrary.getRandomColour(seed)
        val otherColours = LocalVisualAssetLibrary.colours.filter { it.objectNameHindi != colour.objectNameHindi }

        val questionHindi = "चित्र में प्रदर्शित रंग कौन सा है?"
        val questionSantali = "ᱪᱤᱛᱟᱹᱨ ᱨᱮ ᱩᱫᱩᱜ ᱟᱠᱟᱱ ᱨᱚᱝ ᱚᱠᱟ ᱠᱟᱱᱟ?"

        val optionsHindi = listOf(colour.objectNameHindi, otherColours[0].objectNameHindi, otherColours[1].objectNameHindi)
        val optionsSantali = listOf(colour.objectNameSantali, otherColours[0].objectNameSantali, otherColours[1].objectNameSantali)

        return GeneratedActivityItem(
            questionId = "act_clr_${outcome.id}_$seed",
            learningOutcomeId = outcome.id,
            nipunCode = outcome.nipunCode,
            grade = "Balvatika",
            subject = "Literacy",
            domain = FLNDomain.LITERACY,
            topic = "Colours",
            difficulty = difficulty,
            activityType = ActivityType.IDENTIFY_COLOUR,
            stimulus = colour,
            questionHindi = questionHindi,
            questionSantali = questionSantali,
            questionPhonetic = OlChikiTransliterator.toTeacherPhoneticHUD(questionSantali),
            optionsHindi = optionsHindi,
            optionsSantali = optionsSantali,
            correctAnswerIndex = 0,
            correctAnswerValue = colour.objectNameHindi,
            explanationHindi = "${colour.iconEmoji} रंग को संथाली में ${colour.objectNameSantali} कहते हैं।",
            assetReferences = listOf(colour.iconEmoji)
        )
    }

    private fun generatePictureWordMatch(
        outcome: LearningOutcome,
        difficulty: DifficultyLevel,
        seed: Long
    ): GeneratedActivityItem {
        val objAsset = LocalVisualAssetLibrary.getRandomObject(seed)
        val otherObjs = (LocalVisualAssetLibrary.fruits + LocalVisualAssetLibrary.animals + LocalVisualAssetLibrary.schoolObjects)
            .filter { it.objectNameHindi != objAsset.objectNameHindi }

        val questionHindi = "चित्र ${objAsset.iconEmoji} के लिए सही संथाली शब्द चुनें।"
        val questionSantali = "ᱪᱤᱛᱟᱹᱨ ${objAsset.iconEmoji} ᱞᱟᱹᱜᱤᱫ ᱴᱷᱤᱠ ᱥᱟᱱᱛᱟᱲᱤ ᱥᱟᱵᱟᱫᱽ ᱪᱤᱱᱦᱟᱹᱣ ᱯᱮ᱾"

        val optionsHindi = listOf("${objAsset.objectNameHindi} (${objAsset.objectNameSantali})", "${otherObjs[0].objectNameHindi} (${otherObjs[0].objectNameSantali})", "${otherObjs[1].objectNameHindi} (${otherObjs[1].objectNameSantali})")
        val optionsSantali = listOf(objAsset.objectNameSantali, otherObjs[0].objectNameSantali, otherObjs[1].objectNameSantali)

        return GeneratedActivityItem(
            questionId = "act_pwm_${outcome.id}_$seed",
            learningOutcomeId = outcome.id,
            nipunCode = outcome.nipunCode,
            grade = "Grade 1",
            subject = "Literacy",
            domain = FLNDomain.LITERACY,
            topic = "Picture Word Matching",
            difficulty = difficulty,
            activityType = ActivityType.PICTURE_WORD_MATCH,
            stimulus = objAsset,
            questionHindi = questionHindi,
            questionSantali = questionSantali,
            questionPhonetic = OlChikiTransliterator.toTeacherPhoneticHUD(questionSantali),
            optionsHindi = optionsHindi,
            optionsSantali = optionsSantali,
            correctAnswerIndex = 0,
            correctAnswerValue = objAsset.objectNameSantali,
            explanationHindi = "${objAsset.objectNameHindi} को संथाली में ${objAsset.objectNameSantali} कहते हैं।",
            assetReferences = listOf(objAsset.iconEmoji)
        )
    }

    private fun generateMatching(
        outcome: LearningOutcome,
        difficulty: DifficultyLevel,
        seed: Long
    ): GeneratedActivityItem {
        val pairs = listOf(
            MatchingPair("पानी", "ᱫᱟᱜ", "Water", "ᱫᱟᱜ"),
            MatchingPair("किताब", "ᱯᱩᱛᱷᱤ", "Book", "ᱯᱩᱛᱷᱤ"),
            MatchingPair("फल", "ᱡᱚ", "Fruit", "ᱡᱚ")
        )
        val questionHindi = "बाईं ओर के शब्दों को सही संथाली अर्थ से मिलाएं।"
        val questionSantali = "ᱞᱮᱸᱜᱟ ᱯᱟᱦᱴᱟ ᱨᱮᱱᱟᱜ ᱥᱟᱵᱟᱫᱽ ᱴᱷᱤᱠ ᱥᱟᱱᱛᱟᱲᱤ ᱢᱮᱱᱮᱛ ᱥᱟᱶ ᱢᱤᱞᱟᱹᱣ ᱯᱮ᱾"

        return GeneratedActivityItem(
            questionId = "act_mat_${outcome.id}_$seed",
            learningOutcomeId = outcome.id,
            nipunCode = outcome.nipunCode,
            grade = "Grade 1",
            subject = "Literacy",
            domain = FLNDomain.LITERACY,
            topic = "Matching",
            difficulty = difficulty,
            activityType = ActivityType.MATCHING,
            stimulus = VisualStimulus("मिलान", "ᱢᱤᱞᱟᱹᱣ", "🔗"),
            questionHindi = questionHindi,
            questionSantali = questionSantali,
            questionPhonetic = OlChikiTransliterator.toTeacherPhoneticHUD(questionSantali),
            optionsHindi = pairs.map { "${it.leftItemHindi} ➜ ${it.leftItemSantali}" },
            optionsSantali = pairs.map { "${it.leftItemSantali} ➜ ${it.rightItemSantali}" },
            correctAnswerIndex = 0,
            correctAnswerValue = "All Pairs Matched",
            explanationHindi = "संथाली शब्दों का सही मिलान प्रस्तुत किया गया है।",
            matchingPairs = pairs
        )
    }

    private fun generateSimplePattern(
        outcome: LearningOutcome,
        difficulty: DifficultyLevel,
        seed: Long
    ): GeneratedActivityItem {
        val c1 = LocalVisualAssetLibrary.colours[0] // Red 🔴
        val c2 = LocalVisualAssetLibrary.colours[2] // Blue 🔵

        val questionHindi = "दिए गए पैटर्न को पूरा करें: 🔴 🔵 🔴 🔵 [ ? ]"
        val questionSantali = "ᱮᱢ ᱟᱠᱟᱱ ᱯᱮᱴᱚᱨᱱ ᱯᱩᱨᱟᱹᱣ ᱯᱮ: 🔴 🔵 🔴 🔵 [ ? ]"

        val optionsHindi = listOf("लाल / ᱟᱨᱟ 🔴", "नीला / ᱞᱤᱞ 🔵", "हरा / ᱦᱟᱹᱨᱤᱭᱟᱹᱲ 🟢")
        val optionsSantali = listOf("ᱟᱨᱟ 🔴", "ᱞᱤᱞ 🔵", "ᱦᱟᱹᱨᱤYᱟᱹᱲ 🟢")

        return GeneratedActivityItem(
            questionId = "act_pat_${outcome.id}_$seed",
            learningOutcomeId = outcome.id,
            nipunCode = outcome.nipunCode,
            grade = "Grade 1",
            subject = "Numeracy",
            domain = FLNDomain.NUMERACY,
            topic = "Patterns",
            difficulty = difficulty,
            activityType = ActivityType.SIMPLE_PATTERN,
            stimulus = VisualStimulus("पैटर्न", "ᱯᱮᱴᱚᱨᱱ", "🔴", secondaryIconEmoji = "🔵"),
            questionHindi = questionHindi,
            questionSantali = questionSantali,
            questionPhonetic = OlChikiTransliterator.toTeacherPhoneticHUD(questionSantali),
            optionsHindi = optionsHindi,
            optionsSantali = optionsSantali,
            correctAnswerIndex = 0,
            correctAnswerValue = "लाल / ᱟᱨᱟ 🔴",
            explanationHindi = "पैटर्न 🔴 और 🔵 का बारी-बारी से दोहराव है, इसलिए अगला रंग 🔴 है।",
            assetReferences = listOf("🔴", "🔵")
        )
    }
}
