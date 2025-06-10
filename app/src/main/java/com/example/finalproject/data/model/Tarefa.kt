package com.example.finalproject.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tarefa(
    @SerialName("tarefa_uuid")
    val id: String? = null,

    @SerialName("projeto_uuid")
    val projetoId: String,

    @SerialName("nome")
    val nome: String,

    @SerialName("descricao")
    val descricao: String? = null,

    @SerialName("prioridade")
    val prioridade: String = "media",

    @SerialName("status")
    val status: String = "pendente",

    @SerialName("data_inicio")
    val dataInicio: String? = null,

    @SerialName("data_fim")
    val dataFim: String? = null,

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
