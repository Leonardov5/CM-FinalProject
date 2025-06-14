package com.example.finalproject.ui.viewmodels.projects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.model.ProjectAnalytics
import com.example.finalproject.data.repository.ProjetoRepository
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExportFormat(val extension: String, val displayName: String) {
    CSV(".csv", "CSV"),
    JSON(".json", "JSON"),
    TXT(".txt", "Text")
}

class ProjectAnalyticsExporter(
    private val projetoRepository: ProjetoRepository = ProjetoRepository()
) : ViewModel() {

    // UI states
    var isLoading by mutableStateOf(false)
        private set

    var analyticsData by mutableStateOf<ProjectAnalytics?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var exportSuccess by mutableStateOf<Boolean?>(null)
        private set

    /**
     * Load analytics data for a specific project
     */
    fun loadAnalytics(projectId: String) {
        isLoading = true
        errorMessage = null
        exportSuccess = null

        viewModelScope.launch {
            try {
                val result = projetoRepository.getProjectAnalytics(projectId)
                analyticsData = result
                if (result == null) {
                    errorMessage = "No analytics data available for this project"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load analytics"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Export analytics data to file
     */
    fun exportAnalytics(projectId: String, filePath: String, format: ExportFormat = ExportFormat.CSV) {
        viewModelScope.launch {
            try {
                val data = analyticsData ?: run {
                    // Try loading data if not already loaded
                    val result = projetoRepository.getProjectAnalytics(projectId)
                    if (result == null) {
                        setError("No data to export")
                        return@launch
                    }
                    result
                }

                val fileContent = when (format) {
                    ExportFormat.CSV -> generateCsvContent(data)
                    ExportFormat.JSON -> generateJsonContent(data)
                    ExportFormat.TXT -> generateTxtContent(data)
                }

                // Ensure the file has the correct extension
                val finalFilePath = if (!filePath.endsWith(format.extension)) {
                    "$filePath${format.extension}"
                } else {
                    filePath
                }

                File(finalFilePath).writeText(fileContent)
                println(fileContent)
                exportSuccess = true
            } catch (e: Exception) {
                setError(e.message ?: "Failed to export analytics")
            }
        }
    }

    /**
     * Set error message and update export success status
     */
    fun setError(message: String) {
        errorMessage = message
        exportSuccess = false
    }

    /**
     * Generate CSV content
     */
    private fun generateCsvContent(data: ProjectAnalytics): String {
        return buildString {
            append("Project Analytics Report\n")
            append("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n\n")
            append("Project ID,${data.projeto_uuid}\n")
            append("Project Name,${data.nome}\n")
            append("Completion Rate,${data.taxa_conclusao}%\n")
            append("Total Tasks,${data.total_tasks}\n")
            append("Completed Tasks,${data.completed_tasks}\n")
            append("Average Task Completion,${String.format("%.2f", data.avg_task_completion)}%\n")
        }
    }

    /**
     * Generate JSON content
     */
    private fun generateJsonContent(data: ProjectAnalytics): String {
        return buildString {
            append("{\n")
            append("  \"report\": \"Project Analytics Report\",\n")
            append("  \"generated\": \"${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\",\n")
            append("  \"project_id\": \"${data.projeto_uuid}\",\n")
            append("  \"project_name\": \"${data.nome}\",\n")
            append("  \"completion_rate\": ${data.taxa_conclusao},\n")
            append("  \"total_tasks\": ${data.total_tasks},\n")
            append("  \"completed_tasks\": ${data.completed_tasks},\n")
            append("  \"average_task_completion\": ${String.format("%.2f", data.avg_task_completion)}\n")
            append("}")
        }
    }

    /**
     * Generate TXT content
     */
    private fun generateTxtContent(data: ProjectAnalytics): String {
        return buildString {
            append("Project Analytics Report\n")
            append("=======================\n\n")
            append("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n\n")
            append("Project ID: ${data.projeto_uuid}\n")
            append("Project Name: ${data.nome}\n")
            append("Completion Rate: ${data.taxa_conclusao}%\n")
            append("Total Tasks: ${data.total_tasks}\n")
            append("Completed Tasks: ${data.completed_tasks}\n")
            append("Average Task Completion: ${String.format("%.2f", data.avg_task_completion)}%\n")
        }
    }

    /**
     * Reset states after export operation
     */
    fun resetExportState() {
        exportSuccess = null
        errorMessage = null
    }
}