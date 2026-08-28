package com.alchemists.tribetalk.translation

enum class Language(val displayName: String, val code: String) {
    HINDI("Hindi", "hi"),
    SANTALI("Santali", "sat");

    override fun toString(): String = displayName
}
