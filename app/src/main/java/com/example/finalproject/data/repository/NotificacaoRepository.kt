package com.example.finalproject.data.repository

import com.example.finalproject.data.model.Notificacao
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.data.service.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class NotificacaoRepository {

    private val supabase = SupabaseProvider.client

    suspend fun listarNotificacoesDoUsuario(): List<Notificacao> {
        return try {
            println("DEBUG - Listando notificações do usuário")
            val currentUserUUID = AuthService.getCurrentUserId() ?: return emptyList()

            val notificacoes = supabase.from("notificacao")
                .select {
                    filter {
                        eq("utilizador_uuid", currentUserUUID)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Notificacao>()

            println("DEBUG - Notificações carregadas: ${notificacoes.size}")
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
            println("DEBUG - Notificação marcada como lida: $notificacaoId")
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
            println("DEBUG - Todas as notificações marcadas como lidas")
            true
        } catch (e: Exception) {
            println("DEBUG - Erro ao marcar todas notificações como lidas: ${e.message}")
            false
        }
    }

    suspend fun contarNotificacoesNaoLidas(): Int {
        return try {
            val currentUserUUID = AuthService.getCurrentUserId() ?: return 0

            val notificacoesNaoLidas = supabase.from("notificacao")
                .select {
                    filter {
                        eq("utilizador_uuid", currentUserUUID)
                        eq("vista", false)
                    }
                }
                .decodeList<Notificacao>()

            val result = notificacoesNaoLidas.size
            println("DEBUG - Contagem de notificações não lidas: $result")
            result
        } catch (e: Exception) {
            println("DEBUG - Erro ao contar notificações não lidas: ${e.message}")
            0
        }
    }

    suspend fun deletarNotificacao(notificacaoId: String): Boolean {
        return try {
            supabase.from("notificacao")
                .delete {
                    filter { eq("notificacao_uuid", notificacaoId) }
                }
            println("DEBUG - Notificação deletada: $notificacaoId")
            true
        } catch (e: Exception) {
            println("DEBUG - Erro ao deletar notificação: ${e.message}")
            false
        }
    }
}