package com.example.finalproject

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import com.example.finalproject.ui.screens.tasks.TaskManagementScreen
import org.junit.Rule
import org.junit.Test
import kotlin.math.tan

class NavigationTest {
    @get:Rule

    val rule = createComposeRule()

    @Test
    fun testTaskManagementScreen() {
        rule.setContent { TaskManagementScreen() }

        rule.onRoot().assertExists()
    }
}