package com.alchemists.tribetalk.translation

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NeuralNMTTranslationEngineTest {

    private lateinit var neuralEngine: NeuralNMTTranslationEngine

    @Before
    fun setUp() {
        neuralEngine = NeuralNMTTranslationEngine(onnxEngine = null)
    }

    @Test
    fun testSentencePieceTokenization() {
        val tokens = SentencePieceTokenizer.tokenize("किताब खोलो", Language.HINDI)
        assertTrue(tokens.isNotEmpty())
        assertEquals(SentencePieceTokenizer.BOS_TOKEN_ID, tokens[0])
        assertEquals(SentencePieceTokenizer.HIN_DEVA_LANG_ID, tokens[1])
        assertEquals(SentencePieceTokenizer.EOS_TOKEN_ID, tokens.last())
    }

    @Test
    fun testArbitraryWeatherSentenceTranslation() {
        val result = neuralEngine.translate("कल बारिश होगी", Language.HINDI, Language.SANTALI)
        assertTrue(result.matched)
        assertEquals("ONNX_EDGE_AI", result.engineTier)
        assertTrue(result.olChikiText.contains("ᱜᱟᱯᱟ") || result.olChikiText.contains("ᱫᱟᱜ"))
        assertTrue(result.phoneticDevanagari.isNotBlank())
        assertTrue(result.confidence.contains("Edge AI Neural"))
    }

    @Test
    fun testArbitraryOpenEndedQuestionTranslation() {
        val result = neuralEngine.translate("आज मौसम कैसा है?", Language.HINDI, Language.SANTALI)
        assertTrue(result.matched)
        assertEquals("ONNX_EDGE_AI", result.engineTier)
        assertTrue(result.olChikiText.endsWith("?"))
        assertFalse(result.requiresReview)
    }

    @Test
    fun testMeraNaamTranslation() {
        val result = neuralEngine.translate("मेरा नाम", Language.HINDI, Language.SANTALI)
        assertTrue(result.matched)
        assertEquals("ONNX_EDGE_AI", result.engineTier)
        assertEquals("ᱤᱧᱟᱜ ᱧᱩᱛᱩᱢ", result.olChikiText)
        assertTrue(OlChikiTransliterator.isOlChiki(result.olChikiText))
        assertFalse(result.olChikiText.contains("मेरा"))
    }

    @Test
    fun testEmptyInputHandling() {
        val result = neuralEngine.translate("   ", Language.HINDI, Language.SANTALI)
        assertFalse(result.matched)
        assertEquals("", result.translatedText)
    }
}
