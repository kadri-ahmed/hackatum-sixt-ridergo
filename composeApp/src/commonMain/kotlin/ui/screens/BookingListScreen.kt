package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlinx.coroutines.launch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dto.SavedBooking
import repositories.SavedBookingRepository
import ui.theme.SixtOrange
import viewmodels.BookingFlowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(
    onBack: () -> Unit,
    onBookingSelected: (dto.Deal) -> Unit,
    onBookingDetail: (String) -> Unit,
    savedBookingRepository: SavedBookingRepository = org.koin.compose.koinInject(),
    bookingFlowViewModel: BookingFlowViewModel = org.koin.compose.koinInject()
) {
    val savedBookings by savedBookingRepository.getSavedBookings().collectAsState(initial = emptyList())
    
    val confirmedBookings = savedBookings.filter { it.status == dto.BookingStatus.CONFIRMED }
    val draftBookings = savedBookings.filter { it.status == dto.BookingStatus.DRAFT }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings") },
                // Back button removed as it's now a main tab
            )
        }
    ) { paddingValues ->
        if (savedBookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No bookings yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (draftBookings.isNotEmpty()) {
                    item {
                        Text(
                            text = "Unconfirmed / Drafts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(draftBookings) { booking ->
                        SavedBookingCard(
                            booking = booking,
                            onClick = {
                                // Resume booking
                                bookingFlowViewModel.setBookingId(booking.bookingId)
                                bookingFlowViewModel.selectVehicle(booking.vehicle.vehicle.id)
                                if (booking.protectionPackage != null) {
                                    bookingFlowViewModel.setSelectedProtectionPackageId(booking.protectionPackage.id)
                                }
                                booking.addonIds.forEach { bookingFlowViewModel.toggleAddon(it) }
                                bookingFlowViewModel.setModifying(true)
                                onBookingSelected(booking.vehicle)
                            },
                            onDelete = {
                                // Delete booking
                                kotlinx.coroutines.GlobalScope.launch {
                                    savedBookingRepository.deleteBooking(booking.id)
                                }
                            }
                        )
                    }
                }
                
                if (confirmedBookings.isNotEmpty()) {
                    item {
                        Text(
                            text = "Confirmed / Past",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    items(confirmedBookings) { booking ->
                        SavedBookingCard(
                            booking = booking,
                            onClick = {
                                onBookingDetail(booking.id)
                            },
                            isConfirmed = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedBookingCard(
    booking: SavedBooking,
    onClick: () -> Unit,
    isConfirmed: Boolean = false,
    onDelete: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (booking.vehicle.vehicle.images.isNotEmpty()) {
                AsyncImage(
                    model = booking.vehicle.vehicle.images.first(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.LightGray, RoundedCornerShape(8.dp))
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${booking.vehicle.vehicle.brand} ${booking.vehicle.vehicle.model}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total: ${booking.currency}${booking.totalPrice}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SixtOrange,
                    fontWeight = FontWeight.Bold
                )
                if (booking.protectionPackage != null) {
                    Text(
                        text = "+ ${booking.protectionPackage.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isConfirmed) {
                     Text(
                        text = "Confirmed",
                        style = MaterialTheme.typography.labelSmall,
                        color = androidx.compose.ui.graphics.Color.Green,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            if (!isConfirmed) {
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Resume",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
