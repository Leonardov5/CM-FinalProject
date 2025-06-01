package com.example.finalproject.data.model

/**
 * Modelo que representa um usuário no sistema
 */
data class User(
    val id: String,
    val email: String,
    val displayName: String? = null
)
