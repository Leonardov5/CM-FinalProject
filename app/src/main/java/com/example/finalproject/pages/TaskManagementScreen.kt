package com.example.finalproject.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finalproject.components.BottomNavigation
import com.example.finalproject.components.TaskCard
import com.example.finalproject.components.TabRow
import com.example.finalproject.data.model.Task
import com.example.finalproject.data.model.TaskStatus
import com.example.finalproject.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagementScreen(
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {}
) {
    val tasks = remember {
        listOf(
            Task(1, "Task 1 - Completed", TaskStatus.COMPLETED, "20/04/2025", 3, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla facilisi. Nullam euismod, nisl eget aliquam ultricies, nunc nisl aliquet nunc, quis aliquam nisl nunc eu nisl."),
            Task(2, "Task 2 - On-Going", TaskStatus.ON_GOING, "15/05/2025", 2, "Implementar a funcionalidade de login com autenticação de dois fatores."),
            Task(3, "Task 3 - Completed", TaskStatus.COMPLETED, "10/03/2025", 1, "Criar wireframes para a nova interface do usuário."),
            Task(4, "Task 4 - Completed", TaskStatus.COMPLETED, "05/02/2025", 2, "Realizar testes de usabilidade com usuários."),
            Task(5, "Task 5 - To-Do", TaskStatus.TO_DO, "30/06/2025", 3, "Desenvolver a API de integração com serviços de terceiros.")
        )
    }

    var selectedTab by remember { mutableStateOf(TaskStatus.ON_GOING) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var showFabActions by remember { mutableStateOf(false) }

    // Um único Scaffold que contém tanto a lista de tarefas quanto a tela de detalhes
    Scaffold(
        topBar = {
            if (selectedTask == null) {
                // Barra superior para a lista de tarefas
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(0.7f),
                                shape = RoundedCornerShape(25.dp),
                                color = surfaceVariantLight
                            ) {
                                Text(
                                    text = "Project X",
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    color = onSurfaceVariantLight
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = primaryLight
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onProfileClick) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = primaryLight
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundLight
                    )
                )
            } else {
                // Barra superior para a tela de detalhes
                TopAppBar(
                    title = {
                        Text(
                            text = "Detalhes da Task",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { selectedTask = null }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundLight,
                        titleContentColor = onBackgroundLight
                    )
                )
            }
        },
        floatingActionButton = {
            if (selectedTask != null) {
                // FAB para a tela de detalhes
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
                                    // Implementar lógica para alterar status
                                    showFabActions = false
                                    selectedTask = null
                                }
                            )
                            ActionButton(
                                icon = Icons.Default.PlayArrow,
                                label = "Marcar como Em Andamento",
                                onClick = {
                                    // Implementar lógica para alterar status
                                    showFabActions = false
                                    selectedTask = null
                                }
                            )
                            ActionButton(
                                icon = Icons.Default.Add,
                                label = "Adicionar Trabalhador",
                                onClick = {
                                    showFabActions = false
                                    // Implementar lógica para adicionar trabalhador
                                }
                            )
                            ActionButton(
                                icon = Icons.Default.Delete,
                                label = "Excluir Task",
                                onClick = {
                                    showFabActions = false
                                    selectedTask = null
                                    // Implementar lógica para excluir task
                                }
                            )
                        }
                    }

                    // Main FAB
                    FloatingActionButton(
                        onClick = { showFabActions = !showFabActions },
                        containerColor = primaryLight,
                        contentColor = onPrimaryLight
                    ) {
                        Icon(
                            imageVector = if (showFabActions) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = if (showFabActions) "Fechar menu" else "Abrir menu"
                        )
                    }
                }
            }
        },
        // O bottomBar já é fornecido pelo AppNavigator, não precisamos adicionar aqui
        floatingActionButtonPosition = FabPosition.End,
        containerColor = backgroundLight
    ) { paddingValues ->
        if (selectedTask == null) {
            // Mostrar lista de tasks quando nenhuma estiver selecionada
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(backgroundLight)
            ) {
                // Tab Row
                TabRow(selectedTab) { tab ->
                    selectedTab = tab
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tasks List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks.filter { it.status == selectedTab }) { task ->
                        TaskCard(
                            task = task,
                            onClick = { selectedTask = task }
                        )
                    }

                    // Add some bottom padding
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        } else {
            // Mostrar tela de detalhes quando uma task for selecionada
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Título da task
                Text(
                    text = selectedTask!!.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = onBackgroundLight,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Status indicator
                StatusChip(status = selectedTask!!.status)

                Spacer(modifier = Modifier.height(24.dp))

                // Seção de progresso
                TaskInfoSection(
                    title = "Progresso",
                    content = {
                        LinearProgressIndicator(
                            progress = when(selectedTask!!.status) {
                                TaskStatus.TO_DO -> 0f
                                TaskStatus.ON_GOING -> 0.5f
                                TaskStatus.COMPLETED -> 1f
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = primaryLight,
                            trackColor = surfaceVariantLight
                        )

                        Text(
                            text = when(selectedTask!!.status) {
                                TaskStatus.TO_DO -> "0%"
                                TaskStatus.ON_GOING -> "50%"
                                TaskStatus.COMPLETED -> "100%"
                            },
                            fontSize = 14.sp,
                            color = onBackgroundLight,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Seção de descrição
                selectedTask!!.description?.let {
                    TaskInfoSection(
                        title = "Descrição",
                        content = {
                            Text(
                                text = it,
                                fontSize = 16.sp,
                                color = onBackgroundLight
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Seção de data de criação
                selectedTask!!.created?.let {
                    TaskInfoSection(
                        title = "Data de Criação",
                        content = {
                            Text(
                                text = it,
                                fontSize = 16.sp,
                                color = onBackgroundLight
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Seção de prioridade
                selectedTask!!.priority?.let {
                    TaskInfoSection(
                        title = "Prioridade",
                        content = {
                            Text(
                                text = "$it",
                                fontSize = 16.sp,
                                color = onBackgroundLight
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Seção de trabalhadores (simulado)
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
                                    .background(surfaceVariantLight),
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

                // Adicionando espaço suficiente para o FAB
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskManagementScreenPreview() {
    MaterialTheme {
        TaskManagementScreen()
    }
}

