package com.example.finalproject

import com.example.finalproject.ui.screens.tasks.TaskDetailScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.finalproject.ui.components.BottomNavigation
import com.example.finalproject.ui.screens.auth.LoginScreen
import com.example.finalproject.ui.screens.auth.RegisterScreen
import com.example.finalproject.ui.screens.tasks.TaskManagementScreen
import com.example.finalproject.ui.screens.ProfileScreen
import com.example.finalproject.ui.screens.projects.ProjectDetailScreen
import com.example.finalproject.ui.screens.projects.ProjectsScreen
import com.example.finalproject.ui.screens.UpdatesScreen
import com.example.finalproject.data.service.AuthService
import kotlinx.coroutines.launch
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object TaskManagement : Screen("tasks")
    object Projects : Screen("projects")
    object Updates : Screen("updates")
    object Profile : Screen("profile")
    object TaskDetail : Screen("task/{taskId}") {
        fun createRoute(taskId: String) = "task/$taskId"
    }
    object ProjectDetail : Screen("project/{projectId}") {
        fun createRoute(projectId: String) = "project/$projectId"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = when (currentDestination?.route) {
        Screen.Login.route, Screen.Register.route, Screen.Profile.route,
        Screen.TaskDetail.route, Screen.ProjectDetail.route -> false
        else -> true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigation(
                    currentRoute = currentDestination?.route ?: startDestination,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Configuração para evitar múltiplas instâncias da mesma tela
                            launchSingleTop = true
                            // Restaura o estado quando re-selecionado
                            restoreState = true
                            // Pop até a tela inicial para evitar pilha grande
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(route = Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.TaskManagement.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(route = Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = Screen.TaskManagement.route) {
                TaskManagementScreen(
                    modifier = Modifier,
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onTaskClick = { task ->
                        navController.navigate(Screen.TaskDetail.createRoute(task.id.toString()))
                    }
                )
            }

            composable(route = Screen.Projects.route) {
                ProjectsScreen(
                    modifier = Modifier,
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onProjectClick = { projectId ->
                        navController.navigate(Screen.ProjectDetail.createRoute(projectId))
                    }
                )
            }

            composable(route = Screen.Updates.route) {
                UpdatesScreen(
                    modifier = Modifier,
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route)
                    }
                )
            }

            composable(route = Screen.Profile.route) {
                val scope = rememberCoroutineScope()
                ProfileScreen(
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    onLogout = {
                        scope.launch {
                            AuthService.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(
                route = Screen.TaskDetail.route,
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                val scope = rememberCoroutineScope()

                TaskDetailScreen(
                    taskId = taskId,
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    onStatusChange = { newStatus ->
                        // Por enquanto não faz nada com o status
                    },
                    onDeleteTask = {
                        navController.popBackStack()
                    },
                    onAddWorker = {
                        // Por enquanto não faz nada
                    }
                )
            }

            composable(
                route = Screen.ProjectDetail.route,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                ProjectDetailScreen(
                    projetoId = projectId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAddTaskClick = {
                        // Implemente a adição de task ao projeto
                    }
                )
            }
        }
    }
}
