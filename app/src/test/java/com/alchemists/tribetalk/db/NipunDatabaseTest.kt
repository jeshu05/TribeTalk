package com.alchemists.tribetalk.db

import com.alchemists.tribetalk.analytics.engine.DynamicMetricsEngine
import com.alchemists.tribetalk.analytics.models.ActivityType
import com.alchemists.tribetalk.analytics.models.AssessmentAttempt
import com.alchemists.tribetalk.analytics.repository.ClassroomRosterRepository
import com.alchemists.tribetalk.analytics.repository.ResponsePersistenceRepository
import com.alchemists.tribetalk.db.repository.NipunLocalRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NipunDatabaseTest {

    @Before
    fun setUp() {
        ResponsePersistenceRepository.clearAllAttempts(null)
    }

    @Test
    fun testClassroomFiltering() {
        val roster = ClassroomRosterRepository.getRoster(null)
        assertNotNull("Classroom roster should exist", roster)
        assertEquals("Classroom grade name must be Class 2A", "Class 2A", roster.className)
        assertEquals("Should contain 20 students", 20, roster.students.size)

        val lessons = NipunLocalRepository.getLessonsForGrade("Class 1")
        assertTrue("Lessons for Class 1 must not be empty", lessons.isNotEmpty())
    }

    @Test
    fun testLocalSearchCurriculum() {
        val results = NipunLocalRepository.searchCurriculum("1 से 10")
        assertTrue("Search results for '1 से 10' should return matching lessons", results.isNotEmpty())
    }

    @Test
    fun testTeacherResourceUpload() {
        NipunLocalRepository.addTeachingResource("class_1", "lo_num_01", "Math Worksheet PDF", "PDF", "assets/resources/math.pdf")
        val resources = NipunLocalRepository.getResourcesForClass("class_1")
        assertEquals("Should contain 1 uploaded resource", 1, resources.size)
        assertEquals("Resource type must be PDF", "PDF", resources.first().type)
    }

    @Test
    fun testAssessmentAttemptRecordingAndAnalytics() {
        val attempt1 = AssessmentAttempt("att_t1", "class_2a", "Student 01", "fln_num_01", "act_01", "NIPUN-NUM-M1", "q1", ActivityType.VISUAL_COUNTING, "3", "3", true)
        val attempt2 = AssessmentAttempt("att_t2", "class_2a", "Student 01", "fln_num_01", "act_01", "NIPUN-NUM-M1", "q2", ActivityType.VISUAL_COUNTING, "4", "4", true)

        ResponsePersistenceRepository.recordAttempt(null, attempt1)
        ResponsePersistenceRepository.recordAttempt(null, attempt2)

        val attempts = ResponsePersistenceRepository.getAllAttempts(null)
        assertEquals("Attempts recorded must be 2", 2, attempts.size)

        val summary = DynamicMetricsEngine.calculateSummary(attempts)
        assertEquals("Total questions attempted must be 2", 2, summary.totalQuestionsAttempted)
        assertEquals("Class accuracy percentage must be 100%", 100.0f, summary.classAccuracyPercentage, 0.01f)
    }

    @Test
    fun testEmptyAnalyticsHandling() {
        val summary = DynamicMetricsEngine.calculateSummary(emptyList())
        assertEquals("Total questions attempted must be 0", 0, summary.totalQuestionsAttempted)
        assertEquals("Class accuracy must be 0%", 0.0f, summary.classAccuracyPercentage, 0.01f)
        assertTrue("Student summaries must be empty", summary.studentSummaries.isEmpty())
    }
}
