package com.example.finalproject.ui.screens.tasks

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.ui.components.TaskCard
import com.example.finalproject.ui.components.TabRow
import com.example.finalproject.data.model.Task
import com.example.finalproject.ui.theme.*
import com.example.finalproject.ui.viewmodels.tasks.TaskManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagementScreen(
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onTaskClick: (Task) -> Unit = {},
    viewModel: TaskManagementViewModel = viewModel()
) {
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
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Project X",
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
                            contentDescription = "Menu"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Perfil",
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
            // Tab Row
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

                    // Add some bottom padding
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
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
