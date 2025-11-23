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

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isOffer: Boolean = false,
    val offerDetails: String? = null,
    val deal: dto.Deal? = null,
    val proposedBooking: dto.SavedBooking? = null,
    val addonNames: List<String> = emptyList(),
    val isSaved: Boolean = false,
    val id: String = kotlin.random.Random.nextLong().toString() // Moved to end to preserve positional args
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
                        onBookClick = { message ->
                            if (message.isSaved) {
                                // Navigate if already saved
                                message.proposedBooking?.let { booking ->
                                    onBookingSaved(booking.id)
                                }
                            } else {
                                // Just save if not saved
                                viewModel.saveBooking(message)
                            }
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
    onBookClick: ((ChatMessage) -> Unit)? = null
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
                
                if (message.deal != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    // Premium Vehicle Card
                    SixtCard(
                        onClick = { onVehicleClick?.invoke(message.deal) },
                        onLongClick = { onLongClickVehicle?.invoke(message.deal) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Image
                            if (message.deal.vehicle.images.isNotEmpty()) {
                                coil3.compose.AsyncImage(
                                    model = message.deal.vehicle.images.first(),
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
                                            text = message.deal.vehicle.brand,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = message.deal.vehicle.model,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "${message.deal.pricing.displayPrice.currency} ${message.deal.pricing.displayPrice.amount}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SixtOrange,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                }
                                
                                // Show proposed details if available
                                if (message.proposedBooking != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    if (message.proposedBooking.protectionPackage != null || message.addonNames.isNotEmpty()) {
                                        Text(
                                            text = "Includes:",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    
                                    if (message.proposedBooking.protectionPackage != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Shield, // Assuming Shield icon exists, or use CheckCircle
                                                contentDescription = null,
                                                tint = SixtOrange,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = message.proposedBooking.protectionPackage.name,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                    
                                    message.addonNames.forEach { addonName ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Add, // Or a generic plus/check icon
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
                                            if (message.isSaved) {
                                                // Navigate to booking detail
                                                message.proposedBooking?.let { booking ->
                                                    onBookClick?.invoke(message) // This will trigger navigation in parent
                                                }
                                            } else {
                                                // Save booking
                                                onBookClick?.invoke(message) 
                                            }
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
                }
            }
        }
    }
}
