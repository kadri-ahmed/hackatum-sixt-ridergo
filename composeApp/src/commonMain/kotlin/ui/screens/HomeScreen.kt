package ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import ui.components.SwipeableChatCard
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
import org.jetbrains.compose.resources.painterResource
import ridergo.composeapp.generated.resources.Res
import ridergo.composeapp.generated.resources.cleveride

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onVehicleSelect: (Deal) -> Unit,
    navigateToProfile: (Int, Boolean) -> Unit,
    navigateToSearch: (String) -> Unit,
    navigateToTripDetails: () -> Unit,
    navigateToChat: () -> Unit,
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

    val homeVisitCount by bookingFlowViewModel.homeVisitCount.collectAsState()

    LaunchedEffect(Unit) {
        bookingFlowViewModel.incrementHomeVisitCount()
        var currentBookingId = bookingFlowViewModel.bookingId.value
        var retryCount = 0
        val maxRetries = 1

        while (retryCount <= maxRetries) {
            if (currentBookingId == null) {
                when (val result = bookingRepository.createBooking()) {
                    is Result.Success -> {
                        currentBookingId = result.data.id
                        bookingFlowViewModel.setBookingId(currentBookingId)
                    }
                    is Result.Error -> {
                        error = "Failed to create booking session"
                        isLoading = false
                        return@LaunchedEffect
                    }
                }
            }

            if (currentBookingId != null) {
                when (val result = vehiclesRepository.getAvailableVehicles(currentBookingId)) {
                    is Result.Success -> {
                        allDeals = result.data.deals
                        isLoading = false
                        return@LaunchedEffect
                    }
                    is Result.Error -> {
                        if (retryCount < maxRetries) {
                            println("Booking $currentBookingId invalid or expired, retrying...")
                            bookingFlowViewModel.clearBooking()
                            currentBookingId = null
                            retryCount++
                        } else {
                            error = "Failed to load vehicles"
                            isLoading = false
                            return@LaunchedEffect
                        }
                    }
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
    
    // Chat card logic: Show on 1st visit, then every 3rd visit (1, 4, 7...)
    // Note: homeVisitCount starts at 0, increments to 1 on first composition
    val shouldShowChatCard = (homeVisitCount % 3 == 1)
    var isChatCardDismissed by remember { mutableStateOf(false) }
    
    // Reset dismissal when the frequency condition changes (e.g. new visit)
    LaunchedEffect(shouldShowChatCard) {
        if (shouldShowChatCard) {
            isChatCardDismissed = false
        }
    }

    val showChatCard = shouldShowChatCard && !isChatCardDismissed

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
            // Use SVG logo - adapts to dark/light mode
            // Logo is black, so in dark mode it should be white, in light mode it should be black
            val logoColor = MaterialTheme.colorScheme.onSurface // Adapts automatically: white in dark mode, black in light mode
            
            Image(
                painter = painterResource(Res.drawable.cleveride),
                contentDescription = "Cleveride Logo",
                modifier = Modifier
                    .height(19.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(logoColor)
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
                                // Render vehicle cards first (bottom of stack)
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
                                            onLongClick = { selectedVehicleForInfo = deal },
                                            shouldAnimate = !(showChatCard && selectedContext == ContextFilter.All)
                                        )
                                    }
                                }
                                
                                // Render chat card last so it appears on top
                                if (showChatCard && selectedContext == ContextFilter.All) {
                                    androidx.compose.runtime.key("chat_card") {
                                        SwipeableChatCard(
                                            onSwipeLeft = {
                                                isChatCardDismissed = true
                                            },
                                            onSwipeRight = {
                                                navigateToChat()
                                                isChatCardDismissed = true
                                            }
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