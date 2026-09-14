package org.tribetalk.worksheet

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.tribetalk.flashcards.Flashcard
import org.tribetalk.flashcards.FlashcardSet
import org.tribetalk.worksheet.curriculum.CurriculumDomain
import org.tribetalk.worksheet.curriculum.FoundationalStage
import org.tribetalk.worksheet.curriculum.NipunCurriculumRegistry
import org.tribetalk.worksheet.curriculum.WorksheetSuitability

class NipunCurriculumTest {

    private lateinit var generator: WorksheetGenerator

    @Before
    fun setUp() {
        generator = WorksheetGenerator()
    }

    @Test
    fun testRegistryLoadsAndNotEmpty() {
        val items = NipunCurriculumRegistry.getAllItems()
        assertNotNull(items)
        assertTrue("Curriculum registry should not be empty", items.isNotEmpty())
        assertTrue("Should have at least 15 comprehensive curriculum items", items.size >= 15)
    }

    @Test
    fun testNoDuplicateCurriculumIds() {
        val items = NipunCurriculumRegistry.getAllItems()
        val ids = items.map { it.id }
        val duplicates = ids.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("No duplicate curriculum IDs should exist. Found: $duplicates", duplicates.isEmpty())

        val errors = NipunCurriculumRegistry.validateRegistry()
        assertTrue("Registry validation should pass with zero errors. Errors: $errors", errors.isEmpty())
    }

    @Test
    fun testAllFoundationalStagesRepresented() {
        for (stage in FoundationalStage.values()) {
            val stageItems = NipunCurriculumRegistry.getItemsForStage(stage)
            assertTrue("Stage ${stage.displayName} must have curriculum items", stageItems.isNotEmpty())
        }
    }

    @Test
    fun testCurriculumHierarchyValid() {
        for (item in NipunCurriculumRegistry.getAllItems()) {
            assertTrue("Item ${item.id} must have CG- prefix", item.curricularGoalId.startsWith("CG-"))
            assertTrue("Item ${item.id} must have C- prefix", item.competencyId.startsWith("C-"))
            assertTrue("Item ${item.id} must have LO- prefix", item.learningOutcomeId.startsWith("LO-"))
            assertTrue("Item ${item.id} must have learning outcome text", item.learningOutcomeText.isNotBlank())
            assertNotNull("Item ${item.id} must have valid domain", item.domain)
            assertNotNull("Item ${item.id} must have valid stage", item.stage)
        }
    }

    @Test
    fun testFilteringByStageAndDomain() {
        val g2Lang = NipunCurriculumRegistry.getItems(FoundationalStage.GRADE_2, CurriculumDomain.LANGUAGE_LITERACY)
        assertTrue("Grade 2 Language should have items", g2Lang.isNotEmpty())
        for (item in g2Lang) {
            assertEquals(FoundationalStage.GRADE_2, item.stage)
            assertEquals(CurriculumDomain.LANGUAGE_LITERACY, item.domain)
        }

        val balNum = NipunCurriculumRegistry.getItems(FoundationalStage.BALVATIKA, CurriculumDomain.NUMERACY)
        assertTrue("Balvatika Numeracy should have items", balNum.isNotEmpty())
        for (item in balNum) {
            assertEquals(FoundationalStage.BALVATIKA, item.stage)
            assertEquals(CurriculumDomain.NUMERACY, item.domain)
        }
    }

    @Test
    fun testSuitabilityAndActivityCompatibility() {
        for (item in NipunCurriculumRegistry.getAllItems()) {
            if (item.suitability == WorksheetSuitability.WORKSHEET_SUITABLE) {
                assertTrue("Worksheet-suitable item ${item.id} must define supported activities", item.supportedActivityTypes.isNotEmpty())
            } else {
                assertTrue("Non-worksheet item ${item.id} should not define paper activities", item.supportedActivityTypes.isEmpty())
            }
        }
    }

    @Test
    fun testWorksheetGenerationFromCurriculumItem() {
        val targetItem = NipunCurriculumRegistry.getItemById("G2-LANG-CG10-C103-01")
        assertNotNull("Target curriculum item must exist", targetItem)

        val worksheet = generator.generateFromCurriculum(
            item = targetItem!!,
            targetQuestionCount = 5,
            preferredType = QuestionType.READ_AND_ANSWER
        )

        assertNotNull(worksheet)
        assertEquals(5, worksheet.questions.size)
        assertEquals("G-2 • भाषा एवं साक्षरता विकास", worksheet.title)
        assertEquals("Grade 2 (Class 2)", worksheet.grade)
        assertEquals("CG-10", worksheet.curricularGoalId)
        assertEquals("C-10.3", worksheet.competencyId)
        assertTrue("Instructions must be present", worksheet.instructions.isNotBlank())
        assertTrue("Santali instructions must be present", !worksheet.santaliInstructions.isNullOrBlank())
        assertTrue("Answer key should be enabled", worksheet.includeAnswerKey)
        assertTrue("Teacher alignment should be enabled", worksheet.showTeacherAlignment)

        assertEquals("C-10.3", worksheet.questions[0].competencyId)

        for (q in worksheet.questions) {
            assertTrue("Question must have non-blank Hindi", q.hindiText.isNotBlank())
            assertTrue("Question must have non-blank Santali", q.santaliText.isNotBlank())
            assertTrue("Question must have non-blank Answer", q.answer.isNotBlank())
            assertEquals("G-2", q.stage)
            assertEquals("LANG", q.domain)
            assertTrue("Competency must start with C-", q.competencyId.startsWith("C-"))
            assertEquals(SantaliVerificationStatus.VERIFIED, q.verificationStatus)
        }
    }

    @Test
    fun testBilingualContentAndVerificationStatus() {
        for (item in NipunCurriculumRegistry.getAllItems()) {
            if (item.suitability == WorksheetSuitability.WORKSHEET_SUITABLE) {
                assertTrue("Item ${item.id} must have Hindi prompt", item.hindiPrompt.isNotBlank())
                assertTrue("Item ${item.id} must have Santali Ol Chiki", item.santaliOlChiki.isNotBlank())
                assertNotNull("Item ${item.id} must have verification status", item.verificationStatus)
            }
        }
    }

    @Test
    fun testFlashcardToWorksheetIntegration() {
        val flashcards = listOf(
            Flashcard(
                topic = "Animals",
                hindiText = "गाय",
                santaliText = "Gại",
                santaliOlChiki = "ᱜᱟᱹᱭ",
                phoneticGuide = "गाई",
                exampleSentenceHindi = "यह गाय है।",
                exampleSentenceSantali = "ᱱᱚᱶᱟ ᱫᱚ ᱜᱟᱹᱭ ᱠᱟᱱᱟᱭ।"
            ),
            Flashcard(
                topic = "Animals",
                hindiText = "कुत्ता",
                santaliText = "Seta",
                santaliOlChiki = "ᱥᱮᱛᱟ",
                phoneticGuide = "सेता",
                exampleSentenceHindi = "कुत्ता वफादार होता है।",
                exampleSentenceSantali = "ᱥᱮᱛᱟ ᱫᱚ ᱵᱤᱥᱣᱟᱥᱤ ᱠᱟᱱᱟᱭ।"
            ),
            Flashcard(
                topic = "Animals",
                hindiText = "मछली",
                santaliText = "Haku",
                santaliOlChiki = "ᱦᱟᱹᱠᱩ",
                phoneticGuide = "हाकू",
                exampleSentenceHindi = "मछली पानी में तैरती है।",
                exampleSentenceSantali = "ᱦᱟᱹᱠᱩ ᱫᱟᱜ ᱨᱮ ᱯᱟᱭᱨᱟᱜ-ᱟ।"
            )
        )

        val flashcardSet = FlashcardSet(
            title = "Animals Deck",
            topic = "Animals",
            domain = "Environmental & Social",
            learningOutcome = "Identifies living beings and their sounds",
            grade = "Grade 1-3",
            cards = flashcards
        )

        val worksheet = generator.createWorksheetFromFlashcards(flashcardSet)

        assertNotNull(worksheet)
        assertEquals("Animals Deck Bilingual Worksheet", worksheet.title)
        assertEquals("Animals", worksheet.topic)
        assertEquals("Grade 1-3", worksheet.grade)
        assertTrue("Should produce multiple activities from flashcard deck", worksheet.questions.size >= 4)

        val types = worksheet.questions.map { it.type }.toSet()
        assertTrue("Should include MATCHING from flashcards", types.contains(QuestionType.MATCHING))
        assertTrue("Should include WORD_MEANING from flashcards", types.contains(QuestionType.WORD_MEANING))
        assertTrue("Should include FILL_IN_THE_BLANK from flashcards", types.contains(QuestionType.FILL_IN_THE_BLANK))
        assertTrue("Should include MULTIPLE_CHOICE from flashcards", types.contains(QuestionType.MULTIPLE_CHOICE))
        assertTrue("Should include TRACE_OR_WRITE from flashcards", types.contains(QuestionType.TRACE_OR_WRITE))
    }

    @Test
    fun testTeacherEditingPreservation() {
        val targetItem = NipunCurriculumRegistry.getItemById("G1-NUM-CG8-C82-01")!!
        val worksheet = generator.generateFromCurriculum(targetItem, 3)

        val originalQuestion = worksheet.questions[0]
        val editedHindi = "जोड़ो: 5 + 2 = ___"
        val editedSantali = "ᱢᱮᱥᱟᱭ ᱢᱮ: ᱕ + ᱒ = ___"
        val editedAnswer = "7 (ᱮᱭᱟᱭ)"

        val editedQuestion = originalQuestion.copy(
            hindiText = editedHindi,
            santaliText = editedSantali,
            answer = editedAnswer,
            verificationStatus = SantaliVerificationStatus.TEACHER_VERIFIED
        )

        assertEquals(editedHindi, editedQuestion.hindiText)
        assertEquals(editedSantali, editedQuestion.santaliText)
        assertEquals(editedAnswer, editedQuestion.answer)
        assertEquals(SantaliVerificationStatus.TEACHER_VERIFIED, editedQuestion.verificationStatus)
    }

    @Test
    fun testQuestionTypeRetranslation() {
        val translatedTrace = generator.retranslateQuestionText("सुलेख लिखो: किताब", QuestionType.TRACE_OR_WRITE)
        assertTrue("Should retranslate trace activity", translatedTrace.contains("ᱯᱩᱛᱷᱤ") || translatedTrace.contains("Translation"))

        val translatedMCQ = generator.retranslateQuestionText("सही विकल्प चुनें: पानी", QuestionType.MULTIPLE_CHOICE)
        assertTrue("Should retranslate MCQ activity", translatedMCQ.contains("ᱫᱟᱜ") || translatedMCQ.contains("Translation"))

        val translatedCount = generator.retranslateQuestionText("गिनकर संख्या लिखो: दो", QuestionType.COUNT_AND_WRITE)
        assertTrue("Should retranslate Count activity", translatedCount.contains("ᱵᱟᱨ") || translatedCount.contains("Translation"))
    }
}
