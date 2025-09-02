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
import javax.inject.Inject

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
}

data class CalendarUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)