package org.example.ridergo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import repositories.BookingRepository
import repositories.VehiclesRepository
import ui.screens.BookingSummaryScreen
import ui.screens.ProtectionScreen
import ui.screens.SearchScreen
import ui.screens.VehicleListScreen
import ui.theme.AppTheme

enum class Screen {
    Search,
    VehicleList,
    Protection,
    BookingSummary
}

@Composable
fun App(bookingRepository: BookingRepository, vehiclesRepository: VehiclesRepository) {
    var currentScreen by remember { mutableStateOf(Screen.Search) }

    AppTheme {
        when (currentScreen) {
            Screen.Search -> {
                SearchScreen(
                    onSearch = { destination ->
                        // In a real app, we would pass the destination to the ViewModel/Repository
                        currentScreen = Screen.VehicleList
                    }
                )
            }
            Screen.VehicleList -> {
                VehicleListScreen(
                    onBack = { currentScreen = Screen.Search },
                    onVehicleSelected = { deal ->
                        // Store selected vehicle
                        currentScreen = Screen.Protection
                    }
                )
            }
            Screen.Protection -> {
                ProtectionScreen(
                    onBack = { currentScreen = Screen.VehicleList },
                    onConfirm = {
                        currentScreen = Screen.BookingSummary
                    }
                )
            }
            Screen.BookingSummary -> {
                BookingSummaryScreen(
                    onBack = { currentScreen = Screen.Protection },
                    onConfirmBooking = {
                        // Reset flow or show success
                        currentScreen = Screen.Search
                    }
                )
            }
        }
    }
}
