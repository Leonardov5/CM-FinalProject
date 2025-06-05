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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.finalproject.data.RepositoryProvider
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.data.service.UserService
import com.example.finalproject.ui.theme.backgroundLight
import com.example.finalproject.ui.theme.onBackgroundLight
import com.example.finalproject.ui.theme.onPrimaryLight
import com.example.finalproject.ui.theme.outlineLight
import com.example.finalproject.ui.theme.primaryLight
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
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Criar Conta",
                style = MaterialTheme.typography.headlineMedium,
                color = primaryLight,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Nome Completo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = primaryLight,
                    unfocusedIndicatorColor = outlineLight,
                    cursorColor = primaryLight,
                    focusedTextColor = onBackgroundLight
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nome de utilizador") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = primaryLight,
                    unfocusedIndicatorColor = outlineLight,
                    cursorColor = primaryLight,
                    focusedTextColor = onBackgroundLight
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = primaryLight,
                    unfocusedIndicatorColor = outlineLight,
                    cursorColor = primaryLight,
                    focusedTextColor = onBackgroundLight
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = primaryLight,
                    unfocusedIndicatorColor = outlineLight,
                    cursorColor = primaryLight,
                    focusedTextColor = onBackgroundLight
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = primaryLight,
                    unfocusedIndicatorColor = outlineLight,
                    cursorColor = primaryLight,
                    focusedTextColor = onBackgroundLight
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ->
                            errorMessage = "Por favor, preencha todos os campos"
                        password != confirmPassword ->
                            errorMessage = "As passwords não coincidem"
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
                                                email = email,
                                                nome = fullName,
                                                tipo = "normal"
                                            )

                                            if (userDataSaved) {
                                                isRegistering = false
                                                Toast.makeText(context, "Registro bem-sucedido!", Toast.LENGTH_SHORT).show()
                                                onRegisterSuccess()
                                            } else {
                                                isRegistering = false
                                                // O usuário foi criado, mas os dados não foram salvos
                                                Toast.makeText(
                                                    context,
                                                    "Registro realizado, mas houve um problema ao salvar seus dados.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                onRegisterSuccess()
                                            }
                                        } else {
                                            isRegistering = false
                                            errorMessage = "Registro realizado, mas falha ao fazer login automático"
                                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        isRegistering = false
                                        errorMessage = "Erro ao registrar: verifique se o email é válido ou já está em uso"
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    isRegistering = false
                                    errorMessage = "Erro ao registrar: ${e.message}"
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryLight,
                    contentColor = onPrimaryLight
                )
            ) {
                if (isRegistering) {
                    CircularProgressIndicator(
                        color = onPrimaryLight,
                        modifier = Modifier.height(24.dp)
                    )
                } else {
                    Text("Registrar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = primaryLight
                )
            ) {
                Text("Já tem uma conta? Entrar")
            }
        }
    }
}
