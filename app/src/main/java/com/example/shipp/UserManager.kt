package com.example.shipp

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

//Data class para representar un usuario
data class User(
    val user: String,
    val nombre: String,
    val apellido: String,
    val email: String,
    val password: String
)

object UserManager {
    private const val PREFS_NAME = "user_prefs"
    private const val USERS_KEY = "users"

    //aqui obtenemos la lista de usuarios almacenada en SharedPreferences
    fun getUsers(context: Context): MutableList<User> {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val usersJson = sharedPreferences.getString(USERS_KEY, "")
        return if (!usersJson.isNullOrEmpty()) {
            val type = object : TypeToken<MutableList<User>>() {}.type
            Gson().fromJson(usersJson, type)
        } else {
            mutableListOf()
        }
    }

    //agregamos un usuario nuevo y lo guardamos en sharedpreferences
    fun addUser(context: Context, user: User) {
        val users = getUsers(context)
        if (!users.any { it.email == user.email }){ //evitamos subir al mismo usuario 2 veces
            users.add(user)
            saveUsers(context, users)
        }

    }

    //guardamos la lista de usuarios en sharedPreferences
    private fun saveUsers(context: Context, users: List<User>) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val usersJson = Gson().toJson(users)
        sharedPreferences.edit {
            putString(USERS_KEY, usersJson)
            apply()
        }
    }

    //comprobamos si un usuario existe y validamos su contrasenna
    fun validateUser(context: Context, email: String, password: String): Boolean {
        val users = getUsers(context)
        return users.any { it.email == email && it.password == password}
    }

}