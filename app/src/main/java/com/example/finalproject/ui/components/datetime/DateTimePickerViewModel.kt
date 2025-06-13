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

    // Estado do componente
    private val _uiState = MutableStateFlow(DateTimePickerUIState())
    val uiState: StateFlow<DateTimePickerUIState> = _uiState.asStateFlow()

    // Formatador de data para exibição
    private val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    // Configurações
    val yearRange = (LocalDate.now().year - 5)..(LocalDate.now().year + 5)
    
    init {
        // Inicializa com a data e hora atuais
        val now = LocalDateTime.now()
        _uiState.update {
            it.copy(
                selectedDate = now.toLocalDate(),
                selectedTime = now.toLocalTime()
            )
        }
    }

    // Atualizar a visualização (data ou hora)
    fun showDatePicker() {
        _uiState.update { it.copy(showDatePicker = true, showTimePicker = false) }
    }

    fun showTimePicker() {
        _uiState.update { it.copy(showDatePicker = false, showTimePicker = true) }
    }

    // Atualizar a data selecionada
    fun updateSelectedDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    // Navegar pelos anos
    fun incrementYear() {
        val currentDate = _uiState.value.selectedDate
        if (currentDate.year < yearRange.last) {
            _uiState.update { it.copy(selectedDate = currentDate.plusYears(1)) }
        }
    }

    fun decrementYear() {
        val currentDate = _uiState.value.selectedDate
        if (currentDate.year > yearRange.first) {
            _uiState.update { it.copy(selectedDate = currentDate.minusYears(1)) }
        }
    }

    // Navegar pelos meses
    fun incrementMonth() {
        val currentDate = _uiState.value.selectedDate
        _uiState.update { it.copy(selectedDate = currentDate.plusMonths(1)) }
    }

    fun decrementMonth() {
        val currentDate = _uiState.value.selectedDate
        _uiState.update { it.copy(selectedDate = currentDate.minusMonths(1)) }
    }

    // Atualizar a hora selecionada
    fun incrementHour() {
        val currentTime = _uiState.value.selectedTime
        _uiState.update { it.copy(selectedTime = currentTime.plusHours(1)) }
    }
    
    fun decrementHour() {
        val currentTime = _uiState.value.selectedTime
        _uiState.update { it.copy(selectedTime = currentTime.minusHours(1)) }
    }

    fun incrementMinute() {
        val currentTime = _uiState.value.selectedTime
        _uiState.update { it.copy(selectedTime = currentTime.plusMinutes(1)) }
    }

    fun decrementMinute() {
        val currentTime = _uiState.value.selectedTime
        _uiState.update { it.copy(selectedTime = currentTime.minusMinutes(1)) }
    }

    // Inicializar com um dateTime específico
    fun setInitialDateTime(dateTime: LocalDateTime?) {
        if (dateTime != null) {
            _uiState.update {
                it.copy(
                    selectedDate = dateTime.toLocalDate(),
                    selectedTime = dateTime.toLocalTime(),
                    showDatePicker = true,  // Iniciar mostrando o seletor de data
                    showTimePicker = false
                )
            }
        }
    }

    // Obter o dateTime selecionado
    fun getSelectedDateTime(): LocalDateTime {
        val state = _uiState.value
        return LocalDateTime.of(state.selectedDate, state.selectedTime)
    }

    // Formatar a data para exibição
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

