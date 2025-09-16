package fr.uptrash.fuckupplanning.ui.homework

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.uptrash.fuckupplanning.data.model.Homework
import fr.uptrash.fuckupplanning.data.repository.HomeworkRepository
import fr.uptrash.fuckupplanning.data.repository.MMIYear
import fr.uptrash.fuckupplanning.data.repository.SettingsRepository
import fr.uptrash.fuckupplanning.data.repository.TPGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@HiltViewModel
class HomeworkViewModel @Inject constructor(
    private val homeworkRepository: HomeworkRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeworkUiState())
    val uiState: StateFlow<HomeworkUiState> = _uiState.asStateFlow()

    private val _allHomework = MutableStateFlow<List<Homework>>(emptyList())

    init {
        loadHomework()
        setupReactiveFiltering()
    }

    private fun setupReactiveFiltering() {
        viewModelScope.launch {
            combine(
                _allHomework,
                settingsRepository.selectedTPGroupFlow,
                settingsRepository.selectedMMIYearFlow
            ) { homeworkList, tpGroup, mmiYear ->
                Triple(homeworkList, tpGroup, mmiYear)
            }.collect { (homeworkList, tpGroup, mmiYear) ->
                val filteredHomework = homeworkList.filter { homework ->
                    (tpGroup == TPGroup.ALL || homework.tp == tpGroup) &&
                            homework.year == mmiYear
                }
                _uiState.value = _uiState.value.copy(
                    homeworkList = filteredHomework,
                    selectedTPGroup = tpGroup,
                    selectedMMIYear = mmiYear
                )
            }
        }
    }

    private fun loadHomework() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            homeworkRepository.getAllHomework()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
                .collect { homeworkList ->
                    _allHomework.value = homeworkList
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun addHomework(
        description: String,
        dueDate: Long,
    ) {
        viewModelScope.launch {
            val homework = Homework(
                description = description,
                dueDate = dueDate,
                year = _uiState.value.selectedMMIYear,
                tp = _uiState.value.selectedTPGroup
            )

            homeworkRepository.addHomework(homework)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

    fun toggleHomeworkCompletion(homework: Homework) {
        viewModelScope.launch {
            val updatedHomework = homework.copy(isCompleted = !homework.isCompleted)
            homeworkRepository.updateHomework(updatedHomework)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

    fun deleteHomework(homeworkId: String) {
        viewModelScope.launch {
            homeworkRepository.deleteHomework(homeworkId)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }
}

data class HomeworkUiState @OptIn(ExperimentalTime::class) constructor(
    val isLoading: Boolean = false,
    val homeworkList: List<Homework> = emptyList(),
    val selectedTPGroup: TPGroup = TPGroup.ALL,
    val selectedMMIYear: MMIYear = MMIYear.MMI1,
    val error: String? = null,
    val showAddDialog: Boolean = false
)