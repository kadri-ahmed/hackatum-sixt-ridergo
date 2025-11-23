package services

import dto.BookingDto
import models.BookingHistoryEntry
import models.DestinationContext
import models.PrivacyConsent
import models.UserProfile
import models.BookingFrequency
import models.TripPurpose
import repositories.BookingHistoryRepository
import utils.getCurrentTimestamp

/**
 * Service for building and updating user profiles from behavioral data
 * Privacy-first: Only uses anonymized, aggregated data
 */
class PersonalizationService(
    private val bookingHistoryRepository: BookingHistoryRepository
) {
    
    /**
     * Update user profile based on completed booking
     * Only stores anonymized, aggregated patterns
     */
    suspend fun updateProfileFromBooking(
        booking: BookingDto,
        tripContext: DestinationContext? = null,
        currentProfile: UserProfile
    ): UserProfile {
        // Create history entry
        val historyEntry = BookingHistoryEntry.fromBooking(booking, tripContext)
        
        // Store history (respects privacy settings)
        if (currentProfile.privacyConsent != PrivacyConsent.NONE) {
            bookingHistoryRepository.saveBookingHistory(historyEntry)
        }
        
        // Update aggregated preferences (no personal data)
        return updateProfileFromHistoryEntry(historyEntry, currentProfile)
    }
    
    /**
     * Rebuild profile from all stored booking history
     */
    suspend fun rebuildProfile(currentProfile: UserProfile): UserProfile {
        val historyEntries = bookingHistoryRepository.getBookingHistory()
        
        if (historyEntries.isEmpty()) {
            return currentProfile
        }
        
        var profile = currentProfile
        
        // Aggregate data from all history entries
        historyEntries.forEach { entry ->
            profile = updateProfileFromHistoryEntry(entry, profile)
        }
        
        // Calculate booking frequency
        val frequency = calculateBookingFrequency(historyEntries)
        
        return profile.copy(
            bookingFrequency = frequency,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Update profile from a single history entry
     */
    private fun updateProfileFromHistoryEntry(
        entry: BookingHistoryEntry,
        currentProfile: UserProfile
    ): UserProfile {
        val vehicleTypes = currentProfile.preferredVehicleTypes.toMutableList()
        val brands = currentProfile.preferredBrands.toMutableList()
        val fuelTypes = currentProfile.preferredFuelTypes.toMutableList()
        val tripPurposes = currentProfile.commonTripPurposes.toMutableList()
        val locations = currentProfile.preferredLocations.toMutableList()
        val protectionHistory = currentProfile.protectionPackageHistory.toMutableMap()
        val addonCategories = currentProfile.addonCategories.toMutableMap()
        val seasonalPrefs = currentProfile.seasonalPreferences.toMutableMap()
        
        // Update vehicle preferences
        entry.vehicleGroupType?.let { vehicleTypes.add(it) }
        entry.vehicleBrand?.let { brands.add(it) }
        entry.fuelType?.let { fuelTypes.add(it) }
        
        // Update trip purposes
        entry.tripPurpose?.let { tripPurposes.add(it) }
        
        // Update locations
        entry.location?.let { locations.add(it) }
        
        // Update protection package history
        entry.protectionPackageId?.let { packageId ->
            protectionHistory[packageId] = (protectionHistory[packageId] ?: 0) + 1
        }
        
        // Update addon preferences
        entry.selectedAddonIds.forEach { addonId ->
            // Extract category from addon ID (assuming format like "category_id")
            val category = addonId.split("_").firstOrNull() ?: "other"
            addonCategories[category] = (addonCategories[category] ?: 0) + 1
        }
        
        // Update seasonal preferences
        entry.season?.let { season ->
            entry.vehicleGroupType?.let { vehicleType ->
                val seasonPrefs = (seasonalPrefs[season] ?: emptyList()).toMutableList()
                if (!seasonPrefs.contains(vehicleType)) {
                    seasonPrefs.add(vehicleType)
                }
                seasonalPrefs[season] = seasonPrefs
            }
        }
        
        // Calculate most preferred protection package
        val preferredPackage = protectionHistory.maxByOrNull { it.value }?.key
        
        // Note: Average budget and typical traveler count are calculated from all history
        // For now, we'll use the current profile values and update incrementally
        // Full recalculation would require access to all history entries, which we don't have here
        val avgBudget = currentProfile.averageBudgetRange
        val typicalTravelerCount = currentProfile.typicalTravelerCount
        
        // Update budget range incrementally
        val updatedBudgetRange = if (entry.pricePaid != null) {
            val currentMin = currentProfile.averageBudgetRange?.min ?: entry.pricePaid
            val currentMax = currentProfile.averageBudgetRange?.max ?: entry.pricePaid
            models.PriceRange(
                min = minOf(currentMin, entry.pricePaid),
                max = maxOf(currentMax, entry.pricePaid)
            )
        } else {
            currentProfile.averageBudgetRange
        }
        
        // Update typical traveler count incrementally
        val updatedTravelerCount = entry.passengerCount ?: currentProfile.typicalTravelerCount
        
        return currentProfile.copy(
            preferredVehicleTypes = vehicleTypes.distinct().take(10), // Keep top 10
            preferredBrands = brands.distinct().take(10),
            preferredFuelTypes = fuelTypes.distinct(),
            averageBudgetRange = updatedBudgetRange,
            typicalTravelerCount = updatedTravelerCount,
            commonTripPurposes = tripPurposes.distinct(),
            frequentlySelectedAddons = (currentProfile.frequentlySelectedAddons + entry.selectedAddonIds).distinct().take(20),
            addonCategories = addonCategories,
            preferredProtectionPackage = preferredPackage,
            protectionPackageHistory = protectionHistory,
            preferredLocations = locations.distinct().take(10),
            seasonalPreferences = seasonalPrefs,
            lastUpdated = getCurrentTimestamp()
        )
    }
    
    /**
     * Calculate booking frequency from history
     */
    private fun calculateBookingFrequency(history: List<BookingHistoryEntry>): BookingFrequency {
        if (history.isEmpty()) return BookingFrequency.UNKNOWN
        
        // Group by year and count
        val bookingsPerYear = history.groupBy { entry ->
            entry.timestamp.substringBefore("-").toIntOrNull() ?: 0
        }.mapValues { it.value.size }
        
        val avgBookingsPerYear = bookingsPerYear.values.average()
        
        return when {
            avgBookingsPerYear < 2 -> BookingFrequency.RARE
            avgBookingsPerYear < 5 -> BookingFrequency.OCCASIONAL
            avgBookingsPerYear < 10 -> BookingFrequency.REGULAR
            else -> BookingFrequency.FREQUENT
        }
    }
    
    /**
     * Get personalized recommendations based on profile
     */
    fun getPersonalizedPreferences(profile: UserProfile): Map<String, Any> {
        return mapOf(
            "preferredVehicleTypes" to profile.preferredVehicleTypes,
            "preferredBrands" to profile.preferredBrands,
            "preferredFuelTypes" to profile.preferredFuelTypes,
            "typicalTravelerCount" to (profile.typicalTravelerCount ?: 1),
            "commonTripPurposes" to profile.commonTripPurposes.map { it.name },
            "preferredProtectionPackage" to (profile.preferredProtectionPackage ?: ""),
            "frequentlySelectedAddons" to profile.frequentlySelectedAddons.take(5)
        )
    }
    
    private fun getCurrentTimestamp(): String {
        return utils.getCurrentTimestamp()
    }
}
