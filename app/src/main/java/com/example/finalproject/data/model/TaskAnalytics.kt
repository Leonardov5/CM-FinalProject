package com.example.finalproject.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskAnalytics(
    val tarefa_uuid: String,
    val task_name: String, // Changed from nome
    val task_description: String? = null, // Added to match view
    val projeto_uuid: String,
    val project_name: String, // Changed from projeto_nome
    val prioridade: String,
    val status: String,
    val taxa_conclusao: Float,
    val data_inicio: String? = null,
    val data_fim: String? = null,
    val total_works: Int, // Changed from total_trabalhos
    val total_minutes_spent: Int, // Changed from total_tempo_dispensado
    val total_contribution: Float = 0f, // Added to match view
    val total_observations: Int, // Changed from total_observacoes
    val total_assigned_users: Int = 0, // Added to match view
    val days_since_creation: Int = 0, // Added to match view
    val days_to_deadline: Int? = null, // Added to match view
    val is_overdue: Boolean = false, // Added to match view
    val created_by_name: String? = null, // Added to match view
    val modified_by_name: String? = null, // Added to match view
    val created_at: String? = null, // Added to match view
    val updated_at: String? = null // Added to match view
)
