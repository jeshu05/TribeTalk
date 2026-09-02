package com.alchemists.tribetalk.nlp

import com.alchemists.tribetalk.nlp.models.NlpResult

/**
 * Offline NLP Middleware Processor.
 * Coordinates Hindi text normalization, ASR noise cleanup, number extraction,
 * FLN domain classification, and translation readiness verification.
 */
object OfflineNlpProcessor {

    fun process(rawAsrText: String): NlpResult {
        val trimmedRaw = rawAsrText.trim()
        if (trimmedRaw.isEmpty()) {
            return NlpResult(
                originalText = "",
                normalizedText = "",
                cleanedText = "",
                domain = null,
                topic = null,
                intent = null,
                extractedNumbers = emptyList(),
                extractedNumberRange = null,
                isCompleteUtterance = false,
                isTranslationReady = false,
                confidence = 0.0f,
                reason = "Empty input text"
            )
        }

        // 1. Devanagari Unicode & Whitespace Normalization
        val normalized = HindiNormalizer.normalize(trimmedRaw)

        // 2. Number & Range Extraction
        val extractedNumbers = HindiNumberExtractor.extractNumbers(normalized)
        val extractedRange = HindiNumberExtractor.extractNumberRange(normalized)

        // 3. FLN Domain, Topic & Intent Detection
        val domain = ClassroomIntentDetector.detectDomain(normalized)
        val topic = ClassroomIntentDetector.detectTopic(normalized)
        val intent = ClassroomIntentDetector.detectIntent(normalized)

        // 4. Utterance Completeness & Translation Readiness
        val wordCount = normalized.split("\\s+".toRegex()).size
        val isComplete = wordCount >= 2 && (normalized.endsWith("।") || normalized.endsWith("?") || normalized.endsWith("!"))
        val isReady = wordCount >= 1 && normalized.isNotBlank()

        val confidence = when {
            isComplete -> 0.95f
            wordCount >= 2 -> 0.85f
            else -> 0.60f
        }

        return NlpResult(
            originalText = trimmedRaw,
            normalizedText = normalized,
            cleanedText = normalized.replace("[।,?!]".toRegex(), "").trim(),
            domain = domain,
            topic = topic,
            intent = intent,
            extractedNumbers = extractedNumbers,
            extractedNumberRange = extractedRange,
            isCompleteUtterance = isComplete,
            isTranslationReady = isReady,
            confidence = confidence,
            reason = if (isReady) "Valid Hindi utterance ready for translation" else "Incomplete text"
        )
    }
}
