package com.example.finalproject.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.R
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

    var errorMessageRes by mutableStateOf<Int?>(null)
        private set

    var successMessageRes by mutableStateOf<Int?>(null)
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

    fun hideEmailPasswordDialog() {
        showEmailPasswordDialog = false
        passwordForEmailChange = ""
        email = originalEmail
    }

    fun loadProfileData(context: Context) {
        viewModelScope.launch {
            isLoading = true
            try {
                val db = AppDatabase.getInstance(context)
                val userDao = db.userDao()
                val userId = AuthService.getCurrentUserId()

                if (userId != null) {
                    val localUser = userDao.getUserById(userId)
                    if (localUser != null) {
                        name = localUser.nome
                        username = localUser.username
                        email = localUser.email
                        originalEmail = localUser.email
                        profileImageUrl = localUser.fotografia
                    }

                    isOnline = isOnline()

                    if (isOnline) {
                        UserSyncManager.syncUserDataWithConflictResolution(context)

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
                    errorMessageRes = R.string.invalid_credentials
                }
            } catch (e: Exception) {
                errorMessageRes = R.string.profile_load_error
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun saveProfileWithoutEmailChange(context: Context) {
        viewModelScope.launch {
            isSaving = true
            errorMessageRes = null
            successMessageRes = null

            try {
                if (isOnline) {
                    val success = UserService.updateUserData(
                        username = username,
                        nome = name
                    )

                    if (success) {
                        successMessageRes = R.string.profile_update_success
                    } else {
                        errorMessageRes = R.string.profile_update_server_error
                    }
                } else {
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
                        successMessageRes = R.string.profile_update_local_success
                    } else {
                        errorMessageRes = R.string.invalid_credentials
                    }
                }
            } catch (e: Exception) {
                errorMessageRes = R.string.profile_update_error
            } finally {
                isSaving = false
            }
        }
    }

    fun saveProfileChanges(context: Context) {
        if (email != originalEmail) {
            showEmailPasswordDialog = true
        } else {
            saveProfileWithoutEmailChange(context)
        }
    }

    fun saveProfileWithEmailChange(context: Context, password: String) {
        viewModelScope.launch {
            isSaving = true
            errorMessageRes = null
            successMessageRes = null

            try {
                val emailUpdateSuccess = AuthService.updateEmail(email, password)

                if (emailUpdateSuccess) {
                    val updateInDbSuccess = UserService.updateUserData(
                        username = username,
                        nome = name
                    )

                    if (updateInDbSuccess) {
                        successMessageRes = R.string.profile_update_success
                        originalEmail = email
                    } else {
                        errorMessageRes = R.string.profile_update_server_error
                    }
                } else {
                    errorMessageRes = R.string.profile_email_update_error
                    email = originalEmail
                }
            } catch (e: Exception) {
                errorMessageRes = R.string.profile_email_update_error
                email = originalEmail
            } finally {
                isSaving = false
                passwordForEmailChange = ""
                showEmailPasswordDialog = false
            }
        }
    }

    fun updatePassword(context: Context) {
        viewModelScope.launch {
            if (currentPassword.isBlank()) {
                errorMessageRes = R.string.password_required_error
                return@launch
            }

            if (newPassword.isBlank()) {
                errorMessageRes = R.string.password_required_error
                return@launch
            }

            if (newPassword != confirmPassword) {
                errorMessageRes = R.string.passwords_do_not_match_error
                return@launch
            }

            if (newPassword.length < 6) {
                errorMessageRes = R.string.password_too_short_error
                return@launch
            }

            errorMessageRes = null
            successMessageRes = null

            isSaving = true

            try {
                val success = AuthService.updatePassword(currentPassword, newPassword)

                if (success) {
                    successMessageRes = R.string.password_update_success
                    currentPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                    isPasswordChangeVisible = false
                } else {
                    errorMessageRes = R.string.password_update_server_error
                }
            } catch (e: Exception) {
                errorMessageRes = R.string.password_update_server_error
            } finally {
                isSaving = false
            }
        }
    }

    fun uploadProfileImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            isUploadingImage = true
            errorMessageRes = null

            try {
                val userId = AuthService.getCurrentUserId() ?: throw Exception(context.getString(R.string.invalid_credentials))
                val fileName = "$userId.jpg"

                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception(context.getString(R.string.loading_image_error))

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
                    successMessageRes = R.string.profile_image_update_success
                } else {
                    errorMessageRes = R.string.profile_image_update_error
                }
            } catch (e: Exception) {
                errorMessageRes = R.string.upload_image_error
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
                    successMessageRes = R.string.logout_success
                    onLogoutSuccess()
                } else {
                    errorMessageRes = R.string.logout_error
                }
            } catch (e: Exception) {
                errorMessageRes = R.string.logout_error
            }
        }
    }

    fun clearMessages() {
        errorMessageRes = null
        successMessageRes = null
    }
}