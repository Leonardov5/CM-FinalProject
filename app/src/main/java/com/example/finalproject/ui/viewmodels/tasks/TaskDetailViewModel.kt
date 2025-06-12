package com.example.finalproject.ui.viewmodels.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.finalproject.data.model.TarefaStatus
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.model.Tarefa
import com.example.finalproject.data.model.User
import com.example.finalproject.data.model.UserProject
import com.example.finalproject.data.repository.ProjetoRepository
import com.example.finalproject.data.repository.TarefaRepository
import com.example.finalproject.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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


    var trabalhadoresTarefa by mutableStateOf<List<String>>(emptyList())
        private set

    var membrosProjetoUserProject by mutableStateOf<List<UserProject>>(emptyList())
        private set

    var membrosProjeto by mutableStateOf<List<User>>(emptyList())
        private set

    var filtredMembros by mutableStateOf<List<User>>(emptyList())
        private set

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
}