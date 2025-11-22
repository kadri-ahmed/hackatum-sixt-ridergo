package dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val city: String,
    val country: String,
    val airportCode: String? = null
)
