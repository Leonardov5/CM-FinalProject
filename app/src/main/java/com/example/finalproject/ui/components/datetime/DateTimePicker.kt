package com.example.finalproject.ui.components.datetime

import android.R.attr.onClick
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.finalproject.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun DateTimePickerField(
    label: String,
    selectedDateTime: LocalDateTime?,
    formattedDateTime: String,
    onDateTimePickerClick: () -> Unit,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    // TextField com elementos clicáveis
    Box(modifier = Modifier.fillMaxWidth()) {
        // TextField normal
        OutlinedTextField(
            value = formattedDateTime,
            onValueChange = { /* Readonly, não faz nada */ },
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Selecionar data e hora"
                )
            },
            isError = isError,
            enabled = true,
            supportingText = if (isError) {
                { Text(errorMessage) }
            } else null
        )

        // Box clicável que cobre exatamente o OutlinedTextField (sem o supportingText)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    onClick = onDateTimePickerClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(
    onDismissRequest: () -> Unit,
    onDateTimeSelected: (LocalDateTime) -> Unit,
    initialDateTime: LocalDateTime? = null,
    viewModel: DateTimePickerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // Inicialize o viewModel com o datetime inicial se fornecido
    LaunchedEffect(initialDateTime) {
        viewModel.setInitialDateTime(initialDateTime)
    }

    // Observar o estado do UI
    val uiState by viewModel.uiState.collectAsState()

    // Converter para milissegundos para uso com os componentes nativos
    val initialMillis = if (uiState.selectedDate != null) {
        // Converter java.time.LocalDate para milissegundos usando Calendar
        val date = uiState.selectedDate
        val calendar = Calendar.getInstance()
        calendar.set(date.year, date.monthValue - 1, date.getDayOfMonth(), 12, 0, 0)
        // Usar 12:00 (meio-dia) para evitar problemas de fuso horário
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.timeInMillis
    } else {
        // Usar Calendar para obter a data atual no fuso horário local
        val calendar = Calendar.getInstance()
        // Usar meio-dia (12:00) em vez de meia-noite para evitar problemas de fuso horário
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.timeInMillis
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    val timePickerState = rememberTimePickerState(
        initialHour = uiState.selectedTime.getHour(),
        initialMinute = uiState.selectedTime.getMinute()
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { viewModel.showDatePicker() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (uiState.showDatePicker)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                ) {
                    Text(stringResource(id = R.string.date))
                }

                TextButton(
                    onClick = { viewModel.showTimePicker() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (uiState.showTimePicker)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                ) {
                    Text(stringResource(id = R.string.hour))
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.showDatePicker) {
                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier
                            .fillMaxWidth(),
                        title = null,
                        headline = null,
                        showModeToggle = true
                    )
                } else if (uiState.showTimePicker) {
                    TimePicker(
                        state = timePickerState,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Converter os estados dos pickers nativos para LocalDateTime
                    val selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()

                    // Usar Calendar para converter de milissegundos para LocalDate
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = selectedDateMillis

                    val selectedDate = LocalDate.of(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1, // Calendar months are 0-based
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )

                    val selectedTime = LocalTime.of(
                        timePickerState.hour,
                        timePickerState.minute
                    )

                    // Combinar data e hora
                    val dateTime = LocalDateTime.of(selectedDate, selectedTime)

                    onDateTimeSelected(dateTime)
                }
            ) {
                Text(stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(id = R.string.cancel))
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(4.dp)
    )
}

