package com.example.finalproject.ui.viewmodels.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.repository.UtilizadorRepository
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val utilizadorRepository = UtilizadorRepository()

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

    fun onEmailChange(newEmail: String) {
        email = newEmail
        if (errorMessage != null) {
            errorMessage = null
        }
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        if (errorMessage != null) {
            errorMessage = null
        }
    }

    fun togglePasswordVisibility() {
        showPassword = !showPassword
    }

    fun login() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Fill all fields"
            return
        }

        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                val loginSuccess = utilizadorRepository.loginUser(email, password)

                if (loginSuccess) {
                    isLoginSuccessful = true
                    clearFieldsAfterSuccess()
                } else {
                    errorMessage = "Email or password is incorrect"
                }
            } catch (e: Exception) {
                errorMessage = "Error logging in: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearLoginSuccessState() {
        isLoginSuccessful = false
    }

    fun clearErrorMessage() {
        errorMessage = null
    }

    private fun clearFieldsAfterSuccess() {
        email = ""
        password = ""
        errorMessage = null
        showPassword = false
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}