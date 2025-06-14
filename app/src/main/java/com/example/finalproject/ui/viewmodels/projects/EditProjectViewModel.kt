package com.example.finalproject.ui.viewmodels.projects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.model.Projeto
import com.example.finalproject.data.repository.ProjetoRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

class EditProjectViewModel(
    private val projetoRepository: ProjetoRepository = ProjetoRepository()
) : ViewModel() {

    var nome by mutableStateOf("")
        private set

    var descricao by mutableStateOf("")
        private set

    var status by mutableStateOf("ativo")
        private set

    var taxaConclusao by mutableStateOf(0f)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var hasError by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val _events = MutableSharedFlow<EditProjectEvent>()
    val events: SharedFlow<EditProjectEvent> = _events

    fun initWithProject(projeto: Projeto?) {
        projeto?.let {
            nome = it.nome
            descricao = it.descricao ?: ""
            status = it.status
            taxaConclusao = it.taxaConclusao.toFloat()
        }
    }

    fun updateNome(value: String) {
        nome = value
        clearError()
    }

    fun updateDescricao(value: String) {
        descricao = value
        clearError()
    }

    fun updateStatus(value: String) {
        status = value
        clearError()
    }

    fun updateTaxaConclusao(value: Float) {
        taxaConclusao = value
        clearError()
    }

    private fun validate(): Boolean {
        if (nome.isBlank()) {
            setError("O nome do projeto não pode estar vazio")
            return false
        }

        if (taxaConclusao < 0 || taxaConclusao > 100) {
            setError("A taxa de conclusão deve estar entre 0 e 100")
            return false
        }

        return true
    }

    fun saveProject(projetoId: String?) {
        if (!validate()) return

        if (projetoId == null) {
            setError("ID do projeto não fornecido. Não é possível editar.")
            return
        }

        isLoading = true
        hasError = false

        viewModelScope.launch {
            try {
                val success = projetoRepository.atualizarProjeto(
                    UUID.fromString(projetoId),
                    nome,
                    descricao.takeIf { it.isNotBlank() },
                    status,
                    taxaConclusao
                )

                if (success) {
                    _events.emit(EditProjectEvent.Success(projetoId))
                } else {
                    setError("Não foi possível atualizar o projeto")
                }
            } catch (e: Exception) {
                setError("Erro ao atualizar projeto: ${e.message}")
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    private fun setError(message: String) {
        hasError = true
        errorMessage = message
    }

    private fun clearError() {
        hasError = false
        errorMessage = null
    }

    sealed class EditProjectEvent {
        data class Success(val projetoId: String) : EditProjectEvent()
    }
}
