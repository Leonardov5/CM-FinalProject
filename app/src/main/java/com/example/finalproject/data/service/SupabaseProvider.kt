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

object SupabaseProvider {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Postgrest) {
            serializer = KotlinXSerializer(json)
            this@createSupabaseClient.requestTimeout = 5.seconds
        }
        install(Storage)
        install(Auth)
    }
    suspend fun isDatabaseConnected(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                client.postgrest["utilizador"]
                    .select { limit(1) }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
