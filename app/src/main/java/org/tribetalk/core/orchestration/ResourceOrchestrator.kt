package org.tribetalk.core.orchestration

import android.util.Log
import kotlinx.coroutines.*
import org.tribetalk.curriculum.ai.LocalLanguageModel
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

enum class TaskPriority(val level: Int) {
    P0_CLASSROOM_REALTIME(0), // ASR, NMT Translation, TTS
    P1_USER_INTERACTION(1),   // UI rendering, Flashcard navigation, Worksheet preview
    P2_BACKGROUND_GENERATION(2) // Qwen 0.5B generation, prefetching
}

enum class OrchestratorModelState {
    COLD,
    LOADING,
    WARM,
    GENERATING,
    IDLE,
    UNLOADED
}

/**
 * Central Resource Orchestrator for TribeTalk.
 * Enforces strict priority-based scheduling and memory budgeting on 2-4 GB RAM tablets.
 */
object ResourceOrchestrator {

    private const val TAG = "ResourceOrchestrator"
    private const val MIN_FREE_MEMORY_MB = 250L // Minimum free RAM required to permit Qwen generation
    private const val IDLE_UNLOAD_TIMEOUT_MS = 30_000L // Unload Qwen after 30s idle

    private val activeP0Count = AtomicInteger(0)
    private val isP2PreemptRequested = AtomicBoolean(false)
    private var modelState = OrchestratorModelState.COLD
    private var registeredSlm: LocalLanguageModel? = null

    private val orchestratorScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var idleTimerJob: Job? = null

    @Synchronized
    fun registerSlm(slm: LocalLanguageModel) {
        registeredSlm = slm
    }

    /**
     * Notifies the orchestrator that a task has started.
     */
    @Synchronized
    fun notifyTaskStarted(priority: TaskPriority) {
        if (priority == TaskPriority.P0_CLASSROOM_REALTIME) {
            val count = activeP0Count.incrementAndGet()
            Log.i(TAG, "P0 task started (active count: $count). Preempting any background P2 tasks.")
            isP2PreemptRequested.set(true)
            cancelIdleTimer()
            registeredSlm?.cancelGeneration()
        }
    }

    /**
     * Notifies the orchestrator that a task has completed.
     */
    @Synchronized
    fun notifyTaskCompleted(priority: TaskPriority) {
        if (priority == TaskPriority.P0_CLASSROOM_REALTIME) {
            val count = activeP0Count.decrementAndGet().coerceAtLeast(0)
            Log.i(TAG, "P0 task finished (remaining P0 active: $count)")
            if (count == 0) {
                isP2PreemptRequested.set(false)
                scheduleIdleTimer()
            }
        }
    }

    /**
     * Checks if background generation can proceed.
     */
    fun canExecuteP2(): Boolean {
        if (activeP0Count.get() > 0 || isP2PreemptRequested.get()) {
            return false
        }
        val freeMemMb = getAvailableMemoryMb()
        if (freeMemMb < MIN_FREE_MEMORY_MB) {
            Log.w(TAG, "Insufficient free memory ($freeMemMb MB < $MIN_FREE_MEMORY_MB MB). Denying P2 workload.")
            return false
        }
        return true
    }

    fun isPreemptionRequested(): Boolean = isP2PreemptRequested.get()

    @Synchronized
    fun updateModelState(newState: OrchestratorModelState) {
        modelState = newState
        Log.i(TAG, "Model state transition -> $newState")
        if (newState == OrchestratorModelState.IDLE) {
            scheduleIdleTimer()
        } else {
            cancelIdleTimer()
        }
    }

    fun getModelState(): OrchestratorModelState = modelState

    private fun scheduleIdleTimer() {
        cancelIdleTimer()
        idleTimerJob = orchestratorScope.launch {
            delay(IDLE_UNLOAD_TIMEOUT_MS)
            if (modelState == OrchestratorModelState.IDLE && activeP0Count.get() == 0) {
                Log.i(TAG, "Idle timeout reached. Evicting Qwen 0.5B session to reclaim memory.")
                registeredSlm?.unload()
                modelState = OrchestratorModelState.UNLOADED
            }
        }
    }

    private fun cancelIdleTimer() {
        idleTimerJob?.cancel()
        idleTimerJob = null
    }

    private fun getAvailableMemoryMb(): Long {
        val runtime = Runtime.getRuntime()
        val maxMem = runtime.maxMemory()
        val totalMem = runtime.totalMemory()
        val freeMem = runtime.freeMemory()
        val allocMem = totalMem - freeMem
        return (maxMem - allocMem) / (1024 * 1024)
    }
}
