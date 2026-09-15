package org.tribetalk.curriculum

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.tribetalk.curriculum.ai.*
import org.tribetalk.curriculum.cache.CurriculumCacheManager
import org.tribetalk.curriculum.context.TeachingContext
import org.tribetalk.curriculum.ontology.CurriculumRegistry
import org.tribetalk.curriculum.render.FlashcardSetGenerator
import org.tribetalk.curriculum.spec.ActivityType
import org.tribetalk.fln.model.WorksheetConfig
import org.tribetalk.fln.model.WorksheetDifficulty
import org.tribetalk.fln.worksheet.WorksheetGenerator

class CurriculumPipelineE2ETest {

    private val objective = CurriculumRegistry.getDefaultObjective() // Grade 1 Numeracy: Numbers 1-10
    private val context = TeachingContext(
        objectiveId = objective.id,
        grade = objective.grade,
        domain = objective.domain,
        difficulty = WorksheetDifficulty.EASY
    )

    @Before
    fun setUp() {
        CurriculumCacheManager.clearAll()
    }

    @Test
    fun testCompleteGenerativeCurriculumPipelineWithRealSimulation() = runBlocking {
        // 1. Setup Mock Qwen Engine simulating autoregressive JSON generation
        val mockQwenModel = object : LocalLanguageModel {
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
            override suspend fun load(): ModelLoadResult = ModelLoadResult(true, "Qwen", 150L, 480.0f)
            override suspend fun generate(request: GenerationRequest): GenerationResult {
                val structuredJson = """{
                    "activityType": "COUNT",
                    "difficulty": 1,
                    "quantity": 8,
                    "visualAsset": "flower",
                    "visualTheme": "GARDEN",
                    "visualLayout": "GRID",
                    "correctAnswer": "8",
                    "distractorOptions": ["6", "7", "8", "9"]
                }"""
                return GenerationResult(
                    text = structuredJson,
                    promptTokens = 138,
                    generatedTokens = 45,
                    firstTokenLatencyMs = 80L,
                    totalLatencyMs = 220L,
                    tokensPerSecond = 14.8f,
                    peakMemoryMb = 480.0f,
                    success = true
                )
            }
            override fun cancelGeneration() {}
            override suspend fun unload() {}
        }

        val planner = QwenContentPlanner(mockQwenModel)
        val seed = 42L

        // 2. Execute Planner Step
        val planResult = planner.generateActivityPlan(context, objective, seed)
        assertEquals("QWEN_0.5B", planResult.source)
        val spec = planResult.spec

        // 3. Verify Deterministic Validation & Repair Pipeline Passed
        assertEquals(objective.id, spec.objectiveId)
        assertEquals(ActivityType.COUNT, spec.activityType)
        assertEquals(1, spec.items.size)
        assertEquals(8, spec.items[0].quantity)
        assertEquals("8", spec.items[0].correctAnswer)
        assertEquals("flower", spec.items[0].visualAsset)
        assertFalse("Hindi instruction must be populated", spec.languageSpec.instructionHindi.isBlank())
        assertFalse("Santali instruction must be populated", spec.languageSpec.instructionSantali.isBlank())

        // 4. Generate 6-Card Pedagogical Flashcard Set
        val flashcards = FlashcardSetGenerator.generateSet(spec, context)
        assertEquals("Must generate exactly 6 cards for the lesson progression", 6, flashcards.size)

        val card1 = flashcards[0]
        assertFalse("Card 1 must have Santali Ol Chiki", card1.santaliOlChiki.isBlank())
        assertFalse("Card 1 must have Hindi text", card1.hindiText.isBlank())
        assertFalse("Card 1 must have phonetic guide", card1.teacherPhoneticGuide.isBlank())

        val card2 = flashcards[1]
        assertEquals("Card 2 must represent the target numeral count", 8, card2.numeralValue)
        assertEquals("Card 2 counting quantity must match 8", 8, card2.countingQuantity)

        val card5 = flashcards[4]
        assertEquals("Card 5 numeral value must match 8", 8, card5.numeralValue)

        // 5. Generate Curriculum Worksheets
        val wsConfig = WorksheetConfig(grade = context.grade, difficulty = context.difficulty)
        val wsItems = WorksheetGenerator.generateFromActivitySpec(spec, wsConfig)
        assertTrue("Worksheet items must be generated", wsItems.isNotEmpty())
        assertEquals(8, wsItems[0].quantity)
        assertEquals(8, wsItems[0].mathAnswer)
        assertEquals("8", wsItems[0].options[wsItems[0].correctIndex])
        assertTrue(wsItems[0].options.contains("8"))

        // 6. Test Multi-tier Caching Integration
        val cacheKey = CurriculumCacheManager.buildCacheKey(context, objective, seed)
        CurriculumCacheManager.putActivity(cacheKey, spec)
        CurriculumCacheManager.putFlashcards(cacheKey, flashcards)
        CurriculumCacheManager.putWorksheet(cacheKey, wsItems)

        val cachedSpec = CurriculumCacheManager.getCachedActivity(cacheKey)
        assertNotNull("Activity must be retrieved from cache", cachedSpec)
        assertEquals(spec.id, cachedSpec?.id)

        val cachedCards = CurriculumCacheManager.getCachedFlashcards(cacheKey)
        assertNotNull("Flashcards must be retrieved from cache", cachedCards)
        assertEquals(6, cachedCards?.size)

        val cachedWs = CurriculumCacheManager.getCachedWorksheet(cacheKey)
        assertNotNull("Worksheet must be retrieved from cache", cachedWs)
        assertEquals(wsItems.size, cachedWs?.size)
    }

    @Test
    fun testOfflineFallbackPipelineWhenSpeechPreemptsSlm() = runBlocking {
        // Simulate SLM being unavailable or preempted by real-time speech ASR/TTS
        val preemptedModel = object : LocalLanguageModel {
            override fun getModelInfo(): ModelInfo = ModelInfo(
                modelName = "Qwen-2.5-0.5B-Instruct",
                runtime = "ONNX",
                format = "INT8",
                quantization = "INT8",
                parameterCount = "0.5B",
                modelSizeBytes = 512000000L,
                isLoaded = false,
                state = "PREEMPTED"
            )
            override fun isLoaded(): Boolean = false
            override suspend fun load(): ModelLoadResult = ModelLoadResult(false, "Qwen", 0L, 0.0f, "Preempted")
            override suspend fun generate(request: GenerationRequest): GenerationResult = GenerationResult(
                text = "",
                promptTokens = 0,
                generatedTokens = 0,
                firstTokenLatencyMs = 0L,
                totalLatencyMs = 0L,
                tokensPerSecond = 0.0f,
                peakMemoryMb = 0.0f,
                success = false,
                errorMessage = "Preempted by P0"
            )
            override fun cancelGeneration() {}
            override suspend fun unload() {}
        }

        val planner = QwenContentPlanner(preemptedModel)
        val result = planner.generateActivityPlan(context, objective, 999L)

        // Verifies fallback triggered and produces valid artifacts
        assertEquals("DETERMINISTIC_FALLBACK", result.source)
        assertNotNull(result.spec)
        assertTrue("Fallback spec must be valid", result.validationPassed)

        val cards = FlashcardSetGenerator.generateSet(result.spec, context)
        assertEquals(6, cards.size)

        val wsConfig = WorksheetConfig(grade = context.grade, difficulty = context.difficulty)
        val wsItems = WorksheetGenerator.generateFromActivitySpec(result.spec, wsConfig)
        assertTrue(wsItems.isNotEmpty())
    }
}
