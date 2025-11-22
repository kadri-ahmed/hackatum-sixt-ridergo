package ai.scorers

import ai.models.Impact
import ai.models.RecommendationReason
import ai.models.WeatherCondition
import dto.Vehicle

class WeatherScorer {
    
    fun score(
        vehicle: Vehicle,
        weather: WeatherCondition,
        reasons: MutableList<RecommendationReason>
    ): Float {
        val score = when (weather) {
            WeatherCondition.SNOWY -> scoreSnowy(vehicle, reasons)
            WeatherCondition.RAINY -> scoreRainy(vehicle, reasons)
            WeatherCondition.SUNNY -> scoreSunny(vehicle, reasons)
            WeatherCondition.MIXED -> 0.8f // baseline
        }
        
        return score.coerceIn(0f, 1f)
    }
    
    private fun scoreSnowy(vehicle: Vehicle, reasons: MutableList<RecommendationReason>): Float {
        val hasAllSeasonTires = vehicle.tyreType.contains("All", ignoreCase = true) ||
                                vehicle.tyreType.contains("Winter", ignoreCase = true)
        val has4WD = vehicle.attributes.any {
            it.value.contains("4WD", ignoreCase = true) ||
            it.value.contains("AWD", ignoreCase = true)
        }
        
        return when {
            hasAllSeasonTires && has4WD -> {
                reasons.add(
                    RecommendationReason(
                        factor = "Winter Ready",
                        explanation = "All-season tires and 4WD for snowy conditions",
                        impact = Impact.HIGH
                    )
                )
                1.0f
            }
            hasAllSeasonTires -> 0.8f
            has4WD -> 0.7f
            else -> 0.4f
        }
    }
    
    private fun scoreRainy(vehicle: Vehicle, reasons: MutableList<RecommendationReason>): Float {
        val hasAllSeasonTires = vehicle.tyreType.contains("All", ignoreCase = true)
        
        return when {
            hasAllSeasonTires -> {
                reasons.add(
                    RecommendationReason(
                        factor = "Rainy Day Safe",
                        explanation = "All-season tires provide good grip in wet conditions",
                        impact = Impact.MEDIUM
                    )
                )
                1.0f
            }
            else -> 0.7f
        }
    }
    
    private fun scoreSunny(vehicle: Vehicle, reasons: MutableList<RecommendationReason>): Float {
        // For sunny weather, most vehicles score well
        // Convertibles get a bonus
        val isConvertible = vehicle.attributes.any {
            it.value.contains("convertible", ignoreCase = true)
        }
        
        return when {
            isConvertible -> {
                reasons.add(
                    RecommendationReason(
                        factor = "Perfect Weather",
                        explanation = "Enjoy the sunshine with a convertible",
                        impact = Impact.MEDIUM
                    )
                )
                1.0f
            }
            else -> 0.8f // All vehicles are generally good in sunny weather
        }
    }
}
