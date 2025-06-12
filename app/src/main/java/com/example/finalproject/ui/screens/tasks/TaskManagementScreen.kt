package com.example.finalproject.ui.screens.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.R
import com.example.finalproject.data.model.Tarefa
import com.example.finalproject.ui.components.TaskCard
import com.example.finalproject.ui.components.TabRow
import com.example.finalproject.ui.theme.*
import com.example.finalproject.ui.viewmodels.tasks.TaskManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagementScreen(
    projetoId: String? = null,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onTaskClick: (Tarefa) -> Unit = {},
    viewModel: TaskManagementViewModel = viewModel()
) {
    LaunchedEffect(projetoId, viewModel.projects) {
        viewModel.loadTasks()
        viewModel.loadProjects()
        projetoId?.let { id ->
            val projeto = viewModel.projects.find { it.id.toString() == id }
            if (projeto != null) {
                viewModel.selectProject(projeto)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.showProjectDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(0.7f),
                            shape = RoundedCornerShape(25.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = viewModel.selectedProject?.nome ?: stringResource(id = R.string.select_project),
                                modifier = Modifier.padding(vertical = 12.dp),
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(id = R.string.menu),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = stringResource(id = R.string.profile),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },

                windowInsets = WindowInsets(0)
            )
        }
    ) { paddingValues ->
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primaryLight)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                TabRow(viewModel.selectedTab) { tab ->
                    viewModel.selectTab(tab)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tasks List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.filteredTasks) { task ->
                        TaskCard(
                            task = task,
                            onClick = { onTaskClick(task) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
    if (viewModel.showProjectDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showProjectDialog = false },
            title = { Text(stringResource(id = R.string.select_project)) },
            text = {
                Column {
                    // Opção para mostrar todas as tarefas
                    Text(
                        text = stringResource(id = R.string.all_projects),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectProject(null)
                                viewModel.showProjectDialog = false
                            },
                        color = if (viewModel.selectedProject == null)
                            MaterialTheme.colorScheme.secondaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 2.dp
                    ) {
                        Text(
                            text = "Todos os projetos",
                            modifier = Modifier.padding(12.dp),
                            color = if (viewModel.selectedProject == null)
                                MaterialTheme.colorScheme.onSecondaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // List of projects
                    viewModel.projects.forEach { projeto ->
                        val isSelected = viewModel.selectedProject?.id == projeto.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectProject(projeto)
                                    viewModel.showProjectDialog = false
                                },
                            color = if (isSelected)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = projeto.nome,
                                modifier = Modifier.padding(12.dp),
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TaskManagementScreenPreview() {
    MaterialTheme {
        TaskManagementScreen()
    }
}
