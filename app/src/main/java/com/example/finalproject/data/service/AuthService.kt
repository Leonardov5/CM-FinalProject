package com.example.finalproject.data.service

import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Serviço responsável por operações de autenticação com Supabase
 */
object AuthService {
    private val supabase = SupabaseProvider.client

    /**
     * Registra um novo usuário com email e senha
     * @param email O email do usuário
     * @param password A senha do usuário
     * @return true se o registro for bem-sucedido, false caso contrário
     */
    suspend fun register(email: String, password: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Faz login com email e senha
     * @param email O email do usuário
     * @param password A senha do usuário
     * @return true se o login for bem-sucedido, false caso contrário
     */
    suspend fun login(email: String, password: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Verifica se o usuário está autenticado
     * @return true se o usuário estiver autenticado, false caso contrário
     */
    fun isAuthenticated(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }

    /**
     * Faz logout do usuário atual
     * @return true se o logout for bem-sucedido, false caso contrário
     */
    suspend fun logout(): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                supabase.auth.signOut()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Obtém o ID do usuário atual
     * @return O ID do usuário ou null se não estiver autenticado
     */
    fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    /**
     * Obtém o email do usuário atual
     * @return O email do usuário ou null se não estiver autenticado
     */
    fun getCurrentUserEmail(): String? {
        return supabase.auth.currentUserOrNull()?.email
    }
}
