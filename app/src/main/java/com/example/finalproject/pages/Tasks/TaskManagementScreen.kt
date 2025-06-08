package com.example.finalproject.pages.Tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finalproject.components.TaskCard
import com.example.finalproject.components.TabRow
import com.example.finalproject.data.model.Task
import com.example.finalproject.data.model.TaskStatus
import com.example.finalproject.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagementScreen(
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onTaskClick: (Task) -> Unit = {}
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

    Scaffold(
        topBar = {
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
        },
        containerColor = backgroundLight
    ) { paddingValues ->
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
                        onClick = { onTaskClick(task) }
                    )
                }

                // Add some bottom padding
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
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