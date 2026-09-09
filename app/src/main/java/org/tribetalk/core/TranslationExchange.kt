package org.tribetalk.core

/**
 * Immutable container for a single bidirectional speech/text translation exchange.
 * Constructed directly across the JNI boundary from native C++ pipeline results.
 */
data class TranslationExchange(
    val sourceText: String,
    val targetText: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val audioSamples: FloatArray,
    val totalLatencyMs: Float,
    val success: Boolean,
    val errorMessage: String = ""
) {
    val hasAudio: Boolean
        get() = audioSamples.isNotEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TranslationExchange

        if (sourceText != other.sourceText) return false
        if (targetText != other.targetText) return false
        if (sourceLanguage != other.sourceLanguage) return false
        if (targetLanguage != other.targetLanguage) return false
        if (!audioSamples.contentEquals(other.audioSamples)) return false
        if (totalLatencyMs != other.totalLatencyMs) return false
        if (success != other.success) return false
        if (errorMessage != other.errorMessage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sourceText.hashCode()
        result = 31 * result + targetText.hashCode()
        result = 31 * result + sourceLanguage.hashCode()
        result = 31 * result + targetLanguage.hashCode()
        result = 31 * result + audioSamples.contentHashCode()
        result = 31 * result + totalLatencyMs.hashCode()
        result = 31 * result + success.hashCode()
        result = 31 * result + errorMessage.hashCode()
        return result
    }
}
