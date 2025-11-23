package dto

import kotlinx.serialization.Serializable

@Serializable
data class SavedBooking(
    val id: String,
    val bookingId: String,
    val vehicle: Deal,
    val protectionPackage: ProtectionPackageDto?,
    val addonIds: Set<String>,
    val timestamp: Long,
    val totalPrice: Double,
    val currency: String,
    val status: BookingStatus = BookingStatus.DRAFT
)

enum class BookingStatus {
    DRAFT,
    CONFIRMED
}
