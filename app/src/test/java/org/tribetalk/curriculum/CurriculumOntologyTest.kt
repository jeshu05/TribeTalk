package org.tribetalk.curriculum

import org.junit.Assert.*
import org.junit.Test
import org.tribetalk.curriculum.ontology.CurriculumRegistry
import org.tribetalk.curriculum.ontology.LearningObjective
import org.tribetalk.curriculum.spec.ActivityType
import org.tribetalk.fln.model.FlnDomain
import org.tribetalk.fln.model.FlnGrade

class CurriculumOntologyTest {

    @Test
    fun testDefaultObjectiveIsGrade1Numeracy() {
        val defaultObj = CurriculumRegistry.getDefaultObjective()
        assertNotNull(defaultObj)
        assertEquals("NIPUN_G1_NUM_COUNT_1_10", defaultObj.id)
        assertEquals(FlnGrade.GRADE_1, defaultObj.grade)
        assertEquals(FlnDomain.NUMERACY_COUNTING, defaultObj.domain)
        assertEquals(1..10, defaultObj.numberRange)
        assertTrue(defaultObj.allowedActivityTypes.contains(ActivityType.COUNT))
        assertTrue(defaultObj.allowedActivityTypes.contains(ActivityType.COUNT_AND_MATCH))
        assertTrue(defaultObj.allowedActivityTypes.contains(ActivityType.COMPARE_QUANTITIES))
        assertTrue(defaultObj.allowedActivityTypes.contains(ActivityType.CIRCLE_CORRECT))
    }

    @Test
    fun testAllObjectivesHaveMultilingualTitles() {
        val allObjectives = CurriculumRegistry.getAllObjectives()
        assertTrue("At least 3 objectives should be registered", allObjectives.size >= 3)

        for (obj in allObjectives) {
            assertFalse("ID must not be blank", obj.id.isBlank())
            assertFalse("English title must not be blank", obj.titleEnglish.isBlank())
            assertFalse("Hindi title must not be blank", obj.titleHindi.isBlank())
            assertFalse("Santali title must not be blank", obj.titleSantali.isBlank())
            assertFalse("Unit must not be blank", obj.unit.isBlank())
            assertFalse("Skill must not be blank", obj.skill.isBlank())
            assertTrue("Allowed activity types cannot be empty", obj.allowedActivityTypes.isNotEmpty())
            assertTrue("Allowed visual categories cannot be empty", obj.allowedVisualCategories.isNotEmpty())
        }
    }

    @Test
    fun testGradeFiltering() {
        val g1Objectives = CurriculumRegistry.getObjectivesForGrade(FlnGrade.GRADE_1)
        assertTrue("Grade 1 must have objectives", g1Objectives.isNotEmpty())
        for (obj in g1Objectives) {
            assertEquals(FlnGrade.GRADE_1, obj.grade)
        }

        val balObjectives = CurriculumRegistry.getObjectivesForGrade(FlnGrade.BALVATIKA)
        assertTrue("Balvatika must have objectives", balObjectives.isNotEmpty())
        for (obj in balObjectives) {
            assertEquals(FlnGrade.BALVATIKA, obj.grade)
        }
    }

    @Test
    fun testLookupById() {
        val found = CurriculumRegistry.getObjective("NIPUN_G1_NUM_COUNT_1_10")
        assertNotNull(found)
        assertEquals("Counting Objects 1 to 10", found?.titleEnglish)

        val notFound = CurriculumRegistry.getObjective("NON_EXISTENT_ID")
        assertNull(notFound)
    }

    @Test
    fun testPrerequisitesChain() {
        val g1Count = CurriculumRegistry.getObjective("NIPUN_G1_NUM_COUNT_1_10")
        assertNotNull(g1Count)
        assertTrue(g1Count!!.prerequisites.contains("NIPUN_BAL_NUM_COUNT_1_5"))

        val balCount = CurriculumRegistry.getObjective(g1Count.prerequisites.first())
        assertNotNull("Prerequisite objective must exist in registry", balCount)
        assertEquals(FlnGrade.BALVATIKA, balCount!!.grade)
    }
}
