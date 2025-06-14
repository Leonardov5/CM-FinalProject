package com.example.finalproject.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Notificacao(
    @SerialName("notificacao_uuid")
    val id: String? = null,

    @SerialName("utilizador_uuid")
    val utilizadorId: String,

    @SerialName("mensagem")
    val mensagem: String,

    @SerialName("objeto")
    val objeto: String? = null,

    @SerialName("data")
    val data: String? = null,

    @SerialName("vista")
    val vista: Boolean = false,

    @SerialName("created_at")
    val createdAt: String? = null
)