package com.example.finalproject.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Observacao(
    @SerialName("observacao_uuid")
    val id: String? = null,

    @SerialName("tarefa_uuid")
    val tarefaId: String,

    @SerialName("observacao")
    val observacao: String,

    @SerialName("anexos")
    val anexos: List<String> = emptyList(),

    @SerialName("created_by")
    val createdBy: String? = null,

    @SerialName("modified_by")
    val modifiedBy: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)