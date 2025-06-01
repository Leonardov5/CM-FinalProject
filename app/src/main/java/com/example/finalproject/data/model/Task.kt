package com.example.finalproject.data.model

data class Task(
    val id: Int,
    val title: String,
    val status: TaskStatus,
    val created: String? = null,
    val priority: Int? = null,
    val description: String? = null
)

enum class TaskStatus {
    TO_DO, ON_GOING, COMPLETED
}
