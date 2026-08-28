package com.alchemists.tribetalk.translation

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class TranslationEngineTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var tempFile: File
    private lateinit var translationMemory: TranslationMemory
    private lateinit var translationEngine: OfflineFLNTranslationEngine

    @Before
    fun setUp() {
        tempFile = tempFolder.newFile("test_translation_memory.csv")
        translationMemory = TranslationMemory(tempFile)
        translationEngine = OfflineFLNTranslationEngine(translationMemory)
    }

    @After
    fun tearDown() {
        translationMemory.clearMemory()
    }

    @Test
    fun testHindiToSantaliExactMatch() {
        val result = translationEngine.translate("नमस्ते", Language.HINDI, Language.SANTALI)
        assertTrue(result.matched)
        assertEquals("Johar (जोहार)", result.translatedText)
        assertEquals("High (Offline Match)", result.confidence)
        assertFalse(result.requiresReview)
        assertEquals("exact", result.matchType)
    }

    @Test
    fun testSantaliToHindiExactMatch() {
        val result = translationEngine.translate("Ceka menama?", Language.SANTALI, Language.HINDI)
        assertTrue(result.matched)
        assertEquals("आप कैसे हैं? (Aap kaise hain?)", result.translatedText)
        assertEquals("High (Offline Match)", result.confidence)
        assertFalse(result.requiresReview)
        assertEquals("exact", result.matchType)
    }

    @Test
    fun testPunctuationNormalization() {
        val result = translationEngine.translate("नमस्ते!", Language.HINDI, Language.SANTALI)
        assertTrue(result.matched)
        assertEquals("Johar (जोहार)", result.translatedText)
    }

    @Test
    fun testWhitespaceNormalization() {
        val result = translationEngine.translate("   नमस्ते   ", Language.HINDI, Language.SANTALI)
        assertTrue(result.matched)
        assertEquals("Johar (जोहार)", result.translatedText)
    }

    @Test
    fun testCaseNormalization() {
        val result = translationEngine.translate("jOhAr", Language.SANTALI, Language.HINDI)
        assertTrue(result.matched)
        assertEquals("नमस्ते (Namaste)", result.translatedText)
    }

    @Test
    fun testUnknownPhrase() {
        val result = translationEngine.translate("ये क्या है?", Language.HINDI, Language.SANTALI)
        assertFalse(result.matched)
        assertEquals("Translation not available offline", result.translatedText)
        assertEquals("No match", result.confidence)
        assertTrue(result.requiresReview)
        assertEquals("none", result.matchType)
    }

    @Test
    fun testConfidenceLevels() {
        // Exact match
        val exact = translationEngine.translate("नमस्ते", Language.HINDI, Language.SANTALI)
        assertEquals("High (Offline Match)", exact.confidence)

        // Word-level fallback
        val fallback = translationEngine.translate("शिक्षक छात्र", Language.HINDI, Language.SANTALI)
        assertTrue(fallback.matched)
        assertEquals("Macet’ (माचेत’) Paṛhua (पढ़ुआ)", fallback.translatedText)
        assertEquals("Low (Word-level fallback)", fallback.confidence)
        assertTrue(fallback.requiresReview)

        // No match
        val none = translationEngine.translate("अपरिचित वाक्य", Language.HINDI, Language.SANTALI)
        assertEquals("No match", none.confidence)
    }

    @Test
    fun testTeacherCorrectionAndMemoryRetrieval() {
        // Translate a sentence that is not in database
        val originalResult = translationEngine.translate("यह एक मेज है", Language.HINDI, Language.SANTALI)
        assertFalse(originalResult.matched)

        // Teacher corrects the translation
        val correction = TranslationEntry(
            sourceText = "यह एक मेज है",
            targetText = "Nia mit’ tebol kana",
            sourceLang = Language.HINDI,
            targetLang = Language.SANTALI,
            category = "Teacher Correction"
        )
        translationEngine.addCorrection(correction)

        // Verify corrected translation is retrieved with High validation confidence
        val newResult = translationEngine.translate("यह एक मेज है", Language.HINDI, Language.SANTALI)
        assertTrue(newResult.matched)
        assertEquals("Nia mit’ tebol kana", newResult.translatedText)
        assertEquals("High (Teacher Validated)", newResult.confidence)
        assertFalse(newResult.requiresReview)
    }

    @Test
    fun testDuplicateCorrectionHandling() {
        val correction1 = TranslationEntry("किताब", "Pustak", Language.HINDI, Language.SANTALI, "Teacher Correction")
        val correction2 = TranslationEntry("किताब", "Puthī", Language.HINDI, Language.SANTALI, "Teacher Correction")

        translationEngine.addCorrection(correction1)
        translationEngine.addCorrection(correction2)

        // Ensure duplicate is removed and only the latest exists in memory
        val result = translationEngine.translate("किताब", Language.HINDI, Language.SANTALI)
        assertEquals("Puthī", result.translatedText)
        assertEquals(1, translationMemory.getEntries().size)
    }

    @Test
    fun testEmptyInput() {
        val result = translationEngine.translate("   ", Language.HINDI, Language.SANTALI)
        assertFalse(result.matched)
        assertEquals("", result.translatedText)
        assertEquals("No match", result.confidence)
        assertFalse(result.requiresReview)
    }
}
