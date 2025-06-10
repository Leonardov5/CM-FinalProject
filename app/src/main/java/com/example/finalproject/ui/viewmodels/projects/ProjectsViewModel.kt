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

class ProjectsViewModel(
    private val projetoRepository: ProjetoRepository = ProjetoRepository(),
    private val tarefaRepository: TarefaRepository = TarefaRepository()
) : ViewModel() {

    // Estados UI
    var projects by mutableStateOf<List<Projeto>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var user by mutableStateOf<User?>(null)
        private set

    var isAdmin by mutableStateOf(false)
        private set

    var showAddDialog by mutableStateOf(false)
        private set


    var selectedProjectUuid by mutableStateOf<String?>(null)
        private set

    var projectName by mutableStateOf("")
        private set

    var projectDescription by mutableStateOf("")
        private set

    // Funções para manipular estados
    fun onProjectNameChange(name: String) {
        projectName = name
    }

    fun onProjectDescriptionChange(description: String) {
        projectDescription = description
    }

    fun showAddProjectDialog() {
        showAddDialog = true
    }

    fun hideAddProjectDialog() {
        showAddDialog = false
        // Limpar os campos quando o diálogo for fechado
        projectName = ""
        projectDescription = ""
    }

    // Carregar usuário e verificar se é admin
    fun loadUser() {
        viewModelScope.launch {
            try {
                user = UserService.getCurrentUserData()
                isAdmin = user?.admin == true
                println("DEBUG - Usuário carregado: $user")
                println("DEBUG - Usuário é admin: $isAdmin")
            } catch (e: Exception) {
                println("DEBUG - Erro ao carregar usuário: ${e.message}")
            }
        }
    }

    // Carregar projetos
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

    // Criar um novo projeto
    fun createProject(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (projectName.isBlank()) {
            onError("Nome do projeto é obrigatório")
            return
        }

        viewModelScope.launch {
            try {
                isLoading = true
                val novoProjeto = projetoRepository.criarProjeto(
                    nome = projectName,
                    descricao = projectDescription.takeIf { it.isNotBlank() }
                )

                if (novoProjeto != null) {
                    // Atualiza a lista de projetos
                    projects = projetoRepository.listarProjetos()
                    hideAddProjectDialog()
                    onSuccess()
                } else {
                    onError("Erro ao criar projeto")
                }
            } catch (e: Exception) {
                onError("Erro: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    init {
        loadUser()
        loadProjects()
    }
}