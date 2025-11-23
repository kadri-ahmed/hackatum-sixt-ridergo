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
import ui.screens.ChatMessageOption
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
    private var availableAddons: List<dto.AddonCategory> = emptyList()
    private var savedBookings: List<SavedBooking> = emptyList()
    private var chatSessionBookingId: String? = null

    init {
        viewModelScope.launch {
            savedBookingRepository.getSavedBookings().collect { bookings ->
                savedBookings = bookings
            }
        }
    }
    
    /**
     * Ensures we have a real booking ID for the chat session.
     * Creates one if it doesn't exist yet.
     */
    private suspend fun ensureChatSessionBookingId(): String? {
        if (chatSessionBookingId == null) {
            when (val result = bookingRepository.createBooking()) {
                is utils.Result.Success -> {
                    chatSessionBookingId = result.data.id
                }
                is utils.Result.Error -> {
                    return null
                }
            }
        }
        return chatSessionBookingId
    }

    fun startNewChat() {
        chatRepository.clearMessages()
        // Reset the chat session booking ID when starting a new chat
        chatSessionBookingId = null
        availableVehicles = emptyList()
        protectionPackages = emptyList()
        availableAddons = emptyList()
    }

    fun deleteBooking(bookingId: String) {
        viewModelScope.launch {
            savedBookingRepository.deleteBooking(bookingId)
            
            // Update messages to mark the booking as deleted
            // We iterate through all messages and if they contain the booking in existingBookings or bookingsToDelete,
            // we add the ID to deletedBookingIds.
            val currentMessages = messages.value.toMutableList()
            val updatedMessages = currentMessages.map { msg ->
                val hasExisting = msg.existingBookings.any { it.id == bookingId }
                val hasSuggested = msg.bookingsToDelete.any { it.id == bookingId }
                
                if (hasExisting || hasSuggested) {
                    msg.copy(deletedBookingIds = msg.deletedBookingIds + bookingId)
                } else {
                    msg
                }
            }
            updatedMessages.forEach { msg ->
                if (msg.deletedBookingIds.contains(bookingId)) {
                    chatRepository.updateMessage(msg)
                }
            }
            
            chatRepository.addMessage(ChatMessage("Booking deleted.", isUser = false))
        }
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
                // Ensure we have a real booking ID for the chat session
                val bookingId = ensureChatSessionBookingId() ?: run {
                    _errorMessage.value = "Failed to initialize booking session. Please try again."
                    _isLoading.value = false
                    return@launch
                }
                
                // Fetch data if needed
                if (availableVehicles.isEmpty()) {
                    val result = vehiclesRepository.getAvailableVehicles(bookingId)
                    if (result is utils.Result.Success) {
                        availableVehicles = result.data.deals
                    }
                }
                if (protectionPackages.isEmpty()) {
                    val result = bookingRepository.getProtectionPackages(bookingId)
                    if (result is utils.Result.Success) {
                        protectionPackages = result.data.protectionPackages
                    }
                }
                if (availableAddons.isEmpty()) {
                    val result = vehiclesRepository.getAvailableAddons(bookingId)
                    if (result is utils.Result.Success) {
                        availableAddons = result.data.addons
                    }
                }

                // Construct system prompt
                val vehicleListString = availableVehicles.joinToString("\n") { deal ->
                    "- ID: ${deal.vehicle.id}, Name: ${deal.vehicle.brand} ${deal.vehicle.model}, Price: ${deal.pricing.displayPrice.currency} ${deal.pricing.displayPrice.amount}/day"
                }
                val protectionListString = protectionPackages.joinToString("\n") { pkg ->
                    "- ID: ${pkg.id}, Name: ${pkg.name}, Price: ${pkg.price.displayPrice.currency} ${pkg.price.displayPrice.amount}/day"
                }
                
                // Flatten addons for the prompt
                val addonListString = availableAddons.flatMap { it.options }.joinToString("\n") { option ->
                    "- ID: ${option.chargeDetail.id}, Name: ${option.chargeDetail.title}, Price: ${option.additionalInfo.price.displayPrice.currency} ${option.additionalInfo.price.displayPrice.amount}"
                }

                val savedBookingsString = if (savedBookings.isNotEmpty()) {
                    savedBookings.joinToString("\n") { booking ->
                        "- Booking ID: ${booking.id}, Vehicle: ${booking.vehicle.vehicle.brand} ${booking.vehicle.vehicle.model}, Status: ${booking.status}"
                    }
                } else {
                    "No saved bookings."
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
                        You are a helpful assistant for Cleveride, a premium car rental service. 
                        You help users find vehicles, customize their booking, and manage existing bookings.
                        
                        $userContext
                        
                        Current Saved Bookings:
                        $savedBookingsString
                        
                        Available Vehicles:
                        $vehicleListString
                        
                        Available Protection Packages:
                        $protectionListString
                        
                        Available Addons:
                        $addonListString
                        
                        Rules:
                        1. You are a consultative sales assistant. Your goal is to find the *perfect* car for the user.
                        2. If the user's request is vague, ask clarifying questions.
                        3. Recommend vehicles, protection, and addons from the lists above.
                        4. You can perform MULTIPLE actions in a single response using the JSON block.
                        5. Supported Action Types:
                           - "show_vehicle": Show a vehicle card.
                           - "save_booking": Propose a booking to save.
                           - "show_saved_bookings": Show the user's list of bookings (ONLY if explicitly asked).
                           - "suggest_deletion": Suggest deleting a specific booking (e.g. if user changes mind).
                        6. JSON Format:
                        ```json
                        {
                            "actions": [
                                {
                                    "type": "show_vehicle",
                                    "vehicle_id": "VEHICLE_ID"
                                },
                                {
                                    "type": "save_booking",
                                    "vehicle_id": "VEHICLE_ID",
                                    "protection_package_id": "PROTECTION_ID (optional)",
                                    "addon_ids": ["ADDON_ID_1"] (optional)
                                },
                                {
                                    "type": "suggest_deletion",
                                    "booking_id": "BOOKING_ID"
                                }
                            ]
                        }
                        ```
                        7. ALWAYS put the JSON block at the very end of your response.
                        8. Do NOT say "Here is the JSON". Just write your helpful message and append the JSON block silently.
                        9. If the user changes their mind about a booking (e.g. "Actually I want the Audi instead of the BMW"), you should suggest deleting the old booking AND saving the new one in the same response.
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
                        
                        var actionResults = ActionResults()
                        
                        if (actionJson != null) {
                            actionResults = handleActions(actionJson)
                        }
                        
                        chatRepository.addMessage(ChatMessage(
                            text = cleanText, 
                            isUser = false,
                            options = actionResults.options,
                            existingBookings = actionResults.existingBookingsToShow,
                            bookingsToDelete = actionResults.bookingsToDelete
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

    private val json = Json { 
        isLenient = true 
        ignoreUnknownKeys = true 
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
            // We iterate backwards to find the last valid JSON block
            var endIndex = response.lastIndexOf('}')
            while (endIndex != -1) {
                var startIndex = response.lastIndexOf('{', endIndex)
                if (startIndex == -1) break
                
                // Try to expand backwards to find the matching opening brace if nested
                var balance = 1
                var tempIndex = startIndex - 1
                while (tempIndex >= 0 && balance > 0) {
                     // This simple heuristic might fail for complex nested structures with strings containing braces
                     // But for our specific AI output which puts JSON at the end, finding the last { and } is usually enough
                     // unless it's nested.
                     // Let's stick to the previous logic but refine the check.
                     break 
                }

                // Actually, a better approach for our specific case (AI appending JSON at end):
                // Find the last '}' and walk backwards to find the matching '{'.
                var openBraces = 0
                var closeBraces = 0
                var foundStart = -1
                
                for (i in response.length - 1 downTo 0) {
                    if (response[i] == '}') {
                        closeBraces++
                    } else if (response[i] == '{') {
                        openBraces++
                        if (openBraces == closeBraces) {
                            foundStart = i
                            break
                        }
                    }
                }
                
                if (foundStart != -1) {
                    val potentialJson = response.substring(foundStart, response.lastIndexOf('}') + 1)
                    if (potentialJson.contains("\"actions\"") || potentialJson.contains("\"action\"")) {
                        jsonString = potentialJson
                        cleanText = response.substring(0, foundStart).trim()
                        break
                    }
                }
                
                // If we didn't find it with the balance check, try the simple lastIndexOf approach as fallback
                // for simple non-nested JSONs which is what we mostly get.
                val lastOpen = response.lastIndexOf('{')
                val lastClose = response.lastIndexOf('}')
                if (lastOpen != -1 && lastClose > lastOpen) {
                     val potential = response.substring(lastOpen, lastClose + 1)
                     if (potential.contains("\"actions\"") || potential.contains("\"action\"")) {
                         jsonString = potential
                         cleanText = response.substring(0, lastOpen).trim()
                     }
                }
                break // Only try once for now to avoid infinite loops if logic is flawed
            }
        }

        if (jsonString != null) {
            try {
                val jsonObject = json.parseToJsonElement(jsonString) as? JsonObject
                return cleanText to jsonObject
            } catch (e: Exception) {
                println("Failed to parse JSON from AI: $e")
            }
        }
        return cleanText to null
    }
    


    data class ActionResults(
        val options: List<ChatMessageOption> = emptyList(),
        val existingBookingsToShow: List<SavedBooking> = emptyList(),
        val bookingsToDelete: List<SavedBooking> = emptyList()
    )
    
    @OptIn(kotlin.time.ExperimentalTime::class)
    private suspend fun handleActions(json: JsonObject): ActionResults {
        val options = mutableListOf<ChatMessageOption>()
        var existingBookingsToShow: List<SavedBooking> = emptyList()
        val bookingsToDelete = mutableListOf<SavedBooking>()

        val actionsArray = json["actions"]?.jsonArray
        
        actionsArray?.forEach { actionElement ->
            val actionObj = actionElement as? JsonObject ?: return@forEach
            val type = actionObj["type"]?.jsonPrimitive?.contentOrNull
            
            when (type) {
                "show_saved_bookings" -> {
                    existingBookingsToShow = savedBookings
                }
                "suggest_deletion" -> {
                    val bookingId = actionObj["booking_id"]?.jsonPrimitive?.contentOrNull
                    if (bookingId != null) {
                        val booking = savedBookings.find { it.id == bookingId || it.bookingId == bookingId }
                        if (booking != null) {
                            bookingsToDelete.add(booking)
                        }
                    }
                }
                "show_vehicle", "save_booking" -> {
                    val vehicleId = actionObj["vehicle_id"]?.jsonPrimitive?.contentOrNull
                    if (vehicleId != null) {
                        val vehicle = availableVehicles.find { it.vehicle.id == vehicleId }
                        if (vehicle != null) {
                            if (type == "show_vehicle") {
                                options.add(ChatMessageOption(deal = vehicle))
                            } else { // save_booking
                                val protectionId = actionObj["protection_package_id"]?.jsonPrimitive?.contentOrNull
                                val addonIdsJson = actionObj["addon_ids"]?.jsonArray
                                val addonIds = addonIdsJson?.mapNotNull { it.jsonPrimitive.contentOrNull }?.toSet() ?: emptySet()
                                
                                val protection = protectionPackages.find { it.id == protectionId }
                                
                                var addonsPrice = 0.0
                                val currentAddonNames = mutableListOf<String>()
                                val allOptions = availableAddons.flatMap { it.options }
                                
                                addonIds.forEach { id ->
                                    val option = allOptions.find { it.chargeDetail.id == id }
                                    if (option != null) {
                                        currentAddonNames.add(option.chargeDetail.title)
                                        addonsPrice += option.additionalInfo.price.totalPrice?.amount
                                            ?: option.additionalInfo.price.displayPrice.amount 
                                            ?: 0.0
                                    }
                                }
                                
                                val vehiclePrice = vehicle.pricing.totalPrice.amount
                                val protectionPrice = protection?.price?.totalPrice?.amount 
                                    ?: protection?.price?.displayPrice?.amount 
                                    ?: 0.0
                                val totalAmount = vehiclePrice + protectionPrice + addonsPrice
                                
                                // Create a real booking via API
                                when (val createResult = bookingRepository.createBooking()) {
                                    is utils.Result.Success -> {
                                        val realBookingId = createResult.data.id
                                        
                                        // Assign vehicle to the booking
                                        val vehicleResult = bookingRepository.assignVehicleToBooking(
                                            realBookingId,
                                            vehicle.vehicle.id
                                        )
                                        
                                        if (vehicleResult is utils.Result.Success) {
                                            // Assign protection package if present
                                            if (protection != null) {
                                                bookingRepository.assignProtectionPackageToBooking(
                                                    realBookingId,
                                                    protection.id
                                                )
                                            }
                                            
                                            val proposedBooking = SavedBooking(
                                                id = Clock.System.now().toEpochMilliseconds().toString(),
                                                bookingId = realBookingId,
                                                vehicle = vehicle,
                                                protectionPackage = protection,
                                                addonIds = addonIds,
                                                timestamp = Clock.System.now().toEpochMilliseconds(),
                                                totalPrice = totalAmount,
                                                currency = vehicle.pricing.totalPrice.currency,
                                                status = BookingStatus.DRAFT
                                            )
                                            options.add(ChatMessageOption(
                                                deal = vehicle,
                                                proposedBooking = proposedBooking,
                                                addonNames = currentAddonNames
                                            ))
                                        }
                                        // If vehicle assignment fails, we still add the option but without proposedBooking
                                        // The user can still see the vehicle and try again
                                    }
                                    is utils.Result.Error -> {
                                        // If booking creation fails, still show the vehicle option
                                        // but without a proposed booking
                                        options.add(ChatMessageOption(deal = vehicle))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return ActionResults(options, existingBookingsToShow, bookingsToDelete)
    }

    fun saveBooking(message: ChatMessage, option: ChatMessageOption) {
        val booking = option.proposedBooking ?: return
        
        viewModelScope.launch {
            try {
                // Booking already has a real ID from when it was proposed, just save it
                savedBookingRepository.saveBooking(booking)
                val updatedMessage = message.copy(isSaved = true)
                chatRepository.updateMessage(updatedMessage)
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred while saving the booking."
            }
        }
    }


}
