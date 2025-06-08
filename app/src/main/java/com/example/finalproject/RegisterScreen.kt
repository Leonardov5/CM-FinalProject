package com.example.finalproject

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.finalproject.data.PreferencesManager
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.data.service.UserService
import com.example.finalproject.utils.updateAppLanguage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val savedLanguage = PreferencesManager.getLanguage(context)
        updateAppLanguage(context, savedLanguage)
    }

    val coroutineScope = rememberCoroutineScope()

    // Pré-carregar strings localizadas
    val fillAllFieldsMessage = stringResource(id = R.string.fill_all_fields)
    val passwordsDoNotMatchMessage = stringResource(id = R.string.passwords_do_not_match)
    val registrationSuccessMessage = stringResource(id = R.string.registration_success)
    val registrationErrorMessage = stringResource(id = R.string.registration_error)
    val userDataSaveErrorMessage = stringResource(id = R.string.user_data_save_error)
    val autoLoginFailedMessage = stringResource(id = R.string.auto_login_failed)
    val registrationFailedMessage = stringResource(id = R.string.registration_failed)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.create_account),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text(stringResource(id = R.string.full_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),

                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(stringResource(id = R.string.username_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),

                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(id = R.string.email_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(id = R.string.password_label)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(stringResource(id = R.string.confirm_password_label)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ->
                            errorMessage = fillAllFieldsMessage
                        password != confirmPassword ->
                            errorMessage = passwordsDoNotMatchMessage
                        else -> {
                            isRegistering = true
                            coroutineScope.launch {
                                try {
                                    // Registrar o usuário no sistema de autenticação
                                    val registerSuccess = AuthService.register(email, password)

                                    if (registerSuccess) {
                                        // Após registro bem-sucedido, fazer login automaticamente
                                        val loginSuccess = AuthService.login(email, password)

                                        if (loginSuccess) {
                                            // Salvar os dados do usuário na base de dados (sem a senha)
                                            val userDataSaved = UserService.saveUserData(
                                                username = username,
                                                nome = fullName,
                                                admin = false
                                            )

                                            if (userDataSaved) {
                                                isRegistering = false
                                                Toast.makeText(context, registrationSuccessMessage, Toast.LENGTH_SHORT).show()
                                                onRegisterSuccess()
                                            } else {
                                                isRegistering = false
                                                // O usuário foi criado, mas os dados não foram salvos
                                                Toast.makeText(context, userDataSaveErrorMessage, Toast.LENGTH_LONG).show()
                                                onRegisterSuccess()
                                            }
                                        } else {
                                            isRegistering = false
                                            errorMessage = autoLoginFailedMessage
                                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        isRegistering = false
                                        errorMessage = registrationFailedMessage
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    isRegistering = false
                                    errorMessage = registrationErrorMessage.format(e.message ?: "")
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),

            ) {
                if (isRegistering) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp)
                    )
                } else {
                    Text(stringResource(id = R.string.register_button))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth(),

            ) {
                Text(stringResource(id = R.string.already_have_account))
            }
        }
    }
}