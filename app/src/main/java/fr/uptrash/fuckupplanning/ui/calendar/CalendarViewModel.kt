package fr.uptrash.fuckupplanning.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.uptrash.fuckupplanning.data.model.Event
import fr.uptrash.fuckupplanning.data.repository.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

enum class CalendarViewMode {
    DAY, WEEK
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getEvents().fold(
                onSuccess = { events ->
                    _uiState.value = _uiState.value.copy(
                        events = events,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
            )
        }
    }

    fun switchViewMode(viewMode: CalendarViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = viewMode)
    }

    fun navigateToDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    fun navigatePrevious() {
        val currentDate = _uiState.value.selectedDate
        val newDate = when (_uiState.value.viewMode) {
            CalendarViewMode.DAY -> currentDate.minus(1, DateTimeUnit.DAY)
            CalendarViewMode.WEEK -> currentDate.minus(7, DateTimeUnit.DAY)
        }
        _uiState.value = _uiState.value.copy(selectedDate = newDate)
    }

    fun navigateNext() {
        val currentDate = _uiState.value.selectedDate
        val newDate = when (_uiState.value.viewMode) {
            CalendarViewMode.DAY -> currentDate.plus(1, DateTimeUnit.DAY)
            CalendarViewMode.WEEK -> currentDate.plus(7, DateTimeUnit.DAY)
        }
        _uiState.value = _uiState.value.copy(selectedDate = newDate)
    }
}

data class CalendarUiState @OptIn(ExperimentalTime::class) constructor(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val viewMode: CalendarViewMode = CalendarViewMode.WEEK,
    val selectedDate: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
)