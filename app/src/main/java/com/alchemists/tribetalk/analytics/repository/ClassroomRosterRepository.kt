package com.alchemists.tribetalk.analytics.repository

import android.content.Context
import com.alchemists.tribetalk.analytics.models.ClassroomRoster

object ClassroomRosterRepository {

    private val defaultRoster = ClassroomRoster(
        classId = "class_2a",
        className = "Class 2A",
        students = List(20) { idx -> "Student %02d".format(idx + 1) }
    )

    fun getRoster(context: Context? = null): ClassroomRoster {
        return defaultRoster
    }

    fun getNextStudent(currentStudentId: String, roster: ClassroomRoster = defaultRoster): String {
        val idx = roster.students.indexOf(currentStudentId)
        if (idx == -1 || idx >= roster.students.size - 1) {
            return roster.students.first()
        }
        return roster.students[idx + 1]
    }
}
