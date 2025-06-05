package com.example.finalproject.data.repository

import com.example.finalproject.data.model.User
import com.example.finalproject.data.service.AuthService

/**
 * Repositório para gerenciar operações relacionadas a usuários
 */
class UserRepository {
    /**
     * Registra um novo usuário
     * @param email Email do usuário
     * @param password Senha do usuário
     * @return true se o registro for bem-sucedido, false caso contrário
     */
    suspend fun registerUser(email: String, password: String): Boolean {
        return AuthService.register(email, password)
    }

    /**
     * Realiza login do usuário
     * @param email Email do usuário
     * @param password Senha do usuário
     * @return true se o login for bem-sucedido, false caso contrário
     */
    suspend fun loginUser(email: String, password: String): Boolean {
        return AuthService.login(email, password)
    }

    /**
     * Realiza logout do usuário atual
     * @return true se o logout for bem-sucedido, false caso contrário
     */
    suspend fun logoutUser(): Boolean {
        return AuthService.logout()
    }

    /**
     * Verifica se há um usuário autenticado
     * @return true se houver um usuário autenticado, false caso contrário
     */
    fun isUserAuthenticated(): Boolean {
        return AuthService.isAuthenticated()
    }

    /**
     * Obtém o usuário atual
     * @return Objeto User com os dados do usuário atual ou null se não estiver autenticado
     */
    fun getCurrentUser(): User? {
        val userId = AuthService.getCurrentUserId() ?: return null
        val userEmail = AuthService.getCurrentUserEmail() ?: return null

        return User(
            id = userId,
            email = userEmail
        )
    }
}