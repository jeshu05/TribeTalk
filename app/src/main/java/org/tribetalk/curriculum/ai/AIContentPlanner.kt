package org.tribetalk.curriculum.ai

import org.tribetalk.curriculum.context.TeachingContext
import org.tribetalk.curriculum.ontology.LearningObjective
import org.tribetalk.curriculum.spec.ActivitySpec

/**
 * Result bundle of an activity planning operation.
 */
data class ActivityPlanResult(
    val spec: ActivitySpec,
    val source: String, // "QWEN_0.5B" or "DETERMINISTIC_FALLBACK"
    val generationMs: Long,
    val tokensGenerated: Int = 0,
    val validationPassed: Boolean,
    val issuesFound: List<String> = emptyList()
)

/**
 * High-level abstraction for curriculum activity generation.
 * Decouples pedagogical planning from the underlying language model or fallback engine.
 */
interface AIContentPlanner {
    suspend fun generateActivityPlan(
        context: TeachingContext,
        objective: LearningObjective,
        seed: Long = System.currentTimeMillis()
    ): ActivityPlanResult

    suspend fun generateActivityPlanFromTopic(
        topic: String,
        context: TeachingContext,
        seed: Long = System.currentTimeMillis()
    ): ActivityPlanResult {
        val defaultObjective = org.tribetalk.curriculum.ontology.CurriculumRegistry.getObjective("NIPUN_G1_NUM_COUNT_1_10")
            ?: org.tribetalk.curriculum.ontology.CurriculumRegistry.getDefaultObjective()
        return generateActivityPlan(context, defaultObjective, seed)
    }
}

