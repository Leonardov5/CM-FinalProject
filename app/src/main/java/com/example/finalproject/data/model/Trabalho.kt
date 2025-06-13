package com.example.finalproject.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Trabalho(
    @SerialName("trabalho_uuid")
    val id: String? = null,

    @SerialName("tarefa_uuid")
    val tarefaId: String,

    @SerialName("data")
    val data: String,

    @SerialName("local")
    val local: String? = null,

    @SerialName("taxa_conclusao")
    val taxaConclusao: Double,

    @SerialName("tempo_dispensado")
    val tempoDispensado: Int,

    @SerialName("created_by")
    val createdBy: String? = null,

    @SerialName("modified_by")
    val modifiedBy: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)
