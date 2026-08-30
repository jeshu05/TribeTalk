package com.alchemists.tribetalk.auth

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class AuthManager(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun register(user: User): Boolean {
        val users = getAllJson()
        for (i in 0 until users.length()) {
            if (users.getJSONObject(i).getString("email").equals(user.email, ignoreCase = true)) {
                return false
            }
        }
        users.put(toJson(user))
        prefs.edit().putString(KEY_USERS, users.toString()).apply()
        return true
    }

    fun login(email: String, password: String): User? {
        val users = getAllJson()
        for (i in 0 until users.length()) {
            val obj = users.getJSONObject(i)
            if (obj.getString("email").equals(email.trim(), ignoreCase = true) &&
                obj.getString("password") == password
            ) {
                return fromJson(obj)
            }
        }
        return null
    }

    fun allUsers(): List<User> {
        val users = getAllJson()
        return buildList {
            for (i in 0 until users.length()) {
                add(fromJson(users.getJSONObject(i)))
            }
        }
    }

    fun clearAll() {
        prefs.edit().remove(KEY_USERS).apply()
    }

    private fun getAllJson(): JSONArray {
        val raw = prefs.getString(KEY_USERS, null)
        return if (raw.isNullOrBlank()) JSONArray() else JSONArray(raw)
    }

    private fun toJson(user: User): JSONObject = JSONObject().apply {
        put("name", user.name)
        put("email", user.email.trim())
        put("password", user.password)
        put("role", user.role.name)
    }

    private fun fromJson(obj: JSONObject): User = User(
        name = obj.getString("name"),
        email = obj.getString("email"),
        password = obj.getString("password"),
        role = UserRole.fromName(obj.getString("role"))
    )

    companion object {
        private const val PREFS_NAME = "tribetalk_auth"
        private const val KEY_USERS = "registered_users"
    }
}