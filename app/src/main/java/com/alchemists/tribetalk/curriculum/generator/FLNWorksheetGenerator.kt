package com.alchemists.tribetalk.curriculum.generator

import com.alchemists.tribetalk.curriculum.models.*

object FLNWorksheetGenerator {

    fun generateWorksheet(
        outcome: LearningOutcome,
        grade: String,
        domain: FLNDomain,
        numQuestions: Int = 4,
        difficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
        seed: Long? = null
    ): Worksheet {
        val effectiveSeed = seed ?: System.currentTimeMillis()
        val generatedActivities = mutableListOf<GeneratedActivityItem>()
        val worksheetItems = mutableListOf<WorksheetItem>()

        val activityTypes = listOf(
            ActivityType.COUNT_OBJECTS,
            ActivityType.NUMBER_RECOGNITION,
            ActivityType.ADDITION,
            ActivityType.IDENTIFY_SHAPE,
            ActivityType.IDENTIFY_COLOUR,
            ActivityType.PICTURE_WORD_MATCH
        )

        for (i in 0 until numQuestions) {
            val type = activityTypes[i % activityTypes.size]
            val itemSeed = effectiveSeed + i * 1000L
            val activityItem = FLNActivityGenerator.generateActivity(
                outcome = outcome,
                type = type,
                difficulty = difficulty,
                seed = itemSeed
            )

            generatedActivities.add(activityItem)
            worksheetItems.add(
                WorksheetItem(
                    id = "ws_item_${outcome.id}_$i",
                    learningOutcomeId = outcome.id,
                    promptHindi = activityItem.questionHindi,
                    promptSantali = activityItem.questionSantali,
                    promptPhonetic = activityItem.questionPhonetic,
                    expectedAnswer = activityItem.correctAnswerValue,
                    optionsHindi = activityItem.optionsHindi,
                    optionsSantali = activityItem.optionsSantali,
                    activityItem = activityItem
                )
            )
        }

        return Worksheet(
            id = "ws_${outcome.id}_$effectiveSeed",
            learningOutcomeId = outcome.id,
            nipunCode = outcome.nipunCode,
            titleHindi = "FLN अभ्यास कार्यपत्रक (${outcome.nipunCode})",
            titleSantali = "FLN ᱠᱟᱹᱢᱤ ᱥᱟᱠᱟᱢ (${outcome.nipunCode})",
            grade = grade,
            domain = domain,
            difficulty = difficulty,
            items = worksheetItems,
            generatedActivities = generatedActivities
        )
    }
}
