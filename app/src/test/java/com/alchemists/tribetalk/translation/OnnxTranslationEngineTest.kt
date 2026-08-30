package com.alchemists.tribetalk.translation

import org.junit.Assert.*
import org.junit.Test

class OnnxTranslationEngineTest {

    @Test
    fun testOnnxEngineInitializationWithoutContext() {
        val engine = OnnxTranslationEngine(context = null)
        // Model is absent without context, so isModelAvailable is false
        assertFalse(engine.isModelAvailable())
        assertNull(engine.translateTokens(longArrayOf(2, 10, 3)))
        engine.close()
    }
}
