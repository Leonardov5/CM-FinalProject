package com.example.finalproject.ui.viewmodels.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.finalproject.data.model.DemoTasks
import com.example.finalproject.data.model.Task
import com.example.finalproject.data.model.TaskStatus

class TaskManagementViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set

    var selectedTab by mutableStateOf(TaskStatus.TO_DO)
        private set

    var tasks by mutableStateOf(DemoTasks.tasks)
        private set

    val filteredTasks: List<Task>
        get() = tasks.filter { it.status == selectedTab }

    fun selectTab(tab: TaskStatus) {
        selectedTab = tab
    }
}
