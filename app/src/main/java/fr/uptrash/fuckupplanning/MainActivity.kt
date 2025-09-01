package fr.uptrash.fuckupplanning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.uptrash.fuckupplanning.ui.login.LoginScreen
import fr.uptrash.fuckupplanning.ui.theme.FuckUpPlanningTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FuckUpPlanningTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                // Navigate to main app screen
                                // navController.navigate("main") {
                                //     popUpTo("login") { inclusive = true }
                                // }
                            }
                        )
                    }

                    // Add other destinations here
                    // composable("main") { MainScreen() }
                }
            }
        }
    }
}