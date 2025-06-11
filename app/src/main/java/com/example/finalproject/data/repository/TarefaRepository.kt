package com.example.finalproject.data.repository

import com.example.finalproject.data.model.Tarefa
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.data.service.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

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
    suspend fun listarTarefas(): List<Tarefa> {
        return try {
            val tarefas = supabase.from("tarefa")
                .select()
                .decodeList<Tarefa>()

            println("DEBUG - Tarefas carregadas: ${tarefas.size}")
            tarefas
        } catch (e: Exception) {
            println("DEBUG - Erro ao listar tarefas: ${e.message}")
            emptyList()
        }
    }
}
