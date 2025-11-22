package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import ui.common.SectionHeader
import ui.common.SixtCard
import ui.common.SixtInput
import ui.common.SixtPrimaryButton
import ui.theme.SixtOrange
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import viewmodels.SearchViewModel


@OptIn(KoinExperimentalAPI::class)
@Composable
fun SearchScreen(
    viewModel: viewmodels.SearchViewModel = koinViewModel(),
    onSearch: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var destination by remember { mutableStateOf("") }
    var pickupDate by remember { mutableStateOf("Oct 24, 10:00 AM") }
    var returnDate by remember { mutableStateOf("Oct 27, 10:00 AM") }

    // Handle side effects
    LaunchedEffect(uiState) {
        if (uiState is ui.state.SearchUiState.Success) {
            val bookingId = (uiState as ui.state.SearchUiState.Success).bookingId
            onSearch(bookingId)
            viewModel.resetState()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.ensureBookingCreated()
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(SixtOrange, Color(0xFFFF8F00))
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "RiderGo",
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White
                        )
                        Text(
                            text = "Premium Car Rental",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    SixtCard {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SectionHeader("Where to?")
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = SixtOrange)
                                Spacer(modifier = Modifier.width(8.dp))
                                SixtInput(
                                    value = destination,
                                    onValueChange = { destination = it },
                                    label = "Pick-up Location"
                                )
                            }

                            SectionHeader("When?")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.DateRange, contentDescription = null, tint = SixtOrange)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    SixtInput(
                                        value = pickupDate,
                                        onValueChange = { pickupDate = it },
                                        label = "Pick-up"
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    SixtInput(
                                        value = returnDate,
                                        onValueChange = { returnDate = it },
                                        label = "Return"
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            SixtPrimaryButton(
                                text = "Find Vehicles",
                                onClick = { viewModel.createBooking(destination) },
                                enabled = uiState !is ui.state.SearchUiState.Loading
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Contextual Intelligence Teaser
                    if (destination.isNotEmpty()) {
                        ContextualInsightCard(destination)
                    }
                }
            }
            
            // Loading Overlay
            if (uiState is ui.state.SearchUiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    ui.common.LoadingIndicator(message = "Creating your booking...")
                }
            }
            
            // Error Overlay
            if (uiState is ui.state.SearchUiState.Error) {
                val errorMsg = (uiState as ui.state.SearchUiState.Error).message
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    SixtCard(modifier = Modifier.padding(32.dp)) {
                        ui.common.ErrorView(
                            message = errorMsg,
                            onRetry = { viewModel.createBooking(destination) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContextualInsightCard(destination: String) {
    SixtCard {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Trip Insights for $destination",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "We've detected mountainous terrain and a chance of snow. We recommend SUVs with 4WD for your safety.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}