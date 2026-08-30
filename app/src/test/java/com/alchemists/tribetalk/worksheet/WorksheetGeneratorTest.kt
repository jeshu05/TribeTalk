package com.alchemists.tribetalk.worksheet

import com.alchemists.tribetalk.translation.Language
import com.alchemists.tribetalk.translation.OfflineFLNTranslationEngine
import com.alchemists.tribetalk.translation.TranslationMemory
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class WorksheetGeneratorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var tempFile: File
    private lateinit var translationMemory: TranslationMemory
    private lateinit var translationEngine: OfflineFLNTranslationEngine
    private lateinit var generator: WorksheetGenerator

    @Before
    fun setUp() {
        tempFile = tempFolder.newFile("test_tm.csv")
        translationMemory = TranslationMemory(tempFile)
        translationEngine = OfflineFLNTranslationEngine(translationMemory)
        generator = WorksheetGenerator(translationEngine)
    }

    @After
    fun tearDown() {
        translationMemory.clearMemory()
    }

    @Test
    fun testWorksheetModelCreation() {
        val question = WorksheetQuestion(
            type = QuestionType.WORD_MEANING,
            hindiText = "गाय",
            santaliText = "ᱜᱟᱹᱭ (गाय)",
            options = emptyList(),
            answer = "ᱜᱟᱹᱭ",
            editable = true
        )
        val worksheet = Worksheet(
            title = "Animals Bilingual Worksheet",
            topic = "Animals",
            grade = "Grade 3",
            instructions = "Solve all questions.",
            questions = listOf(question)
        )

        assertEquals("Animals Bilingual Worksheet", worksheet.title)
        assertEquals("Animals", worksheet.topic)
        assertEquals("Grade 3", worksheet.grade)
        assertEquals(1, worksheet.questions.size)
        assertEquals(QuestionType.WORD_MEANING, worksheet.questions[0].type)
        assertTrue(worksheet.questions[0].editable)
    }

    @Test
    fun testGenerateWorksheetBasicFlow() {
        val topic = "Animals"
        val hindiContent = """
            गाय एक पालतू जानवर है।
            कुत्ता एक पालतू जानवर है।
            मछली पानी में रहती है।
        """.trimIndent()

        val worksheet = generator.generateWorksheet(topic, hindiContent, "Grade 3")

        assertNotNull(worksheet)
        assertEquals("Animals Bilingual Worksheet", worksheet.title)
        assertEquals("Animals", worksheet.topic)
        assertEquals("Grade 3", worksheet.grade)
        assertTrue(worksheet.questions.size in 5..10)

        // Verify question types exist
        val types = worksheet.questions.map { it.type }.toSet()
        assertTrue("Should include WORD_MEANING", types.contains(QuestionType.WORD_MEANING))
        assertTrue("Should include FILL_IN_THE_BLANK", types.contains(QuestionType.FILL_IN_THE_BLANK))
        assertTrue("Should include MATCHING", types.contains(QuestionType.MATCHING))
        assertTrue("Should include MULTIPLE_CHOICE", types.contains(QuestionType.MULTIPLE_CHOICE))
        assertTrue("Should include READ_AND_ANSWER", types.contains(QuestionType.READ_AND_ANSWER))
    }

    @Test
    fun testBilingualContentIntegrity() {
        val topic = "Classroom"
        val hindiContent = "किताब खोलो। लिखना शुरू करो।"

        val worksheet = generator.generateWorksheet(topic, hindiContent, "Grade 2")

        for (q in worksheet.questions) {
            assertTrue("Hindi text should not be blank", q.hindiText.isNotBlank())
            assertTrue("Santali text should not be blank", q.santaliText.isNotBlank())
            assertNotNull(q.answer)
        }
    }

    @Test
    fun testUnavailableTranslationHandling() {
        val untranslatableWord = "अपरिचितअजनबीअजीबशब्द१२३"
        val santali = generator.translateToSantali(untranslatableWord)

        assertEquals("Translation unavailable", santali)
    }

    @Test
    fun testEmptyInputHandling() {
        val worksheet = generator.generateWorksheet("", "", null)

        assertNotNull(worksheet)
        assertTrue(worksheet.questions.isEmpty())
    }

    @Test
    fun testTeacherEditingDoesNotMutateTranslationMemory() {
        val topic = "Animals"
        val hindiContent = "गाय एक पालतू जानवर है।"
        val worksheet = generator.generateWorksheet(topic, hindiContent, "Grade 3")

        val initialQuestion = worksheet.questions.first()
        val editedQuestion = initialQuestion.copy(
            hindiText = "संशोधित हिन्दी प्रश्न",
            santaliText = "ᱥᱟᱱᱛᱟᱲᱤ ᱵᱚᱫᱚᱞ",
            answer = "नया उत्तर"
        )

        val updatedQuestions = worksheet.questions.toMutableList().apply {
            set(0, editedQuestion)
        }
        val updatedWorksheet = worksheet.copy(questions = updatedQuestions)

        // Verify updated worksheet has modified question
        assertEquals("संशोधित हिन्दी प्रश्न", updatedWorksheet.questions[0].hindiText)
        assertEquals("ᱥᱟᱱᱛᱟᱲᱤ ᱵᱚᱫᱚᱞ", updatedWorksheet.questions[0].santaliText)
        assertEquals("नया उत्तर", updatedWorksheet.questions[0].answer)

        // Verify TranslationMemory remains untouched (0 entries)
        assertEquals(0, translationMemory.getEntries().size)
    }

    @Test
    fun testMultipleChoiceQuestionStructure() {
        val topic = "Nature"
        val hindiContent = "पानी जीवन के लिए आवश्यक है।"
        val worksheet = generator.generateWorksheet(topic, hindiContent, "Grade 1")

        val mcq = worksheet.questions.firstOrNull { it.type == QuestionType.MULTIPLE_CHOICE }
        assertNotNull(mcq)
        assertTrue(mcq!!.options.isNotEmpty())
        assertTrue(mcq.hindiText.contains("विकल्प"))
        assertTrue(mcq.answer.isNotBlank())
    }

    @Test
    fun testAllQuestionTypesSupported() {
        val supportedTypes = QuestionType.values().toList()
        assertEquals(5, supportedTypes.size)
        assertTrue(supportedTypes.contains(QuestionType.MATCHING))
        assertTrue(supportedTypes.contains(QuestionType.FILL_IN_THE_BLANK))
        assertTrue(supportedTypes.contains(QuestionType.MULTIPLE_CHOICE))
        assertTrue(supportedTypes.contains(QuestionType.WORD_MEANING))
        assertTrue(supportedTypes.contains(QuestionType.READ_AND_ANSWER))
    }

    @Test
    fun testHindiEditTriggersRetranslation() {
        // Teacher edits Hindi text to a known database phrase
        val editedHindi = "किताब खोलो"
        val santaliResult = generator.retranslateQuestionText(editedHindi, QuestionType.WORD_MEANING)

        assertTrue(santaliResult.isNotBlank())
        assertTrue("Santali translation should contain translated phrase", santaliResult.contains("ᱯᱩᱛᱷᱤ") || santaliResult.contains("Puthī"))
    }

    @Test
    fun testSuccessfulTranslationUpdatesSantaliOnlyAndLeavesHindiUnchanged() {
        val originalHindi = "किताब खोलो"
        val editedHindi = "लिखना शुरू करो"

        // Simulate question editing in UI
        val originalQuestion = WorksheetQuestion(
            type = QuestionType.READ_AND_ANSWER,
            hindiText = originalHindi,
            santaliText = "ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ"
        )

        // Re-translate from edited Hindi
        val retranslatedSantali = generator.retranslateQuestionText(editedHindi, originalQuestion.type)
        val updatedQuestion = originalQuestion.copy(
            hindiText = editedHindi,
            santaliText = retranslatedSantali
        )

        assertEquals("लिखना शुरू करो", updatedQuestion.hindiText)
        assertNotEquals("ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ", updatedQuestion.santaliText)
        assertTrue(updatedQuestion.santaliText.contains("ᱚᱞ") || updatedQuestion.santaliText.contains("Ol"))
    }

    @Test
    fun testUnavailableTranslationReturnsFallbackMessage() {
        val unknownHindi = "यह एक पूरी तरह से अज्ञात और अजीब वाक्य है १०९८७"
        val santaliResult = generator.retranslateQuestionText(unknownHindi, QuestionType.READ_AND_ANSWER)

        assertEquals("Translation unavailable for this text", santaliResult)
    }

    @Test
    fun testEmptyHindiTextRejection() {
        val emptyResult = generator.retranslateQuestionText("   ", QuestionType.WORD_MEANING)
        assertEquals("", emptyResult)
    }

    @Test
    fun testTeacherManualSantaliEditsArePreserved() {
        val hindi = "नमस्ते"
        val generatedSantali = generator.retranslateQuestionText(hindi, QuestionType.WORD_MEANING)

        // Teacher manually modifies the Santali text
        val teacherModifiedSantali = "ᱢᱟᱱᱟᱣ ᱡᱚᱦᱟᱨ (Custom Greeting)"

        val question = WorksheetQuestion(
            type = QuestionType.WORD_MEANING,
            hindiText = hindi,
            santaliText = teacherModifiedSantali
        )

        // Verify the manual edit is preserved on the question model
        assertEquals("नमस्ते", question.hindiText)
        assertEquals("ᱢᱟᱱᱟᱣ ᱡᱚᱦᱟᱨ (Custom Greeting)", question.santaliText)
        // Verify translation memory is still untouched
        assertEquals(0, translationMemory.getEntries().size)
    }

    @Test
    fun testRetranslationDoesNotModifyTranslationMemory() {
        val initialSize = translationMemory.getEntries().size
        generator.retranslateQuestionText("किताब खोलो", QuestionType.WORD_MEANING)
        generator.retranslateQuestionText("पानी", QuestionType.FILL_IN_THE_BLANK)
        generator.retranslateQuestionText("शिक्षक", QuestionType.MATCHING)

        // Ensure TranslationMemory is not mutated
        assertEquals(initialSize, translationMemory.getEntries().size)
    }

    @Test
    fun testRetranslationAcrossAllQuestionTypes() {
        for (type in QuestionType.values()) {
            val result = generator.retranslateQuestionText("पानी", type)
            assertTrue("Type ${type.name} should yield valid translation", result.isNotBlank())
            assertNotEquals("Translation unavailable for this text", result)
        }
    }
}
