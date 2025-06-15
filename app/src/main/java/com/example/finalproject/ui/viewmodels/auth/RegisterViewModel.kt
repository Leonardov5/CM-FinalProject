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

    var successMessage by mutableStateOf<String?>(null)
        private set

    var warningMessage by mutableStateOf<String?>(null)
        private set

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

        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                val registerSuccess = AuthService.register(email, password)

                if (registerSuccess) {
                    val loginSuccess = AuthService.login(email, password)

                    if (loginSuccess) {
                        val userDataSaved = UserService.saveUserData(
                            username = username,
                            nome = name,
                            admin = false
                        )

                        if (userDataSaved) {
                            successMessage = "Registro realizado com sucesso!"
                            isRegistrationSuccessful = true
                        } else {
                            warningMessage = "Usuário criado, mas houve erro ao salvar dados adicionais"
                            isRegistrationSuccessful = true
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

    fun clearRegistrationSuccessState() {
        isRegistrationSuccessful = false
        successMessage = null
        warningMessage = null
    }

    fun clearSuccessMessage() {
        successMessage = null
    }

    fun clearWarningMessage() {
        warningMessage = null
    }

    fun clearErrorMessage() {
        errorMessage = null
    }

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
}