package com.example.finalproject

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.finalproject.pages.Tasks.TaskManagementScreen

// Rotas para nossa navegação
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object TaskManagement : Screen("task_management") // Tela de gerenciamento de tarefas
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Navegar para a tela principal após o login bem-sucedido
                    navController.navigate(Screen.TaskManagement.route) {
                        // Limpar a pilha de navegação para que o usuário não possa voltar para a tela de login
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    // Navegar para a tela de registro
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    // Após o registro bem-sucedido, navegar de volta para a tela de login
                    navController.navigate(Screen.Login.route) {
                        // Limpar a pilha de navegação ao voltar para o login
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    // Voltar para a tela de login
                    navController.popBackStack()
                }
            )
        }

        // Aqui você pode adicionar mais composables para outras telas do seu aplicativo
        composable(route = Screen.TaskManagement.route) {
            // Usando a tela de gerenciamento de tarefas
            TaskManagementScreen()
        }
    }
}

@Composable
fun MainPlaceholder(navController: NavHostController) {
    // Este é apenas um placeholder para a tela principal após o login
    // Substitua-o pela sua implementação real
    androidx.compose.material3.Surface(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            androidx.compose.material3.Text(
                text = "Tela Principal",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
            )

            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))

            androidx.compose.material3.Button(
                onClick = {
                    // Voltar para a tela de login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                androidx.compose.material3.Text("Sair")
            }
        }
    }
}
