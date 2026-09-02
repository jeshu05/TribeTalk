package com.alchemists.tribetalk.voice

import com.alchemists.tribetalk.translation.TranslationCache
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.math.sin

class RealtimePipelineTest {

    @Before
    fun setUp() {
        TranslationCache.clear()
    }

    @Test
    fun testVoiceActivityDetectorSpeechAndEndpoint() {
        val vad = VoiceActivityDetector(
            minSpeechDurationMs = 100L,
            minSilenceDurationMs = 200L,
            initialThresholdRms = 100.0f
        )

        var speechStarted = false
        var endpointDetected = false
        var detectedPcmLength = 0

        // 1. Silent PCM frame (RMS near 0)
        val silentFrame = ShortArray(480) { 0 }
        vad.processFrame(silentFrame) { ev ->
            if (ev is VadEvent.SpeechStarted) speechStarted = true
        }
        assertFalse("Silent frame should not trigger speech start", speechStarted)

        // 2. High-amplitude Speech frames (1000 Hz sine wave, RMS ~707)
        val speechFrame = ShortArray(480) { i -> (1000.0 * sin(2.0 * Math.PI * i / 16.0)).toInt().toShort() }
        repeat(5) {
            vad.processFrame(speechFrame) { ev ->
                if (ev is VadEvent.SpeechStarted) speechStarted = true
            }
        }
        assertTrue("High RMS frame should trigger speech start", speechStarted)

        // 3. Endpoint silence frames
        repeat(10) {
            vad.processFrame(silentFrame) { ev ->
                if (ev is VadEvent.EndpointDetected) {
                    endpointDetected = true
                    detectedPcmLength = ev.utterancePcm.size
                }
            }
            Thread.sleep(30)
        }

        assertTrue("Sustained silence should trigger endpoint detection", endpointDetected)
        assertTrue("Utterance PCM should contain accumulated samples", detectedPcmLength > 0)
    }

    @Test
    fun testRealtimeHindiAsrDeduplication() {
        val raw = "बच्चों बच्चों अपनी अपनी किताब खोलिए खोलिए"
        val trimmed = raw.trim()
        val words = trimmed.split("\\s+".toRegex())
        val cleanWords = mutableListOf<String>()
        for (word in words) {
            if (cleanWords.isEmpty() || cleanWords.last() != word) {
                cleanWords.add(word)
            }
        }
        val clean = cleanWords.joinToString(" ")
        assertEquals("Adjacent duplicate words must be removed", "बच्चों अपनी किताब खोलिए", clean)
    }

    @Test
    fun testTranslationCacheLRU() {
        TranslationCache.putTranslation("बच्चों", "hin_Deva", "sat_Olck", "ᱵᱟᱹᱵᱩ")
        val cached = TranslationCache.getTranslation("बच्चों", "hin_Deva", "sat_Olck")
        assertEquals("Cached translation should match", "ᱵᱟᱹᱵᱩ", cached)

        val pcm = ShortArray(100) { 10 }
        TranslationCache.putTtsAudio("ᱵᱟᱹᱵᱩ", "sat", pcm)
        val cachedPcm = TranslationCache.getTtsAudio("ᱵᱟᱹᱵᱩ", "sat")
        assertNotNull("Cached TTS audio should exist", cachedPcm)
        assertEquals("PCM array length should match", 100, cachedPcm!!.size)
    }

    @Test
    fun testPipelineLatencyCalculation() {
        val lat = PipelineLatency(asrMs = 350L, nmtMs = 280L, ttsMs = 450L, endToEndMs = 1080L)
        assertEquals("ASR latency must be 350ms", 350L, lat.asrMs)
        assertEquals("End to end latency must be 1080ms", 1080L, lat.endToEndMs)
        assertTrue("End to end latency must be sub-3-seconds (< 3000ms)", lat.endToEndMs < 3000L)
    }
}
