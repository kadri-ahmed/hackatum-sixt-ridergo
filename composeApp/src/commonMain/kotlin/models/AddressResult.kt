package models

data class AddressResult(
    val id: String,
    val name: String,
    val formattedAddress: String,
    val latitude: Double,
    val longitude: Double
)
