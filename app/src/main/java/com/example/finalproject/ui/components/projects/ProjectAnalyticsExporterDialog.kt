package com.example.finalproject.ui.components.projects

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.finalproject.R
import com.example.finalproject.ui.viewmodels.projects.ExportFormat
import com.example.finalproject.ui.viewmodels.projects.ProjectAnalyticsExporter
import com.example.finalproject.ui.viewmodels.projects.ProjectDetailViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProjectAnalyticsExporterDialog(
    show: Boolean,
    projetoId: String,
    onDismiss: () -> Unit,
    onExport: (ExportFormat) -> Unit
) {
    if (!show) return

    val context = LocalContext.current
    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Obter strings traduzidas no contexto composable
    val fileSavedToText = stringResource(R.string.file_saved_to)
    val fileSavedSuccessToastText = stringResource(R.string.file_saved_success_toast)
    val fileExportedText = stringResource(R.string.file_exported)
    val closeText = stringResource(R.string.close)
    val cancelText = stringResource(R.string.cancel)
    val exportText = stringResource(R.string.export)

    // Get the project name from the viewModel
    val viewModel = remember { ProjectDetailViewModel() }


    // Load project data if not already loaded
    LaunchedEffect(projetoId) {
        viewModel.loadProject(projetoId)
    }

    // Get the analytics exporter to monitor state
    val analyticsExporter = remember { ProjectAnalyticsExporter() }

    // Success state to show confirmation
    var exportSuccess by remember { mutableStateOf(false) }
    var fileSaved by remember { mutableStateOf("") }

    // Selected format state
    var selectedFormat by remember { mutableStateOf(ExportFormat.CSV) }

    // Use actual project name or fallback to the ID if not available yet
    val projectName = viewModel.projeto?.nome?.replace(" ", "_") ?: "Project_$projetoId"

    // Generate filename based on project name, not ID
    val fileName by remember(selectedFormat, projectName) {
        mutableStateOf("${projectName}_analytics_$currentDate${selectedFormat.extension}")
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
                    "Downloads/project_analytics"
                } else {
                    "App Files/project_analytics"
                }

                fileSaved = "$fileSavedToText\n$appStoragePath/${fileName}"

                // Show a toast message that the file was saved successfully
                Toast.makeText(
                    context,
                    fileSavedSuccessToastText,
                    Toast.LENGTH_SHORT
                ).show()

                // Also print file location to logcat for debugging
                println("File should be at: ${externalFilesDir?.absolutePath}/project_analytics/${fileName}")
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
                    text = stringResource(R.string.export_project_analytics),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Format selection
                Text(
                    text = stringResource(R.string.select_format),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportFormat.values().forEach { format ->
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
                    text = stringResource(R.string.file_will_be_saved_as),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (analyticsExporter.isLoading || viewModel.isLoading) {
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
                                text = stringResource(R.string.file_saved_successfully),
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
                                text = stringResource(R.string.access_file_through_manager),
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
                        Text(if (exportSuccess) stringResource(R.string.close) else stringResource(R.string.cancel))
                    }

                    if (!exportSuccess) {
                        Button(
                            onClick = {
                                onExport(selectedFormat)

                                Toast.makeText(
                                    context,
                                    fileExportedText,
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            enabled = !analyticsExporter.isLoading && !viewModel.isLoading
                        ) {
                            Text(stringResource(R.string.export))
                        }
                    }
                }
            }
        }
    }
}