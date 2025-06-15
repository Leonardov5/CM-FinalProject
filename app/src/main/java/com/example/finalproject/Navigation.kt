package com.example.finalproject

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.finalproject.ui.screens.tasks.TaskDetailScreen
import com.example.finalproject.ui.screens.tasks.ObservacoesScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.finalproject.data.PreferencesManager
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.ui.components.BottomNavigation
import com.example.finalproject.ui.screens.IntroSlider
import com.example.finalproject.ui.screens.ProfileScreen
import com.example.finalproject.ui.screens.UpdatesScreen
import com.example.finalproject.ui.screens.auth.LoginScreen
import com.example.finalproject.ui.screens.auth.RegisterScreen
import com.example.finalproject.ui.screens.tasks.TaskManagementScreen
import com.example.finalproject.ui.screens.projects.ProjectDetailScreen
import com.example.finalproject.ui.screens.projects.ProjectsScreen
import com.example.finalproject.ui.screens.tasks.TrabalhosScreen
import com.example.finalproject.ui.viewmodels.tasks.TaskDetailViewModel
import kotlinx.coroutines.launch
import com.example.finalproject.ui.screens.users.UserManagementScreen
import com.example.finalproject.ui.viewmodels.users.UserManagementViewModel

sealed class Screen(val route: String) {
    object IntroSlider : Screen("intro_slider")
    object Login : Screen("login")
    object Register : Screen("register")
    object TaskManagement : Screen("tasks?projetoId={projetoId}") {
        fun createRoute(projetoId: String?) =
            if (projetoId != null) "tasks?projetoId=$projetoId" else "tasks"
    }
    object Projects : Screen("projects")
    object Updates : Screen("updates")
    object Profile : Screen("profile")
    object TaskDetail : Screen("task/{taskId}") {
        fun createRoute(taskId: String) = "task/$taskId"
    }
    object ProjectDetail : Screen("project/{projectId}") {
        fun createRoute(projectId: String) = "project/$projectId"
    }
    object Observacoes : Screen("observacoes/{tarefaId}") {
        fun createRoute(tarefaId: String) = "observacoes/$tarefaId"
    }
    object Trabalhos : Screen("trabalhos/{tarefaId}") {
        fun createRoute(tarefaId: String) = "trabalhos/$tarefaId"
    }
    object UserManagement : Screen("user_management")
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    var isAuthenticated by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isAuthenticated = AuthService.isAuthenticated()
        isLoading = false
    }

    val startDestination = when {
        isLoading -> Screen.Login.route
        PreferencesManager.isFirstLaunch(context) -> Screen.IntroSlider.route
        isAuthenticated -> Screen.TaskManagement.route
        else -> Screen.Login.route
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = when (currentDestination?.route) {
        Screen.IntroSlider.route, Screen.Login.route, Screen.Register.route, Screen.Profile.route,
        Screen.TaskDetail.route, Screen.ProjectDetail.route, Screen.Observacoes.route,
        Screen.Trabalhos.route -> false
        else -> true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigation(
                    currentRoute = currentDestination?.route ?: startDestination,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Evita múltiplas instâncias da mesma página
                            launchSingleTop = true
                            // Restaura o estado quando re-selecionado
                            restoreState = true
                            // Pop até a página inicial do grafo
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

            composable(
                route = "tasks?projetoId={projetoId}",
                arguments = listOf(
                    navArgument("projetoId") {
                        type = NavType.StringType
                        defaultValue = null
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val projetoId = backStackEntry.arguments?.getString("projetoId")
                TaskManagementScreen(
                    projetoId = projetoId,
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
                val viewModel = viewModel<TaskDetailViewModel>()

                TaskDetailScreen(
                    taskId = taskId,
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    onStatusChange = { newStatus ->
                    },
                    onDeleteTask = {
                        navController.popBackStack()
                    },
                    onAddWorker = {
                    },
                    onNavigateToTrabalhos = { tarefaId ->
                        navController.navigate(Screen.Trabalhos.createRoute(tarefaId))
                    },
                    viewModel = viewModel
                )

                LaunchedEffect(viewModel.navigateToObservacoesEvent) {
                    viewModel.navigateToObservacoesEvent?.let { tarefaId ->
                        navController.navigate(Screen.Observacoes.createRoute(tarefaId))
                        viewModel.onObservacoesNavigated()
                    }
                }
            }

            composable(
                route = Screen.Observacoes.route,
                arguments = listOf(navArgument("tarefaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tarefaId = backStackEntry.arguments?.getString("tarefaId") ?: ""

                ObservacoesScreen(
                    tarefaId = tarefaId,
                    onBackPressed = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.Trabalhos.route,
                arguments = listOf(navArgument("tarefaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tarefaId = backStackEntry.arguments?.getString("tarefaId") ?: ""

                TrabalhosScreen(
                    tarefaId = tarefaId,
                    onBackPressed = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = Screen.UserManagement.route) {
                val userManagementViewModel: UserManagementViewModel = viewModel()
                UserManagementScreen(
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route)
                    },
                    viewModel = userManagementViewModel,
                    onAddUser = {  },
                )
            }

            composable(
                route = Screen.ProjectDetail.route,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: ""

                ProjectDetailScreen(
                    projetoId = projectId,
                    navController = navController,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = Screen.IntroSlider.route) {
                IntroSlider(navController = navController)
            }
        }
    }

}
