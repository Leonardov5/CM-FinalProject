package com.example.finalproject.ui.viewmodels.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.finalproject.data.model.Tarefa
import com.example.finalproject.data.model.TarefaStatus
import com.example.finalproject.data.model.User
import com.example.finalproject.data.model.UserProject

// Mock implementation that will be used during tests
class TaskDetailViewModel : ViewModel() {

    var task by mutableStateOf<Tarefa?>(
        Tarefa(
            id = "task1",
            nome = "Implement user authentication",
            descricao = "Add login and registration functionality with Firebase Auth",
            projetoId = "project1",
            status = TarefaStatus.pendente.name,
            taxaConclusao = 0.0,
            prioridade = 1.toString(),
            createdAt = "2025-06-10T09:30:00Z"
        )
    )
        private set

    var isLoading by mutableStateOf(false)
        private set

    var showAddWorkerDialog by mutableStateOf(false)
        private set

    // Following the original ViewModel implementation style
    var showDeleteTaskDialog by mutableStateOf(false)
        private set

    var showEditTaskDialog by mutableStateOf(false)
        private set

    var isAdmin by mutableStateOf(false)
        private set

    var isManager by mutableStateOf(false)
        private set

    // Navigation events
    var navigateToObservacoesEvent by mutableStateOf<String?>(null)
        private set

    var navigateToTrabalhosEvent by mutableStateOf<String?>(null)
        private set

    var workerJoinDate by mutableStateOf<String?>(null)
        private set

    var showTaskAnalyticsExporterDialog by mutableStateOf(false)
        private set

    var trabalhadoresTarefa by mutableStateOf<List<String>>(listOf("user1", "user2"))
        private set

    var membrosProjetoUserProject by mutableStateOf<List<UserProject>>(
        listOf(
            UserProject("user1", "project1", true),
            UserProject("user2", "project1", false)
        )
    )
        private set

    var membrosProjeto by mutableStateOf<List<User>>(
        listOf(
            User(id = "user1", nome = "John Doe", email = "john@example.com"),
            User(id = "user2", nome = "Jane Smith", email = "jane@example.com")
        )
    )
        private set

    var filtredMembros by mutableStateOf<List<User>>(emptyList())
        private set

    // Methods from original ViewModel
    fun toggleEditTaskDialog() {
        showEditTaskDialog = !showEditTaskDialog
    }

    fun toggleDeleteTaskDialog() {
        showDeleteTaskDialog = !showDeleteTaskDialog
    }

    fun fetchWorkerJoinedDate(userId: String) {
        workerJoinDate = "2025-01-01T00:00:00Z" // Mock date
    }

    fun updateWorkerJoinDate(date: String?) {
        workerJoinDate = date
    }

    fun checkUser(currentUser: User? = null) {
        // Mock implementation
        isAdmin = true
        isManager = true
    }

    fun navigateToObservacoes(tarefaId: String) {
        navigateToObservacoesEvent = tarefaId
    }

    fun onObservacoesNavigated() {
        navigateToObservacoesEvent = null
    }

    fun navigateToTrabalhos(tarefaId: String) {
        navigateToTrabalhosEvent = tarefaId
    }

    fun onTrabalhosNavigated() {
        navigateToTrabalhosEvent = null
    }

    fun showTaskAnalyticsExporterDialog() {
        showTaskAnalyticsExporterDialog = true
    }

    fun hideTaskAnalyticsExporterDialog() {
        showTaskAnalyticsExporterDialog = false
    }

    fun exportTaskAnalytics(format: Any, context: android.content.Context? = null) {
        // Mock implementation
    }

    fun loadTrabalhadoresTarefa(tarefaId: String) {
        // Mock implementation does nothing or sets predefined data
    }

    fun loadMembrosProjeto(projetoId: String) {
        // Mock implementation does nothing or sets predefined data
    }

    fun filterMembros() {
        // Mock implementation does nothing or sets predefined data
    }

    fun toggleAddWorkerDialog() {
        showAddWorkerDialog = !showAddWorkerDialog
    }

    fun statusToEnum(status: String?): TarefaStatus =
        TarefaStatus.values().find { it.name == status } ?: TarefaStatus.pendente

    fun loadTask(taskId: String) {
        // Mock implementation does nothing or sets predefined data
    }

    fun addWorkerToTask(userId: String, tarefaId: String, onResult: (Boolean) -> Unit) {
        onResult(true)
    }

    fun removeWorkerFromTask(userId: String, tarefaId: String, onResult: (Boolean) -> Unit) {
        onResult(true)
    }

    fun reloadTaskAfterLogWork(tarefaId: String, onComplete: () -> Unit = {}) {
        onComplete()
    }

    fun deletarTarefa(taskId: String, onResult: (Boolean) -> Unit) {
        onResult(true)
    }

    fun editarTarefa(
        tarefaId: String,
        nome: String,
        descricao: String,
        prioridade: String,
        status: String,
        dataInicio: String?,
        dataFim: String?,
        taxaConclusao: Double,
        onComplete: (Boolean) -> Unit
    ) {
        onComplete(true)
    }
}