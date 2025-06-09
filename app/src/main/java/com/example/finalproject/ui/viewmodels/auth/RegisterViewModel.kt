package com.example.finalproject.ui.viewmodels.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.data.service.UserService
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    // Estados da UI
    var name by mutableStateOf("")
        private set

    var username by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isRegistrationSuccessful by mutableStateOf(false)
        private set

    var showPassword by mutableStateOf(false)
        private set

    var showConfirmPassword by mutableStateOf(false)
        private set

    // Estados para diferentes tipos de mensagens
    var successMessage by mutableStateOf<String?>(null)
        private set

    var warningMessage by mutableStateOf<String?>(null)
        private set

    // Funções para atualizar estados
    fun onNameChange(newName: String) {
        name = newName
        clearMessages()
    }

    fun onUsernameChange(newUsername: String) {
        username = newUsername
        clearMessages()
    }

    fun onEmailChange(newEmail: String) {
        email = newEmail
        clearMessages()
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        clearMessages()
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        confirmPassword = newConfirmPassword
        clearMessages()
    }

    fun togglePasswordVisibility() {
        showPassword = !showPassword
    }

    fun toggleConfirmPasswordVisibility() {
        showConfirmPassword = !showConfirmPassword
    }

    private fun clearMessages() {
        if (errorMessage != null) {
            errorMessage = null
        }
        if (successMessage != null) {
            successMessage = null
        }
        if (warningMessage != null) {
            warningMessage = null
        }
    }

    // Função para realizar o registro completo (igual ao código original)
    fun register() {
        // Validação
        if (name.isBlank() || username.isBlank() || email.isBlank() ||
            password.isBlank() || confirmPassword.isBlank()) {
            errorMessage = "Todos os campos são obrigatórios"
            return
        }

        if (password != confirmPassword) {
            errorMessage = "As senhas não coincidem"
            return
        }

        if (password.length < 6) {
            errorMessage = "A senha deve ter pelo menos 6 caracteres"
            return
        }

        // Iniciar processo de registro
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                // 1. Registrar o usuário no sistema de autenticação
                val registerSuccess = AuthService.register(email, password)

                if (registerSuccess) {
                    // 2. Após registro bem-sucedido, fazer login automaticamente
                    val loginSuccess = AuthService.login(email, password)

                    if (loginSuccess) {
                        // 3. Salvar os dados do usuário na base de dados (sem a senha)
                        val userDataSaved = UserService.saveUserData(
                            username = username,
                            nome = name,
                            admin = false
                        )

                        if (userDataSaved) {
                            // Sucesso completo
                            successMessage = "Registro realizado com sucesso!"
                            isRegistrationSuccessful = true
                        } else {
                            // O usuário foi criado, mas os dados não foram salvos
                            warningMessage = "Usuário criado, mas houve erro ao salvar dados adicionais"
                            isRegistrationSuccessful = true // Ainda consideramos sucesso
                        }
                    } else {
                        errorMessage = "Falha no login automático após registro"
                    }
                } else {
                    errorMessage = "Falha no registro do usuário"
                }
            } catch (e: Exception) {
                errorMessage = "Erro ao registrar usuário: ${e.message ?: "Erro desconhecido"}"
            } finally {
                isLoading = false
            }
        }
    }

    // Função para limpar estado de registro bem-sucedido (útil após navegação)
    fun clearRegistrationSuccessState() {
        isRegistrationSuccessful = false
        successMessage = null
        warningMessage = null
    }

    // Função para limpar mensagens específicas
    fun clearSuccessMessage() {
        successMessage = null
    }

    fun clearWarningMessage() {
        warningMessage = null
    }

    fun clearErrorMessage() {
        errorMessage = null
    }

    // Função para limpar todos os campos
    fun clearFields() {
        name = ""
        username = ""
        email = ""
        password = ""
        confirmPassword = ""
        errorMessage = null
        successMessage = null
        warningMessage = null
    }

    // Função para validar email (opcional - pode ser usada para validação em tempo real)
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Função para validação completa (pode ser chamada antes do registro)
    fun validateFields(): Boolean {
        when {
            name.isBlank() -> {
                errorMessage = "Nome é obrigatório"
                return false
            }
            username.isBlank() -> {
                errorMessage = "Nome de usuário é obrigatório"
                return false
            }
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
            password.length < 6 -> {
                errorMessage = "A senha deve ter pelo menos 6 caracteres"
                return false
            }
            password != confirmPassword -> {
                errorMessage = "As senhas não coincidem"
                return false
            }
            else -> return true
        }
    }
}