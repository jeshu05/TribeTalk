package com.alchemists.tribetalk.nlp

import com.alchemists.tribetalk.curriculum.models.FLNDomain
import com.alchemists.tribetalk.nlp.models.ClassroomIntent
import org.junit.Assert.*
import org.junit.Test

class OfflineNlpMiddlewareTest {

    @Test
    fun testHindiNormalizationAndPunctuation() {
        val input = "बच्चों   आज  हम   गिनती सीखेंगे"
        val normalized = HindiNormalizer.normalize(input)
        assertEquals("Whitespace should be collapsed and sentence ended with full stop", "बच्चों आज हम गिनती सीखेंगे।", normalized)

        val questionInput = "चित्र में कितनी गाड़ियाँ हैं"
        val questionNormalized = HindiNormalizer.normalize(questionInput)
        assertEquals("Interrogative sentence should end with question mark", "चित्र में कितनी गाड़ियाँ हैं?", questionNormalized)
    }

    @Test
    fun testAsrDeduplication() {
        val artifactInput = "बच्चों बच्चों अपनी अपनी किताब खोलिए"
        val cleanedArtifact = HindiNormalizer.normalize(artifactInput)
        assertEquals("ASR artifact duplicates must be removed", "बच्चों अपनी किताब खोलिए।", cleanedArtifact)

        val legitimateInput = "बच्चों धीरे धीरे पढ़ो"
        val cleanedLegitimate = HindiNormalizer.normalize(legitimateInput)
        assertEquals("Legitimate Hindi reduplication phrase 'धीरे धीरे' must be preserved", "बच्चों धीरे धीरे पढ़ो।", cleanedLegitimate)
    }

    @Test
    fun testHindiNumberAndRangeExtraction() {
        val text = "बच्चों आज हम एक से दस तक गिनती सीखेंगे"
        val numbers = HindiNumberExtractor.extractNumbers(text)
        assertTrue("Should extract number words 'एक' and 'दस'", numbers.contains("एक") && numbers.contains("दस"))

        val range = HindiNumberExtractor.extractNumberRange(text)
        assertNotNull("Should extract range pair 1 to 10", range)
        assertEquals("Range start must be 1", 1, range!!.first)
        assertEquals("Range end must be 10", 10, range.second)
    }

    @Test
    fun testClassroomIntentAndDomainDetection() {
        val mathText = "सामने रखे कंकड़ों को गिनकर जोड़ बनाइए"
        val mathDomain = ClassroomIntentDetector.detectDomain(mathText)
        val mathTopic = ClassroomIntentDetector.detectTopic(mathText)
        assertEquals("Domain must be NUMERACY", FLNDomain.NUMERACY, mathDomain)
        assertEquals("Topic must be ADDITION", "ADDITION", mathTopic)

        val litText = "इस कहानी को ध्यान से पढ़िए और शब्द पहचानिए"
        val litDomain = ClassroomIntentDetector.detectDomain(litText)
        assertEquals("Domain must be LITERACY", FLNDomain.LITERACY, litDomain)
        assertEquals("Intent must be TEACHING_INSTRUCTION", ClassroomIntent.TEACHING_INSTRUCTION, ClassroomIntentDetector.detectIntent(litText))
    }

    @Test
    fun testUtteranceBoundaryDetection() {
        val incomplete = "बच्चों आज हम"
        assertFalse("Incomplete phrase should not trigger boundary completion", UtteranceBoundaryDetector.isUtteranceComplete(incomplete, 100L))

        val complete = "बच्चों आज हम गिनती सीखेंगे।"
        assertTrue("Phrase ending with punctuation should trigger boundary completion", UtteranceBoundaryDetector.isUtteranceComplete(complete, 0L))

        val verbEnding = "अपनी किताब खोलिए"
        assertTrue("Phrase ending with verb 'खोलिए' should trigger boundary completion", UtteranceBoundaryDetector.isUtteranceComplete(verbEnding, 0L))
    }

    @Test
    fun testSantaliPostProcessing() {
        val rawSantali = " ᱵᱟᱹᱵᱩ  ᱛᱮᱦᱮᱧ  ᱟᱵᱚ  ᱞᱮᱠᱷᱟ  ᱵᱚᱱ  ᱪᱮᱫᱟ "
        val processed = SantaliPostProcessor.postProcess(rawSantali)
        assertTrue("Santali output must contain Ol Chiki script", SantaliPostProcessor.containsOlChiki(processed))
        assertTrue("Santali output should end with Santali punctuation ᱾", processed.endsWith("᱾"))
    }

    @Test
    fun testFullOfflineProcessorWorkflow() {
        val rawAsr = "बच्चों   आज  हम   गिनती सीखेंगे"
        val nlpResult = OfflineNlpProcessor.process(rawAsr)

        assertEquals("Normalized text should be formatted", "बच्चों आज हम गिनती सीखेंगे।", nlpResult.normalizedText)
        assertEquals("Domain should be NUMERACY", FLNDomain.NUMERACY, nlpResult.domain)
        assertTrue("Utterance should be translation ready", nlpResult.isTranslationReady)
        assertTrue("Confidence should be >= 0.85", nlpResult.confidence >= 0.85f)
    }
}
