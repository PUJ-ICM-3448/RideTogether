package com.example.proyecto

import android.content.Context

object UserManager {
    data class User(
        val email: String,
        val password: String,
        val role: String,
        val name: String
    )

    fun saveUser(context: Context, user: User) {
        val sharedPref = context.getSharedPreferences("UsersPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_${user.email}_name", user.name)
            putString("user_${user.email}_password", user.password)
            putString("user_${user.email}_role", user.role)
            apply()
        }
    }

    fun authenticate(context: Context, email: String, password: String, role: String): Boolean {
        val sharedPref = context.getSharedPreferences("UsersPrefs", Context.MODE_PRIVATE)
        val savedPassword = sharedPref.getString("user_${email}_password", null)
        val savedRole = sharedPref.getString("user_${email}_role", null)
        
        return savedPassword == password && savedRole == role
    }

    fun userExists(context: Context, email: String): Boolean {
        val sharedPref = context.getSharedPreferences("UsersPrefs", Context.MODE_PRIVATE)
        return sharedPref.contains("user_${email}_password")
    }
}
