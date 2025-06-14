package com.example.finalproject.ui.viewmodels.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.model.Tarefa
import com.example.finalproject.data.model.TarefaStatus
import com.example.finalproject.data.model.User
import com.example.finalproject.data.model.UserProject
import com.example.finalproject.data.repository.ProjetoRepository
import com.example.finalproject.data.repository.TarefaRepository
import com.example.finalproject.data.repository.UserRepository
import com.example.finalproject.data.service.UserService
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val taskRepository: TarefaRepository = TarefaRepository(),
    private val projetoRepository: ProjetoRepository = ProjetoRepository()

) : ViewModel(){

    var task by mutableStateOf<Tarefa?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var showAddWorkerDialog by mutableStateOf(false)
        private set

    var isAdmin by mutableStateOf(false)
        private set

    var isManager by mutableStateOf(false)
        private set

    var trabalhadoresTarefa by mutableStateOf<List<String>>(emptyList())
        private set

    var membrosProjetoUserProject by mutableStateOf<List<UserProject>>(emptyList())
        private set

    var membrosProjeto by mutableStateOf<List<User>>(emptyList())
        private set

    var filtredMembros by mutableStateOf<List<User>>(emptyList())
        private set

    // Evento de navegação para tela de observações
    var navigateToObservacoesEvent by mutableStateOf<String?>(null)
        private set

    // Evento de navegação para tela de trabalhos
    var navigateToTrabalhosEvent by mutableStateOf<String?>(null)
        private set

    fun checkUser(currentUser: User? = null) {
        viewModelScope.launch {
            try {
                val user = currentUser ?: UserService.getCurrentUserData()
                isAdmin = user?.admin == true
                val membrosProjetoCompleto = projetoRepository.listarMembrosProjetoCompleto(task?.projetoId ?: "")
                isManager = membrosProjetoCompleto.any { it.userId == user?.id && it.isManager }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadTrabalhadoresTarefa(tarefaId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                trabalhadoresTarefa = taskRepository.getTrabalhadoresDaTarefa(tarefaId)
                println("Debug - foram carregados ${trabalhadoresTarefa.size} trabalhadores para a tarefa $tarefaId")
            } catch (e: Exception) {
                println("Error fetching task workers: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun loadMembrosProjeto(projetoId: String) {
        viewModelScope.launch {
            val membros = projetoRepository.listarMembrosDoProjeto(projetoId) // retorna List<UserProject>
            membrosProjetoUserProject = membros
            val allUsers = UserRepository().listarTodosUsuarios()
            membrosProjeto = membros.mapNotNull { membro ->
                allUsers.find { it.id == membro.userId }
            }
        }
    }

    fun filterMembros() {
        val gestoresProjeto = membrosProjetoUserProject.filter { it.isManager }.map { it.userId }
        filtredMembros = membrosProjeto.filter { user ->
            user.id !in trabalhadoresTarefa && user.id !in gestoresProjeto
        }
    }


    fun toggleAddWorkerDialog() {
        showAddWorkerDialog = !showAddWorkerDialog
    }

    fun statusToEnum(status: String?): TarefaStatus =
        TarefaStatus.values().find { it.name == status } ?: TarefaStatus.pendente

    fun loadTask(taskId: String){
        println("Debug - Loading task with ID: $taskId")
        viewModelScope.launch {
            isLoading = true
            try {
                task = taskRepository.getTarefaById(taskId)
            } catch (e: Exception) {
                println("Error fetching task: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun addWorkerToTask(userId: String, tarefaId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = taskRepository.adicionarUsuarioATarefa(userId, tarefaId)
            onResult(result)
            if (result) {
                loadTask(tarefaId) // Atualiza a tarefa para refletir os trabalhadores
            }
        }
    }

    fun removeWorkerFromTask(userId: String, tarefaId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = taskRepository.removerUsuarioDaTarefa(userId, tarefaId)
            onResult(result)
            if (result) {
                loadTask(tarefaId)
                loadTrabalhadoresTarefa(tarefaId)
            }
        }
    }

    // Esta função não registra trabalho, apenas atualiza a interface após um registro
    fun reloadTaskAfterLogWork(
        tarefaId: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                // Apenas recarrega a tarefa para atualizar a UI
                loadTask(tarefaId)
                onComplete()
            } catch (e: Exception) {
                println("Error reloading task: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Função para navegar para a tela de observações
    fun navigateToObservacoes(tarefaId: String) {
        navigateToObservacoesEvent = tarefaId
    }

    // Função para limpar o evento de navegação após consumido
    fun onObservacoesNavigated() {
        navigateToObservacoesEvent = null
    }

    // Função para navegar para a tela de trabalhos
    fun navigateToTrabalhos(tarefaId: String) {
        navigateToTrabalhosEvent = tarefaId
    }

    // Função para limpar o evento de navegação de trabalhos após consumido
    fun onTrabalhosNavigated() {
        navigateToTrabalhosEvent = null
    }
}