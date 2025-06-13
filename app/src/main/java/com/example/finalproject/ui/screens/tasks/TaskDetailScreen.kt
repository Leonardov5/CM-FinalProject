package com.example.finalproject.ui.screens.tasks

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.R
import com.example.finalproject.data.PreferencesManager
import com.example.finalproject.data.model.Tarefa
import com.example.finalproject.data.model.TarefaStatus
import com.example.finalproject.data.model.User
import com.example.finalproject.ui.components.tasks.AddWorkerDialog
import com.example.finalproject.ui.components.tasks.LogWorkDialog
import com.example.finalproject.ui.components.tasks.WorkerCardTask
import com.example.finalproject.ui.components.tasks.WorkerTaskDetailDialog
import com.example.finalproject.ui.theme.*
import com.example.finalproject.ui.viewmodels.tasks.TaskDetailViewModel
import com.example.finalproject.ui.viewmodels.tasks.TaskManagementViewModel
import com.example.finalproject.utils.updateAppLanguage
import java.time.LocalDateTime

fun formatDate(iso: String?): String? {
    return try {
        val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        iso?.let { dateFormat.format(isoFormat.parse(it)) }
    } catch (e: Exception) {
        iso
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    onBackPressed: () -> Unit,
    onStatusChange: (TarefaStatus) -> Unit = {},
    onDeleteTask: () -> Unit = {},
    onAddWorker: () -> Unit = {},
    viewModel: TaskDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    var showLogWorkDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.loadTask(taskId)
        viewModel.loadTrabalhadoresTarefa(taskId)

        val savedLanguage = PreferencesManager.getLanguage(context)
        updateAppLanguage(context, savedLanguage)
    }

    LaunchedEffect(viewModel.task?.projetoId) {
        viewModel.task?.projetoId?.let { projetoId ->
            if (projetoId.isNotBlank()) {
                viewModel.loadMembrosProjeto(projetoId)
                viewModel.filterMembros()
                viewModel.checkUser()
            }
        }
    }

    LaunchedEffect(viewModel.membrosProjeto) {
        viewModel.membrosProjeto.let { membros ->
            if (membros.isNotEmpty()) {
                viewModel.filterMembros()
            }
        }
    }


    var showFabActions by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalhes da Task",
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },

                windowInsets = WindowInsets(0)
            )
        },
        floatingActionButton = {
            if (viewModel.isAdmin || viewModel.isManager) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // FAB menu
                    AnimatedVisibility(
                        visible = showFabActions,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }) + expandVertically(),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }) + shrinkVertically()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            ActionButton(
                                icon = Icons.Default.Done,
                                label = stringResource(id = R.string.mark_as_completed),
                                onClick = {
                                    onStatusChange(TarefaStatus.concluida)
                                    showFabActions = false
                                }
                            )
                            ActionButton(
                                icon = Icons.Default.PlayArrow,
                                label = stringResource(id = R.string.mark_as_active),
                                onClick = {
                                    onStatusChange(TarefaStatus.em_andamento)
                                    showFabActions = false
                                }
                            )
                            ActionButton(
                                icon = Icons.Default.Add,
                                label = stringResource(id = R.string.add_worker),
                                onClick = {
                                    showFabActions = false
                                    viewModel.toggleAddWorkerDialog()
                                }
                            )
                            ActionButton(
                                icon = Icons.Default.Work,
                                label = "Log Work",
                                onClick = {
                                    showFabActions = false
                                    showLogWorkDialog = true
                                }
                            )
                            if( viewModel.isAdmin) {
                                ActionButton(
                                    icon = Icons.Default.Delete,
                                    label = stringResource(id = R.string.delete_task),
                                    onClick = {
                                        showFabActions = false
                                        onDeleteTask()
                                    }
                                )
                            }
                        }
                    }
                    FloatingActionButton(
                        onClick = { showFabActions = !showFabActions },

                        ) {
                        Icon(
                            imageVector = if (showFabActions) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = if (showFabActions) stringResource(id = R.string.close_menu) else stringResource(
                                id = R.string.open_menu
                            )
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                viewModel.isLoading -> {
                    CircularProgressIndicator()
                }
                viewModel.task == null -> {
                    Text(
                        text = "Tarefa não encontrada"
                    )
                }
                else -> {
                    TaskContent(
                        task = viewModel.task!!,
                        onStatusChange = onStatusChange,
                        onDeleteTask = onDeleteTask,
                        onAddWorker = onAddWorker,
                        viewModel = viewModel,
                        onLogWork = { hours, description ->
                            //viewModel.logWork(taskId, hours, description) {
                                // feedback opcional
                            //}
                        }
                    )
                }
            }
        }
    }

    if (viewModel.showAddWorkerDialog) {
        AddWorkerDialog(
            users = viewModel.filtredMembros,
            onDismiss = { viewModel.toggleAddWorkerDialog() },
            onAdd = { userIds ->
                userIds.forEach { userId ->
                    viewModel.addWorkerToTask(userId, taskId) { /* feedback opcional */ }
                }
                viewModel.toggleAddWorkerDialog()
            }
        )
    }

    if (showLogWorkDialog && viewModel.task != null) {
        LogWorkDialog(
            show = true,
            tarefaId = taskId,
            onDismiss = { showLogWorkDialog = false },
            onSuccess = {
                // Apenas recarregar a tarefa para mostrar possíveis atualizações
                viewModel.reloadTaskAfterLogWork(taskId)
            }
        )
    }
}

@Composable
private fun TaskContent(
    task: Tarefa,
    onStatusChange: (TarefaStatus) -> Unit,
    onDeleteTask: () -> Unit,
    onAddWorker: () -> Unit,
    viewModel: TaskDetailViewModel = viewModel(),
    onLogWork: (hours: Int, description: String) -> Unit = { _, _ -> }
) {
    val statusEnum = viewModel.statusToEnum(task.status)
    var selectedWorker by remember { mutableStateOf<User?>(null) }
    var showWorkerDialog by remember { mutableStateOf(false) }

    StatusChip(status = statusEnum)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Título da task
        Text(
            text = task.nome,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Status indicator
        StatusChip(status = statusEnum)

        Spacer(modifier = Modifier.height(24.dp))

        // Seção de progresso
        TaskInfoSection(
            title = stringResource(id = R.string.progress),
            content = {
                LinearProgressIndicator(
                    progress = task.taxaConclusao.toFloat().coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                )

                Text(
                    text = "${(task.taxaConclusao * 100).toInt()}%",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Seção de descrição
        task.descricao?.let {
            TaskInfoSection(
                title = stringResource(id = R.string.description),
                content = {
                    Text(
                        text = it,
                        fontSize = 16.sp,
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Seção de data de criação
        task.createdAt?.let {
            TaskInfoSection(
                title = stringResource(id = R.string.created_at),
                content = {
                    Text(
                        text = formatDate(it) ?: "",
                        fontSize = 16.sp,
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Seção de prioridade
        task.prioridade?.let {
            TaskInfoSection(
                title = stringResource(id = R.string.priority),
                content = {
                    Text(
                        text = "$it",
                        fontSize = 16.sp,
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Seção de trabalhadores
        TaskInfoSection(
            title = stringResource(id = R.string.workers),
            content = {
                Column {
                    viewModel.trabalhadoresTarefa.forEach { userId ->
                        val user = viewModel.membrosProjeto.find { it.id == userId }
                        if (user != null) {
                            WorkerCardTask(
                                worker = user,
                                onClick = {
                                    if (viewModel.isAdmin || viewModel.isManager){
                                        selectedWorker = user
                                        showWorkerDialog = true
                                    }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if(viewModel.isAdmin || viewModel.isManager){
                        Button(
                            onClick = { viewModel.toggleAddWorkerDialog() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_worker))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(id = R.string.add_worker))
                        }

                    }
                }
            }
        )

        // Diálogo de detalhes do trabalhador
        WorkerTaskDetailDialog(
            show = showWorkerDialog,
            worker = selectedWorker,
            onDismiss = { showWorkerDialog = false },
            onRemove = { userId ->
                viewModel.removeWorkerFromTask(userId, viewModel.task?.id ?: "") {
                    showWorkerDialog = false
                }
            }
        )
        // Espaço para o FAB
        Spacer(modifier = Modifier.height(100.dp))
    }
}

// Componentes específicos do TaskDetailScreen
@Composable
private fun TaskInfoSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        content()
    }
}

@Composable
private fun StatusChip(status: TarefaStatus) {
    val (backgroundColor, textColor) = when(status) {
        TarefaStatus.pendente -> Pair(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurface)
        TarefaStatus.em_andamento -> Pair(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        TarefaStatus.concluida -> Pair(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        TarefaStatus.cancelada -> Pair(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
    }

    val statusText = when(status) {
        TarefaStatus.pendente -> stringResource(id = R.string.to_do)
        TarefaStatus.em_andamento -> stringResource(id = R.string.on_going)
        TarefaStatus.concluida -> stringResource(id = R.string.completed)
        TarefaStatus.cancelada -> stringResource(id = R.string.cancelled)
    }

    Surface(
        color = backgroundColor,
        contentColor = textColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
            )
            Text(
                text = label,
            )
        }
    }
}
