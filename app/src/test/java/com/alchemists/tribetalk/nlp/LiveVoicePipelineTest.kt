package com.alchemists.tribetalk.nlp

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList

class LiveVoicePipelineTest {

    @Test
    fun testLiveUtteranceProcessorFinalHandling() {
        val processor = LiveUtteranceProcessor()

        // Valid classroom sentences
        val result1 = processor.processFinal("नमस्ते बच्चों")
        assertEquals("नमस्ते बच्चों", result1)

        val result2 = processor.processFinal("आज हम गिनती सीखेंगे")
        assertEquals("आज हम गिनती सीखेंगे", result2)

        val result3 = processor.processFinal("सब मेरे साथ बोलो")
        assertEquals("सब मेरे साथ बोलो", result3)

        // Sub-word noise rejection
        val noise = processor.processFinal("..")
        assertNull(noise)
    }

    @Test
    fun testLiveUtteranceProcessorStreamingBoundary() {
        val processor = LiveUtteranceProcessor()

        // Partial without boundary punctuation
        val partial1 = processor.processPartial("आज हम")
        assertNull(partial1)

        // Partial with completed sentence boundary
        val partial2 = processor.processPartial("आज हम गिनती सीखेंगे।")
        assertEquals("आज हम गिनती सीखेंगे।", partial2)
    }

    @Test
    fun testSequentialUtteranceQueueOrder() = runBlocking {
        val executionOrder = CopyOnWriteArrayList<String>()
        val queue = LiveUtteranceQueue { item ->
            executionOrder.add(item.hindiText)
        }

        queue.start(this)

        // Enqueue 3 utterances: A, B, C
        queue.enqueue("नमस्ते बच्चों।")
        queue.enqueue("आज हम गिनती सीखेंगे।")
        queue.enqueue("सब मेरे साथ बोलो।")

        // Wait for coroutine channel processing
        delay(200)

        // Verify strictly sequential order A -> B -> C (Never out of order or overlapping)
        assertEquals(3, executionOrder.size)
        assertEquals("नमस्ते बच्चों।", executionOrder[0])
        assertEquals("आज हम गिनती सीखेंगे।", executionOrder[1])
        assertEquals("सब मेरे साथ बोलो।", executionOrder[2])

        queue.stop()
    }
}
