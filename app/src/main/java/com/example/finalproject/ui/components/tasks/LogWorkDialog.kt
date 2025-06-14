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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.R
import com.example.finalproject.ui.components.datetime.DateTimePickerDialog
import com.example.finalproject.ui.components.datetime.DateTimePickerField
import com.example.finalproject.ui.components.datetime.DateTimePickerViewModel

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
    // Carregar a taxa de conclusão atual da tarefa quando o diálogo é aberto
    LaunchedEffect(show) {
        if (show) {
            viewModel.carregarTaxaConclusaoAtual(tarefaId)
        }
    }

    if (!show) return

    // Limpar os campos ao iniciar o diálogo
    LaunchedEffect(show) {
        if (show) {
            viewModel.resetForm()
        }
    }

    var showDateTimePicker by remember { mutableStateOf(false) }
    val formattedDateTime = dateTimePickerViewModel.formatDateTime(viewModel.data)
    val contributionMaxError = stringResource(id = R.string.contribution_max_error)
    val contributionMaxError2 = stringResource(id = R.string.contribution_max_error2)

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
                    text = stringResource(id = R.string.LogWork),
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de data e hora
                DateTimePickerField(
                    label = stringResource(id = R.string.date),
                    selectedDateTime = viewModel.data,
                    formattedDateTime = formattedDateTime,
                    onDateTimePickerClick = { showDateTimePicker = true },
                    isError = viewModel.dataError,
                    errorMessage = stringResource(id = R.string.date_mandatory)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Campo de local
                OutlinedTextField(
                    value = viewModel.local,
                    onValueChange = { viewModel.local = it },
                    label = { Text(stringResource(id = R.string.location)) },
                    isError = viewModel.localError,
                    supportingText = if (viewModel.localError) {
                        { Text(stringResource(id = R.string.location_mandatory)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Campo de contribuição
                OutlinedTextField(
                    value = viewModel.contribuicao,
                    onValueChange = {
                        // Aceitar apenas números e pontos
                        if (it.isEmpty() || it.matches(Regex("^\\d*(\\.\\d*)?$"))) {
                            viewModel.contribuicao = it
                        }
                    },
                    label = { Text(stringResource(id = R.string.contribution) + " (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = viewModel.contribuicaoError,
                    supportingText = if (viewModel.contribuicaoError) {
                        { Text(stringResource(id = R.string.contribution_error)) }
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
                    label = { Text(stringResource(id = R.string.time_spent)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = viewModel.tempoDispensadoError,
                    supportingText = if (viewModel.tempoDispensadoError) {
                        { Text(stringResource(id = R.string.time_spent_error)) }
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
                            contributionMaxError = contributionMaxError,
                            contributionMaxError2 = contributionMaxError2,
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
                    Text(stringResource(id = R.string.resgiter_work))
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
                    Text(stringResource(id = R.string.cancel))
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
