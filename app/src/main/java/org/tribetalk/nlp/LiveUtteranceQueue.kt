package org.tribetalk.nlp

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

/**
 * Single queued utterance item with strict sequential ID.
 */
data class QueuedUtterance(
    val sequenceId: Int,
    val id: String = UUID.randomUUID().toString().take(8),
    val hindiText: String,
    val timestampMs: Long = System.currentTimeMillis()
)

/**
 * Coroutine-safe sequential FIFO Utterance Queue for Live Classroom Voice Translation.
 *
 * Guarantees strict sequential end-to-end ordering (Translate -> Display -> Synthesize -> Playback)
 * with internal sequence IDs so that Utterance 1 finishes playback completely before Utterance 2 begins.
 */
class LiveUtteranceQueue(
    private val onProcessUtterance: suspend (QueuedUtterance) -> Unit
) {
    companion object {
        private const val TAG = "LiveUtteranceQueue"
        private const val MAX_QUEUE_CAPACITY = 10
    }

    private var channel = Channel<QueuedUtterance>(MAX_QUEUE_CAPACITY)
    private var workerJob: Job? = null
    private val queueCount = AtomicInteger(0)
    private val sequenceCounter = AtomicInteger(0)

    fun start(scope: CoroutineScope) {
        stop()
        channel = Channel(MAX_QUEUE_CAPACITY)
        workerJob = scope.launch(Dispatchers.Default) {
            Log.i(TAG, "[QUEUE] Live Utterance Queue worker started")
            try {
                for (item in channel) {
                    queueCount.decrementAndGet()
                    Log.i(TAG, "[QUEUE] Processing item #${item.sequenceId} (${item.id}): \"${item.hindiText}\" (Remaining in queue: ${queueCount.get()})")
                    try {
                        onProcessUtterance(item)
                    } catch (e: Exception) {
                        Log.e(TAG, "[QUEUE ERROR] Error processing item #${item.sequenceId} (${item.id}): ${e.localizedMessage}", e)
                    }
                }
            } catch (e: CancellationException) {
                Log.i(TAG, "[QUEUE] Worker job cancelled")
            }
        }
    }

    fun enqueue(hindiText: String): Boolean {
        val trimmed = hindiText.trim()
        if (trimmed.isEmpty()) return false

        val seq = sequenceCounter.incrementAndGet()
        val item = QueuedUtterance(sequenceId = seq, hindiText = trimmed)
        val success = channel.trySend(item).isSuccess
        if (success) {
            val count = queueCount.incrementAndGet()
            Log.i(TAG, "[QUEUE] Enqueued item #${item.sequenceId}: \"$trimmed\" (Queue size: $count)")
        } else {
            Log.w(TAG, "[QUEUE WARNING] Queue full or closed, dropped item #${item.sequenceId}: \"$trimmed\"")
        }
        return success
    }

    fun size(): Int = queueCount.get()

    fun resetSequence() {
        sequenceCounter.set(0)
    }

    fun stop() {
        workerJob?.cancel()
        workerJob = null
        channel.close()
        queueCount.set(0)
    }
}
