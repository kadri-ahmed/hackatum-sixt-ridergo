package ai.models

import kotlinx.serialization.Serializable

/**
 * User preferences learned from historical selections and ratings
 */
@Serializable
data class UserPreferences(
    val preferredBrands: List<String> = emptyList(),
    val preferredTransmission: String? = null, // "Automatic" or "Manual"
    val preferredFuelType: String? = null, // "Electric", "Petrol", "Diesel"
    val luxuryPreference: Float = 0.5f, // 0.0 = economy, 1.0 = luxury
    val sizePreference: VehicleSize = VehicleSize.MEDIUM
)

@Serializable
enum class VehicleSize {
    COMPACT,    // Small cars
    MEDIUM,     // Standard sedans
    LARGE,      // SUVs, vans
    LUXURY      // Premium vehicles
}
