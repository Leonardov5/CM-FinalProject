package com.example.finalproject.data.repository

import com.example.finalproject.data.model.User
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.data.service.SupabaseProvider
import io.github.jan.supabase.postgrest.from

class UtilizadorRepository {
    suspend fun registerUser(email: String, password: String): Boolean {
        return AuthService.register(email, password)
    }

    suspend fun loginUser(email: String, password: String): Boolean {
        return AuthService.login(email, password)
    }

    fun getCurrentUser(): User? {
        val userId = AuthService.getCurrentUserId() ?: return null
        val userEmail = AuthService.getCurrentUserEmail() ?: return null

        return User(
            id = userId,
            email = userEmail
        )
    }

    suspend fun listarTodosUtilizadores(): List<User> {
        return try {
            SupabaseProvider.client.from("utilizador")
                .select()
                .decodeList<User>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun eliminarUtilizador(userId: String): Boolean {
        return try {
            SupabaseProvider.client.from("utilizador")
                .delete{
                    filter { eq("utilizador_uuid", userId) }
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun atualizarUtilizador(userId: String, nome: String, username: String): Boolean {
        return try {
            SupabaseProvider.client.from("utilizador")
                .update(
                    mapOf(
                        "nome" to nome,
                        "username" to username,
                    )
                ) {
                    filter { eq("utilizador_uuid", userId) }
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}