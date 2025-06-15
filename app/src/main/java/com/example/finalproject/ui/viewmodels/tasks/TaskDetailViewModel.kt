package com.example.finalproject.ui.viewmodels.tasks

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.model.Tarefa
import com.example.finalproject.data.model.TarefaStatus
import com.example.finalproject.data.model.Utilizador
import com.example.finalproject.data.model.UserProject
import com.example.finalproject.data.repository.ProjetoRepository
import com.example.finalproject.data.repository.TarefaRepository
import com.example.finalproject.data.repository.UtilizadorRepository
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

    var membrosProjeto by mutableStateOf<List<Utilizador>>(emptyList())
        private set

    var filtredMembros by mutableStateOf<List<Utilizador>>(emptyList())
        private set

    var navigateToObservacoesEvent by mutableStateOf<String?>(null)
        private set

    var showDeleteTaskDialog by mutableStateOf(false)
        private set

    var showEditTaskDialog by mutableStateOf(false)
        private set

    fun toggleEditTaskDialog() {
        showEditTaskDialog = !showEditTaskDialog
    }

    fun toggleDeleteTaskDialog() {
        showDeleteTaskDialog = !showDeleteTaskDialog
    }

    var navigateToTrabalhosEvent by mutableStateOf<String?>(null)
        private set

    var workerJoinDate by mutableStateOf<String?>(null)
        private set

    fun fetchWorkerJoinedDate(userId: String) {
        workerJoinDate = null
        viewModelScope.launch {
            try {
                task?.id?.let { tarefaId ->

                    val date = taskRepository.obterDataEntradaUtilizador(userId, tarefaId)

                    workerJoinDate = date
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateWorkerJoinDate(date: String?) {
        workerJoinDate = date
    }

    var showTaskAnalyticsExporterDialog by mutableStateOf(false)
        private set

    fun checkUser(currentUser: Utilizador? = null) {
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
        isLoading = true
        viewModelScope.launch {
            try {
                trabalhadoresTarefa = taskRepository.getTrabalhadoresTarefa(tarefaId)

                isLoading = false
            } catch (e: Exception) {
                e.printStackTrace()
                isLoading = false
            }
        }
    }

    fun loadMembrosProjeto(projetoId: String) {
        viewModelScope.launch {
            val membros = projetoRepository.listarMembrosProjeto(projetoId)
            membrosProjetoUserProject = membros
            val allUsers = UtilizadorRepository().listarTodosUtilizadores()
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
                task = taskRepository.obterTarefaPorId(taskId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun addWorkerToTask(userId: String, tarefaId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = taskRepository.adicionarUtilizadorTarefa(userId, tarefaId)
            onResult(result)
            if (result) {
                loadTask(tarefaId)
                loadTrabalhadoresTarefa(tarefaId)
                task?.projetoId?.let { projetoId ->
                    loadMembrosProjeto(projetoId)
                }
            }
        }
    }

    fun removeWorkerFromTask(userId: String, tarefaId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = taskRepository.removerUtilizadorDaTarefa(userId, tarefaId)
            onResult(result)
            if (result) {
                loadTask(tarefaId)
                loadTrabalhadoresTarefa(tarefaId)
                task?.projetoId?.let { projetoId ->
                    loadMembrosProjeto(projetoId)
                }
            }
        }
    }

    fun reloadTaskAfterLogWork(
        tarefaId: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                loadTask(tarefaId)
                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
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

    fun eliminarTarefa(taskId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            val sucesso = taskRepository.eliminarTarefaPorId(taskId)
            isLoading = false
            onResult(sucesso)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
        viewModelScope.launch {
            isLoading = true
            val sucesso = taskRepository.atualizarTarefa(
                tarefaId = tarefaId,
                nome = nome,
                descricao = descricao,
                prioridade = prioridade,
                status = status,
                dataInicio = dataInicio,
                dataFim = dataFim,
                taxaConclusao = taxaConclusao
            )

            if (sucesso) {
                loadTask(tarefaId)
            }

            isLoading = false
            onComplete(sucesso)
        }
    }

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
                    val externalFilesDir = context?.getExternalFilesDir(null)

                    if (externalFilesDir != null) {
                        val exportDir = java.io.File(externalFilesDir, "task_analytics")
                        if (!exportDir.exists()) {
                            exportDir.mkdirs()
                        }

                        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                        val taskName = task?.nome?.replace(" ", "_") ?: "task"
                        val baseFileName = "${taskName}_analytics_$currentDate"

                        val filePath = java.io.File(exportDir, baseFileName).absolutePath

                        val taskAnalyticsExporter = TaskAnalyticsExporter()
                        taskAnalyticsExporter.exportAnalytics(taskId, filePath, format)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun marcarTarefaComoConcluida(tarefaId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                isLoading = true
                val tarefaAtual = task
                if (tarefaAtual != null) {
                    val sucesso = taskRepository.atualizarTarefa(
                        tarefaId = tarefaId,
                        nome = tarefaAtual.nome,
                        descricao = tarefaAtual.descricao ?: "",
                        prioridade = tarefaAtual.prioridade,
                        status = TarefaStatus.concluida.name,
                        dataInicio = tarefaAtual.dataInicio,
                        dataFim = tarefaAtual.dataFim,
                        taxaConclusao = 100.0
                    )

                    if (sucesso) {
                        loadTask(tarefaId)
                    }

                    onComplete(sucesso)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            } finally {
                isLoading = false
            }
        }
    }
}