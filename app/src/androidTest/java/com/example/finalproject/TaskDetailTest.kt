package com.example.finalproject

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.finalproject.ui.screens.tasks.TaskDetailScreen
import com.example.finalproject.ui.screens.tasks.formatDate
import com.example.finalproject.ui.viewmodels.tasks.TaskDetailViewModel
import org.junit.Rule
import org.junit.Test

class TaskDetailTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun testTaskDetailScreen() {

        rule.setContent {
            TaskDetailScreen(
                taskId = "task1",
                onBackPressed = {},
                viewModel = TaskDetailViewModel()
            )
        }

        // Check if the task title is displayed
        rule.onNodeWithText("Implement user authentication").assertIsDisplayed()

        // Check if the task description is displayed
        rule.onNodeWithText("Add login and registration functionality with Firebase Auth").assertIsDisplayed()

        // Check if the due date is formatted correctly
        rule.onNodeWithText(formatDate("2025-06-10T09:30:00Z") ?: "").assertIsDisplayed()
    }
}