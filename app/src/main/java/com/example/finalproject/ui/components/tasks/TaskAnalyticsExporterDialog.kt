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

    // Get the analytics exporter to monitor state
    val analyticsExporter = remember { TaskAnalyticsExporter() }

    // Success state to show confirmation
    var exportSuccess by remember { mutableStateOf(false) }
    var fileSaved by remember { mutableStateOf("") }

    // Selected format state
    var selectedFormat by remember { mutableStateOf(TaskExportFormat.CSV) }

    // Generate filename based on task name or ID if name not available
    val displayName = taskName?.replace(" ", "_") ?: "Task_$taskId"
    val fileName by remember(selectedFormat, displayName) {
        mutableStateOf("${displayName}_analytics_$currentDate${selectedFormat.extension}")
    }

    // Check if we have storage permission
    val hasStoragePermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Listen for export success from analytics exporter
    LaunchedEffect(analyticsExporter.exportSuccess) {
        analyticsExporter.exportSuccess?.let { success ->
            if (success) {
                exportSuccess = true

                // Set the file path for display
                val externalFilesDir = context.getExternalFilesDir(null)
                val appStoragePath = if (hasStoragePermission) {
                    "Downloads/task_analytics"
                } else {
                    "App Files/task_analytics"
                }

                fileSaved = "File saved to:\n$appStoragePath/${fileName}"

                // Show a toast message that the file was saved successfully
                Toast.makeText(
                    context,
                    "File saved successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                // Also print file location to logcat for debugging
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
                    text = "Export Task Analytics",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Format selection
                Text(
                    text = "Select Format:",
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
                    text = "File will be saved as:",
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

                // Success message
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
                                text = "✓ File Saved Successfully!",
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
                                text = "You can access it through your device's file manager",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Show error message if there is one
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
                        Text(if (exportSuccess) "Close" else "Cancel")
                    }

                    if (!exportSuccess) {
                        Button(
                            onClick = {
                                onExport(selectedFormat)

                                // Show a toast when starting the export
                                Toast.makeText(
                                    context,
                                    "Starting export...",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            enabled = !analyticsExporter.isLoading
                        ) {
                            Text("Export")
                        }
                    }
                }
            }
        }
    }
}
