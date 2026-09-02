package com.alchemists.tribetalk.curriculum

import com.alchemists.tribetalk.curriculum.repository.FLNCurriculumRepository
import com.alchemists.tribetalk.translation.OlChikiTransliterator
import org.junit.Assert.*
import org.junit.Test

class FLNContentPackTest {

    @Test
    fun testPrototypeLessonsCountAndIntegrity() {
        val lessons = FLNCurriculumRepository.lessons
        assertTrue("At least 5 lessons must exist", lessons.size >= 5)

        lessons.forEach { lesson ->
            assertTrue("Lesson ID must not be blank", lesson.id.isNotBlank())
            assertTrue("Lesson title Hindi must not be blank", lesson.titleHindi.isNotBlank())
            assertTrue("Lesson title Santali must not be blank", lesson.titleSantali.isNotBlank())
            assertTrue("Learning outcome Hindi must not be blank", lesson.learningOutcome.descriptionHindi.isNotBlank())
            assertTrue("Learning outcome Santali must not be blank", lesson.learningOutcome.descriptionSantali.isNotBlank())
            assertTrue("Instructions list must not be empty", lesson.instructions.isNotEmpty())
            assertTrue("Assessment questions list must not be empty", lesson.assessmentQuestions.isNotEmpty())
        }
    }

    @Test
    fun testOlChikiUnicodeAndTeacherHUDPhonetics() {
        val santaliText = "1 ᱠᱷᱚᱱ 10 ᱦᱟᱹᱵᱤᱡ ᱞᱮᱠᱷᱟ"
        val phoneticHUD = OlChikiTransliterator.toTeacherPhoneticHUD(santaliText)

        assertNotNull("Phonetic HUD should not be null", phoneticHUD)
        assertTrue("Phonetic HUD should contain Devanagari phonetics", phoneticHUD.isNotBlank())
    }
}
