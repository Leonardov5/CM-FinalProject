package com.example.finalproject.ui.components.tasks

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.ui.components.datetime.DateTimePickerDialog
import com.example.finalproject.ui.components.datetime.DateTimePickerField
import com.example.finalproject.ui.components.datetime.DateTimePickerViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LogWorkDialog(
    show: Boolean,
    tarefaId: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: LogWorkViewModel = viewModel(),
    dateTimePickerViewModel: DateTimePickerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    if (!show) return

    // Limpar os campos ao iniciar o diálogo
    LaunchedEffect(show) {
        if (show) {
            viewModel.resetForm()
        }
    }

    var showDateTimePicker by remember { mutableStateOf(false) }
    val formattedDateTime = dateTimePickerViewModel.formatDateTime(viewModel.data)

    Dialog(onDismissRequest = {
        viewModel.resetForm()
        onDismiss()
    }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Registar Trabalho",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de data e hora
                DateTimePickerField(
                    label = "Data",
                    selectedDateTime = viewModel.data,
                    formattedDateTime = formattedDateTime,
                    onDateTimePickerClick = { showDateTimePicker = true },
                    isError = viewModel.dataError,
                    errorMessage = "Data é obrigatória"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Campo de local
                OutlinedTextField(
                    value = viewModel.local,
                    onValueChange = { viewModel.local = it },
                    label = { Text("Local") },
                    isError = viewModel.localError,
                    supportingText = if (viewModel.localError) {
                        { Text("Local é obrigatório") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Campo de taxa de conclusão
                OutlinedTextField(
                    value = viewModel.taxaConclusao,
                    onValueChange = {
                        // Aceitar apenas números e pontos
                        if (it.isEmpty() || it.matches(Regex("^\\d*(\\.\\d*)?$"))) {
                            viewModel.taxaConclusao = it
                        }
                    },
                    label = { Text("Taxa de Conclusão (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = viewModel.taxaConclusaoError,
                    supportingText = if (viewModel.taxaConclusaoError) {
                        { Text("Valor deve estar entre 0 e 100") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Campo de tempo dispensado
                OutlinedTextField(
                    value = viewModel.tempoDispensado,
                    onValueChange = {
                        // Aceitar apenas números inteiros
                        if (it.isEmpty() || it.matches(Regex("^\\d+$"))) {
                            viewModel.tempoDispensado = it
                        }
                    },
                    label = { Text("Tempo Dispensado (minutos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = viewModel.tempoDispensadoError,
                    supportingText = if (viewModel.tempoDispensadoError) {
                        { Text("Tempo deve ser maior que 0") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mensagem de erro
                viewModel.errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Botões
                Button(
                    onClick = {
                        viewModel.saveTrabalho(
                            tarefaId = tarefaId,
                            onSuccess = {
                                viewModel.resetForm()
                                onSuccess()
                                onDismiss()
                            },
                            onError = { /* Erro já é exibido via viewModel.errorMessage */ }
                        )
                    },
                    enabled = !viewModel.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Text("Registrar")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botão de cancelar com estilo diferente
                Button(
                    onClick = {
                        viewModel.resetForm()
                        onDismiss()
                    },
                    enabled = !viewModel.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cancelar")
                }
            }
        }
    }

    if (showDateTimePicker) {
        DateTimePickerDialog(
            onDismissRequest = { showDateTimePicker = false },
            onDateTimeSelected = { selectedDateTime ->
                viewModel.data = selectedDateTime
                showDateTimePicker = false
            },
            initialDateTime = viewModel.data,
            viewModel = dateTimePickerViewModel
        )
    }
}
