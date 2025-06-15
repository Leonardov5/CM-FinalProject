package com.example.finalproject.data.service

import com.example.finalproject.data.model.Utilizador
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object UserService {
    private val supabase = SupabaseProvider.client
    private const val USERS_TABLE = "utilizador"

    suspend fun saveUserData(username: String, nome: String = "", admin: Boolean = false): Boolean {
        return try {
            val userId = AuthService.getCurrentUserId() ?: return false
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())

            val user = Utilizador(
                id = userId,
                username = username,
                nome = nome,
                admin = admin,
                createdAt = now,
                updatedAt = now
            )

            withContext(Dispatchers.IO) {
                val userJson = buildJsonObject {
                    put("utilizador_uuid", userId)
                    put("username", username)
                    put("nome", nome)
                    put("admin", admin)
                    put("created_at", now)
                    put("updated_at", now)
                }

                supabase.from(USERS_TABLE)
                    .upsert(userJson)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getCurrentUserData(): Utilizador? {
        return try {
            val userId = AuthService.getCurrentUserId() ?: return null

            withContext(Dispatchers.IO) {
                val users = supabase.from(USERS_TABLE)
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("utilizador_uuid", userId)
                        }
                        limit(1)
                    }
                    .decodeList<Utilizador>()

                val user = users.firstOrNull()
                if (user != null) {
                    val email = AuthService.getCurrentUserEmail() ?: ""
                    user.email = email
                }
                user
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateUserData(username: String? = null, nome: String? = null, fotografia: String? = null): Boolean {
        return try {
            val userId = AuthService.getCurrentUserId() ?: return false
            val currentUser = getCurrentUserData() ?: return false
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())

            val updates = buildJsonObject {
                if (username != null) put("username", username)
                if (nome != null) put("nome", nome)
                if (fotografia != null) put("fotografia", fotografia)
                put("updated_at", now)
            }

            withContext(Dispatchers.IO) {
                supabase.from(USERS_TABLE)
                    .update(updates) {
                        filter {
                            eq("utilizador_uuid", userId)
                        }
                    }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

