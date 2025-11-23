package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dto.SavedBooking
import repositories.SavedBookingRepository
import ui.common.SixtCard
import ui.theme.SixtOrange
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun BookingDetailScreen(
    bookingId: String,
    onBack: () -> Unit,
    savedBookingRepository: SavedBookingRepository = org.koin.compose.koinInject()
) {
    val savedBookings by savedBookingRepository.getSavedBookings().collectAsState(initial = emptyList())
    val booking = savedBookings.find { it.id == bookingId }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (savedBookings.isEmpty()) {
            ui.common.LoadingIndicator(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                message = "Loading booking details..."
            )
        } else if (booking == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Booking not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Booking ID: ${booking.bookingId}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    val statusColor = if (booking.status == dto.BookingStatus.CONFIRMED) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    val statusTextColor = if (booking.status == dto.BookingStatus.CONFIRMED) Color(0xFF2E7D32) else Color(0xFFE65100)
                    val statusText = if (booking.status == dto.BookingStatus.CONFIRMED) "CONFIRMED" else "DRAFT"
                    
                    Surface(
                        color = statusColor,
                        contentColor = statusTextColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Vehicle Card
                SixtCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (booking.vehicle.vehicle.images.isNotEmpty()) {
                            AsyncImage(
                                model = booking.vehicle.vehicle.images.first(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "${booking.vehicle.vehicle.brand} ${booking.vehicle.vehicle.model}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = booking.vehicle.vehicle.groupType,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Details Section
                SixtCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Trip Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        DetailRow("Protection", booking.protectionPackage?.name ?: "None")
                        
                        if (booking.addonIds.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Addons:", fontWeight = FontWeight.SemiBold)
                            booking.addonIds.forEach { addonId ->
                                Text(
                                    text = "• $addonId", // Ideally map ID to name
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Price", fontWeight = FontWeight.Bold)
                            Text(
                                 text = "${booking.currency} ${booking.totalPrice}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SixtOrange
                            )
                        }
                    }
                }

                if (booking.status == dto.BookingStatus.DRAFT) {
                    var isConfirming by remember { mutableStateOf(false) }
                    
                    Button(
                        onClick = {
                            isConfirming = true
                            scope.launch {
                                // Simulate network delay
                                kotlinx.coroutines.delay(1500)
                                val confirmedBooking = booking.copy(status = dto.BookingStatus.CONFIRMED)
                                savedBookingRepository.saveBooking(confirmedBooking)
                                isConfirming = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SixtOrange),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isConfirming
                    ) {
                        if (isConfirming) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Confirm Booking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // QR Code Placeholder for Confirmed bookings
                    SixtCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(Color.Black, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                // Mock QR Code visual
                                Box(
                                    modifier = Modifier
                                        .size(180.dp)
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Scan at counter",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}
