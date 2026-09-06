package com.alchemists.tribetalk.nlp

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HindiNlpProcessorTest {

    @Before
    fun setUp() {
        HindiNlpProcessor.clearContext()
    }

    @Test
    fun testTextNormalizationAndDeduplication() {
        // Test repeated stutter tokens
        val deduplicated1 = HindiNlpProcessor.normalizeHindiText("नमस्ते नमस्ते")
        assertEquals("नमस्ते", deduplicated1)

        val deduplicated2 = HindiNlpProcessor.normalizeHindiText("यह यह किताब किताब है")
        assertEquals("यह किताब है", deduplicated2)

        // Test whitespace and punctuation cleanup
        val normalizedWhitespace = HindiNlpProcessor.normalizeHindiText("बच्चों    आज    हम  गिनती   सीखेंगे...")
        assertEquals("बच्चों आज हम गिनती सीखेंगे।", normalizedWhitespace)
    }

    @Test
    fun testSentenceUtteranceSegmentation() {
        val multiSentence = "नमस्ते। आप कैसे हैं?"
        val segments = HindiNlpProcessor.segmentUtterances(multiSentence)

        assertEquals(2, segments.size)
        assertEquals("नमस्ते।", segments[0])
        assertEquals("आप कैसे हैं?", segments[1])

        val commandSentence = "किताब खोलो और पढ़ो। अपना काम करो!"
        val commandSegments = HindiNlpProcessor.segmentUtterances(commandSentence)
        assertEquals(2, commandSegments.size)
        assertEquals("किताब खोलो और पढ़ो।", commandSegments[0])
        assertEquals("अपना काम करो!", commandSegments[1])
    }

    @Test
    fun testLowConfidenceAndNoiseFiltering() {
        // Empty or single character noise
        val emptyResult = HindiNlpProcessor.process("")
        assertFalse(emptyResult.isValid)
        assertNotNull(emptyResult.feedbackMessage)

        val singleCharNoise = HindiNlpProcessor.process("a")
        assertFalse(singleCharNoise.isValid)

        val punctuationOnly = HindiNlpProcessor.process("?!?...")
        assertFalse(punctuationOnly.isValid)

        // Valid classroom utterances
        val validGreeting = HindiNlpProcessor.process("नमस्ते")
        assertTrue(validGreeting.isValid)
        assertEquals("नमस्ते", validGreeting.normalizedText)

        val validLesson = HindiNlpProcessor.process("बच्चों आज हम गिनती सीखेंगे")
        assertTrue(validLesson.isValid)
        assertEquals("बच्चों आज हम गिनती सीखेंगे", validLesson.normalizedText)
    }

    @Test
    fun testContextRetention() {
        HindiNlpProcessor.process("नमस्ते")
        HindiNlpProcessor.process("आज हम गिनती सीखेंगे")

        val history = HindiNlpProcessor.getContextHistory()
        assertEquals(2, history.size)
        assertEquals("नमस्ते", history[0])
        assertEquals("आज हम गिनती सीखेंगे", history[1])
    }

    @Test
    fun testLatencyTrackerCalculations() {
        val tracker = LatencyTracker()
        val t0 = 1000L

        tracker.markAsrFinal(t0)
        tracker.markNlpReady(t0 + 20L) // +20ms NLP
        tracker.markTranslationStart(t0 + 25L)
        tracker.markTranslationEnd(t0 + 75L) // +50ms Translation
        tracker.markTtsStart(t0 + 80L)
        tracker.markTtsResponse(t0 + 400L) // +320ms TTS
        tracker.markPlaybackStart(t0 + 420L) // +20ms AudioTrack start

        val metrics = tracker.computeMetrics()
        assertEquals(20L, metrics.nlpDurationMs)
        assertEquals(50L, metrics.translationDurationMs)
        assertEquals(320L, metrics.ttsDurationMs)
        assertEquals(420L, metrics.totalE2eLatencyMs)
        assertTrue(metrics.isWithinTarget) // 420ms <= 3000ms
    }
}
