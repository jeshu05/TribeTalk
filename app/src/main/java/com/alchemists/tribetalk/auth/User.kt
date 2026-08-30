package com.alchemists.tribetalk.auth

data class User(
    val name: String,
    val email: String,
    val password: String,
    val role: UserRole
) {
    val displayName: String get() = if (name.isBlank()) email else name
}