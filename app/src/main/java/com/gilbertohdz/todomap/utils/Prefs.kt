package com.gilbertohdz.todomap.utils

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var userToken: String?
        get() = prefs.getString(PREF_NAME, null)
        set(value) {
            prefs.edit().putString(USER_TOKEN, value).apply()
        }

    companion object {
        private const val PREF_NAME = "com.gilbertohdz.todomap.prefs"
        private const val USER_TOKEN = "com.gilbertohdz.user.token"
    }
}