package com.example.finalproject.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo para a relação entre Utilizador e Projeto
 */
@Serializable
data class UserProject(
    @SerialName("utilizador_uuid")
    val userId: String,
    @SerialName("projeto_uuid")
    val projectId: String,
    @SerialName("ativo")
    val active: Boolean = true,
    @SerialName("e_gestor")
    val isManager: Boolean = false,
    @SerialName("created_at")
    val createdAt: String = ""
)