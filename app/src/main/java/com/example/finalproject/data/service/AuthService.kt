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
                try {
                    // Tenta fazer logout no servidor
                    supabase.auth.signOut()
                } catch (e: Exception) {
                    // Se falhar no servidor, apenas loga o erro
                    e.printStackTrace()
                    // Não retorna false aqui, pois ainda queremos limpar a sessão local
                }

                // Limpa qualquer sessão local, independentemente do resultado do signOut()
                supabase.auth.clearSession()
            }
            // Sempre retorna true, pois mesmo que o logout no servidor falhe,
            // ainda removemos a sessão localmente
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

    /**
     * Refresca a sessão do usuário atual
     * @return true se a sessão for atualizada com sucesso, false caso contrário
     */
    suspend fun refreshSession(): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                supabase.auth.refreshCurrentSession()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Atualiza a senha do usuário atual
     * @param currentPassword A senha atual do usuário
     * @param newPassword A nova senha do usuário
     * @return true se a atualização for bem-sucedida, false caso contrário
     */
    suspend fun updatePassword(currentPassword: String, newPassword: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                try {
                    // Verificar a senha atual
                    val currentEmail = getCurrentUserEmail() ?: return@withContext false

                    // Faz login para verificar a senha atual
                    supabase.auth.signInWith(Email) {
                        this.email = currentEmail
                        this.password = currentPassword
                    }

                    // Se chegou aqui, a senha está correta, então podemos atualizar a senha
                    supabase.auth.modifyUser {
                        this.password = newPassword
                    }

                    // Atualizar a sessão para refletir as alterações
                        supabase.auth.refreshCurrentSession()

                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Atualiza o email do usuário atual na autenticação do Supabase
     * @param email O novo email do usuário
     * @param password A senha atual do usuário para verificação
     * @return true se a atualização for bem-sucedida, false caso contrário
     */
    suspend fun updateEmail(email: String, password: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                // Primeiro, verificar se a senha está correta fazendo login
                try {
                    // Verificar a senha atual
                    val currentEmail = getCurrentUserEmail() ?: return@withContext false

                    // Faz login para verificar a senha (sem alterar a sessão atual)
                    supabase.auth.signInWith(Email) {
                        this.email = currentEmail
                        this.password = password
                    }

                    // Se chegou aqui, a senha está correta, então podemos atualizar o email
                    supabase.auth.modifyUser {
                        this.email = email
                    }

                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
