package org.tribetalk.core

import org.junit.Assert.*
import org.junit.Test

class TribeTalkTranslatorTest {

    @Test
    fun testCacheLifecycleAndClearing() {
        TribeTalkTranslator.clearCache()
        assertEquals(0L, TribeTalkTranslator.cacheHits)
        assertEquals(0L, TribeTalkTranslator.cacheMisses)

        // Phonics caching test (does not require Android Context initialization)
        val sampleOlChiki = "ᱥᱟᱱᱛᱟᱲᱤ"
        val phonetic1 = TribeTalkTranslator.olChikiToSpeechPhonetics(sampleOlChiki)
        val phonetic2 = TribeTalkTranslator.olChikiToSpeechPhonetics(sampleOlChiki)

        assertNotNull(phonetic1)
        assertEquals(phonetic1, phonetic2)

        TribeTalkTranslator.clearCache()
        assertEquals(0L, TribeTalkTranslator.cacheHits)
        assertEquals(0L, TribeTalkTranslator.cacheMisses)
    }

    @Test
    fun testEmptyInputHandling() {
        val res = TribeTalkTranslator.translate("", true)
        assertEquals("", res)

        val phoneticEmpty = TribeTalkTranslator.olChikiToSpeechPhonetics("")
        assertEquals("", phoneticEmpty)
    }
}
