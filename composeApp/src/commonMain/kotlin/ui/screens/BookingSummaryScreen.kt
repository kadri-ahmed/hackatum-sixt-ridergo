package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ui.common.SectionHeader
import ui.common.SixtCard
import ui.common.SixtPrimaryButton
import ui.state.BookingSummaryUiState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import ui.common.ErrorView
import ui.common.LoadingIndicator
import ui.theme.SixtOrange
import viewmodels.BookingSummaryViewModel


@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun BookingSummaryScreen(
    viewModel: BookingSummaryViewModel = koinViewModel(),
    onBack: () -> Unit,
    onConfirmBooking: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBooking()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Summary") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Total",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        // In a real app, we would calculate this from the state
                        Text(
                            "€ 405.00",
                            style = MaterialTheme.typography.headlineSmall,
                            color = SixtOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    SixtPrimaryButton(
                        text = "Confirm Booking",
                        onClick = {
                            viewModel.confirmBooking(onConfirmBooking)
                        },
                        enabled = uiState is BookingSummaryUiState.Success
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is BookingSummaryUiState.Loading -> {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        message = "Preparing your summary..."
                    )
                }
                is BookingSummaryUiState.Error -> {
                    ErrorView(
                        modifier = Modifier.align(Alignment.Center),
                        message = state.message,
                        onRetry = { viewModel.loadBooking() }
                    )
                }
                is BookingSummaryUiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Vehicle Section
                        item {
                            SectionHeader("Vehicle")
                            SixtCard {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Placeholder for image
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.DirectionsCar, contentDescription = null, tint = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            "BMW X5", // Hardcoded for now as BookingDto structure might vary
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Premium SUV",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Dates & Location
                        item {
                            SectionHeader("Trip Details")
                            SixtCard {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = SixtOrange, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Pick-up", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                            Text("Munich Airport", style = MaterialTheme.typography.bodyMedium)
                                            Text("Oct 24, 10:00 AM", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = SixtOrange, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Return", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                            Text("Munich Airport", style = MaterialTheme.typography.bodyMedium)
                                            Text("Oct 27, 10:00 AM", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }

                        // Protection
                        item {
                            SectionHeader("Protection")
                            SixtCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Smart Protection", style = MaterialTheme.typography.titleMedium)
                                        Text("Reduced excess", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                    Text("€ 45.00", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Price Breakdown
                        item {
                            SectionHeader("Price Breakdown")
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Vehicle Rental (3 days)", style = MaterialTheme.typography.bodyMedium)
                                    Text("€ 360.00", style = MaterialTheme.typography.bodyMedium)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Protection Package", style = MaterialTheme.typography.bodyMedium)
                                    Text("€ 45.00", style = MaterialTheme.typography.bodyMedium)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Taxes & Fees", style = MaterialTheme.typography.bodyMedium)
                                    Text("Included", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun PriceRow(label: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(amount, style = MaterialTheme.typography.bodyMedium)
    }
}
