package com.example.finalproject.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.R
import com.example.finalproject.Screen
import com.example.finalproject.data.PreferencesManager
import com.example.finalproject.ui.viewmodels.ProfileViewModel
import com.example.finalproject.utils.updateAppLanguage

@Composable
fun BottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    var isLanguageLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val savedLanguage = PreferencesManager.getLanguage(context)
        updateAppLanguage(context, savedLanguage)
        isLanguageLoaded = true
    }

    if (!isLanguageLoaded) return // Wait until language is loaded

    val items = listOf(
        NavigationItem(
            route = Screen.TaskManagement.route,
            icon = Icons.Default.Task,
            label = stringResource(R.string.bottom_nav_tasks)
        ),
        NavigationItem(
            route = Screen.Projects.route,
            icon = Icons.Default.Folder,
            label = stringResource(R.string.bottom_nav_projects)
        ),
        NavigationItem(
            route = Screen.Updates.route,
            icon = Icons.Default.Notifications,
            label = stringResource(R.string.bottom_nav_updates)
        )
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute.startsWith(item.route),
                onClick = {
                    if (currentRoute != item.route) {
                        onNavigate(item.route)
                    }
                }
            )
        }
    }
}