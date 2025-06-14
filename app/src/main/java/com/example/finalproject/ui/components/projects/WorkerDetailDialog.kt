package com.example.finalproject.ui.components.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.finalproject.R
import com.example.finalproject.data.model.UserProject
import com.example.finalproject.ui.theme.primaryLight
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WorkerDetailDialog(
    show: Boolean,
    worker: UserProject?,
    onDismiss: () -> Unit,
    onEdit: (String, Boolean, Boolean) -> Unit = { _, _, _ -> },
    isAdmin: Boolean = false
) {
    if (show && worker != null) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Imagem
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (worker.utilizador?.fotografia?.isNotBlank() == true) {
                                val context = LocalContext.current
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(worker.utilizador.fotografia)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                androidx.compose.foundation.Image(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(primaryLight),
                                    modifier = Modifier.size(80.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nome
                        Text(
                            text = worker.nome,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        // Username
                        worker.utilizador?.username?.let {
                            if (it.isNotBlank()) {
                                Text(
                                    text = "@$it",
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Data
                        worker.createdAt.let {
                            val formattedDate = try {
                                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                outputFormat.format(inputFormat.parse(it)!!)
                            } catch (e: Exception) {
                                it
                            }

                            InfoRow(
                                icon = Icons.Default.Schedule,
                                label = stringResource(id = R.string.joined_project_at) + ":",
                                value = formattedDate
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Cargo
                        val role = if (worker.isManager) {
                            stringResource(id = R.string.project_manager)
                        } else {
                            stringResource(id = R.string.team_member)
                        }

                        Surface(
                            color = if (worker.isManager)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = role,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = if (worker.isManager)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Opção de gestor
                        if (isAdmin) {
                            Spacer(modifier = Modifier.height(16.dp))

                            var isManagerState by remember { mutableStateOf(worker.isManager) }
                            var isActiveState by remember { mutableStateOf(worker.active) }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = isManagerState,
                                    onCheckedChange = { isManagerState = it }
                                )

                                Text(
                                    text = stringResource(id = R.string.manager),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { isManagerState = !isManagerState }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Switch(
                                    checked = isActiveState,
                                    onCheckedChange = { isActiveState = it },
                                    modifier = Modifier.scale(0.7f)
                                )

                                Text(
                                    text = if (isActiveState)
                                        stringResource(id = R.string.active)
                                    else
                                        stringResource(id = R.string.inactive),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { isActiveState = !isActiveState }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Guardar
                            Button(
                                onClick = { onEdit(worker.userId, isManagerState, isActiveState) },
                                enabled = isManagerState != worker.isManager || isActiveState != worker.active,
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.save),
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
