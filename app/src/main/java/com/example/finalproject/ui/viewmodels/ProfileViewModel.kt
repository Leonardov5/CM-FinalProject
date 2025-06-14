package com.example.finalproject.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.local.AppDatabase
import com.example.finalproject.data.local.LocalUser
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.data.service.SupabaseProvider
import com.example.finalproject.data.service.UserService
import com.example.finalproject.data.sync.UserSyncManager
import com.example.finalproject.ui.screens.isOnline
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileViewModel: ViewModel() {
    var name by mutableStateOf("")
        private set

    var username by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var originalEmail by mutableStateOf("")
        private set

    var currentPassword by mutableStateOf("")
        private set

    var newPassword by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    var profileImageUrl by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var isOnline by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var isUploadingImage by mutableStateOf(false)
        private set

    var showEmailPasswordDialog by mutableStateOf(false)
        private set

    var passwordForEmailChange by mutableStateOf("")
        private set

    var isPasswordChangeVisible by mutableStateOf(false)
        private set

    var showCurrentPassword by mutableStateOf(false)
        private set

    var showNewPassword by mutableStateOf(false)
        private set

    var showConfirmPassword by mutableStateOf(false)
        private set

    var showPasswordForEmailChange by mutableStateOf(false)
        private set

    var isAdmin by mutableStateOf(false)
        private set

    fun checkIfAdmin() {
        viewModelScope.launch {
            try {
                val user = UserService.getCurrentUserData()
                isAdmin = user?.admin == true
            } catch (e: Exception) {
                isAdmin = false
            }
        }
    }

    fun onNameChange(newName: String) {
        name = newName
    }

    fun onUsernameChange(newUsername: String) {
        username = newUsername
    }

    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun onCurrentPasswordChange(newPassword: String) {
        currentPassword = newPassword
    }

    fun onNewPasswordChange(newPassword: String) {
        this.newPassword = newPassword
    }

    fun onConfirmPasswordChange(newPassword: String) {
        confirmPassword = newPassword
    }

    fun onPasswordForEmailChangeChange(newPassword: String) {
        passwordForEmailChange = newPassword
    }

    fun toggleShowCurrentPassword() {
        showCurrentPassword = !showCurrentPassword
    }

    fun toggleShowNewPassword() {
        showNewPassword = !showNewPassword
    }

    fun toggleShowConfirmPassword() {
        showConfirmPassword = !showConfirmPassword
    }

    fun toggleShowPasswordForEmailChange() {
        showPasswordForEmailChange = !showPasswordForEmailChange
    }

    fun togglePasswordChangeVisible() {
        isPasswordChangeVisible = !isPasswordChangeVisible
    }

    fun showEmailPasswordDialog() {
        showEmailPasswordDialog = true
    }

    fun hideEmailPasswordDialog() {
        showEmailPasswordDialog = false
        passwordForEmailChange = ""
        email = originalEmail
    }

    // Carregar dados do perfil
    fun loadProfileData(context: Context) {
        viewModelScope.launch {
            isLoading = true
            try {
                val db = AppDatabase.getInstance(context)
                val userDao = db.userDao()
                val userId = AuthService.getCurrentUserId()

                if (userId != null) {
                    // Carregar dados do banco de dados local primeiro
                    val localUser = userDao.getUserById(userId)
                    if (localUser != null) {
                        name = localUser.nome
                        username = localUser.username
                        email = localUser.email
                        originalEmail = localUser.email
                        profileImageUrl = localUser.fotografia
                    }

                    // Verificar conectividade
                    isOnline = isOnline()

                    // Se estiver online, sincronizar dados
                    if (isOnline) {
                        UserSyncManager.syncUserDataWithConflictResolution(context)

                        // Atualizar os dados locais após a sincronização
                        val updatedUser = userDao.getUserById(userId)
                        if (updatedUser != null) {
                            name = updatedUser.nome
                            username = updatedUser.username
                            email = updatedUser.email
                            originalEmail = updatedUser.email
                            profileImageUrl = updatedUser.fotografia
                        }
                    }
                } else {
                    errorMessage = "Usuário não autenticado."
                }
            } catch (e: Exception) {
                errorMessage = "Erro ao carregar dados: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // Função para salvar o perfil sem alterar o email
    fun saveProfileWithoutEmailChange(context: Context) {
        viewModelScope.launch {
            isSaving = true
            errorMessage = null
            successMessage = null

            try {
                if (isOnline) {
                    val success = UserService.updateUserData(
                        username = username,
                        nome = name
                    )

                    if (success) {
                        successMessage = "Perfil atualizado com sucesso!"
                    } else {
                        errorMessage = "Erro ao atualizar perfil."
                    }
                } else {
                    // Salvar localmente se estiver offline
                    val db = AppDatabase.getInstance(context)
                    val userDao = db.userDao()
                    val userId = AuthService.getCurrentUserId()

                    if (userId != null) {
                        val localUser = LocalUser(
                            id = userId,
                            username = username,
                            nome = name,
                            email = email,
                            fotografia = profileImageUrl,
                            updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
                        )
                        userDao.insertOrUpdate(localUser)
                        successMessage = "Alterações salvas localmente. Serão sincronizadas quando online."
                    } else {
                        errorMessage = "Usuário não autenticado. Não foi possível salvar localmente."
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Erro ao atualizar perfil: ${e.message}"
            } finally {
                isSaving = false
            }
        }
    }

    // Função para verificar se o email foi alterado e decidir o fluxo de salvamento
    fun saveProfileChanges(context: Context) {
        if (email != originalEmail) {
            showEmailPasswordDialog = true
        } else {
            saveProfileWithoutEmailChange(context)
        }
    }

    // Função para salvar o perfil com alteração de email (requer verificação de senha)
    fun saveProfileWithEmailChange(password: String) {
        viewModelScope.launch {
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
                        nome = name
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

    // Função para atualizar a senha
    fun updatePassword() {
        viewModelScope.launch {
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
    }

    // Função para fazer upload da imagem de perfil
    fun uploadProfileImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            isUploadingImage = true
            errorMessage = null

            try {
                val userId = AuthService.getCurrentUserId() ?: throw Exception("Utilizador não autenticado")
                val fileName = "$userId.jpg"

                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Não foi possível abrir a imagem")

                val bytes = inputStream.readBytes()
                inputStream.close()

                val supabase = SupabaseProvider.client
                supabase.storage.from("perfil")
                    .upload(fileName, bytes, upsert = true)


                val imageUrl = supabase.storage.from("perfil")
                    .publicUrl(fileName)

                val success = UserService.updateUserData(fotografia = imageUrl)

                if (success) {
                    profileImageUrl = imageUrl
                    successMessage = "Foto de perfil atualizada com sucesso!"
                } else {
                    errorMessage = "Não foi possível atualizar a foto no perfil"
                }
            } catch (e: Exception) {
                errorMessage = "Erro ao processar a imagem: ${e.message}"
                e.printStackTrace()
            } finally {
                isUploadingImage = false
            }
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val success = AuthService.logout()
                if (success) {
                    onLogoutSuccess()
                } else {
                    errorMessage = "Erro ao fazer logout"
                }
            } catch (e: Exception) {
                errorMessage = "Erro ao fazer logout: ${e.message}"
            }
        }
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}