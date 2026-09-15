package org.tribetalk.curriculum.context

import org.tribetalk.fln.model.FlnDomain
import org.tribetalk.fln.model.FlnGrade
import org.tribetalk.fln.model.WorksheetDifficulty

/**
 * Central instructional state for TribeTalk classroom sessions.
 * Source of truth for curriculum constraints, language policies, and learner progress.
 */
data class TeachingContext(
    val grade: FlnGrade = FlnGrade.GRADE_1,
    val domain: FlnDomain = FlnDomain.NUMERACY_COUNTING,
    val unitId: String = "NUM_U1_COUNTING",
    val lessonId: String = "G1_NUM_COUNT_1_10",
    val objectiveId: String = "NIPUN_G1_NUM_COUNT_1_10",
    val skillId: String = "COUNTING",
    val difficulty: WorksheetDifficulty = WorksheetDifficulty.EASY,
    val primaryLanguage: String = "hi",
    val targetLanguage: String = "sat",
    val lessonProgress: Float = 0.65f,
    val recentActivityIds: List<String> = emptyList()
) {
    fun withProgress(progress: Float): TeachingContext = copy(lessonProgress = progress.coerceIn(0f, 1f))

    fun withNextActivity(activityId: String): TeachingContext {
        val updated = (recentActivityIds + activityId).takeLast(10)
        return copy(recentActivityIds = updated)
    }

    fun toContextKey(): String = "${grade.name}_${domain.name}_${objectiveId}_${difficulty.name}"
}
