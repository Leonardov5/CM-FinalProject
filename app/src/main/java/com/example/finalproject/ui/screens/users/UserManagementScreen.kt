package com.example.finalproject.ui.screens.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finalproject.R
import com.example.finalproject.data.PreferencesManager
import com.example.finalproject.data.model.Utilizador
import com.example.finalproject.ui.components.users.EditUserDialog
import com.example.finalproject.ui.components.users.RegisterUserDialog
import com.example.finalproject.ui.viewmodels.users.UserManagementViewModel
import com.example.finalproject.utils.updateAppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    onProfileClick: () -> Unit,
    viewModel: UserManagementViewModel,
    onAddUser: () -> Unit,
) {

    val users by viewModel.users.collectAsState()
    var userToDelete by remember { mutableStateOf<Utilizador?>(null) }

    var adminPasswordInput by remember { mutableStateOf("") }
    var adminPasswordError by remember { mutableStateOf<String?>(null) }

    var invalidPassword = stringResource(id = R.string.invalid_credentials)

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val savedLanguage = PreferencesManager.getLanguage(context)
        updateAppLanguage(context, savedLanguage)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(0.7f),
                            shape = RoundedCornerShape(25.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = stringResource(id = R.string.user_management_title),
                                modifier = Modifier.padding(vertical = 12.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = stringResource(id = R.string.menu),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onProfileClick() }) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = stringResource(id = R.string.profile),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.toggleAdminPasswordDialog() }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_user))
            }
        }
    ) { paddingValues ->
        if (users.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(id = R.string.no_users_found))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(users) { user ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(user.nome, style = MaterialTheme.typography.titleMedium)
                                Text(user.username, style = MaterialTheme.typography.bodyMedium)
                                if (user.admin) {
                                    Text(stringResource(id = R.string.admin_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Row {
                                IconButton(
                                    onClick = { viewModel.togleEditDialog(user) },
                                    enabled = !user.admin
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = stringResource(id = R.string.edit_user))
                                }
                                IconButton(
                                    onClick = { userToDelete = user },
                                    enabled = !user.admin
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.delete_user))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmar eliminar utilizador
    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text(stringResource(id = R.string.delete_user_dialog_title)) },
            text = { Text(stringResource(id = R.string.delete_user_dialog_text, userToDelete?.nome ?: "")) },            confirmButton = {
                TextButton(onClick = {
                    userToDelete?.let { viewModel.deleteUser(it.id) }
                    userToDelete = null
                }) {
                    Text(stringResource(id = R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }

    // Editar utilizador
    if (viewModel.showEditDialog && viewModel.userToEdit != null) {
        EditUserDialog(
            user = viewModel.userToEdit!!,
            onDismiss = { viewModel.togleEditDialog(null) },
            onConfirm = { nome, username ->
                viewModel.editarUtilizador(viewModel.userToEdit!!.id, nome, username) { sucesso ->
                    viewModel.togleEditDialog(null)
                }
            }
        )
    }

    if (viewModel.showAdminPasswordDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleAdminPasswordDialog() },
            title = { Text(stringResource(id = R.string.admin_password_dialog_title)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = adminPasswordInput,
                        onValueChange = { adminPasswordInput = it },
                        label = { Text(stringResource(id = R.string.password_label)) },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    if (adminPasswordError != null) {
                        Text(adminPasswordError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.confirmAdminPassword(adminPasswordInput) { success ->
                        if (success) {
                            viewModel.toggleAdminPasswordDialog()
                            adminPasswordInput = ""
                            adminPasswordError = null
                            viewModel.toggleCreateDialog()
                        } else {
                            adminPasswordError = invalidPassword
                        }
                    }
                }) {
                    Text(stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleAdminPasswordDialog() }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }

    // Registar novo utilizador
    if (viewModel.showCreateDialog) {
        RegisterUserDialog(
            onDismiss = { viewModel.toggleCreateDialog() },
            onConfirm = { name, username, email, password, isAdmin ->
                viewModel.registerNewUser(name, username, email, password, isAdmin)
                viewModel.toggleCreateDialog()
            }
        )
    }


}