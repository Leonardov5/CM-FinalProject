package com.example.finalproject.ui.viewmodels.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.model.TaskAnalytics
import com.example.finalproject.data.repository.TarefaRepository
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TaskExportFormat(val extension: String, val displayName: String) {
    CSV(".csv", "CSV"),
    JSON(".json", "JSON"),
    TXT(".txt", "Text")
}

class TaskAnalyticsExporter(
    private val tarefaRepository: TarefaRepository = TarefaRepository()
) : ViewModel() {

    // UI states
    var isLoading by mutableStateOf(false)
        private set

    var analyticsData by mutableStateOf<TaskAnalytics?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var exportSuccess by mutableStateOf<Boolean?>(null)
        private set

    fun loadAnalytics(taskId: String) {
        isLoading = true
        errorMessage = null
        exportSuccess = null

        viewModelScope.launch {
            try {
                val result = tarefaRepository.getTaskAnalytics(taskId)
                analyticsData = result
                if (result == null) {
                    errorMessage = "No analytics data available for this task"
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
    fun exportAnalytics(taskId: String, filePath: String, format: TaskExportFormat = TaskExportFormat.CSV) {
        viewModelScope.launch {
            try {
                val data = analyticsData ?: run {
                    // Try loading data if not already loaded
                    val result = tarefaRepository.getTaskAnalytics(taskId)
                    if (result == null) {
                        setError("No data to export")
                        return@launch
                    }
                    result
                }

                val fileContent = when (format) {
                    TaskExportFormat.CSV -> generateCsvContent(data)
                    TaskExportFormat.JSON -> generateJsonContent(data)
                    TaskExportFormat.TXT -> generateTxtContent(data)
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
    private fun generateCsvContent(data: TaskAnalytics): String {
        return buildString {
            append("Task Analytics Report\n")
            append("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n\n")
            append("Task ID,${data.tarefa_uuid}\n")
            append("Task Name,${data.task_name}\n")
            append("Task Description,${data.task_description ?: "N/A"}\n")
            append("Project ID,${data.projeto_uuid}\n")
            append("Project Name,${data.project_name}\n")
            append("Priority,${data.prioridade}\n")
            append("Status,${data.status}\n")
            append("Completion Rate,${data.taxa_conclusao}%\n")
            append("Start Date,${data.data_inicio ?: "N/A"}\n")
            append("End Date,${data.data_fim ?: "N/A"}\n")
            append("Total Work Records,${data.total_works}\n")
            append("Total Time Spent (minutes),${data.total_minutes_spent}\n")
            append("Total Contribution,${data.total_contribution}\n")
            append("Total Observations,${data.total_observations}\n")
            append("Total Assigned Users,${data.total_assigned_users}\n")
            append("Days Since Creation,${data.days_since_creation}\n")
            append("Days to Deadline,${data.days_to_deadline ?: "N/A"}\n")
            append("Is Overdue,${data.is_overdue}\n")
            append("Created By,${data.created_by_name ?: "N/A"}\n")
            append("Modified By,${data.modified_by_name ?: "N/A"}\n")
            append("Created At,${data.created_at ?: "N/A"}\n")
            append("Updated At,${data.updated_at ?: "N/A"}\n")
        }
    }

    /**
     * Generate JSON content
     */
    private fun generateJsonContent(data: TaskAnalytics): String {
        return buildString {
            append("{\n")
            append("  \"report\": \"Task Analytics Report\",\n")
            append("  \"generated\": \"${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\",\n")
            append("  \"task_id\": \"${data.tarefa_uuid}\",\n")
            append("  \"task_name\": \"${data.task_name}\",\n")
            append("  \"task_description\": ${if (data.task_description != null) "\"${data.task_description}\"" else "null"},\n")
            append("  \"project_id\": \"${data.projeto_uuid}\",\n")
            append("  \"project_name\": \"${data.project_name}\",\n")
            append("  \"priority\": \"${data.prioridade}\",\n")
            append("  \"status\": \"${data.status}\",\n")
            append("  \"completion_rate\": ${data.taxa_conclusao},\n")
            append("  \"start_date\": ${if (data.data_inicio != null) "\"${data.data_inicio}\"" else "null"},\n")
            append("  \"end_date\": ${if (data.data_fim != null) "\"${data.data_fim}\"" else "null"},\n")
            append("  \"total_work_records\": ${data.total_works},\n")
            append("  \"total_time_spent_minutes\": ${data.total_minutes_spent},\n")
            append("  \"total_contribution\": ${data.total_contribution},\n")
            append("  \"total_observations\": ${data.total_observations},\n")
            append("  \"total_assigned_users\": ${data.total_assigned_users},\n")
            append("  \"days_since_creation\": ${data.days_since_creation},\n")
            append("  \"days_to_deadline\": ${if (data.days_to_deadline != null) data.days_to_deadline else "null"},\n")
            append("  \"is_overdue\": ${data.is_overdue},\n")
            append("  \"created_by\": ${if (data.created_by_name != null) "\"${data.created_by_name}\"" else "null"},\n")
            append("  \"modified_by\": ${if (data.modified_by_name != null) "\"${data.modified_by_name}\"" else "null"},\n")
            append("  \"created_at\": ${if (data.created_at != null) "\"${data.created_at}\"" else "null"},\n")
            append("  \"updated_at\": ${if (data.updated_at != null) "\"${data.updated_at}\"" else "null"}\n")
            append("}")
        }
    }

    /**
     * Generate TXT content
     */
    private fun generateTxtContent(data: TaskAnalytics): String {
        return buildString {
            append("Task Analytics Report\n")
            append("=======================\n\n")
            append("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n\n")
            append("Task ID: ${data.tarefa_uuid}\n")
            append("Task Name: ${data.task_name}\n")
            append("Task Description: ${data.task_description ?: "N/A"}\n")
            append("Project ID: ${data.projeto_uuid}\n")
            append("Project Name: ${data.project_name}\n")
            append("Priority: ${data.prioridade}\n")
            append("Status: ${data.status}\n")
            append("Completion Rate: ${data.taxa_conclusao}%\n")
            append("Start Date: ${data.data_inicio ?: "N/A"}\n")
            append("End Date: ${data.data_fim ?: "N/A"}\n")
            append("Total Work Records: ${data.total_works}\n")
            append("Total Time Spent (minutes): ${data.total_minutes_spent}\n")
            append("Total Contribution: ${data.total_contribution}\n")
            append("Total Observations: ${data.total_observations}\n")
            append("Total Assigned Users: ${data.total_assigned_users}\n")
            append("Days Since Creation: ${data.days_since_creation}\n")
            append("Days to Deadline: ${data.days_to_deadline ?: "N/A"}\n")
            append("Is Overdue: ${data.is_overdue}\n")
            append("Created By: ${data.created_by_name ?: "N/A"}\n")
            append("Modified By: ${data.modified_by_name ?: "N/A"}\n")
            append("Created At: ${data.created_at ?: "N/A"}\n")
            append("Updated At: ${data.updated_at ?: "N/A"}\n")
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
