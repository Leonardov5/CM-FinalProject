package com.example.finalproject.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Utilizador(
    @SerialName("utilizador_uuid")
    val id: String = "",
    val nome: String = "",
    val fotografia: String? = null,
    val username: String = "",
    var email: String = "",
    val admin: Boolean = false,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = ""
)
