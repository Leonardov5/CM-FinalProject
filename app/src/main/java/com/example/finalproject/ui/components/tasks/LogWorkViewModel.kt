package com.example.finalproject.ui.components.tasks

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.repository.TarefaRepository
import com.example.finalproject.data.repository.TrabalhoRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class LogWorkViewModel : ViewModel() {
    var data by mutableStateOf<LocalDateTime?>(LocalDateTime.now())
    var local by mutableStateOf("")
    var contribuicao by mutableStateOf("0.0")
    var tempoDispensado by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    var dataError by mutableStateOf(false)
    var localError by mutableStateOf(false)
    var contribuicaoError by mutableStateOf(false)
    var tempoDispensadoError by mutableStateOf(false)

    var maxContribuicaoPossivel by mutableStateOf(100.0)
        private set

    private val trabalhoRepository = TrabalhoRepository()
    private val tarefaRepository = TarefaRepository()

    fun validateForm(
        contributionMaxError: String,
        contributionMaxError2: String
    ): Boolean {
        dataError = false
        localError = false
        contribuicaoError = false
        tempoDispensadoError = false

        var isValid = true

        if (data == null) {
            dataError = true
            isValid = false
        }

        if (local.isBlank()) {
            localError = true
            isValid = false
        }

        try {
            val contribuicaoValue = contribuicao.toDoubleOrNull()
            if (contribuicaoValue == null || contribuicaoValue < 0) {
                contribuicaoError = true
                isValid = false
            } else if (contribuicaoValue > maxContribuicaoPossivel) {
                contribuicaoError = true
                errorMessage = "$contributionMaxError $maxContribuicaoPossivel% $contributionMaxError2"
                isValid = false
            }
        } catch (e: Exception) {
            contribuicaoError = true
            isValid = false
        }

        try {
            val tempo = tempoDispensado.toIntOrNull()
            if (tempo == null || tempo <= 0) {
                tempoDispensadoError = true
                isValid = false
            }
        } catch (e: Exception) {
            tempoDispensadoError = true
            isValid = false
        }

        return isValid
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveTrabalho(
        tarefaId: String,
        contributionMaxError: String,
        contributionMaxError2: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!validateForm(contributionMaxError, contributionMaxError2)) {
            onError("Please fill in all fields correctly.")
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val dataFormatada = data?.let {
                    trabalhoRepository.formatarDataHoraParaISO(it)
                } ?: LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

                val trabalho = trabalhoRepository.registarTrabalho(
                    tarefaId = tarefaId,
                    data = dataFormatada,
                    local = local.takeIf { it.isNotBlank() },
                    contribuicao = contribuicao.toDoubleOrNull() ?: 0.0,
                    tempoDispensado = tempoDispensado.toIntOrNull() ?: 0
                )

                if (trabalho != null) {
                    isLoading = false
                    onSuccess()
                } else {
                    isLoading = false
                    errorMessage = "Falha ao registar o trabalho"
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = e.message
            }
        }
    }

    fun carregarTaxaConclusaoAtual(tarefaId: String) {
        viewModelScope.launch {
            try {
                val tarefa = tarefaRepository.obterTarefaPorId(tarefaId)
                tarefa?.let {
                    if (tarefa.taxaConclusao >= 100) {
                        contribuicao = "0.0"
                    } else {
                        val maxContribuicao = 100.0 - tarefa.taxaConclusao
                        val valorPadrao = minOf(10.0, maxContribuicao)
                        contribuicao = valorPadrao.toString()
                    }

                    maxContribuicaoPossivel = 100.0 - tarefa.taxaConclusao
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun resetForm() {
        data = LocalDateTime.now()
        local = ""
        contribuicao = "0.0"
        tempoDispensado = ""
        dataError = false
        localError = false
        contribuicaoError = false
        tempoDispensadoError = false
        errorMessage = null
    }
}