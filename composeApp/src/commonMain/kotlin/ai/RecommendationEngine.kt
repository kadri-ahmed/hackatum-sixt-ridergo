package ai

import ai.models.*
import ai.scorers.*
import dto.Vehicle

/**
 * AI-powered recommendation engine that scores vehicles based on trip context
 */
class RecommendationEngine {
    
    private val terrainScorer = TerrainScorer()
    private val weatherScorer = WeatherScorer()
    private val capacityScorer = CapacityScorer()
    private val purposeScorer = PurposeScorer()
    
    // Scoring weights (must sum to 1.0)
    private val weights = ScoringWeights(
        terrain = 0.25f,
        weather = 0.20f,
        capacity = 0.25f,
        purpose = 0.20f,
        userPreference = 0.10f
    )
    
    /**
     * Score and rank all vehicles based on trip context
     */
    fun scoreVehicles(
        vehicles: List<Vehicle>,
        context: TripContext,
        userPreferences: UserPreferences = UserPreferences()
    ): List<VehicleRecommendation> {
        return vehicles
            .map { vehicle -> scoreVehicle(vehicle, context, userPreferences) }
            .sortedByDescending { it.score }
            .mapIndexed { index, recommendation ->
                recommendation.copy(rank = index + 1)
            }
    }
    
    /**
     * Score a single vehicle
     */
    fun scoreVehicle(
        vehicle: Vehicle,
        context: TripContext,
        userPreferences: UserPreferences = UserPreferences()
    ): VehicleRecommendation {
        val reasons = mutableListOf<RecommendationReason>()
        
        // Calculate individual scores
        val terrainScore = terrainScorer.score(vehicle, context.terrain, reasons)
        val weatherScore = weatherScorer.score(vehicle, context.weather, reasons)
        val capacityScore = capacityScorer.score(vehicle, context, reasons)
        val purposeScore = purposeScorer.score(vehicle, context.tripPurpose, reasons)
        val preferenceScore = scoreUserPreference(vehicle, userPreferences, reasons)
        
        // Weighted total score
        val totalScore = (
            terrainScore * weights.terrain +
            weatherScore * weights.weather +
            capacityScore * weights.capacity +
            purposeScore * weights.purpose +
            preferenceScore * weights.userPreference
        ).coerceIn(0f, 1f)
        
        return VehicleRecommendation.fromScore(
            vehicle = vehicle,
            score = totalScore,
            reasons = reasons.sortedByDescending { it.impact }
        )
    }
    
    private fun scoreUserPreference(
        vehicle: Vehicle,
        preferences: UserPreferences,
        reasons: MutableList<RecommendationReason>
    ): Float {
        var score = 0.5f // baseline
        
        // Brand preference
        if (preferences.preferredBrands.contains(vehicle.brand)) {
            score += 0.3f
            reasons.add(
                RecommendationReason(
                    factor = "Brand Match",
                    explanation = "Matches your preferred brand: ${vehicle.brand}",
                    impact = Impact.MEDIUM
                )
            )
        }
        
        // Transmission preference
        if (preferences.preferredTransmission == vehicle.transmissionType) {
            score += 0.2f
        }
        
        return score.coerceIn(0f, 1f)
    }
}

data class ScoringWeights(
    val terrain: Float,
    val weather: Float,
    val capacity: Float,
    val purpose: Float,
    val userPreference: Float
)
