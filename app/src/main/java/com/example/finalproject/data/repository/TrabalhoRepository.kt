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

    suspend fun registrarTrabalho(
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

            println("DEBUG - Trabalho registrado: $novoTrabalho")
            novoTrabalho
        } catch (e: Exception) {
            println("DEBUG - Erro ao registrar trabalho: ${e.message}")
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

            println("DEBUG - Trabalhos carregados: ${trabalhos.size}")
            trabalhos
        } catch (e: Exception) {
            println("DEBUG - Erro ao listar trabalhos: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTrabalhoById(trabalhoId: String): Trabalho? {
        return try {
            val trabalho = supabase.from("trabalho")
                .select {
                    filter {
                        eq("trabalho_uuid", trabalhoId)
                    }
                    limit(1)
                }
                .decodeSingle<Trabalho>()
            trabalho
        } catch (e: Exception) {
            println("DEBUG - Erro ao buscar trabalho por id: ${e.message}")
            null
        }
    }

    // Metodo para excluir um trabalho
    suspend fun excluirTrabalho(trabalhoId: String): Boolean {
        return try {
            supabase.from("trabalho")
                .delete {
                    filter {
                        eq("trabalho_uuid", trabalhoId)
                    }
                }

            println("DEBUG - Trabalho excluído com sucesso: $trabalhoId")
            true
        } catch (e: Exception) {
            println("DEBUG - Erro ao excluir trabalho: ${e.message}")
            false
        }
    }

    // Formatar LocalDateTime para string ISO
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatarDataHora(dataHora: LocalDateTime): String {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        return dataHora.format(formatter)
    }
}
