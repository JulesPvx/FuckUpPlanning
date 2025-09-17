package fr.uptrash.fuckupplanning

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
import dagger.hilt.android.AndroidEntryPoint
import fr.uptrash.fuckupplanning.ui.auth.AuthViewModel
import fr.uptrash.fuckupplanning.ui.calendar.CalendarScreen
import fr.uptrash.fuckupplanning.ui.calendar.CalendarViewModel
import fr.uptrash.fuckupplanning.ui.calendar.RestaurantMenuView
import fr.uptrash.fuckupplanning.ui.calendar.SettingsView
import fr.uptrash.fuckupplanning.ui.homework.HomeworkScreen
import fr.uptrash.fuckupplanning.ui.theme.FuckUpPlanningTheme
import fr.uptrash.fuckupplanning.ui.theme.ThemeMode
import fr.uptrash.fuckupplanning.ui.theme.ThemeViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )

        setContent {
            val calendarViewModel: CalendarViewModel = hiltViewModel()
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val authViewModel: AuthViewModel = hiltViewModel()

            val themeUiState by themeViewModel.uiState.collectAsStateWithLifecycle()
            val calendarUiState by calendarViewModel.uiState.collectAsStateWithLifecycle()
            val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

            // Initialize anonymous authentication if user is not already authenticated
            LaunchedEffect(Unit) {
                if (!authUiState.isAuthenticated) {
                    Log.d("MainActivity", "User not authenticated, signing in anonymously...")
                    authViewModel.signInAnonymously()
                }
            }

            // Log authentication state changes
            LaunchedEffect(authUiState.isAuthenticated) {
                if (authUiState.isAuthenticated) {
                    Log.d("MainActivity", "User authenticated: ${authUiState.user?.uid}")
                    Log.d("MainActivity", "Is anonymous: ${authUiState.user?.isAnonymous}")
                } else {
                    Log.d("MainActivity", "User not authenticated")
                }
            }

            val navController = rememberNavController()
            val startDestination = Destination.CALENDAR
            var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

            LaunchedEffect(themeUiState.themeMode) {
                Log.d("MainActivity", "Theme mode changed: ${themeUiState.themeMode}")
            }

            FuckUpPlanningTheme(
                appTheme = themeUiState.currentTheme,
                darkTheme = when (themeUiState.themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        stringResource(R.string.app_name),
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                actions = {
                                    // Menu button for restaurant
                                    IconButton(onClick = { calendarViewModel.showMenu() }) {
                                        Icon(
                                            Icons.Default.LocalDining,
                                            contentDescription = stringResource(R.string.restaurant_menu)
                                        )
                                    }

                                    IconButton(onClick = { calendarViewModel.showSettings() }) {
                                        Icon(
                                            Icons.Default.Settings,
                                            contentDescription = stringResource(R.string.settings)
                                        )
                                    }
                                    IconButton(onClick = {
                                        calendarViewModel.loadEvents()
                                    }) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = stringResource(R.string.refresh)
                                        )
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                                Destination.entries.forEachIndexed { index, destination ->
                                    NavigationBarItem(
                                        selected = selectedDestination == index,
                                        onClick = {
                                            navController.navigate(route = destination.route)
                                            selectedDestination = index
                                        },
                                        icon = {
                                            Icon(
                                                destination.icon,
                                                contentDescription = destination.contentDescription
                                            )
                                        },
                                        label = { Text(text = stringResource(destination.label)) }
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
                        NavHost(
                            navController = navController,
                            startDestination = startDestination.route,
                        ) {
                            composable(
                                route = Destination.CALENDAR.route
                            ) {
                                CalendarScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    paddingValues = paddingValues,
                                    calendarViewModel = calendarViewModel,
                                    themeViewModel = themeViewModel
                                )
                            }

                            composable(
                                route = Destination.HOMEWORK.route
                            ) {
                                HomeworkScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    paddingValues = paddingValues
                                )
                            }
                        }
                    }
                }

                // Settings Modal
                if (calendarUiState.showSettings) {
                    ModalBottomSheet(
                        onDismissRequest = { calendarViewModel.dismissSettings() },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentWindowInsets = { WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
                    ) {
                        SettingsView(
                            selectedThemeMode = themeUiState.themeMode,
                            selectedTheme = themeUiState.currentTheme,
                            selectedTPGroup = calendarUiState.selectedTPGroup,
                            selectedMMIYear = calendarUiState.selectedMMIYear,
                            onTPGroupChange = { calendarViewModel.selectTPGroup(it) },
                            onMMIYearChange = { calendarViewModel.selectMMIYear(it) },
                            onThemeChange = { themeViewModel.updateTheme(it) },
                            onThemeModeChange = { themeViewModel.updateThemeMode(it) },
                            onDismiss = { calendarViewModel.dismissSettings() }
                        )
                    }
                }

                // Restaurant Menu Modal
                if (calendarUiState.showMenu) {
                    ModalBottomSheet(
                        onDismissRequest = { calendarViewModel.dismissMenu() },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentWindowInsets = { WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
                    ) {
                        RestaurantMenuView(
                            menu = calendarUiState.restaurantMenu,
                            isLoading = calendarUiState.isMenuLoading,
                            error = calendarUiState.menuError,
                            onDismiss = { calendarViewModel.dismissMenu() },
                            onRefresh = { calendarViewModel.showMenu(forceRefresh = true) }
                        )
                    }
                }
            }
        }
    }
}