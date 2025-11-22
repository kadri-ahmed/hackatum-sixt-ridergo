package ai.models

import kotlinx.serialization.Serializable

@Serializable
data class TripContext(
    val destination: String = "",
    val passengers: Int = 1,
    val luggage: Int = 0,
    val terrain: TerrainType = TerrainType.MIXED,
    val weather: WeatherCondition = WeatherCondition.SUNNY,
    val tripPurpose: TripPurpose = TripPurpose.LEISURE,
    val duration: Int = 1 // days
)

@Serializable
enum class TerrainType {
    CITY,       // Urban driving, traffic
    HIGHWAY,    // Long-distance, high-speed
    MOUNTAIN,   // Hills, steep roads, rough terrain
    MIXED       // Combination
}

@Serializable
enum class WeatherCondition {
    SUNNY,      // Clear, dry conditions
    RAINY,      // Wet roads
    SNOWY,      // Winter conditions
    MIXED       // Variable weather
}

@Serializable
enum class TripPurpose {
    BUSINESS,   // Professional, meetings
    LEISURE,    // Vacation, sightseeing
    FAMILY,     // Family trip, children
    ADVENTURE   // Outdoor activities, exploration
}
