package org.tribetalk.curriculum.ai

import android.content.Context
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tribetalk.core.NativePipeline
import java.io.File
import java.nio.FloatBuffer
import java.nio.LongBuffer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Production on-device Qwen 2.5 0.5B inference engine for TribeTalk.
 * Runs 100% offline using INT8 quantized ONNX Runtime weights.
 */
class QwenLocalModel(private val context: Context) : LocalLanguageModel {

    companion object {
        private const val TAG = "QwenLocalModel"
        private const val MODEL_NAME = "Qwen2.5-0.5B-Instruct"
        private const val NUM_LAYERS = 24
        private const val NUM_HEADS = 2
        private const val HEAD_DIM = 64
        private const val VOCAB_SIZE = 151936
    }

    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    private val tokenizer = QwenTokenizer()

    private val isModelLoaded = AtomicBoolean(false)
    private val isCancelRequested = AtomicBoolean(false)
    private var currentState = "COLD"
    private var modelFile: File? = null
    private var vocabFile: File? = null

    init {
        resolveModelFiles()
    }

    private fun resolveModelFiles() {
        val candidateDirs = listOf(
            File(context.getExternalFilesDir(null), "models/qwen"),
            File(context.getExternalFilesDir(null), "models"),
            File("/sdcard/Android/data/org.tribetalk/files/models/qwen"),
            File(context.filesDir, "models/qwen"),
            File("staged_models/qwen"),
            File("models/qwen")
        )

        for (dir in candidateDirs) {
            val mf = File(dir, "model_int8.onnx")
            val vf = File(dir, "vocab.json")
            if (mf.exists() && mf.length() > 1024 * 1024) {
                modelFile = mf
                vocabFile = if (vf.exists()) vf else File(dir, "tokenizer.json")
                Log.i(TAG, "Resolved Qwen model at: ${mf.absolutePath} (${mf.length() / (1024*1024)} MB)")
                break
            }
        }
    }

    override fun isLoaded(): Boolean = isModelLoaded.get()

    override suspend fun load(): ModelLoadResult = withContext(Dispatchers.IO) {
        val t0 = System.currentTimeMillis()
        currentState = "LOADING"

        if (isModelLoaded.get()) {
            return@withContext ModelLoadResult(
                success = true,
                modelName = MODEL_NAME,
                loadTimeMs = 0L,
                memoryUsageMb = getApproximateMemoryMb()
            )
        }

        resolveModelFiles()
        val mf = modelFile
        if (mf == null || !mf.exists()) {
            currentState = "UNLOADED"
            return@withContext ModelLoadResult(
                success = false,
                modelName = MODEL_NAME,
                loadTimeMs = System.currentTimeMillis() - t0,
                memoryUsageMb = 0f,
                errorMessage = "Qwen 0.5B model artifact (model_int8.onnx) not found on device storage"
            )
        }

        try {
            vocabFile?.let { tokenizer.loadVocab(it) }

            // Initialize ONNX Runtime
            ortEnv = OrtEnvironment.getEnvironment()
            val opts = OrtSession.SessionOptions().apply {
                setIntraOpNumThreads(2)
                setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT)
            }
            ortSession = ortEnv?.createSession(mf.absolutePath, opts)
            isModelLoaded.set(true)
            currentState = "WARM"

            // Also initialize native pipeline bridge if available
            NativePipeline.qwenInit(mf.parent ?: "")
            NativePipeline.qwenLoad()

            val dt = System.currentTimeMillis() - t0
            Log.i(TAG, "Qwen 0.5B loaded successfully in ${dt}ms")

            ModelLoadResult(
                success = true,
                modelName = MODEL_NAME,
                loadTimeMs = dt,
                memoryUsageMb = getApproximateMemoryMb()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed loading Qwen model session: ${e.message}", e)
            currentState = "UNLOADED"
            isModelLoaded.set(false)
            ModelLoadResult(
                success = false,
                modelName = MODEL_NAME,
                loadTimeMs = System.currentTimeMillis() - t0,
                memoryUsageMb = 0f,
                errorMessage = e.message
            )
        }
    }

    override suspend fun generate(request: GenerationRequest): GenerationResult = withContext(Dispatchers.IO) {
        val t0 = System.currentTimeMillis()
        isCancelRequested.set(false)

        if (!isModelLoaded.get()) {
            val loadRes = load()
            if (!loadRes.success) {
                return@withContext GenerationResult(
                    text = "",
                    promptTokens = 0,
                    generatedTokens = 0,
                    firstTokenLatencyMs = 0L,
                    totalLatencyMs = System.currentTimeMillis() - t0,
                    tokensPerSecond = 0f,
                    peakMemoryMb = 0f,
                    success = false,
                    errorMessage = loadRes.errorMessage ?: "Model failed to load"
                )
            }
        }

        currentState = "GENERATING"
        val session = ortSession
        val env = ortEnv

        if (session == null || env == null) {
            currentState = "IDLE"
            return@withContext GenerationResult(
                text = "",
                promptTokens = 0,
                generatedTokens = 0,
                firstTokenLatencyMs = 0L,
                totalLatencyMs = System.currentTimeMillis() - t0,
                tokensPerSecond = 0f,
                peakMemoryMb = 0f,
                success = false,
                errorMessage = "ONNX Session not available"
            )
        }

        try {
            val promptTokenIds = tokenizer.encode(request.prompt)
            val promptTokensCount = promptTokenIds.size
            val generatedIds = mutableListOf<Long>()

            var firstTokenLatency = 0L
            val maxTokens = request.maxTokens.coerceIn(16, 256)

            // Feed initial prompt tokens
            var currentTokenIds = promptTokenIds
            var pastSeqLen = 0

            // Initialize past key values buffers
            val pastTensors = HashMap<String, OnnxTensor>()
            for (layer in 0 until NUM_LAYERS) {
                val emptyKey = OnnxTensor.createTensor(env, FloatBuffer.allocate(0), longArrayOf(1, NUM_HEADS.toLong(), 0, HEAD_DIM.toLong()))
                val emptyVal = OnnxTensor.createTensor(env, FloatBuffer.allocate(0), longArrayOf(1, NUM_HEADS.toLong(), 0, HEAD_DIM.toLong()))
                pastTensors["past_key_values.$layer.key"] = emptyKey
                pastTensors["past_key_values.$layer.value"] = emptyVal
            }

            for (step in 0 until maxTokens) {
                if (isCancelRequested.get()) {
                    Log.i(TAG, "Generation cancelled at step $step")
                    cleanupTensors(pastTensors)
                    currentState = "IDLE"
                    return@withContext GenerationResult(
                        text = tokenizer.decode(generatedIds),
                        promptTokens = promptTokensCount,
                        generatedTokens = generatedIds.size,
                        firstTokenLatencyMs = firstTokenLatency,
                        totalLatencyMs = System.currentTimeMillis() - t0,
                        tokensPerSecond = 0f,
                        peakMemoryMb = getApproximateMemoryMb(),
                        success = false,
                        errorMessage = "Generation cancelled by higher-priority task",
                        cancelled = true
                    )
                }

                val seqLen = currentTokenIds.size
                val totalLen = pastSeqLen + seqLen

                val inputIdsTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(currentTokenIds), longArrayOf(1, seqLen.toLong()))
                val attentionMaskTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(LongArray(totalLen) { 1L }), longArrayOf(1, totalLen.toLong()))
                val positionIds = LongArray(seqLen) { (pastSeqLen + it).toLong() }
                val positionIdsTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(positionIds), longArrayOf(1, seqLen.toLong()))

                val inputs = HashMap<String, OnnxTensor>()
                inputs["input_ids"] = inputIdsTensor
                inputs["attention_mask"] = attentionMaskTensor
                inputs["position_ids"] = positionIdsTensor
                inputs.putAll(pastTensors)

                val stepT0 = System.currentTimeMillis()
                val result = session.run(inputs)

                if (step == 0) {
                    firstTokenLatency = System.currentTimeMillis() - stepT0
                }

                // Extract logits
                val logitsTensor = result.get("logits").get() as OnnxTensor
                val logitsBuffer = logitsTensor.floatBuffer
                val offset = (seqLen - 1) * VOCAB_SIZE
                var maxVal = -Float.MAX_VALUE
                var maxIdx = 0

                // Greedy argmax over vocabulary
                val checkLimit = VOCAB_SIZE.coerceAtMost(logitsBuffer.remaining() - offset)
                for (v in 0 until checkLimit) {
                    val logit = logitsBuffer.get(offset + v)
                    if (logit > maxVal) {
                        maxVal = logit
                        maxIdx = v
                    }
                }

                val nextTokenId = maxIdx.toLong()

                // Check stop tokens
                if (tokenizer.isSpecialToken(nextTokenId)) {
                    result.close()
                    inputIdsTensor.close()
                    attentionMaskTensor.close()
                    positionIdsTensor.close()
                    break
                }

                generatedIds.add(nextTokenId)

                // Update KV cache tensors
                for (layer in 0 until NUM_LAYERS) {
                    pastTensors["past_key_values.$layer.key"]?.close()
                    pastTensors["past_key_values.$layer.value"]?.close()
                    val newKey = result.get("present.$layer.key").get() as OnnxTensor
                    val newVal = result.get("present.$layer.value").get() as OnnxTensor
                    pastTensors["past_key_values.$layer.key"] = newKey
                    pastTensors["past_key_values.$layer.value"] = newVal
                }

                pastSeqLen = totalLen
                currentTokenIds = longArrayOf(nextTokenId)

                inputIdsTensor.close()
                attentionMaskTensor.close()
                positionIdsTensor.close()
            }

            cleanupTensors(pastTensors)
            currentState = "IDLE"

            val totalDt = System.currentTimeMillis() - t0
            val genTokens = generatedIds.size
            val tokPerSec = if (totalDt > 0) (genTokens.toFloat() / totalDt.toFloat()) * 1000f else 0f
            val decodedText = tokenizer.decode(generatedIds)

            Log.i(TAG, "Generated $genTokens tokens in ${totalDt}ms (${tokPerSec} tok/s)")

            GenerationResult(
                text = decodedText,
                promptTokens = promptTokensCount,
                generatedTokens = genTokens,
                firstTokenLatencyMs = firstTokenLatency,
                totalLatencyMs = totalDt,
                tokensPerSecond = tokPerSec,
                peakMemoryMb = getApproximateMemoryMb(),
                success = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error during Qwen token generation: ${e.message}", e)
            currentState = "IDLE"
            GenerationResult(
                text = "",
                promptTokens = 0,
                generatedTokens = 0,
                firstTokenLatencyMs = 0L,
                totalLatencyMs = System.currentTimeMillis() - t0,
                tokensPerSecond = 0f,
                peakMemoryMb = 0f,
                success = false,
                errorMessage = e.message
            )
        }
    }

    override fun cancelGeneration() {
        isCancelRequested.set(true)
        NativePipeline.qwenCancel()
    }

    override suspend fun unload() {
        withContext(Dispatchers.IO) {
            try {
                ortSession?.close()
                ortSession = null
                ortEnv?.close()
                ortEnv = null
                NativePipeline.qwenUnload()
                isModelLoaded.set(false)
                currentState = "UNLOADED"
                Log.i(TAG, "Qwen 0.5B session unloaded and native resources freed.")
            } catch (e: Exception) {
                Log.w(TAG, "Error while unloading Qwen session: ${e.message}")
            }
        }
    }

    override fun getModelInfo(): ModelInfo {
        return ModelInfo(
            modelName = MODEL_NAME,
            runtime = "ONNX Runtime Mobile (INT8)",
            format = "ONNX",
            quantization = "INT8",
            parameterCount = "490M",
            modelSizeBytes = modelFile?.length() ?: 512096557L,
            isLoaded = isModelLoaded.get(),
            state = currentState
        )
    }

    private fun cleanupTensors(map: Map<String, OnnxTensor>) {
        map.values.forEach {
            try { it.close() } catch (e: Exception) { /* ignore */ }
        }
    }

    private fun getApproximateMemoryMb(): Float {
        val runtime = Runtime.getRuntime()
        val used = runtime.totalMemory() - runtime.freeMemory()
        return used / (1024f * 1024f)
    }
}
