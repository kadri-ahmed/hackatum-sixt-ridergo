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

@Composable
fun App(bookingRepository: BookingRepository, vehiclesRepository: VehiclesRepository) {
    var isDarkTheme by remember { mutableStateOf(true) } // Default to dark
    var showEasterEgg by remember { mutableStateOf(false) }

    fun toggleTheme() {
        if (isDarkTheme) {
            // Trying to switch from Dark to Light
            val chance = Random.nextInt(1, 101) // 1 to 100
            if (chance <= 10) {
                // 10% chance to trigger Easter egg
                showEasterEgg = true
            } else {
                isDarkTheme = false
            }
        } else {
            // Switching from Light to Dark is always allowed
            isDarkTheme = true
        }
    }

    AppTheme(darkTheme = isDarkTheme) {
        MainScreen(
            isDarkTheme = isDarkTheme,
            onToggleTheme = { toggleTheme() }
        )

        SlideUpComponent(
            isVisible = showEasterEgg,
            onDismiss = { showEasterEgg = false }
        ) {
            Text("Once you go black, You never go back.")
        }
    }
}
