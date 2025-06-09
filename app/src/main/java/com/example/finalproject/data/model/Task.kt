package com.example.finalproject.data.model

data class Task(
    val id: String,
    val title: String,
    val description: String? = null,
    val status: TaskStatus,
    val projectId: String? = null,
    val assignedTo: List<String> = emptyList(),
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val priority: Int? = null
)

enum class TaskStatus {
    TO_DO, ON_GOING, COMPLETED
}
