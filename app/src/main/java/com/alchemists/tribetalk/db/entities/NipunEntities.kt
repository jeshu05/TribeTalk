package com.alchemists.tribetalk.db.entities

data class TeacherEntity(
    val id: String = "t_01",
    val name: String,
    val selectedClassesJson: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class ClassroomEntity(
    val classId: String,
    val grade: String,
    val name: String
)

data class DomainEntity(
    val domainId: String,
    val name: String,
    val type: String
)

data class CompetencyEntity(
    val competencyId: String,
    val domainId: String,
    val code: String,
    val descriptionHindi: String,
    val descriptionSantali: String
)

data class LearningOutcomeEntity(
    val outcomeId: String,
    val competencyId: String,
    val nipunCode: String,
    val grade: String,
    val domain: String,
    val topic: String,
    val descriptionHindi: String,
    val descriptionSantali: String,
    val isOfficial: Boolean = false
)

data class LessonEntity(
    val lessonId: String,
    val outcomeId: String,
    val grade: String,
    val domain: String,
    val topic: String,
    val titleHindi: String,
    val titleSantali: String,
    val instructionsJson: String,
    val activitiesJson: String
)

data class FlashcardEntity(
    val flashcardId: String,
    val outcomeId: String,
    val titleHindi: String,
    val titleSantali: String,
    val iconEmoji: String,
    val imagePath: String
)

data class WorksheetEntity(
    val worksheetId: String,
    val outcomeId: String,
    val titleHindi: String,
    val titleSantali: String,
    val questionsJson: String
)

data class StudentEntity(
    val studentId: String,
    val classId: String,
    val name: String,
    val rollNumber: Int
)

data class StudentOutcomeProgressEntity(
    val id: String,
    val studentId: String,
    val outcomeId: String,
    val status: String,
    val accuracyPercentage: Float,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class TeachingResourceEntity(
    val resourceId: String,
    val classId: String,
    val outcomeId: String,
    val title: String,
    val type: String,
    val filePath: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ContentTranslationEntity(
    val contentId: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val translatedText: String,
    val audioPath: String,
    val status: String = "ACTIVE"
)
