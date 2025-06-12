package com.example.finalproject.ui.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finalproject.R
import com.example.finalproject.data.model.Tarefa
import com.example.finalproject.data.model.TarefaStatus
import com.example.finalproject.data.model.Task
import com.example.finalproject.data.model.TaskStatus
import com.example.finalproject.ui.theme.*

fun formatDate(iso: String?): String? {
    return try {
        val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        iso?.let { dateFormat.format(isoFormat.parse(it)) }
    } catch (e: Exception) {
        iso
    }
}

@Composable
fun TabRow(
    selectedTab: TarefaStatus,
    onTabSelected: (TarefaStatus) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                height = 2.dp,
            )
        }
    ) {
        Tab(
            selected = selectedTab == TarefaStatus.pendente,
            onClick = { onTabSelected(TarefaStatus.pendente) },
            text = { Text(stringResource(id = R.string.to_do)) },
        )
        Tab(
            selected = selectedTab == TarefaStatus.em_andamento,
            onClick = { onTabSelected(TarefaStatus.em_andamento) },
            text = { Text(stringResource(id = R.string.on_going)) },
        )
        Tab(
            selected = selectedTab == TarefaStatus.concluida,
            onClick = { onTabSelected(TarefaStatus.concluida) },
            text = { Text(stringResource(id = R.string.done)) },
        )
    }
}

@Composable
fun TaskCard(task: Tarefa, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
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
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.nome,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )

                Text(
                    text = stringResource(
                        id = R.string.created_at,
                        formatDate(task.createdAt) ?: stringResource(id = R.string.unknown_creation_date)
                    ),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = stringResource(id = R.string.view_details),
                    modifier = Modifier.rotate(90f),
                )
            }
        }
    }
}
