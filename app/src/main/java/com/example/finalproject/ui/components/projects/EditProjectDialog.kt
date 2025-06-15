package com.example.finalproject.ui.components.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.R
import com.example.finalproject.data.model.Projeto
import com.example.finalproject.ui.components.dropdown.DropdownMenuBox
import com.example.finalproject.ui.viewmodels.projects.EditProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProjectDialog(
    show: Boolean,
    projeto: Projeto? = null,
    onDismiss: () -> Unit,
    onSaveProject: (nome: String, descricao: String, status: String, taxaConclusao: Float) -> Unit,
    viewModel: EditProjectViewModel = viewModel()
) {
    if (!show) return

    val isEditing = projeto != null
    val titleRes = if (isEditing) R.string.edit_project else R.string.add_project

    LaunchedEffect(key1 = projeto) {
        viewModel.initWithProject(projeto)
    }

    val statusOptions = listOf(
        stringResource(id = R.string.active),
        stringResource(id = R.string.inactive),
        stringResource(id = R.string.completed),
        stringResource(id = R.string.cancelled)
    )

    val statusMapping = mapOf(
        stringResource(id = R.string.active) to "ativo",
        stringResource(id = R.string.inactive) to "inativo",
        stringResource(id = R.string.completed) to "concluido",
        stringResource(id = R.string.cancelled) to "cancelado"
    )

    val currentStatusText = statusMapping.entries.find { it.value == viewModel.status }?.key ?: viewModel.status

    val nome = viewModel.nome
    val descricao = viewModel.descricao
    val status = viewModel.status
    val taxaConclusao = viewModel.taxaConclusao
    val hasError = viewModel.hasError
    val errorMessage = viewModel.errorMessage
    val isLoading = viewModel.isLoading

    var taxaConclusaoStr by remember(taxaConclusao) { mutableStateOf(taxaConclusao.toString()) }
    var taxaConclusaoError by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.events.collect { event ->
            when (event) {
                is EditProjectViewModel.EditProjectEvent.Success -> {
                    val savedNome = viewModel.nome
                    val savedDescricao = viewModel.descricao
                    val savedStatus = viewModel.status
                    val savedTaxaConclusao = viewModel.taxaConclusao

                    onSaveProject(savedNome, savedDescricao, savedStatus, savedTaxaConclusao)
                    onDismiss()
                }
            }
        }
    }

    fun validateAndSave() {
        val taxaConclusaoValue = taxaConclusaoStr.toFloatOrNull()
        if (taxaConclusaoValue == null || taxaConclusaoValue < 0 || taxaConclusaoValue > 100) {
            taxaConclusaoError = true
            return
        }

        viewModel.updateTaxaConclusao(taxaConclusaoValue)
        viewModel.saveProject(projeto?.id?.toString())
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Título
                Text(
                    text = stringResource(id = titleRes),
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Nome
                OutlinedTextField(
                    value = nome,
                    onValueChange = { viewModel.updateNome(it) },
                    label = { Text(stringResource(R.string.project_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nome.isBlank(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Descrição
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { viewModel.updateDescricao(it) },
                    label = { Text(stringResource(R.string.description)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    singleLine = false
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status
                DropdownMenuBox(
                    label = stringResource(R.string.status),
                    options = statusOptions,
                    selectedOption = currentStatusText,
                    onOptionSelected = { selectedText ->
                        val internalValue = statusMapping[selectedText] ?: selectedText
                        viewModel.updateStatus(internalValue)
                    },
                    isError = false
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Taxa de conclusão
                OutlinedTextField(
                    value = taxaConclusaoStr,

                    onValueChange = {
                        taxaConclusaoStr = it
                        taxaConclusaoError = false
                        it.toFloatOrNull()?.let { value ->
                            if (value in 0f..100f) {
                                viewModel.updateTaxaConclusao(value)
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.completion_rate)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = taxaConclusaoError || hasError,
                    singleLine = true,
                    trailingIcon = { Text("%") }
                )

                if (taxaConclusaoError) {
                    Text(
                        text = stringResource(R.string.completion_rate_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                } else if (hasError) {
                    Text(
                        text = errorMessage ?: stringResource(R.string.unknown_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Guardar e cancelar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { validateAndSave() },
                        enabled = nome.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.save))
                        }
                    }
                }
            }
        }
    }
}
