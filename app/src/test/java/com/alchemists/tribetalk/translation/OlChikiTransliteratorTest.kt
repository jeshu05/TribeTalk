package com.alchemists.tribetalk.translation

import org.junit.Assert.*
import org.junit.Test

class OlChikiTransliteratorTest {

    @Test
    fun testIsOlChikiDetection() {
        assertTrue(OlChikiTransliterator.isOlChiki("ᱡᱚᱦᱟᱨ"))
        assertTrue(OlChikiTransliterator.isOlChiki("ᱪᱮᱠᱟ ᱢᱮᱱᱟᱢᱟ?"))
        assertFalse(OlChikiTransliterator.isOlChiki("Johar"))
        assertFalse(OlChikiTransliterator.isOlChiki("नमस्ते"))
    }

    @Test
    fun testLatinToOlChiki() {
        assertEquals("ᱡᱚᱦᱟᱨ", OlChikiTransliterator.toOlChiki("Johar"))
        assertEquals("ᱪᱮᱠᱟ ᱢᱮᱱᱟᱢᱟ", OlChikiTransliterator.toOlChiki("Ceka menama"))
        assertEquals("ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ", OlChikiTransliterator.toOlChiki("puthi jhije me"))
    }

    @Test
    fun testDevanagariToOlChiki() {
        assertEquals("ᱡᱚᱦᱟᱨ", OlChikiTransliterator.toOlChiki("जोहार"))
        assertEquals("ᱪᱮᱠᱟ ᱢᱮᱱᱟᱢᱟ?", OlChikiTransliterator.toOlChiki("चेका मेनामा?"))
    }

    @Test
    fun testOlChikiToLatinPhonetic() {
        val latin = OlChikiTransliterator.toLatinPhonetic("ᱡᱚᱦᱟᱨ")
        assertEquals("Johar", latin)
    }

    @Test
    fun testTeacherAssistHUDPhonetics() {
        val hudText = OlChikiTransliterator.toTeacherPhoneticHUD("ᱡᱚᱦᱟᱨ")
        assertTrue(hudText.contains("ज") || hudText.contains("जोहार"))
        assertTrue(hudText.contains("Johar"))
    }

    @Test
    fun testDigitTransliteration() {
        val olChikiDigits = OlChikiTransliterator.toOlChiki("12345")
        assertEquals("᱑᱒᱓᱔᱕", olChikiDigits)
    }
}
