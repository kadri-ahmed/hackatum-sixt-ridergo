package models

import kotlinx.serialization.Serializable

/**
 * User profile for personalization
 * All data is stored locally and anonymized
 */
@Serializable
data class UserProfile(
    // Behavioral preferences (inferred from usage, not explicit)
    val preferredVehicleTypes: List<String> = emptyList(), // e.g., "SUV", "Sedan", "Electric"
    val preferredBrands: List<String> = emptyList(),
    val preferredFuelTypes: List<String> = emptyList(),
    val averageBudgetRange: PriceRange? = null,
    val typicalTravelerCount: Int? = null,
    val commonTripPurposes: List<TripPurpose> = emptyList(),
    
    // Addon preferences (from past selections)
    val frequentlySelectedAddons: List<String> = emptyList(), // Addon IDs
    val addonCategories: Map<String, Int> = emptyMap(), // Category -> selection count
    
    // Protection package preferences
    val preferredProtectionPackage: String? = null, // Most frequently selected package ID
    val protectionPackageHistory: Map<String, Int> = emptyMap(), // Package ID -> selection count
    
    // Booking patterns (anonymized)
    val bookingFrequency: BookingFrequency = BookingFrequency.UNKNOWN,
    val preferredLocations: List<String> = emptyList(), // City names only, no addresses
    val seasonalPreferences: Map<String, List<String>> = emptyMap(), // Season -> vehicle types
    
    // Privacy settings
    val privacyConsent: PrivacyConsent = PrivacyConsent.NONE,
    val dataRetentionDays: Int = 90, // Default 90 days
    val lastUpdated: String = ""
) {
    /**
     * Check if profile has enough data for meaningful personalization
     */
    fun hasEnoughData(): Boolean {
        return bookingHistoryCount() >= 2 || 
               preferredVehicleTypes.isNotEmpty() ||
               preferredBrands.isNotEmpty()
    }
    
    /**
     * Get total booking history count (for privacy, we don't store full history)
     */
    fun bookingHistoryCount(): Int {
        return protectionPackageHistory.values.sum()
    }
}

@Serializable
data class PriceRange(
    val min: Double,
    val max: Double,
    val currency: String = "EUR"
)

@Serializable
enum class BookingFrequency {
    UNKNOWN,
    RARE,      // < 2 bookings/year
    OCCASIONAL, // 2-5 bookings/year
    REGULAR,   // 5-10 bookings/year
    FREQUENT   // > 10 bookings/year
}

@Serializable
enum class PrivacyConsent {
    NONE,              // No data collection
    LOCAL_ONLY,        // Store locally only (default)
    ANONYMIZED_SYNC,   // Sync anonymized data
    FULL_SYNC          // Full sync (requires explicit opt-in)
}
