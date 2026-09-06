package com.alchemists.tribetalk

import com.alchemists.tribetalk.insights.ClassroomAnalyticsManager
import com.alchemists.tribetalk.lessons.Lesson
import com.alchemists.tribetalk.lessons.LessonRepository
import com.alchemists.tribetalk.translation.OfflineFLNTranslationEngine
import com.alchemists.tribetalk.translation.TranslationMemory
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class Phase11ModulesTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var tempFile: File
    private lateinit var translationMemory: TranslationMemory
    private lateinit var translationEngine: OfflineFLNTranslationEngine

    @Before
    fun setUp() {
        tempFile = tempFolder.newFile("test_phase11_tm.csv")
        translationMemory = TranslationMemory(tempFile)
        translationEngine = OfflineFLNTranslationEngine(translationMemory)
    }

    @After
    fun tearDown() {
        translationMemory.clearMemory()
    }

    @Test
    fun testLessonModelCreation() {
        val lesson = Lesson(
            id = "test_lesson_1",
            title = "Test Lesson",
            topic = "Greetings",
            grade = "Grade 1",
            learningDomain = "Foundational Literacy",
            learningOutcome = "Oral Vocabulary",
            hindiIntroduction = "नमस्ते सीखें",
            santaliIntroduction = "ᱡᱚᱦᱟᱨ ᱪᱮᱫᱚᱜ ᱢᱮ",
            lessonScriptHindi = "नमस्ते",
            lessonScriptSantali = "ᱡᱚᱦᱟᱨ",
            activityInstructionsHindi = "दोहराएं",
            activityInstructionsSantali = "ᱫᱚᱦᱲᱟᱭ ᱢᱮ",
            assessmentPromptsHindi = "नाम बताएं",
            assessmentPromptsSantali = "ᱧᱩᱛᱩᱢ ᱞᱟᱹᱭ ᱢᱮ"
        )

        assertEquals("test_lesson_1", lesson.id)
        assertEquals("Test Lesson", lesson.title)
        assertEquals("Grade 1", lesson.grade)
        assertEquals("Foundational Literacy", lesson.learningDomain)
        assertEquals("नमस्ते", lesson.lessonScriptHindi)
        assertEquals("ᱡᱚᱦᱟᱨ", lesson.lessonScriptSantali)
        assertTrue(lesson.createdAt > 0)
    }

    @Test
    fun testLessonRepositoryGetAllAndCategories() {
        val allLessons = LessonRepository.getAllLessons()
        assertTrue("Repository should contain at least 7 demo units", allLessons.size >= 7)

        val literacyLessons = allLessons.filter { it.learningDomain.contains("Literacy") }
        val numeracyLessons = allLessons.filter { it.learningDomain.contains("Numeracy") }

        assertTrue("Should have literacy lessons", literacyLessons.isNotEmpty())
        assertTrue("Should have numeracy lessons", numeracyLessons.isNotEmpty())

        for (l in allLessons) {
            assertTrue(l.title.isNotBlank())
            assertTrue(l.lessonScriptHindi.isNotBlank())
            assertTrue(l.lessonScriptSantali.isNotBlank())
            assertTrue(l.activityInstructionsHindi.isNotBlank())
            assertTrue(l.assessmentPromptsHindi.isNotBlank())
        }
    }

    @Test
    fun testLessonRepositorySearch() {
        val searchGreetings = LessonRepository.searchLessons("Greetings")
        assertTrue(searchGreetings.isNotEmpty())
        assertTrue(searchGreetings.any { it.title.contains("Greetings") })

        val searchCounting = LessonRepository.searchLessons("Counting")
        assertTrue(searchCounting.isNotEmpty())
        assertTrue(searchCounting.any { it.title.contains("Counting") })

        val searchHindi = LessonRepository.searchLessons("किताब")
        assertTrue(searchHindi.isNotEmpty())
    }

    @Test
    fun testLessonSectionRetranslation() {
        val translated = LessonRepository.translateSection("नमस्ते", translationEngine)
        assertTrue(translated.isNotBlank())
        assertTrue(translated.contains("ᱡᱚᱦᱟᱨ") || translated.contains("Johar"))

        val unavailable = LessonRepository.translateSection("अजीबअपरिचितअज्ञात९९९", translationEngine)
        assertEquals("Translation unavailable", unavailable)
    }

    @Test
    fun testLessonRetranslationDoesNotMutateTranslationMemory() {
        val initialSize = translationMemory.getEntries().size
        LessonRepository.translateSection("किताब खोलो", translationEngine)
        LessonRepository.translateSection("पानी", translationEngine)

        assertEquals(initialSize, translationMemory.getEntries().size)
    }

    @Test
    fun testClassroomAnalyticsRecordingAndMetrics() {
        ClassroomAnalyticsManager.clearActivity()

        ClassroomAnalyticsManager.logLessonOpened("Greetings", "Foundational Literacy")
        ClassroomAnalyticsManager.logFlashcardSession("Animals", 5)
        ClassroomAnalyticsManager.logWorksheetCreated("Numbers", 6)
        ClassroomAnalyticsManager.logVoiceTranslationSession(8)

        val metrics = ClassroomAnalyticsManager.getMetrics()
        assertEquals(1, metrics.lessonsUsedCount)
        assertEquals(1, metrics.flashcardSessionsCount)
        assertEquals(1, metrics.worksheetsCreatedCount)
        assertEquals(8, metrics.voiceTranslationsCount)

        val recent = ClassroomAnalyticsManager.getRecentActivities()
        assertEquals(4, recent.size)
        assertTrue(recent[0].title.contains("Live Classroom"))
    }
}
