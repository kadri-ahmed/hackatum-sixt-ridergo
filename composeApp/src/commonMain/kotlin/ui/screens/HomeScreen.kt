package ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dto.Deal
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import dto.UpsellReason
import network.api.SixtApi
import network.api.SixtApiImpl
import repositories.VehiclesRepository
import repositories.VehiclesRepositoryImpl
import ui.components.SwipeableVehicleCard
import utils.Result

@Composable
fun HomeScreen(
    onVehicleSelect: (Deal) -> Unit,
    navigateToProfile: (Int, Boolean) -> Unit,
    navigateToSearch: (String) -> Unit,
    popBackStack: () -> Unit,
    popUpToLogin: () -> Unit,
) {
    // Dependency Injection (Simplified for this step)
    // Dependency Injection (Simplified for this step)
    val client = remember { 
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
        } 
    }
    val api = remember { SixtApiImpl(client) }
    val repository = remember<VehiclesRepository> { VehiclesRepositoryImpl(api) }

    var deals by remember { mutableStateOf<List<Deal>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        when (val result = repository.getAvailableVehicles("mock_booking_id")) {
            is Result.Success -> {
                deals = result.data.deals
                isLoading = false
            }
            is Result.Error -> {
                error = "Failed to load vehicles"
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "WHICH VEHICLE WOULD YOU LIKE TO DRIVE TODAY?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            } else if (deals.isEmpty()) {
                Text("No more vehicles available.", style = MaterialTheme.typography.bodyLarge)
            } else {
                deals.reversed().forEach { deal ->
                    SwipeableVehicleCard(
                        deal = deal,
                        onSwipeLeft = {
                            deals = deals.dropLast(1)
                        },
                        onSwipeRight = {
                            // Handle selection
                            onVehicleSelect(deal)
                            deals = deals.dropLast(1)
                        }
                    )
                }
            }
        }
    }
}