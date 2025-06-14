package com.example.finalproject.data

import android.content.Context
import androidx.core.content.edit

object PreferencesManager {
    private const val LANGUAGE_KEY = "language_key"
    private const val FIRST_LAUNCH_KEY = "first_launch_key"

    private const val PREFS_NAME = "app_prefs"

    fun saveLanguage(context: Context, language: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit() { putString(LANGUAGE_KEY, language) }
    }

    fun getLanguage(context: Context): String {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(LANGUAGE_KEY, "en") ?: "en" // Padrão: inglês
    }

    /**
     * Check if this is the first time the app is launched
     * @return true if first launch, false otherwise
     */
    fun isFirstLaunch(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(FIRST_LAUNCH_KEY, true)
    }

    /**
     * Mark the first launch as complete after showing intro slider
     */
    fun setFirstLaunchComplete(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit { putBoolean(FIRST_LAUNCH_KEY, false) }
    }
}