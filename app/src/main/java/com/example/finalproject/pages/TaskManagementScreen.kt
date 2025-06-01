package com.example.finalproject.pages

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
fun TaskManagementScreen(modifier: Modifier = Modifier) {
    val tasks = remember {
        listOf(
            Task(1, "Task 1 - Completed", TaskStatus.COMPLETED, "20/04/2025", 3, "Lorem ipsum dolor sit amet, consectetur"),
            Task(2, "Task 2 - On-Going", TaskStatus.ON_GOING),
            Task(3, "Task 3 - Completed", TaskStatus.COMPLETED),
            Task(4, "Task 4 - Completed", TaskStatus.COMPLETED),
            Task(5, "Task 5 - To-Do", TaskStatus.TO_DO)
        )
    }

    var selectedTab by remember { mutableStateOf(TaskStatus.ON_GOING) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundLight)
    ) {
        // Top Bar
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
                IconButton(onClick = { }) {
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
                TaskCard(task = task)
            }

            // Add some bottom padding
            item {
                Spacer(modifier = Modifier.height(16.dp))
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

