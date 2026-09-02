package com.alchemists.tribetalk.curriculum

import com.alchemists.tribetalk.curriculum.generator.TemplateContentGenerator
import com.alchemists.tribetalk.curriculum.models.DifficultyLevel
import com.alchemists.tribetalk.curriculum.models.FLNDomain
import com.alchemists.tribetalk.curriculum.repository.FLNCurriculumRepository
import com.alchemists.tribetalk.ui.viewmodel.FLNViewModel
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class FLNContentGeneratorTest {

    @Test
    fun testContentGeneratorVariantGeneration() {
        val lesson = FLNCurriculumRepository.lessons.first()
        val outcome = lesson.learningOutcome

        // Generate multiple worksheet items from the same learning outcome
        val worksheet = TemplateContentGenerator.generateWorksheet(
            outcome = outcome,
            grade = lesson.grade,
            domain = lesson.domain,
            numQuestions = 4,
            difficulty = DifficultyLevel.BEGINNER
        )

        assertNotNull("Generated worksheet should not be null", worksheet)
        assertEquals("Worksheet NIPUN code must match outcome", outcome.nipunCode, worksheet.nipunCode)
        assertEquals("Worksheet learningOutcomeId must match outcome ID", outcome.id, worksheet.learningOutcomeId)
        assertEquals("Worksheet should contain 4 items", 4, worksheet.items.size)

        // Verify that different question variants were generated for the same outcome
        val prompt1 = worksheet.items[0].promptHindi
        val prompt2 = worksheet.items[1].promptHindi
        assertNotEquals("Prompts for different items should be distinct variants", prompt1, prompt2)
    }

    @Test
    fun testNIPUNLearningOutcomePreservation() {
        val outcome = FLNCurriculumRepository.lessons.first().learningOutcome

        val generatedLesson = TemplateContentGenerator.generateLesson(
            outcome = outcome,
            grade = "Grade 1",
            domain = FLNDomain.NUMERACY,
            topic = "Counting Objects",
            difficulty = DifficultyLevel.INTERMEDIATE
        )

        assertEquals("Generated lesson outcome ID must match original", outcome.id, generatedLesson.learningOutcome.id)
        assertEquals("Generated lesson NIPUN code must match original", outcome.nipunCode, generatedLesson.learningOutcome.nipunCode)

        generatedLesson.activities.forEach { act ->
            assertEquals("Activity learningOutcomeId must match outcome ID", outcome.id, act.learningOutcomeId)
        }

        generatedLesson.assessmentQuestions.forEach { q ->
            assertEquals("Assessment question learningOutcomeId must match outcome ID", outcome.id, q.learningOutcomeId)
        }
    }

    @Test
    fun testViewModelStateManagement() {
        val viewModel = FLNViewModel()

        // Filter by NUMERACY
        viewModel.selectDomainFilter(FLNDomain.NUMERACY)
        val selectionState = viewModel.selectionState.value
        assertEquals("Selection filter should be NUMERACY", FLNDomain.NUMERACY, selectionState.domainFilter)
        assertTrue("Filtered lessons should only be NUMERACY", selectionState.lessons.all { it.domain == FLNDomain.NUMERACY })

        // Load specific lesson
        val targetLessonId = "fln_num_01"
        viewModel.loadLesson(targetLessonId)
        val detailState = viewModel.detailState.value
        assertNotNull("Loaded lesson should not be null", detailState.lesson)
        assertEquals("Loaded lesson ID should match target", targetLessonId, detailState.lesson?.id)
    }

    @Test
    fun testAbsenceOfHardcodedCurriculumStringsInUI() {
        val candidateDir1 = File("app/src/main/java/com/alchemists/tribetalk/ui/screens")
        val candidateDir2 = File("src/main/java/com/alchemists/tribetalk/ui/screens")
        val uiDir = if (candidateDir1.exists()) candidateDir1 else candidateDir2

        assertTrue("UI screens directory should exist", uiDir.exists() && uiDir.isDirectory)

        val forbiddenHardcodedStrings = listOf(
            "बच्चों आज हम गिनती सीखेंगे",
            "एक, दो, तीन, चार, पाँच!",
            "1 ᱠᱷᱚᱱ 10 ᱦᱟᱹᱵᱤᱡ ᱞᱮᱠᱷᱟ",
            "तीन के बाद कौन सी संख्या आती है"
        )

        uiDir.walk().filter { it.extension == "kt" }.forEach { file ->
            val content = file.readText()
            forbiddenHardcodedStrings.forEach { forbidden ->
                assertFalse(
                    "UI file ${file.name} must not contain hardcoded curriculum content string: '$forbidden'",
                    content.contains(forbidden)
                )
            }
        }
    }
}
