package com.example.finalproject.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.finalproject.data.model.Tarefa
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.data.service.SupabaseProvider
import com.example.finalproject.data.service.UserService
import io.github.jan.supabase.postgrest.from

class TarefaRepository {

    private val supabase = SupabaseProvider.client

    suspend fun criarTarefa(
        projetoUUID: String,
        nome: String,
        descricao: String? = null,
        prioridade: String = "media",
        status: String = "pendente",
        dataInicio: String? = null,
        dataFim: String? = null
    ): Tarefa? {
        return try {
            val currentUserUUID = AuthService.getCurrentUserId()

            val novaTarefa = supabase.from("tarefa").insert(
                Tarefa(
                    id = null,
                    projetoId = projetoUUID,
                    nome = nome,
                    descricao = descricao,
                    prioridade = prioridade,
                    status = status,
                    dataInicio = dataInicio,
                    dataFim = dataFim,
                    taxaConclusao = 0.0,
                    createdBy = currentUserUUID,
                    modifiedBy = currentUserUUID
                )
            ) {
                select()
            }.decodeSingle<Tarefa>()

            novaTarefa
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun obterTarefaPorId(taskId: String): Tarefa? {
        return try {
            val tarefa = supabase.from("tarefa")
                .select {
                    filter {
                        eq("tarefa_uuid", taskId)
                    }
                    limit(1)
                }
                .decodeSingle<Tarefa>()
            tarefa
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun adicionarUtilizadorTarefa(userId: String, tarefaId: String): Boolean {
        return try {
            val result = supabase.from("utilizador_tarefa")
                .insert(mapOf("utilizador_uuid" to userId, "tarefa_uuid" to tarefaId)) {
                    select()
                }
                .decodeSingleOrNull<Map<String, String>>()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun removerUtilizadorDaTarefa(userId: String, tarefaId: String): Boolean {
        return try {
            val result = supabase.from("utilizador_tarefa")
                .delete {
                    filter {
                        eq("utilizador_uuid", userId)
                        eq("tarefa_uuid", tarefaId)
                    }
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getTrabalhadoresTarefa(tarefaId: String): List<String> {
        return try {
            val result = supabase.from("utilizador_tarefa")
                .select {
                    filter { eq("tarefa_uuid", tarefaId) }
                }
                .decodeList<Map<String, String>>()
            result.mapNotNull { it["utilizador_uuid"] }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun obterDataEntradaUtilizador(userId: String, tarefaId: String): String? {
        return try {
            val result = supabase.from("utilizador_tarefa")
                .select {
                    filter {
                        eq("utilizador_uuid", userId)
                        eq("tarefa_uuid", tarefaId)
                    }
                    limit(1)
                }
                .decodeList<Map<String, String>>()
                .firstOrNull()

            result?.get("created_at")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun listarTarefas(): List<Tarefa> {
        return try {
            val currentUser = UserService.getCurrentUserData()
            if (currentUser == null) {
                return emptyList()
            }

            if(currentUser.admin){
                return supabase.from("tarefa")
                    .select()
                    .decodeList<Tarefa>()
            }
            val userId = currentUser.id

            val tarefaIds = supabase.from("utilizador_tarefa")
                .select {
                    filter { eq("utilizador_uuid", userId) }
                }
                .decodeList<Map<String, String>>()
                .mapNotNull { it["tarefa_uuid"] }

            val projetoIds = supabase.from("utilizador_projeto")
                .select {
                    filter { eq("utilizador_uuid", userId); eq("e_gestor", true) }
                }
                .decodeList<Map<String, String>>()
                .mapNotNull { it["projeto_uuid"] }

            val tarefasDiretas = if (tarefaIds.isNotEmpty()) {
                supabase.from("tarefa")
                    .select {
                        filter {
                            tarefaIds.forEach { id ->
                                or { eq("tarefa_uuid", id) }
                            }
                        }
                    }
                    .decodeList<Tarefa>()
            } else emptyList()

            val tarefasGestor = if (projetoIds.isNotEmpty()) {
                supabase.from("tarefa")
                    .select {
                        filter {
                            projetoIds.forEach { id ->
                                or { eq("projeto_uuid", id) }
                            }
                        }
                    }
                    .decodeList<Tarefa>()
            } else emptyList()

            val tarefas = (tarefasDiretas + tarefasGestor).distinctBy { it.id }

            tarefas
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun eliminarTarefaPorId(taskId: String): Boolean {
        return try {
            supabase.from("tarefa")
                .delete {
                    filter { eq("tarefa_uuid", taskId) }
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun atualizarTarefa(
        tarefaId: String,
        nome: String,
        descricao: String,
        prioridade: String,
        status: String,
        dataInicio: String?,
        dataFim: String?,
        taxaConclusao: Double
    ): Boolean {
        return try {
            val currentUserUUID = AuthService.getCurrentUserId()
            val currentTimestamp = java.time.OffsetDateTime.now().toString()

            supabase.from("tarefa")
                .update({
                    set("nome", nome)
                    set("descricao", descricao)
                    set("prioridade", prioridade)
                    set("status", status)
                    set("data_inicio", dataInicio)
                    set("data_fim", dataFim)
                    set("taxa_conclusao", taxaConclusao)
                    set("modified_by", currentUserUUID)
                    set("updated_at", currentTimestamp)
                }) {
                    filter { eq("tarefa_uuid", tarefaId) }
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getTaskAnalytics(taskId: String): com.example.finalproject.data.model.TaskAnalytics? {
        return try {
            val result = supabase.from("view_task_analytics")
                .select {
                    filter {
                        eq("tarefa_uuid", taskId)
                    }
                }
                .decodeSingleOrNull<com.example.finalproject.data.model.TaskAnalytics>()

            result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
