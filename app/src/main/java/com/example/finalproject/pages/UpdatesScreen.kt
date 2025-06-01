package com.example.finalproject.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finalproject.components.*
import com.example.finalproject.ui.theme.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Update(
    val id: Int,
    val title: String,
    val message: String,
    val date: String,
    val isNew: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesScreen(modifier: Modifier = Modifier) {
    val updates = remember {
        listOf(
            Update(1, "Task assigned", "You've been assigned to the task 'Create wireframes'", "Jun 1, 2025", true),
            Update(2, "Comment on your task", "Maria commented on 'Update documentation'", "May 31, 2025", true),
            Update(3, "Deadline approaching", "Task 'Design UI Components' is due tomorrow", "May 30, 2025"),
            Update(4, "Project update", "Project 'Website Redesign' has been updated", "May 29, 2025"),
            Update(5, "Task completed", "João marked the task 'Setup database' as completed", "May 28, 2025")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundLight)
    ) {
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(0.7f),
                        shape = RoundedCornerShape(25.dp),
                        color = surfaceVariantLight
                    ) {
                        Text(
                            text = "Updates",
                            modifier = Modifier.padding(vertical = 12.dp),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = onSurfaceVariantLight
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = primaryLight
                    )
                }
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = primaryLight
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = backgroundLight
            )
        )

        // Updates List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(updates) { update ->
                UpdateCard(update = update)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun UpdateCard(update: Update) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (update.isNew) secondaryContainerLight else surfaceLight,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Notification icon with circle background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (update.isNew) tertiaryLight.copy(alpha = 0.2f) else surfaceVariantLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (update.isNew) tertiaryLight else outlineLight,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = update.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (update.isNew) onTertiaryContainerLight else onSurfaceLight
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = update.message,
                    fontSize = 14.sp,
                    color = if (update.isNew) onTertiaryContainerLight else onSurfaceLight
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = update.date,
                    fontSize = 12.sp,
                    color = if (update.isNew) onTertiaryContainerLight.copy(alpha = 0.7f) else onSurfaceLight.copy(alpha = 0.7f)
                )
            }

            if (update.isNew) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(tertiaryLight)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdatesScreenPreview() {
    MaterialTheme {
        UpdatesScreen()
    }
}
