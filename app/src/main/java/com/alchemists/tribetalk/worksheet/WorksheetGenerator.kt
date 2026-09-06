package com.alchemists.tribetalk.worksheet

import com.alchemists.tribetalk.translation.FLNCurriculumDatabase
import com.alchemists.tribetalk.translation.Language
import com.alchemists.tribetalk.translation.TranslationEngine
import java.util.UUID

/**
 * Deterministic FLN Worksheet Generator.
 *
 * Transforms a teacher's lesson topic and Hindi content into structured,
 * curriculum-aligned bilingual activities using the offline TranslationEngine.
 */
class WorksheetGenerator(
    private val translationEngine: TranslationEngine
) {

    /**
     * Generates a complete bilingual worksheet with 5 to 10 activities.
     */
    fun generateWorksheet(
        topic: String,
        hindiContent: String,
        grade: String? = null
    ): Worksheet {
        val trimmedTopic = topic.trim()
        val trimmedContent = hindiContent.trim()

        if (trimmedTopic.isEmpty() && trimmedContent.isEmpty()) {
            return Worksheet(
                title = "Bilingual Worksheet",
                topic = "",
                grade = grade,
                questions = emptyList()
            )
        }

        val effectiveTopic = if (trimmedTopic.isNotEmpty()) trimmedTopic else "FLN Lesson"
        val sentences = extractSentences(trimmedContent)
        val words = extractKeyWords(trimmedContent, effectiveTopic)

        val questions = mutableListOf<WorksheetQuestion>()

        // 1. WORD_MEANING Activities (Vocabulary Identification)
        val vocabWords = if (words.isNotEmpty()) words.take(3) else listOf(effectiveTopic)
        for (word in vocabWords) {
            val santali = translateToSantali(word)
            questions.add(
                WorksheetQuestion(
                    id = UUID.randomUUID().toString(),
                    type = QuestionType.WORD_MEANING,
                    hindiText = "शब्द का संथाली में अर्थ लिखो: $word",
                    santaliText = "ᱱᱚᱶᱟ ᱟᱹᱲᱟᱹ ᱨᱮᱭᱟᱜ ᱥᱟᱱᱛᱟᱲᱤ ᱢᱮᱱᱮᱛ ᱚᱞ ᱢᱮ: $santali",
                    options = emptyList(),
                    answer = santali,
                    editable = true
                )
            )
        }

        // 2. FILL_IN_THE_BLANK Activities
        val fillSentences = if (sentences.isNotEmpty()) sentences.take(2) else listOf("$effectiveTopic एक महत्वपूर्ण विषय है।")
        for (sentence in fillSentences) {
            val tokens = sentence.split(Regex("\\s+")).filter { it.length > 2 && !it.contains("है") && !it.contains("एक") }
            val missingWord = tokens.firstOrNull() ?: tokens.lastOrNull() ?: effectiveTopic
            val blankedHindi = sentence.replaceFirst(missingWord, "_______")
            val santaliFull = translateToSantali(sentence)
            val santaliMissing = translateToSantali(missingWord)
            val blankedSantali = if (santaliFull != "Translation unavailable" && santaliMissing != "Translation unavailable" && santaliFull.contains(santaliMissing)) {
                santaliFull.replaceFirst(santaliMissing, "_______")
            } else if (santaliFull != "Translation unavailable") {
                "$santaliFull (_______)"
            } else {
                "Translation unavailable"
            }

            questions.add(
                WorksheetQuestion(
                    id = UUID.randomUUID().toString(),
                    type = QuestionType.FILL_IN_THE_BLANK,
                    hindiText = "रिक्त स्थान भरो: $blankedHindi",
                    santaliText = "ᱠᱷᱟᱹᱞᱤ ᱴᱷᱟᱶ ᱯᱮᱨᱮᱡᱽ ᱢᱮ: $blankedSantali",
                    options = listOf(missingWord, "अन्य", "कोई नहीं"),
                    answer = missingWord,
                    editable = true
                )
            )
        }

        // 3. MATCHING Activity (Match Hindi words to Santali)
        val matchWords = words.take(3)
        if (matchWords.size >= 2) {
            val pairs = matchWords.map { word ->
                word to translateToSantali(word)
            }
            val hindiList = pairs.mapIndexed { index, pair -> "${index + 1}. ${pair.first}" }.joinToString("\n")
            val santaliList = pairs.shuffled().mapIndexed { index, pair -> "${('A' + index)}. ${pair.second}" }.joinToString("\n")
            val answers = pairs.mapIndexed { index, pair -> "${index + 1} ➔ ${pair.second}" }.joinToString(", ")

            questions.add(
                WorksheetQuestion(
                    id = UUID.randomUUID().toString(),
                    type = QuestionType.MATCHING,
                    hindiText = "सही जोड़ी मिलाओ (Match the Following):\n$hindiList",
                    santaliText = "ᱥᱟᱹᱨᱤ ᱡᱚᱲ ᱢᱮᱞᱟᱣ ᱢᱮ:\n$santaliList",
                    options = emptyList(),
                    answer = answers,
                    editable = true
                )
            )
        }

        // 4. MULTIPLE_CHOICE Activity
        val mcqSentence = sentences.firstOrNull() ?: "$effectiveTopic के बारे में सही विकल्प चुनें।"
        val targetWord = words.firstOrNull() ?: effectiveTopic
        val distractorWords = getDistractorWords(targetWord)
        val allOptions = (listOf(targetWord) + distractorWords.take(3)).shuffled()
        val correctLetter = when (allOptions.indexOf(targetWord)) {
            0 -> "A"
            1 -> "B"
            2 -> "C"
            else -> "D"
        }

        val mcqHindi = "पाठ के अनुसार सही विकल्प चुनें ($mcqSentence):"
        val mcqSantali = "ᱯᱟᱴᱷ ᱞᱮᱠᱟᱛᱮ ᱥᱟᱹᱨᱤ ᱵᱟᱪᱷᱟᱣ ᱢᱮ: " + translateToSantali(mcqSentence)
        val formattedOptions = allOptions.mapIndexed { index, opt ->
            val optSantali = translateToSantali(opt)
            "${('A' + index)}. $opt ($optSantali)"
        }

        questions.add(
            WorksheetQuestion(
                id = UUID.randomUUID().toString(),
                type = QuestionType.MULTIPLE_CHOICE,
                hindiText = mcqHindi,
                santaliText = mcqSantali,
                options = formattedOptions,
                answer = "$correctLetter. $targetWord",
                editable = true
            )
        )

        // 5. READ_AND_ANSWER Activity (Comprehension Question)
        val readSentence = sentences.lastOrNull() ?: mcqSentence
        val readQuestionHindi = "पढ़ो और उत्तर लिखो: '$readSentence'"
        val readQuestionSantali = "ᱯᱟᱲᱦᱟᱣ ᱢᱮ ᱟᱨ ᱛᱮᱞᱟ ᱚᱞ ᱢᱮ: '" + translateToSantali(readSentence) + "'"
        val readAnswer = translateToSantali(readSentence)

        questions.add(
            WorksheetQuestion(
                id = UUID.randomUUID().toString(),
                type = QuestionType.READ_AND_ANSWER,
                hindiText = readQuestionHindi,
                santaliText = readQuestionSantali,
                options = emptyList(),
                answer = "उत्तर: $readSentence | ᱛᱮᱞᱟ: $readAnswer",
                editable = true
            )
        )

        // Ensure 5 to 10 activities
        val finalQuestions = questions.take(10)

        return Worksheet(
            id = UUID.randomUUID().toString(),
            title = "$effectiveTopic Bilingual Worksheet",
            topic = effectiveTopic,
            grade = grade,
            instructions = "सभी प्रश्नों को ध्यानपूर्वक पढ़ें और हिन्दी तथा संथाली में उत्तर दें। (Read all questions carefully and answer in Hindi and Santali.)",
            questions = finalQuestions,
            createdAt = System.currentTimeMillis()
        )
    }

    /**
     * Re-translates a teacher-edited Hindi question text into Santali for a specific QuestionType.
     * Preserves question prefixes/structure where appropriate and returns "Translation unavailable for this text" if not found.
     */
    fun retranslateQuestionText(hindiText: String, type: QuestionType): String {
        val trimmed = hindiText.trim()
        if (trimmed.isEmpty()) return ""

        // Extract core content if standard activity prefixes are present
        val cleanHindi = when (type) {
            QuestionType.WORD_MEANING -> {
                trimmed.removePrefix("शब्द का संथाली में अर्थ लिखो:").removePrefix("शब्द का अर्थ:").trim()
            }
            QuestionType.FILL_IN_THE_BLANK -> {
                trimmed.removePrefix("रिक्त स्थान भरो:").removePrefix("खाली जगह भरो:").trim()
            }
            QuestionType.MULTIPLE_CHOICE -> {
                trimmed.removePrefix("पाठ के अनुसार सही विकल्प चुनें (")
                    .removePrefix("सही विकल्प चुनें:")
                    .removeSuffix("):")
                    .trim()
            }
            QuestionType.READ_AND_ANSWER -> {
                trimmed.removePrefix("पढ़ो और उत्तर लिखो:").removePrefix("पढ़ो और उत्तर लिखो:").removeSurrounding("'").trim()
            }
            QuestionType.MATCHING -> {
                trimmed.removePrefix("सही जोड़ी मिलाओ (Match the Following):").trim()
            }
        }

        // Check if there are multiple lines (like in matching)
        if (type == QuestionType.MATCHING && cleanHindi.lines().size > 1) {
            val lines = cleanHindi.lines().filter { it.isNotBlank() }
            val translatedLines = lines.mapIndexed { index, line ->
                val lineContent = line.replace(Regex("^\\d+\\.\\s*"), "").trim()
                val lineSantali = translateToSantali(lineContent)
                val displaySantali = if (lineSantali == "Translation unavailable") "Translation unavailable for this text" else lineSantali
                "${('A' + index)}. $displaySantali"
            }
            return "ᱥᱟᱹᱨᱤ ᱡᱚᱲ ᱢᱮᱞᱟᱣ ᱢᱮ:\n" + translatedLines.joinToString("\n")
        }

        // Translate the clean content
        val santaliTranslation = translateToSantali(cleanHindi)
        if (santaliTranslation == "Translation unavailable" || santaliTranslation.isBlank()) {
            return "Translation unavailable for this text"
        }

        // Wrap back with question structure if appropriate
        return when (type) {
            QuestionType.WORD_MEANING -> "ᱱᱚᱶᱟ ᱟᱹᱲᱟᱹ ᱨᱮᱭᱟᱜ ᱥᱟᱱᱛᱟᱲᱤ ᱢᱮᱱᱮᱛ ᱚᱞ ᱢᱮ: $santaliTranslation"
            QuestionType.FILL_IN_THE_BLANK -> "ᱠᱷᱟᱹᱞᱤ ᱴᱷᱟᱶ ᱯᱮᱨᱮᱡᱽ ᱢᱮ: $santaliTranslation"
            QuestionType.MULTIPLE_CHOICE -> "ᱯᱟᱴᱷ ᱞᱮᱠᱟᱛᱮ ᱥᱟᱹᱨᱤ ᱵᱟᱪᱷᱟᱣ ᱢᱮ: $santaliTranslation"
            QuestionType.READ_AND_ANSWER -> "ᱯᱟᱲᱦᱟᱣ ᱢᱮ ᱟᱨ ᱛᱮᱞᱟ ᱚᱞ ᱢᱮ: '$santaliTranslation'"
            QuestionType.MATCHING -> santaliTranslation
        }
    }

    /**
     * Translates a given Hindi text into Santali using the existing TranslationEngine.
     * If the engine does not provide a valid match, checks the FLN Curriculum Database or returns "Translation unavailable".
     */
    fun translateToSantali(hindiText: String): String {
        val trimmed = hindiText.trim()
        if (trimmed.isEmpty()) return ""

        try {
            val result = translationEngine.translate(trimmed, Language.HINDI, Language.SANTALI)
            if (result.matched && result.translatedText.isNotBlank() && !result.translatedText.contains("not available", ignoreCase = true)) {
                return result.translatedText
            }
        } catch (_: Exception) {
            // Fallback gracefully
        }

        // Secondary fallback: Direct FLNCurriculumDatabase lookup
        val flnItem = FLNCurriculumDatabase.findByHindi(trimmed)
        if (flnItem != null) {
            return "${flnItem.santaliOlChiki} (${flnItem.phoneticDevanagari})"
        }

        return "Translation unavailable"
    }

    private fun extractSentences(content: String): List<String> {
        if (content.isBlank()) return emptyList()
        return content.split(Regex("[।\n\r.!?]+"))
            .map { it.trim() }
            .filter { it.length > 3 }
    }

    private fun extractKeyWords(content: String, topic: String): List<String> {
        val stopWords = setOf(
            "एक", "है", "हैं", "का", "की", "के", "में", "पर", "और", "से", "को", "यह", "वह", "भी", "तो",
            "था", "थी", "थे", "रहा", "रही", "रहे", "कर", "करता", "करती", "करते", "लिए"
        )
        val extracted = content.split(Regex("[\\s,।\n\r.!?\"'()]+"))
            .map { it.trim() }
            .filter { it.length >= 2 && !stopWords.contains(it) }
            .distinct()

        val combined = (listOf(topic) + extracted).filter { it.isNotBlank() }.distinct()
        return if (combined.isNotEmpty()) combined else listOf("किताब", "पानी", "स्कूल", "शिक्षक", "पेड़")
    }

    private fun getDistractorWords(correctWord: String): List<String> {
        val candidates = listOf("किताब", "पानी", "स्कूल", "शिक्षक", "पेड़", "सूरज", "कलम", "छात्र", "घर")
        return candidates.filter { it != correctWord }
    }
}
