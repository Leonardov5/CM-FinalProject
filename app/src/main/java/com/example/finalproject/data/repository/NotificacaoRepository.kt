package com.example.finalproject.data.repository

import com.example.finalproject.data.model.Notificacao
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.data.service.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class NotificacaoRepository {

    private val supabase = SupabaseProvider.client

    suspend fun listarNotificacoesDoUtilizador(): List<Notificacao> {
        return try {
            val currentUserUUID = AuthService.getCurrentUserId() ?: return emptyList()

            val notificacoes = supabase.from("notificacao")
                .select {
                    filter {
                        eq("utilizador_uuid", currentUserUUID)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Notificacao>()

            notificacoes
        } catch (e: Exception) {
            println("DEBUG - Erro ao listar notificações: ${e.message}")
            emptyList()
        }
    }

    suspend fun marcarComoLida(notificacaoId: String): Boolean {
        return try {
            supabase.from("notificacao")
                .update({
                    set("vista", true)
                }) {
                    filter { eq("notificacao_uuid", notificacaoId) }
                }

            true
        } catch (e: Exception) {
            println("DEBUG - Erro ao marcar notificação como lida: ${e.message}")
            false
        }
    }

    suspend fun marcarTodasComoLidas(): Boolean {
        return try {
            val currentUserUUID = AuthService.getCurrentUserId() ?: return false

            supabase.from("notificacao")
                .update({
                    set("vista", true)
                }) {
                    filter {
                        eq("utilizador_uuid", currentUserUUID)
                        eq("vista", false)
                    }
                }

            true
        } catch (e: Exception) {
            println("DEBUG - Erro ao marcar todas notificações como lidas: ${e.message}")
            false
        }
    }

    suspend fun eliminarNotificacao(notificacaoId: String): Boolean {
        return try {
            supabase.from("notificacao")
                .delete {
                    filter { eq("notificacao_uuid", notificacaoId) }
                }

            true
        } catch (e: Exception) {
            println("DEBUG - Erro ao eliminar notificação: ${e.message}")
            false
        }
    }
}