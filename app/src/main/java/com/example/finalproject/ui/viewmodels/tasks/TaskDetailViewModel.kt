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
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.data.service.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

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

    var showTaskAnalyticsExporterDialog by mutableStateOf(false)
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

    // Methods for task analytics exporter dialog
    fun showTaskAnalyticsExporterDialog() {
        showTaskAnalyticsExporterDialog = true
    }

    fun hideTaskAnalyticsExporterDialog() {
        showTaskAnalyticsExporterDialog = false
    }

    fun exportTaskAnalytics(format: TaskExportFormat, context: android.content.Context? = null) {
        task?.id?.let { taskId ->
            viewModelScope.launch {
                try {
                    // Get the app's external files directory
                    val externalFilesDir = context?.getExternalFilesDir(null)

                    if (externalFilesDir != null) {
                        // Create directory for exports
                        val exportDir = java.io.File(externalFilesDir, "task_analytics")
                        if (!exportDir.exists()) {
                            exportDir.mkdirs()
                        }

                        // Create filename from task name and date
                        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                        val taskName = task?.nome?.replace(" ", "_") ?: "task"
                        val baseFileName = "${taskName}_analytics_$currentDate"

                        val filePath = java.io.File(exportDir, baseFileName).absolutePath

                        // Export the analytics data
                        val taskAnalyticsExporter = TaskAnalyticsExporter()
                        taskAnalyticsExporter.exportAnalytics(taskId, filePath, format)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}