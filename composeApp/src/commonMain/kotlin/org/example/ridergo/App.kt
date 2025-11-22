package org.example.ridergo

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import navigation.SetupNavGraph
import repositories.BookingRepository
import repositories.VehiclesRepository
import ui.theme.AppTheme

@Composable
fun App(bookingRepository: BookingRepository, vehiclesRepository: VehiclesRepository) {
    AppTheme {
        val navController = rememberNavController()
        SetupNavGraph(navController = navController)
    }
}
