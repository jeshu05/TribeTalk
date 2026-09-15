package org.tribetalk.curriculum.cache

import android.util.Log
import kotlinx.coroutines.*
import org.tribetalk.core.orchestration.ResourceOrchestrator
import org.tribetalk.core.orchestration.TaskPriority
import org.tribetalk.curriculum.ai.AIContentPlanner
import org.tribetalk.curriculum.context.TeachingContext
import org.tribetalk.curriculum.ontology.LearningObjective
import org.tribetalk.curriculum.render.FlashcardSetGenerator
import org.tribetalk.fln.model.WorksheetConfig
import org.tribetalk.fln.worksheet.WorksheetGenerator

/**
 * Background prefetching worker for TribeTalk.
 * Runs at P2 priority when lesson context changes and immediately yields to classroom speech.
 */
class CurriculumPrefetcher(
    private val planner: AIContentPlanner
) {
    companion object {
        private const val TAG = "CurriculumPrefetcher"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var prefetchJob: Job? = null

    fun prefetchForLesson(
        context: TeachingContext,
        objective: LearningObjective,
        seed: Long = System.currentTimeMillis()
    ) {
        prefetchJob?.cancel()

        prefetchJob = scope.launch {
            if (!ResourceOrchestrator.canExecuteP2()) {
                Log.i(TAG, "Prefetch skipped: P0 task active or low memory.")
                return@launch
            }

            val cacheKey = CurriculumCacheManager.buildCacheKey(context, objective, seed)
            if (CurriculumCacheManager.getCachedActivity(cacheKey) != null) {
                Log.i(TAG, "Content already cached for ${objective.id}")
                return@launch
            }

            try {
                Log.i(TAG, "Starting background prefetch for lesson: ${objective.id}")
                val planResult = planner.generateActivityPlan(context, objective, seed)

                if (ResourceOrchestrator.isPreemptionRequested()) {
                    Log.i(TAG, "Prefetch yielded to classroom P0 task.")
                    return@launch
                }

                // Cache Activity
                CurriculumCacheManager.putActivity(cacheKey, planResult.spec)

                // Pre-generate and cache Flashcards
                val cards = FlashcardSetGenerator.generateSet(planResult.spec, context)
                CurriculumCacheManager.putFlashcards(cacheKey, cards)

                // Pre-generate and cache Worksheet
                val wsItems = WorksheetGenerator.generateFromActivitySpec(planResult.spec, WorksheetConfig(seed = seed))
                CurriculumCacheManager.putWorksheet(cacheKey, wsItems)

                Log.i(TAG, "Prefetch completed successfully for ${objective.id} (source: ${planResult.source})")
            } catch (e: Exception) {
                if (e is CancellationException) {
                    Log.i(TAG, "Prefetch cancelled.")
                } else {
                    Log.w(TAG, "Prefetch error: ${e.message}")
                }
            }
        }
    }

    fun cancel() {
        prefetchJob?.cancel()
    }
}
