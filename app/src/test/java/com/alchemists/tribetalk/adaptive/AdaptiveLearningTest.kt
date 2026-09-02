package com.alchemists.tribetalk.adaptive

import com.alchemists.tribetalk.adaptive.engine.LearningRecommendationEngine
import com.alchemists.tribetalk.adaptive.models.ConceptProgress
import com.alchemists.tribetalk.adaptive.repository.AdaptiveLearningRepository
import com.alchemists.tribetalk.assessment.models.AssessmentResult
import com.alchemists.tribetalk.assessment.models.MasteryLevel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AdaptiveLearningTest {

    @Before
    fun setUp() {
        AdaptiveLearningRepository.clearAllData(null)
    }

    @Test
    fun testConceptMasteryCalculation() {
        val result = AssessmentResult(
            resultId = "res_01",
            lessonId = "math_counting_01",
            learningOutcomeId = "NIPUN-NUM-M1",
            concept = "Counting 1–10",
            totalQuestions = 5,
            correctAnswers = 4,
            incorrectAnswers = 1,
            accuracyPercentage = 80.0f,
            masteryLevel = MasteryLevel.MASTERED
        )

        AdaptiveLearningRepository.recordAssessmentResult(null, result)
        val progress = AdaptiveLearningRepository.getStudentProgress(null).find { it.learningOutcomeId == "NIPUN-NUM-M1" }

        assertNotNull("Progress should be recorded", progress)
        assertEquals("Mastery level should be MASTERED", MasteryLevel.MASTERED, progress!!.masteryLevel)
        assertEquals("Latest accuracy should be 80%", 80.0f, progress.latestAccuracy, 0.01f)
    }

    @Test
    fun testImprovementCalculationAndHistory() {
        // Attempt 1: 40% (Needs Practice)
        val res1 = AssessmentResult(
            resultId = "res_01",
            lessonId = "math_counting_01",
            learningOutcomeId = "NIPUN-NUM-M1",
            concept = "Counting 1–10",
            totalQuestions = 5,
            correctAnswers = 2,
            incorrectAnswers = 3,
            accuracyPercentage = 40.0f,
            masteryLevel = MasteryLevel.NEEDS_PRACTICE
        )
        AdaptiveLearningRepository.recordAssessmentResult(null, res1)

        // Attempt 2: 80% (Mastered) -> Improvement = +40%
        val res2 = AssessmentResult(
            resultId = "res_02",
            lessonId = "math_counting_01",
            learningOutcomeId = "NIPUN-NUM-M1",
            concept = "Counting 1–10",
            totalQuestions = 5,
            correctAnswers = 4,
            incorrectAnswers = 1,
            accuracyPercentage = 80.0f,
            masteryLevel = MasteryLevel.MASTERED
        )
        AdaptiveLearningRepository.recordAssessmentResult(null, res2)

        val progress = AdaptiveLearningRepository.getStudentProgress(null).find { it.learningOutcomeId == "NIPUN-NUM-M1" }!!

        assertEquals("Attempts count should be 2", 2, progress.attemptsCount)
        assertEquals("Previous accuracy should be 40%", 40.0f, progress.previousAccuracy!!, 0.01f)
        assertEquals("Latest accuracy should be 80%", 80.0f, progress.latestAccuracy, 0.01f)
        assertEquals("Improvement points should be +40%", 40.0f, progress.improvementPoints, 0.01f)

        val history = AdaptiveLearningRepository.getAssessmentHistory(null)
        assertEquals("Assessment history should contain 2 entries", 2, history.size)
    }

    @Test
    fun testRecommendationSelectionRules() {
        val weakProgress = ConceptProgress(
            learningOutcomeId = "NIPUN-NUM-M1",
            concept = "Counting 1–10",
            latestAccuracy = 40.0f,
            masteryLevel = MasteryLevel.NEEDS_PRACTICE
        )

        val rec = LearningRecommendationEngine.generateRecommendation(weakProgress)

        assertEquals("Recommendation priority for NEEDS_PRACTICE should be HIGH", "HIGH", rec.priority)
        assertTrue("Reason Hindi should mention weak accuracy", rec.reasonHindi.contains("40%"))
        assertTrue("Reason Santali should contain Ol Chiki prompt", rec.reasonSantali.isNotBlank())
    }

    @Test
    fun testClassLevelAggregationAndInsights() {
        val res = AssessmentResult(
            resultId = "res_c1",
            lessonId = "math_counting_01",
            learningOutcomeId = "NIPUN-NUM-M1",
            concept = "Counting 1–10",
            totalQuestions = 5,
            correctAnswers = 5,
            incorrectAnswers = 0,
            accuracyPercentage = 100.0f,
            masteryLevel = MasteryLevel.MASTERED
        )
        AdaptiveLearningRepository.recordAssessmentResult(null, res)

        val summary = AdaptiveLearningRepository.getClassOverviewSummary(null)

        assertNotNull("Class overview summary should not be null", summary)
        assertTrue("Class insights list should contain calculated metrics", summary.insights.isNotEmpty())
        assertTrue("Concept averages map should contain Counting 1–10", summary.conceptAverages.containsKey("Counting 1–10"))
    }

    @Test
    fun testEndToEndClosedLearningLoopSimulation() {
        // Step 1: Initial Assessment -> Score 40% (NEEDS PRACTICE)
        val initialRes = AssessmentResult(
            resultId = "res_init",
            lessonId = "math_addition_01",
            learningOutcomeId = "NIPUN-NUM-M3",
            concept = "Addition 1-5",
            totalQuestions = 5,
            correctAnswers = 2,
            incorrectAnswers = 3,
            accuracyPercentage = 40.0f,
            masteryLevel = MasteryLevel.NEEDS_PRACTICE
        )
        AdaptiveLearningRepository.recordAssessmentResult(null, initialRes)

        val rec1 = AdaptiveLearningRepository.getPrimaryRecommendation(null)
        assertEquals("System should recommend practice for Addition 1-5", "NIPUN-NUM-M3", rec1.learningOutcomeId)

        // Step 2: Student Practices and Re-Assesses -> Score 100% (MASTERED)
        val practiceRes = AssessmentResult(
            resultId = "res_prac",
            lessonId = "math_addition_01",
            learningOutcomeId = "NIPUN-NUM-M3",
            concept = "Addition 1-5",
            totalQuestions = 5,
            correctAnswers = 5,
            incorrectAnswers = 0,
            accuracyPercentage = 100.0f,
            masteryLevel = MasteryLevel.MASTERED
        )
        AdaptiveLearningRepository.recordAssessmentResult(null, practiceRes)

        // Step 3: Verify Status Updated to MASTERED and Improvement Recorded (+60%)
        val updatedProgress = AdaptiveLearningRepository.getStudentProgress(null).find { it.learningOutcomeId == "NIPUN-NUM-M3" }!!
        assertEquals("Mastery level should update to MASTERED", MasteryLevel.MASTERED, updatedProgress.masteryLevel)
        assertEquals("Improvement points should be +60%", 60.0f, updatedProgress.improvementPoints, 0.01f)
    }
}
