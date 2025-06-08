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
import com.example.finalproject.data.RepositoryProvider
import com.example.finalproject.utils.updateAppLanguage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(

    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoggingIn by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var isLanguageLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val savedLanguage = PreferencesManager.getLanguage(context)
        updateAppLanguage(context, savedLanguage)
        isLanguageLoaded = true // Marca que o idioma foi carregado
    }
    val coroutineScope = rememberCoroutineScope()

    // Pré-carregar strings localizadas
    val invalidCredentialsMessage = stringResource(id = R.string.invalid_credentials)
    val loginErrorMessage = stringResource(id = R.string.login_error)
    val fillAllFieldsMessage = stringResource(id = R.string.fill_all_fields)



    if (isLanguageLoaded) {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.welcome_message),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

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
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            isLoggingIn = true
                            coroutineScope.launch {
                                try {
                                    val loginSuccess = RepositoryProvider.userRepository.loginUser(email, password)
                                    isLoggingIn = false
                                    if (loginSuccess) {
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = invalidCredentialsMessage
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    isLoggingIn = false
                                    errorMessage = loginErrorMessage.format(e.message)
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            errorMessage = fillAllFieldsMessage
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),

                ) {
                    Text(stringResource(id = R.string.login_button))
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.fillMaxWidth(),

                ) {
                    Text(stringResource(id = R.string.create_account_button))
                }
            }
        }
    }
}