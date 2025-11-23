package org.example.ridergo

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import repositories.BookingRepository
import repositories.VehiclesRepository
import ui.common.SlideUpComponent
import ui.screens.MainScreen
import ui.theme.AppTheme
import kotlin.random.Random

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun App(
    bookingRepository: BookingRepository, 
    vehiclesRepository: VehiclesRepository,
    storage: utils.Storage = org.koin.compose.koinInject(),
    userRepository: repositories.UserRepository = org.koin.compose.koinInject()
) {
    var isDarkTheme by remember { mutableStateOf(storage.getPreference("is_dark_theme") != "false") } // Default to true (dark) if not set
    var showEasterEgg by remember { mutableStateOf(false) }
    var isDemoMode by remember { mutableStateOf(storage.getPreference("live_demo_enabled") == "true") }
    var groqApiKey by remember { mutableStateOf(storage.getPreference("groq_api_key") ?: "") }

    val startDestination = if (userRepository.hasProfile()) ui.navigation.Screen.Home.route else ui.navigation.Screen.Signup.route

    fun toggleTheme() {
        if (isDarkTheme) {
            // Trying to switch from Dark to Light
            val chance = Random.nextInt(1, 101) // 1 to 100
            if (chance <= 10) {
                // 10% chance to trigger Easter egg
                showEasterEgg = true
            } else {
                isDarkTheme = false
                storage.savePreference("is_dark_theme", "false")
            }
        } else {
            // Switching from Light to Dark is always allowed
            isDarkTheme = true
            storage.savePreference("is_dark_theme", "true")
        }
    }

    AppTheme(darkTheme = isDarkTheme) {
        MainScreen(
            startDestination = startDestination,
            isDarkTheme = isDarkTheme,
            onToggleTheme = { toggleTheme() },
            isDemoMode = isDemoMode,
            onToggleDemoMode = { 
                val newMode = !isDemoMode
                isDemoMode = newMode
                storage.savePreference("live_demo_enabled", newMode.toString())
            },
            apiKey = groqApiKey,
            onApiKeyChange = { newKey ->
                groqApiKey = newKey
                storage.savePreference("groq_api_key", newKey)
            }
        )

        SlideUpComponent(
            isVisible = showEasterEgg,
            onDismiss = { showEasterEgg = false }
        ) {
            Text("Once you go black, You never go back.")
        }
    }
}
