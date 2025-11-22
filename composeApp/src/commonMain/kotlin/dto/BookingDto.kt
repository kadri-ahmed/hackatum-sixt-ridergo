package dto

import kotlinx.serialization.Serializable

@Serializable
data class BookingDto (
    val bookedCategory: String,
    val selectedVehicle: Vehicle? = null,
    val protectionPackages: String? = null,
    val status: String,
    val createdAt: String,
    val id: String,
)

@Serializable
data class CreateBookingDto (
    val id: String,
)
