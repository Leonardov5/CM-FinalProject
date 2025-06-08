package com.example.finalproject.data.service

import com.example.finalproject.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.Json

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
        }
        install(Storage)
        install(Auth)
    }
}
