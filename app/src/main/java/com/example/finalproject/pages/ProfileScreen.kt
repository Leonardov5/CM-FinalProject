package com.example.finalproject.pages

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.data.service.UserService
import com.example.finalproject.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackPressed: () -> Unit,
    onLogout: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Estados para campos do perfil
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var originalEmail by remember { mutableStateOf("") } // Para rastrear se o email foi alterado
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Estado para controlar o diálogo de verificação de senha para alteração de email
    var showEmailPasswordDialog by remember { mutableStateOf(false) }
    var passwordForEmailChange by remember { mutableStateOf("") }
    var showPasswordForEmailChange by remember { mutableStateOf(false) }

    // Estados para controle de UI
    var isPasswordChangeVisible by remember { mutableStateOf(false) }
    var isLanguageMenuExpanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("Português") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    // Carregar dados do usuário quando a tela for aberta
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            AuthService.refreshSession()
            val user = com.example.finalproject.data.service.UserService.getCurrentUserData()
            if (user != null) {
                name = user.nome
                username = user.username
                // Buscar o email do serviço de autenticação em vez do objeto User
                email = AuthService.getCurrentUserEmail() ?: ""
                originalEmail = email // Armazena o email original para comparação
            }
        } catch (e: Exception) {
            errorMessage = "Erro ao carregar dados: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Função para salvar o perfil sem alterar o email
    fun saveProfileWithoutEmailChange() {
        coroutineScope.launch {
            isSaving = true
            errorMessage = null
            successMessage = null

            try {
                val success = com.example.finalproject.data.service.UserService.updateUserData(
                    username = username,
                    nome = name
                )

                if (success) {
                    successMessage = "Perfil atualizado com sucesso!"
                } else {
                    errorMessage = "Não foi possível atualizar o perfil"
                }
            } catch (e: Exception) {
                errorMessage = "Erro ao atualizar: ${e.message}"
            } finally {
                isSaving = false
            }
        }
    }


    // Função para salvar as alterações do perfil
    fun saveProfileChanges() {
        // Verificar se o email foi alterado
        if (email != originalEmail) {
            // Se o email foi alterado, mostrar diálogo para confirmar senha
            showEmailPasswordDialog = true
            return
        }

        // Se o email não foi alterado, apenas salvar as outras alterações
        saveProfileWithoutEmailChange()
    }

    // Função para salvar o perfil com alteração de email (requer verificação de senha)
    fun saveProfileWithEmailChange(password: String) {
        coroutineScope.launch {
            isSaving = true
            errorMessage = null
            successMessage = null

            try {
                // Primeiro, verificar se a senha está correta e atualizar o email na autenticação
                val emailUpdateSuccess = AuthService.updateEmail(email, password)

                if (emailUpdateSuccess) {
                    // Depois de atualizar o email na autenticação, atualizamos no banco de dados
                    val updateInDbSuccess = com.example.finalproject.data.service.UserService.updateUserData(
                        username = username,
                        nome = name,
                    )

                    if (updateInDbSuccess) {
                        successMessage = "Perfil atualizado com sucesso! Verifique seu email para confirmar a alteração."
                        originalEmail = email // Atualizar o email original para evitar repetir o diálogo
                    } else {
                        errorMessage = "Email atualizado na autenticação, mas houve um problema ao atualizar o perfil no banco de dados."
                    }
                } else {
                    // Senha incorreta ou problema ao atualizar o email
                    errorMessage = "Senha incorreta ou problema ao atualizar o email."
                    email = originalEmail // Restaurar o email original
                }
            } catch (e: Exception) {
                errorMessage = "Erro ao atualizar: ${e.message}"
                email = originalEmail // Restaurar o email original em caso de erro
            } finally {
                isSaving = false
                passwordForEmailChange = "" // Limpar a senha por segurança
                showEmailPasswordDialog = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Meu Perfil",
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    // Botão de idioma com dropdown
                    Box {
                        IconButton(onClick = { isLanguageMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = "Alterar idioma",
                                tint = primaryLight
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
                                text = { Text("Português") },
                                onClick = {
                                    selectedLanguage = "Português"
                                    isLanguageMenuExpanded = false
                                },
                                leadingIcon = {
                                    if (selectedLanguage == "Português") {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = primaryLight
                                        )
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("English") },
                                onClick = {
                                    selectedLanguage = "English"
                                    isLanguageMenuExpanded = false
                                },
                                leadingIcon = {
                                    if (selectedLanguage == "English") {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = primaryLight
                                        )
                                    }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundLight,
                    titleContentColor = onBackgroundLight
                )
            )
        },
        containerColor = backgroundLight
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
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                primaryLight,
                                secondaryLight
                            )
                        )
                    )
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.split(" ").take(2).joinToString("") { it.take(1) }.uppercase(),
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )

                // Botão para alterar a foto
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .alpha(0f)
                        .clickable {
                            // Implementar lógica para alterar a foto
                        }
                )

                // Ícone de câmera para indicar que pode alterar a foto
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(surfaceVariantLight)
                        .clickable {
                            // Implementar lógica para alterar a foto
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Alterar foto",
                        tint = primaryLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Campos de perfil
            ProfileTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nome Completo",
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileTextField(
                value = username,
                onValueChange = { username = it },
                label = "Nome de Usuário",
                leadingIcon = Icons.Default.AccountCircle
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Seção de alteração de senha
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = surfaceVariantLight.copy(alpha = 0.5f),
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isPasswordChangeVisible = !isPasswordChangeVisible
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
                                tint = primaryLight
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "Alterar Senha",
                                fontWeight = FontWeight.Medium,
                                color = onBackgroundLight
                            )
                        }

                        val rotation by animateFloatAsState(
                            targetValue = if (isPasswordChangeVisible) 180f else 0f,
                            label = "rotation"
                        )

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.rotate(rotation),
                            tint = onBackgroundLight
                        )
                    }

                    if (isPasswordChangeVisible) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo de senha atual
                        ProfileTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = "Senha Atual",
                            leadingIcon = Icons.Default.Lock,
                            isPassword = true,
                            showPassword = showCurrentPassword,
                            onTogglePasswordVisibility = { showCurrentPassword = !showCurrentPassword }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Campo de nova senha
                        ProfileTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = "Nova Senha",
                            leadingIcon = Icons.Default.Lock,
                            isPassword = true,
                            showPassword = showNewPassword,
                            onTogglePasswordVisibility = { showNewPassword = !showNewPassword }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Campo de confirmação de senha
                        ProfileTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = "Confirmar Nova Senha",
                            leadingIcon = Icons.Default.Lock,
                            isPassword = true,
                            showPassword = showConfirmPassword,
                            onTogglePasswordVisibility = { showConfirmPassword = !showConfirmPassword }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botão de atualizar senha
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    // Validar as senhas
                                    if (currentPassword.isBlank()) {
                                        errorMessage = "A senha atual é obrigatória"
                                        return@launch
                                    }

                                    if (newPassword.isBlank()) {
                                        errorMessage = "A nova senha é obrigatória"
                                        return@launch
                                    }

                                    if (newPassword != confirmPassword) {
                                        errorMessage = "As senhas não coincidem"
                                        return@launch
                                    }

                                    if (newPassword.length < 6) {
                                        errorMessage = "A senha deve ter pelo menos 6 caracteres"
                                        return@launch
                                    }

                                    // Limpar mensagens anteriores
                                    errorMessage = null
                                    successMessage = null

                                    // Mostrar indicador de loading
                                    isSaving = true

                                    try {
                                        // Chamar o serviço para atualizar a senha
                                        val success = AuthService.updatePassword(currentPassword, newPassword)

                                        if (success) {
                                            successMessage = "Senha atualizada com sucesso!"
                                            // Limpar os campos de senha
                                            currentPassword = ""
                                            newPassword = ""
                                            confirmPassword = ""
                                            // Fechar a seção de alteração de senha
                                            isPasswordChangeVisible = false
                                        } else {
                                            errorMessage = "Não foi possível atualizar a senha. Verifique se a senha atual está correta."
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Erro ao atualizar a senha: ${e.message}"
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryLight,
                                contentColor = onPrimaryLight
                            )
                        ) {
                            Text("Atualizar Senha")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão de salvar alterações
            Button(
                onClick = {
                    saveProfileChanges()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryLight,
                    contentColor = onPrimaryLight
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = onPrimaryLight,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Salvar Alterações")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mensagens de feedback
            AnimatedVisibility(visible = successMessage != null) {
                Text(
                    text = successMessage ?: "",
                    color = Color.Green,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão de logout
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        if (AuthService.logout()) {
                            // Voltar para a tela de login
                            onLogout()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = primaryLight
                )
            ) {
                Text("Sair da Conta")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Diálogo para confirmar senha ao alterar email
        if (showEmailPasswordDialog) {
            AlertDialog(
                onDismissRequest = {
                    showEmailPasswordDialog = false
                    email = originalEmail // Restaurar o email original se o usuário cancelar
                    passwordForEmailChange = "" // Limpar a senha por segurança
                },
                title = { Text("Confirmar senha") },
                text = {
                    Column {
                        Text("Para alterar seu email, por favor confirme sua senha atual:")
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = passwordForEmailChange,
                            onValueChange = { passwordForEmailChange = it },
                            label = { Text("Senha atual") },
                            visualTransformation = if (showPasswordForEmailChange)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            trailingIcon = {
                                IconButton(onClick = { showPasswordForEmailChange = !showPasswordForEmailChange }) {
                                    Icon(
                                        imageVector = if (showPasswordForEmailChange)
                                            Icons.Rounded.VisibilityOff
                                        else
                                            Icons.Rounded.Visibility,
                                        contentDescription = if (showPasswordForEmailChange)
                                            "Ocultar senha"
                                        else
                                            "Mostrar senha"
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
                            if (passwordForEmailChange.isNotEmpty()) {
                                saveProfileWithEmailChange(passwordForEmailChange)
                            }
                        },
                        enabled = passwordForEmailChange.isNotEmpty()
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showEmailPasswordDialog = false
                            email = originalEmail // Restaurar o email original
                            passwordForEmailChange = "" // Limpar a senha por segurança
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePasswordVisibility: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = primaryLight
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
                            "Ocultar senha"
                        else
                            "Mostrar senha",
                        tint = outlineLight
                    )
                }
            }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = primaryLight,
            unfocusedIndicatorColor = outlineLight,
            cursorColor = primaryLight,
            focusedTextColor = onBackgroundLight,
            unfocusedTextColor = onBackgroundLight,
            focusedContainerColor = surfaceVariantLight.copy(alpha = 0.3f),
            unfocusedContainerColor = surfaceVariantLight.copy(alpha = 0.3f)
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
