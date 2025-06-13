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
import com.example.finalproject.data.service.AuthService
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class LogWorkViewModel : ViewModel() {
    // Estados para os campos do formulário
    var data by mutableStateOf<LocalDateTime?>(LocalDateTime.now())
    var local by mutableStateOf("")
    var taxaConclusao by mutableStateOf("0.0")
    var tempoDispensado by mutableStateOf("")

    // Estado de carregamento e erros
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Estados de validação
    var dataError by mutableStateOf(false)
    var localError by mutableStateOf(false)
    var taxaConclusaoError by mutableStateOf(false)
    var tempoDispensadoError by mutableStateOf(false)

    private val trabalhoRepository = TrabalhoRepository()
    private val tarefaRepository = TarefaRepository()

    // Método para validar o formulário
    fun validateForm(): Boolean {
        // Resetar erros
        dataError = false
        localError = false
        taxaConclusaoError = false
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

        // Validar taxa de conclusão
        try {
            val taxa = taxaConclusao.toDoubleOrNull()
            if (taxa == null || taxa < 0 || taxa > 100) {
                taxaConclusaoError = true
                isValid = false
            }
        } catch (e: Exception) {
            taxaConclusaoError = true
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
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!validateForm()) {
            onError("Por favor, preencha todos os campos corretamente.")
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
                    taxaConclusao = taxaConclusao.toDoubleOrNull() ?: 0.0,
                    tempoDispensado = tempoDispensado.toIntOrNull() ?: 0
                )

                if (trabalho != null) {
                    isLoading = false
                    onSuccess()
                } else {
                    isLoading = false
                    errorMessage = "Falha ao registrar o trabalho"
                    onError("Falha ao registrar o trabalho")
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = e.message
                onError(e.message ?: "Erro ao registrar trabalho")
            }
        }
    }

    // Método para resetar o formulário
    @RequiresApi(Build.VERSION_CODES.O)
    fun resetForm() {
        data = LocalDateTime.now()
        local = ""
        taxaConclusao = "0.0"
        tempoDispensado = ""
        dataError = false
        localError = false
        taxaConclusaoError = false
        tempoDispensadoError = false
        errorMessage = null
    }
}
