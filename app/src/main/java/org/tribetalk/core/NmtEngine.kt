package org.tribetalk.core

import android.content.Context
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.io.File
import java.nio.FloatBuffer
import java.nio.LongBuffer

/**
 * On-device IndicTrans2 NMT engine (Phase 3, validated).
 *
 * SHIPPING STACK (proven in scripts/run_phase3_nmt.py — 10/10 piece-identical,
 * RSS 548.8 MB -> 309.1 MB, 10.0 ms/token on desktop CPU):
 *
 *   nmt/encoder_model_int8_pruned.onnx      101.1 MB  pruned src vocab 93478
 *   nmt/decoder_model_merged_pruned.onnx    166.1 MB  pruned tgt vocab 93436
 *   nmt/vocab.src.pruned.tsv + vocab.tgt.pruned.tsv (renumbered 0..N-1)
 *
 * The decoder is the optimum `merge_decoders` fusion of the first-step and
 * KV-cached graphs: ONE session serves all steps through the `use_cache_branch`
 * bool input (false = step 1 consumes encoder_hidden_states, true = steps 2..N
 * consume past_key_values.*). The two-session layout this engine previously
 * assumed is gone, and with it the "Invalid input name" crash that silently
 * forced every translation onto the lexicon fallback.
 *
 * Memory design (2 GB tablet):
 *  - Sessions created lazily and CLOSED on [release] (governor-managed).
 *  - INT8 + pruned variants preferred; unpruned fallbacks accepted for
 *    compatibility with older models.zip layouts.
 *  - Max new tokens hard-capped (32) to bound KV-cache growth.
 *  - Pruned TSV vocabs loaded (93k rows each, ~4.1 MB total).
 */
class NmtEngine(private val context: Context) {

    companion object {
        private const val TAG = "NmtEngine"

        /**
         * Decoder sequence length cap. 128 tokens ensures full sentences and multi-word
         * Ol Chiki phrases are translated without truncation.
         */
        private const val MAX_NEW_TOKENS = 128

        /** Source side is capped so the encoder attention is [1, 96, 96]. */
        private const val MAX_SRC_TOKENS = 96

        // config.json: decoder_start_token_id = 2, eos_token_id = 2, pad = 1.
        // The previous BOS_ID = 1 fed the PAD token as the decoder start.
        private const val DECODER_START_ID = 2L
        private const val EOS_ID = 2L

        /** Longest sentencepiece piece considered during greedy matching. */
        private const val MAX_PIECE = 20
    }

    private val ortEnv: OrtEnvironment by lazy { OrtEnvironment.getEnvironment() }

    private var encoderSession: OrtSession? = null

    /**
     * First decoder step. Emits `encoder_hidden_states`-fed cross-attention KV
     * and therefore MUST be a separate graph from [decoderPastSession]:
     * decoder_with_past_model.onnx does not accept `encoder_hidden_states`
     * (measured input list: input_ids, encoder_attention_mask + 72 past_*).
     */
    private var decoderFirstSession: OrtSession? = null

    /** Steps 2..N, consuming past_key_values.* / emitting present.*. */
    private var decoderPastSession: OrtSession? = null

    private var srcVocab: Map<String, Int> = emptyMap()
    private var tgtVocabRev: Map<Int, String> = emptyMap()
    private var loaded = false

    val isLoaded: Boolean
        get() = loaded

    private fun resolve(rel: String): File? {
        val roots = listOfNotNull(
            context.getExternalFilesDir(null)?.let { File(it, "models") },
            File("/sdcard/Android/data/org.tribetalk/files/models"),
            File(context.filesDir, "models")
        )
        return roots.firstNotNullOfOrNull { root ->
            File(root, rel).takeIf { it.exists() && it.length() > 0 }
        }
    }

    @Synchronized
    fun load(): Boolean {
        if (loaded) return true
        return try {
            val srcVocabFile = resolve("nmt/vocab.src.pruned.tsv") ?: resolve("nmt/vocab.src.tsv")
            val tgtVocabFile = resolve("nmt/vocab.tgt.pruned.tsv") ?: resolve("nmt/vocab.tgt.tsv")
            if (srcVocabFile == null || tgtVocabFile == null) return false

            srcVocab = loadVocab(srcVocabFile)
            tgtVocabRev = loadVocab(tgtVocabFile).entries.associate { it.value to it.key }
            // Hard requirement: without the FLORES source tag the decoder cannot
            // tell Hindi from Santali, so a missing tag must fail the load immediately
            // instead of allocating 280MB of sessions and hanging the UI.
            val haveTags = srcVocab.containsKey("hin_Deva") && srcVocab.containsKey("sat_Olck")
            if (!haveTags || srcVocab.isEmpty() || tgtVocabRev.isEmpty()) {
                Log.w(TAG, "NMT unusable: srcVocab=${srcVocab.size} tgtVocab=${tgtVocabRev.size} languageTags=$haveTags")
                return false
            }

            val enc = resolve("nmt/encoder_model_int8_pruned.onnx")
                ?: resolve("nmt/encoder_model_int8.onnx")
                ?: resolve("nmt/encoder_model.onnx") ?: return false
            // Preferred: ONE fused decoder graph (scripts/merge_nmt_decoders.py)
            // that serves step 1 and steps 2..N through `use_cache_branch`.
            val decMerged = resolve("nmt/decoder_model_merged_pruned.onnx")
                ?: resolve("nmt/decoder_model_merged_int8.onnx")
                ?: resolve("nmt/decoder_model_merged.onnx")
            val decFirst = decMerged
                ?: resolve("nmt/decoder_model_int8.onnx")
                ?: resolve("nmt/decoder_model.onnx") ?: return false
            // Only the unfused fallback needs the second decoder; for the fused
            // graph both roles are served by decoderFirstSession.
            val decPast = if (decMerged != null) {
                decFirst
            } else {
                resolve("nmt/decoder_with_past_model_int8.onnx")
                    ?: resolve("nmt/decoder_with_past_model.onnx") ?: return false
            }

            val opts = OrtSession.SessionOptions().apply {
                setIntraOpNumThreads(2)
                setInterOpNumThreads(1)
                setMemoryPatternOptimization(true)
                setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            }
            encoderSession = ortEnv.createSession(enc.absolutePath, opts)
            decoderFirstSession = ortEnv.createSession(decFirst.absolutePath, opts)
            decoderPastSession = if (decFirst.name == decPast.name) {
                decoderFirstSession // fused graph serves both phases
            } else {
                ortEnv.createSession(decPast.absolutePath, opts)
            }

            loaded = true
            loaded
        } catch (e: Exception) {
            Log.e(TAG, "NMT load failed", e)
            release()
            false
        }
    }

    private fun loadVocab(file: File?): Map<String, Int> {
        if (file == null) return emptyMap()
        val map = mutableMapOf<String, Int>()
        file.useLines { lines ->
            for (line in lines) {
                val idx = line.lastIndexOf('\t')
                if (idx > 0) {
                    val piece = unescapePiece(line.substring(0, idx))
                    val id = line.substring(idx + 1).toIntOrNull()
                    if (id != null) map[piece] = id
                }
                if (map.size >= 200_000) break // memory bound
            }
        }
        return map
    }

    /**
     * Reverses the escaping applied by scripts/export_nmt_vocab.py. Piece text can
     * contain backslashes, newlines and tabs; the TSV is line- and tab-delimited,
     * so those are written as \\, \\n and \\t.
     */
    private fun unescapePiece(raw: String): String {
        if (raw.indexOf('\\') < 0) return raw
        val sb = StringBuilder(raw.length)
        var i = 0
        while (i < raw.length) {
            val c = raw[i]
            if (c == '\\' && i + 1 < raw.length) {
                when (raw[i + 1]) {
                    'n' -> { sb.append('\n'); i += 2 }
                    't' -> { sb.append('\t'); i += 2 }
                    '\\' -> { sb.append('\\'); i += 2 }
                    else -> { sb.append(c); i++ }
                }
            } else {
                sb.append(c); i++
            }
        }
        return sb.toString()
    }

    @Synchronized
    fun release() {
        if (decoderFirstSession === decoderPastSession) {
            decoderPastSession = null
        }
        for (s in listOf(encoderSession, decoderFirstSession, decoderPastSession)) {
            try { s?.close() } catch (_: Throwable) {}
        }
        encoderSession = null
        decoderFirstSession = null
        decoderPastSession = null
        loaded = false
    }

    /**
     * Greedy translate with KV cache. Returns target text or "" on failure
     * (caller falls back to the lexicon translator).
     */
    fun translate(sourceText: String, hindiToSantali: Boolean): String {
        if (!loaded && !load()) return ""
        val enc = encoderSession ?: return ""
        val decPast = decoderPastSession ?: return ""
        if (srcVocab.isEmpty() || tgtVocabRev.isEmpty()) return ""

        return try {
            val srcTokens = tokenize(sourceText, srcVocab, hindiToSantali).take(MAX_SRC_TOKENS)
            if (srcTokens.isEmpty()) return ""

            val inputIds = LongArray(srcTokens.size) { srcTokens[it].toLong() }
            val attn = LongArray(srcTokens.size) { 1L }
            val encIdsT = OnnxTensor.createTensor(ortEnv, LongBuffer.wrap(inputIds), longArrayOf(1, srcTokens.size.toLong()))
            val encAttnT = OnnxTensor.createTensor(ortEnv, LongBuffer.wrap(attn), longArrayOf(1, srcTokens.size.toLong()))

            val encOut = enc.run(mapOf("input_ids" to encIdsT, "attention_mask" to encAttnT))
            val hiddenT = encOut[0] as OnnxTensor

            val decInputNames = decPast.inputNames
            val isMerged = decInputNames.contains("use_cache_branch")

            val generated = mutableListOf<Long>()
            var pastKey: MutableMap<String, OnnxTensor> = mutableMapOf()
            var decoderInput = longArrayOf(DECODER_START_ID)

            for (step in 0 until MAX_NEW_TOKENS) {
                val decIdsT = OnnxTensor.createTensor(ortEnv, LongBuffer.wrap(decoderInput), longArrayOf(1, decoderInput.size.toLong()))
                val feed = mutableMapOf<String, OnnxTensor>()
                if (decInputNames.contains("input_ids")) {
                    feed["input_ids"] = decIdsT
                }
                if (decInputNames.contains("encoder_hidden_states")) {
                    feed["encoder_hidden_states"] = hiddenT
                }
                if (decInputNames.contains("encoder_attention_mask")) {
                    feed["encoder_attention_mask"] = encAttnT
                }

                val dummyTensorsToClose = mutableListOf<OnnxTensor>()
                var useCacheTensor: OnnxTensor? = null

                if (isMerged) {
                    if (step == 0) {
                        useCacheTensor = OnnxTensor.createTensor(ortEnv, booleanArrayOf(false))
                        feed["use_cache_branch"] = useCacheTensor
                        for (layer in 0 until 18) {
                            val dk = OnnxTensor.createTensor(ortEnv, FloatBuffer.allocate(0), longArrayOf(1, 8, 0, 64))
                            val dv = OnnxTensor.createTensor(ortEnv, FloatBuffer.allocate(0), longArrayOf(1, 8, 0, 64))
                            val ek = OnnxTensor.createTensor(ortEnv, FloatBuffer.allocate(0), longArrayOf(1, 8, 0, 64))
                            val ev = OnnxTensor.createTensor(ortEnv, FloatBuffer.allocate(0), longArrayOf(1, 8, 0, 64))
                            feed["past_key_values.$layer.decoder.key"] = dk
                            feed["past_key_values.$layer.decoder.value"] = dv
                            feed["past_key_values.$layer.encoder.key"] = ek
                            feed["past_key_values.$layer.encoder.value"] = ev
                            dummyTensorsToClose.add(dk)
                            dummyTensorsToClose.add(dv)
                            dummyTensorsToClose.add(ek)
                            dummyTensorsToClose.add(ev)
                        }
                    } else {
                        useCacheTensor = OnnxTensor.createTensor(ortEnv, booleanArrayOf(true))
                        feed["use_cache_branch"] = useCacheTensor
                        feed.putAll(pastKey)
                    }
                } else {
                    feed.putAll(pastKey)
                }

                val out = decPast.run(feed)

                // Clean up step inputs
                dummyTensorsToClose.forEach { try { it.close() } catch (_: Throwable) {} }
                useCacheTensor?.let { try { it.close() } catch (_: Throwable) {} }
                decIdsT.close()

                val logits = out[0] as OnnxTensor
                val logitsBuf = logits.floatBuffer
                val vocabSize = logits.info.shape[2].toInt()
                val offset = (decoderInput.size - 1) * vocabSize

                var best = Float.NEGATIVE_INFINITY
                var bestId = EOS_ID.toInt()
                for (v in 0 until vocabSize) {
                    val l = logitsBuf.get(offset + v)
                    if (l > best) { best = l; bestId = v }
                }
                logits.close()

                if (bestId == EOS_ID.toInt()) {
                    out.close()
                    break
                }
                generated.add(bestId.toLong())

                // Swap past KV tensors for present.* from this step
                val newPast = mutableMapOf<String, OnnxTensor>()
                for (entry in out) {
                    val key = entry.key
                    val value = entry.value
                    if (key.startsWith("present.")) {
                        (value as? OnnxTensor)?.let {
                            newPast[key.replace("present.", "past_key_values.")] = it
                        }
                    }
                }
                pastKey.values.forEach { try { it.close() } catch (_: Throwable) {} }
                pastKey = newPast

                decoderInput = longArrayOf(bestId.toLong())
            }

            pastKey.values.forEach { try { it.close() } catch (_: Throwable) {} }
            encOut.close()
            encIdsT.close(); encAttnT.close()

            generated.mapNotNull { tgtVocabRev[it.toInt()] }
                .joinToString("").replace("▁", " ").trim()
        } catch (e: Exception) {
            Log.e(TAG, "NMT translate failed; caller falls back to lexicon", e)
            ""
        }
    }

    /** Greedy longest-piece-first tokenization with ▁ word boundaries. */
    private fun tokenize(text: String, vocab: Map<String, Int>, hindiToSantali: Boolean): List<Int> {
        val ids = mutableListOf<Int>()
        val srcTag = if (hindiToSantali) "hin_Deva" else "sat_Olck"
        val tgtTag = if (hindiToSantali) "sat_Olck" else "hin_Deva"
        val srcTagId = vocab[srcTag] ?: return emptyList()
        val tgtTagId = vocab[tgtTag] ?: return emptyList()
        ids.add(srcTagId)
        ids.add(tgtTagId)

        for (word in text.split(Regex("\\s+")).filter { it.isNotBlank() }) {
            val sb = StringBuilder("▁").append(word)
            var pos = 0
            while (pos < sb.length) {
                var matchId: Int? = null
                var matchLen = 1
                for (end in minOf(sb.length, pos + 20) downTo pos + 1) {
                    val piece = sb.substring(pos, end)
                    val id = vocab[piece]
                    if (id != null) { matchId = id; matchLen = end - pos; break }
                }
                if (matchId != null) ids.add(matchId)
                pos += matchLen
            }
        }
        vocab["</s>"]?.let { ids.add(it) }
        return ids
    }
}