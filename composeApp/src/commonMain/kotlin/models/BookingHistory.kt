package models

import dto.BookingDto
import kotlinx.serialization.Serializable

/**
 * Local booking history entry
 * Stores minimal anonymized data for personalization
 */
@Serializable
data class BookingHistoryEntry(
    val bookingId: String,
    val timestamp: String,
    
    // Vehicle preferences (anonymized)
    val vehicleBrand: String? = null,
    val vehicleModel: String? = null,
    val vehicleGroupType: String? = null,
    val fuelType: String? = null,
    val passengerCount: Int? = null,
    val pricePaid: Double? = null,
    
    // Protection package
    val protectionPackageId: String? = null,
    val protectionPackageName: String? = null,
    
    // Addons (IDs only, no personal details)
    val selectedAddonIds: List<String> = emptyList(),
    
    // Trip context (anonymized)
    val tripPurpose: TripPurpose? = null,
    val location: String? = null, // City only
    val season: String? = null, // "Spring", "Summer", etc.
    
    // No personal identifiers stored
) {
    companion object {
        /**
         * Create history entry from completed booking
         */
        fun fromBooking(booking: BookingDto, tripContext: DestinationContext? = null): BookingHistoryEntry {
            val vehicle = booking.selectedVehicle?.vehicle
            val season = tripContext?.startDate?.let { extractSeason(it) }
            
            return BookingHistoryEntry(
                bookingId = booking.id,
                timestamp = booking.createdAt,
                vehicleBrand = vehicle?.brand,
                vehicleModel = vehicle?.model,
                vehicleGroupType = vehicle?.groupType,
                fuelType = vehicle?.fuelType,
                passengerCount = vehicle?.passengersCount,
                pricePaid = booking.selectedVehicle?.pricing?.totalPrice?.amount,
                protectionPackageId = booking.protectionPackages?.id,
                protectionPackageName = booking.protectionPackages?.name,
                selectedAddonIds = emptyList(), // Would be populated from booking.addons if available
                tripPurpose = tripContext?.purpose,
                location = tripContext?.location?.city,
                season = season
            )
        }
        
        private fun extractSeason(dateString: String): String {
            // Simple season extraction from date
            // Format: "2025-12-01T09:00:00+01:00" or "2025-12-01"
            val datePart = dateString.substringBefore("T")
            val monthStr = datePart.split("-").getOrNull(1) ?: return "Unknown"
            val month = monthStr.toIntOrNull() ?: return "Unknown"
            return when (month) {
                12, 1, 2 -> "Winter"
                in 3..5 -> "Spring"
                in 6..8 -> "Summer"
                in 9..11 -> "Autumn"
                else -> "Unknown"
            }
        }
    }
}
