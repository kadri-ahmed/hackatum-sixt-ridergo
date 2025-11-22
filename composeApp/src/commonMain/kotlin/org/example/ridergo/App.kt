package org.example.ridergo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dto.BookingDto
import kotlinx.coroutines.launch
import network.api.SixtApiImpl
import org.jetbrains.compose.ui.tooling.preview.Preview
import utils.onError
import utils.onSuccess

@Composable
@Preview
fun App(client: SixtApiImpl) {
    MaterialTheme {
        var bookingId by remember { mutableStateOf("") }
        var booking by remember { mutableStateOf<BookingDto?>(null) }
        var totalAvailableVehicles by remember { mutableStateOf<Int>(0) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        val scope = rememberCoroutineScope()

        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null

                    client.createBooking()
                        .onSuccess {
                            bookingId = it.id
                            isLoading = false
                        }
                        .onError {
                            errorMessage = "Failed to retrieve Booking " + it.toString()
                            isLoading = false
                        }
                }
            }) {
                Text("Create Booking")
            }

            if (bookingId.isNotEmpty()) {
                Text("ID: $bookingId")
                Button(onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null

                        client.getBooking(bookingId)
                            .onSuccess {
                                booking = it
                                isLoading = false
                            }
                            .onError {
                                errorMessage = it.toString()
                                isLoading = false
                            }
                    }
                }) {
                    Text("Get Booking")
                }
            }

            if (booking != null) {
                Text("Booking: $booking")
            }

            Button(onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null

                    client.getAvailableVehicles(bookingId)
                        .onSuccess {
                            totalAvailableVehicles = it.totalVehicles
                            isLoading = false
                        }
                        .onError {
                            errorMessage = it.toString()
                            isLoading = false
                        }
                }
            }) {
                if(isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(15.dp),
                        strokeWidth = 1.dp,
                        color = Color.White
                    )
                } else {
                    Text("Get Available Vehicles")
                }
            }

            Text("Available Vehicles: $totalAvailableVehicles")

            errorMessage?.let { Text(
                text = it,
                color = Color.Red
            )}
        }
    }
}