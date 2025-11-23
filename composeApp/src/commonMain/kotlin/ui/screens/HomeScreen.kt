package ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
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
import repositories.VehiclesRepository
import ui.components.SwipeableVehicleCard
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.EditCalendar
import ui.common.SlideUpComponent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import ui.components.VehicleCard
import ui.components.VehicleQuickInfo
import utils.Result

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onVehicleSelect: (Deal) -> Unit,
    navigateToProfile: (Int, Boolean) -> Unit,
    navigateToSearch: (String) -> Unit,
    navigateToTripDetails: () -> Unit,
    popBackStack: () -> Unit,
    popUpToLogin: () -> Unit,
) {
    // Dependency Injection (Simplified for this step)
    // Dependency Injection
    val bookingFlowViewModel: viewmodels.BookingFlowViewModel = org.koin.compose.koinInject()
    val vehiclesRepository: repositories.VehiclesRepository = org.koin.compose.koinInject()
    val bookingRepository: repositories.BookingRepository = org.koin.compose.koinInject()

    var allDeals by remember { mutableStateOf<List<Deal>>(emptyList()) }
    var selectedContext by remember { mutableStateOf<ContextFilter>(ContextFilter.All) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        var bookingId = bookingFlowViewModel.bookingId.value
        if (bookingId == null) {
            when (val result = bookingRepository.createBooking()) {
                is Result.Success -> {
                    bookingId = result.data.id
                    bookingFlowViewModel.setBookingId(bookingId)
                }
                is Result.Error -> {
                    error = "Failed to create booking session"
                    isLoading = false
                    return@LaunchedEffect
                }
            }
        }
        
        if (bookingId != null) {
            when (val result = vehiclesRepository.getAvailableVehicles(bookingId)) {
                is Result.Success -> {
                    allDeals = result.data.deals
                    isLoading = false
                }
                is Result.Error -> {
                    error = "Failed to load vehicles"
                    isLoading = false
                }
            }
        }
    }

    val filteredDeals = remember(allDeals, selectedContext) {
        when (selectedContext) {
            ContextFilter.All -> allDeals
            ContextFilter.Mountain -> allDeals.filter { 
                it.vehicle.groupType.contains("SUV", ignoreCase = true) || 
                it.vehicle.upsellReasons.any { reason -> reason.title.contains("mountain", ignoreCase = true) }
            }
            ContextFilter.City -> allDeals.filter { 
                it.vehicle.groupType.contains("Sedan", ignoreCase = true) || 
                it.vehicle.groupType.contains("Compact", ignoreCase = true) 
            }
            ContextFilter.Family -> allDeals.filter { 
                it.vehicle.passengersCount >= 5 || it.vehicle.bagsCount >= 3
            }
        }
    }

    var isSwipeMode by remember { mutableStateOf(true) }
    
    utils.OnShake {
        isSwipeMode = !isSwipeMode
    }

    var selectedVehicleForInfo by remember { mutableStateOf<Deal?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ... (Header and Chips)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WHICH VEHICLE WOULD YOU LIKE TO DRIVE TODAY?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            androidx.compose.material3.IconButton(onClick = { 
                bookingFlowViewModel.bookingId.value?.let { id -> navigateToSearch(id) } 
            }) {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
            androidx.compose.material3.IconButton(onClick = { navigateToTripDetails() }) {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Default.EditCalendar,
                    contentDescription = "Trip Details"
                )
            }
            androidx.compose.material3.IconButton(onClick = { isSwipeMode = !isSwipeMode }) {
                androidx.compose.material3.Icon(
                    if (isSwipeMode) androidx.compose.material.icons.Icons.Default.List else androidx.compose.material.icons.Icons.Default.ViewCarousel,
                    contentDescription = "Toggle View"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Context Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ContextFilter.values().forEach { filter ->
                ContextChip(
                    text = filter.name,
                    isSelected = selectedContext == filter,
                    onClick = { selectedContext = filter }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            } else if (filteredDeals.isEmpty()) {
                Text("No vehicles match your criteria.", style = MaterialTheme.typography.bodyLarge)
            } else {
                var swipeableDeals by remember(filteredDeals) { mutableStateOf(filteredDeals) }
                
                if (swipeableDeals.isEmpty()) {
                     Text("No more vehicles available.", style = MaterialTheme.typography.bodyLarge)
                } else {
                    androidx.compose.animation.AnimatedContent(
                        targetState = isSwipeMode,
                        transitionSpec = {
                            (androidx.compose.animation.scaleIn() + androidx.compose.animation.fadeIn())
                                .togetherWith(androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut())
                        }
                    ) { targetMode ->
                        if (targetMode) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                swipeableDeals.reversed().forEach { deal ->
                                    androidx.compose.runtime.key(deal.vehicle.id) {
                                        SwipeableVehicleCard(
                                            deal = deal,
                                            onSwipeLeft = {
                                                swipeableDeals = swipeableDeals.drop(1)
                                            },
                                            onSwipeRight = {
                                                // Handle selection
                                                onVehicleSelect(deal)
                                                swipeableDeals = swipeableDeals.drop(1)
                                            },
                                            onLongClick = { selectedVehicleForInfo = deal }
                                        )
                                    }
                                }
                            }
                        } else {
                            androidx.compose.foundation.lazy.LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(swipeableDeals) { deal ->
                                    ui.components.VehicleCard(
                                        deal = deal, 
                                        onSelect = { onVehicleSelect(deal) },
                                        onLongClick = { selectedVehicleForInfo = deal }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            if (selectedVehicleForInfo != null) {
                SlideUpComponent(
                    isVisible = true,
                    onDismiss = { selectedVehicleForInfo = null }
                ) {
                    VehicleQuickInfo(
                        deal = selectedVehicleForInfo!!,
                        onSelect = {
                            onVehicleSelect(selectedVehicleForInfo!!)
                            selectedVehicleForInfo = null
                        }
                    )
                }
            }
        }
    }
}

enum class ContextFilter {
    All, Mountain, City, Family
}

@Composable
fun ContextChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}