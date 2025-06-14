// UserManagementViewModel.kt
package com.example.finalproject.ui.viewmodels.users

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.model.User
import com.example.finalproject.data.repository.UtilizadorRepository
import com.example.finalproject.data.service.UserService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserManagementViewModel(
    private val utilizadorRepository: UtilizadorRepository = UtilizadorRepository()
    ) : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    var userToEdit by mutableStateOf<User?>(null)
        private set

    var showEditDialog by mutableStateOf(false)
        private set

    var showCreateDialog by mutableStateOf(false)
        private set
    var adminPassword by mutableStateOf<String?>(null)
    var adminEmail by mutableStateOf<String?>(null)

    var showAdminPasswordDialog by mutableStateOf(false)
        private set


    fun toggleAdminPasswordDialog() {
        showAdminPasswordDialog = !showAdminPasswordDialog
    }


    init { loadUsers() }

    fun loadUsers() {
        viewModelScope.launch {
            _users.value = utilizadorRepository.listarTodosUtilizadores()
            adminEmail = utilizadorRepository.getCurrentUser()?.email
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            val success = utilizadorRepository.eliminarUtilizador(userId)
            if (success) loadUsers()
        }
    }

    fun toggleCreateDialog() {
        showCreateDialog = !showCreateDialog
    }

    fun togleEditDialog(user: User? = null) {
        userToEdit = user
        showEditDialog = !showEditDialog
    }

    fun editarUsuario(userId: String, nome: String, username: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val sucesso = utilizadorRepository.atualizarUtilizador(userId, nome, username)
            if (sucesso) loadUsers()
            onResult(sucesso)
        }
    }

    fun registerNewUser(
        name: String,
        username: String,
        email: String,
        password: String,
        isAdmin: Boolean
    ) {
        viewModelScope.launch {
            val authSuccess = utilizadorRepository.registerUser(email, password)
            if (authSuccess) {
                UserService.saveUserData(username, name, isAdmin)
                val adminEmailValue = adminEmail
                val adminPasswordValue = adminPassword
                if (adminEmailValue != null && adminPasswordValue != null) {
                    utilizadorRepository.loginUser(adminEmailValue, adminPasswordValue)
                }
                loadUsers()
            }
        }
    }

    fun confirmAdminPassword(password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val email = adminEmail ?: return@launch onResult(false)
            val success = utilizadorRepository.loginUser(email, password)
            if (success) {
                adminPassword = password
            }
            onResult(success)
        }
    }
}