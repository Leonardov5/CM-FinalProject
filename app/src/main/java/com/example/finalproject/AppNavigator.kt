package com.example.finalproject

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.finalproject.components.BottomNavigation
import com.example.finalproject.pages.ProfileScreen
import com.example.finalproject.pages.ProjectDetailScreen
import com.example.finalproject.pages.ProjectsScreen
import com.example.finalproject.pages.Tasks.TaskDetailScreen
import com.example.finalproject.pages.Tasks.TaskManagementScreen
import com.example.finalproject.pages.UpdatesScreen
import com.example.finalproject.data.model.Task

/**
 * Navegador principal do aplicativo que gerencia a navegação entre as diferentes telas
 * utilizando a barra de navegação inferior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigator(
    isLoggedIn: Boolean,
    onLogout: () -> Unit
) {
    if (!isLoggedIn) {
        // Se o usuário não estiver logado, mostrar a tela de login
        LoginScreen(
            onLoginSuccess = { /* Será implementado no MainActivity */ },
            onNavigateToRegister = { /* Será implementado no MainActivity */ }
        )
        return
    }

    // Estado para controlar a tela atual
    var currentRoute by remember { mutableStateOf("tasks") }
    var showProfileScreen by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var selectedProjectId by remember { mutableStateOf<String?>(null) }

    when {
        showProfileScreen -> {
            // Mostrar a tela de perfil quando o usuário clicar no ícone de perfil
            ProfileScreen(
                onBackPressed = { showProfileScreen = false },
                onLogout = {
                    showProfileScreen = false
                    onLogout()  // Chamar o callback onLogout
                }
            )
        }
        selectedTask != null -> {
            // Mostrar a tela de detalhes da task
            TaskDetailScreen(
                task = selectedTask!!,
                onBackPressed = { selectedTask = null },
                onStatusChange = { newStatus ->
                    // Aqui você pode implementar a lógica para atualizar o status da task
                    // Por exemplo, atualizar o banco de dados
                    selectedTask = selectedTask?.copy(status = newStatus)
                },
                onDeleteTask = {
                    // Implementar lógica para deletar a task
                    selectedTask = null
                },
                onAddWorker = {
                    // Implementar lógica para adicionar trabalhador
                }
            )
        }
        selectedProjectId != null -> {
            // Mostrar a tela de detalhes do projeto
            ProjectDetailScreen(
                projetoId = selectedProjectId!!,
                onBackClick = { selectedProjectId = null },
                onAddTaskClick = {
                    // Aqui você pode implementar a lógica para adicionar tarefas ao projeto
                    // Por exemplo, navegar para uma tela de criação de tarefas
                }
            )
        }
        else -> {
            Scaffold(
                bottomBar = {
                    BottomNavigation(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            // Atualizar a rota atual quando o usuário navegar
                            currentRoute = route
                        }
                    )
                }
            ) { paddingValues ->
                // Conteúdo da tela atual
                when (currentRoute) {
                    "tasks" -> TaskManagementScreen(
                        modifier = Modifier.padding(paddingValues),
                        onProfileClick = { showProfileScreen = true },
                        onTaskClick = { task ->
                            selectedTask = task
                        }
                    )
                    "projects" -> ProjectsScreen(
                        modifier = Modifier.padding(paddingValues),
                        onProfileClick = { showProfileScreen = true },
                        onProjectClick = { projectId ->
                            selectedProjectId = projectId
                        }
                    )
                    "updates" -> UpdatesScreen(
                        modifier = Modifier.padding(paddingValues),
                        onProfileClick = { showProfileScreen = true }
                    )
                }
            }
        }
    }
}

