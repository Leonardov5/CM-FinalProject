package com.example.finalproject.ui.screens.tasks

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.data.model.Task
import com.example.finalproject.data.model.TaskStatus
import com.example.finalproject.data.model.DemoTasks
import com.example.finalproject.data.model.Tarefa
import com.example.finalproject.data.model.TarefaStatus
import com.example.finalproject.ui.components.tasks.AddWorkerDialog
import com.example.finalproject.ui.theme.*
import com.example.finalproject.ui.viewmodels.tasks.TaskDetailViewModel
import com.example.finalproject.ui.viewmodels.tasks.TaskManagementViewModel

fun formatDate(iso: String?): String? {
    return try {
        val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        iso?.let { dateFormat.format(isoFormat.parse(it)) }
    } catch (e: Exception) {
        iso
    }
}

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
    LaunchedEffect(key1 = true) {
        viewModel.loadTask(taskId)
    }

    LaunchedEffect(viewModel.task?.projetoId) {
        viewModel.task?.projetoId?.let { projetoId ->
            if (projetoId.isNotBlank()) {
                viewModel.loadMembrosProjeto(projetoId)
                viewModel.filterMembros()
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
                            label = "Marcar como Concluída",
                            onClick = {
                                onStatusChange(TarefaStatus.concluida)
                                showFabActions = false
                            }
                        )
                        ActionButton(
                            icon = Icons.Default.PlayArrow,
                            label = "Marcar como Em Andamento",
                            onClick = {
                                onStatusChange(TarefaStatus.em_andamento)
                                showFabActions = false
                            }
                        )
                        ActionButton(
                            icon = Icons.Default.Add,
                            label = "Adicionar Trabalhador",
                            onClick = {
                                showFabActions = false
                                viewModel.toggleAddWorkerDialog()
                            }
                        )
                        ActionButton(
                            icon = Icons.Default.Delete,
                            label = "Excluir Task",
                            onClick = {
                                showFabActions = false
                                onDeleteTask()
                            }
                        )
                    }
                }
                FloatingActionButton(
                    onClick = { showFabActions = !showFabActions },

                ) {
                    Icon(
                        imageVector = if (showFabActions) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (showFabActions) "Fechar menu" else "Abrir menu"
                    )
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
                        viewModel = viewModel
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
}

@Composable
private fun TaskContent(
    task: Tarefa,
    onStatusChange: (TarefaStatus) -> Unit,
    onDeleteTask: () -> Unit,
    onAddWorker: () -> Unit,
    viewModel: TaskDetailViewModel = viewModel()
) {
    val statusEnum = viewModel.statusToEnum(task.status)

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
            title = "Progresso",
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
                title = "Descrição",
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
                title = "Data de Criação",
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
                title = "Prioridade",
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
            title = "Trabalhadores",
            content = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WorkerAvatar("LV", primaryLight)
                    WorkerAvatar("MA", secondaryLight)
                    WorkerAvatar("GG", tertiaryLight)

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { onAddWorker() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Adicionar trabalhador",
                            tint = onSurfaceVariantLight
                        )
                    }
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
        TarefaStatus.pendente -> "A Fazer"
        TarefaStatus.em_andamento -> "Em Andamento"
        TarefaStatus.concluida -> "Concluído"
        TarefaStatus.cancelada -> "Cancelada"
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
private fun WorkerAvatar(
    initials: String,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontWeight = FontWeight.Bold
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
