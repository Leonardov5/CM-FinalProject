package com.example.finalproject.ui.components.tasks

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.finalproject.R
import com.example.finalproject.data.model.User

@Composable
fun WorkerTaskDetailDialog(
    show: Boolean,
    worker: User?,
    onDismiss: () -> Unit,
    onRemove: (String) -> Unit
) {
    if (show && worker != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(worker.nome) },
            text = { Text(stringResource(id = R.string.remove_from_task_confirm)) },
            confirmButton = {
                Button(onClick = { onRemove(worker.id) }) {
                    Text(stringResource(id = R.string.remove))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }
}