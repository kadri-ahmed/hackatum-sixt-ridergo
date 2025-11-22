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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import dto.Deal
import ui.common.SixtPrimaryButton
import ui.theme.SixtOrange
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect

@Composable
fun BookingSummaryScreen(
    viewModel: viewmodels.BookingSummaryViewModel = org.koin.compose.viewmodel.koinViewModel(),
    onConfirm: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBooking()
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is ui.state.BookingSummaryUiState.Loading -> {
                    ui.common.LoadingIndicator(message = "Loading booking details...")
                }
                is ui.state.BookingSummaryUiState.Error -> {
                    ui.common.ErrorView(
                        message = state.message,
                        onRetry = { viewModel.loadBooking() },
                        onCancel = { 
                            // Navigate back to home or previous screen
                            // We can reuse onConfirm which resets to home, or add a specific onCancel
                            onConfirm() 
                        },
                        cancelText = "Back to Home"
                    )
                }
                is ui.state.BookingSummaryUiState.Success -> {
                    val booking = state.booking
                    val vehicle = booking.selectedVehicle?.vehicle
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(SixtOrange.copy(alpha = 0.1f), CircleShape)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = SixtOrange,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Great Choice!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (vehicle != null) {
                            Text(
                                text = "You selected the ${vehicle.brand} ${vehicle.model}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // Vehicle Image
                            if (vehicle.images.isNotEmpty()) {
                                AsyncImage(
                                    model = vehicle.images.first(),
                                    contentDescription = "${vehicle.brand} ${vehicle.model}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                             Text("Vehicle details not available")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (booking.protectionPackages != null) {
                             Text(
                                text = "Protection: ${booking.protectionPackages.name}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        SixtPrimaryButton(
                            text = "Confirm Booking",
                            onClick = { viewModel.confirmBooking(onConfirm) }
                        )
                    }
                }
            }
        }
    }
}
