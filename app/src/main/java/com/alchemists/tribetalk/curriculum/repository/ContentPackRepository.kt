package com.alchemists.tribetalk.curriculum.repository

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.alchemists.tribetalk.curriculum.models.*
import com.alchemists.tribetalk.translation.OlChikiTransliterator
import org.json.JSONObject
import java.io.InputStream

object ContentPackRepository {

    private const val TAG = "ContentPackRepository"
    private var cachedLessons: List<Lesson>? = null

    fun loadContentPackLessons(context: Context): List<Lesson> {
        cachedLessons?.let { return it }

        val loadedLessons = mutableListOf<Lesson>()
        try {
            val manifestStream: InputStream = context.assets.open("content_pack/manifest.json")
            val manifestStr = manifestStream.bufferedReader().use { it.readText() }
            val manifestJson = JSONObject(manifestStr)
            val lessonArray = manifestJson.getJSONArray("lessons")

            for (i in 0 until lessonArray.length()) {
                val lessonId = lessonArray.getString(i)
                val lessonJsonPath = "content_pack/lessons/$lessonId/lesson.json"
                try {
                    val stream: InputStream = context.assets.open(lessonJsonPath)
                    val jsonStr = stream.bufferedReader().use { it.readText() }
                    val json = JSONObject(jsonStr)

                    val outcomeObj = json.getJSONObject("learning_outcome")
                    val titleObj = json.getJSONObject("title")
                    val instObj = json.getJSONObject("teacher_instruction")
                    val actObj = json.getJSONObject("activity")
                    val assessObj = json.getJSONObject("assessment")
                    val visualObj = json.optJSONObject("visual_asset")

                    val outcome = LearningOutcome(
                        id = json.getString("outcome_id"),
                        nipunCode = json.getString("outcome_id"),
                        descriptionHindi = outcomeObj.getString("hindi"),
                        descriptionSantali = outcomeObj.getString("santali"),
                        descriptionPhonetic = OlChikiTransliterator.toTeacherPhoneticHUD(outcomeObj.getString("santali"))
                    )

                    val instruction = LessonInstruction(
                        id = "inst_${lessonId}_1",
                        stepNumber = 1,
                        teacherPromptHindi = instObj.getString("hindi"),
                        santaliTranslation = instObj.getString("santali"),
                        phoneticDevanagari = OlChikiTransliterator.toTeacherPhoneticHUD(instObj.getString("santali")),
                        audioAssetPath = "content_pack/lessons/$lessonId/audio/instruction.wav"
                    )

                    val activity = Activity(
                        id = "act_$lessonId",
                        learningOutcomeId = outcome.id,
                        titleHindi = actObj.getString("instruction_hindi"),
                        titleSantali = actObj.getString("instruction_santali"),
                        type = ActivityType.valueOf(actObj.getString("type").uppercase()),
                        descriptionHindi = actObj.getString("instruction_hindi"),
                        descriptionSantali = actObj.getString("instruction_santali"),
                        interactiveItems = jsonArrayToList(actObj.optJSONArray("interactive_items")),
                        difficulty = DifficultyLevel.BEGINNER
                    )

                    val optionsHindi = jsonArrayToList(assessObj.getJSONArray("options_hindi"))
                    val optionsSantali = jsonArrayToList(assessObj.getJSONArray("options_santali"))

                    val assessment = AssessmentQuestion(
                        id = "q_$lessonId",
                        learningOutcomeId = outcome.id,
                        questionHindi = assessObj.getString("question_hindi"),
                        questionSantali = assessObj.getString("question_santali"),
                        questionPhonetic = OlChikiTransliterator.toTeacherPhoneticHUD(assessObj.getString("question_santali")),
                        optionsHindi = optionsHindi,
                        optionsSantali = optionsSantali,
                        correctOptionIndex = assessObj.getInt("correct_answer_index"),
                        explanationHindi = assessObj.optString("explanation_hindi", "Correct!"),
                        difficulty = DifficultyLevel.BEGINNER
                    )

                    val domainStr = json.optString("domain", "NUMERACY")
                    val domain = try { FLNDomain.valueOf(domainStr) } catch (e: Exception) { FLNDomain.NUMERACY }

                    val lesson = Lesson(
                        id = lessonId,
                        grade = "Grade ${json.optInt("grade", 1)}",
                        domain = domain,
                        topic = json.optString("subject", "Mathematics"),
                        titleHindi = titleObj.getString("hindi"),
                        titleSantali = titleObj.getString("santali"),
                        titlePhonetic = OlChikiTransliterator.toTeacherPhoneticHUD(titleObj.getString("santali")),
                        learningOutcome = outcome,
                        instructions = listOf(instruction),
                        activities = listOf(activity),
                        assessmentQuestions = listOf(assessment)
                    )

                    loadedLessons.add(lesson)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse lesson $lessonId: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load content pack manifest: ${e.message}")
        }

        if (loadedLessons.isNotEmpty()) {
            cachedLessons = loadedLessons
        }
        return loadedLessons.ifEmpty { FLNCurriculumRepository.lessons }
    }

    fun playPreRenderedAudio(context: Context, assetWavPath: String, onComplete: () -> Unit = {}) {
        try {
            val afd = context.assets.openFd(assetWavPath)
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                prepare()
                setOnCompletionListener {
                    it.release()
                    onComplete()
                }
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play pre-rendered audio $assetWavPath: ${e.message}")
            onComplete()
        }
    }

    private fun jsonArrayToList(array: org.json.JSONArray?): List<String> {
        if (array == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            list.add(array.getString(i))
        }
        return list
    }
}
