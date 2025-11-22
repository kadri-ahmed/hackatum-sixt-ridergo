package org.example.ridergo

import androidx.compose.runtime.Composable
import repositories.BookingRepository
import repositories.VehiclesRepository
import ui.screens.MainScreen
import ui.theme.AppTheme

@Composable
fun App(bookingRepository: BookingRepository, vehiclesRepository: VehiclesRepository) {
    AppTheme {
        MainScreen()
    }
}
