package org.tribetalk.curriculum.ai

/**
 * Result of loading an on-device local language model.
 */
data class ModelLoadResult(
    val success: Boolean,
    val modelName: String,
    val loadTimeMs: Long,
    val memoryUsageMb: Float,
    val errorMessage: String? = null
)

/**
 * Request payload for structured autoregressive token generation.
 */
data class GenerationRequest(
    val prompt: String,
    val maxTokens: Int = 128,
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val seed: Long = 42L,
    val stopTokens: List<String> = listOf("<|im_end|>", "<|endoftext|>")
)

/**
 * Detailed telemetry and result of on-device model generation.
 */
data class GenerationResult(
    val text: String,
    val promptTokens: Int,
    val generatedTokens: Int,
    val firstTokenLatencyMs: Long,
    val totalLatencyMs: Long,
    val tokensPerSecond: Float,
    val peakMemoryMb: Float,
    val success: Boolean,
    val errorMessage: String? = null,
    val cancelled: Boolean = false
)

/**
 * Metadata descriptor for an active or staged on-device SLM.
 */
data class ModelInfo(
    val modelName: String,
    val runtime: String,
    val format: String,
    val quantization: String,
    val parameterCount: String,
    val modelSizeBytes: Long,
    val isLoaded: Boolean,
    val state: String
)

/**
 * Standard abstraction for on-device local language model inference on Android.
 * Decouples curriculum planning from specific inference runtimes (ONNX Runtime, GGUF/llama.cpp, native).
 */
interface LocalLanguageModel {
    suspend fun load(): ModelLoadResult
    suspend fun generate(request: GenerationRequest): GenerationResult
    suspend fun unload()
    fun isLoaded(): Boolean
    fun getModelInfo(): ModelInfo
    fun cancelGeneration()
}
