package com.alchemists.tribetalk.db.repository

import android.content.Context
import com.alchemists.tribetalk.curriculum.repository.FLNCurriculumRepository
import com.alchemists.tribetalk.db.entities.*

object NipunLocalRepository {

    private var teacherProfile = TeacherEntity(
        id = "t_01",
        name = "Primary School Teacher",
        selectedClassesJson = "[\"class_1\", \"class_2\", \"class_3\"]"
    )

    private val uploadedResources = mutableListOf<TeachingResourceEntity>()

    fun getTeacher(context: Context? = null): TeacherEntity {
        return teacherProfile
    }

    fun updateTeacherClasses(selectedClasses: List<String>) {
        val jsonStr = selectedClasses.joinToString(prefix = "[\"", postfix = "\"]", separator = "\",\"")
        teacherProfile = teacherProfile.copy(selectedClassesJson = jsonStr)
    }

    fun getClassrooms(): List<ClassroomEntity> {
        return listOf(
            ClassroomEntity("class_1", "Class 1", "Class 1 (Grade 1)"),
            ClassroomEntity("class_2", "Class 2", "Class 2 (Grade 2)"),
            ClassroomEntity("class_3", "Class 3", "Class 3 (Grade 3)")
        )
    }

    fun getOutcomesForGrade(grade: String): List<LearningOutcomeEntity> {
        val lessons = FLNCurriculumRepository.lessons
        return lessons
            .filter { it.grade.contains(grade, ignoreCase = true) || grade.isBlank() }
            .map { les ->
                LearningOutcomeEntity(
                    outcomeId = les.learningOutcome.id,
                    competencyId = "comp_${les.learningOutcome.id}",
                    nipunCode = les.learningOutcome.nipunCode,
                    grade = les.grade,
                    domain = les.domain.name,
                    topic = les.topic,
                    descriptionHindi = les.learningOutcome.descriptionHindi,
                    descriptionSantali = les.learningOutcome.descriptionSantali,
                    isOfficial = false
                )
            }
    }

    fun getLessonsForGrade(grade: String): List<LessonEntity> {
        val gradeDigit = grade.filter { it.isDigit() }
        return FLNCurriculumRepository.lessons
            .filter { les ->
                grade.isBlank() || 
                les.grade.contains(grade, ignoreCase = true) || 
                (gradeDigit.isNotEmpty() && les.grade.contains(gradeDigit))
            }
            .map { les ->
                LessonEntity(
                    lessonId = les.id,
                    outcomeId = les.learningOutcome.id,
                    grade = les.grade,
                    domain = les.domain.name,
                    topic = les.topic,
                    titleHindi = les.titleHindi,
                    titleSantali = les.titleSantali,
                    instructionsJson = "[]",
                    activitiesJson = "[]"
                )
            }
    }

    fun searchCurriculum(query: String): List<LessonEntity> {
        if (query.isBlank()) return getLessonsForGrade("")
        return getLessonsForGrade("").filter {
            it.titleHindi.contains(query, ignoreCase = true) ||
            it.titleSantali.contains(query, ignoreCase = true) ||
            it.topic.contains(query, ignoreCase = true)
        }
    }

    fun addTeachingResource(classId: String, outcomeId: String, title: String, type: String, filePath: String) {
        val resource = TeachingResourceEntity(
            resourceId = "res_${System.currentTimeMillis()}",
            classId = classId,
            outcomeId = outcomeId,
            title = title,
            type = type,
            filePath = filePath
        )
        uploadedResources.add(resource)
    }

    fun getResourcesForClass(classId: String): List<TeachingResourceEntity> {
        return uploadedResources.filter { it.classId == classId || classId.isBlank() }
    }
}
