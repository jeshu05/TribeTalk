package com.alchemists.tribetalk.curriculum

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Pre-packaged, fully-offline NIPUN/FLN curriculum data model.
 *
 * Every official record carries [isOfficial] and [sourceRef] so that approved
 * NIPUN/FLN data is clearly distinguished from local demo/test records.
 *
 * Hierarchy:
 * Grade/Class -> Domain -> Competency -> LearningOutcome -> Lesson/Activity
 *   -> Flashcard / Worksheet / Assessment -> StudentOutcomeProgress
 */

@Entity
data class Domain(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kind: String,               // LITERACY | NUMERACY
    val description: String? = null,
    val isOfficial: Boolean,
    val sourceRef: String? = null,  // source document / page
    val sortOrder: Int = 0
)

@Entity
data class Competency(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val domainId: Long,
    val grade: Int,
    val name: String,
    val description: String? = null,
    val isOfficial: Boolean,
    val sourceRef: String? = null,
    val sortOrder: Int = 0
)

@Entity(
    indices = [Index(value = ["grade", "domainId"])]
)
data class LearningOutcome(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val competencyId: Long,
    val domainId: Long,
    val grade: Int,
    val code: String?,              // e.g. LO-1
    val title: String,
    val description: String? = null,
    val isOfficial: Boolean,
    val sourceRef: String? = null,
    val sortOrder: Int = 0
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = LearningOutcome::class,
            parentColumns = ["id"],
            childColumns = ["learningOutcomeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("learningOutcomeId"), Index("grade"), Index("domainId")]
)
data class Lesson(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val learningOutcomeId: Long? = null,
    val domainId: Long? = null,
    val grade: Int,
    val title: String,
    val content: String? = null,
    val story: String? = null,       // story/poem/rhyme text when applicable
    val isOfficial: Boolean,
    val sourceRef: String? = null,
    val sortOrder: Int = 0
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Lesson::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("lessonId"), Index("learningOutcomeId")]
)
data class Activity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lessonId: Long? = null,
    val learningOutcomeId: Long? = null,
    val domainId: Long? = null,
    val grade: Int,
    val title: String,
    val instructions: String? = null,
    val type: String? = null,        // oral, listening, speaking, picture, game...
    val isOfficial: Boolean,
    val sourceRef: String? = null,
    val sortOrder: Int = 0
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = LearningOutcome::class,
            parentColumns = ["id"],
            childColumns = ["learningOutcomeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("learningOutcomeId"), Index("grade"), Index("domainId")]
)
data class Assessment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val learningOutcomeId: Long? = null,
    val domainId: Long? = null,
    val grade: Int,
    val title: String,
    val instruction: String? = null,
    val questionType: String? = null,
    val prompt: String? = null,
    val answerKey: String? = null,
    val isOfficial: Boolean,
    val sourceRef: String? = null,
    val sortOrder: Int = 0
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = LearningOutcome::class,
            parentColumns = ["id"],
            childColumns = ["learningOutcomeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("learningOutcomeId"), Index("grade"), Index("domainId")]
)
data class TeachingResource(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val learningOutcomeId: Long? = null,
    val domainId: Long? = null,
    val grade: Int,
    val title: String,
    val resourceType: String,        // flashcard | worksheet | lesson | chart
    val isOfficial: Boolean,
    val sourceRef: String? = null,
    val sortOrder: Int = 0
)

@Entity(
    indices = [Index("learningOutcomeId"), Index("grade"), Index("domainId")]
)
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val learningOutcomeId: Long? = null,
    val domainId: Long? = null,
    val grade: Int,
    val cardType: String,            // letter | word | picture | number | counting | shape | pattern | story
    val front: String,
    val back: String? = null,
    val imagePath: String? = null,   // path to local asset; never BLOB
    val audioPath: String? = null,   // path to local asset
    val isOfficial: Boolean,
    val sourceRef: String? = null,
    val sortOrder: Int = 0
)

@Entity(
    indices = [Index("learningOutcomeId"), Index("grade"), Index("domainId")]
)
data class Worksheet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val learningOutcomeId: Long? = null,
    val domainId: Long? = null,
    val grade: Int,
    val title: String,
    val questionType: String,        // MCQ | FILL_BLANKS | MATCHING | COUNTING | PICTURE | READING | WRITING
    val content: String = "[]",      // JSON array of questions
    val isOfficial: Boolean,
    val sourceRef: String? = null,
    val sortOrder: Int = 0
)

@Entity
data class Classroom(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val grade: Int,
    val label: String
)

@Entity
data class TeacherClassroom(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: Long,
    val classroomId: Long,
    val grade: Int
)

@Entity
data class Teacher(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val displayName: String
)

@Entity(
    indices = [Index(value = ["classroomId"], unique = true)]
)
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classroomId: Long,
    val name: String,
    val admissionNo: String? = null
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId"), Index("learningOutcomeId")]
)
data class StudentOutcomeProgress(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val learningOutcomeId: Long,
    val grade: Int,
    val status: String,             // NOT_STARTED | NEEDS_SUPPORT | DEVELOPING | ACHIEVED
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity
data class UserResource(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: Long,
    val grade: Int,
    val domainId: Long? = null,
    val learningOutcomeId: Long? = null,
    val title: String,
    val filePath: String,           // local storage path; never BLOB
    val fileType: String,           // pdf | ppt | pptx
    val mimeType: String? = null,
    val sizeBytes: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    indices = [Index(value = ["contentId", "contentType", "languageCode"], unique = true)]
)
data class ContentTranslation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contentId: Long,
    val contentType: String,        // lesson | activity | flashcard | worksheet | learning_outcome ...
    val languageCode: String,       // hi, ho, mnr (Mundari), sat (Santali)
    val translatedText: String? = null,
    val audioPath: String? = null,
    val status: String = "PENDING", // PENDING | COMPLETED
    val confidence: Float = 0f
)
