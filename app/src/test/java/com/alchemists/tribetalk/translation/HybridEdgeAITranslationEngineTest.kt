package com.alchemists.tribetalk.translation

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class HybridEdgeAITranslationEngineTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var tempFile: File
    private lateinit var translationMemory: TranslationMemory
    private lateinit var engine: HybridEdgeAITranslationEngine

    @Before
    fun setUp() {
        tempFile = tempFolder.newFile("test_hybrid_tm.csv")
        translationMemory = TranslationMemory(tempFile)
        engine = HybridEdgeAITranslationEngine(translationMemory, onnxEngine = null)
    }

    @After
    fun tearDown() {
        translationMemory.clearMemory()
    }

    @Test
    fun testHindiToSantaliCuratedFLNMatch() {
        val result = engine.translate("नमस्ते", Language.HINDI, Language.SANTALI)
        assertTrue(result.matched)
        assertEquals("ᱡᱚᱦᱟᱨ", result.olChikiText)
        assertEquals("CURATED_FLN", result.engineTier)
        assertTrue(result.phoneticDevanagari.contains("जोहार"))
        assertFalse(result.requiresReview)
    }

    @Test
    fun testClassroomInstructionTranslation() {
        val result = engine.translate("किताब खोलो", Language.HINDI, Language.SANTALI)
        assertTrue(result.matched)
        assertEquals("ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ", result.olChikiText)
        assertTrue(result.phoneticDevanagari.contains("पुथी"))
        assertEquals("CURATED_FLN", result.engineTier)
    }

    @Test
    fun testStudentDoubtTranslation() {
        val result = engine.translate("पानी पीना है", Language.HINDI, Language.SANTALI)
        assertTrue(result.matched)
        assertEquals("ᱫᱟᱜ ᱧᱩ ᱥᱟᱱᱟᱹᱧ ᱠᱟᱱᱟ", result.olChikiText)
        assertEquals("CURATED_FLN", result.engineTier)
    }

    @Test
    fun testSantaliToHindiExactMatch() {
        val result = engine.translate("ᱡᱚᱦᱟᱨ", Language.SANTALI, Language.HINDI)
        assertTrue(result.matched)
        assertEquals("नमस्ते", result.translatedText)
        assertEquals("CURATED_FLN", result.engineTier)
    }

    @Test
    fun testAlternativePhrasingMatch() {
        // "पुस्तक खोलो" is an alternative phrasing for "किताब खोलो"
        val result = engine.translate("पुस्तक खोलो", Language.HINDI, Language.SANTALI)
        assertTrue(result.matched)
        assertEquals("ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ", result.olChikiText)
        assertEquals("CURATED_FLN", result.engineTier)
    }

    @Test
    fun testTeacherMemoryPriorityOverride() {
        val initialResult = engine.translate("यह एक मेज है", Language.HINDI, Language.SANTALI)
        assertTrue(initialResult.matched)
        assertEquals("ONNX_EDGE_AI", initialResult.engineTier)

        // Teacher corrects the translation
        val correction = TranslationEntry(
            sourceText = "यह एक मेज है",
            targetText = "ᱱᱤᱭᱟᱹ ᱢᱤᱫ ᱴᱮᱵᱩᱞ ᱠᱟᱱᱟ",
            sourceLang = Language.HINDI,
            targetLang = Language.SANTALI,
            category = "Teacher Correction"
        )
        engine.addCorrection(correction)

        val updatedResult = engine.translate("यह एक मेज है", Language.HINDI, Language.SANTALI)
        assertTrue(updatedResult.matched)
        assertEquals("ᱱᱤᱭᱟᱹ ᱢᱤᱫ ᱴᱮᱵᱩᱞ ᱠᱟᱱᱟ", updatedResult.translatedText)
        assertEquals("High (Teacher Validated)", updatedResult.confidence)
        assertEquals("TEACHER_MEMORY", updatedResult.engineTier)
    }

    @Test
    fun testMorphologicalSubwordFallback() {
        // "किताब कलम" (book pen) composed of 2 FLN words
        val result = engine.translate("किताब कलम", Language.HINDI, Language.SANTALI)
        assertTrue(result.matched)
        assertEquals("ᱯᱩᱛᱷᱤ ᱠᱚᱞᱚᱢ", result.olChikiText)
        assertEquals("MORPHOLOGICAL_SUBWORD", result.engineTier)
        assertTrue(result.requiresReview)
    }

    @Test
    fun testTransliterationFallbackForOutVocabOlChiki() {
        val result = engine.translate("ᱥᱟᱱᱛᱟᱲᱤ", Language.SANTALI, Language.HINDI)
        assertTrue(result.matched)
        assertEquals("TRANSLITERATION_FALLBACK", result.engineTier)
    }

    @Test
    fun testEmptyInputHandling() {
        val result = engine.translate("   ", Language.HINDI, Language.SANTALI)
        assertFalse(result.matched)
        assertEquals("", result.translatedText)
        assertEquals("none", result.matchType)
    }
}
