package com.example.finalproject.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.finalproject.Screen

@Composable
fun BottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        NavigationItem(
            route = Screen.TaskManagement.route,
            icon = Icons.Default.Task,
            label = "Tarefas"
        ),
        NavigationItem(
            route = Screen.Projects.route,
            icon = Icons.Default.Folder,
            label = "Projetos"
        ),
        NavigationItem(
            route = Screen.Updates.route,
            icon = Icons.Default.Notifications,
            label = "Atualizações"
        )
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute.startsWith(item.route),
                onClick = {
                    if (currentRoute != item.route) {
                        onNavigate(item.route)
                    }
                }
            )
        }
    }
}