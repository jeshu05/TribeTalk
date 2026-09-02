package com.alchemists.tribetalk.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.alchemists.tribetalk.adaptive.models.*
import com.alchemists.tribetalk.adaptive.repository.AdaptiveLearningRepository
import com.alchemists.tribetalk.assessment.models.AssessmentResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AdaptiveLearningUiState(
    val studentProfile: StudentProfile = StudentProfile(),
    val conceptProgressList: List<ConceptProgress> = emptyList(),
    val primaryRecommendation: LearningRecommendation? = null,
    val classSummary: ClassOverviewSummary = ClassOverviewSummary(),
    val historyList: List<AssessmentHistoryEntry> = emptyList()
)

class AdaptiveLearningViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AdaptiveLearningUiState())
    val uiState: StateFlow<AdaptiveLearningUiState> = _uiState.asStateFlow()

    fun loadData(context: Context? = null) {
        val progress = AdaptiveLearningRepository.getStudentProgress(context)
        val rec = AdaptiveLearningRepository.getPrimaryRecommendation(context)
        val classSum = AdaptiveLearningRepository.getClassOverviewSummary(context)
        val history = AdaptiveLearningRepository.getAssessmentHistory(context)

        _uiState.value = AdaptiveLearningUiState(
            studentProfile = StudentProfile(),
            conceptProgressList = progress,
            primaryRecommendation = rec,
            classSummary = classSum,
            historyList = history
        )
    }

    fun recordNewAssessmentResult(context: Context?, result: AssessmentResult) {
        AdaptiveLearningRepository.recordAssessmentResult(context, result)
        loadData(context)
    }
}
