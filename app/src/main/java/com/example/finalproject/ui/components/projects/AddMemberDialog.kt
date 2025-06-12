import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finalproject.R
import com.example.finalproject.data.model.User

@Composable
fun AddMemberDialog(
    isAdmin: Boolean,
    users: List<User>,
    onDismiss: () -> Unit,
    onAdd: (String, Boolean) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var isManager by remember { mutableStateOf(false) }

    val filteredUsers = users.filter { it.nome.contains(search, ignoreCase = true) && it.nome.isNotBlank() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.add_member_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = search,
                    onValueChange = {
                        search = it
                        selectedUser = null
                    },
                    label = { Text(stringResource(id = R.string.search_user)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))

                // Lista de usuários filtrados
                if (filteredUsers.isNotEmpty() && search.isNotBlank()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(filteredUsers.take(5)) { user ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        search = user.nome
                                        selectedUser = user
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text(user.nome)
                                if (user.username.isNotBlank()) {
                                    Text(
                                        "@${user.username}",
                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Divider()
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                if (isAdmin) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isManager,
                            onCheckedChange = { isManager = it }
                        )
                        Text(stringResource(id = R.string.manager))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedUser?.let { onAdd(it.id, isManager) } },
                enabled = selectedUser != null
            ) { Text(stringResource(id = R.string.add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.cancel)) }
        }
    )
}