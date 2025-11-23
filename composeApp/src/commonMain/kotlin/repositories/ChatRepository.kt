package repositories

import dto.GroqMessage
import network.api.GroqApi
import utils.NetworkError
import utils.Result

import ui.screens.ChatMessage
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    suspend fun sendMessage(messages: List<GroqMessage>, apiKey: String? = null): Result<String, NetworkError>
    
    val messages: StateFlow<List<ChatMessage>>
    fun addMessage(message: ChatMessage)
    fun updateMessage(message: ChatMessage)
    fun clearMessages()
}
