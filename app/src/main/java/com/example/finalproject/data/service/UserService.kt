package com.example.finalproject.data.service

import com.example.finalproject.data.model.User
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.text.get

object UserService {
    private val supabase = SupabaseProvider.client
    private const val USERS_TABLE = "utilizador"  // Nome correto da tabela

    suspend fun saveUserData(username: String, nome: String = "", tipo: String = "normal"): Boolean {
        return try {
            val userId = AuthService.getCurrentUserId() ?: return false
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())

            val user = User(
                id = userId,
                username = username,
                nome = nome,
                tipo = tipo,
                createdAt = now,
                updatedAt = now
            )

            withContext(Dispatchers.IO) {
                // Transformar o objeto User em um JsonObject para garantir que todos os campos sejam enviados
                val userJson = buildJsonObject {
                    put("utilizador_uuid", userId)
                    put("username", username)
                    put("nome", nome)
                    put("tipo", tipo)  // Garantir que o tipo seja sempre enviado
                    put("created_at", now)
                    put("updated_at", now)
                }

                // Usando o método upsert com o JsonObject em vez do objeto User
                supabase.from(USERS_TABLE)
                    .upsert(userJson)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getCurrentUserData(): User? {
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
                    .decodeList<User>()

                val user = users.firstOrNull()
                if (user != null) {
                    // Obter o email atualizado diretamente do AuthService
                    val email = AuthService.getCurrentUserEmail() ?: ""
                    user.email = email
                    println("Email do usuário: $email")
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

