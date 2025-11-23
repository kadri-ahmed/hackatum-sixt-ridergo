package dto

import kotlinx.serialization.Serializable

@Serializable
data class BookingDto (
    val bookedCategory: String,
    val selectedVehicle: Deal? = null,
    val protectionPackages: ProtectionPackageDto? = null,
    val addons: List<AddonOption>? = null,
    val status: String,
    val createdAt: String,
    val id: String,
)

@Serializable
data class CreateBookingDto (
    val id: String,
)
