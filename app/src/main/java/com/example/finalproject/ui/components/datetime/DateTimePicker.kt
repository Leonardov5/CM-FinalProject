package com.example.finalproject.ui.components.datetime

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.finalproject.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar

@Composable
fun DateTimePickerField(
    label: String,
    selectedDateTime: LocalDateTime?,
    formattedDateTime: String,
    onDateTimePickerClick: () -> Unit,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = formattedDateTime,
            onValueChange = { },
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
    LaunchedEffect(initialDateTime) {
        viewModel.setInitialDateTime(initialDateTime)
    }

    val uiState by viewModel.uiState.collectAsState()

    // Converter para milissegundos para uso com os componentes nativos
    // Usamos 12:00 (meio-dia) para evitar problemas de fuso horário
    val initialMillis = if (uiState.selectedDate != null) {
        val date = uiState.selectedDate
        val calendar = Calendar.getInstance()
        calendar.set(date.year, date.monthValue - 1, date.getDayOfMonth(), 12, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.timeInMillis
    } else {
        val calendar = Calendar.getInstance()
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
                    val selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = selectedDateMillis

                    val selectedDate = LocalDate.of(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )

                    val selectedTime = LocalTime.of(
                        timePickerState.hour,
                        timePickerState.minute
                    )

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

