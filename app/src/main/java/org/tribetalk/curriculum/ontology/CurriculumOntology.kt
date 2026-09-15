package org.tribetalk.curriculum.ontology

import org.tribetalk.curriculum.spec.ActivityType
import org.tribetalk.fln.model.FlnDomain
import org.tribetalk.fln.model.FlnGrade

/**
 * Structured Learning Objective under NIPUN Bharat MTB-MLE Guidelines.
 */
data class LearningObjective(
    val id: String,
    val grade: FlnGrade,
    val domain: FlnDomain,
    val unit: String,
    val skill: String,
    val titleEnglish: String,
    val titleHindi: String,
    val titleSantali: String,
    val numberRange: IntRange = 1..10,
    val allowedActivityTypes: List<ActivityType>,
    val allowedVisualCategories: List<String> = listOf("plants", "animals", "nature", "tokens", "school_and_play", "people"),
    val prerequisites: List<String> = emptyList(),
    val assessmentTypes: List<ActivityType> = emptyList()
) {
    val title: String get() = titleEnglish
}

/**
 * In-memory authoritative registry of NIPUN Bharat learning objectives for TribeTalk.
 */
object CurriculumRegistry {

    private val objectives = LinkedHashMap<String, LearningObjective>()

    init {
        registerBuiltinObjectives()
    }

    private fun registerBuiltinObjectives() {
        // Vertical Slice: Grade 1 Numeracy: Numbers 1-10 Counting Objects
        register(
            LearningObjective(
                id = "NIPUN_G1_NUM_COUNT_1_10",
                grade = FlnGrade.GRADE_1,
                domain = FlnDomain.NUMERACY_COUNTING,
                unit = "Numbers & Counting",
                skill = "COUNTING",
                titleEnglish = "Counting Objects 1 to 10",
                titleHindi = "1 से 10 तक वस्तुओं की गिनती",
                titleSantali = "᱑ ᱠᱷᱚᱱ ᱑᱐ ᱡᱤᱱᱤᱥ ᱞᱮᱠᱷᱟ",
                numberRange = 1..10,
                allowedActivityTypes = listOf(
                    ActivityType.COUNT,
                    ActivityType.COUNT_AND_MATCH,
                    ActivityType.COMPARE_QUANTITIES,
                    ActivityType.CIRCLE_CORRECT
                ),
                allowedVisualCategories = listOf("plants", "animals", "nature", "tokens", "school_and_play", "people"),
                prerequisites = listOf("NIPUN_BAL_NUM_COUNT_1_5"),
                assessmentTypes = listOf(ActivityType.COUNT, ActivityType.CIRCLE_CORRECT)
            )
        )

        // Prerequisite: Balvatika 1-5 Counting
        register(
            LearningObjective(
                id = "NIPUN_BAL_NUM_COUNT_1_5",
                grade = FlnGrade.BALVATIKA,
                domain = FlnDomain.NUMERACY_COUNTING,
                unit = "Early Number Sense",
                skill = "COUNTING",
                titleEnglish = "Counting 1 to 5 Concrete Objects",
                titleHindi = "1 से 5 तक मूर्त वस्तुओं की गिनती",
                titleSantali = "᱑ ᱠᱷᱚᱱ ᱕ ᱡᱤᱱᱤᱥ ᱞᱮᱠᱷᱟ",
                numberRange = 1..5,
                allowedActivityTypes = listOf(ActivityType.COUNT, ActivityType.CIRCLE_CORRECT),
                allowedVisualCategories = listOf("plants", "animals", "tokens")
            )
        )

        // Grade 1 Literacy: Picture-Word Association
        register(
            LearningObjective(
                id = "NIPUN_G1_LIT_WORD_ASSOC",
                grade = FlnGrade.GRADE_1,
                domain = FlnDomain.LITERACY_VOCABULARY,
                unit = "Living Vocabulary",
                skill = "VOCABULARY",
                titleEnglish = "Picture & Word Association",
                titleHindi = "चित्र और शब्द का सम्बंध",
                titleSantali = "ᱪᱤᱛᱟᱹᱨ ᱟᱨ ᱟᱹᱲᱟᱹ ᱡᱚᱲ",
                numberRange = 1..1,
                allowedActivityTypes = listOf(ActivityType.MATCH, ActivityType.VOCABULARY_RECOGNITION),
                allowedVisualCategories = listOf("plants", "animals", "nature", "people")
            )
        )
    }

    fun register(obj: LearningObjective) {
        objectives[obj.id] = obj
    }

    fun getObjective(id: String): LearningObjective? = objectives[id]

    fun getObjectivesForGrade(grade: FlnGrade): List<LearningObjective> {
        return objectives.values.filter { it.grade == grade }
    }

    fun getAllObjectives(): List<LearningObjective> = objectives.values.toList()

    fun getDefaultObjective(): LearningObjective {
        return objectives["NIPUN_G1_NUM_COUNT_1_10"] ?: objectives.values.first()
    }
}
