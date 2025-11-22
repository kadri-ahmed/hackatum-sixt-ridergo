package viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dto.GroqMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import repositories.ChatRepository
import ui.screens.ChatMessage

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("Hello! I'm your intelligent assistant. How can I help you today?", isUser = false)
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank() || _isLoading.value) return

        // Add user message
        val userMessage = ChatMessage(text, isUser = true)
        _messages.value = _messages.value + userMessage

        // Show loading indicator
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                // Convert chat messages to Groq format
                val groqMessages = _messages.value.map { msg ->
                    GroqMessage(
                        role = if (msg.isUser) "user" else "assistant",
                        content = msg.text
                    )
                }

                when (val result = chatRepository.sendMessage(groqMessages)) {
                    is utils.Result.Success -> {
                        val assistantResponse = result.data
                        _messages.value = _messages.value + ChatMessage(assistantResponse, isUser = false)
                    }
                    is utils.Result.Error -> {
                        val errorMsg = when (result.error) {
                            utils.NetworkError.NO_INTERNET -> "No internet connection. Please check your network."
                            utils.NetworkError.UNAUTHORIZED -> "API key is invalid or missing. Please check your configuration."
                            utils.NetworkError.SERVER_ERROR -> "Server error. Please try again later."
                            else -> "Failed to get response. Please try again."
                        }
                        _errorMessage.value = errorMsg
                        // Optionally add error message as a bot message
                        _messages.value = _messages.value + ChatMessage(
                            errorMsg,
                            isUser = false
                        )
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "An unexpected error occurred: ${e.message}"
                _errorMessage.value = errorMsg
                _messages.value = _messages.value + ChatMessage(
                    "Sorry, I encountered an error. Please try again.",
                    isUser = false
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
