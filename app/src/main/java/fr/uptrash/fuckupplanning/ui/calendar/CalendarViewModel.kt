package fr.uptrash.fuckupplanning.ui.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.uptrash.fuckupplanning.data.model.Event
import fr.uptrash.fuckupplanning.data.repository.CalendarRepository
import fr.uptrash.fuckupplanning.data.repository.RestaurantMenuRepository
import fr.uptrash.fuckupplanning.data.repository.SettingsRepository
import fr.uptrash.fuckupplanning.data.repository.TPGroup
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
    DAY, WEEK, MONTH
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: CalendarRepository,
    private val restaurantRepository: RestaurantMenuRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.selectedTPGroupFlow.collect { tpGroup ->
                selectTPGroup(tpGroup)
            }
        }
    }

    fun loadEvents() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            repository.getEvents().fold(
                onSuccess = { events ->
                    _uiState.value = _uiState.value.copy(
                        allEvents = events,
                        isLoading = false,
                        error = null
                    )
                    loadSettings()
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
            CalendarViewMode.MONTH -> currentDate.minus(1, DateTimeUnit.MONTH)
        }
        _uiState.value = _uiState.value.copy(selectedDate = newDate)
    }

    fun navigateNext() {
        val currentDate = _uiState.value.selectedDate
        val newDate = when (_uiState.value.viewMode) {
            CalendarViewMode.DAY -> currentDate.plus(1, DateTimeUnit.DAY)
            CalendarViewMode.WEEK -> currentDate.plus(7, DateTimeUnit.DAY)
            CalendarViewMode.MONTH -> currentDate.plus(1, DateTimeUnit.MONTH)
        }
        _uiState.value = _uiState.value.copy(selectedDate = newDate)
    }

    fun selectEvent(event: Event?) {
        _uiState.value = _uiState.value.copy(selectedEvent = event)
    }

    fun dismissEventDetail() {
        _uiState.value = _uiState.value.copy(selectedEvent = null)
    }

    fun selectDayForCourseList(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(selectedDayForCourseList = date)
    }

    fun dismissDayCourseList() {
        _uiState.value = _uiState.value.copy(selectedDayForCourseList = null)
    }

    fun selectTPGroup(tpGroup: TPGroup) {
        _uiState.value = _uiState.value.copy(selectedTPGroup = tpGroup)
        viewModelScope.launch {
            settingsRepository.saveSelectedTPGroup(tpGroup)
        }
        applyTPFilter()
    }

    fun showSettings() {
        _uiState.value = _uiState.value.copy(showSettings = true)
    }

    fun dismissSettings() {
        _uiState.value = _uiState.value.copy(showSettings = false)
    }

    /**
     * Show the restaurant menu modal. The menu is cached in uiState.restaurantMenu and
     * will not be refetched unless [forceRefresh] is true or the cache is empty.
     */
    fun showMenu(forceRefresh: Boolean = false) {
        val currentMenu = _uiState.value.restaurantMenu

        // If we already have a menu and we're not forcing a refresh, just show it without refetching.
        if (currentMenu.isNotEmpty() && !forceRefresh) {
            _uiState.value =
                _uiState.value.copy(showMenu = true, isMenuLoading = false, menuError = null)
            return
        }

        // Otherwise, fetch the menu and update the cache.
        _uiState.value =
            _uiState.value.copy(showMenu = true, isMenuLoading = true, menuError = null)
        viewModelScope.launch {
            try {
                val menu = restaurantRepository.fetchMenu()
                _uiState.value = _uiState.value.copy(
                    restaurantMenu = menu,
                    isMenuLoading = false,
                    menuError = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isMenuLoading = false,
                    menuError = e.message ?: "Failed to load menu"
                )
            }
        }
    }

    fun dismissMenu() {
        _uiState.value = _uiState.value.copy(showMenu = false)
    }

    private fun applyTPFilter() {
        val currentState = _uiState.value
        Log.d("CalendarViewModel", "Applying TP filter: ${currentState.selectedTPGroup}")
        Log.d("CalendarViewModel", "Total events before filter: ${currentState.allEvents.size}")
        val filteredEvents = when (currentState.selectedTPGroup) {
            TPGroup.ALL -> currentState.allEvents
            TPGroup.TP1 -> {
                currentState.allEvents.filter { event ->
                    // Show TP1, TDA, CM courses, or events with no courseType (general events)
                    event.courseType == null ||
                            event.courseType == "TP1" ||
                            event.courseType == "TDA" ||
                            event.courseType == "CM"
                }
            }

            TPGroup.TP2 -> {
                currentState.allEvents.filter { event ->
                    // Show TP2, TDA, CM courses, or events with no courseType (general events)
                    event.courseType == null ||
                            event.courseType == "TP2" ||
                            event.courseType == "TDA" ||
                            event.courseType == "CM"
                }
            }

            TPGroup.TP3 -> {
                currentState.allEvents.filter { event ->
                    // Show TP3, TDB, CM courses, or events with no courseType (general events)
                    event.courseType == null ||
                            event.courseType == "TP3" ||
                            event.courseType == "TDB" ||
                            event.courseType == "CM"
                }
            }

            TPGroup.TP4 -> {
                currentState.allEvents.filter { event ->
                    // Show TP4, TDB, CM courses, or events with no courseType (general events)
                    event.courseType == null ||
                            event.courseType == "TP4" ||
                            event.courseType == "TDB" ||
                            event.courseType == "CM"
                }
            }
        }
        _uiState.value = currentState.copy(events = filteredEvents)
    }
}

data class CalendarUiState @OptIn(ExperimentalTime::class) constructor(
    val events: List<Event> = emptyList(),
    val allEvents: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val viewMode: CalendarViewMode = CalendarViewMode.WEEK,
    val selectedDate: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val selectedEvent: Event? = null,
    val selectedTPGroup: TPGroup = TPGroup.ALL,
    val selectedDayForCourseList: LocalDate? = null,
    val showSettings: Boolean = false,

    // Restaurant menu UI state
    val showMenu: Boolean = false,
    val restaurantMenu: Map<String, List<RestaurantMenuRepository.MenuItem>> = emptyMap(),
    val isMenuLoading: Boolean = false,
    val menuError: String? = null
)