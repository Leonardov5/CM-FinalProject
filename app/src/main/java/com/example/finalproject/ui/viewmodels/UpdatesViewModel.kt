// Modificar a classe UpdatesViewModel
package com.example.finalproject.ui.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.R
import com.example.finalproject.data.model.Notificacao
import com.example.finalproject.data.repository.NotificacaoRepository
import com.example.finalproject.utils.updateAppLanguage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class UpdatesViewModel : ViewModel() {

    private val notificacoesRepository = NotificacaoRepository()

    var notificacoes by mutableStateOf<List<Notificacao>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    private var applicationContext: Context? = null


    // Carregar notificações do usuário
    fun loadUpdates() {
        viewModelScope.launch {
            isLoading = true
            try {
                notificacoes = notificacoesRepository.listarNotificacoesDoUsuario()
            } catch (e: Exception) {
                println("DEBUG - Erro ao carregar notificações: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Formatar a data da notificação para exibição
    fun formatarData(notificacao: Notificacao): String {
        val dateString = notificacao.createdAt ?: return "Data desconhecida"

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM, yyyy", Locale("pt", "BR"))

            val date = inputFormat.parse(dateString)
            return date?.let { outputFormat.format(it) } ?: "Data inválida"
        } catch (e: Exception) {
            return try {
                // Tenta outro formato caso o primeiro falhe
                val alternativeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMM, yyyy", Locale("pt", "BR"))
                val date = alternativeFormat.parse(dateString)
                date?.let { outputFormat.format(it) } ?: "Data inválida"
            } catch (e2: Exception) {
                "Data inválida"
            }
        }
    }

    // Marcar uma notificação como lida
    fun markAsRead(notificacaoId: String) {
        viewModelScope.launch {
            val success = notificacoesRepository.marcarComoLida(notificacaoId)
            if (success) {
                notificacoes = notificacoes.map { notificacao ->
                    if (notificacao.id == notificacaoId) {
                        notificacao.copy(vista = true)
                    } else {
                        notificacao
                    }
                }
            }
        }
    }

    // Em UpdatesViewModel.kt - Adicione este método
    fun deleteNotification(notificacaoId: String) {
        viewModelScope.launch {
            val success = notificacoesRepository.deletarNotificacao(notificacaoId)
            if (success) {
                notificacoes = notificacoes.filter { it.id != notificacaoId }
            }
        }
    }


    // Marcar todas as notificações como lidas
    fun markAllAsRead() {
        viewModelScope.launch {
            val success = notificacoesRepository.marcarTodasComoLidas()
            if (success) {
                notificacoes = notificacoes.map { it.copy(vista = true) }
            }
        }
    }

    // Lista original de notificações (para restaurar após filtro)
    private var notificacoesOriginais = emptyList<Notificacao>()

    // Filtrar notificações com base na consulta de pesquisa
    fun filterNotificacoes(query: String) {
        if (notificacoesOriginais.isEmpty()) {
            notificacoesOriginais = notificacoes
        }

        if (query.isBlank()) {
            resetFilter()
            return
        }

        val queryLowerCase = query.lowercase(Locale.getDefault())
        notificacoes = notificacoesOriginais.filter { notificacao ->
            // Pesquisa no título e mensagem
            val title = getTitleFromNotificacao(notificacao)
            val message = getMessageFromNotificacao(notificacao)
            val objeto = notificacao.objeto ?: ""

            title.lowercase(Locale.getDefault()).contains(queryLowerCase) ||
            message.lowercase(Locale.getDefault()).contains(queryLowerCase) ||
            objeto.lowercase(Locale.getDefault()).contains(queryLowerCase)
        }
    }

    // Resetar filtro e mostrar todas as notificações originais
    fun resetFilter() {
        if (notificacoesOriginais.isNotEmpty()) {
            notificacoes = notificacoesOriginais
        }
    }

    // Funções auxiliares para obter título e mensagem a partir de códigos de notificação
    private fun getTitleFromNotificacao(notificacao: Notificacao): String {
        return when (notificacao.mensagem) {
            "USER_ADDED_TO_TASK" -> "Adicionado a uma tarefa"
            "USER_ADDED_TO_PROJECT" -> "Adicionado a um projeto"
            "PROJECT_STATUS_CHANGED_TO_ACTIVE" -> "Projeto ativado"
            "PROJECT_STATUS_CHANGED_TO_INACTIVE" -> "Projeto desativado"
            "PROJECT_STATUS_CHANGED_TO_COMPLETED" -> "Projeto concluído"
            "PROJECT_STATUS_CHANGED_TO_CANCELED" -> "Projeto cancelado"
            "TASK_STATUS_CHANGED_TO_PENDING" -> "Tarefa pendente"
            "TASK_STATUS_CHANGED_TO_IN_PROGRESS" -> "Tarefa em andamento"
            "TASK_STATUS_CHANGED_TO_COMPLETED" -> "Tarefa concluída"
            "TASK_STATUS_CHANGED_TO_CANCELED" -> "Tarefa cancelada"
            else -> "Notificação"
        }
    }

    private fun getMessageFromNotificacao(notificacao: Notificacao): String {
        return when (notificacao.mensagem) {
            "USER_ADDED_TO_TASK" -> "Você foi adicionado a uma nova tarefa"
            "USER_ADDED_TO_PROJECT" -> "Você foi adicionado a um novo projeto"
            "PROJECT_STATUS_CHANGED_TO_ACTIVE" -> "Um projeto mudou para status ativo"
            "PROJECT_STATUS_CHANGED_TO_INACTIVE" -> "Um projeto mudou para status inativo"
            "PROJECT_STATUS_CHANGED_TO_COMPLETED" -> "Um projeto foi concluído"
            "PROJECT_STATUS_CHANGED_TO_CANCELED" -> "Um projeto foi cancelado"
            "TASK_STATUS_CHANGED_TO_PENDING" -> "Uma tarefa mudou para status pendente"
            "TASK_STATUS_CHANGED_TO_IN_PROGRESS" -> "Uma tarefa mudou para em andamento"
            "TASK_STATUS_CHANGED_TO_COMPLETED" -> "Uma tarefa foi concluída"
            "TASK_STATUS_CHANGED_TO_CANCELED" -> "Uma tarefa foi cancelada"
            else -> notificacao.mensagem
        }
    }

    init {
        loadUpdates()
    }
}