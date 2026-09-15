package org.tribetalk.curriculum.ai

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.tribetalk.core.orchestration.OrchestratorModelState
import org.tribetalk.core.orchestration.ResourceOrchestrator
import org.tribetalk.core.orchestration.TaskPriority

class ResourceOrchestratorTest {

    private class MockSlm : LocalLanguageModel {
        var cancelCalled = false
        var unloadCalled = false

        override fun getModelInfo(): ModelInfo = ModelInfo(
            modelName = "MockQwen",
            runtime = "ONNX",
            format = "INT8",
            quantization = "INT8",
            parameterCount = "0.5B",
            modelSizeBytes = 512000000L,
            isLoaded = true,
            state = "READY"
        )
        override fun isLoaded(): Boolean = true
        override suspend fun load(): ModelLoadResult = ModelLoadResult(
            success = true,
            modelName = "MockQwen",
            loadTimeMs = 50L,
            memoryUsageMb = 256.0f
        )
        override suspend fun generate(request: GenerationRequest): GenerationResult = GenerationResult(
            text = "{}",
            promptTokens = 10,
            generatedTokens = 5,
            firstTokenLatencyMs = 20L,
            totalLatencyMs = 50L,
            tokensPerSecond = 100.0f,
            peakMemoryMb = 280.0f,
            success = true
        )
        override fun cancelGeneration() {
            cancelCalled = true
        }
        override suspend fun unload() {
            unloadCalled = true
        }
    }

    private lateinit var mockSlm: MockSlm

    @Before
    fun setUp() {
        mockSlm = MockSlm()
        ResourceOrchestrator.registerSlm(mockSlm)
    }

    @Test
    fun testP0PreemptsP2TasksAndCancelsSlm() {
        assertFalse("Preemption should not be requested initially", ResourceOrchestrator.isPreemptionRequested())

        // Start P0 Classroom Real-time task (e.g. Teacher presses Push-to-Talk)
        ResourceOrchestrator.notifyTaskStarted(TaskPriority.P0_CLASSROOM_REALTIME)

        assertTrue("Preemption flag must be raised when P0 task starts", ResourceOrchestrator.isPreemptionRequested())
        assertFalse("P2 tasks must be forbidden when P0 is active", ResourceOrchestrator.canExecuteP2())
        assertTrue("SLM cancelGeneration() must have been called", mockSlm.cancelCalled)

        // Complete P0 task
        ResourceOrchestrator.notifyTaskCompleted(TaskPriority.P0_CLASSROOM_REALTIME)

        assertFalse("Preemption flag must be cleared when P0 completes", ResourceOrchestrator.isPreemptionRequested())
    }

    @Test
    fun testNestedP0TasksKeepPreemptionActiveUntilAllComplete() {
        ResourceOrchestrator.notifyTaskStarted(TaskPriority.P0_CLASSROOM_REALTIME) // e.g. ASR
        ResourceOrchestrator.notifyTaskStarted(TaskPriority.P0_CLASSROOM_REALTIME) // e.g. TTS overlap

        assertTrue(ResourceOrchestrator.isPreemptionRequested())
        assertFalse(ResourceOrchestrator.canExecuteP2())

        ResourceOrchestrator.notifyTaskCompleted(TaskPriority.P0_CLASSROOM_REALTIME) // ASR ends
        assertTrue("Preemption should remain active while second P0 task is running", ResourceOrchestrator.isPreemptionRequested())

        ResourceOrchestrator.notifyTaskCompleted(TaskPriority.P0_CLASSROOM_REALTIME) // TTS ends
        assertFalse("Preemption cleared once all P0 tasks end", ResourceOrchestrator.isPreemptionRequested())
    }

    @Test
    fun testModelStateTransitions() {
        ResourceOrchestrator.updateModelState(OrchestratorModelState.COLD)
        assertEquals(OrchestratorModelState.COLD, ResourceOrchestrator.getModelState())

        ResourceOrchestrator.updateModelState(OrchestratorModelState.LOADING)
        assertEquals(OrchestratorModelState.LOADING, ResourceOrchestrator.getModelState())

        ResourceOrchestrator.updateModelState(OrchestratorModelState.WARM)
        assertEquals(OrchestratorModelState.WARM, ResourceOrchestrator.getModelState())

        ResourceOrchestrator.updateModelState(OrchestratorModelState.GENERATING)
        assertEquals(OrchestratorModelState.GENERATING, ResourceOrchestrator.getModelState())

        ResourceOrchestrator.updateModelState(OrchestratorModelState.IDLE)
        assertEquals(OrchestratorModelState.IDLE, ResourceOrchestrator.getModelState())

        ResourceOrchestrator.updateModelState(OrchestratorModelState.UNLOADED)
        assertEquals(OrchestratorModelState.UNLOADED, ResourceOrchestrator.getModelState())
    }
}
