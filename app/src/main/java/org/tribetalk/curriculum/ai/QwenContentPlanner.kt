package org.tribetalk.curriculum.ai

import android.util.Log
import org.json.JSONObject
import org.tribetalk.curriculum.context.TeachingContext
import org.tribetalk.curriculum.ontology.CurriculumRegistry
import org.tribetalk.curriculum.ontology.LearningObjective
import org.tribetalk.curriculum.spec.ActivitySpec
import org.tribetalk.curriculum.validator.ActivityValidationPipeline
import org.tribetalk.fln.pipeline.ActivitySynthesizer

/**
 * Production curriculum activity planner using local on-device Qwen 0.5B.
 * Never delegates to cloud APIs. Never overrides curriculum constraints.
 * Features resilient JSON repair and bounded adaptive retries.
 */
class QwenContentPlanner(
    private val localModel: LocalLanguageModel
) : AIContentPlanner {

    companion object {
        private const val TAG = "CurriculumPlanner"
        private const val MAX_RETRIES = 1
        private const val MAX_TOKENS = 256
    }

    override suspend fun generateActivityPlan(
        context: TeachingContext,
        objective: LearningObjective,
        seed: Long
    ): ActivityPlanResult {
        val t0 = System.currentTimeMillis()
        val modelInfo = localModel.getModelInfo()
        val prompt = QwenPromptBuilder.buildPrompt(context, objective, seed)

        for (attempt in 0..MAX_RETRIES) {
            val temperature = if (attempt == 0) 0.2f else 0.4f
            val genResult = localModel.generate(
                GenerationRequest(
                    prompt = prompt,
                    maxTokens = MAX_TOKENS,
                    temperature = temperature,
                    seed = seed + attempt
                )
            )

            if (genResult.success && genResult.text.isNotBlank()) {
                val parsedJson = JsonRepairEngine.repairAndParse(genResult.text)
                if (parsedJson != null) {
                    val candidateSpec = ActivitySpec.fromPlannerOutput(parsedJson, objective, seed)
                    val valResult = ActivityValidationPipeline.validate(candidateSpec, objective)

                    val dt = System.currentTimeMillis() - t0
                    Log.i(
                        TAG,
                        "model=Qwen-0.5B runtime=${modelInfo.runtime} activity=${valResult.spec.activityType.name} " +
                                "generation_ms=$dt input_tokens=${genResult.promptTokens} output_tokens=${genResult.generatedTokens} " +
                                "validation=${if (valResult.isValid) "PASS" else "REPAIRED"} fallback=false"
                    )

                    return ActivityPlanResult(
                        spec = valResult.spec,
                        source = "QWEN_0.5B",
                        generationMs = dt,
                        tokensGenerated = genResult.generatedTokens,
                        validationPassed = valResult.isValid,
                        issuesFound = valResult.allIssues
                    )
                }
            }
        }

        // Bounded retries exhausted or model unavailable -> Fallback to trusted template
        val fallbackSpec = DeterministicActivityPlanner.plan(context, objective, seed)
        val valRes = ActivityValidationPipeline.validate(fallbackSpec, objective)
        val dt = System.currentTimeMillis() - t0

        Log.w(
            TAG,
            "model=Qwen-0.5B generation=FAILED reason=\"Model unavailable or invalid output\" fallback=true generation_ms=$dt"
        )

        return ActivityPlanResult(
            spec = valRes.spec,
            source = "DETERMINISTIC_FALLBACK",
            generationMs = dt,
            tokensGenerated = 0,
            validationPassed = true,
            issuesFound = listOf("Used trusted deterministic fallback template.")
        )
    }

    override suspend fun generateActivityPlanFromTopic(
        topic: String,
        context: TeachingContext,
        seed: Long
    ): ActivityPlanResult {
        val t0 = System.currentTimeMillis()
        val modelInfo = localModel.getModelInfo()
        val prompt = QwenPromptBuilder.buildTopicPrompt(topic, context.grade, seed)

        // Select best matching objective for topic
        val objective = findBestObjectiveForTopic(topic)

        for (attempt in 0..MAX_RETRIES) {
            val temperature = if (attempt == 0) 0.2f else 0.4f
            val genResult = localModel.generate(
                GenerationRequest(
                    prompt = prompt,
                    maxTokens = MAX_TOKENS,
                    temperature = temperature,
                    seed = seed + attempt
                )
            )

            if (genResult.success && genResult.text.isNotBlank()) {
                val parsedJson = JsonRepairEngine.repairAndParse(genResult.text)
                if (parsedJson != null) {
                    val candidateSpec = ActivitySpec.fromPlannerOutput(parsedJson, objective, seed)
                    val valResult = ActivityValidationPipeline.validate(candidateSpec, objective)

                    val dt = System.currentTimeMillis() - t0
                    Log.i(
                        TAG,
                        "model=Qwen-0.5B topic=\"$topic\" runtime=${modelInfo.runtime} activity=${valResult.spec.activityType.name} " +
                                "generation_ms=$dt fallback=false"
                    )

                    return ActivityPlanResult(
                        spec = valResult.spec,
                        source = "QWEN_0.5B",
                        generationMs = dt,
                        tokensGenerated = genResult.generatedTokens,
                        validationPassed = valResult.isValid,
                        issuesFound = valResult.allIssues
                    )
                }
            }
        }

        // Procedural topic synthesis fallback
        val ir = ActivitySynthesizer.synthesizeFromTopic(topic, context.grade)
        val candidateSpec = ActivitySpec.fromActivityIR(ir, objective)
        val valRes = ActivityValidationPipeline.validate(candidateSpec, objective)
        val dt = System.currentTimeMillis() - t0

        return ActivityPlanResult(
            spec = valRes.spec,
            source = "SYNTHESIZER_FALLBACK",
            generationMs = dt,
            tokensGenerated = 0,
            validationPassed = true,
            issuesFound = listOf("Used procedural topic synthesizer.")
        )
    }

    private fun findBestObjectiveForTopic(topic: String): LearningObjective {
        val clean = topic.lowercase()
        val defaultObj = CurriculumRegistry.getDefaultObjective()

        return when {
            clean.contains("शब्द") || clean.contains("word") || clean.contains("vocab") || clean.contains("चित्र") ->
                CurriculumRegistry.getObjective("NIPUN_G1_LIT_WORD_ASSOC") ?: defaultObj
            clean.contains("बाल") || clean.contains("balvatika") ->
                CurriculumRegistry.getObjective("NIPUN_BAL_NUM_COUNT_1_5") ?: defaultObj
            else ->
                CurriculumRegistry.getObjective("NIPUN_G1_NUM_COUNT_1_10") ?: defaultObj
        }
    }
}
