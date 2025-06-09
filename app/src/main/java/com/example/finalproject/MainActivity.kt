package com.example.finalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.finalproject.ui.theme.FinalProjectTheme
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.finalproject.data.sync.SyncWorker
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.ui.screens.auth.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fun scheduleSyncOnNetworkAvailable() {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this).enqueue(syncRequest)
        }

        enableEdgeToEdge()
        setContent {
            FinalProjectTheme(dynamicColor = false) {
                // Usar o Navigation Component
                AppNavigation(
                    startDestination = if (AuthService.isAuthenticated()) {
                        Screen.TaskManagement.route
                    } else {
                        Screen.Login.route
                    }
                )
            }
            scheduleSyncOnNetworkAvailable()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FinalProjectTheme {
        LoginScreen(
            onLoginSuccess = {},
            onNavigateToRegister = {}
        )
    }
}


