package org.example.ridergo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import repositories.BookingRepository
import repositories.VehiclesRepository
import utils.NetworkError
import utils.Result

@Composable
fun App(bookingRepository: BookingRepository, vehiclesRepository: VehiclesRepository) {
    MaterialTheme {
        Homepage(bookingRepository = bookingRepository)
    }
}

@Composable
fun Homepage(bookingRepository: BookingRepository) {
    var isLoading by remember { mutableStateOf(false) }
    var bookingId by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<NetworkError?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Cleveride",
                style = MaterialTheme.typography.headlineLarge
            )
            
            Text(
                text = "Start your booking session",
                style = MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = {
                    isLoading = true
                    error = null
                    bookingId = null
                    coroutineScope.launch {
                        when (val result = bookingRepository.createBooking()) {
                            is Result.Success -> {
                                bookingId = result.data.id
                                isLoading = false
                            }
                            is Result.Error -> {
                                error = result.error
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text("Creating...")
                } else {
                    Text("Start Booking Session")
                }
            }

            bookingId?.let { id ->
                Text(
                    text = "Booking created! ID: $id",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            error?.let { networkError ->
                Text(
                    text = "Error: ${networkError.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
