package com.example.finalproject.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.R
import com.example.finalproject.data.PreferencesManager
import com.example.finalproject.data.model.Notificacao
import com.example.finalproject.ui.viewmodels.UpdatesViewModel
import com.example.finalproject.utils.updateAppLanguage
import kotlin.math.round

@Composable
private fun getTitleFromNotificacao(notificacao: Notificacao): String {
    return when (notificacao.mensagem) {
        "USER_ADDED_TO_TASK" -> stringResource(R.string.notification_user_added_to_task_title)
        "USER_ADDED_TO_PROJECT" -> stringResource(R.string.notification_user_added_to_project_title)
        "PROJECT_STATUS_CHANGED_TO_ACTIVE" -> stringResource(R.string.notification_project_status_active_title)
        "PROJECT_STATUS_CHANGED_TO_INACTIVE" -> stringResource(R.string.notification_project_status_inactive_title)
        "PROJECT_STATUS_CHANGED_TO_COMPLETED" -> stringResource(R.string.notification_project_status_completed_title)
        "PROJECT_STATUS_CHANGED_TO_CANCELED" -> stringResource(R.string.notification_project_status_canceled_title)
        "TASK_STATUS_CHANGED_TO_PENDING" -> stringResource(R.string.notification_task_status_pending_title)
        "TASK_STATUS_CHANGED_TO_IN_PROGRESS" -> stringResource(R.string.notification_task_status_in_progress_title)
        "TASK_STATUS_CHANGED_TO_COMPLETED" -> stringResource(R.string.notification_task_status_completed_title)
        "TASK_STATUS_CHANGED_TO_CANCELED" -> stringResource(R.string.notification_task_status_canceled_title)
        else -> stringResource(R.string.notification_default_title)
    }
}

@Composable
private fun getMessageFromNotificacao(notificacao: Notificacao): String {
    return when (notificacao.mensagem) {
        "USER_ADDED_TO_TASK" -> stringResource(R.string.notification_user_added_to_task_message)
        "USER_ADDED_TO_PROJECT" -> stringResource(R.string.notification_user_added_to_project_message)
        "PROJECT_STATUS_CHANGED_TO_ACTIVE" -> stringResource(R.string.notification_project_status_active_message)
        "PROJECT_STATUS_CHANGED_TO_INACTIVE" -> stringResource(R.string.notification_project_status_inactive_message)
        "PROJECT_STATUS_CHANGED_TO_COMPLETED" -> stringResource(R.string.notification_project_status_completed_message)
        "PROJECT_STATUS_CHANGED_TO_CANCELED" -> stringResource(R.string.notification_project_status_canceled_message)
        "TASK_STATUS_CHANGED_TO_PENDING" -> stringResource(R.string.notification_task_status_pending_message)
        "TASK_STATUS_CHANGED_TO_IN_PROGRESS" -> stringResource(R.string.notification_task_status_in_progress_message)
        "TASK_STATUS_CHANGED_TO_COMPLETED" -> stringResource(R.string.notification_task_status_completed_message)
        "TASK_STATUS_CHANGED_TO_CANCELED" -> stringResource(R.string.notification_task_status_canceled_message)
        else -> notificacao.mensagem
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesScreen(
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    viewModel: UpdatesViewModel = viewModel()
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val savedLanguage = PreferencesManager.getLanguage(context)
        updateAppLanguage(context, savedLanguage)
        viewModel.loadUpdates()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.filterNotificacoes(it)
                            },
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.search),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            searchQuery = ""
                                            viewModel.resetFilter()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(25.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .padding(0.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.background
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0)
            )
        },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            // Notifications list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.notificacoes) { notificacao ->
                    if (notificacao.id != null) {
                        UpdateCard(
                            notificacao = notificacao,
                            title = getTitleFromNotificacao(notificacao),
                            message = getMessageFromNotificacao(notificacao),
                            getFormattedDate = viewModel::formatarData,
                            onClick = { id -> viewModel.markAsRead(id) },
                            onDelete = {
                                viewModel.deleteNotification(it)
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
@Composable
fun UpdateCard(
    notificacao: Notificacao,
    title: String,
    message: String,
    getFormattedDate: (Notificacao) -> String,
    onClick: (String) -> Unit = {},
    onDelete: (String) -> Unit = {}
) {
    val id = notificacao.id ?: return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (!notificacao.vista) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 2.dp,
        onClick = { onClick(id) }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Close notification button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = if (!notificacao.vista)
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onDelete(id) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.delete),
                        tint = if (!notificacao.vista)
                            MaterialTheme.colorScheme.onSecondary
                        else
                            MaterialTheme.colorScheme.inverseSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (!notificacao.vista) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (!notificacao.vista) MaterialTheme.colorScheme.onSecondary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!notificacao.vista) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = message,
                        fontSize = 14.sp,
                        color = if (!notificacao.vista) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    )

                    notificacao.objeto?.let { objeto ->
                        if (objeto.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (!notificacao.vista)
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                            ) {
                                Text(
                                    text = objeto,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = if (!notificacao.vista)
                                        MaterialTheme.colorScheme.onSecondary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = getFormattedDate(notificacao),
                        fontSize = 12.sp,
                        color = if (!notificacao.vista) MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

            }
        }
    }
}