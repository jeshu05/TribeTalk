package com.alchemists.tribetalk.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.alchemists.tribetalk.analytics.engine.DynamicMetricsEngine
import com.alchemists.tribetalk.analytics.models.AssessmentAttempt
import com.alchemists.tribetalk.analytics.models.ClassroomAnalyticsSummary
import com.alchemists.tribetalk.analytics.repository.ClassroomRosterRepository
import com.alchemists.tribetalk.analytics.repository.ResponsePersistenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TeacherAnalyticsViewModel : ViewModel() {

    private val _summaryState = MutableStateFlow(ClassroomAnalyticsSummary())
    val summaryState: StateFlow<ClassroomAnalyticsSummary> = _summaryState.asStateFlow()

    fun loadAnalytics(context: Context? = null) {
        val attempts = ResponsePersistenceRepository.getAllAttempts(context)
        val roster = ClassroomRosterRepository.getRoster(context)
        val calculated = DynamicMetricsEngine.calculateSummary(attempts, roster)
        _summaryState.value = calculated
    }

    fun recordNewAttempt(context: Context?, attempt: AssessmentAttempt) {
        ResponsePersistenceRepository.recordAttempt(context, attempt)
        loadAnalytics(context)
    }

    fun clearAllData(context: Context? = null) {
        ResponsePersistenceRepository.clearAllAttempts(context)
        loadAnalytics(context)
    }
}
