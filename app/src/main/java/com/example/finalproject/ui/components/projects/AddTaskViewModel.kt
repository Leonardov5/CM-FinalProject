package com.example.finalproject.ui.components.projects

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AddTaskViewModel : ViewModel() {

    // Estado do formulário
    private val _uiState = MutableStateFlow(AddTaskUIState())
    val uiState: StateFlow<AddTaskUIState> = _uiState.asStateFlow()

    // Formatador de data para ISO (para envio ao servidor)
    @RequiresApi(Build.VERSION_CODES.O)
    private val isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    // Listas de opções
    val prioridades = listOf("baixa", "media", "alta", "critica")
    val statusList = listOf("pendente", "em_andamento", "concluida", "cancelada")

    // Funções para atualizar os campos
    fun updateNome(nome: String) {
        _uiState.update { it.copy(
            nome = nome,
            nomeError = false
        ) }
    }

    fun updateDescricao(descricao: String) {
        _uiState.update { it.copy(
            descricao = descricao,
            descricaoError = false
        ) }
    }

    fun updatePrioridade(prioridade: String) {
        _uiState.update { it.copy(
            prioridade = prioridade,
            prioridadeError = false
        ) }
    }

    fun updateStatus(status: String) {
        _uiState.update { it.copy(
            status = status,
            statusError = false
        ) }
    }

    fun updateDataInicio(dataInicio: LocalDateTime?) {
        _uiState.update { it.copy(
            dataInicio = dataInicio,
            dataInicioError = false
        ) }
    }

    fun updateDataFim(dataFim: LocalDateTime?) {
        _uiState.update { it.copy(
            dataFim = dataFim,
            dataFimError = false
        ) }
    }

    // Funções para controlar os diálogos de data/hora
    fun showDataInicioDialog() {
        _uiState.update { it.copy(showDataInicioDialog = true) }
    }

    fun hideDataInicioDialog() {
        _uiState.update { it.copy(showDataInicioDialog = false) }
    }

    fun showDataFimDialog() {
        _uiState.update { it.copy(showDataFimDialog = true) }
    }

    fun hideDataFimDialog() {
        _uiState.update { it.copy(showDataFimDialog = false) }
    }

    // Função para validar e submeter o formulário
    @RequiresApi(Build.VERSION_CODES.O)
    fun validateAndSubmitTask(
        onAddTask: (
            nome: String,
            descricao: String,
            prioridade: String,
            status: String,
            dataInicio: String?,
            dataFim: String?
        ) -> Unit
    ): Boolean {
        val currentState = _uiState.value

        // Validar campos obrigatórios
        val isNomeValid = currentState.nome.isNotBlank()
        val isDescricaoValid = currentState.descricao.isNotBlank()
        val isPrioridadeValid = currentState.prioridade.isNotBlank()
        val isStatusValid = currentState.status.isNotBlank()
        val isDataInicioValid = currentState.dataInicio != null
        val isDataFimValid = currentState.dataFim != null

        // Atualizar estado com erros
        _uiState.update { it.copy(
            nomeError = !isNomeValid,
            descricaoError = !isDescricaoValid,
            prioridadeError = !isPrioridadeValid,
            statusError = !isStatusValid,
            dataInicioError = !isDataInicioValid,
            dataFimError = !isDataFimValid
        ) }

        // Verificar se todos os campos são válidos
        val isFormValid = isNomeValid && isDescricaoValid &&
                          isPrioridadeValid && isStatusValid &&
                          isDataInicioValid && isDataFimValid

        // Submeter o formulário se for válido
        if (isFormValid) {
            onAddTask(
                currentState.nome,
                currentState.descricao,
                currentState.prioridade,
                currentState.status,
                currentState.dataInicio?.format(isoFormatter),
                currentState.dataFim?.format(isoFormatter)
            )

            // Limpar o formulário após submissão
            resetForm()
        }

        return isFormValid
    }

    // Função para resetar o formulário
    fun resetForm() {
        _uiState.update { AddTaskUIState() }
    }
}

// Classe que representa o estado da UI
data class AddTaskUIState(
    val nome: String = "",
    val descricao: String = "",
    val prioridade: String = "",
    val status: String = "",
    val dataInicio: LocalDateTime? = null,
    val dataFim: LocalDateTime? = null,

    // Estados de erro
    val nomeError: Boolean = false,
    val descricaoError: Boolean = false,
    val prioridadeError: Boolean = false,
    val statusError: Boolean = false,
    val dataInicioError: Boolean = false,
    val dataFimError: Boolean = false,

    // Estados de diálogos
    val showDataInicioDialog: Boolean = false,
    val showDataFimDialog: Boolean = false
)
