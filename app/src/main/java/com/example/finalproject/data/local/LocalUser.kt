package com.example.finalproject.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class LocalUser(
    @PrimaryKey val id: String,
    val username: String,
    val nome: String,
    val email: String,
    val fotografia: String?,
    val updatedAt: String
)