package org.example.ridergo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import network.api.SixtApiImpl
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import ridergo.composeapp.generated.resources.Res
import ridergo.composeapp.generated.resources.compose_multiplatform
import utils.onError
import utils.onSuccess

@Composable
@Preview
fun App(client: SixtApiImpl) {
    MaterialTheme {
        var bookingId by remember { mutableStateOf("") }
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
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
            TextField(
                value = bookingId,
                onValueChange = { bookingId = it },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                placeholder = { Text("Booking ID: $bookingId") },
            )
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
                    Text("Get Booking")
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