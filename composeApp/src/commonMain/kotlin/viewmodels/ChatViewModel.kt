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
import models.UserContext
import utils.ChatContextExtractor
import recommendations.VehicleRecommendationEngine
import models.DestinationContext

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val storage: utils.Storage,
    private val vehiclesRepository: repositories.VehiclesRepository,
    private val bookingFlowViewModel: BookingFlowViewModel,
    private val userProfileRepository: repositories.UserProfileRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("Hello! I'm your intelligent assistant. How can I help you today?", isUser = false)
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _isProfileLoaded = MutableStateFlow(false)
    val isProfileLoaded: StateFlow<Boolean> = _isProfileLoaded.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Track user context across conversation
    private var userContext = UserContext()
    
    // User profile loaded from booking history
    private var userProfile: models.UserProfile? = null

    private var availableVehicles: List<dto.Deal> = emptyList()
    
    init {
        // Load user profile and initialize context when chat opens
        loadUserProfile()
    }
    
    /**
     * Load user profile from booking history to personalize responses
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                userProfile = userProfileRepository.getUserProfile()
                
                // Initialize user context from profile preferences
                userProfile?.let { profile ->
                    // Only use profile if user has consented and has enough data
                    if (profile.privacyConsent != models.PrivacyConsent.NONE && profile.hasEnoughData()) {
                        // Set typical traveler count if available
                        profile.typicalTravelerCount?.let { count ->
                            userContext = userContext.copy(travelerCount = count)
                        }
                        
                        // Set most common trip purpose if available
                        profile.commonTripPurposes.firstOrNull()?.let { purpose ->
                            userContext = userContext.copy(tripPurpose = purpose)
                        }
                        
                        // Add preferences from profile
                        val preferences = mutableListOf<String>()
                        if (profile.preferredVehicleTypes.isNotEmpty()) {
                            preferences.add("prefers ${profile.preferredVehicleTypes.first()} vehicles")
                        }
                        if (profile.preferredFuelTypes.contains("Electric") || profile.preferredFuelTypes.contains("Hybrid")) {
                            preferences.add("eco-friendly")
                        }
                        if (profile.preferredBrands.isNotEmpty()) {
                            preferences.add("prefers ${profile.preferredBrands.first()} brand")
                        }
                        
                        if (preferences.isNotEmpty()) {
                            userContext = userContext.copy(preferences = preferences)
                        }
                        
                        // Update greeting message to be more personalized
                        val personalizedGreeting = buildPersonalizedGreeting(profile)
                        if (personalizedGreeting != null) {
                            _messages.value = listOf(
                                ChatMessage(personalizedGreeting, isUser = false)
                            )
                        }
                    }
                }
                _isProfileLoaded.value = true
            } catch (e: Exception) {
                // Silently fail - profile loading is optional
                _isProfileLoaded.value = true
            }
        }
    }
    
    /**
     * Build a personalized greeting based on user profile
     */
    private fun buildPersonalizedGreeting(profile: models.UserProfile): String? {
        val parts = mutableListOf<String>()
        
        // Add personalized elements
        if (profile.preferredBrands.isNotEmpty()) {
            parts.add("I see you've rented ${profile.preferredBrands.first()} vehicles before")
        } else if (profile.preferredVehicleTypes.isNotEmpty()) {
            parts.add("I notice you prefer ${profile.preferredVehicleTypes.first()} vehicles")
        }
        
        if (profile.commonTripPurposes.isNotEmpty()) {
            val purpose = profile.commonTripPurposes.first()
            val purposeText = when (purpose) {
                models.TripPurpose.BUSINESS -> "business trips"
                models.TripPurpose.VACATION -> "vacations"
                models.TripPurpose.FAMILY -> "family trips"
                models.TripPurpose.UNKNOWN -> "trips"
            }
            parts.add("you often travel for $purposeText")
        }
        
        return if (parts.isNotEmpty()) {
            "Hello! ${parts.joinToString(" and ")}. How can I help you find the perfect vehicle today?"
        } else {
            null // Use default greeting
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isLoading.value) return

        // Extract context from user message
        val extractedContext = ChatContextExtractor.extractFromMessage(text)
        userContext = userContext.merge(extractedContext)

        // Add user message
        val userMessage = ChatMessage(text, isUser = true)
        _messages.value = _messages.value + userMessage

        // Show loading indicator
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                // Get booking ID from BookingFlowViewModel
                val bookingId = bookingFlowViewModel.bookingId.value
                if (bookingId == null) {
                    // If no booking ID, create one or show error
                    _messages.value = _messages.value + ChatMessage(
                        "Please start a booking first by searching for a vehicle.",
                        isUser = false
                    )
                    _isLoading.value = false
                    return@launch
                }
                
                // Get available vehicles and recommendations
                val vehiclesResult = vehiclesRepository.getAvailableVehicles(bookingId)
                val availableDeals = if (vehiclesResult is utils.Result.Success) {
                    vehiclesResult.data.deals
                } else {
                    emptyList()
                }

                // Generate recommendations using the engine with user profile
                val destinationContext = userContext.toDestinationContext()
                val recommendationEngine = VehicleRecommendationEngine(
                    destinationContext = destinationContext,
                    userProfile = userProfile
                )
                val scoredDeals = if (availableDeals.isNotEmpty()) {
                    recommendationEngine.scoreAndRankDeals(availableDeals)
                } else {
                    emptyList()
                }

                // Build enhanced system prompt with user profile context
                val systemPrompt = buildSystemPrompt(userContext, scoredDeals, userProfile)

                val systemMessage = GroqMessage(
                    role = "system",
                    content = systemPrompt
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
                        var assistantResponse = result.data
                        
                        // If we have recommendations, enhance the response with top vehicles
                        val topRecommendations = scoredDeals.take(3)
                        if (topRecommendations.isNotEmpty() && userContext.hasMinimumInfo()) {
                            // Find vehicles mentioned in response or add top recommendations
                            val mentionedVehicles = findMentionedVehicles(assistantResponse, availableDeals)
                            val vehiclesToInclude = if (mentionedVehicles.isNotEmpty()) {
                                mentionedVehicles
                            } else {
                                topRecommendations.map { it.deal }
                            }

                            // Enhance response with vehicle details if not already present
                            if (mentionedVehicles.isEmpty() && vehiclesToInclude.isNotEmpty()) {
                                val vehicleDetails = formatVehicleRecommendations(vehiclesToInclude, scoredDeals)
                                assistantResponse += "\n\n$vehicleDetails"
                            }
                        }
                        
                        // Try to find matching vehicles in the response
                        val matchingDeal = findMatchingVehicle(assistantResponse, availableDeals)

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

    /**
     * Build enhanced system prompt with user context, profile, and recommendations
     */
    private fun buildSystemPrompt(
        context: UserContext,
        scoredDeals: List<recommendations.ScoredDeal>,
        profile: models.UserProfile? = null
    ): String {
        val basePrompt = StringBuilder()
        basePrompt.append("You are a helpful and friendly assistant for RiderGo, a premium car rental service. ")
        basePrompt.append("Your goal is to help users find the perfect vehicle for their needs.\n\n")

        // Add user context information
        val contextInfo = mutableListOf<String>()
        if (context.travelerCount != null) {
            contextInfo.add("Number of travelers: ${context.travelerCount}")
        }
        if (context.tripPurpose != null) {
            contextInfo.add("Trip purpose: ${context.tripPurpose.name.lowercase()}")
        }
        if (context.location != null) {
            contextInfo.add("Location: ${context.location.city}, ${context.location.country}")
        }
        if (context.startDate != null) {
            contextInfo.add("Start date: ${context.startDate}")
        }
        if (context.endDate != null) {
            contextInfo.add("End date: ${context.endDate}")
        }
        if (context.budget != null) {
            contextInfo.add("Budget: €${context.budget}")
        }
        if (context.preferences.isNotEmpty()) {
            contextInfo.add("Preferences: ${context.preferences.joinToString(", ")}")
        }

        if (contextInfo.isNotEmpty()) {
            basePrompt.append("Current user information:\n")
            contextInfo.forEach { info ->
                basePrompt.append("- $info\n")
            }
            basePrompt.append("\n")
        }
        
        // Add user preferences from booking history
        profile?.let { userProfile ->
            val profileInfo = mutableListOf<String>()
            
            if (userProfile.preferredBrands.isNotEmpty()) {
                profileInfo.add("Based on past bookings, user prefers: ${userProfile.preferredBrands.joinToString(", ")} vehicles")
            }
            
            if (userProfile.preferredVehicleTypes.isNotEmpty()) {
                profileInfo.add("User typically rents: ${userProfile.preferredVehicleTypes.joinToString(", ")}")
            }
            
            if (userProfile.preferredFuelTypes.isNotEmpty()) {
                profileInfo.add("User prefers: ${userProfile.preferredFuelTypes.joinToString(", ")} fuel types")
            }
            
            if (userProfile.averageBudgetRange != null) {
                val budget = userProfile.averageBudgetRange
                profileInfo.add("User's typical budget range: €${budget.min} - €${budget.max}")
            }
            
            if (userProfile.typicalTravelerCount != null) {
                profileInfo.add("User typically travels with ${userProfile.typicalTravelerCount} ${if (userProfile.typicalTravelerCount == 1) "person" else "people"}")
            }
            
            if (userProfile.commonTripPurposes.isNotEmpty()) {
                val purposes = userProfile.commonTripPurposes.map { it.name.lowercase() }.joinToString(", ")
                profileInfo.add("User's common trip purposes: $purposes")
            }
            
            if (userProfile.preferredLocations.isNotEmpty()) {
                profileInfo.add("User frequently rents in: ${userProfile.preferredLocations.joinToString(", ")}")
            }
            
            if (profileInfo.isNotEmpty()) {
                basePrompt.append("User preferences (from booking history):\n")
                profileInfo.forEach { info ->
                    basePrompt.append("- $info\n")
                }
                basePrompt.append("\nUse this information to provide more personalized recommendations. ")
                basePrompt.append("When suggesting vehicles, mention if they match the user's historical preferences.\n\n")
            }
        }

        // Check if we need more information
        val missingInfo = context.getMissingInfo()
        if (missingInfo.isNotEmpty() && !context.hasMinimumInfo()) {
            basePrompt.append("IMPORTANT: The user hasn't provided enough information yet. ")
            basePrompt.append("You should politely ask for the following information to provide better recommendations:\n")
            missingInfo.forEach { info ->
                basePrompt.append("- $info\n")
            }
            basePrompt.append("\nBe conversational and ask one or two questions at a time, not all at once.\n\n")
        }

        // Add top recommendations if available
        val topRecommendations = scoredDeals.take(3)
        if (topRecommendations.isNotEmpty() && context.hasMinimumInfo()) {
            basePrompt.append("Top recommended vehicles (ranked by relevance):\n")
            topRecommendations.forEachIndexed { index, scoredDeal ->
                val deal = scoredDeal.deal
                val vehicle = deal.vehicle
                val topReasons = scoredDeal.reasons.take(2)
                basePrompt.append("${index + 1}. ${vehicle.brand} ${vehicle.model}")
                basePrompt.append(" - €${deal.pricing.displayPrice.amount}/day")
                if (topReasons.isNotEmpty()) {
                    basePrompt.append(" (${topReasons.joinToString(", ") { it.title }})")
                }
                basePrompt.append("\n")
            }
            basePrompt.append("\nWhen recommending vehicles, always mention the full name (Brand + Model) clearly. ")
            basePrompt.append("Explain why each vehicle is a good fit based on the user's needs.\n\n")
        }

        basePrompt.append("Guidelines:\n")
        basePrompt.append("- Be friendly, helpful, and conversational\n")
        basePrompt.append("- Ask clarifying questions when information is missing\n")
        basePrompt.append("- When recommending vehicles, mention specific features that match user needs\n")
        basePrompt.append("- Always mention the full vehicle name (Brand + Model) when recommending\n")
        basePrompt.append("- If the user asks about specific vehicles, provide detailed information\n")
        basePrompt.append("- Keep responses concise but informative\n")

        return basePrompt.toString()
    }

    /**
     * Format vehicle recommendations for the LLM response
     */
    private fun formatVehicleRecommendations(
        deals: List<dto.Deal>,
        scoredDeals: List<recommendations.ScoredDeal>
    ): String {
        if (deals.isEmpty()) return ""

        val formatted = StringBuilder()
        formatted.append("Here are my top recommendations:\n\n")

        deals.take(3).forEachIndexed { index, deal ->
            val scoredDeal = scoredDeals.firstOrNull { it.deal.vehicle.id == deal.vehicle.id }
            val topReasons = scoredDeal?.reasons?.take(2) ?: emptyList()

            formatted.append("${index + 1}. **${deal.vehicle.brand} ${deal.vehicle.model}**\n")
            formatted.append("   Price: €${deal.pricing.displayPrice.amount}/day\n")
            if (topReasons.isNotEmpty()) {
                formatted.append("   Why: ${topReasons.joinToString("; ") { it.description }}\n")
            }
            formatted.append("\n")
        }

        return formatted.toString()
    }

    /**
     * Find vehicles mentioned in the LLM response
     */
    private fun findMentionedVehicles(
        text: String,
        availableDeals: List<dto.Deal>
    ): List<dto.Deal> {
        val mentioned = mutableListOf<dto.Deal>()
        for (deal in availableDeals) {
            val fullName = "${deal.vehicle.brand} ${deal.vehicle.model}"
            if (text.contains(fullName, ignoreCase = true) ||
                text.contains(deal.vehicle.model, ignoreCase = true)) {
                mentioned.add(deal)
            }
        }
        return mentioned
    }

    private suspend fun findMatchingVehicle(
        text: String,
        availableDeals: List<dto.Deal>
    ): dto.Deal? {
        if (availableDeals.isEmpty()) return null

        // Check if any vehicle brand+model is mentioned in the text
        return availableDeals.firstOrNull { deal ->
            val fullName = "${deal.vehicle.brand} ${deal.vehicle.model}"
            text.contains(fullName, ignoreCase = true) ||
            text.contains(deal.vehicle.model, ignoreCase = true)
        }
    }
}
