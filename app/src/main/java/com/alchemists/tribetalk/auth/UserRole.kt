package com.alchemists.tribetalk.auth

enum class UserRole(val displayName: String) {
    ADMIN("Admin"),
    TEACHER("Teacher"),
    STUDENT("Student");

    companion object {
        fun fromName(name: String): UserRole =
            entries.firstOrNull { it.name == name } ?: STUDENT
    }
}