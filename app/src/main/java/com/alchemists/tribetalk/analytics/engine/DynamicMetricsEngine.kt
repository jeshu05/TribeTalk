package com.alchemists.tribetalk.analytics.engine

import com.alchemists.tribetalk.analytics.models.*
import com.alchemists.tribetalk.curriculum.repository.FLNCurriculumRepository

object DynamicMetricsEngine {

    fun calculateSummary(attempts: List<AssessmentAttempt>, roster: ClassroomRoster = ClassroomRoster()): ClassroomAnalyticsSummary {
        if (attempts.isEmpty()) {
            return ClassroomAnalyticsSummary(
                roster = roster,
                totalStudentsAssessed = 0,
                totalQuestionsAttempted = 0,
                totalCorrect = 0,
                totalIncorrect = 0,
                classAccuracyPercentage = 0.0f,
                studentSummaries = emptyList(),
                outcomeProgressList = emptyList(),
                weakestOutcomes = emptyList(),
                recommendations = emptyList(),
                sessionHistory = emptyList()
            )
        }

        val totalAttempted = attempts.size
        val totalCorrect = attempts.count { it.isCorrect }
        val totalIncorrect = totalAttempted - totalCorrect
        val classAccuracy = (totalCorrect.toFloat() / totalAttempted) * 100.0f

        val assessedStudents = attempts.map { it.studentId }.toSet()

        // 1. Student-Level Analytics Calculation
        val studentGrouped = attempts.groupBy { it.studentId }
        val studentSummaries = mutableListOf<StudentAccuracySummary>()

        for ((studentId, sAttempts) in studentGrouped) {
            val sTot = sAttempts.size
            val sCorr = sAttempts.count { it.isCorrect }
            val sAcc = (sCorr.toFloat() / sTot) * 100.0f
            studentSummaries.add(
                StudentAccuracySummary(
                    studentId = studentId,
                    totalAttempted = sTot,
                    correctCount = sCorr,
                    accuracyPercentage = sAcc,
                    masteryThreshold = MasteryThreshold.fromAccuracy(sAcc)
                )
            )
        }

        // 2. Learning Outcome Analytics Calculation & Improvement Tracking
        val outcomeGrouped = attempts.groupBy { it.learningOutcomeId }
        val outcomeList = mutableListOf<LearningOutcomeProgress>()

        for ((outcomeId, oAttempts) in outcomeGrouped) {
            val oTot = oAttempts.size
            val oCorr = oAttempts.count { it.isCorrect }
            val oAcc = (oCorr.toFloat() / oTot) * 100.0f

            // Find topic name from FLNCurriculumRepository
            val lesson = FLNCurriculumRepository.lessons.find { it.learningOutcome.id == outcomeId || it.learningOutcome.nipunCode == outcomeId }
            val topicName = lesson?.topic ?: outcomeId

            // Chronological splits to compute previousAccuracy & improvementPoints
            val sortedAttempts = oAttempts.sortedBy { it.timestamp }
            val prevAcc = if (sortedAttempts.size >= 2) {
                val half = sortedAttempts.size / 2
                val firstHalf = sortedAttempts.take(half)
                (firstHalf.count { it.isCorrect }.toFloat() / firstHalf.size) * 100.0f
            } else null

            val improvement = if (prevAcc != null) oAcc - prevAcc else 0.0f

            outcomeList.add(
                LearningOutcomeProgress(
                    learningOutcomeId = outcomeId,
                    nipunCode = outcomeId,
                    topic = topicName,
                    totalAttempted = oTot,
                    correctCount = oCorr,
                    accuracyPercentage = oAcc,
                    masteryThreshold = MasteryThreshold.fromAccuracy(oAcc),
                    previousAccuracy = prevAcc,
                    improvementPoints = improvement
                )
            )
        }

        // 3. Weakest Outcomes Identification (< 60% Needs Reinforcement)
        val weakest = outcomeList.filter { it.accuracyPercentage < 60.0f }.sortedBy { it.accuracyPercentage }

        // 4. Dynamic Follow-up Recommendation Mapping
        val recommendations = mutableListOf<TeacherRecommendation>()

        for (weak in weakest) {
            val accInt = weak.accuracyPercentage.toInt()
            val lesson = FLNCurriculumRepository.lessons.find { it.learningOutcome.id == weak.learningOutcomeId || it.learningOutcome.nipunCode == weak.learningOutcomeId }
            val lessonId = lesson?.id ?: "fln_num_01"
            val topicName = lesson?.topic ?: weak.topic

            val followUpTitle = when {
                weak.topic.contains("Addition", ignoreCase = true) || weak.topic.contains("जोड़", ignoreCase = true) -> "Visual Addition Using Objects"
                weak.topic.contains("Subtraction", ignoreCase = true) || weak.topic.contains("घटाव", ignoreCase = true) -> "Visual Subtraction Activity"
                weak.topic.contains("Counting", ignoreCase = true) || weak.topic.contains("गिनती", ignoreCase = true) -> "Visual Counting Game"
                else -> "Reinforcement Practice: ${weak.topic}"
            }

            recommendations.add(
                TeacherRecommendation(
                    recommendationId = "rec_${weak.learningOutcomeId}",
                    learningOutcomeId = weak.learningOutcomeId,
                    weakSkill = weak.topic,
                    accuracyPercentage = weak.accuracyPercentage,
                    recommendedActivityTitle = followUpTitle,
                    recommendedActivityId = lessonId,
                    reasonHindi = "अधिगम परिणाम '${weak.topic}' में सटीकता $accInt% है (सुदृढ़ीकरण आवश्यक)।",
                    reasonSantali = "'${weak.topic}' ᱨᱮ ᱟᱨᱦᱚᱸ ᱚᱞ-ᱯᱟᱲᱦᱟᱣ ᱞᱟᱹᱠᱛᱤ ᱢᱮᱱᱟᱜᱼᱟ ($accInt%)᱾"
                )
            )
        }

        return ClassroomAnalyticsSummary(
            roster = roster,
            totalStudentsAssessed = assessedStudents.size,
            totalQuestionsAttempted = totalAttempted,
            totalCorrect = totalCorrect,
            totalIncorrect = totalIncorrect,
            classAccuracyPercentage = classAccuracy,
            studentSummaries = studentSummaries.sortedBy { it.studentId },
            outcomeProgressList = outcomeList,
            weakestOutcomes = weakest,
            recommendations = recommendations,
            sessionHistory = attempts.sortedBy { it.timestamp }
        )
    }
}
