package com.example.finalproject.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

fun updateAppLanguage(context: Context, language: String) {
    val locale = Locale(language)
    Locale.setDefault(locale)
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}