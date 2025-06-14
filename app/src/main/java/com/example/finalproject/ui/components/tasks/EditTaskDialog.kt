package com.example.finalproject.ui.components.tasks

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.R
import com.example.finalproject.data.model.Tarefa
import com.example.finalproject.ui.components.datetime.DateTimePickerDialog
import com.example.finalproject.ui.components.datetime.DateTimePickerField
import com.example.finalproject.ui.components.datetime.DateTimePickerViewModel
import com.example.finalproject.ui.components.dropdown.DropdownMenuBox
import com.example.finalproject.ui.viewmodels.tasks.EditTaskViewModel
import java.time.LocalDateTime

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
fun EditTaskDialog(
    show: Boolean,
    tarefa: Tarefa,
    onDismiss: () -> Unit,
    onSave: (
        id: String,
        nome: String,
        descricao: String,
        prioridade: String,
        status: String,
        dataInicio: String?,
        dataFim: String?,
        taxaConclusao: Double
    ) -> Unit,
    viewModel: EditTaskViewModel = viewModel(),
    dateTimePickerViewModel: DateTimePickerViewModel = viewModel()
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

    LaunchedEffect(tarefa) {
        viewModel.initWithTask(tarefa)
    }


    val uiState by viewModel.uiState.collectAsState()

    val dataInicioLocal = remember(uiState.dataInicio) {
        uiState.dataInicio?.let {
            try {
                LocalDateTime.parse(it)
            } catch (e: Exception) {
                null
            }
        }
    }
    val dataFimLocal = remember(uiState.dataFim) {
        uiState.dataFim?.let {
            try {
                LocalDateTime.parse(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    // Mostrar DateTimePicker para data e hora de início
    if (uiState.showDataInicioDialog) {
        DateTimePickerDialog(
            onDismissRequest = { viewModel.hideDataInicioDialog() },
            onDateTimeSelected = { localDateTime ->
                viewModel.updateDataInicio(localDateTime.toString())
                viewModel.hideDataInicioDialog()
            },
            initialDateTime = dataInicioLocal
        )
    }

    // DateTimePicker para data e hora de fim
    if (uiState.showDataFimDialog) {
        DateTimePickerDialog(
            onDismissRequest = { viewModel.hideDataFimDialog() },
            onDateTimeSelected = { localDateTime ->
                viewModel.updateDataFim(localDateTime.toString())
                viewModel.hideDataFimDialog()
            },
            initialDateTime = dataFimLocal
        )
    }

    AlertDialog(
        onDismissRequest = {
            viewModel.resetForm()
            onDismiss()
        },
        title = { Text(stringResource(R.string.edit_task)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.nome,
                    onValueChange = { viewModel.updateNome(it) },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.nomeError,
                    supportingText = {
                        if (uiState.nomeError) {
                            Text(
                                text = stringResource(R.string.name_required),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = uiState.descricao,
                    onValueChange = { viewModel.updateDescricao(it) },
                    label = { Text(stringResource(R.string.description)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.descricaoError,
                    supportingText = {
                        if (uiState.descricaoError) {
                            Text(
                                text = stringResource(R.string.description_required),
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
                        // Converter de volta para o valor do backend ao salvar
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

                // Seletor de data e hora de início
                DateTimePickerField(
                    label = stringResource(R.string.start_date_time),
                    selectedDateTime = dataInicioLocal,
                    formattedDateTime = dateTimePickerViewModel.formatDateTime(dataInicioLocal),
                    onDateTimePickerClick = { viewModel.showDataInicioDialog() },
                    isError = uiState.dataInicioError,
                    errorMessage = stringResource(R.string.start_date_required)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Seletor de data e hora de fim
                DateTimePickerField(
                    label = stringResource(R.string.end_date_time),
                    selectedDateTime = dataFimLocal,
                    formattedDateTime = dateTimePickerViewModel.formatDateTime(dataFimLocal),
                    onDateTimePickerClick = { viewModel.showDataFimDialog() },
                    isError = uiState.dataFimError,
                    errorMessage = stringResource(R.string.end_date_required)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo para taxa de conclusão
                OutlinedTextField(
                    value = uiState.taxaConclusao.toString(),
                    onValueChange = {
                        it.toDoubleOrNull()?.let { value ->
                            if (value in 0.0..100.0) {
                                viewModel.updateTaxaConclusao(value)
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.completion_rate)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.taxaConclusaoError,
                    supportingText = {
                        if (uiState.taxaConclusaoError) {
                            Text(
                                text = stringResource(R.string.completion_rate_error),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    trailingIcon = { Text("%") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (viewModel.validateAndSubmitTask(
                        id = tarefa.id ?: "",
                        onSave = onSave
                    )) {
                    onDismiss()
                }
            }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                viewModel.resetForm()
                onDismiss()
            }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}