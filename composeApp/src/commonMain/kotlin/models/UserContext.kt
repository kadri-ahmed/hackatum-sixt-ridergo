package models

import dto.LocationDto
import models.TripPurpose
import models.TripType

/**
 * User context extracted from chat conversation
 * Used to provide personalized vehicle recommendations
 */
data class UserContext(
    val travelerCount: Int? = null,
    val tripPurpose: TripPurpose? = null,
    val location: LocationDto? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val tripType: TripType? = null,
    val budget: Double? = null,
    val preferences: List<String> = emptyList(), // e.g., "luxury", "eco-friendly", "spacious"
    val specialNeeds: List<String> = emptyList() // e.g., "wheelchair accessible", "child seat"
) {
    /**
     * Check if we have enough information to make reliable recommendations
     */
    fun hasMinimumInfo(): Boolean {
        return travelerCount != null && tripPurpose != null
    }
    
    /**
     * Get missing information fields that would improve recommendations
     */
    fun getMissingInfo(): List<String> {
        val missing = mutableListOf<String>()
        if (travelerCount == null) missing.add("number of travelers")
        if (tripPurpose == null) missing.add("trip purpose (business, vacation, family)")
        if (location == null) missing.add("pickup location")
        if (startDate == null) missing.add("rental start date")
        if (endDate == null) missing.add("rental end date")
        return missing
    }
    
    /**
     * Convert to DestinationContext if we have enough information
     */
    fun toDestinationContext(): DestinationContext? {
        val location = this.location ?: return null
        val startDate = this.startDate ?: return null
        val endDate = this.endDate ?: return null
        
        return DestinationContext(
            location = location,
            startDate = startDate,
            endDate = endDate,
            tripType = tripType ?: TripType.ROUND_TRIP,
            travelerCount = travelerCount ?: 1,
            purpose = tripPurpose ?: TripPurpose.UNKNOWN
        )
    }
    
    /**
     * Merge with new context information
     */
    fun merge(other: UserContext): UserContext {
        return UserContext(
            travelerCount = other.travelerCount ?: this.travelerCount,
            tripPurpose = other.tripPurpose ?: this.tripPurpose,
            location = other.location ?: this.location,
            startDate = other.startDate ?: this.startDate,
            endDate = other.endDate ?: this.endDate,
            tripType = other.tripType ?: this.tripType,
            budget = other.budget ?: this.budget,
            preferences = (this.preferences + other.preferences).distinct(),
            specialNeeds = (this.specialNeeds + other.specialNeeds).distinct()
        )
    }
}
