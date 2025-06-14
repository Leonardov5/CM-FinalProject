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
    // Estados para os campos do formulário
    var data by mutableStateOf<LocalDateTime?>(LocalDateTime.now())
    var local by mutableStateOf("")
    var contribuicao by mutableStateOf("0.0")
    var tempoDispensado by mutableStateOf("")

    // Estado de carregamento e erros
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Estados de validação
    var dataError by mutableStateOf(false)
    var localError by mutableStateOf(false)
    var contribuicaoError by mutableStateOf(false)
    var tempoDispensadoError by mutableStateOf(false)

    // Variável para armazenar o máximo de contribuição possível
    var maxContribuicaoPossivel by mutableStateOf(100.0)
        private set

    private val trabalhoRepository = TrabalhoRepository()
    private val tarefaRepository = TarefaRepository()

    // Método para validar o formulário
    fun validateForm(
        contributionMaxError: String,
        contributionMaxError2: String
    ): Boolean {
        // Resetar erros
        dataError = false
        localError = false
        contribuicaoError = false
        tempoDispensadoError = false

        var isValid = true

        // Validar data
        if (data == null) {
            dataError = true
            isValid = false
        }

        // Validar local
        if (local.isBlank()) {
            localError = true
            isValid = false
        }

        // Validar contribuição
        try {
            val contribuicaoValue = contribuicao.toDoubleOrNull()
            if (contribuicaoValue == null || contribuicaoValue < 0) {
                contribuicaoError = true
                isValid = false
            } else if (contribuicaoValue > maxContribuicaoPossivel) {
                // Verifica se a contribuição excede o máximo permitido para completar a tarefa
                contribuicaoError = true
                errorMessage = "$contributionMaxError $maxContribuicaoPossivel% $contributionMaxError2"
                isValid = false
            }
        } catch (e: Exception) {
            contribuicaoError = true
            isValid = false
        }

        // Validar tempo dispensado
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

    // Metodo para salvar o registro de trabalho
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
                // Formatar a data para string ISO
                val dataFormatada = data?.let {
                    trabalhoRepository.formatarDataHora(it)
                } ?: LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

                // Registrar o trabalho no Supabase
                val trabalho = trabalhoRepository.registrarTrabalho(
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
                    errorMessage = "Falha ao registrar o trabalho"
                    onError("Failure to register work")
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = e.message
                onError(e.message ?: "Error registering work")
            }
        }
    }

    // Metodo para carregar a taxa de conclusão atual da tarefa e calcular o máximo de contribuição possível
    fun carregarTaxaConclusaoAtual(tarefaId: String) {
        viewModelScope.launch {
            try {
                val tarefa = tarefaRepository.getTarefaById(tarefaId)
                tarefa?.let {
                    // Verificar se a tarefa já está 100% concluída
                    if (tarefa.taxaConclusao >= 100) {
                        contribuicao = "0.0" // Não permite mais contribuição
                    } else {
                        // Calcular o máximo de contribuição possível (100% - taxa atual)
                        val maxContribuicao = 100.0 - tarefa.taxaConclusao
                        // Definir uma contribuição padrão de 10%, ou o máximo disponível se for menor que 10%
                        val valorPadrao = minOf(10.0, maxContribuicao)
                        contribuicao = valorPadrao.toString()
                    }

                    // Atualiza o valor máximo de contribuição possível
                    maxContribuicaoPossivel = 100.0 - tarefa.taxaConclusao

                    // Log para depuração
                    println("DEBUG - Taxa conclusão atual da tarefa: ${tarefa.taxaConclusao}%")
                    println("DEBUG - Contribuição máxima possível: ${100.0 - (tarefa.taxaConclusao)}%")
                }
            } catch (e: Exception) {
                println("DEBUG - Erro ao carregar taxa de conclusão atual: ${e.message}")
            }
        }
    }

    // Método para resetar o formulário
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