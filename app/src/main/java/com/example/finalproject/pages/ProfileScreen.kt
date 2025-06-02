package com.example.finalproject.pages

import androidx.compose.animation.core.animateFloatAsState
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
import com.example.finalproject.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackPressed: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Estados para campos do perfil
    var name by remember { mutableStateOf("Leonardo Vieira") }
    var username by remember { mutableStateOf("leonardovieira") }
    var email by remember { mutableStateOf(AuthService.getCurrentUserEmail() ?: "user@example.com") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Estados para controle de UI
    var isPasswordChangeVisible by remember { mutableStateOf(false) }
    var isLanguageMenuExpanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("Português") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

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
                                imageVector = Icons.Default.LocationOn,
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
                                // Implementar lógica para atualizar a senha
                                coroutineScope.launch {
                                    // Validar senhas e enviar para o backend
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
                    // Implementar lógica para salvar as alterações de perfil
                    coroutineScope.launch {
                        // Enviar dados para o backend
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryLight,
                    contentColor = onPrimaryLight
                )
            ) {
                Text("Salvar Alterações")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão de logout
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        if (AuthService.logout()) {
                            // Voltar para a tela de login
                            onBackPressed()
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
