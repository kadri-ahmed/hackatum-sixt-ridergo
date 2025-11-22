package org.example.ridergo

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import navigation.SetupNavGraph
import org.koin.compose.koinInject
import repositories.BookingRepository
import repositories.PreferencesRepository
import repositories.VehiclesRepository
import ui.theme.AppTheme
import utils.ThemeMode

@Composable
fun App(bookingRepository: BookingRepository, vehiclesRepository: VehiclesRepository) {
    val preferencesRepository: PreferencesRepository = koinInject()
    val themeMode by preferencesRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    AppTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()
        SetupNavGraph(navController = navController)
    }
}
