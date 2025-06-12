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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finalproject.data.model.Tarefa
import com.example.finalproject.data.model.TarefaStatus
import com.example.finalproject.data.model.Task
import com.example.finalproject.data.model.TaskStatus
import com.example.finalproject.ui.theme.*

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
            text = { Text("To-Do") },
        )
        Tab(
            selected = selectedTab == TarefaStatus.em_andamento,
            onClick = { onTabSelected(TarefaStatus.em_andamento) },
            text = { Text("On-Going") },
        )
        Tab(
            selected = selectedTab == TarefaStatus.concluida,
            onClick = { onTabSelected(TarefaStatus.concluida) },
            text = { Text("Done") },
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
        color = MaterialTheme.colorScheme.secondaryContainer,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

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
                    text = "Criado: ${task.createdAt ?: "Data não disponível"}",
                    fontSize = 12.sp,
                )
            }

            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Ver detalhes",
                    modifier = Modifier.rotate(90f),
                )
            }
        }
    }
}
