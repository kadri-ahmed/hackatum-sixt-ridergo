package ai.models

import dto.Vehicle

data class VehicleRecommendation(
    val vehicle: Vehicle,
    val score: Float, // 0.0 to 1.0
    val matchPercentage: Int, // 0 to 100
    val reasons: List<RecommendationReason>,
    val rank: Int = 0
) {
    companion object {
        fun fromScore(vehicle: Vehicle, score: Float, reasons: List<RecommendationReason>): VehicleRecommendation {
            return VehicleRecommendation(
                vehicle = vehicle,
                score = score,
                matchPercentage = (score * 100).toInt().coerceIn(0, 100),
                reasons = reasons
            )
        }
    }
}

data class RecommendationReason(
    val factor: String,
    val explanation: String,
    val impact: Impact
)

enum class Impact {
    HIGH,    // Major factor in recommendation
    MEDIUM,  // Moderate influence
    LOW      // Minor consideration
}
