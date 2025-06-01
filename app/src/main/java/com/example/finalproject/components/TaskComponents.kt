package com.example.finalproject.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
            selected = selectedTab == TaskStatus.ON_GOING,
            onClick = { onTabSelected(TaskStatus.ON_GOING) },
            text = { Text("On-Going") },
            selectedContentColor = primaryLight,
            unselectedContentColor = outlineLight
        )
        Tab(
            selected = selectedTab == TaskStatus.TO_DO,
            onClick = { onTabSelected(TaskStatus.TO_DO) },
            text = { Text("To-Do") },
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
fun TaskCard(task: Task) {
    var isExpanded by remember { mutableStateOf(task.id == 1) } // First task expanded by default
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        color = secondaryContainerLight,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = onPrimaryContainerLight,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotationAngle),
                    tint = onPrimaryContainerLight
                )
            }

            // Expanded Content
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                task.created?.let { created ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Created:",
                            fontSize = 14.sp,
                            color = onPrimaryContainerLight,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = created,
                            fontSize = 14.sp,
                            color = onPrimaryContainerLight,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                task.priority?.let { priority ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Priority:",
                            fontSize = 14.sp,
                            color = onPrimaryContainerLight,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = priority.toString(),
                            fontSize = 14.sp,
                            color = onPrimaryContainerLight,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                task.description?.let { description ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "Description:",
                            fontSize = 14.sp,
                            color = onPrimaryContainerLight,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = description,
                            fontSize = 14.sp,
                            color = onPrimaryContainerLight,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .weight(1f)
                        )
                    }
                }
            }
        }
    }
}

