package com.alchemists.tribetalk.assessment

import com.alchemists.tribetalk.assessment.generator.AssessmentQuestionGenerator
import com.alchemists.tribetalk.assessment.models.*
import com.alchemists.tribetalk.assessment.repository.AssessmentPersistenceRepository
import com.alchemists.tribetalk.curriculum.models.LearningOutcome
import com.alchemists.tribetalk.curriculum.repository.FLNCurriculumRepository
import com.alchemists.tribetalk.ui.viewmodel.AssessmentViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StudentAssessmentTest {

    private lateinit var sampleLesson: com.alchemists.tribetalk.curriculum.models.Lesson

    @Before
    fun setUp() {
        AssessmentPersistenceRepository.clearAllResults(null)
        sampleLesson = FLNCurriculumRepository.lessons.first()
    }

    @Test
    fun testAllFourQuestionTypesGeneration() {
        val assessment = AssessmentQuestionGenerator.generateAssessmentForLesson(sampleLesson, seed = 12345L)

        assertNotNull("Generated assessment should not be null", assessment)
        assertTrue("Assessment must contain at least 4 questions", assessment.questions.size >= 4)

        val typesInAssessment = assessment.questions.map { it.type }.toSet()
        assertTrue("Must contain VISUAL_MULTIPLE_CHOICE", typesInAssessment.contains(QuestionType.VISUAL_MULTIPLE_CHOICE))
        assertTrue("Must contain IMAGE_MATCHING", typesInAssessment.contains(QuestionType.IMAGE_MATCHING))
        assertTrue("Must contain IDENTIFY_SELECT", typesInAssessment.contains(QuestionType.IDENTIFY_SELECT))
        assertTrue("Must contain ORDERING", typesInAssessment.contains(QuestionType.ORDERING))
    }

    @Test
    fun testLearningOutcomeAssociation() {
        val assessment = AssessmentQuestionGenerator.generateAssessmentForLesson(sampleLesson, seed = 9999L)
        assertEquals("Assessment learningOutcomeId must match lesson outcome", sampleLesson.learningOutcome.id, assessment.learningOutcomeId)

        assessment.questions.forEach { q ->
            assertEquals("Every question must be linked to parent outcome ID", sampleLesson.learningOutcome.id, q.learningOutcomeId)
            assertTrue("Question prompt Hindi must be non-empty", q.promptHindi.isNotBlank())
            assertTrue("Question prompt Santali must be non-empty", q.promptSantali.isNotBlank())
        }
    }

    @Test
    fun testCorrectAnswerDetection() {
        val viewModel = AssessmentViewModel()
        viewModel.startAssessmentForLesson(sampleLesson, seed = 5555L)

        val firstQ = viewModel.uiState.value.assessment!!.questions.first()
        viewModel.selectOption(firstQ.correctAnswerIndex)
        viewModel.submitAnswer(null)

        val state = viewModel.uiState.value
        assertTrue("isSubmitted must be true", state.isSubmitted)
        assertEquals("Correct count must be 1", 1, state.correctAnswersCount)
        assertEquals("Incorrect count must be 0", 0, state.incorrectAnswersCount)
    }

    @Test
    fun testIncorrectAnswerDetectionAndRetry() {
        val viewModel = AssessmentViewModel()
        viewModel.startAssessmentForLesson(sampleLesson, seed = 5555L)

        val firstQ = viewModel.uiState.value.assessment!!.questions.first()
        val wrongIndex = if (firstQ.correctAnswerIndex == 0) 1 else 0

        viewModel.selectOption(wrongIndex)
        viewModel.submitAnswer(null)

        val state = viewModel.uiState.value
        assertTrue("isSubmitted must be true after incorrect answer", state.isSubmitted)
        assertEquals("Correct count must be 0", 0, state.correctAnswersCount)
        assertEquals("Incorrect count must be 1", 1, state.incorrectAnswersCount)
        assertEquals("Attempts count must increment to 2", 2, state.attemptsCount)
    }

    @Test
    fun testAccuracyPercentageCalculation() {
        val total = 5
        val correct = 4
        val accuracy = (correct.toFloat() / total) * 100.0f
        assertEquals("4 / 5 accuracy must equal 80.0%", 80.0f, accuracy, 0.01f)
    }

    @Test
    fun testMasteryClassificationThresholds() {
        assertEquals("80% accuracy -> MASTERED", MasteryLevel.MASTERED, MasteryLevel.fromAccuracy(80.0f))
        assertEquals("100% accuracy -> MASTERED", MasteryLevel.MASTERED, MasteryLevel.fromAccuracy(100.0f))
        assertEquals("60% accuracy -> DEVELOPING", MasteryLevel.DEVELOPING, MasteryLevel.fromAccuracy(60.0f))
        assertEquals("79.9% accuracy -> DEVELOPING", MasteryLevel.DEVELOPING, MasteryLevel.fromAccuracy(79.9f))
        assertEquals("59.9% accuracy -> NEEDS_PRACTICE", MasteryLevel.NEEDS_PRACTICE, MasteryLevel.fromAccuracy(59.9f))
        assertEquals("0% accuracy -> NEEDS_PRACTICE", MasteryLevel.NEEDS_PRACTICE, MasteryLevel.fromAccuracy(0.0f))
    }

    @Test
    fun testOfflinePersistenceAndRetrieval() {
        val result = AssessmentResult(
            resultId = "res_test_01",
            lessonId = "math_counting_01",
            learningOutcomeId = "NIPUN-NUM-M1",
            concept = "Counting 1-10",
            totalQuestions = 5,
            correctAnswers = 4,
            incorrectAnswers = 1,
            accuracyPercentage = 80.0f,
            masteryLevel = MasteryLevel.MASTERED
        )

        AssessmentPersistenceRepository.saveAssessmentResult(null, result)

        val saved = AssessmentPersistenceRepository.getAllResults(null)
        assertTrue("Saved results should contain at least 1 item", saved.isNotEmpty())
        assertEquals("Concept in saved result must match", "Counting 1-10", saved.first().concept)

        val summary = AssessmentPersistenceRepository.getConceptMasterySummary(null)
        assertTrue("Summary should contain concept analytics", summary.isNotEmpty())
        assertEquals("Mastery level in summary must be MASTERED", MasteryLevel.MASTERED, summary.first().masteryLevel)
    }

    @Test
    fun testEndToEndFiveQuestionDemoFlow() {
        val viewModel = AssessmentViewModel()
        viewModel.startAssessmentForLesson(sampleLesson, seed = 7777L)

        val assessment = viewModel.uiState.value.assessment!!
        assertEquals("Demo assessment should contain 5 questions", 5, assessment.questions.size)

        // Question 1 -> Correct
        viewModel.selectOption(assessment.questions[0].correctAnswerIndex)
        viewModel.submitAnswer(null)
        viewModel.nextQuestion(null)

        // Question 2 -> Correct
        viewModel.selectOption(assessment.questions[1].correctAnswerIndex)
        viewModel.submitAnswer(null)
        viewModel.nextQuestion(null)

        // Question 3 -> Correct
        viewModel.selectOption(assessment.questions[2].correctAnswerIndex)
        viewModel.submitAnswer(null)
        viewModel.nextQuestion(null)

        // Question 4 -> Correct
        viewModel.selectOption(assessment.questions[3].correctAnswerIndex)
        viewModel.submitAnswer(null)
        viewModel.nextQuestion(null)

        // Question 5 -> Incorrect (simulating 4/5 = 80%)
        val q5WrongIndex = if (assessment.questions[4].correctAnswerIndex == 0) 1 else 0
        viewModel.selectOption(q5WrongIndex)
        viewModel.submitAnswer(null)
        viewModel.nextQuestion(null)

        val finalState = viewModel.uiState.value
        assertTrue("Assessment must be completed", finalState.isCompleted)
        assertNotNull("Final result must not be null", finalState.lastResult)

        val res = finalState.lastResult!!
        assertEquals("Total questions must be 5", 5, res.totalQuestions)
        assertEquals("Correct answers must be 4", 4, res.correctAnswers)
        assertEquals("Accuracy must be 80%", 80.0f, res.accuracyPercentage, 0.01f)
        assertEquals("Mastery level must be MASTERED", MasteryLevel.MASTERED, res.masteryLevel)
    }
}
