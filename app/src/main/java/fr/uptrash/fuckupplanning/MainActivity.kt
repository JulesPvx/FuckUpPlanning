package fr.uptrash.fuckupplanning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import fr.uptrash.fuckupplanning.ui.calendar.CalendarScreen
import fr.uptrash.fuckupplanning.ui.theme.FuckUpPlanningTheme
import fr.uptrash.fuckupplanning.ui.theme.ThemeViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val uiState by themeViewModel.uiState.collectAsStateWithLifecycle()

            FuckUpPlanningTheme(
                appTheme = uiState.currentTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalendarScreen(
                        modifier = Modifier.fillMaxSize(),
                        themeViewMode = themeViewModel
                    )
                }
            }
        }
    }
}