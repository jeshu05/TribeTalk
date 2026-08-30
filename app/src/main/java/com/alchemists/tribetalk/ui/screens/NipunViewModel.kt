package com.alchemists.tribetalk.ui.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alchemists.tribetalk.curriculum.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NipunViewModel(private val repo: NipunRepository) : ViewModel() {

    val teacher: MutableLiveData<Teacher?> = MutableLiveData()
    val classrooms: MutableLiveData<List<Classroom>> = MutableLiveData(emptyList())
    val grades: MutableLiveData<List<Classroom>> = MutableLiveData(emptyList())
    val domains: MutableLiveData<List<Domain>> = MutableLiveData(emptyList())
    val outcomes: MutableLiveData<List<LearningOutcome>> = MutableLiveData(emptyList())
    val lessons: MutableLiveData<List<Lesson>> = MutableLiveData(emptyList())
    val activities: MutableLiveData<List<Activity>> = MutableLiveData(emptyList())
    val assessments: MutableLiveData<List<Assessment>> = MutableLiveData(emptyList())
    val flashcards: MutableLiveData<List<Flashcard>> = MutableLiveData(emptyList())
    val worksheets: MutableLiveData<List<Worksheet>> = MutableLiveData(emptyList())
    val students: MutableLiveData<List<Student>> = MutableLiveData(emptyList())
    val progress: MutableLiveData<List<StudentOutcomeProgress>> = MutableLiveData(emptyList())
    val userResources: MutableLiveData<List<UserResource>> = MutableLiveData(emptyList())
    val error: MutableLiveData<String?> = MutableLiveData(null)

    private var currentGrade = 0

    fun boot() {
        viewModelScope.launch {
            val id = repo.restoreSession()
            if (id > 0) {
                teacher.value = repo.currentTeacherById(id)
                loadTeacherClassrooms(id)
            }
        }
    }

    fun login(email: String, password: String, onDone: (Teacher?) -> Unit) {
        viewModelScope.launch {
            val t = withContext(Dispatchers.IO) { repo.loginTeacher(email, password) }
            teacher.value = t
            if (t != null) {
                loadTeacherClassrooms(t.id)
                onDone(t)
            } else {
                error.value = "Invalid email or password"
            }
        }
    }

    fun register(name: String, email: String, password: String, onDone: (Teacher?) -> Unit) {
        viewModelScope.launch {
            val t = withContext(Dispatchers.IO) { repo.registerTeacher(name, email, password) }
            teacher.value = t
            if (t != null) {
                repo.saveSession(t.id)
                loadTeacherClassrooms(t.id)
                onDone(t)
            } else {
                error.value = "That email is already registered"
            }
        }
    }

    fun setTeacherClassrooms(grades: List<Int>, onDone: () -> Unit) {
        val t = teacher.value ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo.setTeacherClassrooms(t.id, grades) }
            loadTeacherClassrooms(t.id)
            onDone()
        }
    }

    fun loadAllClassrooms(onDone: (List<Classroom>) -> Unit) {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { repo.allClassrooms() }
            grades.value = emptyList()
            onDone(list)
        }
    }

    fun loadDomains(onDone: (List<Domain>) -> Unit) {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { repo.allDomains() }
            domains.value = list
            onDone(list)
        }
    }

    fun loadOutcomes(grade: Int, onDone: (List<LearningOutcome>) -> Unit) {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { repo.outcomesForGrade(grade) }
            outcomes.value = list
            onDone(list)
        }
    }

    fun loadOutcomeDetail(grade: Int, loId: Long) {
        currentGrade = grade
        viewModelScope.launch {
            lessons.value = withContext(Dispatchers.IO) { repo.lessonsForOutcome(grade, loId) }
            activities.value = withContext(Dispatchers.IO) { repo.activitiesForOutcome(grade, loId) }
            assessments.value = withContext(Dispatchers.IO) { repo.assessmentsForOutcome(grade, loId) }
            flashcards.value = withContext(Dispatchers.IO) { repo.flashcardsForOutcome(grade, loId) }
            worksheets.value = withContext(Dispatchers.IO) { repo.worksheetsForOutcome(grade, loId) }
        }
    }

    fun loadFlashcards(grade: Int, loId: Long?, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            flashcards.value = if (loId != null) {
                withContext(Dispatchers.IO) { repo.flashcardsForOutcome(grade, loId) }
            } else {
                withContext(Dispatchers.IO) { repo.flashcardsForGrade(grade) }
            }
            onDone()
        }
    }

    fun loadWorksheets(grade: Int, loId: Long?, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            worksheets.value = if (loId != null) {
                withContext(Dispatchers.IO) { repo.worksheetsForOutcome(grade, loId) }
            } else {
                emptyList()
            }
            onDone()
        }
    }

    fun loadStudents(classroomId: Long) {
        viewModelScope.launch {
            students.value = withContext(Dispatchers.IO) { repo.studentsForClassroom(classroomId) }
        }
    }

    fun addStudent(classroomId: Long, name: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo.addStudent(classroomId, name, null) }
            loadStudents(classroomId)
        }
    }

    fun loadProgress(grade: Int) {
        currentGrade = grade
        viewModelScope.launch {
            progress.value = withContext(Dispatchers.IO) { repo.progressForGrade(grade) }
        }
    }

    fun setProgress(studentId: Long, outcomeId: Long, grade: Int, status: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo.setStudentProgress(studentId, outcomeId, grade, status) }
            loadProgress(grade)
        }
    }

    fun loadUserResources(grade: Int) {
        val t = teacher.value ?: return
        viewModelScope.launch {
            userResources.value = withContext(Dispatchers.IO) { repo.userResourcesFor(t.id, grade) }
        }
    }

    fun addUserResource(
        grade: Int, domainId: Long?, outcomeId: Long?, title: String,
        path: String, fileType: String, mime: String?, size: Long
    ) {
        val t = teacher.value ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repo.addUserResource(t.id, grade, domainId, outcomeId, title, path, fileType, mime, size)
            }
            loadUserResources(grade)
        }
    }

    private fun loadTeacherClassrooms(teacherId: Long) {
        viewModelScope.launch {
            classrooms.value = withContext(Dispatchers.IO) { repo.getTeacherClassrooms(teacherId) }
        }
    }
}
