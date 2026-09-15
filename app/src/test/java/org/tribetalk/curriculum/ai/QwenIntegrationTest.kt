package org.tribetalk.curriculum.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.tribetalk.curriculum.context.TeachingContext
import org.tribetalk.curriculum.ontology.CurriculumRegistry
import org.tribetalk.curriculum.spec.ActivityType
import org.tribetalk.curriculum.validator.ActivityValidationPipeline
import java.io.File

class QwenIntegrationTest {

    private val objective = CurriculumRegistry.getDefaultObjective()
    private val context = TeachingContext(objectiveId = objective.id)

    @Test
    fun testRealVocabLoadingAndBpeTokenization() {
        val vocabFile = File("models/qwen/vocab.json").let {
            if (it.exists()) it else File("../models/qwen/vocab.json")
        }

        val tokenizer = QwenTokenizer()
        if (vocabFile.exists()) {
            val loaded = tokenizer.loadVocab(vocabFile)
            assertTrue("Vocab must load successfully from actual file", loaded)
        }

        // Test tokenization of Qwen chat template tags
        val samplePrompt = "<|im_start|>system\nYou are a curriculum planner.<|im_end|>\n<|im_start|>user\nPlan counting 1 to 10.<|im_end|>\n<|im_start|>assistant\n"
        val tokenIds = tokenizer.encode(samplePrompt)
        assertTrue("Prompt should produce non-empty token array", tokenIds.isNotEmpty())
        assertEquals("First token should be IM_START", QwenTokenizer.IM_START_TOKEN_ID, tokenIds[0])

        // Verify detokenization reproduces prompt text
        val decoded = tokenizer.decode(tokenIds)
        println("DEBUG DECODED: '$decoded'")
        assertTrue("Decoded text must contain user message", decoded.contains("Plan counting 1 to 10"))
        assertTrue("Decoded text must contain assistant turn", decoded.contains("assistant"))
    }

    @Test
    fun testQwenPromptBuilderStructure() {
        val prompt = QwenPromptBuilder.buildPrompt(context, objective, 42L)
        assertNotNull(prompt)
        assertTrue("Prompt must start with system turn", prompt.startsWith("<|im_start|>system"))
        assertTrue("Prompt must contain objective ID", prompt.contains(objective.id))
        assertTrue("Prompt must contain number range", prompt.contains("1 to 10"))
        assertTrue("Prompt must contain JSON schema specification", prompt.contains("\"activityType\": \"COUNT\""))
        assertTrue("Prompt must end with assistant trigger", prompt.endsWith("<|im_start|>assistant\n"))
    }

    @Test
    fun testDeterministicFallbackPlannerGuaranteesValidSpec() {
        val t0 = System.currentTimeMillis()
        val spec = DeterministicActivityPlanner.plan(context, objective, 777L)
        val elapsed = System.currentTimeMillis() - t0

        assertTrue("Deterministic planner must execute in < 25ms", elapsed < 25)
        assertNotNull(spec)
        assertEquals(objective.id, spec.objectiveId)
        assertTrue("Quantity must be in range 1..10", spec.items[0].quantity in 1..10)
        when (spec.activityType) {
            ActivityType.COMPARE_QUANTITIES -> {
                assertTrue(listOf("<", ">", "=").contains(spec.items[0].correctAnswer))
            }
            else -> {
                assertEquals(spec.items[0].quantity.toString(), spec.items[0].correctAnswer)
            }
        }
        assertTrue(spec.items[0].options.contains(spec.items[0].correctAnswer))

        // Validate that deterministic spec passes validation with 0 issues
        val valResult = ActivityValidationPipeline.validate(spec, objective)
        println("DEBUG DETERMINISTIC ISSUES: ${valResult.allIssues}")
        assertTrue("Deterministic spec must pass validation", valResult.isValid)
        assertTrue("Deterministic spec should have 0 issues", valResult.allIssues.isEmpty())
    }

    @Test
    fun testQwenContentPlannerWithModelResponse() = runBlocking {
        val mockSuccessfulModel = object : LocalLanguageModel {
            override fun getModelInfo(): ModelInfo = ModelInfo(
                modelName = "Qwen-2.5-0.5B-Instruct",
                runtime = "ONNX",
                format = "INT8",
                quantization = "INT8",
                parameterCount = "0.5B",
                modelSizeBytes = 512000000L,
                isLoaded = true,
                state = "READY"
            )
            override fun isLoaded(): Boolean = true
            override suspend fun load(): ModelLoadResult = ModelLoadResult(true, "Qwen", 120L, 480.0f)
            override suspend fun generate(request: GenerationRequest): GenerationResult {
                val qwenOutput = """{
                    "activityType": "COUNT",
                    "difficulty": 1,
                    "quantity": 6,
                    "visualAsset": "mango",
                    "visualTheme": "GARDEN",
                    "visualLayout": "GRID",
                    "correctAnswer": "6",
                    "distractorOptions": ["4", "5", "6", "7"]
                }"""
                return GenerationResult(
                    text = qwenOutput,
                    promptTokens = 150,
                    generatedTokens = 42,
                    firstTokenLatencyMs = 100L,
                    totalLatencyMs = 250L,
                    tokensPerSecond = 14.2f,
                    peakMemoryMb = 480.0f,
                    success = true
                )
            }
            override fun cancelGeneration() {}
            override suspend fun unload() {}
        }

        val planner = QwenContentPlanner(mockSuccessfulModel)
        val result = planner.generateActivityPlan(context, objective, 1234L)

        assertEquals("QWEN_0.5B", result.source)
        assertTrue("Generated tokens must be tracked", result.tokensGenerated > 0)
        assertEquals(ActivityType.COUNT, result.spec.activityType)
        assertEquals("mango", result.spec.items[0].visualAsset)
        assertEquals(6, result.spec.items[0].quantity)
        assertEquals("6", result.spec.items[0].correctAnswer)
    }

    @Test
    fun testQwenContentPlannerTransparentFallbackOnError() = runBlocking {
        val failingModel = object : LocalLanguageModel {
            override fun getModelInfo(): ModelInfo = ModelInfo(
                modelName = "Qwen-2.5-0.5B-Instruct",
                runtime = "ONNX",
                format = "INT8",
                quantization = "INT8",
                parameterCount = "0.5B",
                modelSizeBytes = 512000000L,
                isLoaded = false,
                state = "ERROR"
            )
            override fun isLoaded(): Boolean = false
            override suspend fun load(): ModelLoadResult = ModelLoadResult(false, "Qwen", 0L, 0.0f, "Model file not found")
            override suspend fun generate(request: GenerationRequest): GenerationResult {
                return GenerationResult(
                    text = "",
                    promptTokens = 0,
                    generatedTokens = 0,
                    firstTokenLatencyMs = 0L,
                    totalLatencyMs = 0L,
                    tokensPerSecond = 0.0f,
                    peakMemoryMb = 0.0f,
                    success = false,
                    errorMessage = "Offline/Unloaded"
                )
            }
            override fun cancelGeneration() {}
            override suspend fun unload() {}
        }

        val planner = QwenContentPlanner(failingModel)
        val result = planner.generateActivityPlan(context, objective, 555L)

        // Must gracefully fall back without throwing an exception or returning null
        assertEquals("DETERMINISTIC_FALLBACK", result.source)
        assertNotNull(result.spec)
        assertTrue("Fallback spec must be valid", result.validationPassed)
        assertTrue("Quantity in range", result.spec.items[0].quantity in 1..10)
    }
}
