package com.example.finalproject.ui.components.tasks

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finalproject.data.model.User

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.example.finalproject.R


@Composable
fun AddWorkerDialog(
    users: List<User>,
    onDismiss: () -> Unit,
    onAdd: (List<String>) -> Unit
) {
    var selectedUsers by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Trabalhadores") },
        text = {
            if (users.isEmpty()) {
                Text("Nenhum membro disponível.")
            } else {
                LazyColumn {
                    items(users) { user ->
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = selectedUsers.contains(user.id),
                                onCheckedChange = { checked ->
                                    selectedUsers = if (checked)
                                        selectedUsers + user.id
                                    else
                                        selectedUsers - user.id
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(user.nome)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(selectedUsers.toList())
                },
                enabled = selectedUsers.isNotEmpty()
            ) {
                Text(stringResource(id = R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.cancel)) }
        }
    )
}