package ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import ui.theme.SixtOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingSummaryScreen(
    onBack: () -> Unit,
    onConfirmBooking: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review & Book") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // Vehicle Details
                SixtCard {
                    SectionHeader("Vehicle")
                    Text(
                        text = "BMW X5",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SUV • Automatic • 5 Seats",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Dates & Location
                SixtCard {
                    SectionHeader("Trip Details")
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("Pick-up", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Text("Oct 24, 10:00 AM", style = MaterialTheme.typography.bodyMedium)
                            Text("Munich Airport", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Return", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Text("Oct 27, 10:00 AM", style = MaterialTheme.typography.bodyMedium)
                            Text("Munich Airport", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Protection
                SixtCard {
                    SectionHeader("Protection")
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Smart Protection", style = MaterialTheme.typography.bodyMedium)
                        Text("USD 45.00", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }

                // Price Breakdown
                SixtCard {
                    SectionHeader("Price Breakdown")
                    PriceRow("Vehicle Rental (3 days)", "USD 360.00")
                    PriceRow("Smart Protection", "USD 45.00")
                    PriceRow("Taxes & Fees", "USD 40.50")
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("USD 445.50", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SixtOrange)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SixtPrimaryButton(
                    text = "Confirm Booking",
                    onClick = onConfirmBooking
                )
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
