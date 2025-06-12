package com.example.finalproject.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UtilizadorInfo(
    @SerialName("nome")
    val nome: String = "",
    @SerialName("fotografia")
    val fotografia: String? = null,
    @SerialName("username")
    val username: String = "",
    @SerialName("admin")
    val admin: Boolean = false
)

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
    val createdAt: String = "",

    @SerialName("utilizador")
    val utilizador: UtilizadorInfo? = null
) {
    val nome: String get() = utilizador?.nome ?: ""
    val fotografia: String? get() = utilizador?.fotografia
    val username: String get() = utilizador?.username ?: ""
    val admin: Boolean get() = utilizador?.admin ?: false
}
