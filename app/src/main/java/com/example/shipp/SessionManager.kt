package com.example.shipp

import android.content.Context

object SessionManager {
    private const val PREF_NAME = "user_session"
    private const val KEY_EMAIL = "user_email"

    //guardar email de usuario
    fun saveUserSession(context: Context, email: String) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(KEY_EMAIL, email)
            apply()
        }
    }

    //get email de usuario
    fun getUserSession(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_EMAIL, null)
    }

    //cerrar sesion, eliminar datos de usuario
    fun logoutUser(context: Context) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove(KEY_EMAIL)
            apply()
        }
    }

}