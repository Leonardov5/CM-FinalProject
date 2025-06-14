package com.example.finalproject.data.repository

import com.example.finalproject.data.model.Tarefa
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.data.service.SupabaseProvider
import com.example.finalproject.data.service.UserService
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.nio.file.Files.exists

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

            println("DEBUG - Tarefa criada: $novaTarefa")
            novaTarefa
        } catch (e: Exception) {
            println("DEBUG - Erro ao criar tarefa: ${e.message}")
            null
        }
    }
    
    suspend fun listarTarefasPorProjeto(projetoUUID: String): List<Tarefa> {
        return try {
            val tarefas = supabase.from("tarefa")
                .select {
                    filter {
                        eq("projeto_uuid", projetoUUID)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Tarefa>()
                
            println("DEBUG - Tarefas carregadas: ${tarefas.size}")
            tarefas
        } catch (e: Exception) {
            println("DEBUG - Erro ao listar tarefas: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTarefaById(taskId: String): Tarefa? {
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
            println("DEBUG - Erro ao buscar tarefa por id: ${e.message}")
            null
        }
    }

    suspend fun adicionarUsuarioATarefa(userId: String, tarefaId: String): Boolean {
        return try {
            val result = supabase.from("utilizador_tarefa")
                .insert(mapOf("utilizador_uuid" to userId, "tarefa_uuid" to tarefaId)) {
                    select()
                }
                .decodeSingleOrNull<Map<String, String>>()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun removerUsuarioDaTarefa(userId: String, tarefaId: String): Boolean {
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
            false
        }
    }

    suspend fun getTrabalhadoresDaTarefa(tarefaId: String): List<String> {
        return try {
            val result = supabase.from("utilizador_tarefa")
                .select {
                    filter { eq("tarefa_uuid", tarefaId) }
                }
                .decodeList<Map<String, String>>()
            println("DEBUG - Trabalhadores da tarefa $tarefaId: ${result.size}")
            result.mapNotNull { it["utilizador_uuid"] }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUsuarioJoinDate(userId: String, tarefaId: String): String? {
        // Verificar se os IDs não estão vazios
        if (userId.isBlank() || tarefaId.isBlank()) {
            println("DEBUG - ID de usuário ou tarefa vazios. userId: '$userId', tarefaId: '$tarefaId'")
            return null
        }

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
            println("DEBUG - Erro ao buscar data de entrada do usuário $userId: ${e.message}")
            null
        }
    }

    suspend fun listarTarefas(): List<Tarefa> {
        return try {
            val currentUser = UserService.getCurrentUserData()
            if (currentUser == null) {
                println("DEBUG - Usuário atual não encontrado")
                return emptyList()
            }

            if(currentUser.admin){
                println("DEBUG - Usuário é administrador, retornando todas as tarefas")
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

            println("DEBUG - Tarefas filtradas: ${tarefas.size}")
            tarefas
        } catch (e: Exception) {
            println("DEBUG - Erro ao listar tarefas: ${e.message}")
            emptyList()
        }
    }

    suspend fun deletarTarefaPorId(taskId: String): Boolean {
        return try {
            supabase.from("tarefa")
                .delete {
                    filter { eq("tarefa_uuid", taskId) }
                }
            true
        } catch (e: Exception) {
            println("DEBUG - Erro ao deletar tarefa: ${e.message}")
            false
        }
    }

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
            println("DEBUG - Atualizando tarefa: $tarefaId")

            // Criar timestamp ISO 8601 para o campo updated_at
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

            println("DEBUG - Tarefa atualizada com sucesso: $tarefaId")
            true
        } catch (e: Exception) {
            println("DEBUG - Erro ao atualizar tarefa: ${e.message}")
            false
        }
    }
}
