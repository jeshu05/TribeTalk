package com.alchemists.tribetalk.curriculum

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * High-level access layer over [AppDatabase]. All operations run against the
 * bundled SQLite database so the entire NIPUN/FLN module works offline.
 */
class NipunRepository private constructor(
    private val dao: NipunDao
) {
    // ---------- Auth (local teacher accounts) ----------
    @Volatile
    private var loggedInTeacherId: Long = -1

    private val prefs: SharedPreferences by lazy {
        appPrefs
    }

    var currentTeacherId: Long
        get() = loggedInTeacherId
        set(value) {
            loggedInTeacherId = value
        }

    suspend fun registerTeacher(name: String, email: String, password: String): Teacher? {
        if (dao.teacherByEmail(email) != null) return null
        val id = dao.insertTeacher(
            Teacher(name = name, email = email, passwordHash = password, displayName = name)
        )
        return dao.teacherById(id)
    }

    suspend fun loginTeacher(email: String, password: String): Teacher? {
        val teacher = dao.teacherByEmail(email) ?: return null
        if (teacher.passwordHash != password) return null
        loggedInTeacherId = teacher.id
        saveSession(teacher.id)
        return teacher
    }

    /**
     * Replaces the teacher's classroom set (setup wizard). Caller supplies grades.
     */
    suspend fun setTeacherClassrooms(teacherId: Long, grades: List<Int>) {
        dao.clearTeacherClassrooms(teacherId)
        grades.forEach { grade ->
            val classroom = dao.classroomByGrade(grade) ?: return@forEach
            dao.insertTeacherClassroom(
                TeacherClassroom(teacherId = teacherId, classroomId = classroom.id, grade = grade)
            )
        }
    }

    suspend fun getTeacherClassrooms(teacherId: Long): List<Classroom> {
        val refs = dao.teacherClassroomsOnce(teacherId)
        return refs.mapNotNull { dao.classroomById(it.classroomId) }.sortedBy { it.grade }
    }

    fun observeTeacherClassrooms(teacherId: Long): Flow<List<TeacherClassroom>> =
        dao.observeTeacherClassrooms(teacherId)

    suspend fun teacherHasClassrooms(teacherId: Long): Boolean = dao.teacherClassroomCount(teacherId) > 0

    // ---------- Curriculum browsing ----------
    suspend fun currentTeacherById(id: Long): Teacher? = dao.teacherById(id)
    suspend fun allClassrooms(): List<Classroom> = dao.getClassroomsOnce()
    suspend fun allDomains(): List<Domain> = dao.domainsOnce()
    suspend fun outcomesForGrade(grade: Int): List<LearningOutcome> = dao.learningOutcomesForGradeOnce(grade)
    suspend fun lessonsForOutcome(grade: Int, loId: Long): List<Lesson> = dao.lessonsForOutcomeOnce(grade, loId)
    suspend fun activitiesForOutcome(grade: Int, loId: Long): List<Activity> = dao.activitiesForOutcomeOnce(grade, loId)
    suspend fun assessmentsForOutcome(grade: Int, loId: Long): List<Assessment> = dao.assessmentsForOutcomeOnce(grade, loId)
    suspend fun flashcardsForOutcome(grade: Int, loId: Long): List<Flashcard> = dao.flashcardsForOutcomeOnce(grade, loId)
    suspend fun flashcardsForGrade(grade: Int): List<Flashcard> = dao.flashcardsForGradeOnce(grade)
    suspend fun worksheetsForOutcome(grade: Int, loId: Long): List<Worksheet> = dao.worksheetsForOutcomeOnce(grade, loId)
    suspend fun studentsForClassroom(classroomId: Long): List<Student> = dao.studentsOnce(classroomId)
    suspend fun progressForGrade(grade: Int): List<StudentOutcomeProgress> = dao.progressForGradeOnce(grade)
    suspend fun userResourcesFor(teacherId: Long, grade: Int): List<UserResource> = dao.userResourcesOnce(teacherId, grade)

    fun observeGrades(): Flow<List<Int>> = dao.observeGrades()
    fun observeDomains(): Flow<List<Domain>> = dao.observeDomains()
    fun observeDomainsOfKind(kind: String): Flow<List<Domain>> = dao.observeDomainsOfKind(kind)
    fun observeCompetencies(grade: Int, domainId: Long): Flow<List<Competency>> = dao.observeCompetencies(grade, domainId)
    fun observeLearningOutcomes(grade: Int, domainId: Long): Flow<List<LearningOutcome>> = dao.observeLearningOutcomes(grade, domainId)
    fun observeLearningOutcomesForGrade(grade: Int): Flow<List<LearningOutcome>> = dao.observeLearningOutcomesForGrade(grade)
    fun observeLessonsForOutcome(grade: Int, loId: Long): Flow<List<Lesson>> = dao.observeLessonsForOutcome(grade, loId)
    fun observeActivitiesForOutcome(grade: Int, loId: Long): Flow<List<Activity>> = dao.observeActivitiesForOutcome(grade, loId)
    fun observeAssessmentsForOutcome(grade: Int, loId: Long): Flow<List<Assessment>> = dao.observeAssessmentsForOutcome(grade, loId)
    fun observeFlashcardsForOutcome(grade: Int, loId: Long): Flow<List<Flashcard>> = dao.observeFlashcardsForOutcome(grade, loId)
    fun observeFlashcardsForGrade(grade: Int): Flow<List<Flashcard>> = dao.observeFlashcardsForGrade(grade)
    fun observeWorksheetsForOutcome(grade: Int, loId: Long): Flow<List<Worksheet>> = dao.observeWorksheetsForOutcome(grade, loId)
    fun observeTeachingResources(grade: Int, domainId: Long): Flow<List<TeachingResource>> = dao.observeTeachingResources(grade, domainId)
    fun searchOutcomes(grade: Int, q: String): Flow<List<LearningOutcome>> = dao.searchOutcomes(grade, q)

    // ---------- Students & progress ----------
    suspend fun addStudent(classroomId: Long, name: String, admissionNo: String?): Long =
        dao.insertStudent(Student(classroomId = classroomId, name = name, admissionNo = admissionNo))

    fun observeStudents(classroomId: Long): Flow<List<Student>> = dao.observeStudents(classroomId)
    fun observeProgressForGrade(grade: Int): Flow<List<StudentOutcomeProgress>> = dao.observeProgressForGrade(grade)
    fun observeProgressForStudent(studentId: Long): Flow<List<StudentOutcomeProgress>> = dao.observeProgressForStudent(studentId)

    suspend fun setStudentProgress(studentId: Long, learningOutcomeId: Long, grade: Int, status: String) {
        val existing = dao.progressFor(studentId, learningOutcomeId)
        if (existing != null) {
            dao.insertProgress(existing.copy(status = status, updatedAt = System.currentTimeMillis()))
        } else {
            dao.insertProgress(
                StudentOutcomeProgress(studentId = studentId, learningOutcomeId = learningOutcomeId, grade = grade, status = status)
            )
        }
    }

    // ---------- User resources (uploaded PDF/PPT) ----------
    fun observeUserResources(teacherId: Long, grade: Int): Flow<List<UserResource>> =
        dao.observeUserResources(teacherId, grade)

    suspend fun addUserResource(
        teacherId: Long, grade: Int, domainId: Long?, learningOutcomeId: Long?,
        title: String, filePath: String, fileType: String, mimeType: String?, sizeBytes: Long
    ): Long = dao.insertUserResource(
        UserResource(teacherId = teacherId, grade = grade, domainId = domainId,
            learningOutcomeId = learningOutcomeId, title = title, filePath = filePath,
            fileType = fileType, mimeType = mimeType, sizeBytes = sizeBytes)
    )

    suspend fun deleteUserResource(id: Long) = dao.deleteUserResource(id)

    // ---------- Session persistence ----------
    fun saveSession(teacherId: Long) {
        prefs.edit().putLong(KEY_SESSION, teacherId).apply()
        loggedInTeacherId = teacherId
    }

    fun restoreSession(): Long {
        val id = prefs.getLong(KEY_SESSION, -1L)
        loggedInTeacherId = id
        return id
    }

    fun clearSession() {
        prefs.edit().remove(KEY_SESSION).apply()
        loggedInTeacherId = -1
    }

    companion object {
        private const val PREFS = "tribetalk_nipun"
        private const val KEY_SESSION = "current_teacher_id"

        lateinit var appPrefs: SharedPreferences
            private set

        @Volatile
        private var instance: NipunRepository? = null

        private val seedScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

        fun getInstance(context: Context): NipunRepository {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val dao = AppDatabase.getInstance(context, DemoDataSeeder()).nipunDao()
                    val repo = NipunRepository(dao)
                    appPrefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    instance = repo
                    // Ensure classroom + demo curriculum are present for first offline run.
                    seedScope.launch {
                        try {
                            DemoDataSeeder().seed(dao)
                        } catch (t: Throwable) {
                            // Ignore duplicate-seed races; DB regions are guarded by count checks.
                        }
                    }
                    repo
                }
            }
        }
    }
}

private fun <T> Flow<T>.flowIdentity(): Flow<T> = this
