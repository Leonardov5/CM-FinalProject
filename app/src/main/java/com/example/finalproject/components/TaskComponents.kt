package com.example.finalproject.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finalproject.data.model.Task
import com.example.finalproject.data.model.TaskStatus
import com.example.finalproject.ui.theme.*

@Composable
fun TabRow(
    selectedTab: TaskStatus,
    onTabSelected: (TaskStatus) -> Unit
) {
    androidx.compose.material3.TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = backgroundLight,
        contentColor = primaryLight,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                height = 2.dp,
                color = primaryLight
            )
        }
    ) {
        Tab(
            selected = selectedTab == TaskStatus.TO_DO,
            onClick = { onTabSelected(TaskStatus.TO_DO) },
            text = { Text("To-Do") },
            selectedContentColor = primaryLight,
            unselectedContentColor = outlineLight
        )
        Tab(
            selected = selectedTab == TaskStatus.ON_GOING,
            onClick = { onTabSelected(TaskStatus.ON_GOING) },
            text = { Text("On-Going") },
            selectedContentColor = primaryLight,
            unselectedContentColor = outlineLight
        )
        Tab(
            selected = selectedTab == TaskStatus.COMPLETED,
            onClick = { onTabSelected(TaskStatus.COMPLETED) },
            text = { Text("Done") },
            selectedContentColor = primaryLight,
            unselectedContentColor = outlineLight
        )
    }
}

@Composable
fun TaskCard(task: Task, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = secondaryContainerLight,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = null,
                tint = onSecondaryContainerLight,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = onSecondaryContainerLight
                )

                Text(
                    text = "Criado: ${task.created ?: "Data não disponível"}",
                    fontSize = 12.sp,
                    color = onSecondaryContainerLight.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Ver detalhes",
                    modifier = Modifier.rotate(90f),
                    tint = onSecondaryContainerLight
                )
            }
        }
    }
}
