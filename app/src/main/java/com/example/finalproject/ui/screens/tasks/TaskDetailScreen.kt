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
import com.example.finalproject.data.model.Task
import com.example.finalproject.data.model.TaskStatus
import com.example.finalproject.data.model.DemoTasks
import com.example.finalproject.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    onBackPressed: () -> Unit,
    onStatusChange: (TaskStatus) -> Unit = {},
    onDeleteTask: () -> Unit = {},
    onAddWorker: () -> Unit = {}
) {
    var showFabActions by remember { mutableStateOf(false) }
    val task = remember(taskId) { DemoTasks.getTaskById(taskId) }

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
                                onStatusChange(TaskStatus.COMPLETED)
                                showFabActions = false
                            }
                        )
                        ActionButton(
                            icon = Icons.Default.PlayArrow,
                            label = "Marcar como Em Andamento",
                            onClick = {
                                onStatusChange(TaskStatus.ON_GOING)
                                showFabActions = false
                            }
                        )
                        ActionButton(
                            icon = Icons.Default.Add,
                            label = "Adicionar Trabalhador",
                            onClick = {
                                showFabActions = false
                                onAddWorker()
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
                .padding(paddingValues)
        ) {
            if (task == null) {
                Text(
                    text = "Tarefa não encontrada",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                TaskContent(
                    task = task,
                    onStatusChange = onStatusChange,
                    onDeleteTask = onDeleteTask,
                    onAddWorker = onAddWorker
                )
            }
        }
    }
}

@Composable
private fun TaskContent(
    task: Task,
    onStatusChange: (TaskStatus) -> Unit,
    onDeleteTask: () -> Unit,
    onAddWorker: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Título da task
        Text(
            text = task.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Status indicator
        StatusChip(status = task.status)

        Spacer(modifier = Modifier.height(24.dp))

        // Seção de progresso
        TaskInfoSection(
            title = "Progresso",
            content = {
                LinearProgressIndicator(
                    progress = when(task.status) {
                        TaskStatus.TO_DO -> 0f
                        TaskStatus.ON_GOING -> 0.5f
                        TaskStatus.COMPLETED -> 1f
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),

                )

                Text(
                    text = when(task.status) {
                        TaskStatus.TO_DO -> "0%"
                        TaskStatus.ON_GOING -> "50%"
                        TaskStatus.COMPLETED -> "100%"
                    },
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Seção de descrição
        task.description?.let {
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
                        text = it,
                        fontSize = 16.sp,
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Seção de prioridade
        task.priority?.let {
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
private fun StatusChip(status: TaskStatus) {
    val (backgroundColor, textColor) = when(status) {
        TaskStatus.TO_DO -> Pair(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurface)
        TaskStatus.ON_GOING -> Pair(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        TaskStatus.COMPLETED -> Pair(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
    }

    val statusText = when(status) {
        TaskStatus.TO_DO -> "A Fazer"
        TaskStatus.ON_GOING -> "Em Andamento"
        TaskStatus.COMPLETED -> "Concluído"
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
