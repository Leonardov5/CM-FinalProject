package com.example.finalproject.ui.components.projects

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finalproject.ui.components.datetime.DateTimePickerDialog
import com.example.finalproject.ui.components.datetime.DateTimePickerField
import com.example.finalproject.ui.components.datetime.DateTimePickerViewModel
import com.example.finalproject.ui.components.dropdown.DropdownMenuBox

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTaskDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onAddTask: (
        nome: String,
        descricao: String,
        prioridade: String,
        status: String,
        dataInicio: String?,
        dataFim: String?
    ) -> Unit,
    viewModel: AddTaskViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    dateTimePickerViewModel: DateTimePickerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    if (!show) return

    // Coletar o estado da UI do ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Mostrar DateTimePicker para data e hora de início
    if (uiState.showDataInicioDialog) {
        DateTimePickerDialog(
            onDismissRequest = { viewModel.hideDataInicioDialog() },
            onDateTimeSelected = {
                viewModel.updateDataInicio(it)
                viewModel.hideDataInicioDialog()
            },
            initialDateTime = uiState.dataInicio
        )
    }

    // DateTimePicker para data e hora de fim
    if (uiState.showDataFimDialog) {
        DateTimePickerDialog(
            onDismissRequest = { viewModel.hideDataFimDialog() },
            onDateTimeSelected = {
                viewModel.updateDataFim(it)
                viewModel.hideDataFimDialog()
            },
            initialDateTime = uiState.dataFim
        )
    }

    AlertDialog(
        onDismissRequest = {
            viewModel.resetForm()
            onDismiss()
        },
        title = { Text("Adicionar Tarefa") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.nome,
                    onValueChange = { viewModel.updateNome(it) },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.nomeError,
                    supportingText = {
                        if (uiState.nomeError) {
                            Text(
                                text = "O nome é obrigatório",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                //Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.descricao,
                    onValueChange = { viewModel.updateDescricao(it) },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.descricaoError,
                    supportingText = {
                        if (uiState.descricaoError) {
                            Text(
                                text = "A descrição é obrigatória",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                //Spacer(modifier = Modifier.height(16.dp))

                DropdownMenuBox(
                    label = "Prioridade",
                    options = viewModel.prioridades,
                    selectedOption = uiState.prioridade,
                    onOptionSelected = { viewModel.updatePrioridade(it) },
                    isError = uiState.prioridadeError,
                    errorMessage = "A prioridade é obrigatória"
                )

                Spacer(modifier = Modifier.height(16.dp))

                DropdownMenuBox(
                    label = "Status",
                    options = viewModel.statusList,
                    selectedOption = uiState.status,
                    onOptionSelected = { viewModel.updateStatus(it) },
                    isError = uiState.statusError,
                    errorMessage = "O status é obrigatório"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Seletor de data e hora de início
                DateTimePickerField(
                    label = "Data e Hora de Início",
                    selectedDateTime = uiState.dataInicio,
                    formattedDateTime = dateTimePickerViewModel.formatDateTime(uiState.dataInicio),
                    onDateTimePickerClick = { viewModel.showDataInicioDialog() },
                    isError = uiState.dataInicioError,
                    errorMessage = "A data de início é obrigatória"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Seletor de data e hora de fim
                DateTimePickerField(
                    label = "Data e Hora de Fim",
                    selectedDateTime = uiState.dataFim,
                    formattedDateTime = dateTimePickerViewModel.formatDateTime(uiState.dataFim),
                    onDateTimePickerClick = { viewModel.showDataFimDialog() },
                    isError = uiState.dataFimError,
                    errorMessage = "A data de fim é obrigatória"
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (viewModel.validateAndSubmitTask(onAddTask)) {
                    onDismiss()
                }
            }) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                viewModel.resetForm()
                onDismiss()
            }) {
                Text("Cancelar")
            }
        }
    )
}
