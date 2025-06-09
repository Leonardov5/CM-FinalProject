package com.example.finalproject.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.ui.screens.Update
import kotlinx.coroutines.launch

class UpdatesViewModel : ViewModel() {

    // Estados UI
    var updates by mutableStateOf<List<Update>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    // Carregar atualizações
    fun loadUpdates() {
        viewModelScope.launch {
            isLoading = true
            try {
                // Aqui você pode implementar a lógica real para carregar as atualizações do servidor
                // Por enquanto, vamos usar dados fictícios
                updates = listOf(
                    Update(1, "Task assigned", "You've been assigned to the task 'Create wireframes'", "Jun 1, 2025", true),
                    Update(2, "Comment on your task", "Maria commented on 'Update documentation'", "May 31, 2025", true),
                    Update(3, "Deadline approaching", "Task 'Design UI Components' is due tomorrow", "May 30, 2025"),
                    Update(4, "Project update", "Project 'Website Redesign' has been updated", "May 29, 2025"),
                    Update(5, "Task completed", "João marked the task 'Setup database' as completed", "May 28, 2025")
                )
            } catch (e: Exception) {
                println("DEBUG - Erro ao carregar atualizações: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Marcar uma atualização como lida
    fun markAsRead(updateId: Int) {
        viewModelScope.launch {
            updates = updates.map { update ->
                if (update.id == updateId) {
                    update.copy(isNew = false)
                } else {
                    update
                }
            }
        }
    }

    // Marcar todas as atualizações como lidas
    fun markAllAsRead() {
        viewModelScope.launch {
            updates = updates.map { it.copy(isNew = false) }
        }
    }

    init {
        loadUpdates()
    }
}
