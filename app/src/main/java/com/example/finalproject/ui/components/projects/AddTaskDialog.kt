package com.example.finalproject.ui.components.projects

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finalproject.R
import com.example.finalproject.ui.components.datetime.DateTimePickerDialog
import com.example.finalproject.ui.components.datetime.DateTimePickerField
import com.example.finalproject.ui.components.datetime.DateTimePickerViewModel
import com.example.finalproject.ui.components.dropdown.DropdownMenuBox

@Composable
fun prioridadeDisplay(prioridade: String): String = when (prioridade) {
    "baixa" -> stringResource(id = R.string.priority_low)
    "media" -> stringResource(id = R.string.priority_medium)
    "alta" -> stringResource(id = R.string.priority_high)
    "critica" -> stringResource(id = R.string.priority_critical)
    else -> prioridade
}

@Composable
fun statusDisplay(status: String): String = when (status) {
    "pendente" -> stringResource(id = R.string.status_pending)
    "em_andamento" -> stringResource(id = R.string.status_in_progress)
    "concluida" -> stringResource(id = R.string.status_completed)
    "cancelada" -> stringResource(id = R.string.status_cancelled)
    else -> status
}

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

    val low = stringResource(id = R.string.priority_low)
    val medium = stringResource(id = R.string.priority_medium)
    val high = stringResource(id = R.string.priority_high)
    val critical = stringResource(id = R.string.priority_critical)

    val pending = stringResource(id = R.string.status_pending)
    val inProgress = stringResource(id = R.string.status_in_progress)
    val completed = stringResource(id = R.string.status_completed)
    val cancelled = stringResource(id = R.string.status_cancelled)

    val uiState by viewModel.uiState.collectAsState()

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
        title = { Text(stringResource(id = R.string.add_task)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.nome,
                    onValueChange = { viewModel.updateNome(it) },
                    label = { Text(stringResource(id = R.string.name)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.nomeError,
                    supportingText = {
                        if (uiState.nomeError) {
                            Text(
                                text = stringResource(id = R.string.name_required),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = uiState.descricao,
                    onValueChange = { viewModel.updateDescricao(it) },
                    label = { Text(stringResource(id = R.string.description)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.descricaoError,
                    supportingText = {
                        if (uiState.descricaoError) {
                            Text(
                                text = stringResource(id = R.string.description_required),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                DropdownMenuBox(
                    label = stringResource(R.string.priority),
                    options = viewModel.prioridades.map { prioridadeDisplay(it) },
                    selectedOption = prioridadeDisplay(uiState.prioridade),
                    onOptionSelected = { selected ->
                        val backendValue = when (selected) {
                            low -> "baixa"
                            medium -> "media"
                            high -> "alta"
                            critical -> "critica"
                            else -> selected
                        }
                        viewModel.updatePrioridade(backendValue)
                    },
                    isError = uiState.prioridadeError,
                    errorMessage = stringResource(R.string.priority_required)
                )

                Spacer(modifier = Modifier.height(16.dp))

                DropdownMenuBox(
                    label = stringResource(R.string.status),
                    options = viewModel.statusList.map { statusDisplay(it) },
                    selectedOption = statusDisplay(uiState.status),
                    onOptionSelected = { selected ->
                        val backendValue = when (selected) {
                            pending -> "pendente"
                            inProgress -> "em_andamento"
                            completed -> "concluida"
                            cancelled -> "cancelada"
                            else -> selected
                        }
                        viewModel.updateStatus(backendValue)
                    },
                    isError = uiState.statusError,
                    errorMessage = stringResource(R.string.status_required)
                )

                Spacer(modifier = Modifier.height(16.dp))

                DateTimePickerField(
                    label = stringResource(id = R.string.start_date_time),
                    selectedDateTime = uiState.dataInicio,
                    formattedDateTime = dateTimePickerViewModel.formatDateTime(uiState.dataInicio),
                    onDateTimePickerClick = { viewModel.showDataInicioDialog() },
                    isError = uiState.dataInicioError,
                    errorMessage = stringResource(id = R.string.start_date_required)
                )

                Spacer(modifier = Modifier.height(16.dp))

                DateTimePickerField(
                    label = stringResource(id = R.string.end_date_time),
                    selectedDateTime = uiState.dataFim,
                    formattedDateTime = dateTimePickerViewModel.formatDateTime(uiState.dataFim),
                    onDateTimePickerClick = { viewModel.showDataFimDialog() },
                    isError = uiState.dataFimError,
                    errorMessage = stringResource(id = R.string.end_date_required)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (viewModel.validateAndSubmitTask(onAddTask)) {
                    onDismiss()
                }
            }) {
                Text(stringResource(id = R.string.add))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                viewModel.resetForm()
                onDismiss()
            }) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}
