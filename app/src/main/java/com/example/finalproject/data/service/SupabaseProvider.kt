package com.example.finalproject.data.service

import com.example.finalproject.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

/**
 * Provedor de acesso ao cliente Supabase
 */
object SupabaseProvider {
    // Configuração personalizada do serializador JSON
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        // Usar o serializador personalizado com configurações de tolerância a campos desconhecidos
        install(Postgrest) {
            serializer = KotlinXSerializer(json)
            this@createSupabaseClient.requestTimeout = 5.seconds // Define o timeout como 10 segundos
        }
        install(Storage)
        install(Auth)
    }
    suspend fun isDatabaseConnected(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Realiza uma consulta mínima para verificar a conexão
                client.postgrest["projeto"]
                    .select { limit(1) }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
