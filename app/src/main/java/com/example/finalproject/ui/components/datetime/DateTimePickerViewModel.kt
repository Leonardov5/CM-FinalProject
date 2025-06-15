package com.example.finalproject.ui.components.datetime

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class DateTimePickerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DateTimePickerUIState())
    val uiState: StateFlow<DateTimePickerUIState> = _uiState.asStateFlow()

    private val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    val yearRange = (LocalDate.now().year - 5)..(LocalDate.now().year + 5)
    
    init {
        val now = LocalDateTime.now()
        _uiState.update {
            it.copy(
                selectedDate = now.toLocalDate(),
                selectedTime = now.toLocalTime()
            )
        }
    }

    fun showDatePicker() {
        _uiState.update { it.copy(showDatePicker = true, showTimePicker = false) }
    }

    fun showTimePicker() {
        _uiState.update { it.copy(showDatePicker = false, showTimePicker = true) }
    }

    fun setInitialDateTime(dateTime: LocalDateTime?) {
        if (dateTime != null) {
            _uiState.update {
                it.copy(
                    selectedDate = dateTime.toLocalDate(),
                    selectedTime = dateTime.toLocalTime(),
                    showDatePicker = true,
                    showTimePicker = false
                )
            }
        }
    }

    fun formatDateTime(dateTime: LocalDateTime?): String {
        return dateTime?.format(displayFormatter) ?: ""
    }
}

@RequiresApi(Build.VERSION_CODES.O)
data class DateTimePickerUIState constructor(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedTime: LocalTime = LocalTime.now(),
    val showDatePicker: Boolean = true,
    val showTimePicker: Boolean = false
)

