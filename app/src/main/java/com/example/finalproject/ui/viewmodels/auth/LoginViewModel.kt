package com.example.finalproject.ui.viewmodels.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.RepositoryProvider
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    // Estados da UI
    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isLoginSuccessful by mutableStateOf(false)
        private set

    var showPassword by mutableStateOf(false)
        private set

    // Funções para atualizar estados
    fun onEmailChange(newEmail: String) {
        email = newEmail
        // Limpar mensagem de erro quando o usuário começa a digitar novamente
        if (errorMessage != null) {
            errorMessage = null
        }
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        // Limpar mensagem de erro quando o usuário começa a digitar novamente
        if (errorMessage != null) {
            errorMessage = null
        }
    }

    fun togglePasswordVisibility() {
        showPassword = !showPassword
    }

    // Função para realizar o login (usando a mesma lógica do código original)
    fun login() {
        // Validação básica
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Preencha todos os campos"
            return
        }

        // Iniciar processo de login
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                // Usar a mesma lógica do código original
                val loginSuccess = RepositoryProvider.userRepository.loginUser(email, password)

                if (loginSuccess) {
                    isLoginSuccessful = true
                    // Limpar campos após login bem-sucedido
                    clearFieldsAfterSuccess()
                } else {
                    errorMessage = "Email ou senha incorretos"
                }
            } catch (e: Exception) {
                errorMessage = "Erro ao fazer login: ${e.message ?: "Erro desconhecido"}"
            } finally {
                isLoading = false
            }
        }
    }

    // Função para limpar estado de login bem-sucedido (útil após navegação)
    fun clearLoginSuccessState() {
        isLoginSuccessful = false
    }

    // Função para limpar mensagem de erro (para usar após Toast)
    fun clearErrorMessage() {
        errorMessage = null
    }

    // Função para limpar todos os campos
    fun clearFields() {
        email = ""
        password = ""
        errorMessage = null
        showPassword = false
    }

    // Função privada para limpar campos após login bem-sucedido
    private fun clearFieldsAfterSuccess() {
        email = ""
        password = ""
        errorMessage = null
        showPassword = false
    }

    // Função para validar email (opcional)
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Função para validação completa
    fun validateFields(): Boolean {
        when {
            email.isBlank() -> {
                errorMessage = "Email é obrigatório"
                return false
            }
            !isValidEmail(email) -> {
                errorMessage = "Email inválido"
                return false
            }
            password.isBlank() -> {
                errorMessage = "Senha é obrigatória"
                return false
            }
            else -> return true
        }
    }
}