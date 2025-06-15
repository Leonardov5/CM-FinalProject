package com.example.finalproject.data.service

import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AuthService {
    private val supabase = SupabaseProvider.client

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

    fun isAuthenticated(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }

    suspend fun logout(): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                try {
                    supabase.auth.signOut()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                supabase.auth.clearSession()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    fun getCurrentUserEmail(): String? {
        return supabase.auth.currentUserOrNull()?.email
    }

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

    suspend fun updatePassword(currentPassword: String, newPassword: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                try {
                    val currentEmail = getCurrentUserEmail() ?: return@withContext false

                    supabase.auth.signInWith(Email) {
                        this.email = currentEmail
                        this.password = currentPassword
                    }

                    supabase.auth.modifyUser {
                        this.password = newPassword
                    }

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

    suspend fun updateEmail(email: String, password: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                try {
                    val currentEmail = getCurrentUserEmail() ?: return@withContext false

                    supabase.auth.signInWith(Email) {
                        this.email = currentEmail
                        this.password = password
                    }

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
