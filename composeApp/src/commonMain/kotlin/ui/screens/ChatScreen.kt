package ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import ui.common.SixtInput
import ui.theme.SixtOrange
import viewmodels.ChatViewModel
import dto.Deal
import ui.common.SixtCard
import ui.common.SlideUpComponent
import ui.components.VehicleQuickInfo
import ui.common.getCurrencySymbol

data class ChatMessageOption(
    val deal: dto.Deal,
    val proposedBooking: dto.SavedBooking? = null,
    val addonNames: List<String> = emptyList()
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val options: List<ChatMessageOption> = emptyList(),
    val existingBookings: List<dto.SavedBooking> = emptyList(),
    val bookingsToDelete: List<dto.SavedBooking> = emptyList(),
    val deletedBookingIds: Set<String> = emptySet(),
    val isSaved: Boolean = false,
    val id: String = kotlin.random.Random.nextLong().toString()
)

@OptIn(KoinExperimentalAPI::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = koinViewModel(),
    onVehicleSelect: (Deal) -> Unit,
    onBookingSaved: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var selectedVehicle by remember { mutableStateOf<Deal?>(null) }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    androidx.compose.runtime.LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            // Scroll to the last item (messages + loading indicator if present)
            val lastIndex = messages.size + if (isLoading) 0 else -1
            listState.animateScrollToItem(lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RiderGo Assistant") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(onClick = { viewModel.startNewChat() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "New Chat",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(
                        message = message,
                        onLongClickVehicle = { deal ->
                            selectedVehicle = deal
                        },
                        onVehicleClick = onVehicleSelect,
                        onBookClick = { message, option ->
                            if (message.isSaved) {
                                // Navigate if already saved
                                option.proposedBooking?.let { booking ->
                                    onBookingSaved(booking.id)
                                }
                            } else {
                                // Just save if not saved
                                viewModel.saveBooking(message, option)
                            }
                        },
                        onDeleteBooking = { bookingId ->
                            viewModel.deleteBooking(bookingId)
                        }
                    )
                }
                
                // Show loading indicator when waiting for response
                if (isLoading) {
                    item {
                        ChatBubble(
                            ChatMessage("Thinking...", isUser = false)
                        )
                    }
                }
            }

            // Show error message if any
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SixtInput(
                        value = messageText,
                        onValueChange = { messageText = it },
                        label = "Type a message...",
                        enabled = !isLoading
                    )
                }
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank() && !isLoading) {
                            viewModel.sendMessage(messageText)
                            messageText = ""
                        }
                    },
                    enabled = !isLoading && messageText.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(8.dp),
                            color = SixtOrange
                        )
                    } else {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = SixtOrange)
                    }
                }
            }
        }
        
        if (selectedVehicle != null) {
            SlideUpComponent(
                isVisible = true,
                onDismiss = { selectedVehicle = null }
            ) {
                VehicleQuickInfo(
                    deal = selectedVehicle!!,
                    onSelect = {
                        onVehicleSelect(selectedVehicle!!)
                        selectedVehicle = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: ChatMessage, 
    onLongClickVehicle: ((Deal) -> Unit)? = null,
    onVehicleClick: ((Deal) -> Unit)? = null,
    onBookClick: ((ChatMessage, ChatMessageOption) -> Unit)? = null,
    onDeleteBooking: ((String) -> Unit)? = null
) {
    val backgroundColor = if (message.isUser) SixtOrange else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val shape = if (message.isUser) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(backgroundColor)
                .padding(16.dp)
                .widthIn(max = 320.dp)
        ) {
            Column {
                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text, 
                        color = contentColor,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                // Suggested Deletions
                if (message.bookingsToDelete.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Suggested for removal:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    message.bookingsToDelete.forEach { booking ->
                        SixtCard(
                            onClick = { /* Navigate to details? */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = booking.vehicle.vehicle.brand + " " + booking.vehicle.vehicle.model,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                        Text(
                                            text = booking.status.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (booking.status == dto.BookingStatus.CONFIRMED) SixtOrange else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${getCurrencySymbol(booking.currency)}${ui.common.formatPrice(booking.totalPrice)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                if (message.deletedBookingIds.contains(booking.id)) {
                                    androidx.compose.material3.Button(
                                        onClick = {},
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Deleted", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    }
                                } else {
                                    androidx.compose.material3.OutlinedButton(
                                        onClick = { onDeleteBooking?.invoke(booking.id) },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Delete Booking", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                // Existing Bookings List
                if (message.existingBookings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your Bookings:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    message.existingBookings.forEach { booking ->
                        SixtCard(
                            onClick = { /* Navigate to details? */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = booking.vehicle.vehicle.brand + " " + booking.vehicle.vehicle.model,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                        Text(
                                            text = booking.status.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (booking.status == dto.BookingStatus.CONFIRMED) SixtOrange else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${getCurrencySymbol(booking.currency)}${ui.common.formatPrice(booking.totalPrice)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                if (message.deletedBookingIds.contains(booking.id)) {
                                    androidx.compose.material3.Button(
                                        onClick = {},
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Deleted", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    }
                                } else {
                                    androidx.compose.material3.OutlinedButton(
                                        onClick = { onDeleteBooking?.invoke(booking.id) },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Delete Booking", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                // Options (Deals/Proposals)
                if (message.options.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    message.options.forEach { option ->
                        SixtCard(
                            onClick = { onVehicleClick?.invoke(option.deal) },
                            onLongClick = { onLongClickVehicle?.invoke(option.deal) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                // Image
                                if (option.deal.vehicle.images.isNotEmpty()) {
                                    coil3.compose.AsyncImage(
                                        model = option.deal.vehicle.images.first(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                                
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = option.deal.vehicle.brand,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = option.deal.vehicle.model,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                            )
                                        }
                                        val displayPrice = option.proposedBooking?.totalPrice ?: option.deal.pricing.totalPrice.amount
                                        val displayCurrency = option.proposedBooking?.currency ?: option.deal.pricing.totalPrice.currency
                                        
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${getCurrencySymbol(displayCurrency)}${ui.common.formatPrice(displayPrice)}",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = SixtOrange,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                            )
                                            Text(
                                                text = "Total",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    
                                    // Show proposed details if available
                                    if (option.proposedBooking != null) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        if (option.proposedBooking.protectionPackage != null || option.addonNames.isNotEmpty()) {
                                            Text(
                                                text = "Includes:",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                        
                                        if (option.proposedBooking.protectionPackage != null) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Shield,
                                                    contentDescription = null,
                                                    tint = SixtOrange,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = option.proposedBooking.protectionPackage.name,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                        
                                        option.addonNames.forEach { addonName ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = SixtOrange,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = addonName,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        androidx.compose.material3.Button(
                                            onClick = { 
                                                onBookClick?.invoke(message, option)
                                            },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                                containerColor = if (message.isSaved) MaterialTheme.colorScheme.surfaceContainerHighest else SixtOrange,
                                                contentColor = if (message.isSaved) MaterialTheme.colorScheme.onSurface else Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            if (message.isSaved) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Saved", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                            } else {
                                                Text("Save Booking", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
