package com.example.finalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.finalproject.pages.TaskManagementScreen
import com.example.finalproject.ui.theme.FinalProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinalProjectTheme {
                var isLoggedIn by remember { mutableStateOf(false) }
                var showRegister by remember { mutableStateOf(false) }

                when {
                    isLoggedIn -> {
                        // Usar o AppNavigator para gerenciar a navegação entre as telas
                        AppNavigator(isLoggedIn = true)
                    }
                    showRegister -> {
                        RegisterScreen(
                            onRegisterSuccess = {
                                // Voltar para a tela de login após registro bem-sucedido
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
                            onLoginSuccess = { isLoggedIn = true },
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

