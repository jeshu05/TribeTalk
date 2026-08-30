package com.alchemists.tribetalk.curriculum

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NipunDao {

    // ---------- Data import (development-time seeding) ----------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDomain(d: Domain): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompetency(c: Competency): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningOutcome(lo: LearningOutcome): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(l: Lesson): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(a: Activity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(a: Assessment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachingResource(r: TeachingResource): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(f: Flashcard): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorksheet(w: Worksheet): Long

    // ---------- Classes / Grade - derived helpers ----------
    @Query("SELECT DISTINCT grade FROM Classroom ORDER BY grade")
    fun observeGrades(): Flow<List<Int>>

    // ---------- Domains ----------
    @Query("SELECT * FROM Domain ORDER BY sortOrder, id")
    fun observeDomains(): Flow<List<Domain>>

    @Query("SELECT * FROM Domain ORDER BY sortOrder, id")
    suspend fun domainsOnce(): List<Domain>

    @Query("SELECT * FROM LearningOutcome WHERE grade = :grade AND domainId = :domainId ORDER BY sortOrder, id")
    suspend fun learningOutcomesOnce(grade: Int, domainId: Long): List<LearningOutcome>

    @Query("SELECT * FROM LearningOutcome WHERE grade = :grade ORDER BY domainId, sortOrder, id")
    suspend fun learningOutcomesForGradeOnce(grade: Int): List<LearningOutcome>

    @Query("SELECT * FROM Lesson WHERE grade = :grade AND learningOutcomeId = :loId ORDER BY sortOrder, id")
    suspend fun lessonsForOutcomeOnce(grade: Int, loId: Long): List<Lesson>

    @Query("SELECT * FROM Activity WHERE grade = :grade AND learningOutcomeId = :loId ORDER BY sortOrder, id")
    suspend fun activitiesForOutcomeOnce(grade: Int, loId: Long): List<Activity>

    @Query("SELECT * FROM Assessment WHERE grade = :grade AND learningOutcomeId = :loId ORDER BY sortOrder, id")
    suspend fun assessmentsForOutcomeOnce(grade: Int, loId: Long): List<Assessment>

    @Query("SELECT * FROM Flashcard WHERE grade = :grade AND learningOutcomeId = :loId ORDER BY sortOrder, id")
    suspend fun flashcardsForOutcomeOnce(grade: Int, loId: Long): List<Flashcard>

    @Query("SELECT * FROM Flashcard WHERE grade = :grade ORDER BY cardType, sortOrder, id")
    suspend fun flashcardsForGradeOnce(grade: Int): List<Flashcard>

    @Query("SELECT * FROM Worksheet WHERE grade = :grade AND learningOutcomeId = :loId ORDER BY sortOrder, id")
    suspend fun worksheetsForOutcomeOnce(grade: Int, loId: Long): List<Worksheet>

    @Query("SELECT * FROM Student WHERE classroomId = :classroomId ORDER BY name")
    suspend fun studentsOnce(classroomId: Long): List<Student>

    @Query("SELECT * FROM StudentOutcomeProgress WHERE grade = :grade")
    suspend fun progressForGradeOnce(grade: Int): List<StudentOutcomeProgress>

    @Query("SELECT * FROM UserResource WHERE teacherId = :teacherId AND grade = :grade ORDER BY createdAt DESC")
    suspend fun userResourcesOnce(teacherId: Long, grade: Int): List<UserResource>

    // ---------- Competencies ----------
    @Query("SELECT * FROM Competency WHERE grade = :grade AND domainId = :domainId ORDER BY sortOrder, id")
    fun observeCompetencies(grade: Int, domainId: Long): Flow<List<Competency>>

    // ---------- Learning Outcomes ----------
    @Query("SELECT * FROM LearningOutcome WHERE grade = :grade ORDER BY domainId, sortOrder, id")
    fun observeLearningOutcomesForGrade(grade: Int): Flow<List<LearningOutcome>>

    @Query("SELECT * FROM LearningOutcome WHERE grade = :grade AND domainId = :domainId ORDER BY sortOrder, id")
    fun observeLearningOutcomes(grade: Int, domainId: Long): Flow<List<LearningOutcome>>

    @Query("SELECT * FROM LearningOutcome WHERE grade = :grade AND competencyId = :competencyId ORDER BY sortOrder, id")
    fun observeLearningOutcomesForCompetency(grade: Int, competencyId: Long): Flow<List<LearningOutcome>>

    @Query("SELECT * FROM LearningOutcome WHERE id = :id")
    suspend fun learningOutcomeById(id: Long): LearningOutcome?

    // ---------- Lessons ----------
    @Query("SELECT * FROM Lesson WHERE grade = :grade ORDER BY domainId, sortOrder, id")
    fun observeLessonsForGrade(grade: Int): Flow<List<Lesson>>

    @Query("SELECT * FROM Lesson WHERE grade = :grade AND domainId = :domainId ORDER BY sortOrder, id")
    fun observeLessons(grade: Int, domainId: Long): Flow<List<Lesson>>

    @Query("SELECT * FROM Lesson WHERE grade = :grade AND learningOutcomeId = :loId ORDER BY sortOrder, id")
    fun observeLessonsForOutcome(grade: Int, loId: Long): Flow<List<Lesson>>

    // ---------- Activities ----------
    @Query("SELECT * FROM Activity WHERE grade = :grade AND learningOutcomeId = :loId ORDER BY sortOrder, id")
    fun observeActivitiesForOutcome(grade: Int, loId: Long): Flow<List<Activity>>

    @Query("SELECT * FROM Activity WHERE grade = :grade AND domainId = :domainId ORDER BY sortOrder, id")
    fun observeActivities(grade: Int, domainId: Long): Flow<List<Activity>>

    // ---------- Assessments ----------
    @Query("SELECT * FROM Assessment WHERE grade = :grade AND learningOutcomeId = :loId ORDER BY sortOrder, id")
    fun observeAssessmentsForOutcome(grade: Int, loId: Long): Flow<List<Assessment>>

    @Query("SELECT * FROM Assessment WHERE grade = :grade AND domainId = :domainId ORDER BY sortOrder, id")
    fun observeAssessments(grade: Int, domainId: Long): Flow<List<Assessment>>

    // ---------- Flashcards ----------
    @Query("SELECT * FROM Flashcard WHERE grade = :grade AND domainId = :domainId ORDER BY sortOrder, id")
    fun observeFlashcards(grade: Int, domainId: Long): Flow<List<Flashcard>>

    @Query("SELECT * FROM Flashcard WHERE grade = :grade AND learningOutcomeId = :loId ORDER BY sortOrder, id")
    fun observeFlashcardsForOutcome(grade: Int, loId: Long): Flow<List<Flashcard>>

    @Query("SELECT * FROM Flashcard WHERE grade = :grade ORDER BY cardType, sortOrder, id")
    fun observeFlashcardsForGrade(grade: Int): Flow<List<Flashcard>>

    // ---------- Worksheets ----------
    @Query("SELECT * FROM Worksheet WHERE grade = :grade AND domainId = :domainId ORDER BY sortOrder, id")
    fun observeWorksheets(grade: Int, domainId: Long): Flow<List<Worksheet>>

    @Query("SELECT * FROM Worksheet WHERE grade = :grade AND learningOutcomeId = :loId ORDER BY sortOrder, id")
    fun observeWorksheetsForOutcome(grade: Int, loId: Long): Flow<List<Worksheet>>

    // ---------- Teaching Resources ----------
    @Query("SELECT * FROM TeachingResource WHERE grade = :grade AND domainId = :domainId ORDER BY sortOrder, id")
    fun observeTeachingResources(grade: Int, domainId: Long): Flow<List<TeachingResource>>

    @Query("SELECT * FROM TeachingResource WHERE grade = :grade AND learningOutcomeId = :loId ORDER BY sortOrder, id")
    fun observeResourcesForOutcome(grade: Int, loId: Long): Flow<List<TeachingResource>>

    // ---------- Classrooms ----------
    @Query("SELECT * FROM Classroom ORDER BY grade")
    fun observeClassrooms(): Flow<List<Classroom>>

    @Query("SELECT * FROM Classroom ORDER BY grade")
    suspend fun getClassroomsOnce(): List<Classroom>

    @Query("SELECT * FROM Classroom WHERE id = :id")
    suspend fun classroomById(id: Long): Classroom?

    @Query("SELECT * FROM Classroom WHERE grade = :grade")
    suspend fun classroomByGrade(grade: Int): Classroom?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertClassroom(classroom: Classroom): Long

    // ---------- Teachers / TeacherClassroom ----------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher): Long

    @Query("SELECT * FROM Teacher WHERE email = :email COLLATE NOCASE LIMIT 1")
    suspend fun teacherByEmail(email: String): Teacher?

    @Query("SELECT * FROM Teacher WHERE id = :id")
    suspend fun teacherById(id: Long): Teacher?

    @Query("SELECT * FROM TeacherClassroom WHERE teacherId = :teacherId")
    fun observeTeacherClassrooms(teacherId: Long): Flow<List<TeacherClassroom>>

    @Query("SELECT * FROM TeacherClassroom WHERE teacherId = :teacherId")
    suspend fun teacherClassroomsOnce(teacherId: Long): List<TeacherClassroom>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacherClassroom(tc: TeacherClassroom): Long

    @Query("DELETE FROM TeacherClassroom WHERE teacherId = :teacherId")
    suspend fun clearTeacherClassrooms(teacherId: Long)

    @Query("SELECT COUNT(*) FROM TeacherClassroom WHERE teacherId = :teacherId")
    suspend fun teacherClassroomCount(teacherId: Long): Int

    // ---------- Students ----------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Query("SELECT * FROM Student WHERE classroomId = :classroomId ORDER BY name")
    fun observeStudents(classroomId: Long): Flow<List<Student>>

    @Query("SELECT * FROM Student WHERE id = :id")
    suspend fun studentById(id: Long): Student?

    // ---------- Student progress ----------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(p: StudentOutcomeProgress): Long

    @Query("SELECT * FROM StudentOutcomeProgress WHERE studentId = :studentId AND learningOutcomeId = :loId")
    suspend fun progressFor(studentId: Long, loId: Long): StudentOutcomeProgress?

    @Query("SELECT * FROM StudentOutcomeProgress WHERE studentId = :studentId")
    fun observeProgressForStudent(studentId: Long): Flow<List<StudentOutcomeProgress>>

    @Query("SELECT * FROM StudentOutcomeProgress WHERE grade = :grade")
    fun observeProgressForGrade(grade: Int): Flow<List<StudentOutcomeProgress>>

    // ---------- User resources (uploaded PDF/PPT) ----------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserResource(r: UserResource): Long

    @Query("DELETE FROM UserResource WHERE id = :id")
    suspend fun deleteUserResource(id: Long)

    @Query("SELECT * FROM UserResource WHERE teacherId = :teacherId AND grade = :grade ORDER BY createdAt DESC")
    fun observeUserResources(teacherId: Long, grade: Int): Flow<List<UserResource>>

    // ---------- Translations ----------
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTranslation(t: ContentTranslation): Long

    // ---------- Search ----------
    @Query("SELECT * FROM LearningOutcome WHERE grade = :grade AND (title LIKE '%' || :q || '%' OR description LIKE '%' || :q || '%' OR code LIKE '%' || :q || '%')")
    fun searchOutcomes(grade: Int, q: String): Flow<List<LearningOutcome>>

    @Query("SELECT * FROM Lesson WHERE grade = :grade AND (title LIKE '%' || :q || '%' OR content LIKE '%' || :q || '%' OR story LIKE '%' || :q || '%')")
    fun searchLessons(grade: Int, q: String): Flow<List<Lesson>>
}
