package com.example.finalproject.ui.components.tasks

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.finalproject.ui.viewmodels.tasks.TaskAnalyticsExporter
import com.example.finalproject.ui.viewmodels.tasks.TaskExportFormat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.stringResource
import com.example.finalproject.R
@Composable
fun TaskAnalyticsExporterDialog(
    show: Boolean,
    taskId: String,
    taskName: String?,
    onDismiss: () -> Unit,
    onExport: (TaskExportFormat) -> Unit
) {
    if (!show) return

    val context = LocalContext.current
    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val exportTaskAnalyticsText = stringResource(R.string.export_task_analytics)
    val selectFormatText = stringResource(R.string.select_format)
    val fileWillBeSavedAsText = stringResource(R.string.file_will_be_saved_as)
    val fileSavedSuccessfullyText = stringResource(R.string.file_saved_successfully)
    val fileSavedToText = stringResource(R.string.file_saved_to)
    val accessFileManagerText = stringResource(R.string.access_file_through_manager)
    val startingExportText = stringResource(R.string.starting_export)
    val fileSavedSuccessToastText = stringResource(R.string.file_saved_success_toast)
    val exportText = stringResource(R.string.export)
    val closeText = stringResource(R.string.close)
    val cancelText = stringResource(R.string.cancel)

    val analyticsExporter = remember { TaskAnalyticsExporter() }

    var exportSuccess by remember { mutableStateOf(false) }
    var fileSaved by remember { mutableStateOf("") }

    var selectedFormat by remember { mutableStateOf(TaskExportFormat.CSV) }

    val displayName = taskName?.replace(" ", "_") ?: "Task_$taskId"
    val fileName by remember(selectedFormat, displayName) {
        mutableStateOf("${displayName}_analytics_$currentDate${selectedFormat.extension}")
    }

    val hasStoragePermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(analyticsExporter.exportSuccess) {
        analyticsExporter.exportSuccess?.let { success ->
            if (success) {
                exportSuccess = true

                val externalFilesDir = context.getExternalFilesDir(null)
                val appStoragePath = if (hasStoragePermission) {
                    "Downloads/task_analytics"
                } else {
                    "App Files/task_analytics"
                }

                fileSaved = "$fileSavedToText\n$appStoragePath/${fileName}"

                Toast.makeText(
                    context,
                    fileSavedSuccessToastText,
                    Toast.LENGTH_SHORT
                ).show()

                println("File should be at: ${externalFilesDir?.absolutePath}/task_analytics/${fileName}")
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = exportTaskAnalyticsText,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = selectFormatText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskExportFormat.values().forEach { format ->
                        FilterChip(
                            selected = selectedFormat == format,
                            onClick = { selectedFormat = format },
                            label = { Text(format.displayName) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = fileWillBeSavedAsText,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (analyticsExporter.isLoading) {
                    CircularProgressIndicator()
                }

                if (exportSuccess) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = fileSavedSuccessfullyText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = fileSaved,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = accessFileManagerText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                analyticsExporter.errorMessage?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (exportSuccess) closeText else cancelText)
                    }

                    if (!exportSuccess) {
                        Button(
                            onClick = {
                                onExport(selectedFormat)

                                Toast.makeText(
                                    context,
                                    startingExportText,
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            enabled = !analyticsExporter.isLoading
                        ) {
                            Text(exportText)
                        }
                    }
                }
            }
        }
    }
}