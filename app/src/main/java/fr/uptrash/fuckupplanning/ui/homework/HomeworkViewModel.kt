package fr.uptrash.fuckupplanning.ui.homework

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@HiltViewModel
class HomeworkViewModel @Inject constructor(

) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeworkUiState())
    val uiState: StateFlow<HomeworkUiState> = _uiState.asStateFlow()
}

data class HomeworkUiState @OptIn(ExperimentalTime::class) constructor(
    val isLoading: Boolean = false,
    val error: String? = null
)