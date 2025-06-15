package com.example.finalproject.ui.viewmodels.projects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.R
import com.example.finalproject.data.model.Projeto
import com.example.finalproject.data.model.Utilizador
import com.example.finalproject.data.repository.ProjetoRepository
import com.example.finalproject.data.service.UserService
import kotlinx.coroutines.launch

class ProjectsViewModel(
    private val projetoRepository: ProjetoRepository = ProjetoRepository(),
) : ViewModel() {

    var projects by mutableStateOf<List<Projeto>>(emptyList())
        private set

    private var allProjects = listOf<Projeto>()

    var isLoading by mutableStateOf(false)
        private set

    var user by mutableStateOf<Utilizador?>(null)
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
        projectName = ""
        projectDescription = ""
    }

    fun loadUser() {
        viewModelScope.launch {
            try {
                user = UserService.getCurrentUserData()
                isAdmin = user?.admin == true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadProjects() {
        viewModelScope.launch {
            try {
                isLoading = true
                allProjects = projetoRepository.listarProjetos()
                projects = allProjects
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun filterProjects(query: String) {
        if (query.isBlank()) {
            projects = allProjects
            return
        }

        val filteredList = allProjects.filter { projeto ->
            projeto.nome.contains(query, ignoreCase = true) ||
                    (projeto.descricao?.contains(query, ignoreCase = true) ?: false)
        }

        projects = filteredList
    }

    fun resetFilter() {
        projects = allProjects
    }

    fun createProject(onSuccess: () -> Unit, onError: (Int) -> Unit) {

        viewModelScope.launch {
            try {
                if (projectName.isBlank()) {
                    onError(R.string.error_project_name_empty)
                    return@launch
                }
                isLoading = true
                val novoProjeto = projetoRepository.criarProjeto(
                    nome = projectName,
                    descricao = projectDescription.takeIf { it.isNotBlank() }
                )

                if (novoProjeto != null) {
                    allProjects = projetoRepository.listarProjetos()
                    projects = allProjects
                    hideAddProjectDialog()
                    onSuccess()
                } else {
                    onError(R.string.error_project_create_failed)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(R.string.error_project_create_failed)
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