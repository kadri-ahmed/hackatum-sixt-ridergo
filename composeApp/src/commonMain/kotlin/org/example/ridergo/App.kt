package org.example.ridergo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import repositories.BookingRepository
import repositories.VehiclesRepository

@Composable
fun App(bookingRepository: BookingRepository, vehiclesRepository: VehiclesRepository) {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("RiderGo App")
        }
    }
}
