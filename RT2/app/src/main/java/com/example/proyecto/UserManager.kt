package com.example.proyecto

import android.content.Context

@Deprecated("Usar FirebaseRepository")
object UserManager {
    data class User(
        val email: String, val password: String,
        val role: String,  val name: String
    )
    fun saveUser(context: Context, user: User) {}
    fun authenticate(context: Context, email: String,
                     password: String, role: String) = false
    fun userExists(context: Context, email: String) = false
}