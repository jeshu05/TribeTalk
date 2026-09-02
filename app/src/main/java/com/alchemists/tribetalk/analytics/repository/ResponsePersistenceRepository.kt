package com.alchemists.tribetalk.analytics.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.alchemists.tribetalk.analytics.models.ActivityType
import com.alchemists.tribetalk.analytics.models.AssessmentAttempt
import org.json.JSONArray
import org.json.JSONObject

object ResponsePersistenceRepository {

    private const val PREFS_NAME = "tribetalk_assessment_attempts_prefs"
    private const val KEY_ATTEMPTS = "assessment_attempts_json"
    private const val TAG = "ResponsePersistenceRepo"

    private val inMemoryAttempts = mutableListOf<AssessmentAttempt>()

    fun recordAttempt(context: Context?, attempt: AssessmentAttempt) {
        inMemoryAttempts.add(attempt)
        if (context == null) return

        try {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val existingJsonStr = prefs.getString(KEY_ATTEMPTS, "[]") ?: "[]"
            val array = JSONArray(existingJsonStr)

            val obj = JSONObject().apply {
                put("id", attempt.id)
                put("classId", attempt.classId)
                put("studentId", attempt.studentId)
                put("lessonId", attempt.lessonId)
                put("activityId", attempt.activityId)
                put("learningOutcomeId", attempt.learningOutcomeId)
                put("questionId", attempt.questionId)
                put("activityType", attempt.activityType.name)
                put("selectedAnswer", attempt.selectedAnswer)
                put("expectedAnswer", attempt.expectedAnswer)
                put("isCorrect", attempt.isCorrect)
                put("responseTimeMs", attempt.responseTimeMs)
                put("timestamp", attempt.timestamp)
                put("attemptNumber", attempt.attemptNumber)
            }
            array.put(obj)

            prefs.edit().putString(KEY_ATTEMPTS, array.toString()).apply()
            Log.d(TAG, "Recorded AssessmentAttempt ${attempt.id} for ${attempt.studentId}: isCorrect=${attempt.isCorrect}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist attempt: ${e.message}")
        }
    }

    fun getAllAttempts(context: Context? = null): List<AssessmentAttempt> {
        if (context == null) return inMemoryAttempts.toList()

        val attempts = mutableListOf<AssessmentAttempt>()
        try {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val existingJsonStr = prefs.getString(KEY_ATTEMPTS, "[]") ?: "[]"
            val array = JSONArray(existingJsonStr)

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val typeName = obj.optString("activityType", ActivityType.VISUAL_COUNTING.name)
                val type = try { ActivityType.valueOf(typeName) } catch (e: Exception) { ActivityType.VISUAL_COUNTING }

                attempts.add(
                    AssessmentAttempt(
                        id = obj.getString("id"),
                        classId = obj.optString("classId", "class_2a"),
                        studentId = obj.optString("studentId", "Student 01"),
                        lessonId = obj.getString("lessonId"),
                        activityId = obj.getString("activityId"),
                        learningOutcomeId = obj.getString("learningOutcomeId"),
                        questionId = obj.getString("questionId"),
                        activityType = type,
                        selectedAnswer = obj.getString("selectedAnswer"),
                        expectedAnswer = obj.getString("expectedAnswer"),
                        isCorrect = obj.getBoolean("isCorrect"),
                        responseTimeMs = obj.optLong("responseTimeMs", 0L),
                        timestamp = obj.getLong("timestamp"),
                        attemptNumber = obj.optInt("attemptNumber", 1)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load persisted attempts: ${e.message}")
        }

        return if (attempts.isNotEmpty()) attempts else inMemoryAttempts.toList()
    }

    fun clearAllAttempts(context: Context? = null) {
        inMemoryAttempts.clear()
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.clear()?.apply()
    }
}
