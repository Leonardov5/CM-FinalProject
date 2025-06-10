package com.example.finalproject.ui.components.projects

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    ) -> Unit
) {
    if (!show) return

    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var prioridade by remember { mutableStateOf("media") }
    var status by remember { mutableStateOf("pendente") }
    var dataInicio by remember { mutableStateOf("") }
    var dataFim by remember { mutableStateOf("") }

    val prioridades = listOf("baixa", "media", "alta", "critica")
    val statusList = listOf("pendente", "em_andamento", "concluida", "cancelada")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Tarefa") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                DropdownMenuBox(
                    label = "Prioridade",
                    options = prioridades,
                    selectedOption = prioridade,
                    onOptionSelected = { prioridade = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                DropdownMenuBox(
                    label = "Status",
                    options = statusList,
                    selectedOption = status,
                    onOptionSelected = { status = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dataInicio,
                    onValueChange = { dataInicio = it },
                    label = { Text("Data Início (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dataFim,
                    onValueChange = { dataFim = it },
                    label = { Text("Data Fim (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onAddTask(
                    nome,
                    descricao,
                    prioridade,
                    status,
                    dataInicio.takeIf { it.isNotBlank() },
                    dataFim.takeIf { it.isNotBlank() }
                )
            }) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DropdownMenuBox(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
