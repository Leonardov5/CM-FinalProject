package com.example.finalproject.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de dados para representar um projeto
 */
@Serializable
data class Projeto(
    @SerialName("projeto_uuid")
    val id: String? = null,
    val nome: String = "",
    val descricao: String? = null,
    val status: String = "ativo",
    @SerialName("taxa_conclusao")
    val taxaConclusao: Double = 0.0,
    @SerialName("created_by")
    val createdBy: String? = null,
    @SerialName("modified_by")
    val modifiedBy: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
