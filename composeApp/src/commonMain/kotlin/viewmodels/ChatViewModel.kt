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
import utils.Storage

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val storage: utils.Storage,
    private val vehiclesRepository: repositories.VehiclesRepository
) : ViewModel() {

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

    private var availableVehicles: List<dto.Deal> = emptyList()

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
                // Fetch available vehicles if not already cached
                if (availableVehicles.isEmpty()) {
                    val result = vehiclesRepository.getAvailableVehicles("mock_booking_id")
                    if (result is utils.Result.Success) {
                        availableVehicles = result.data.deals
                    }
                }

                // Construct system prompt with vehicle list
                val vehicleListString = availableVehicles.joinToString("\n") { deal ->
                    "- ${deal.vehicle.brand} ${deal.vehicle.model} (${deal.pricing.displayPrice.currency} ${deal.pricing.displayPrice.amount}/day)"
                }

                val systemMessage = GroqMessage(
                    role = "system",
                    content = """
                        You are a helpful assistant for RiderGo, a premium car rental service. 
                        You help users find vehicles. 
                        
                        Here is the list of ONLY available vehicles you can recommend:
                        $vehicleListString
                        
                        Rules:
                        1. ONLY recommend vehicles from this list. Do not make up cars.
                        2. When recommending a vehicle, mention its full name (Brand + Model) clearly so I can show a card.
                        3. If the user asks for a car not on the list, politely say it's not available and suggest a similar one from the list.
                        4. Keep responses concise and helpful.
                    """.trimIndent()
                )
                
                val groqMessages = listOf(systemMessage) + _messages.value.map { msg ->
                    GroqMessage(
                        role = if (msg.isUser) "user" else "assistant",
                        content = msg.text
                    )
                }
                
                val isLiveDemo = storage.getPreference("live_demo_enabled") == "true"
                val apiKey = if (isLiveDemo) storage.getPreference("groq_api_key") else null

                when (val result = chatRepository.sendMessage(groqMessages, apiKey)) {
                    is utils.Result.Success -> {
                        val assistantResponse = result.data
                        
                        // Try to find a matching vehicle in the response
                        val matchingDeal = findMatchingVehicle(assistantResponse)
                        
                        _messages.value = _messages.value + ChatMessage(
                            text = assistantResponse, 
                            isUser = false,
                            isOffer = matchingDeal != null,
                            deal = matchingDeal
                        )
                    }
                    is utils.Result.Error -> {
                        val errorMsg = when (result.error) {
                            utils.NetworkError.NO_INTERNET -> "No internet connection. Please check your network."
                            utils.NetworkError.UNAUTHORIZED -> "API key is invalid or missing. Please check your configuration."
                            utils.NetworkError.BAD_REQUEST -> "Invalid request. Please check your API key and settings."
                            utils.NetworkError.SERVER_ERROR -> "Server error. Please try again later."
                            else -> "Failed to get response (Error: ${result.error}). Please try again."
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

    private suspend fun findMatchingVehicle(text: String): dto.Deal? {
        // Use cached vehicles if available, otherwise fetch
        if (availableVehicles.isEmpty()) {
             val result = vehiclesRepository.getAvailableVehicles("mock_booking_id")
             if (result is utils.Result.Success) {
                 availableVehicles = result.data.deals
             }
        }
        
        // Check if any vehicle brand+model is mentioned in the text
        return availableVehicles.firstOrNull { deal ->
            val fullName = "${deal.vehicle.brand} ${deal.vehicle.model}"
            text.contains(fullName, ignoreCase = true) || 
            text.contains(deal.vehicle.model, ignoreCase = true)
        }
    }
}
