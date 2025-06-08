package com.example.finalproject.pages

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import com.example.finalproject.R
import com.example.finalproject.data.PreferencesManager
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.data.service.StorageService
import com.example.finalproject.data.service.SupabaseProvider
import com.example.finalproject.data.service.UserService
import com.example.finalproject.ui.theme.*
import com.example.finalproject.utils.updateAppLanguage
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL


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

    // Estados para gerenciamento de imagem
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var profileImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

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

// Pré-carregar strings localizadas
    val profileTitle = stringResource(id = R.string.profile_title)
    val changeLanguage = stringResource(id = R.string.change_language)
    val portuguese = stringResource(id = R.string.portuguese)
    val english = stringResource(id = R.string.english)
    val back = stringResource(id = R.string.back)
    val profileUpdateSuccess = stringResource(id = R.string.profile_update_success)
    val profileUpdateError = stringResource(id = R.string.profile_update_error)
    val passwordUpdateSuccess = stringResource(id = R.string.password_update_success)
    val passwordUpdateError = stringResource(id = R.string.password_update_error)
    val uploadImageError = stringResource(id = R.string.upload_image_error)
    val profileImageUpdateSuccess = stringResource(id = R.string.profile_image_update_success)
    val profileImageUpdateError = stringResource(id = R.string.profile_image_update_error)



    LaunchedEffect(Unit) {
        val savedLanguage = PreferencesManager.getLanguage(context)
        updateAppLanguage(context, savedLanguage)
        println("Carregando idioma: $savedLanguage")

        selectedLanguage = if (savedLanguage == "pt") portuguese else english
    }

    // Função para lidar com a seleção de imagem e iniciar o upload
    fun handleImageSelection(uri: Uri) {
        coroutineScope.launch {
            isUploadingImage = true
            errorMessage = null

            try {
                // Carregar a imagem localmente antes do upload para exibição imediata
                val inputStream = context.contentResolver.openInputStream(uri)
                profileImageBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                val imageUrl = StorageService.uploadProfileImage(context, uri)

                if (imageUrl != null) {
                    // Atualizar a URL da imagem no banco de dados
                    val success = UserService.updateUserData(
                        fotografia = imageUrl
                    )

                    if (success) {
                        profileImageUrl = imageUrl
                        successMessage = "Foto de perfil atualizada com sucesso!"
                    } else {
                        errorMessage = "Não foi possível atualizar a foto no perfil"
                    }
                } else {
                    errorMessage = "Erro ao fazer upload da imagem"
                }
            } catch (e: Exception) {
                errorMessage = "Erro ao processar a imagem: ${e.message}"
            } finally {
                isUploadingImage = false
            }
        }
    }

    // Launcher para seleção de imagem da galeria
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Iniciar upload quando uma imagem for selecionada
            handleImageSelection(it)
        }
    }

    // Função para carregar imagem de URL
    suspend fun loadImageFromUrl(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                println("Tentando carregar imagem da URL: $url")

                // Usar HttpURLConnection para maior controle e logs detalhados
                val connection = URL(url).openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 15000 // 15 segundos de timeout
                connection.readTimeout = 15000
                connection.doInput = true

                // Adicionar token de autenticação ao cabeçalho da requisição
                // Este é o ponto chave para resolver o erro 400
                val session = SupabaseProvider.client.auth.currentSessionOrNull()
                if (session != null) {
                    val token = session.accessToken
                    connection.setRequestProperty("Authorization", "Bearer $token")
                    println("Token de autenticação adicionado ao cabeçalho")
                } else {
                    println("Aviso: Sessão nula, não foi possível adicionar token de autenticação")
                }

                connection.connect()

                val responseCode = connection.responseCode
                println("Resposta HTTP: $responseCode")

                if (responseCode == 200) {
                    val inputStream = connection.inputStream
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()

                    if (bitmap != null) {
                        println("Bitmap carregado com sucesso. Tamanho: ${bitmap.width}x${bitmap.height}")
                        bitmap
                    } else {
                        println("Falha ao decodificar o bitmap (null)")
                        null
                    }
                } else {
                    println("Falha ao carregar imagem. Código de resposta: $responseCode")

                    // Log adicional para depuração em caso de falha
                    try {
                        val errorStream = connection.errorStream
                        val errorMessage = errorStream?.bufferedReader()?.use { it.readText() }
                        println("Mensagem de erro: $errorMessage")
                        errorStream?.close()
                    } catch (e: Exception) {
                        println("Não foi possível ler o erro: ${e.message}")
                    }

                    null
                }
            } catch (e: Exception) {
                println("Exceção ao carregar imagem: ${e.javaClass.simpleName} - ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    // Função para salvar o perfil sem alterar o email
    fun saveProfileWithoutEmailChange() {
        coroutineScope.launch {
            isSaving = true
            errorMessage = null
            successMessage = null

            try {
                val success = UserService.updateUserData(
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
                    val updateInDbSuccess = UserService.updateUserData(
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

    // Carregar dados do usuário quando a tela for aberta
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            AuthService.refreshSession()
            val user = UserService.getCurrentUserData()
            if (user != null) {
                name = user.nome
                username = user.username
                // Buscar o email do serviço de autenticação em vez do objeto User
                email = AuthService.getCurrentUserEmail() ?: ""
                originalEmail = email // Armazena o email original para comparação

                // Carregar URL da imagem de perfil
                if (user.fotografia.isNullOrEmpty()) {
                    // Se não tiver uma imagem no banco de dados, tenta obter do Storage
                    profileImageUrl = StorageService.getProfileImageUrl()
                    println("Carregando imagem do storage: $profileImageUrl")
                } else {
                    profileImageUrl = user.fotografia
                    println("Carregando imagem do banco de dados: $profileImageUrl")
                }

                // Carregar o bitmap da imagem, independente da fonte
                if (profileImageUrl != null) {
                    try {
                        profileImageBitmap = loadImageFromUrl(profileImageUrl!!)
                        println("Imagem carregada com sucesso")
                    } catch (e: Exception) {
                        println("Erro ao carregar imagem: ${e.message}")
                        // Tentar carregar diretamente do Supabase como fallback
                        val userId = AuthService.getCurrentUserId()
                        if (userId != null) {
                            try {
                                val newUrl = StorageService.getProfileImageUrl(userId)
                                if (newUrl != null) {
                                    profileImageUrl = newUrl
                                    profileImageBitmap = loadImageFromUrl(newUrl)
                                    println("Imagem carregada pelo fallback")
                                }
                            } catch (e2: Exception) {
                                println("Fallback também falhou: ${e2.message}")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            errorMessage = "Erro ao carregar dados: ${e.message}"
            println("Erro no LaunchedEffect: ${e.message}")
            e.printStackTrace()
        } finally {
            isLoading = false
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
                                text = { Text(portuguese) },
                                onClick = {
                                    selectedLanguage = portuguese
                                    PreferencesManager.saveLanguage(context, "pt")
                                    updateAppLanguage(context, "pt")
                                    (context as? android.app.Activity)?.recreate() // Recria a atividade
                                    isLanguageMenuExpanded = false
                                },
                                leadingIcon = {
                                    if (selectedLanguage == portuguese) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = primaryLight
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
                                    (context as? android.app.Activity)?.recreate() // Recria a atividade
                                    isLanguageMenuExpanded = false
                                },
                                leadingIcon = {
                                    if (selectedLanguage == english) {
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
                modifier = Modifier.size(120.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    primaryLight,
                                    secondaryLight
                                )
                            )
                        )
                        .border(2.dp, Color.White, CircleShape)
                        .clickable(enabled = !isUploadingImage) {
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploadingImage) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    } else if (profileImageBitmap != null) {
                        Image(
                            bitmap = profileImageBitmap!!.asImageBitmap(),
                            contentDescription = stringResource(id = R.string.update_profile_image),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = name.split(" ").take(2).joinToString("") { it.take(1) }.uppercase(),
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
                        .background(surfaceVariantLight)
                        .clickable(enabled = !isUploadingImage) {
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = stringResource(id = R.string.update_profile_image),
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
                label = stringResource(id = R.string.name_label),
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileTextField(
                value = username,
                onValueChange = { username = it },
                label = stringResource(id = R.string.username_label),
                leadingIcon = Icons.Default.AccountCircle
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileTextField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(id = R.string.email_label),
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

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
                    Text(stringResource(id = R.string.save_changes))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão de logout
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        if (AuthService.logout()) {
                            onLogout()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = primaryLight
                )
            ) {
                Text(stringResource(id = R.string.logout_button))
            }
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
