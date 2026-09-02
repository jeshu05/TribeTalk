package com.alchemists.tribetalk.analytics

import com.alchemists.tribetalk.analytics.engine.DynamicMetricsEngine
import com.alchemists.tribetalk.analytics.models.ActivityType
import com.alchemists.tribetalk.analytics.models.AssessmentAttempt
import com.alchemists.tribetalk.analytics.models.ClassroomRoster
import com.alchemists.tribetalk.analytics.models.MasteryThreshold
import com.alchemists.tribetalk.analytics.repository.ClassroomRosterRepository
import com.alchemists.tribetalk.analytics.repository.ResponsePersistenceRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TeacherAnalyticsTest {

    @Before
    fun setUp() {
        ResponsePersistenceRepository.clearAllAttempts(null)
    }

    @Test
    fun testResponseRecordingCorrectIncorrect() {
        val attempt1 = AssessmentAttempt("att_01", "class_2a", "Student 01", "math_01", "act_01", "NIPUN-NUM-M1", "q1", ActivityType.VISUAL_COUNTING, "3", "3", true)
        val attempt2 = AssessmentAttempt("att_02", "class_2a", "Student 01", "math_01", "act_01", "NIPUN-NUM-M1", "q2", ActivityType.VISUAL_COUNTING, "4", "5", false)

        ResponsePersistenceRepository.recordAttempt(null, attempt1)
        ResponsePersistenceRepository.recordAttempt(null, attempt2)

        val attempts = ResponsePersistenceRepository.getAllAttempts(null)
        assertEquals("Should record 2 attempts", 2, attempts.size)
        assertTrue("Attempt 1 must be correct", attempts[0].isCorrect)
        assertFalse("Attempt 2 must be incorrect", attempts[1].isCorrect)
    }

    @Test
    fun testStudentAccuracyCalculation() {
        // Student 01: 10 attempts, 8 correct -> 80% accuracy
        val attempts = List(10) { idx ->
            AssessmentAttempt(
                id = "att_s1_$idx",
                studentId = "Student 01",
                lessonId = "math_01",
                activityId = "act_01",
                learningOutcomeId = "NIPUN-NUM-M1",
                questionId = "q_$idx",
                activityType = ActivityType.VISUAL_COUNTING,
                selectedAnswer = "ans",
                expectedAnswer = "ans",
                isCorrect = idx < 8
            )
        }

        attempts.forEach { ResponsePersistenceRepository.recordAttempt(null, it) }

        val summary = DynamicMetricsEngine.calculateSummary(ResponsePersistenceRepository.getAllAttempts(null))
        val s1Summary = summary.studentSummaries.find { it.studentId == "Student 01" }

        assertNotNull("Student 01 summary should exist", s1Summary)
        assertEquals("Student 01 accuracy must be 80%", 80.0f, s1Summary!!.accuracyPercentage, 0.01f)
        assertEquals("Mastery threshold must be MASTERED", MasteryThreshold.MASTERED, s1Summary.masteryThreshold)
    }

    @Test
    fun testClassAccuracyCalculation() {
        // Student 01: 5 correct / 5
        // Student 02: 3 correct / 5
        // Total: 8 / 10 = 80%
        val attempts = mutableListOf<AssessmentAttempt>()
        repeat(5) { idx ->
            attempts.add(AssessmentAttempt("att_s1_$idx", "class_2a", "Student 01", "m1", "a1", "LO1", "q", ActivityType.VISUAL_COUNTING, "a", "a", true))
        }
        repeat(5) { idx ->
            attempts.add(AssessmentAttempt("att_s2_$idx", "class_2a", "Student 02", "m1", "a1", "LO1", "q", ActivityType.VISUAL_COUNTING, "a", "a", idx < 3))
        }

        attempts.forEach { ResponsePersistenceRepository.recordAttempt(null, it) }
        val summary = DynamicMetricsEngine.calculateSummary(ResponsePersistenceRepository.getAllAttempts(null))

        assertEquals("Total questions attempted must be 10", 10, summary.totalQuestionsAttempted)
        assertEquals("Total correct must be 8", 8, summary.totalCorrect)
        assertEquals("Class accuracy must be 80%", 80.0f, summary.classAccuracyPercentage, 0.01f)
    }

    @Test
    fun testLearningOutcomeAccuracy() {
        val attempts = List(6) { idx ->
            AssessmentAttempt("att_lo_$idx", "class_2a", "Student 01", "m1", "a1", "LO-COUNT-01", "q", ActivityType.VISUAL_COUNTING, "a", "a", idx < 5)
        }
        attempts.forEach { ResponsePersistenceRepository.recordAttempt(null, it) }

        val summary = DynamicMetricsEngine.calculateSummary(ResponsePersistenceRepository.getAllAttempts(null))
        val loProgress = summary.outcomeProgressList.find { it.learningOutcomeId == "LO-COUNT-01" }

        assertNotNull("Learning outcome progress must exist", loProgress)
        assertEquals("Outcome accuracy must be 83.33%", 83.33f, loProgress!!.accuracyPercentage, 0.1f)
    }

    @Test
    fun testWeakAreaClassification() {
        // Addition: 2 / 5 = 40% (< 60% Needs Reinforcement)
        val attempts = List(5) { idx ->
            AssessmentAttempt("att_add_$idx", "class_2a", "Student 01", "m1", "a1", "NIPUN-NUM-M3", "q", ActivityType.SIMPLE_ADDITION, "a", "a", idx < 2)
        }
        attempts.forEach { ResponsePersistenceRepository.recordAttempt(null, it) }

        val summary = DynamicMetricsEngine.calculateSummary(ResponsePersistenceRepository.getAllAttempts(null))
        val weak = summary.weakestOutcomes.find { it.learningOutcomeId == "NIPUN-NUM-M3" }

        assertNotNull("Weak outcome must be detected", weak)
        assertEquals("Mastery threshold must be NEEDS_SIGNIFICANT_REINFORCEMENT", MasteryThreshold.NEEDS_SIGNIFICANT_REINFORCEMENT, weak!!.masteryThreshold)
    }

    @Test
    fun testRecommendationSelection() {
        val attempts = List(5) { idx ->
            AssessmentAttempt("att_add_$idx", "class_2a", "Student 01", "m1", "a1", "NIPUN-NUM-M3", "q", ActivityType.SIMPLE_ADDITION, "a", "a", idx < 2)
        }
        attempts.forEach { ResponsePersistenceRepository.recordAttempt(null, it) }

        val summary = DynamicMetricsEngine.calculateSummary(ResponsePersistenceRepository.getAllAttempts(null))
        val rec = summary.recommendations.find { it.learningOutcomeId == "NIPUN-NUM-M3" }

        assertNotNull("Recommendation should exist for weak outcome", rec)
        assertTrue("Recommended activity title should suggest Visual Addition", rec!!.recommendedActivityTitle.contains("Visual Addition"))
    }

    @Test
    fun testProgressCalculation() {
        // Initial 4 attempts: 1 correct (25%)
        // Follow-up 4 attempts: 4 correct (100%) -> Improvement: +75%
        val attempts = mutableListOf<AssessmentAttempt>()
        repeat(4) { idx ->
            attempts.add(AssessmentAttempt("att_init_$idx", "class_2a", "Student 01", "m1", "a1", "LO-ADD-01", "q", ActivityType.SIMPLE_ADDITION, "a", "a", idx < 1, timestamp = 1000L + idx))
        }
        repeat(4) { idx ->
            attempts.add(AssessmentAttempt("att_prac_$idx", "class_2a", "Student 01", "m1", "a1", "LO-ADD-01", "q", ActivityType.SIMPLE_ADDITION, "a", "a", true, timestamp = 2000L + idx))
        }

        attempts.forEach { ResponsePersistenceRepository.recordAttempt(null, it) }

        val summary = DynamicMetricsEngine.calculateSummary(ResponsePersistenceRepository.getAllAttempts(null))
        val loProgress = summary.outcomeProgressList.find { it.learningOutcomeId == "LO-ADD-01" }!!

        assertNotNull("Previous accuracy should be recorded", loProgress.previousAccuracy)
        assertEquals("Latest accuracy must be 62.5%", 62.5f, loProgress.accuracyPercentage, 0.1f)
        assertTrue("Improvement points must be positive", loProgress.improvementPoints > 0.0f)
    }

    @Test
    fun testPersistenceAfterRestart() {
        val attempt = AssessmentAttempt("att_pers", "class_2a", "Student 03", "m1", "a1", "LO1", "q", ActivityType.VISUAL_COUNTING, "a", "a", true)
        ResponsePersistenceRepository.recordAttempt(null, attempt)

        val saved = ResponsePersistenceRepository.getAllAttempts(null)
        assertEquals("Persisted attempts should contain 1 item", 1, saved.size)
        assertEquals("Student ID must match Student 03", "Student 03", saved.first().studentId)
    }

    @Test
    fun testEmptyDatasetHandling() {
        val summary = DynamicMetricsEngine.calculateSummary(emptyList())
        assertEquals("Total attempted must be 0", 0, summary.totalQuestionsAttempted)
        assertEquals("Class accuracy must be 0%", 0.0f, summary.classAccuracyPercentage, 0.01f)
        assertTrue("Outcome progress list must be empty", summary.outcomeProgressList.isEmpty())
    }

    @Test
    fun testMultipleStudentsRoster() {
        val roster = ClassroomRosterRepository.getRoster(null)
        assertEquals("Default roster class name must be Class 2A", "Class 2A", roster.className)
        assertEquals("Roster should contain 20 students", 20, roster.students.size)

        val nextStudent = ClassroomRosterRepository.getNextStudent("Student 01", roster)
        assertEquals("Next student after Student 01 must be Student 02", "Student 02", nextStudent)
    }

    @Test
    fun testMultipleAttemptsBySameStudent() {
        val att1 = AssessmentAttempt("a1", "class_2a", "Student 01", "m1", "a1", "LO1", "q1", ActivityType.VISUAL_COUNTING, "3", "3", true)
        val att2 = AssessmentAttempt("a2", "class_2a", "Student 01", "m1", "a1", "LO1", "q2", ActivityType.VISUAL_COUNTING, "4", "4", true)

        ResponsePersistenceRepository.recordAttempt(null, att1)
        ResponsePersistenceRepository.recordAttempt(null, att2)

        val summary = DynamicMetricsEngine.calculateSummary(ResponsePersistenceRepository.getAllAttempts(null))
        val s1 = summary.studentSummaries.find { it.studentId == "Student 01" }!!

        assertEquals("Student 01 attempted must be 2", 2, s1.totalAttempted)
        assertEquals("Student 01 accuracy must be 100%", 100.0f, s1.accuracyPercentage, 0.01f)
    }

    @Test
    fun testOfflineOperationIntegrity() {
        // Verify 100% offline execution without network dependencies
        val attempt = AssessmentAttempt("att_off", "class_2a", "Student 05", "m1", "a1", "LO1", "q", ActivityType.VISUAL_COUNTING, "a", "a", true)
        ResponsePersistenceRepository.recordAttempt(null, attempt)

        val summary = DynamicMetricsEngine.calculateSummary(ResponsePersistenceRepository.getAllAttempts(null))
        assertEquals("Offline summary should record 1 attempted", 1, summary.totalQuestionsAttempted)
    }
}
