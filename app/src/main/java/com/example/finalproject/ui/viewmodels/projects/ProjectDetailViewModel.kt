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
import com.example.finalproject.data.repository.UserRepository
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

    // No ProjectDetailViewModel.kt
    var allUsers by mutableStateOf<List<User>>(emptyList())
        private set

    fun loadAllUsers() {
        viewModelScope.launch {
            allUsers = UserRepository().listarTodosUsuarios()
        }
    }

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

    var navigateToTasksForProject by mutableStateOf<String?>(null)
        private set

    fun onViewTasksClick() {
        println("DEBUG - onViewTasksClick chamado com projeto: $projeto")
        navigateToTasksForProject = projeto?.id?.toString()
    }

    fun onTasksNavigationHandled() {
        navigateToTasksForProject = null
    }

    // No ProjectDetailViewModel
    var showAddMemberDialog by mutableStateOf(false)
        private set

    fun showAddMemberDialog() { showAddMemberDialog = true }
    fun hideAddMemberDialog() { showAddMemberDialog = false }

    fun addMemberToProject(userId: String, isManager: Boolean) = viewModelScope.launch {
        val result = projetoRepository.adicionarUsuarioAoProjeto(userId, projeto?.id.toString(), isManager)
        if (result) {
            hideAddMemberDialog()
            // Atualize a lista de membros se necessário
        }
    }

    var isManager by mutableStateOf(false)
        private set

    fun checkIfManager(projectId: String) {
        viewModelScope.launch {
            val membros = projetoRepository.listarMembrosDoProjeto(projectId)
            val userId = user?.id
            isManager = membros.any { it.userId == userId && it.isManager }
        }
    }

    // No ProjectDetailViewModel.kt
    var membrosProjeto by mutableStateOf<List<User>>(emptyList())
        private set

    fun loadMembrosProjeto(projectId: String) {
        viewModelScope.launch {
            val membros = projetoRepository.listarMembrosDoProjeto(projectId)
            // Supondo que UserProject tem userId, busque os dados completos dos usuários:
            val allUsers = UserRepository().listarTodosUsuarios()
            membrosProjeto = membros.mapNotNull { membro ->
                allUsers.find { it.id == membro.userId }
            }
        }
    }
}
