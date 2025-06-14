package com.example.finalproject

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.data.sync.SyncWorker
import com.example.finalproject.ui.screens.auth.LoginScreen
import com.example.finalproject.ui.theme.FinalProjectTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        lifecycleScope.launch {
            AuthService.refreshSession()
        }
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

            var isSessionChecked by remember { mutableStateOf(false) }
            var isAuthenticated by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                AuthService.refreshSession()
                isAuthenticated = AuthService.isAuthenticated()
                isSessionChecked = true
            }


            FinalProjectTheme(dynamicColor = false) {
                AppNavigation(
                    startDestination = if (isAuthenticated) {
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
