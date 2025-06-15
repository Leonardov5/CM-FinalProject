package com.example.finalproject.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskAnalytics(
    val tarefa_uuid: String,
    val task_name: String,
    val task_description: String? = null,
    val projeto_uuid: String,
    val project_name: String,
    val prioridade: String,
    val status: String,
    val taxa_conclusao: Float,
    val data_inicio: String? = null,
    val data_fim: String? = null,
    val total_works: Int, 
    val total_minutes_spent: Int,
    val total_contribution: Float = 0f,
    val total_observations: Int,
    val total_assigned_users: Int = 0,
    val days_since_creation: Int = 0,
    val days_to_deadline: Int? = null,
    val is_overdue: Boolean = false,
    val created_by_name: String? = null,
    val modified_by_name: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)
