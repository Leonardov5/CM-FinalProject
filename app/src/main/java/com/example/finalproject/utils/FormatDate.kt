package com.example.finalproject.utils

import java.text.SimpleDateFormat
import java.util.Locale

fun formatDate(isoDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(isoDate)
        if (date != null) {
            outputFormat.format(date)
        } else {
            isoDate
        }
    } catch (e: Exception) {
        isoDate
    }
}