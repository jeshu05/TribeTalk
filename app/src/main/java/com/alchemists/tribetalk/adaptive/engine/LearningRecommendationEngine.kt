package com.alchemists.tribetalk.adaptive.engine

import com.alchemists.tribetalk.adaptive.models.ConceptProgress
import com.alchemists.tribetalk.adaptive.models.LearningRecommendation
import com.alchemists.tribetalk.assessment.models.MasteryLevel
import com.alchemists.tribetalk.curriculum.repository.FLNCurriculumRepository

object LearningRecommendationEngine {

    fun generateRecommendation(progress: ConceptProgress): LearningRecommendation {
        val concept = progress.concept
        val accInt = progress.latestAccuracy.toInt()
        val lesson = FLNCurriculumRepository.lessons.find { it.learningOutcome.id == progress.learningOutcomeId }
            ?: FLNCurriculumRepository.lessons.first()

        return when (progress.masteryLevel) {
            MasteryLevel.NEEDS_PRACTICE -> {
                LearningRecommendation(
                    recommendationId = "rec_${progress.learningOutcomeId}_np",
                    learningOutcomeId = progress.learningOutcomeId,
                    concept = concept,
                    lessonId = lesson.id,
                    reasonHindi = "संकल्पना '$concept' में सटीकता $accInt% है (अभ्यास आवश्यक)। $concept पर पुनराभ्यास की सलाह दी जाती है।",
                    reasonSantali = "'$concept' ᱨᱮ ᱟᱨᱦᱚᱸ ᱚᱞ-ᱯᱟᱲᱦᱟᱣ ᱞᱟᱹᱠᱛᱤ ᱢᱮᱱᱟᱜᱼᱟ (ᱥᱟᱹᱨᱤ $accInt%)᱾",
                    priority = "HIGH",
                    targetMastery = MasteryLevel.NEEDS_PRACTICE
                )
            }
            MasteryLevel.DEVELOPING -> {
                LearningRecommendation(
                    recommendationId = "rec_${progress.learningOutcomeId}_dev",
                    learningOutcomeId = progress.learningOutcomeId,
                    concept = concept,
                    lessonId = lesson.id,
                    reasonHindi = "संकल्पना '$concept' में सटीकता $accInt% है। सुदृढ़ीकरण अभ्यास की सलाह दी जाती है।",
                    reasonSantali = "'$concept' ᱨᱮ ᱟᱨᱦᱚᱸ ᱢᱤᱫ ᱫᱷᱟᱣ ᱪᱮᱫᱚᱜ ᱯᱮ (ᱥᱟᱹᱨᱤ $accInt%)᱾",
                    priority = "MEDIUM",
                    targetMastery = MasteryLevel.DEVELOPING
                )
            }
            MasteryLevel.MASTERED -> {
                // Recommend next lesson
                val nextLesson = FLNCurriculumRepository.lessons.find { it.id != lesson.id } ?: lesson
                LearningRecommendation(
                    recommendationId = "rec_${progress.learningOutcomeId}_mas",
                    learningOutcomeId = nextLesson.learningOutcome.id,
                    concept = nextLesson.topic,
                    lessonId = nextLesson.id,
                    reasonHindi = "संकल्पना '$concept' सिद्ध हो चुकी है ($accInt%)! अगली गतिविधि '${nextLesson.topic}' शुरू करें।",
                    reasonSantali = "'$concept' ᱱᱟᱯᱟᱭ ᱥᱮᱪᱮᱫ ᱮᱱᱟ ($accInt%)! ᱤᱱᱟᱹ ᱛᱟᱭᱚᱢ ᱥᱮᱪᱮᱫ ᱨᱮ ᱪᱟᱞᱟᱜ ᱯᱮ᱾",
                    priority = "LOW",
                    targetMastery = MasteryLevel.MASTERED
                )
            }
        }
    }

    fun selectPrimaryRecommendation(allProgress: List<ConceptProgress>): LearningRecommendation {
        if (allProgress.isEmpty()) {
            val defaultLesson = FLNCurriculumRepository.lessons.first()
            return LearningRecommendation(
                recommendationId = "rec_default",
                learningOutcomeId = defaultLesson.learningOutcome.id,
                concept = defaultLesson.topic,
                lessonId = defaultLesson.id,
                reasonHindi = "गिनती 1 से 10 तक गतिविधि प्रारंभ करें।",
                reasonSantali = "1 ᱠᱷᱚᱱ 10 ᱦᱟᱹᱵᱤᱡ ᱞᱮᱠᱷᱟ ᱥᱮᱪᱮᱫ ᱮᱛᱚᱦᱚᱵ ᱯᱮ᱾",
                priority = "HIGH",
                targetMastery = MasteryLevel.NEEDS_PRACTICE
            )
        }

        // Priority order: NEEDS_PRACTICE -> DEVELOPING -> MASTERED
        val sorted = allProgress.sortedWith(compareBy({
            when (it.masteryLevel) {
                MasteryLevel.NEEDS_PRACTICE -> 0
                MasteryLevel.DEVELOPING -> 1
                MasteryLevel.MASTERED -> 2
            }
        }, { it.latestAccuracy }))

        return generateRecommendation(sorted.first())
    }
}
