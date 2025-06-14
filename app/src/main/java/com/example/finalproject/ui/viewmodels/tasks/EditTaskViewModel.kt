package com.example.finalproject.ui.viewmodels.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.model.Tarefa
import com.example.finalproject.data.repository.TarefaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditTaskViewModel(
    private val repository: TarefaRepository = TarefaRepository()
) : ViewModel() {

    val prioridades = listOf("baixa", "media", "alta", "critica")
    val statusList = listOf("pendente", "em_andamento", "concluida", "cancelada")

    private val _uiState = MutableStateFlow(EditTaskUIState())
    val uiState: StateFlow<EditTaskUIState> = _uiState.asStateFlow()

    // Inicializar com os dados da tarefa
    fun initWithTask(tarefa: Tarefa) {
        _uiState.value = EditTaskUIState(
            nome = tarefa.nome,
            descricao = tarefa.descricao ?: "",
            prioridade = tarefa.prioridade ?: "media",
            status = tarefa.status ?: "pendente",
            dataInicio = tarefa.dataInicio,
            dataFim = tarefa.dataFim,
            taxaConclusao = tarefa.taxaConclusao
        )
    }

    // Funções para atualizar campos
    fun updateNome(nome: String) {
        _uiState.value = _uiState.value.copy(nome = nome, nomeError = false)
    }

    fun updateDescricao(descricao: String) {
        _uiState.value = _uiState.value.copy(descricao = descricao, descricaoError = false)
    }

    fun updatePrioridade(prioridade: String) {
        _uiState.value = _uiState.value.copy(prioridade = prioridade, prioridadeError = false)
    }

    fun updateStatus(status: String) {
        _uiState.value = _uiState.value.copy(status = status, statusError = false)
    }

    fun updateDataInicio(dataInicio: String?) {
        _uiState.value = _uiState.value.copy(dataInicio = dataInicio, dataInicioError = false)
    }

    fun updateDataFim(dataFim: String?) {
        _uiState.value = _uiState.value.copy(dataFim = dataFim, dataFimError = false)
    }

    fun updateTaxaConclusao(taxaConclusao: Double) {
        _uiState.value = _uiState.value.copy(taxaConclusao = taxaConclusao, taxaConclusaoError = false)
    }

    // Controle dos diálogos de data
    fun showDataInicioDialog() {
        _uiState.value = _uiState.value.copy(showDataInicioDialog = true)
    }

    fun hideDataInicioDialog() {
        _uiState.value = _uiState.value.copy(showDataInicioDialog = false)
    }

    fun showDataFimDialog() {
        _uiState.value = _uiState.value.copy(showDataFimDialog = true)
    }

    fun hideDataFimDialog() {
        _uiState.value = _uiState.value.copy(showDataFimDialog = false)
    }

    // Resetar formulário
    fun resetForm() {
        _uiState.value = EditTaskUIState()
    }

    // Validar e enviar
    fun validateAndSubmitTask(
        id: String,
        onSave: (String, String, String, String, String, String?, String?, Double) -> Unit
    ): Boolean {
        val currentState = _uiState.value

        // Validar campos obrigatórios
        var isValid = true

        if (currentState.nome.isBlank()) {
            _uiState.value = currentState.copy(nomeError = true)
            isValid = false
        }

        if (currentState.descricao.isBlank()) {
            _uiState.value = currentState.copy(descricaoError = true)
            isValid = false
        }

        if (currentState.prioridade.isBlank()) {
            _uiState.value = currentState.copy(prioridadeError = true)
            isValid = false
        }

        if (currentState.status.isBlank()) {
            _uiState.value = currentState.copy(statusError = true)
            isValid = false
        }

        if (currentState.taxaConclusao < 0 || currentState.taxaConclusao > 100) {
            _uiState.value = currentState.copy(taxaConclusaoError = true)
            isValid = false
        }

        // Se válido, salvar
        if (isValid) {
            viewModelScope.launch {
                repository.atualizarTarefa(
                    tarefaId = id,
                    nome = currentState.nome,
                    descricao = currentState.descricao,
                    prioridade = currentState.prioridade,
                    status = currentState.status,
                    dataInicio = currentState.dataInicio,
                    dataFim = currentState.dataFim,
                    taxaConclusao = currentState.taxaConclusao
                )

                // Notificar o componente pai
                onSave(
                    id,
                    currentState.nome,
                    currentState.descricao,
                    currentState.prioridade,
                    currentState.status,
                    currentState.dataInicio,
                    currentState.dataFim,
                    currentState.taxaConclusao
                )
            }
        }

        return isValid
    }

    // Classe de estado da UI
    data class EditTaskUIState(
        val nome: String = "",
        val descricao: String = "",
        val prioridade: String = "media",
        val status: String = "pendente",
        val dataInicio: String? = null,
        val dataFim: String? = null,
        val taxaConclusao: Double = 0.0,

        val nomeError: Boolean = false,
        val descricaoError: Boolean = false,
        val prioridadeError: Boolean = false,
        val statusError: Boolean = false,
        val dataInicioError: Boolean = false,
        val dataFimError: Boolean = false,
        val taxaConclusaoError: Boolean = false,

        val showDataInicioDialog: Boolean = false,
        val showDataFimDialog: Boolean = false
    )
}