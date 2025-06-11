import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finalproject.data.model.User
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

@Composable
fun AddMemberDialog(
    isAdmin: Boolean,
    users: List<User>,
    onDismiss: () -> Unit,
    onAdd: (String, Boolean) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var isManager by remember { mutableStateOf(false) }

    val filteredUsers = users.filter { it.nome.contains(search, ignoreCase = true) && it.nome.isNotBlank() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar membro ao projeto") },
        text = {
            Column {
                OutlinedTextField(
                    value = search,
                    onValueChange = {
                        search = it
                        expanded = it.isNotBlank() && filteredUsers.isNotEmpty()
                        selectedUser = null
                    },
                    label = { Text("Pesquisar utilizador") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    filteredUsers.take(5).forEach { user ->
                        DropdownMenuItem(
                            text = { Text(user.nome) },
                            onClick = {
                                search = user.nome
                                selectedUser = user
                                expanded = false
                            }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                if (isAdmin) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isManager,
                            onCheckedChange = { isManager = it }
                        )
                        Text("Gestor")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedUser?.let { onAdd(it.id, isManager) } },
                enabled = selectedUser != null
            ) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}