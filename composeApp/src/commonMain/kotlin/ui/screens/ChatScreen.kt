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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isOffer: Boolean = false,
    val offerDetails: String? = null,
    val deal: dto.Deal? = null
)

@OptIn(KoinExperimentalAPI::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = koinViewModel(),
    onVehicleSelect: (Deal) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var selectedVehicle by remember { mutableStateOf<Deal?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                    onVehicleClick = onVehicleSelect
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
        ui.common.SlideUpComponent(
            isVisible = true,
            onDismiss = { selectedVehicle = null }
        ) {
            ui.components.VehicleQuickInfo(
                deal = selectedVehicle!!,
                onSelect = {
                    onVehicleSelect(selectedVehicle!!)
                    selectedVehicle = null
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: ChatMessage, 
    onLongClickVehicle: ((Deal) -> Unit)? = null,
    onVehicleClick: ((Deal) -> Unit)? = null
) {
    val backgroundColor = if (message.isUser) SixtOrange else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val shape = if (message.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(backgroundColor)
                .padding(12.dp)
                .widthIn(max = 300.dp)
        ) {
            Column {
                Text(text = message.text, color = contentColor)
                if (message.deal != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Compact Vehicle Card
                    SixtCard(
                        onClick = { onVehicleClick?.invoke(message.deal) },
                        onLongClick = { onLongClickVehicle?.invoke(message.deal) }
                    ) {
                        Column {
                            // Image
                            if (message.deal.vehicle.images.isNotEmpty()) {
                                coil3.compose.AsyncImage(
                                    model = message.deal.vehicle.images.first(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${message.deal.vehicle.brand} ${message.deal.vehicle.model}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Text(
                                text = "${message.deal.pricing.displayPrice.currency} ${message.deal.pricing.displayPrice.amount} / day",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SixtOrange
                            )
                        }
                    }
                }
            }
        }
    }
}
