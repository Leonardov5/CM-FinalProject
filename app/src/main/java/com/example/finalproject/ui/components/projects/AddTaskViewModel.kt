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

    private val _uiState = MutableStateFlow(AddTaskUIState())
    val uiState: StateFlow<AddTaskUIState> = _uiState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    private val isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    val prioridades = listOf("baixa", "media", "alta", "critica")
    val statusList = listOf("pendente", "em_andamento", "concluida", "cancelada")

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

        val isNomeValid = currentState.nome.isNotBlank()
        val isDescricaoValid = currentState.descricao.isNotBlank()
        val isPrioridadeValid = currentState.prioridade.isNotBlank()
        val isStatusValid = currentState.status.isNotBlank()
        val isDataInicioValid = currentState.dataInicio != null
        val isDataFimValid = currentState.dataFim != null

        _uiState.update { it.copy(
            nomeError = !isNomeValid,
            descricaoError = !isDescricaoValid,
            prioridadeError = !isPrioridadeValid,
            statusError = !isStatusValid,
            dataInicioError = !isDataInicioValid,
            dataFimError = !isDataFimValid
        ) }

        val isFormValid = isNomeValid && isDescricaoValid &&
                          isPrioridadeValid && isStatusValid &&
                          isDataInicioValid && isDataFimValid

        if (isFormValid) {
            onAddTask(
                currentState.nome,
                currentState.descricao,
                currentState.prioridade,
                currentState.status,
                currentState.dataInicio?.format(isoFormatter),
                currentState.dataFim?.format(isoFormatter)
            )

            resetForm()
        }

        return isFormValid
    }

    fun resetForm() {
        _uiState.update { AddTaskUIState() }
    }
}

data class AddTaskUIState(
    val nome: String = "",
    val descricao: String = "",
    val prioridade: String = "",
    val status: String = "",
    val dataInicio: LocalDateTime? = null,
    val dataFim: LocalDateTime? = null,

    val nomeError: Boolean = false,
    val descricaoError: Boolean = false,
    val prioridadeError: Boolean = false,
    val statusError: Boolean = false,
    val dataInicioError: Boolean = false,
    val dataFimError: Boolean = false,

    val showDataInicioDialog: Boolean = false,
    val showDataFimDialog: Boolean = false
)
