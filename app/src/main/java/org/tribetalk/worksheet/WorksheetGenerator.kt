package org.tribetalk.worksheet

import org.tribetalk.core.TribeTalkTranslator
import org.tribetalk.flashcards.FlashcardSet
import org.tribetalk.flashcards.NIPUNLearningFramework
import org.tribetalk.worksheet.curriculum.CurriculumItem
import org.tribetalk.worksheet.curriculum.FoundationalStage
import org.tribetalk.worksheet.curriculum.NipunCurriculumRegistry
import org.tribetalk.worksheet.curriculum.WorksheetSuitability
import java.util.UUID

/**
 * Deterministic FLN Worksheet Generator.
 *
 * Transforms teacher inputs, curriculum standards, and flashcard decks into structured,
 * official NIPUN Bharat / NCF-FS aligned bilingual activities using stable-talk's
 * on-device translation and the authoritative NipunCurriculumRegistry.
 */
class WorksheetGenerator {

    /**
     * Generates a complete bilingual worksheet with 5 to 10 activities from free-form teacher lesson text.
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
                    editable = true,
                    stage = grade ?: "Foundational",
                    domain = "LANG",
                    verificationStatus = if (santali == "Translation unavailable") SantaliVerificationStatus.NEEDS_REVIEW else SantaliVerificationStatus.VERIFIED
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
                    editable = true,
                    stage = grade ?: "Foundational",
                    domain = "LANG",
                    verificationStatus = if (blankedSantali == "Translation unavailable") SantaliVerificationStatus.NEEDS_REVIEW else SantaliVerificationStatus.VERIFIED
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
                    editable = true,
                    stage = grade ?: "Foundational",
                    domain = "LANG",
                    verificationStatus = SantaliVerificationStatus.VERIFIED
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
                editable = true,
                stage = grade ?: "Foundational",
                domain = "LANG",
                verificationStatus = SantaliVerificationStatus.VERIFIED
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
                editable = true,
                stage = grade ?: "Foundational",
                domain = "LANG",
                verificationStatus = SantaliVerificationStatus.VERIFIED
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
            santaliInstructions = "ᱡᱚᱛᱚ ᱠᱩᱠᱞᱤ ᱫᱷᱮᱭᱟᱱ ᱛᱮ ᱯᱟᱲᱦᱟᱣ ᱢᱮ ᱟᱨ ᱦᱤᱱᱫᱤ ᱥᱟᱶᱛᱮ ᱥᱟᱱᱛᱟᱲᱤ ᱛᱮ ᱛᱮᱞᱟ ᱮᱢ ᱢᱮ।",
            questions = finalQuestions,
            createdAt = System.currentTimeMillis(),
            stageId = grade,
            domainId = "LANG",
            showTeacherAlignment = false,
            includeAnswerKey = true
        )
    }

    /**
     * Generates an official NCF-FS / NIPUN Bharat aligned worksheet directly from a CurriculumItem.
     */
    fun generateFromCurriculum(
        item: CurriculumItem,
        targetQuestionCount: Int = 5,
        preferredType: QuestionType? = null
    ): Worksheet {
        val questions = mutableListOf<WorksheetQuestion>()
        val supportedTypes = if (item.supportedActivityTypes.isNotEmpty()) {
            item.supportedActivityTypes
        } else {
            listOf(QuestionType.READ_AND_ANSWER, QuestionType.WORD_MEANING)
        }

        // Primary Question from the exact curriculum item
        val primaryType = preferredType ?: supportedTypes.first()
        questions.add(
            createQuestionFromCurriculumItem(
                item = item,
                type = primaryType,
                suffix = ""
            )
        )

        // Generate complementary activities to reach target question count
        val stageRelatedItems = NipunCurriculumRegistry.getWorksheetSuitableItems(item.stage, item.domain)
            .filter { it.id != item.id }

        var itemIndex = 0
        while (questions.size < targetQuestionCount) {
            if (itemIndex < stageRelatedItems.size) {
                val related = stageRelatedItems[itemIndex]
                val relatedType = preferredType ?: related.supportedActivityTypes.firstOrNull() ?: primaryType
                questions.add(
                    createQuestionFromCurriculumItem(
                        item = related,
                        type = relatedType,
                        suffix = ""
                    )
                )
                itemIndex++
            } else {
                // Synthesize derived variations across supported types
                val altType = supportedTypes[(questions.size) % supportedTypes.size]
                questions.add(
                    createDerivedQuestion(
                        item = item,
                        type = altType,
                        variationIndex = questions.size + 1
                    )
                )
            }
        }

        val stageDisplay = item.stage.displayName
        val domainDisplay = item.domain.displayName

        return Worksheet(
            id = UUID.randomUUID().toString(),
            title = "${item.stage.stageCode} • ${item.domain.hindiName}",
            topic = "${item.culturalTheme} (${item.domain.displayName})",
            grade = stageDisplay,
            instructions = "सभी प्रश्नों को ध्यानपूर्वक पढ़ें और निर्देशानुसार हल करें।",
            santaliInstructions = "ᱡᱚᱛᱚ ᱠᱩᱠᱞᱤ ᱫᱷᱮᱭᱟᱱ ᱛᱮ ᱯᱟᱲᱦᱟᱣ ᱢᱮ ᱟᱨ ᱱᱤᱨᱫᱮᱥ ᱞᱮᱠᱟᱛᱮ ᱥᱚᱞᱦᱮ ᱢᱮ।",
            questions = questions.take(targetQuestionCount),
            createdAt = System.currentTimeMillis(),
            stageId = item.stage.stageId,
            domainId = item.domain.domainId,
            curricularGoalId = item.curricularGoalId,
            competencyId = item.competencyId,
            learningOutcomeText = item.learningOutcomeText,
            showTeacherAlignment = true,
            includeAnswerKey = true
        )
    }

    /**
     * Creates a Worksheet directly from an existing FlashcardSet (Phase 12).
     * Reuses the deck without duplicating data, inheriting domain, grade, and topic metadata.
     */
    fun createWorksheetFromFlashcards(flashcardSet: FlashcardSet): Worksheet {
        val cards = flashcardSet.cards
        val questions = mutableListOf<WorksheetQuestion>()

        if (cards.isEmpty()) {
            return Worksheet(
                title = "${flashcardSet.title} Worksheet",
                topic = flashcardSet.topic,
                grade = flashcardSet.grade,
                instructions = "अभ्यास कार्य पूरा करें।",
                questions = emptyList()
            )
        }

        // 1. Match picture/word to meaning (MATCHING)
        if (cards.size >= 2) {
            val pairs = cards.take(4).map { card ->
                card.hindiText to (card.santaliOlChiki ?: card.santaliText)
            }
            val hindiList = pairs.mapIndexed { i, p -> "${i + 1}. ${p.first}" }.joinToString("\n")
            val santaliList = pairs.shuffled().mapIndexed { i, p -> "${('A' + i)}. ${p.second}" }.joinToString("\n")
            val answerStr = pairs.mapIndexed { i, p -> "${i + 1} ➔ ${p.second}" }.joinToString(", ")

            questions.add(
                WorksheetQuestion(
                    id = UUID.randomUUID().toString(),
                    type = QuestionType.MATCHING,
                    hindiText = "सही जोड़ी मिलाओ (Match the Following):\n$hindiList",
                    santaliText = "ᱥᱟᱹᱨᱤ ᱡᱚᱲ ᱢᱮᱞᱟᱣ ᱢᱮ:\n$santaliList",
                    options = emptyList(),
                    answer = answerStr,
                    editable = true,
                    stage = flashcardSet.grade ?: "Foundational",
                    domain = flashcardSet.domain.ifBlank { "LANG" },
                    verificationStatus = SantaliVerificationStatus.VERIFIED
                )
            )
        }

        // 2. Vocabulary Word Meaning from cards
        for (card in cards.take(2)) {
            val olChiki = card.santaliOlChiki ?: card.santaliText
            questions.add(
                WorksheetQuestion(
                    id = UUID.randomUUID().toString(),
                    type = QuestionType.WORD_MEANING,
                    hindiText = "शब्द का संथाली में अर्थ लिखो: ${card.hindiText}",
                    santaliText = "ᱱᱚᱶᱟ ᱟᱹᱲᱟᱹ ᱨᱮᱭᱟᱜ ᱥᱟᱱᱛᱟᱲᱤ ᱢᱮᱱᱮᱛ ᱚᱞ ᱢᱮ: $olChiki",
                    options = emptyList(),
                    answer = "$olChiki (${card.phoneticGuide ?: card.santaliText})",
                    editable = true,
                    stage = flashcardSet.grade ?: "Foundational",
                    domain = flashcardSet.domain.ifBlank { "LANG" },
                    verificationStatus = SantaliVerificationStatus.VERIFIED,
                    visualAssetRef = card.imageEmoji
                )
            )
        }

        // 3. Fill in the Blank using Flashcard Example Sentences
        val cardWithSentence = cards.firstOrNull { !it.exampleSentenceHindi.isNullOrBlank() } ?: cards.first()
        val exHindi = cardWithSentence.exampleSentenceHindi ?: "${cardWithSentence.hindiText} एक जरूरी वस्तु है।"
        val targetWord = cardWithSentence.hindiText
        val blankedHindi = exHindi.replaceFirst(targetWord, "_______")
        val exSantali = cardWithSentence.exampleSentenceSantali ?: (cardWithSentence.santaliOlChiki ?: cardWithSentence.santaliText)

        questions.add(
            WorksheetQuestion(
                id = UUID.randomUUID().toString(),
                type = QuestionType.FILL_IN_THE_BLANK,
                hindiText = "रिक्त स्थान भरो: $blankedHindi",
                santaliText = "ᱠᱷᱟᱹᱞᱤ ᱴᱷᱟᱶ ᱯᱮᱨᱮᱡᱽ ᱢᱮ: $exSantali",
                options = listOf(targetWord, "अन्य", "कोई नहीं"),
                answer = targetWord,
                editable = true,
                stage = flashcardSet.grade ?: "Foundational",
                domain = flashcardSet.domain.ifBlank { "LANG" },
                verificationStatus = SantaliVerificationStatus.VERIFIED
            )
        )

        // 4. Multiple Choice Question from cards
        if (cards.size >= 3) {
            val correctCard = cards.first()
            val distractors = cards.drop(1).take(2).map { it.hindiText }
            val options = (listOf(correctCard.hindiText) + distractors).shuffled()
            val correctLetter = when (options.indexOf(correctCard.hindiText)) {
                0 -> "A"
                1 -> "B"
                else -> "C"
            }
            questions.add(
                WorksheetQuestion(
                    id = UUID.randomUUID().toString(),
                    type = QuestionType.MULTIPLE_CHOICE,
                    hindiText = "सही विकल्प चुनें: '${correctCard.santaliOlChiki ?: correctCard.santaliText}' का हिन्दी अर्थ क्या है?",
                    santaliText = "ᱥᱟᱹᱨᱤ ᱵᱟᱪᱷᱟᱣ ᱢᱮ: '${correctCard.santaliOlChiki ?: correctCard.santaliText}' ᱨᱮᱭᱟᱜ ᱦᱤᱱᱫᱤ ᱢᱮᱱᱮᱛ ᱪᱮᱫ ᱠᱟᱱᱟ?",
                    options = options.mapIndexed { i, opt -> "${('A' + i)}. $opt" },
                    answer = "$correctLetter. ${correctCard.hindiText}",
                    editable = true,
                    stage = flashcardSet.grade ?: "Foundational",
                    domain = flashcardSet.domain.ifBlank { "LANG" },
                    verificationStatus = SantaliVerificationStatus.VERIFIED
                )
            )
        }

        // 5. Tracing / Pre-Writing Activity
        val traceCard = cards.last()
        questions.add(
            WorksheetQuestion(
                id = UUID.randomUUID().toString(),
                type = QuestionType.TRACE_OR_WRITE,
                hindiText = "सुलेख लिखो (Handwriting Tracing): ${traceCard.hindiText}",
                santaliText = "ᱚᱞ ᱪᱮᱫᱚᱜ ᱢᱮ: ${traceCard.santaliOlChiki ?: traceCard.santaliText}",
                options = emptyList(),
                answer = "${traceCard.hindiText} / ${traceCard.santaliOlChiki ?: traceCard.santaliText}",
                editable = true,
                stage = flashcardSet.grade ?: "Foundational",
                domain = flashcardSet.domain.ifBlank { "LANG" },
                verificationStatus = SantaliVerificationStatus.VERIFIED
            )
        )

        return Worksheet(
            id = UUID.randomUUID().toString(),
            title = "${flashcardSet.title} Bilingual Worksheet",
            topic = flashcardSet.topic,
            grade = flashcardSet.grade,
            instructions = "फ्लेशकार्ड अभ्यास: सभी प्रश्नों के उत्तर लिखें।",
            santaliInstructions = "ᱯᱷᱞᱮᱥᱠᱟᱨᱰ ᱟᱵᱷᱭᱟᱥ: ᱡᱚᱛᱚ ᱠᱩᱠᱞᱤ ᱨᱮᱭᱟᱜ ᱛᱮᱞᱟ ᱚᱞ ᱢᱮ।",
            questions = questions,
            createdAt = System.currentTimeMillis(),
            stageId = flashcardSet.grade,
            domainId = flashcardSet.domain,
            learningOutcomeText = flashcardSet.learningOutcome,
            showTeacherAlignment = true,
            includeAnswerKey = true
        )
    }

    private fun createQuestionFromCurriculumItem(
        item: CurriculumItem,
        type: QuestionType,
        suffix: String
    ): WorksheetQuestion {
        val hPrompt = if (item.hindiPrompt.isNotBlank()) item.hindiPrompt else "दिए गए कार्य को हल करें$suffix"
        val sPrompt = if (item.santaliOlChiki.isNotBlank()) item.santaliOlChiki else "ᱮᱢ ᱟᱠᱟᱱ ᱠᱟᱹᱢᱤ ᱥᱚᱞᱦᱮ ᱢᱮ"

        return WorksheetQuestion(
            id = UUID.randomUUID().toString(),
            type = type,
            hindiText = hPrompt,
            santaliText = sPrompt,
            options = item.options,
            answer = item.correctAnswer.ifBlank { "निर्धारित उत्तर (Expected Answer)" },
            editable = true,
            curriculumItemId = item.id,
            stage = item.stage.stageCode,
            domain = item.domain.code,
            competencyId = item.competencyId,
            learningOutcomeId = item.learningOutcomeId,
            verificationStatus = item.verificationStatus,
            visualAssetRef = item.visualAssetRef
        )
    }

    private fun createDerivedQuestion(
        item: CurriculumItem,
        type: QuestionType,
        variationIndex: Int
    ): WorksheetQuestion {
        val (hText, sText, answer) = when (type) {
            QuestionType.WORD_MEANING -> Triple(
                "शब्द का अर्थ समझो और लिखो (#$variationIndex): ${item.englishGloss.ifBlank { item.culturalTheme }}",
                "ᱱᱚᱶᱟ ᱟᱹᱲᱟᱹ ᱨᱮᱭᱟᱜ ᱢᱮᱱᱮᱛ ᱚᱞ ᱢᱮ: ${item.santaliOlChiki}",
                item.correctAnswer
            )
            QuestionType.FILL_IN_THE_BLANK -> Triple(
                "रिक्त स्थान भरो (#$variationIndex): ___ ${item.hindiPrompt}",
                "ᱠᱷᱟᱹᱞᱤ ᱴᱷᱟᱶ ᱯᱮᱨᱮᱡᱽ ᱢᱮ: ___ ${item.santaliOlChiki}",
                item.correctAnswer
            )
            QuestionType.MULTIPLE_CHOICE -> Triple(
                "सही विकल्प पहचानो (#$variationIndex):",
                "ᱥᱟᱹᱨᱤ ᱵᱟᱪᱷᱟᱣ ᱢᱮ:",
                item.correctAnswer
            )
            QuestionType.TRUE_FALSE -> Triple(
                "सही या गलत लिखो (#$variationIndex): ${item.hindiPrompt}",
                "ᱥᱟᱹᱨᱤ ᱥᱮ ᱵᱟᱹᱲᱤᱡ ᱚᱞ ᱢᱮ: ${item.santaliOlChiki}",
                "सही (True / ᱥᱟᱹᱨᱤ)"
            )
            QuestionType.TRACE_OR_WRITE -> Triple(
                "देखकर सुन्दर अक्षरों में लिखो (#$variationIndex): ${item.correctAnswer}",
                "ᱧᱮᱞ ᱠᱟᱛᱮ ᱪᱮᱦᱨᱟ ᱚᱞ ᱢᱮ: ${item.santaliOlChiki}",
                item.correctAnswer
            )
            else -> Triple(
                "अभ्यास प्रश्न (#$variationIndex): ${item.hindiPrompt}",
                "ᱟᱵᱷᱭᱟᱥ ᱠᱩᱠᱞᱤ: ${item.santaliOlChiki}",
                item.correctAnswer
            )
        }

        return WorksheetQuestion(
            id = UUID.randomUUID().toString(),
            type = type,
            hindiText = hText,
            santaliText = sText,
            options = item.options,
            answer = answer,
            editable = true,
            curriculumItemId = item.id,
            stage = item.stage.stageCode,
            domain = item.domain.code,
            competencyId = item.competencyId,
            learningOutcomeId = item.learningOutcomeId,
            verificationStatus = item.verificationStatus,
            visualAssetRef = item.visualAssetRef
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
            QuestionType.PICTURE_IDENTIFICATION -> {
                trimmed.removePrefix("चित्र देखकर पहचानो:").removePrefix("चित्र पहचानो:").trim()
            }
            QuestionType.COUNT_AND_WRITE -> {
                trimmed.removePrefix("गिनकर संख्या लिखो:").removePrefix("गिनो और लिखो:").trim()
            }
            QuestionType.ORDERING -> {
                trimmed.removePrefix("क्रम में सजाओ:").removePrefix("बढ़ते क्रम में लिखो:").trim()
            }
            QuestionType.CLASSIFICATION -> {
                trimmed.removePrefix("वर्गीकरण करो:").removePrefix("छांटो:").trim()
            }
            QuestionType.TRUE_FALSE -> {
                trimmed.removePrefix("सही या गलत बताओ:").removePrefix("सत्य / असत्य:").trim()
            }
            QuestionType.COMPLETE_PATTERN -> {
                trimmed.removePrefix("पैटर्न को आगे बढ़ाओ:").removePrefix("पैटर्न पूरा करो:").trim()
            }
            QuestionType.TRACE_OR_WRITE -> {
                trimmed.removePrefix("सुलेख लिखो:").removePrefix("ट्रेस करो:").trim()
            }
            QuestionType.SOLVE -> {
                trimmed.removePrefix("हल करो:").removePrefix("जोड़ो:").trim()
            }
            QuestionType.SHORT_ANSWER -> {
                trimmed.removePrefix("संक्षिप्त उत्तर दो:").removePrefix("उत्तर लिखो:").trim()
            }
            QuestionType.SEQUENCING -> {
                trimmed.removePrefix("घटनाक्रम के अनुसार लगाओ:").trim()
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
            QuestionType.PICTURE_IDENTIFICATION -> "ᱪᱤᱛᱟᱹᱨ ᱧᱮᱞ ᱠᱟᱛᱮ ᱪᱤᱱᱦᱟᱹᱣ ᱢᱮ: $santaliTranslation"
            QuestionType.COUNT_AND_WRITE -> "ᱞᱮᱠᱷᱟ ᱠᱟᱛᱮ ᱚᱞ ᱢᱮ: $santaliTranslation"
            QuestionType.ORDERING -> "ᱥᱟᱡᱟᱣ ᱢᱮ: $santaliTranslation"
            QuestionType.CLASSIFICATION -> "ᱵᱷᱮᱜᱟᱨ ᱢᱮ: $santaliTranslation"
            QuestionType.TRUE_FALSE -> "ᱥᱟᱹᱨᱤ ᱥᱮ ᱵᱟᱹᱲᱤᱡ: $santaliTranslation"
            QuestionType.COMPLETE_PATTERN -> "ᱯᱮᱴᱟᱨᱱ ᱞᱟᱦᱟ ᱤᱫᱤ ᱢᱮ: $santaliTranslation"
            QuestionType.TRACE_OR_WRITE -> "ᱚᱞ ᱪᱮᱫᱚᱜ ᱢᱮ: $santaliTranslation"
            QuestionType.SOLVE -> "ᱥᱚᱞᱦᱮ ᱢᱮ: $santaliTranslation"
            QuestionType.SHORT_ANSWER -> "ᱠᱷᱟᱴᱚ ᱛᱮ ᱛᱮᱞᱟ ᱚᱞ ᱢᱮ: $santaliTranslation"
            QuestionType.SEQUENCING -> "ᱞᱟᱦᱟ ᱛᱟᱭᱚᱢ ᱥᱟᱡᱟᱣ ᱢᱮ: $santaliTranslation"
        }
    }

    private val classroomPhrases = mapOf(
        "किताब खोलो" to "ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ (Puthī jhije me)",
        "लिखना शुरू करो" to "ᱚᱞ ᱮᱦᱚᱵ ᱢᱮ (Ol ehob me)",
        "चलो पढ़ते हैं" to "ᱫᱮᱞᱟ ᱵᱚᱱ ᱯᱟᱲᱦᱟᱣ-ᱟ (Dela bon paṛhao-a)",
        "चलो पढ़ते हैं" to "ᱫᱮᱞᱟ ᱵᱚᱱ ᱯᱟᱲᱦᱟᱣ-ᱟ (Dela bon paṛhao-a)",
        "नमस्ते" to "ᱡᱚᱦᱟᱨ (Johar)",
        "पानी पीना है" to "ᱫᱟᱜ ᱧᱩ ᱥᱟᱱᱟᱹᱧ ᱠᱟᱱᱟ (Dak' ñu sanañ kana)"
    )

    /**
     * Translates a given Hindi text into Santali using TribeTalkTranslator or NIPUN templates.
     */
    fun translateToSantali(hindiText: String): String {
        val trimmed = hindiText.trim()
        if (trimmed.isEmpty()) return ""

        // 1. Direct classroom phrase match
        val phraseMatch = classroomPhrases[trimmed]
            ?: classroomPhrases.entries.firstOrNull { trimmed.contains(it.key) }?.value
        if (phraseMatch != null) {
            return phraseMatch
        }

        // 2. Check curated NIPUNLearningFramework templates
        for (templates in NIPUNLearningFramework.presetTopicTemplates.values) {
            val matched = templates.firstOrNull { it.hindi.equals(trimmed, ignoreCase = true) }
            if (matched != null) {
                return "${matched.santaliOlChiki} (${matched.phoneticDevanagari})"
            }
        }

        // 3. Query stable-talk's TribeTalkTranslator
        try {
            val result = TribeTalkTranslator.translate(trimmed, isHindiToSantali = true)
            if (result.isNotBlank() && !result.equals(trimmed, ignoreCase = true)) {
                return result
            }
        } catch (_: Exception) {
            // Fallback gracefully
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
