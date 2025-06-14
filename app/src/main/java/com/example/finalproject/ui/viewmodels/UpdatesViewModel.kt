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

    init {
        loadUpdates()
    }
}