package org.tribetalk.curriculum.ai

import org.tribetalk.curriculum.context.TeachingContext
import org.tribetalk.curriculum.ontology.LearningObjective
import org.tribetalk.curriculum.spec.*
import org.tribetalk.fln.pipeline.SvgCorpusRegistry
import java.util.Random

/**
 * Deterministic, zero-RAM curriculum activity generator (< 5ms).
 * Provides mathematically sound, curriculum-guaranteed activities when
 * AI inference is offline, throttled for P0 speech, or fails validation.
 */
object DeterministicActivityPlanner {

    private val CORE_ASSETS = listOf("apple", "mango", "fish", "flower", "ball", "child", "tree")

    fun plan(
        context: TeachingContext,
        objective: LearningObjective,
        seed: Long
    ): ActivitySpec {
        val random = Random(seed)

        // 1. Resolve Activity Type
        val allowedActs = objective.allowedActivityTypes.ifEmpty { listOf(ActivityType.COUNT) }
        val actType = allowedActs[random.nextInt(allowedActs.size)]

        // 2. Resolve Asset avoiding recent
        val availableAssets = CORE_ASSETS.filter { !context.recentActivityIds.contains(it) }.ifEmpty { CORE_ASSETS }
        val assetKey = availableAssets[random.nextInt(availableAssets.size)]

        val meta = SvgCorpusRegistry.get(assetKey)
        val hiName = meta?.hindiName ?: "वस्तु"
        val satName = meta?.santaliName ?: "ᱡᱤᱱᱤᱥ"
        val engName = meta?.englishName ?: "Object"

        // 3. Resolve Quantities, Correct Answer & Distractors based on activity type
        val minQ = objective.numberRange.first
        val maxQ = objective.numberRange.last
        var quantity = minQ + random.nextInt(maxQ - minQ + 1)
        var secQuantity = 0
        var correctAns = quantity.toString()
        var allOptions: List<String>
        var distractors: List<String>

        when (actType) {
            ActivityType.COMPARE_QUANTITIES -> {
                secQuantity = minQ + random.nextInt(maxQ - minQ + 1)
                correctAns = when {
                    quantity < secQuantity -> "<"
                    quantity > secQuantity -> ">"
                    else -> "="
                }
                allOptions = listOf("<", "=", ">")
                distractors = allOptions.filter { it != correctAns }
            }

            ActivityType.SIMPLE_ADDITION -> {
                secQuantity = minQ + random.nextInt(maxQ - minQ + 1)
                val sum = quantity + secQuantity
                correctAns = sum.toString()
                distractors = listOf(
                    (sum - 1).coerceAtLeast(1),
                    sum + 1,
                    (sum - 2).coerceAtLeast(1),
                    sum + 2
                ).map { it.toString() }.distinct().filter { it != correctAns }.take(3)
                allOptions = (distractors + correctAns).distinct().shuffled(random)
            }

            else -> {
                val candidates = listOf(
                    (quantity - 1).coerceAtLeast(minQ),
                    (quantity + 1).coerceAtMost(maxQ),
                    (quantity - 2).coerceAtLeast(minQ),
                    (quantity + 2).coerceAtMost(maxQ)
                ).map { it.toString() }.distinct().filter { it != correctAns }
                distractors = candidates.take(3)
                allOptions = (distractors + correctAns).distinct().shuffled(random)
            }
        }

        val item = ActivityItemSpec(
            id = "item_${seed}_1",
            visualAsset = assetKey,
            quantity = quantity,
            secondaryQuantity = secQuantity,
            options = allOptions,
            correctAnswer = correctAns,
            explanationHindi = "$quantity $hiName",
            explanationSantali = "$quantity $satName"
        )

        val theme = when (assetKey) {
            "apple", "mango", "flower", "tree" -> VisualTheme.GARDEN
            "fish" -> VisualTheme.NATURE
            "ball", "child" -> VisualTheme.CLASSROOM
            else -> VisualTheme.VILLAGE
        }

        return ActivitySpec(
            id = "det_${objective.id}_${seed}",
            objectiveId = objective.id,
            activityType = actType,
            difficulty = context.difficulty,
            items = listOf(item),
            visualSpec = VisualSpec(
                theme = theme,
                layout = VisualLayout.GRID,
                primaryAssetKey = assetKey
            ),
            languageSpec = LanguageSpec(
                primaryWord = hiName,
                targetWord = satName,
                englishWord = engName,
                instructionHindi = "$hiName गिनें और सही संख्या चुनें:",
                instructionSantali = "$satName ᱞᱮᱠᱷᱟᱭ ᱢᱮ ᱟᱨ ᱥᱟᱹᱦᱤ ᱮᱞ ᱵᱟᱪᱷᱟᱣ ᱢᱮ:",
                phoneticGuide = "$engName ($satName)",
                phraseHindi = "सुंदर $hiName",
                phraseSantali = "ᱢᱚᱡᱽ $satName",
                phraseEnglish = "Nice $engName"
            ),
            answerSpec = AnswerSpec(
                correctValue = correctAns,
                numericValue = correctAns.toIntOrNull() ?: quantity,
                distractors = distractors,
                teacherNote = "Answer: $correctAns ($hiName / $satName)"
            ),
            metadata = ActivityMetadata(
                worksheetId = "ws_${objective.id}_${seed}",
                activityId = "act_${objective.id}_${seed}",
                generationSeed = seed,
                modelVersion = "Deterministic-v1",
                curriculumVersion = "NIPUN-2026.1"
            )
        )
    }
}
