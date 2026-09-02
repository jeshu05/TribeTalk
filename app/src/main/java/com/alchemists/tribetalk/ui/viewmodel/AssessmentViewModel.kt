package com.alchemists.tribetalk.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.alchemists.tribetalk.assessment.generator.AssessmentQuestionGenerator
import com.alchemists.tribetalk.assessment.models.*
import com.alchemists.tribetalk.assessment.repository.AssessmentPersistenceRepository
import com.alchemists.tribetalk.curriculum.models.Lesson
import com.alchemists.tribetalk.curriculum.repository.FLNCurriculumRepository
import com.alchemists.tribetalk.translation.Language
import com.alchemists.tribetalk.voice.VoiceTranslationBridge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class StudentAssessmentUiState(
    val assessment: StudentAssessment? = null,
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isSubmitted: Boolean = false,
    val attemptsCount: Int = 1,
    val correctAnswersCount: Int = 0,
    val incorrectAnswersCount: Int = 0,
    val isCompleted: Boolean = false,
    val lastResult: AssessmentResult? = null,
    val questionResults: List<QuestionResultItem> = emptyList()
)

class AssessmentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(StudentAssessmentUiState())
    val uiState: StateFlow<StudentAssessmentUiState> = _uiState.asStateFlow()

    private var questionStartTimeMs: Long = System.currentTimeMillis()

    fun startAssessmentForLesson(lesson: Lesson, seed: Long? = null) {
        val assessment = AssessmentQuestionGenerator.generateAssessmentForLesson(lesson, seed)
        questionStartTimeMs = System.currentTimeMillis()
        _uiState.value = StudentAssessmentUiState(
            assessment = assessment,
            currentQuestionIndex = 0,
            selectedOptionIndex = null,
            isSubmitted = false,
            attemptsCount = 1,
            correctAnswersCount = 0,
            incorrectAnswersCount = 0,
            isCompleted = false,
            lastResult = null,
            questionResults = emptyList()
        )
    }

    fun startAssessmentForLessonId(lessonId: String, seed: Long? = null) {
        val lesson = FLNCurriculumRepository.getLessonById(lessonId) ?: FLNCurriculumRepository.lessons.first()
        startAssessmentForLesson(lesson, seed)
    }

    fun selectOption(index: Int) {
        _uiState.value = _uiState.value.copy(
            selectedOptionIndex = index,
            isSubmitted = false
        )
    }

    fun submitAnswer(context: Context? = null) {
        val state = _uiState.value
        val assessment = state.assessment ?: return
        val currentQ = assessment.questions.getOrNull(state.currentQuestionIndex) ?: return
        val selectedIdx = state.selectedOptionIndex ?: return

        val isCorrect = selectedIdx == currentQ.correctAnswerIndex
        val responseTime = System.currentTimeMillis() - questionStartTimeMs

        val activityType = when (currentQ.type) {
            com.alchemists.tribetalk.assessment.models.QuestionType.VISUAL_MULTIPLE_CHOICE -> com.alchemists.tribetalk.analytics.models.ActivityType.VISUAL_COUNTING
            com.alchemists.tribetalk.assessment.models.QuestionType.IMAGE_MATCHING -> com.alchemists.tribetalk.analytics.models.ActivityType.NUMBER_RECOGNITION
            com.alchemists.tribetalk.assessment.models.QuestionType.IDENTIFY_SELECT -> com.alchemists.tribetalk.analytics.models.ActivityType.PICTURE_IDENTIFICATION
            com.alchemists.tribetalk.assessment.models.QuestionType.ORDERING -> com.alchemists.tribetalk.analytics.models.ActivityType.MATCHING
        }

        // Persist individual attempt immediately for dynamic analytics calculation
        val attempt = com.alchemists.tribetalk.analytics.models.AssessmentAttempt(
            id = "att_${UUID.randomUUID()}",
            classId = "class_2a",
            studentId = "Student 01",
            lessonId = assessment.lessonId,
            activityId = "act_${assessment.lessonId}",
            learningOutcomeId = currentQ.learningOutcomeId,
            questionId = currentQ.id,
            activityType = activityType,
            selectedAnswer = "$selectedIdx",
            expectedAnswer = "${currentQ.correctAnswerIndex}",
            isCorrect = isCorrect,
            responseTimeMs = responseTime
        )
        com.alchemists.tribetalk.analytics.repository.ResponsePersistenceRepository.recordAttempt(context, attempt)

        val qResult = QuestionResultItem(
            questionId = currentQ.id,
            selectedOptionIndex = selectedIdx,
            correctOptionIndex = currentQ.correctAnswerIndex,
            isCorrect = isCorrect,
            attemptsCount = state.attemptsCount,
            responseTimeMs = responseTime
        )

        val updatedQResults = state.questionResults + qResult

        if (isCorrect) {
            val newCorrect = state.correctAnswersCount + 1
            _uiState.value = state.copy(
                isSubmitted = true,
                correctAnswersCount = newCorrect,
                questionResults = updatedQResults
            )
        } else {
            val newIncorrect = state.incorrectAnswersCount + 1
            _uiState.value = state.copy(
                isSubmitted = true,
                incorrectAnswersCount = newIncorrect,
                attemptsCount = state.attemptsCount + 1,
                questionResults = updatedQResults
            )
        }
    }

    fun nextQuestion(context: Context? = null) {
        val state = _uiState.value
        val assessment = state.assessment ?: return
        val nextIdx = state.currentQuestionIndex + 1

        if (nextIdx < assessment.questions.size) {
            questionStartTimeMs = System.currentTimeMillis()
            _uiState.value = state.copy(
                currentQuestionIndex = nextIdx,
                selectedOptionIndex = null,
                isSubmitted = false,
                attemptsCount = 1
            )
        } else {
            // Assessment Finished - Calculate Final Score & Mastery Level
            val total = assessment.questions.size
            val correct = state.correctAnswersCount
            val incorrect = total - correct
            val accuracy = if (total > 0) (correct.toFloat() / total) * 100.0f else 0.0f
            val mastery = MasteryLevel.fromAccuracy(accuracy)

            val result = AssessmentResult(
                resultId = "res_${UUID.randomUUID()}",
                lessonId = assessment.lessonId,
                learningOutcomeId = assessment.learningOutcomeId,
                concept = assessment.concept,
                totalQuestions = total,
                correctAnswers = correct,
                incorrectAnswers = incorrect,
                accuracyPercentage = accuracy,
                masteryLevel = mastery,
                questionResults = state.questionResults
            )

            // Save Offline to Persistence Layer
            AssessmentPersistenceRepository.saveAssessmentResult(context, result)

            _uiState.value = state.copy(
                isCompleted = true,
                lastResult = result
            )
        }
    }

    fun playSantaliAudio(
        promptHindi: String,
        promptSantali: String,
        voiceTranslationBridge: VoiceTranslationBridge
    ) {
        voiceTranslationBridge.translateAndSpeak(
            recognizedText = promptHindi,
            sourceLanguage = Language.HINDI,
            targetLanguage = Language.SANTALI,
            isVoiceBridgeEnabled = true,
            onStateChange = { _, _ -> },
            onResult = {}
        )
    }
}
