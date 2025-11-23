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

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import dto.BookingStatus
import dto.SavedBooking
import kotlin.time.Clock

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val storage: utils.Storage,
    private val vehiclesRepository: repositories.VehiclesRepository,
    private val bookingRepository: repositories.BookingRepository,
    private val savedBookingRepository: repositories.SavedBookingRepository,
    private val userRepository: repositories.UserRepository
) : ViewModel() {

    val messages: StateFlow<List<ChatMessage>> = chatRepository.messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var availableVehicles: List<dto.Deal> = emptyList()
    private var protectionPackages: List<dto.ProtectionPackageDto> = emptyList()
    // Mock addons for now as they are hardcoded in UI
    private val addons = listOf(
        "GPS" to "GPS Navigation",
        "Baby Seat" to "Baby Seat",
        "Additional Driver" to "Additional Driver"
    )

    fun startNewChat() {
        chatRepository.clearMessages()
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isLoading.value) return

        // Add user message
        val userMessage = ChatMessage(text, isUser = true)
        chatRepository.addMessage(userMessage)

        // Show loading indicator
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                // Fetch data if needed
                if (availableVehicles.isEmpty()) {
                    val result = vehiclesRepository.getAvailableVehicles("mock_booking_id")
                    if (result is utils.Result.Success) {
                        availableVehicles = result.data.deals
                    }
                }
                if (protectionPackages.isEmpty()) {
                    val result = bookingRepository.getProtectionPackages("mock_booking_id")
                    if (result is utils.Result.Success) {
                        protectionPackages = result.data.protectionPackages
                    }
                }

                // Construct system prompt
                val vehicleListString = availableVehicles.joinToString("\n") { deal ->
                    "- ID: ${deal.vehicle.id}, Name: ${deal.vehicle.brand} ${deal.vehicle.model}, Price: ${deal.pricing.displayPrice.currency} ${deal.pricing.displayPrice.amount}/day"
                }
                val protectionListString = protectionPackages.joinToString("\n") { pkg ->
                    "- ID: ${pkg.id}, Name: ${pkg.name}, Price: ${pkg.price.displayPrice.currency} ${pkg.price.displayPrice.amount}/day"
                }
                val addonListString = addons.joinToString("\n") { (id, name) ->
                    "- ID: $id, Name: $name"
                }

                val userProfile = userRepository.getProfile()
                val userContext = if (userProfile != null) {
                    """
                    User Profile:
                    - Name: ${userProfile.name}
                    - Age: ${userProfile.age}
                    - License Preference: ${userProfile.licenseType}
                    - Travel Style: ${userProfile.travelPreference}
                    
                    Use this information to personalize your recommendations. For example, if they prefer Manual, suggest manual cars. If they are on business, suggest suitable cars.
                    """.trimIndent()
                } else {
                    "User profile is unknown. Ask for preferences if needed."
                }

                val systemMessage = GroqMessage(
                    role = "system",
                    content = """
                        You are a helpful assistant for RiderGo, a premium car rental service. 
                        You help users find vehicles and customize their booking.
                        
                        $userContext
                        
                        Available Vehicles:
                        $vehicleListString
                        
                        Available Protection Packages:
                        $protectionListString
                        
                        Available Addons:
                        $addonListString
                        
                        Rules:
                        1. You are a consultative sales assistant. Your goal is to find the *perfect* car for the user, not just any car.
                        2. If the user's request is vague (e.g., "I need a car"), ask clarifying questions BEFORE showing any vehicles.
                        3. Good questions to ask:
                           - "How many passengers will be travelling?"
                           - "How much luggage do you have?"
                           - "Do you prefer automatic or manual transmission?"
                           - "Are you looking for a budget option or something premium?"
                        4. ONLY use the "show_vehicle" action when you have enough information to make a strong recommendation, or if the user explicitly asks for a specific car.
                        5. Recommend vehicles, protection, and addons from the lists above.
                        6. If you want to SHOW a vehicle to the user (e.g. when recommending it), you MUST output a JSON block with action "show_vehicle".
                        7. If the user wants to SAVE or BOOK a trip, you MUST output a JSON block with action "save_booking".
                        8. The JSON format is:
                        ```json
                        {
                            "action": "show_vehicle" OR "save_booking",
                            "vehicle_id": "VEHICLE_ID",
                            "protection_package_id": "PROTECTION_ID (optional, for save_booking)",
                            "addon_ids": ["ADDON_ID_1"] (optional, for save_booking)
                        }
                        ```
                        9. If the user just wants information and you are NOT recommending a specific vehicle to look at, do NOT output JSON.
                        10. Keep responses concise and helpful.
                        11. IMPORTANT: NEVER mention internal IDs (like "VEHICLE_123" or "ADDON_456") in your text response. Use the names (e.g., "BMW X5", "GPS Navigation").
                        12. Present options in a readable, natural manner.
                        13. ALWAYS put the JSON block at the very end of your response.
                        14. Do NOT say "Here is the JSON" or "I will show you the vehicle". Just write your helpful message and append the JSON block silently.
                        15. The user should NOT know that JSON is being used.
                        16. If you are showing a vehicle, just say "I recommend checking out this [Vehicle Name]." and then append the JSON.
                    """.trimIndent()
                )
                
                val groqMessages = listOf(systemMessage) + chatRepository.messages.value.map { msg ->
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
                        
                        // Parse for JSON action
                        val (cleanText, actionJson) = parseResponse(assistantResponse)
                        
                        // We will populate these if we have an action
                        var addonNames: List<String> = emptyList()
                        var proposedBooking: SavedBooking? = null
                        var dealToShow: dto.Deal? = null
                        
                        if (actionJson != null) {
                            val actionResult = handleAction(actionJson)
                            proposedBooking = actionResult.first
                            addonNames = actionResult.second
                            dealToShow = actionResult.third
                        }
                        
                        chatRepository.addMessage(ChatMessage(
                            text = cleanText, 
                            isUser = false,
                            isOffer = dealToShow != null,
                            deal = dealToShow,
                            proposedBooking = proposedBooking,
                            addonNames = addonNames
                        ))
                    }
                    is utils.Result.Error -> {
                        val errorMsg = "Failed to get response. Please try again."
                        _errorMessage.value = errorMsg
                        chatRepository.addMessage(ChatMessage(errorMsg, isUser = false))
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "An unexpected error occurred: ${e.message}"
                _errorMessage.value = errorMsg
                chatRepository.addMessage(ChatMessage("Sorry, I encountered an error.", isUser = false))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun parseResponse(response: String): Pair<String, JsonObject?> {
        var jsonString: String? = null
        var cleanText = response

        // 1. Try to find markdown code block
        val jsonStart = response.indexOf("```json")
        val jsonEnd = response.lastIndexOf("```")
        
        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            jsonString = response.substring(jsonStart + 7, jsonEnd).trim()
            cleanText = response.substring(0, jsonStart).trim()
        } else {
            // 2. Fallback: Try to find the last JSON object in the text
            val lastOpenBrace = response.lastIndexOf('{')
            val lastCloseBrace = response.lastIndexOf('}')
            
            if (lastOpenBrace != -1 && lastCloseBrace != -1 && lastCloseBrace > lastOpenBrace) {
                // Check if it looks like our action JSON
                val potentialJson = response.substring(lastOpenBrace, lastCloseBrace + 1)
                if (potentialJson.contains("\"action\"")) {
                    jsonString = potentialJson
                    cleanText = response.substring(0, lastOpenBrace).trim()
                }
            }
        }

        if (jsonString != null) {
            try {
                val jsonObject = Json.parseToJsonElement(jsonString) as? JsonObject
                return cleanText to jsonObject
            } catch (e: Exception) {
                println("Failed to parse JSON from AI: $e")
            }
        }
        return cleanText to null
    }
    
    @OptIn(kotlin.time.ExperimentalTime::class)
    private suspend fun handleAction(json: JsonObject): Triple<SavedBooking?, List<String>, dto.Deal?> {
        val action = json["action"]?.jsonPrimitive?.contentOrNull
        val vehicleId = json["vehicle_id"]?.jsonPrimitive?.contentOrNull
        
        if (vehicleId != null) {
             val vehicle = availableVehicles.find { it.vehicle.id == vehicleId }
             
             if (vehicle != null) {
                // Extract details for both actions (or default for show_vehicle)
                val protectionId = json["protection_package_id"]?.jsonPrimitive?.contentOrNull
                val addonIdsJson = json["addon_ids"]?.jsonArray
                val addonIds = addonIdsJson?.mapNotNull { it.jsonPrimitive.contentOrNull }?.toSet() ?: emptySet()
                
                val protection = protectionPackages.find { it.id == protectionId }
                
                // Map addon IDs to names
                val addonNames = addonIds.mapNotNull { id ->
                    addons.find { it.first == id }?.second
                }

                val vehiclePrice = vehicle.pricing.totalPrice.amount
                val protectionPrice = protection?.price?.totalPrice?.amount 
                    ?: protection?.price?.displayPrice?.amount 
                    ?: 0.0
                val totalAmount = vehiclePrice + protectionPrice
                
                val proposedBooking = SavedBooking(
                    id = Clock.System.now().toEpochMilliseconds().toString(),
                    bookingId = "chat_booking_${Clock.System.now().toEpochMilliseconds()}", // Mock ID
                    vehicle = vehicle,
                    protectionPackage = protection,
                    addonIds = addonIds,
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    totalPrice = totalAmount,
                    currency = vehicle.pricing.totalPrice.currency,
                    status = BookingStatus.DRAFT
                )
                
                return Triple(proposedBooking, addonNames, vehicle)
             }
        }
        return Triple(null, emptyList(), null)
    }

    fun saveBooking(message: ChatMessage) {
        val booking = message.proposedBooking ?: return
        
        viewModelScope.launch {
            savedBookingRepository.saveBooking(booking)
            
            // Update the message to show it's saved
            val updatedMessage = message.copy(isSaved = true)
            chatRepository.updateMessage(updatedMessage)
        }
    }


}
