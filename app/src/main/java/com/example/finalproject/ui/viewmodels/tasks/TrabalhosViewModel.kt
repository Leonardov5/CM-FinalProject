package com.example.finalproject.ui.viewmodels.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.R
import com.example.finalproject.data.model.Tarefa
import com.example.finalproject.data.model.Trabalho
import com.example.finalproject.data.model.Utilizador
import com.example.finalproject.data.repository.ProjetoRepository
import com.example.finalproject.data.repository.TarefaRepository
import com.example.finalproject.data.repository.TrabalhoRepository
import com.example.finalproject.data.repository.UtilizadorRepository
import com.example.finalproject.data.service.UserService
import kotlinx.coroutines.launch

class TrabalhosViewModel : ViewModel() {

    var trabalhos by mutableStateOf<List<Trabalho>>(emptyList())
        private set

    var utilizadores by mutableStateOf<Map<String, Utilizador>>(emptyMap())
        private set

    var tarefa by mutableStateOf<Tarefa?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorRes by mutableStateOf<Int?>(null)
        private set

    var tarefaId by mutableStateOf<String?>(null)
        private set

    var user by mutableStateOf<Utilizador?>(null)
        private set

    var isAdminUser by mutableStateOf(false)
        private set

    var isManager by mutableStateOf(false)
        private set

    private val trabalhoRepository = TrabalhoRepository()
    private val tarefaRepository = TarefaRepository()
    private val utilizadorRepository = UtilizadorRepository()
    private val projetoRepository = ProjetoRepository()

    fun carregarTrabalhos(tarefaId: String) {
        this.tarefaId = tarefaId
        isLoading = true
        errorRes = null

        viewModelScope.launch {
            try {
                tarefa = tarefaRepository.obterTarefaPorId(tarefaId)

                trabalhos = trabalhoRepository.listarTrabalhosPorTarefa(tarefaId)

                val utilizadoresIds = trabalhos.mapNotNull { it.createdBy }.distinct()
                if (utilizadoresIds.isNotEmpty()) {
                    val listaUtilizadores = utilizadorRepository.listarTodosUtilizadores()
                    utilizadores = listaUtilizadores.filter { it.id in utilizadoresIds }.associateBy { it.id ?: "" }
                }
            } catch (e: Exception) {
                errorRes = R.string.error_loading_works
            } finally {
                isLoading = false
            }
        }
    }

    fun loadUser(currentUser: Utilizador? = null) {
        viewModelScope.launch {
            try {
                user = currentUser ?: UserService.getCurrentUserData()
                isAdminUser = user?.admin == true

                tarefa?.projetoId?.let { projetoId ->
                    isManager = verificarGerente(projetoId, user?.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun verificarGerente(projetoId: String, userId: String?): Boolean {
        if (userId == null) return false

        return try {
            val membros = projetoRepository.listarMembrosProjetoCompleto(projetoId)
            membros.any { it.userId == userId && it.isManager }
        } catch (e: Exception) {
            false
        }
    }

    fun obterUtilizador(userId: String?): Utilizador? {
        return if (userId != null) utilizadores[userId] else null
    }

    fun eliminarTrabalho(trabalhoId: String, onSuccess: () -> Unit, onError: (Int) -> Unit) {
        if (!isAdminUser && !isManager) {
            onError(R.string.work_delete_permission_error)
            return
        }

        isLoading = true

        viewModelScope.launch {
            try {
                val sucesso = trabalhoRepository.eliminarTrabalho(trabalhoId)

                if (sucesso) {
                    tarefaId?.let { carregarTrabalhos(it) }
                    onSuccess()
                } else {
                    onError(R.string.work_delete_error)
                }
            } catch (e: Exception) {
                onError(R.string.work_delete_error_with_message)
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        errorRes = null
    }
}
