package com.example.finalproject.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.finalproject.data.model.Trabalho
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.data.service.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TrabalhoRepository {
    private val supabase = SupabaseProvider.client

    suspend fun registarTrabalho(
        tarefaId: String,
        data: String,
        local: String? = null,
        contribuicao: Double,
        tempoDispensado: Int
    ): Trabalho? {
        return try {
            val currentUserUUID = AuthService.getCurrentUserId()

            val novoTrabalho = supabase.from("trabalho").insert(
                Trabalho(
                    id = null,
                    tarefaId = tarefaId,
                    data = data,
                    local = local,
                    contribuicao = contribuicao,
                    tempoDispensado = tempoDispensado,
                    createdBy = currentUserUUID,
                    modifiedBy = currentUserUUID
                )
            ) {
                select()
            }.decodeSingle<Trabalho>()

            novoTrabalho
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun listarTrabalhosPorTarefa(tarefaId: String): List<Trabalho> {
        return try {
            val trabalhos = supabase.from("trabalho")
                .select {
                    filter {
                        eq("tarefa_uuid", tarefaId)
                    }
                    order("data", Order.DESCENDING)
                }
                .decodeList<Trabalho>()

            trabalhos
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun eliminarTrabalho(trabalhoId: String): Boolean {
        return try {
            supabase.from("trabalho")
                .delete {
                    filter {
                        eq("trabalho_uuid", trabalhoId)
                    }
                }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatarDataHoraParaISO(dataHora: LocalDateTime): String {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        return dataHora.format(formatter)
    }
}
