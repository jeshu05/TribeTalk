package com.alchemists.tribetalk.assessment.generator

import com.alchemists.tribetalk.assessment.models.*
import com.alchemists.tribetalk.curriculum.assets.LocalVisualAssetLibrary
import com.alchemists.tribetalk.curriculum.models.DifficultyLevel
import com.alchemists.tribetalk.curriculum.models.Lesson
import com.alchemists.tribetalk.curriculum.models.VisualStimulus
import com.alchemists.tribetalk.translation.OlChikiTransliterator

object AssessmentQuestionGenerator {

    private val santaliNumNames = mapOf(
        1 to "ᱢᱤᱫ", 2 to "ᱵᱟᱨ", 3 to "ᱯᱮ", 4 to "ᱯᱩᱱ", 5 to "ᱢᱚᱬᱮ",
        6 to "ᱛᱩᱨᱩᱭ", 7 to "ᱮᱭᱟᱭ", 8 to "ᱤᱨᱟᱹᱞ", 9 to "ᱟᱨᱮ", 10 to "ᱜᱮᱞ"
    )

    fun generateAssessmentForLesson(lesson: Lesson, seed: Long? = null): StudentAssessment {
        val effectiveSeed = seed ?: System.currentTimeMillis()
        val outcome = lesson.learningOutcome
        val questions = mutableListOf<AssessmentQuestionItem>()

        // 5 Polished Demo Questions covering all 4 QuestionTypes
        // Question 1: Visual Multiple Choice (Counting 3 Cars)
        questions.add(
            generateVisualMultipleChoice(
                outcomeId = outcome.id,
                objAsset = LocalVisualAssetLibrary.vehicles[0], // Car 🚗
                count = 3,
                qIndex = 1,
                seed = effectiveSeed
            )
        )

        // Question 2: Visual Multiple Choice (Counting 5 Apples)
        questions.add(
            generateVisualMultipleChoice(
                outcomeId = outcome.id,
                objAsset = LocalVisualAssetLibrary.fruits[0], // Apple 🍎
                count = 5,
                qIndex = 2,
                seed = effectiveSeed + 100L
            )
        )

        // Question 3: Image Matching (Select image with 2 Books)
        questions.add(
            generateImageMatching(
                outcomeId = outcome.id,
                objAsset = LocalVisualAssetLibrary.schoolObjects[0], // Book 📖
                targetCount = 2,
                qIndex = 3,
                seed = effectiveSeed + 200L
            )
        )

        // Question 4: Identify / Select (Circle ⭕ Shape)
        questions.add(
            generateIdentifySelect(
                outcomeId = outcome.id,
                targetAsset = LocalVisualAssetLibrary.shapes[0], // Circle ⭕
                qIndex = 4,
                seed = effectiveSeed + 300L
            )
        )

        // Question 5: Ordering / Sequence (1, 2, 3)
        questions.add(
            generateOrdering(
                outcomeId = outcome.id,
                qIndex = 5,
                seed = effectiveSeed + 400L
            )
        )

        return StudentAssessment(
            id = "assess_${lesson.id}_$effectiveSeed",
            lessonId = lesson.id,
            learningOutcomeId = outcome.id,
            titleHindi = "छात्र मूल्यांकन: ${lesson.titleHindi}",
            titleSantali = "ᱯᱟᱹᱴᱷᱩᱣᱟᱹ ᱡᱚᱠᱷᱟ: ${lesson.titleSantali}",
            concept = lesson.topic.ifBlank { "Counting 1–10" },
            questions = questions
        )
    }

    private fun generateVisualMultipleChoice(
        outcomeId: String,
        objAsset: VisualStimulus,
        count: Int,
        qIndex: Int,
        seed: Long
    ): AssessmentQuestionItem {
        val santaliName = santaliNumNames[count] ?: "$count"
        val qHindi = "चित्र में कितनी ${objAsset.objectNameHindi} हैं?"
        val qSantali = "ᱪᱤᱛᱟᱹᱨ ᱨᱮ ᱛᱤᱱᱟᱹᱜ ${objAsset.objectNameSantali} ᱢᱮᱱᱟᱜᱼᱟ?"

        val wrong1 = if (count > 1) count - 1 else count + 1
        val wrong2 = count + 1

        val options = listOf(
            QuestionOption("opt_1", "$count", santaliName, value = "$count"),
            QuestionOption("opt_2", "$wrong1", santaliNumNames[wrong1] ?: "$wrong1", value = "$wrong1"),
            QuestionOption("opt_3", "$wrong2", santaliNumNames[wrong2] ?: "$wrong2", value = "$wrong2")
        )

        return AssessmentQuestionItem(
            id = "q_vmc_${outcomeId}_$qIndex",
            type = QuestionType.VISUAL_MULTIPLE_CHOICE,
            promptHindi = qHindi,
            promptSantali = qSantali,
            promptPhonetic = OlChikiTransliterator.toTeacherPhoneticHUD(qSantali),
            stimulus = objAsset.copy(count = count),
            options = options,
            correctAnswerIndex = 0,
            explanationHindi = "चित्र में $count ${objAsset.objectNameHindi} हैं ($santaliName)।",
            learningOutcomeId = outcomeId
        )
    }

    private fun generateImageMatching(
        outcomeId: String,
        objAsset: VisualStimulus,
        targetCount: Int,
        qIndex: Int,
        seed: Long
    ): AssessmentQuestionItem {
        val santaliName = santaliNumNames[targetCount] ?: "$targetCount"
        val qHindi = "संख्या '$targetCount ($santaliName)' के बराबर वस्तुओं का चित्र चुनें।"
        val qSantali = "'$targetCount ($santaliName)' ᱮᱞ ᱥᱟᱶ ᱥᱚᱢᱟᱱ ᱪᱤᱛᱟᱹᱨ ᱪᱤᱱᱦᱟᱹᱣ ᱯᱮ᱾"

        val optCorrectEmoji = List(targetCount) { objAsset.iconEmoji }.joinToString(" ")
        val optWrong1Emoji = List(targetCount + 1) { objAsset.iconEmoji }.joinToString(" ")
        val optWrong2Emoji = List(if (targetCount > 1) targetCount - 1 else 4) { objAsset.iconEmoji }.joinToString(" ")

        val options = listOf(
            QuestionOption("opt_img_1", "$targetCount वस्तुओं का समूह", "$targetCount ᱡᱤᱱᱤᱥ", iconEmoji = optCorrectEmoji, value = "$targetCount"),
            QuestionOption("opt_img_2", "${targetCount + 1} वस्तुओं का समूह", "${targetCount + 1} ᱡᱤᱱᱤᱥ", iconEmoji = optWrong1Emoji, value = "${targetCount + 1}"),
            QuestionOption("opt_img_3", "समूह 3", "3 ᱡᱤᱱᱤᱥ", iconEmoji = optWrong2Emoji, value = "wrong")
        )

        return AssessmentQuestionItem(
            id = "q_img_${outcomeId}_$qIndex",
            type = QuestionType.IMAGE_MATCHING,
            promptHindi = qHindi,
            promptSantali = qSantali,
            promptPhonetic = OlChikiTransliterator.toTeacherPhoneticHUD(qSantali),
            stimulus = VisualStimulus("अंक $targetCount", "$targetCount ᱮᱞ", "🔢", count = targetCount),
            options = options,
            correctAnswerIndex = 0,
            explanationHindi = "संख्या $targetCount के बराबर समूह में $targetCount ${objAsset.objectNameHindi} हैं।",
            learningOutcomeId = outcomeId
        )
    }

    private fun generateIdentifySelect(
        outcomeId: String,
        targetAsset: VisualStimulus,
        qIndex: Int,
        seed: Long
    ): AssessmentQuestionItem {
        val qHindi = "चित्रों में से ${targetAsset.objectNameHindi} ⭕ चुनें।"
        val qSantali = "ᱪᱤᱛᱟᱹᱨ ᱠᱚ ᱢᱩᱫᱽ ᱨᱮ ${targetAsset.objectNameSantali} ᱪᱤᱱᱦᱟᱹᱣ ᱯᱮ᱾"

        val wrong1 = LocalVisualAssetLibrary.shapes[1] // Square ⏹️
        val wrong2 = LocalVisualAssetLibrary.shapes[2] // Triangle 🔺

        val options = listOf(
            QuestionOption("opt_id_1", targetAsset.objectNameHindi, targetAsset.objectNameSantali, iconEmoji = targetAsset.iconEmoji, value = targetAsset.objectNameHindi),
            QuestionOption("opt_id_2", wrong1.objectNameHindi, wrong1.objectNameSantali, iconEmoji = wrong1.iconEmoji, value = wrong1.objectNameHindi),
            QuestionOption("opt_id_3", wrong2.objectNameHindi, wrong2.objectNameSantali, iconEmoji = wrong2.iconEmoji, value = wrong2.objectNameHindi)
        )

        return AssessmentQuestionItem(
            id = "q_ids_${outcomeId}_$qIndex",
            type = QuestionType.IDENTIFY_SELECT,
            promptHindi = qHindi,
            promptSantali = qSantali,
            promptPhonetic = OlChikiTransliterator.toTeacherPhoneticHUD(qSantali),
            stimulus = targetAsset,
            options = options,
            correctAnswerIndex = 0,
            explanationHindi = "${targetAsset.iconEmoji} को संथाली में ${targetAsset.objectNameSantali} कहते हैं।",
            learningOutcomeId = outcomeId
        )
    }

    private fun generateOrdering(
        outcomeId: String,
        qIndex: Int,
        seed: Long
    ): AssessmentQuestionItem {
        val qHindi = "अंकों का सही क्रम चुनें: 1, 2, 3"
        val qSantali = "ᱮᱞ ᱨᱮᱱᱟᱜ ᱴᱷᱤᱠ ᱠᱨᱚᱢ ᱪᱤᱱᱦᱟᱹᱣ ᱯᱮ: 1, 2, 3"

        val options = listOf(
            QuestionOption("opt_ord_1", "1 ➜ 2 ➜ 3", "1 ➜ 2 ➜ 3", iconEmoji = "🔢", value = "1,2,3"),
            QuestionOption("opt_ord_2", "3 ➜ 1 ➜ 2", "3 ➜ 1 ➜ 2", iconEmoji = "🔢", value = "3,1,2"),
            QuestionOption("opt_ord_3", "2 ➜ 3 ➜ 1", "2 ➜ 3 ➜ 1", iconEmoji = "🔢", value = "2,3,1")
        )

        return AssessmentQuestionItem(
            id = "q_ord_${outcomeId}_$qIndex",
            type = QuestionType.ORDERING,
            promptHindi = qHindi,
            promptSantali = qSantali,
            promptPhonetic = OlChikiTransliterator.toTeacherPhoneticHUD(qSantali),
            stimulus = VisualStimulus("क्रम", "ᱠᱨᱚᱢ", "🔢", count = 3),
            options = options,
            correctAnswerIndex = 0,
            explanationHindi = "अंकों का बढ़ता क्रम 1 ➜ 2 ➜ 3 है।",
            learningOutcomeId = outcomeId
        )
    }
}
