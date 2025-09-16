package fr.uptrash.fuckupplanning.ui.karma

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.uptrash.fuckupplanning.data.repository.AuthRepository
import fr.uptrash.fuckupplanning.data.repository.KarmaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserKarmaUiState(
    val totalKarma: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UserKarmaViewModel @Inject constructor(
    private val karmaRepository: KarmaRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserKarmaUiState())
    val uiState: StateFlow<UserKarmaUiState> = _uiState.asStateFlow()

    init {
        observeUserKarma()
    }

    private fun observeUserKarma() {
        val userId = authRepository.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                try {
                    karmaRepository.observeUserTotalKarma(userId).collect { totalKarma ->
                        _uiState.value = _uiState.value.copy(
                            totalKarma = totalKarma,
                            isLoading = false,
                            error = null
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun refreshKarma() {
        observeUserKarma()
    }
}
