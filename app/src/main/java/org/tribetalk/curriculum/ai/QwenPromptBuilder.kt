package org.tribetalk.curriculum.ai

import org.tribetalk.curriculum.context.TeachingContext
import org.tribetalk.curriculum.ontology.LearningObjective
import org.tribetalk.fln.model.FlnGrade

/**
 * Builds goal-conditioned, schema-constrained prompts for Qwen 2.5 0.5B.
 * Utilizes 1-shot in-context exemplars in native ChatML token format.
 * Strictly guides the model to emit clean, parseable JSON without conversational filler.
 */
object QwenPromptBuilder {

    private const val ALLOWED_ASSETS = "apple, mango, banana, fish, bird, flower, tree, peacock, elephant, cow"

    fun buildPrompt(
        context: TeachingContext,
        objective: LearningObjective,
        seed: Long
    ): String {
        val allowedActs = objective.allowedActivityTypes.joinToString(", ") { it.name }
        val numRange = "${objective.numberRange.first} to ${objective.numberRange.last}"
        val recentHistory = if (context.recentActivityIds.isEmpty()) "None" else context.recentActivityIds.joinToString(", ")

        return """<|im_start|>system
You are a curriculum activity planner for Jharkhand primary schools (NIPUN Bharat MTB-MLE).
Output ONLY a single valid JSON object. No other text, no markdown backticks, no explanations.
<|im_end|>
<|im_start|>user
Grade: GRADE_1
Objective: NIPUN_G1_NUM_COUNT_1_10 - Count objects up to 10 (1 से 10 तक गिनना)
Allowed Activity Types: [COUNT, COUNT_AND_MATCH]
Number Range: 1 to 10
Visual Assets: [apple, mango, fish, flower]
Seed: 101
<|im_end|>
<|im_start|>assistant
{"activityType": "COUNT", "difficulty": 1, "quantity": 7, "visualAsset": "mango", "visualTheme": "GARDEN", "visualLayout": "GRID", "correctAnswer": "7", "distractorOptions": ["5", "6", "7", "8"]}
<|im_end|>
<|im_start|>user
Grade: ${context.grade.name} (${context.grade.displayName})
Subject Domain: ${context.domain.name}
Objective: ${objective.id} - ${objective.titleEnglish} (${objective.titleHindi})
Allowed Activity Types: [$allowedActs]
Number Range: $numRange
Visual Assets: [$ALLOWED_ASSETS]
Difficulty: ${context.difficulty.name}
Recent Activities to Avoid: [$recentHistory]
Seed: $seed
Generate the curriculum activity JSON:
<|im_end|>
<|im_start|>assistant
"""
    }

    fun buildTopicPrompt(
        topic: String,
        grade: FlnGrade,
        seed: Long
    ): String {
        return """<|im_start|>system
You are a curriculum activity planner for Jharkhand primary schools (NIPUN Bharat MTB-MLE).
Output ONLY a single valid JSON object. No other text, no markdown backticks, no explanations.
<|im_end|>
<|im_start|>user
Topic: हाट बाज़ार में फल
Grade: GRADE_1
Seed: 101
<|im_end|>
<|im_start|>assistant
{"activityType":"COUNT_AND_MATCH","difficulty":1,"quantity":5,"visualAsset":"mango","visualTheme":"VILLAGE","visualLayout":"GRID","correctAnswer":"5","distractorOptions":["3","4","5","6"]}
<|im_end|>
<|im_start|>user
Topic: $topic
Grade: ${grade.name} (${grade.displayName})
Allowed Visual Assets: [$ALLOWED_ASSETS]
Seed: $seed
Generate the curriculum activity JSON:
<|im_end|>
<|im_start|>assistant
"""
    }
}

