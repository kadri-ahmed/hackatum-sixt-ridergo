package repositories

import dto.GroqMessage
import network.api.GroqApi
import utils.NetworkError
import utils.Result

import ui.screens.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatRepositoryImpl(private val groqApi: GroqApi) : ChatRepository {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("Hello! I'm your intelligent assistant. I can help you find a car and customize your booking with protection packages and addons.", isUser = false)
        )
    )
    override val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    override fun addMessage(message: ChatMessage) {
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(message)
        _messages.value = currentMessages
    }

    override fun updateMessage(message: ChatMessage) {
        val currentMessages = _messages.value.toMutableList()
        val index = currentMessages.indexOfFirst { it.id == message.id }
        if (index != -1) {
            currentMessages[index] = message
            _messages.value = currentMessages
        }
    }

    override fun clearMessages() {
        _messages.value = listOf(
            ChatMessage("Hello! I'm your intelligent assistant. I can help you find a car and customize your booking with protection packages and addons.", isUser = false)
        )
    }

    override suspend fun sendMessage(messages: List<GroqMessage>, apiKey: String?): Result<String, NetworkError> {
        return when (val result = groqApi.sendChatMessage(messages, apiKey)) {
            is Result.Success -> {
                val response = result.data
                val assistantMessage = response.choices.firstOrNull()?.message?.content
                if (assistantMessage != null) {
                    Result.Success(assistantMessage)
                } else {
                    Result.Error(NetworkError.UNKNOWN)
                }
            }
            is Result.Error -> Result.Error(result.error)
        }
    }
}
