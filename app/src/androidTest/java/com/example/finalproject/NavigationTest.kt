package com.example.finalproject

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.finalproject.ui.components.BottomNavigation
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class NavigationTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun testBottomNavigation_allItemsDisplayed() {
        // Get the strings from the translation files
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tasksLabel = context.getString(R.string.bottom_nav_tasks)
        val projectsLabel = context.getString(R.string.bottom_nav_projects)
        val updatesLabel = context.getString(R.string.bottom_nav_updates)

        rule.setContent {
            BottomNavigation(
                currentRoute = Screen.TaskManagement.route,
                onNavigate = {}
            )
        }

        // Check all navigation items are displayed
        rule.onNodeWithText(tasksLabel).assertIsDisplayed()
        rule.onNodeWithText(projectsLabel).assertIsDisplayed()
        rule.onNodeWithText(updatesLabel).assertIsDisplayed()
    }

    @Test
    fun testBottomNavigation_navigateToProjects() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val projectsLabel = context.getString(R.string.bottom_nav_projects)

        var navigatedRoute = ""
        rule.setContent {
            BottomNavigation(
                currentRoute = Screen.TaskManagement.route,
                onNavigate = { route -> navigatedRoute = route }
            )
        }
        rule.onNodeWithText(projectsLabel).performClick()
        assert(navigatedRoute == Screen.Projects.route)
    }

    @Test
    fun testBottomNavigation_navigateToTasks() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tasksLabel = context.getString(R.string.bottom_nav_tasks)

        var navigatedRoute = ""
        rule.setContent {
            BottomNavigation(
                currentRoute = Screen.Projects.route,
                onNavigate = { route -> navigatedRoute = route }
            )
        }
        rule.onNodeWithText(tasksLabel).performClick()
        assert(navigatedRoute == Screen.TaskManagement.route)
    }

    @Test
    fun testBottomNavigation_navigateToUpdates() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val updatesLabel = context.getString(R.string.bottom_nav_updates)

        var navigatedRoute = ""
        rule.setContent {
            BottomNavigation(
                currentRoute = Screen.TaskManagement.route,
                onNavigate = { route -> navigatedRoute = route }
            )
        }
        rule.onNodeWithText(updatesLabel).performClick()
        assert(navigatedRoute == Screen.Updates.route)
    }

    @Test
    fun testBottomNavigation_noNavigationWhenAlreadyOnRoute() {
        var navigationCount = 0

        rule.setContent {
            BottomNavigation(
                currentRoute = Screen.TaskManagement.route,
                onNavigate = { navigationCount++ }
            )
        }

        // Context to resolve string resources
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tasksLabel = context.getString(R.string.bottom_nav_tasks)

        // Click on Tasks navigation item (already selected)
        rule.onNodeWithText(tasksLabel).performClick()

        // Verify navigation was not triggered
        assert(navigationCount == 0)
    }
}