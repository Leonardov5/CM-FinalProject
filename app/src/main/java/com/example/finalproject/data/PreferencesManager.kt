package com.example.finalproject.data

import android.content.Context
import androidx.core.content.edit

object PreferencesManager {
    private const val LANGUAGE_KEY = "language_key"

    fun saveLanguage(context: Context, language: String) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit() { putString(LANGUAGE_KEY, language) }
    }

    fun getLanguage(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString(LANGUAGE_KEY, "en") ?: "en"
    }
}