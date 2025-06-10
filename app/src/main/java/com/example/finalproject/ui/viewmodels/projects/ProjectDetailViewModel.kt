package com.example.finalproject.ui.viewmodels.projects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.model.Projeto
import com.example.finalproject.data.model.User
import com.example.finalproject.data.repository.ProjetoRepository
import com.example.finalproject.data.repository.TarefaRepository
import com.example.finalproject.data.service.UserService
import kotlinx.coroutines.launch
import java.util.UUID

class ProjectDetailViewModel(
    private val projetoRepository: ProjetoRepository = ProjetoRepository(),
    private val tarefaRepository: TarefaRepository = TarefaRepository()
) : ViewModel() {

    // Estados UI
    var projeto by mutableStateOf<Projeto?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    var isAdmin by mutableStateOf(false)
        private set

    var user by mutableStateOf<User?>(null)
        private set

    var showFabActions by mutableStateOf(false)
        private set

    var showDeleteConfirmDialog by mutableStateOf(false)
        private set

    var showAddTaskDialog by mutableStateOf(false)
        private set

    // Funções para manipular diálogos
    fun showAddTaskDialog() {
        showAddTaskDialog = true
    }

    fun hideAddTaskDialog() {
        showAddTaskDialog = false
    }

    fun toggleFabActions() {
        showFabActions = !showFabActions
    }

    fun showDeleteConfirmDialog() {
        showDeleteConfirmDialog = true
    }

    fun hideDeleteConfirmDialog() {
        showDeleteConfirmDialog = false
    }

    // Carregar o usuário atual
    fun loadUser(currentUser: User? = null) {
        viewModelScope.launch {
            try {
                user = currentUser ?: UserService.getCurrentUserData()
                isAdmin = user?.admin == true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Carregar detalhes do projeto
    fun loadProject(projetoId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val uuid = UUID.fromString(projetoId)
                projeto = projetoRepository.obterProjeto(uuid)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // Adicionar tarefa
    fun addTask(
        projetoId: String,
        nome: String,
        descricao: String,
        prioridade: String,
        status: String,
        dataInicio: String?,
        dataFim: String?
    ) = viewModelScope.launch {
        try {
            val novaTarefa = tarefaRepository.criarTarefa(
                projetoUUID = projetoId,
                nome = nome,
                descricao = descricao.takeIf { it.isNotBlank() },
                prioridade = prioridade,
                status = status,
                dataInicio = dataInicio,
                dataFim = dataFim
            )

            if (novaTarefa != null) {
                loadProject(projetoId)
                hideAddTaskDialog()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Apagar projeto
    fun deleteProject(projetoId: String) = viewModelScope.launch {
        try {
            val result = projetoRepository.apagarProjeto(UUID.fromString(projetoId))
            if (result) {
                hideDeleteConfirmDialog()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Alterar status do projeto
    fun changeProjectStatus(projetoId: String, novoStatus: String) = viewModelScope.launch {
        try {
            val result = projetoRepository.alterarStatusProjeto(
                UUID.fromString(projetoId),
                novoStatus
            )
            if (result) {
                loadProject(projetoId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
