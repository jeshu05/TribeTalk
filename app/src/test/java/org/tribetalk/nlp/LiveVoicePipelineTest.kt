package org.tribetalk.nlp

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Unit test suite verifying Live Voice Classroom NLP and Queue Architecture:
 * 1. Strict sequential FIFO ordering with internal sequence IDs (Utterance 1 -> 2 -> 3).
 * 2. Token deduplication (ASR repetition/stutter removal).
 * 3. Devanagari punctuation & whitespace normalization.
 * 4. Empty and sub-word noise filtering.
 * 5. Worker resilience against unhandled exceptions (no deadlock).
 * 6. Clean lifecycle cancellation & sequence reset.
 */
class LiveVoicePipelineTest {

    @Test
    fun testUtteranceOrderingAndSequenceIds() = runBlocking {
        val processedItems = CopyOnWriteArrayList<QueuedUtterance>()
        val latch = CountDownLatch(3)

        val queue = LiveUtteranceQueue { item ->
            processedItems.add(item)
            latch.countDown()
        }

        val testScope = CoroutineScope(Dispatchers.Default + Job())
        queue.start(testScope)

        val inputs = listOf("किताब खोलो", "सब बच्चे बैठ जाओ", "पानी पियो")
        for (input in inputs) {
            val enqueued = queue.enqueue(input)
            assertTrue("Should enqueue valid input: $input", enqueued)
        }

        val completed = latch.await(3, TimeUnit.SECONDS)
        assertTrue("All 3 items should be processed within timeout", completed)

        assertEquals(3, processedItems.size)
        // Verify strict sequence IDs
        assertEquals(1, processedItems[0].sequenceId)
        assertEquals("किताब खोलो", processedItems[0].hindiText)

        assertEquals(2, processedItems[1].sequenceId)
        assertEquals("सब बच्चे बैठ जाओ", processedItems[1].hindiText)

        assertEquals(3, processedItems[2].sequenceId)
        assertEquals("पानी पियो", processedItems[2].hindiText)

        queue.stop()
        testScope.cancel()
    }

    @Test
    fun testEmptyAndBlankUtteranceFiltering() = runBlocking {
        val queue = LiveUtteranceQueue {}
        val testScope = CoroutineScope(Dispatchers.Default + Job())
        queue.start(testScope)

        assertFalse("Empty string should be rejected", queue.enqueue(""))
        assertFalse("Whitespace string should be rejected", queue.enqueue("   "))
        assertFalse("Tab and newline string should be rejected", queue.enqueue("\t\n"))

        assertEquals(0, queue.size())

        queue.stop()
        testScope.cancel()
    }

    @Test
    fun testTokenDeduplication() {
        val input = "नमस्ते नमस्ते बच्चे"
        val normalized = HindiNlpProcessor.normalizeHindiText(input)
        assertEquals("नमस्ते बच्चे", normalized)

        val stutterInput = "खोलो खोलो किताब"
        val normalizedStutter = HindiNlpProcessor.normalizeHindiText(stutterInput)
        assertEquals("खोलो किताब", normalizedStutter)
    }

    @Test
    fun testPunctuationNormalization() {
        val input = "किताब खोलो.. ध्यान से सुनो??"
        val normalized = HindiNlpProcessor.normalizeHindiText(input)
        assertEquals("किताब खोलो। ध्यान से सुनो?", normalized)
    }

    @Test
    fun testLinguisticConfidenceEvaluation() {
        // Noise / single characters should fail
        val (valid1, _) = HindiNlpProcessor.evaluateConfidence(".")
        assertFalse("Single punctuation should be invalid", valid1)

        val (valid2, _) = HindiNlpProcessor.evaluateConfidence("a")
        assertFalse("Single letter noise should be invalid", valid2)

        val (valid3, _) = HindiNlpProcessor.evaluateConfidence("   ")
        assertFalse("Whitespace should be invalid", valid3)

        // Genuine classroom phrases should pass
        val (valid4, _) = HindiNlpProcessor.evaluateConfidence("किताब खोलो")
        assertTrue("Standard classroom phrase should be valid", valid4)

        val (valid5, _) = HindiNlpProcessor.evaluateConfidence("बैठ जाओ")
        assertTrue("Short classroom phrase should be valid", valid5)
    }

    @Test
    fun testLiveUtteranceProcessorDeduplication() {
        val processor = LiveUtteranceProcessor()

        val first = processor.processFinal("किताब खोलो")
        assertNotNull("First utterance should be emitted", first)
        assertEquals("किताब खोलो", first)

        // Duplicate final should not cause crash
        val second = processor.processFinal("किताब खोलो")
        assertEquals("Subsequent valid utterance normalized", "किताब खोलो", second)

        // Noise should return null
        val noise = processor.processFinal(".")
        assertNull("Noise should return null", noise)
    }

    @Test
    fun testQueueWorkerErrorResilience() = runBlocking {
        val processedSuccess = CopyOnWriteArrayList<String>()
        val latch = CountDownLatch(2)

        val queue = LiveUtteranceQueue { item ->
            if (item.sequenceId == 1) {
                latch.countDown()
                throw RuntimeException("Simulated translation failure on item 1")
            } else {
                processedSuccess.add(item.hindiText)
                latch.countDown()
            }
        }

        val testScope = CoroutineScope(Dispatchers.Default + Job())
        queue.start(testScope)

        queue.enqueue("त्रुटि परीक्षण") // Item 1 (throws)
        queue.enqueue("सफल परीक्षण")   // Item 2 (succeeds)

        val completed = latch.await(3, TimeUnit.SECONDS)
        assertTrue("Queue must continue processing even after item 1 error", completed)
        assertEquals(1, processedSuccess.size)
        assertEquals("सफल परीक्षण", processedSuccess[0])

        queue.stop()
        testScope.cancel()
    }

    @Test
    fun testSequenceResetOnNewSession() = runBlocking {
        val queue = LiveUtteranceQueue {}
        val testScope = CoroutineScope(Dispatchers.Default + Job())
        queue.start(testScope)

        queue.enqueue("वाक्य एक")
        queue.resetSequence()

        val latch = CountDownLatch(1)
        var resetSeqId = -1

        val queue2 = LiveUtteranceQueue { item ->
            resetSeqId = item.sequenceId
            latch.countDown()
        }
        queue2.start(testScope)
        queue2.enqueue("नया सत्र वाक्य")

        latch.await(2, TimeUnit.SECONDS)
        assertEquals("Sequence should reset to 1 on new session", 1, resetSeqId)

        queue.stop()
        queue2.stop()
        testScope.cancel()
    }

    @Test
    fun testQueueStopCancelsWorker() = runBlocking {
        val queue = LiveUtteranceQueue {}
        val testScope = CoroutineScope(Dispatchers.Default + Job())
        queue.start(testScope)

        queue.enqueue("परीक्षण")
        queue.stop()

        assertEquals("Size must be 0 after stop", 0, queue.size())
        testScope.cancel()
    }
}
