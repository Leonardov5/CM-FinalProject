package com.example.finalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.ui.theme.FinalProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinalProjectTheme {
                // Usar o LaunchedEffect para verificar a autenticação ao iniciar
                var isLoggedIn by remember { mutableStateOf(AuthService.isAuthenticated()) }
                var showRegister by remember { mutableStateOf(false) }

                // Usar LaunchedEffect para garantir que a verificação de autenticação
                // só aconteça uma vez ao iniciar e não em cada recomposição
                LaunchedEffect(Unit) {
                    isLoggedIn = AuthService.isAuthenticated()
                }

                when {
                    isLoggedIn -> {
                        // O usuário já está logado, mostrar diretamente a tela de tarefas
                        AppNavigator(
                            isLoggedIn = true,
                            onLogout = {
                                // Ao fazer logout, atualizar o estado para mostrar a tela de login
                                isLoggedIn = false
                            }
                        )
                    }
                    showRegister -> {
                        RegisterScreen(
                            onRegisterSuccess = {
                                // Após registro bem-sucedido, definir como logado e ir para tarefas
                                isLoggedIn = true
                                showRegister = false
                            },
                            onNavigateToLogin = {
                                // Voltar para a tela de login quando clicar no botão
                                showRegister = false
                            }
                        )
                    }
                    else -> {
                        LoginScreen(
                            onLoginSuccess = {
                                // Após login bem-sucedido, definir como logado
                                isLoggedIn = true
                            },
                            onNavigateToRegister = { showRegister = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Você está logado! Bem-vindo à tela inicial.",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FinalProjectTheme {
        LoginScreen(
            onLoginSuccess = {},
            onNavigateToRegister = {}
        )
    }
}