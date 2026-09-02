package com.alchemists.tribetalk.adaptive.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.alchemists.tribetalk.adaptive.engine.LearningRecommendationEngine
import com.alchemists.tribetalk.adaptive.models.*
import com.alchemists.tribetalk.assessment.models.AssessmentResult
import com.alchemists.tribetalk.assessment.models.MasteryLevel
import com.alchemists.tribetalk.curriculum.repository.FLNCurriculumRepository
import org.json.JSONArray
import org.json.JSONObject

object AdaptiveLearningRepository {

    private const val PREFS_NAME = "tribetalk_adaptive_prefs"
    private const val KEY_HISTORY = "assessment_history_json"
    private const val KEY_PROGRESS = "concept_progress_json"
    private const val TAG = "AdaptiveLearningRepo"

    private val inMemoryHistory = mutableListOf<AssessmentHistoryEntry>()
    private val inMemoryProgress = mutableMapOf<String, ConceptProgress>() // conceptId -> ConceptProgress

    fun recordAssessmentResult(context: Context?, result: AssessmentResult) {
        val entry = AssessmentHistoryEntry(
            id = result.resultId,
            studentId = "std_01",
            lessonId = result.lessonId,
            learningOutcomeId = result.learningOutcomeId,
            concept = result.concept,
            timestamp = result.timestamp,
            scorePercentage = result.accuracyPercentage,
            correctCount = result.correctAnswers,
            totalQuestions = result.totalQuestions,
            masteryLevel = result.masteryLevel
        )

        inMemoryHistory.add(entry)

        // Update Concept Progress and Improvement Points
        val existing = inMemoryProgress[result.learningOutcomeId]
        val prevAcc = existing?.latestAccuracy
        val attempts = (existing?.attemptsCount ?: 0) + 1
        val improvement = if (prevAcc != null) result.accuracyPercentage - prevAcc else 0.0f

        val updatedProgress = ConceptProgress(
            studentId = "std_01",
            learningOutcomeId = result.learningOutcomeId,
            concept = result.concept,
            attemptsCount = attempts,
            questionsAnswered = (existing?.questionsAnswered ?: 0) + result.totalQuestions,
            correctAnswers = (existing?.correctAnswers ?: 0) + result.correctAnswers,
            latestAccuracy = result.accuracyPercentage,
            previousAccuracy = prevAcc,
            improvementPoints = improvement,
            masteryLevel = result.masteryLevel,
            lastPracticedAt = result.timestamp,
            recommendedAction = if (result.masteryLevel == MasteryLevel.MASTERED) "Mastered! Move to next topic." else "Practice Recommended"
        )

        inMemoryProgress[result.learningOutcomeId] = updatedProgress
        saveToPrefs(context)
    }

    fun getStudentProgress(context: Context? = null): List<ConceptProgress> {
        loadFromPrefs(context)

        // Seed prototype curriculum concepts if empty
        if (inMemoryProgress.isEmpty()) {
            val seedLessons = FLNCurriculumRepository.lessons
            seedLessons.forEachIndexed { idx, lesson ->
                val outcomeId = lesson.learningOutcome.id
                val (acc, mastery) = when (idx) {
                    0 -> Pair(70.0f, MasteryLevel.DEVELOPING)   // Counting
                    1 -> Pair(100.0f, MasteryLevel.MASTERED)   // Number Rec
                    2 -> Pair(45.0f, MasteryLevel.NEEDS_PRACTICE) // Addition
                    else -> Pair(80.0f, MasteryLevel.MASTERED)
                }
                inMemoryProgress[outcomeId] = ConceptProgress(
                    studentId = "std_01",
                    learningOutcomeId = outcomeId,
                    concept = lesson.topic,
                    attemptsCount = 1,
                    questionsAnswered = 5,
                    correctAnswers = (acc / 20.0f).toInt(),
                    latestAccuracy = acc,
                    masteryLevel = mastery
                )
            }
        }
        return inMemoryProgress.values.toList()
    }

    fun getAssessmentHistory(context: Context? = null): List<AssessmentHistoryEntry> {
        loadFromPrefs(context)
        return inMemoryHistory.toList()
    }

    fun getClassOverviewSummary(context: Context? = null): ClassOverviewSummary {
        val progressList = getStudentProgress(context)
        val conceptAverages = progressList.associate { it.concept to it.latestAccuracy }

        val needingSupport = progressList
            .filter { it.masteryLevel == MasteryLevel.NEEDS_PRACTICE || it.masteryLevel == MasteryLevel.DEVELOPING }
            .map { Pair("Birsa (std_01)", it.concept) }

        val masteredCount = progressList.count { it.masteryLevel == MasteryLevel.MASTERED }
        val needsPracticeCount = progressList.count { it.masteryLevel == MasteryLevel.NEEDS_PRACTICE }

        val insights = listOf(
            "$masteredCount NIPUN learning outcomes mastered.",
            "$needsPracticeCount concepts require additional practice support.",
            "Average counting accuracy improved by +20 percentage points."
        )

        return ClassOverviewSummary(
            totalStudents = 20,
            conceptAverages = conceptAverages,
            studentsNeedingSupport = needingSupport,
            insights = insights
        )
    }

    fun getPrimaryRecommendation(context: Context? = null): LearningRecommendation {
        val progressList = getStudentProgress(context)
        return LearningRecommendationEngine.selectPrimaryRecommendation(progressList)
    }

    fun clearAllData(context: Context? = null) {
        inMemoryHistory.clear()
        inMemoryProgress.clear()
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.clear()?.apply()
    }

    private fun saveToPrefs(context: Context?) {
        if (context == null) return
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val historyArr = JSONArray()
            inMemoryHistory.forEach { h ->
                val obj = JSONObject().apply {
                    put("id", h.id)
                    put("studentId", h.studentId)
                    put("lessonId", h.lessonId)
                    put("learningOutcomeId", h.learningOutcomeId)
                    put("concept", h.concept)
                    put("timestamp", h.timestamp)
                    put("scorePercentage", h.scorePercentage.toDouble())
                    put("correctCount", h.correctCount)
                    put("totalQuestions", h.totalQuestions)
                    put("masteryLevel", h.masteryLevel.name)
                }
                historyArr.put(obj)
            }
            prefs.edit().putString(KEY_HISTORY, historyArr.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save adaptive data: ${e.message}")
        }
    }

    private fun loadFromPrefs(context: Context?) {
        if (context == null || inMemoryHistory.isNotEmpty()) return
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val historyStr = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
            val array = JSONArray(historyStr)

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val acc = obj.getDouble("scorePercentage").toFloat()
                inMemoryHistory.add(
                    AssessmentHistoryEntry(
                        id = obj.getString("id"),
                        studentId = obj.optString("studentId", "std_01"),
                        lessonId = obj.getString("lessonId"),
                        learningOutcomeId = obj.getString("learningOutcomeId"),
                        concept = obj.getString("concept"),
                        timestamp = obj.getLong("timestamp"),
                        scorePercentage = acc,
                        correctCount = obj.getInt("correctCount"),
                        totalQuestions = obj.getInt("totalQuestions"),
                        masteryLevel = MasteryLevel.fromAccuracy(acc)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load adaptive history: ${e.message}")
        }
    }
}
