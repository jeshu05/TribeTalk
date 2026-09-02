package com.alchemists.tribetalk.assessment.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.alchemists.tribetalk.assessment.models.AssessmentResult
import com.alchemists.tribetalk.assessment.models.ConceptMasteryInfo
import com.alchemists.tribetalk.assessment.models.MasteryLevel
import org.json.JSONArray
import org.json.JSONObject

object AssessmentPersistenceRepository {

    private const val PREFS_NAME = "tribetalk_assessment_prefs"
    private const val KEY_RESULTS = "assessment_results_json"
    private const val TAG = "AssessmentPersistence"

    private val inMemoryResults = mutableListOf<AssessmentResult>()

    fun saveAssessmentResult(context: Context?, result: AssessmentResult) {
        inMemoryResults.add(result)
        if (context == null) return

        try {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val existingJsonStr = prefs.getString(KEY_RESULTS, "[]") ?: "[]"
            val array = JSONArray(existingJsonStr)

            val obj = JSONObject().apply {
                put("resultId", result.resultId)
                put("lessonId", result.lessonId)
                put("learningOutcomeId", result.learningOutcomeId)
                put("concept", result.concept)
                put("timestamp", result.timestamp)
                put("totalQuestions", result.totalQuestions)
                put("correctAnswers", result.correctAnswers)
                put("incorrectAnswers", result.incorrectAnswers)
                put("accuracyPercentage", result.accuracyPercentage.toDouble())
                put("masteryLevel", result.masteryLevel.name)
            }
            array.put(obj)

            prefs.edit().putString(KEY_RESULTS, array.toString()).apply()
            Log.d(TAG, "Saved assessment result: ${result.resultId} for ${result.concept}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save assessment result locally: ${e.message}")
        }
    }

    fun getAllResults(context: Context? = null): List<AssessmentResult> {
        if (context == null) return inMemoryResults

        val results = mutableListOf<AssessmentResult>()
        try {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val existingJsonStr = prefs.getString(KEY_RESULTS, "[]") ?: "[]"
            val array = JSONArray(existingJsonStr)

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val accuracy = obj.getDouble("accuracyPercentage").toFloat()
                results.add(
                    AssessmentResult(
                        resultId = obj.getString("resultId"),
                        lessonId = obj.getString("lessonId"),
                        learningOutcomeId = obj.getString("learningOutcomeId"),
                        concept = obj.getString("concept"),
                        timestamp = obj.getLong("timestamp"),
                        totalQuestions = obj.getInt("totalQuestions"),
                        correctAnswers = obj.getInt("correctAnswers"),
                        incorrectAnswers = obj.getInt("incorrectAnswers"),
                        accuracyPercentage = accuracy,
                        masteryLevel = MasteryLevel.fromAccuracy(accuracy)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse stored assessment results: ${e.message}")
        }

        // Combine in-memory if prefs empty
        return if (results.isNotEmpty()) results else inMemoryResults
    }

    fun getConceptMasterySummary(context: Context? = null): List<ConceptMasteryInfo> {
        val allRes = getAllResults(context)
        if (allRes.isEmpty()) return emptyList()

        val grouped = allRes.groupBy { it.concept }
        val summaries = mutableListOf<ConceptMasteryInfo>()

        for ((concept, resList) in grouped) {
            val avgAcc = resList.map { it.accuracyPercentage }.average().toFloat()
            val outcomeId = resList.first().learningOutcomeId
            summaries.add(
                ConceptMasteryInfo(
                    concept = concept,
                    learningOutcomeId = outcomeId,
                    totalAssessments = resList.size,
                    avgAccuracy = avgAcc,
                    masteryLevel = MasteryLevel.fromAccuracy(avgAcc)
                )
            )
        }
        return summaries
    }

    fun clearAllResults(context: Context? = null) {
        inMemoryResults.clear()
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.clear()?.apply()
    }
}
