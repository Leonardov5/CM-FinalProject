package com.example.finalproject.ui.screens

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.R
import com.example.finalproject.data.PreferencesManager
import com.example.finalproject.data.service.SupabaseProvider
import com.example.finalproject.utils.updateAppLanguage
import com.example.finalproject.ui.viewmodels.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

suspend fun isOnline(): Boolean {
    val isOline = SupabaseProvider.isDatabaseConnected()
    println("isOnline: $isOline")
    return isOline
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackPressed: () -> Unit,
    onLogout: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val savedLanguage = PreferencesManager.getLanguage(context)
    var selectedLanguage by remember { mutableStateOf("") }
    val viewModel: ProfileViewModel = viewModel()

    // Carregar dados do perfil
    LaunchedEffect(Unit) {
        viewModel.loadProfileData(context)
        // A imagem agora é carregada diretamente no ViewModel
    }

    // Estados para controle de UI do menu de idioma
    var isLanguageMenuExpanded by remember { mutableStateOf(false) }

    // Carregue as strings no início do Composable
    val profileTitle = stringResource(id = R.string.profile_title)
    val changeLanguage = stringResource(id = R.string.change_language)
    val portuguese = stringResource(id = R.string.portuguese)
    val english = stringResource(id = R.string.english)
    val back = stringResource(id = R.string.back)
    val updatePasswordSection = stringResource(id = R.string.update_password_section)
    val currentPasswordLabel = stringResource(id = R.string.current_password_label)
    val newPasswordLabel = stringResource(id = R.string.new_password_label)
    val confirmNewPasswordLabel = stringResource(id = R.string.confirm_new_password_label)
    val updatePasswordButton = stringResource(id = R.string.update_password_button)
    val confirmPasswordDialogTitle = stringResource(id = R.string.confirm_password_dialog_title)
    val confirmPasswordDialogMessage = stringResource(id = R.string.confirm_password_dialog_message)

    LaunchedEffect(savedLanguage, portuguese, english) {
        selectedLanguage = if (savedLanguage == "en") english else portuguese
    }


    // Launcher para seleção de imagem da galeria
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadProfileImage(context, it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = profileTitle,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = back,
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { isLanguageMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = changeLanguage,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = isLanguageMenuExpanded,
                            onDismissRequest = { isLanguageMenuExpanded = false },
                            properties = PopupProperties(
                                focusable = true,
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true
                            )
                        ) {
                            DropdownMenuItem(
                                text = { Text(portuguese) },
                                onClick = {
                                    selectedLanguage = portuguese
                                    PreferencesManager.saveLanguage(context, "pt")
                                    updateAppLanguage(context, "pt")
                                    (context as? Activity)?.recreate()
                                    isLanguageMenuExpanded = false
                                },
                                leadingIcon = {
                                    if (selectedLanguage == portuguese) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(english) },
                                onClick = {
                                    selectedLanguage = english
                                    PreferencesManager.saveLanguage(context, "en")
                                    updateAppLanguage(context, "en")
                                    (context as? Activity)?.recreate()
                                    isLanguageMenuExpanded = false
                                },
                                leadingIcon = {
                                    if (selectedLanguage == english) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                windowInsets = WindowInsets(0)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Foto do perfil
            Box(
                modifier = Modifier.size(120.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .border(2.dp, Color.White, CircleShape)
                        .clickable(enabled = !viewModel.isUploadingImage) {
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.isUploadingImage) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    } else if (viewModel.profileImageBitmap != null) {
                        Image(
                            bitmap = viewModel.profileImageBitmap!!.asImageBitmap(),
                            contentDescription = stringResource(id = R.string.update_profile_image),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = viewModel.name.split(" ").take(2).joinToString("") { it.take(1) }.uppercase(),
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(enabled = !viewModel.isUploadingImage) {
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = stringResource(id = R.string.update_profile_image),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Campos de perfil
            ProfileTextField(
                value = viewModel.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = stringResource(id = R.string.name_label),
                leadingIcon = Icons.Default.Person,
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileTextField(
                value = viewModel.username,
                onValueChange = { viewModel.onUsernameChange(it) },
                label = stringResource(id = R.string.username_label),
                leadingIcon = Icons.Default.AccountCircle
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileTextField(
                value = viewModel.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = stringResource(id = R.string.email_label),
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                enabled = viewModel.isOnline && !viewModel.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled = !viewModel.isLoading && viewModel.isOnline
                            ) {
                                viewModel.togglePasswordChangeVisible()
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (!viewModel.isLoading && viewModel.isOnline) MaterialTheme.colorScheme.primary else Color.Gray
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = updatePasswordSection,
                                fontWeight = FontWeight.Medium,
                                color = if (!viewModel.isLoading && viewModel.isOnline) MaterialTheme.colorScheme.onBackground else Color.Gray
                            )
                        }

                        val rotation by animateFloatAsState(
                            targetValue = if (viewModel.isPasswordChangeVisible) 180f else 0f,
                            label = "rotation"
                        )

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.rotate(rotation),
                            tint = if (!viewModel.isLoading && viewModel.isOnline) MaterialTheme.colorScheme.onBackground else Color.Gray
                        )
                    }

                    AnimatedVisibility(visible = viewModel.isPasswordChangeVisible) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Campo de senha atual
                            ProfileTextField(
                                value = viewModel.currentPassword,
                                onValueChange = { viewModel.onCurrentPasswordChange(it) },
                                label = currentPasswordLabel,
                                leadingIcon = Icons.Default.Lock,
                                isPassword = true,
                                showPassword = viewModel.showCurrentPassword,
                                onTogglePasswordVisibility = { viewModel.toggleShowCurrentPassword() }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Campo de nova senha
                            ProfileTextField(
                                value = viewModel.newPassword,
                                onValueChange = { viewModel.onNewPasswordChange(it) },
                                label = newPasswordLabel,
                                leadingIcon = Icons.Default.Lock,
                                isPassword = true,
                                showPassword = viewModel.showNewPassword,
                                onTogglePasswordVisibility = { viewModel.toggleShowNewPassword() }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Campo de confirmação de senha
                            ProfileTextField(
                                value = viewModel.confirmPassword,
                                onValueChange = { viewModel.onConfirmPasswordChange(it) },
                                label = confirmNewPasswordLabel,
                                leadingIcon = Icons.Default.Lock,
                                isPassword = true,
                                showPassword = viewModel.showConfirmPassword,
                                onTogglePasswordVisibility = { viewModel.toggleShowConfirmPassword() }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Botão de atualizar senha
                            Button(
                                onClick = { viewModel.updatePassword() },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(text = updatePasswordButton)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão de salvar alterações
            Button(
                onClick = { viewModel.saveProfileChanges(context) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isLoading && !viewModel.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (viewModel.isSaving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(stringResource(id = R.string.save_changes))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão de logout
            OutlinedButton(
                onClick = { viewModel.logout(onLogout) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(stringResource(id = R.string.logout_button))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Mensagens de erro ou sucesso
    if (viewModel.errorMessage != null) {
        LaunchedEffect(viewModel.errorMessage) {
            Toast.makeText(context, viewModel.errorMessage, Toast.LENGTH_LONG).show()
            delay(3000)
            viewModel.clearMessages()
        }
    }

    if (viewModel.successMessage != null) {
        LaunchedEffect(viewModel.successMessage) {
            Toast.makeText(context, viewModel.successMessage, Toast.LENGTH_LONG).show()
            delay(3000)
            viewModel.clearMessages()
        }
    }

    // Diálogo para confirmar senha ao alterar email
    if (viewModel.showEmailPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.hideEmailPasswordDialog()
            },
            title = { Text(confirmPasswordDialogTitle) },
            text = {
                Column {
                    Text(confirmPasswordDialogMessage)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = viewModel.passwordForEmailChange,
                        onValueChange = { viewModel.onPasswordForEmailChangeChange(it) },
                        label = { Text(currentPasswordLabel) },
                        visualTransformation = if (viewModel.showPasswordForEmailChange)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleShowPasswordForEmailChange() }) {
                                Icon(
                                    imageVector = if (viewModel.showPasswordForEmailChange)
                                        Icons.Rounded.VisibilityOff
                                    else
                                        Icons.Rounded.Visibility,
                                    contentDescription = if (viewModel.showPasswordForEmailChange)
                                        stringResource(id = R.string.hide_password)
                                    else
                                        stringResource(id = R.string.show_password)
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.passwordForEmailChange.isNotEmpty()) {
                            viewModel.saveProfileWithEmailChange(viewModel.passwordForEmailChange)
                        }
                    },
                    enabled = viewModel.passwordForEmailChange.isNotEmpty()
                ) {
                    Text(stringResource(id = R.string.confirm_password_dialog_confirm))                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.hideEmailPasswordDialog()
                    }
                ) {
                    Text(stringResource(id = R.string.confirm_password_dialog_cancel))                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePasswordVisibility: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (showPassword)
                            Icons.Rounded.VisibilityOff
                        else
                            Icons.Rounded.Visibility,
                        contentDescription = if (showPassword)
                            stringResource(id = R.string.hide_password)
                        else
                            stringResource(id = R.string.show_password),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        singleLine = true,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        visualTransformation = if (isPassword && !showPassword)
            PasswordVisualTransformation()
        else
            VisualTransformation.None
    )
}
