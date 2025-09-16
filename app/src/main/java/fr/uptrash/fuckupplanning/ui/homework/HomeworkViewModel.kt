package fr.uptrash.fuckupplanning.ui.homework

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.uptrash.fuckupplanning.data.model.Homework
import fr.uptrash.fuckupplanning.data.repository.HomeworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@HiltViewModel
class HomeworkViewModel @Inject constructor(
    private val homeworkRepository: HomeworkRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeworkUiState())
    val uiState: StateFlow<HomeworkUiState> = _uiState.asStateFlow()

    init {
        loadHomework()
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
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        homeworkList = homeworkList,
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
                dueDate = dueDate
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
    val error: String? = null,
    val showAddDialog: Boolean = false
)