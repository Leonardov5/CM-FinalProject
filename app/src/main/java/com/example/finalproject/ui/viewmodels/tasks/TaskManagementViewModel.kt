package com.example.finalproject.ui.viewmodels.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.model.Projeto
import com.example.finalproject.data.model.Tarefa
import com.example.finalproject.data.model.TarefaStatus
import com.example.finalproject.data.model.User
import com.example.finalproject.data.repository.ProjetoRepository
import com.example.finalproject.data.repository.TarefaRepository
import com.example.finalproject.data.service.UserService
import kotlinx.coroutines.launch

class TaskManagementViewModel(
    private val projetoRepository: ProjetoRepository = ProjetoRepository(),
    private val taskRepository: TarefaRepository = TarefaRepository()

) : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set

    var selectedTab by mutableStateOf(TarefaStatus.pendente)
        private set

    var tasks by mutableStateOf<List<Tarefa>>(emptyList())
        private set

    var isAdmin by mutableStateOf(false)
        private set

    var showProjectDialog by mutableStateOf(false)

    fun checkUser(currentUser: User? = null) {
        viewModelScope.launch {
            try {
                val user = currentUser ?: UserService.getCurrentUserData()
                isAdmin = user?.admin == true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun loadTasks() {
        viewModelScope.launch {
            try {
                isLoading = true
                tasks = taskRepository.listarTarefas()
            } catch (e: Exception) {
                println("DEBUG - Erro ao carregar tarefas: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    var projects by mutableStateOf<List<Projeto>>(emptyList())
        private set

    fun loadProjects() {
        viewModelScope.launch {
            try {
                isLoading = true
                println("DEBUG - Carregando projetos com repositório: $projetoRepository")
                projects = projetoRepository.listarProjetos()
                println("DEBUG - Projetos carregados: ${projects.size}")
            } catch (e: Exception) {
                println("DEBUG - Erro ao carregar projetos: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    var selectedProject by mutableStateOf<Projeto?>(null)
        private set

    fun selectProject(projeto: Projeto?) {
        selectedProject = projeto
    }


    val filteredTasks: List<Tarefa>
        get() = tasks.filter {
            println("DEBUG - Filtrando tarefa: ${it.nome}, projetoId: ${it.projetoId}, selectedProject: ${selectedProject?.id}")
            println("Debug - status: ${it.status}, selectedTab: ${selectedTab.name}")
            it.status == selectedTab.name &&
                    (selectedProject?.id == null || it.projetoId == selectedProject?.id)
        }


    fun selectTab(tab: TarefaStatus) {
        selectedTab = tab
    }

}
