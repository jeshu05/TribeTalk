package com.alchemists.tribetalk.curriculum

import com.alchemists.tribetalk.curriculum.models.FLNDomain
import com.alchemists.tribetalk.curriculum.repository.FLNCurriculumRepository
import com.alchemists.tribetalk.translation.OlChikiTransliterator
import org.junit.Assert.*
import org.junit.Test

class FLNCurriculumTest {

    @Test
    fun testFLNCurriculumRepositoryLoading() {
        val lessons = FLNCurriculumRepository.lessons
        assertNotNull("FLN Curriculum lessons list should not be null", lessons)
        assertTrue("FLN Curriculum should contain at least 8 prototype lessons", lessons.size >= 8)
    }

    @Test
    fun testDomainFiltering() {
        val numeracyLessons = FLNCurriculumRepository.getLessonsByDomain(FLNDomain.NUMERACY)
        val literacyLessons = FLNCurriculumRepository.getLessonsByDomain(FLNDomain.LITERACY)

        assertTrue("Numeracy lessons should contain counting and addition", numeracyLessons.isNotEmpty())
        assertTrue("Literacy lessons should contain colours and alphabet", literacyLessons.isNotEmpty())

        numeracyLessons.forEach { lesson ->
            assertEquals("Domain should be NUMERACY", FLNDomain.NUMERACY, lesson.domain)
        }
    }

    @Test
    fun testNIPUNOutcomeCodesAndBilingualMapping() {
        val lessons = FLNCurriculumRepository.lessons

        lessons.forEach { lesson ->
            assertTrue("Lesson ID should be non-empty", lesson.id.isNotBlank())
            assertTrue("NIPUN code should start with NIPUN", lesson.learningOutcome.nipunCode.startsWith("NIPUN"))
            assertTrue("Hindi title should be non-empty", lesson.titleHindi.isNotBlank())
            assertTrue("Santali title should be non-empty", lesson.titleSantali.isNotBlank())
            assertTrue("Phonetic title should be non-empty", lesson.titlePhonetic.isNotBlank())
            assertTrue("Instructions should not be empty", lesson.instructions.isNotEmpty())

            lesson.instructions.forEach { inst ->
                assertTrue("Teacher Hindi prompt should be non-empty", inst.teacherPromptHindi.isNotBlank())
                assertTrue("Santali translation should be non-empty", inst.santaliTranslation.isNotBlank())
                assertTrue("Phonetic Devanagari HUD text should be non-empty", inst.phoneticDevanagari.isNotBlank())
            }
        }
    }

    @Test
    fun testOlChikiPhoneticHUDTransliteration() {
        val sampleOlChiki = "ᱵᱟᱹᱵᱩ ᱛᱮᱦᱮᱧ"
        val phoneticDevanagari = OlChikiTransliterator.toTeacherPhoneticHUD(sampleOlChiki)

        assertNotNull("Phonetic Devanagari output should not be null", phoneticDevanagari)
        assertTrue("Phonetic output should be non-empty", phoneticDevanagari.isNotBlank())
        assertTrue("Phonetic output should contain Devanagari characters", phoneticDevanagari.any { Character.UnicodeBlock.of(it) == Character.UnicodeBlock.DEVANAGARI })
    }

    @Test
    fun testAssessmentQuizAnswers() {
        val countingLesson = FLNCurriculumRepository.getLessonById("fln_num_01")
        assertNotNull("Counting lesson should exist", countingLesson)

        val quiz = countingLesson!!.assessmentQuestions.first()
        assertEquals("Correct option index for counting quiz should be 1", 1, quiz.correctOptionIndex)
        assertEquals("Correct option should be ᱯᱩᱱ", "ᱯᱩᱱ", quiz.optionsSantali[quiz.correctOptionIndex])
    }
}
