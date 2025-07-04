package com.example.finalproject.ui.screens.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.finalproject.ui.components.tasks.TabRow
import com.example.finalproject.ui.components.tasks.TaskCard
import com.example.finalproject.ui.theme.primaryLight
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
        viewModel.checkUser()
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
                            tint = MaterialTheme.colorScheme.background
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
                TabRow(
                    selectedTab = viewModel.selectedTab,
                    onTabSelected = { tab -> viewModel.selectTab(tab) },
                    isAdmin = viewModel.isAdmin
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tarefas
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
                    Surface(
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
                            text = stringResource(id = R.string.all_projects),
                            modifier = Modifier.padding(12.dp),
                            color = if (viewModel.selectedProject == null)
                                MaterialTheme.colorScheme.onSecondaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lista de projetos
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

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                Button(
                    onClick = { viewModel.showProjectDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
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
