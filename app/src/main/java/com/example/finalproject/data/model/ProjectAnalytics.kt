package com.example.finalproject.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProjectAnalytics(
    val projeto_uuid: String,
    val nome: String,
    val taxa_conclusao: Float,
    val total_tasks: Int,
    val completed_tasks: Int,
    val avg_task_completion: Double
)
