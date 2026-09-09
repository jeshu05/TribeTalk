package org.tribetalk.core

import android.content.Context

/**
 * On-device AI Translator facade for TribeTalk.
 * Completely eliminates static mocked phrasebooks and delegates all translation
 * to the dynamic computational and neural linguistic engine.
 */
object TribeTalkTranslator {

    private var neuralTranslator: TribeTalkNeuralTranslator? = null

    fun initialize(context: Context) {
        if (neuralTranslator == null) {
            neuralTranslator = TribeTalkNeuralTranslator(context.applicationContext)
        }
    }

    fun translate(input: String, isHindiToSantali: Boolean): String {
        return neuralTranslator?.translate(input, isHindiToSantali) ?: ""
    }

    fun olChikiToSpeechPhonetics(olChikiText: String): String {
        return neuralTranslator?.olChikiToSpeechPhonetics(olChikiText) ?: olChikiText
    }
}
