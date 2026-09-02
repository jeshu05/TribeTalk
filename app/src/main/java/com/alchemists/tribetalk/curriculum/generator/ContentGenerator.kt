package com.alchemists.tribetalk.curriculum.generator

import com.alchemists.tribetalk.curriculum.models.*

interface ContentGenerator {
    fun generateLesson(
        outcome: LearningOutcome,
        grade: String,
        domain: FLNDomain,
        topic: String,
        difficulty: DifficultyLevel
    ): Lesson

    fun generateWorksheet(
        outcome: LearningOutcome,
        grade: String,
        domain: FLNDomain,
        numQuestions: Int,
        difficulty: DifficultyLevel
    ): Worksheet

    fun generateFlashcards(
        outcome: LearningOutcome,
        count: Int
    ): List<Flashcard>

    fun generateAssessment(
        outcome: LearningOutcome,
        numQuestions: Int,
        difficulty: DifficultyLevel
    ): List<AssessmentQuestion>
}
