package com.example.finalproject.data.service

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Serviço para gerenciar uploads de imagens para o Supabase Storage
 */
object StorageService {
    private val supabase = SupabaseProvider.client
    private const val PROFILE_BUCKET = "perfil"

    /**
     * Faz upload de uma imagem para o bucket de perfil no Supabase Storage
     * @param context O contexto da aplicação
     * @param imageUri O URI da imagem a ser enviada
     * @return URL da imagem enviada ou null em caso de erro
     */
    suspend fun uploadProfileImage(context: Context, imageUri: Uri): String? {
        return try {
            val userId = AuthService.getCurrentUserId() ?: return null

            // Nome do arquivo será o UUID do utilizador
            val fileName = "$userId.jpg"

            println("Fazendo upload da imagem: $fileName para o bucket: $PROFILE_BUCKET")

            // Converter a imagem para um array de bytes
            val bytes = compressImageFromUri(context, imageUri)
                ?: return null

            withContext(Dispatchers.IO) {
                try {
                    // Fazer upload para o bucket "perfil"
                    supabase.storage.from(PROFILE_BUCKET)
                        .upload(fileName, bytes, upsert = true)

                    // Retornar a URL pública da imagem
                    supabase.storage.from(PROFILE_BUCKET)
                        .publicUrl(fileName)


                } catch (e: Exception) {
                    // Logar o erro completo
                    println("Erro ao fazer upload: ${e.message}")
                    e.printStackTrace()
                    throw e
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Comprime e converte uma imagem de um Uri para um array de bytes
     */
    private suspend fun compressImageFromUri(context: Context, imageUri: Uri): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                // Converter Uri para Bitmap
                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: return@withContext null
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                // Comprimir a imagem
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                outputStream.toByteArray()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Obtém a URL pública da imagem de perfil do utilizador
     * @return URL da imagem ou null caso não exista
     */
    suspend fun getProfileImageUrl(userId: String? = null): String? {
        return try {
            val targetUserId = userId ?: AuthService.getCurrentUserId() ?: return null
            val fileName = "$targetUserId.jpg"

            withContext(Dispatchers.IO) {
                try {
                    // Obter a URL pública
                    val publicUrl = supabase.storage.from(PROFILE_BUCKET)
                        .publicUrl(fileName)

                    // Adicionar o token de autenticação à URL se o usuário estiver autenticado
                    val session = supabase.auth.currentSessionOrNull()
                    if (session != null) {
                        // Verificar se já temos um parâmetro de consulta na URL
                        val separator = if (publicUrl.contains("?")) "&" else "?"
                        // Adicionar o token como parâmetro de consulta
                        "$publicUrl${separator}token=${session.accessToken}"
                    } else {
                        publicUrl
                    }
                } catch (e: Exception) {
                    println("Erro ao obter URL da imagem: ${e.message}")
                    e.printStackTrace()
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
