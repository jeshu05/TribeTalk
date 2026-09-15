package org.tribetalk.fln.pipeline

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class SvgCorpusRegistryTest {

    @Test
    fun testCorpusRegistryLookup() {
        val mango = SvgCorpusRegistry.get("mango")
        assertNotNull("Mango must be registered", mango)
        assertEquals("ᱩᱞ", mango?.santaliName)
        assertEquals("आम", mango?.hindiName)
        assertTrue("Mango must be tagged as counting token", mango?.tags?.contains("counting_token") == true)
    }

    @Test
    fun testCountingTokensFilter() {
        val tokens = SvgCorpusRegistry.getCountingTokens()
        assertTrue("Must have multiple counting tokens", tokens.size >= 5)
        val keys = tokens.map { it.objectKey }
        assertTrue("Should contain mango, sal_leaf, and pebble", keys.containsAll(listOf("mango", "sal_leaf", "pebble")))
    }

    @Test
    fun testManifestJsonParsingAndSvgExistence() {
        // Read the actual manifest.json from src/main/assets/
        val manifestFile = File("src/main/assets/fln_svg_corpus/manifest.json")
        if (manifestFile.exists()) {
            val jsonContent = manifestFile.readText()
            SvgCorpusRegistry.loadFromJsonString(jsonContent)
            
            val all = SvgCorpusRegistry.getAll()
            assertTrue("Manifest must have at least 15 assets", all.size >= 15)

            // Verify every referenced SVG file actually exists on disk
            for (asset in all) {
                val svgFile = File("src/main/assets/${asset.relativeFilePath}")
                assertTrue("SVG file must exist: ${asset.relativeFilePath}", svgFile.exists())
                assertTrue("SVG file must not be empty: ${asset.relativeFilePath}", svgFile.length() > 50)
            }
        }
    }
}
