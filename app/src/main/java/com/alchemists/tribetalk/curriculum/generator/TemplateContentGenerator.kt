package com.alchemists.tribetalk.curriculum.generator

import com.alchemists.tribetalk.curriculum.models.*

object TemplateContentGenerator : ContentGenerator {

    override fun generateLesson(
        outcome: LearningOutcome,
        grade: String,
        domain: FLNDomain,
        topic: String,
        difficulty: DifficultyLevel
    ): Lesson {
        val seed = System.currentTimeMillis()
        val activityItem = FLNActivityGenerator.generateActivity(
            outcome = outcome,
            type = ActivityType.COUNT_OBJECTS,
            difficulty = difficulty,
            seed = seed
        )

        val activity = Activity(
            id = "act_${outcome.id}_1",
            learningOutcomeId = outcome.id,
            titleHindi = activityItem.questionHindi,
            titleSantali = activityItem.questionSantali,
            type = activityItem.activityType,
            descriptionHindi = activityItem.explanationHindi,
            descriptionSantali = activityItem.questionSantali,
            interactiveItems = activityItem.optionsSantali,
            difficulty = difficulty
        )

        val assessments = generateAssessment(outcome, 2, difficulty)

        val instructions = listOf(
            LessonInstruction(
                id = "gen_inst_${outcome.id}_1",
                stepNumber = 1,
                teacherPromptHindi = "बच्चों, आज हम ${outcome.descriptionHindi} पर अभ्यास करेंगे।",
                santaliTranslation = "ᱵᱟᱹᱵᱩ, ᱛᱮᱦᱮᱧ ᱟᱵᱚ ${outcome.descriptionSantali} ᱵᱚᱱ ᱪᱮᱫᱟ᱾",
                phoneticDevanagari = "बाबू, तेहेञ आबो ${outcome.descriptionPhonetic}"
            )
        )

        return Lesson(
            id = "gen_les_${outcome.id}_${difficulty.name.lowercase()}",
            grade = grade,
            domain = domain,
            topic = topic,
            titleHindi = "${topic} (${difficulty.labelHindi})",
            titleSantali = "${topic} (${difficulty.labelSantali})",
            titlePhonetic = outcome.descriptionPhonetic,
            learningOutcome = outcome,
            instructions = instructions,
            activities = listOf(activity),
            assessmentQuestions = assessments
        )
    }

    override fun generateWorksheet(
        outcome: LearningOutcome,
        grade: String,
        domain: FLNDomain,
        numQuestions: Int,
        difficulty: DifficultyLevel
    ): Worksheet {
        return FLNWorksheetGenerator.generateWorksheet(
            outcome = outcome,
            grade = grade,
            domain = domain,
            numQuestions = numQuestions,
            difficulty = difficulty
        )
    }

    override fun generateFlashcards(
        outcome: LearningOutcome,
        count: Int
    ): List<Flashcard> {
        return FLNFlashcardGenerator.generateFlashcards(outcome, count)
    }

    override fun generateAssessment(
        outcome: LearningOutcome,
        numQuestions: Int,
        difficulty: DifficultyLevel
    ): List<AssessmentQuestion> {
        val items = mutableListOf<AssessmentQuestion>()
        val activityTypes = listOf(ActivityType.COUNT_OBJECTS, ActivityType.ADDITION, ActivityType.SUBTRACTION, ActivityType.NUMBER_RECOGNITION)

        for (i in 0 until numQuestions) {
            val seed = System.currentTimeMillis() + i * 100L
            val type = activityTypes[i % activityTypes.size]
            val generated = FLNActivityGenerator.generateActivity(outcome, type, difficulty, seed)

            items.add(
                AssessmentQuestion(
                    id = generated.questionId,
                    learningOutcomeId = outcome.id,
                    questionHindi = generated.questionHindi,
                    questionSantali = generated.questionSantali,
                    questionPhonetic = generated.questionPhonetic,
                    optionsHindi = generated.optionsHindi,
                    optionsSantali = generated.optionsSantali,
                    correctOptionIndex = generated.correctAnswerIndex,
                    explanationHindi = generated.explanationHindi,
                    difficulty = difficulty
                )
            )
        }
        return items
    }
}
