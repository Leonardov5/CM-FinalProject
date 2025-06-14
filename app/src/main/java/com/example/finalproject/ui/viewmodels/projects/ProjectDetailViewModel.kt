package com.example.finalproject.ui.viewmodels.projects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.model.Projeto
import com.example.finalproject.data.model.User
import com.example.finalproject.data.model.UserProject
import com.example.finalproject.data.repository.ProjetoRepository
import com.example.finalproject.data.repository.TarefaRepository
import com.example.finalproject.data.repository.UserRepository
import com.example.finalproject.data.service.UserService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File

class ProjectDetailViewModel(
    private val projetoRepository: ProjetoRepository = ProjetoRepository(),
    private val tarefaRepository: TarefaRepository = TarefaRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val analyticsExporter: ProjectAnalyticsExporter = ProjectAnalyticsExporter()
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

    var showEditProjectDialog by mutableStateOf(false)
        private set

    var navigateToTasksForProject by mutableStateOf<String?>(null)
        private set

    var membrosProjeto by mutableStateOf<List<User>>(emptyList())
        private set
    // Lista de membros do projeto com informações completas
    var membrosProjetoCompleto by mutableStateOf<List<UserProject>>(emptyList())
        private set

    var showAddMemberDialog by mutableStateOf(false)
        private set

    var allUsers by mutableStateOf<List<User>>(emptyList())
        private set

    var isManager by mutableStateOf(false)
        private set

    // Variáveis para o diálogo de detalhes do worker
    var showWorkerDetailDialog by mutableStateOf(false)
        private set

    var selectedWorker by mutableStateOf<UserProject?>(null)
        private set

    // Analytics exporter dialog state
    var showAnalyticsExporterDialog by mutableStateOf(false)
        private set

    fun showAddMemberDialog() { showAddMemberDialog = true }
    fun hideAddMemberDialog() { showAddMemberDialog = false }

    fun loadAllUsers() {
        viewModelScope.launch {
            allUsers = UserRepository().listarTodosUsuarios()
        }
    }

    fun onViewTasksClick() {
        println("DEBUG - onViewTasksClick chamado com projeto: $projeto")
        navigateToTasksForProject = projeto?.id?.toString()
    }

    fun onTasksNavigationHandled() {
        navigateToTasksForProject = null
    }

    // Funções para manipular diálogos
    fun showAddTaskDialog() {
        showAddTaskDialog = true
    }

    fun hideAddTaskDialog() {
        showAddTaskDialog = false
    }

    fun showEditProjectDialog() {
        showEditProjectDialog = true
    }

    fun hideEditProjectDialog() {
        showEditProjectDialog = false
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

    // Editar projeto
    fun updateProject(
        projetoId: String,
        nome: String,
        descricao: String,
        status: String,
        taxaConclusao: Float
    ) = viewModelScope.launch {
        try {
            val success = projetoRepository.atualizarProjeto(
                UUID.fromString(projetoId),
                nome,
                descricao.takeIf { it.isNotBlank() },
                status,
                taxaConclusao
            )

            if (success) {
                loadProject(projetoId)
                hideEditProjectDialog()
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    fun addMemberToProject(userId: String, isManager: Boolean) = viewModelScope.launch {
        val result = projetoRepository.adicionarUsuarioAoProjeto(userId, projeto?.id.toString(), isManager)
        if (result) {
            hideAddMemberDialog()
            // Recarregar a lista de membros
            projeto?.id?.toString()?.let { loadMembrosProjetoCompleto(it) }
        }
    }

    fun checkIfManager(projectId: String) {
        viewModelScope.launch {
            val membros = projetoRepository.listarMembrosDoProjeto(projectId)
            val userId = user?.id
            isManager = membros.any { it.userId == userId && it.isManager }
        }
    }

    // Carregar membros do projeto com informações completas (usando JOIN)
    fun loadMembrosProjetoCompleto(projectId: String) {
        viewModelScope.launch {
            try {
                // Obter membros do projeto com informações completas usando JOIN
                membrosProjetoCompleto = projetoRepository.listarMembrosProjetoCompleto(projectId)

                // imprimir a lista de membros para depuração
                membrosProjetoCompleto.forEach {
                    println("Membro: ${it}, Gestor: ${it.isManager}")
                }

                // Verifica se o usuário atual é gestor
                isManager = membrosProjetoCompleto.any {
                    it.userId == user?.id && it.isManager
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Verifica se um usuário específico é gestor do projeto
    fun isUserManager(userId: String): Boolean {
        return membrosProjetoCompleto.any { it.userId == userId && it.isManager }
    }

    // Funções para manipular o diálogo de detalhes do worker
    fun showWorkerDetailDialog(worker: UserProject) {
        selectedWorker = worker
        showWorkerDetailDialog = true
    }

    fun hideWorkerDetailDialog() {
        showWorkerDetailDialog = false
        selectedWorker = null
    }

    // Função para atualizar o papel e o estado do worker (gestor ou não, ativo ou não)
    fun updateWorkerRole(userId: String, isManager: Boolean, isActive: Boolean) = viewModelScope.launch {
        try {
            val success = projetoRepository.atualizarMembrosDoProjetoStatus(
                userId = userId,
                projectId = projeto?.id.toString(),
                isManager = isManager,
                isActive = isActive
            )

            if (success) {
                // Recarregar a lista de membros após a atualização
                loadMembrosProjetoCompleto(projeto?.id.toString())
                hideWorkerDetailDialog()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Methods for analytics exporter dialog
    fun showAnalyticsExporterDialog() {
        showAnalyticsExporterDialog = true
        // Preload analytics data for the current project
        projeto?.id?.toString()?.let { projectId ->
            analyticsExporter.loadAnalytics(projectId)
        }
    }

    fun hideAnalyticsExporterDialog() {
        showAnalyticsExporterDialog = false
        analyticsExporter.resetExportState()
    }

    fun exportProjectAnalytics(format: ExportFormat, context: android.content.Context? = null) {
        projeto?.id?.toString()?.let { projectId ->
            viewModelScope.launch {
                try {
                    // Get the app's external files directory (doesn't require special permissions)
                    val externalFilesDir = context?.getExternalFilesDir(null)

                    if (externalFilesDir != null) {
                        // Create directory for exports
                        val exportDir = File(externalFilesDir, "project_analytics")
                        if (!exportDir.exists()) {
                            exportDir.mkdirs()
                        }

                        // Create filename from project name and date
                        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val projectName = projeto?.nome?.replace(" ", "_") ?: "project"
                        val baseFileName = "${projectName}_analytics_$currentDate"

                        val filePath = File(exportDir, baseFileName).absolutePath

                        // Export the analytics data
                        analyticsExporter.exportAnalytics(projectId, filePath, format)

                        // Add debug log to verify file creation
                        val finalFile = if (filePath.endsWith(format.extension)) {
                            File(filePath)
                        } else {
                            File("$filePath${format.extension}")
                        }
                        println("File saved at: ${finalFile.absolutePath}, exists: ${finalFile.exists()}")
                    } else {
                        analyticsExporter.setError("External storage not available")
                    }
                } catch (e: Exception) {
                    analyticsExporter.setError(e.message ?: "Failed to export analytics")
                    e.printStackTrace()
                }
            }
        }
    }
}
