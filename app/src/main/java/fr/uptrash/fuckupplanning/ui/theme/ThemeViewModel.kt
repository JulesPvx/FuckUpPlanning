package fr.uptrash.fuckupplanning.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.uptrash.fuckupplanning.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.ExperimentalTime

sealed class AppTheme(
    val key: String,
    val displayName: String
) {
    object SYSTEM : AppTheme("system", "System Default")

    companion object {
        fun fromKey(key: String): AppTheme {
            return appThemes.find { it.key == key } ?: SYSTEM
        }
    }
}

sealed class CustomAppTheme(
    key: String,
    displayName: String,
    val light: ColorScheme,
    val dark: ColorScheme
) : AppTheme(key, displayName) {
    object PINK : CustomAppTheme("pink", "Pink", pinkLightScheme, pinkDarkScheme)
    object BLUE : CustomAppTheme("blue", "Blue", blueLightScheme, blueDarkScheme)
}

val appThemes: List<AppTheme> = listOf(
    AppTheme.SYSTEM,
    CustomAppTheme.PINK,
    CustomAppTheme.BLUE
)

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.selectedAppThemeFlow.collect { appTheme ->
                _uiState.value = _uiState.value.copy(currentTheme = appTheme)
            }
        }
    }

    fun updateTheme(appTheme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.saveSelectedAppTheme(appTheme)
        }
    }
}

data class ThemeUiState @OptIn(ExperimentalTime::class) constructor(
    val currentTheme: AppTheme = AppTheme.SYSTEM,
)