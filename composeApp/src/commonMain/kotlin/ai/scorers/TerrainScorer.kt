package ai.scorers

import ai.models.Impact
import ai.models.RecommendationReason
import ai.models.TerrainType
import dto.Vehicle

class TerrainScorer {
    
    fun score(
        vehicle: Vehicle,
        terrain: TerrainType,
        reasons: MutableList<RecommendationReason>
    ): Float {
        val score = when (terrain) {
            TerrainType.MOUNTAIN -> scoreMountain(vehicle, reasons)
            TerrainType.CITY -> scoreCity(vehicle, reasons)
            TerrainType.HIGHWAY -> scoreHighway(vehicle, reasons)
            TerrainType.MIXED -> 0.7f // baseline for mixed terrain
        }
        
        return score.coerceIn(0f, 1f)
    }
    
    private fun scoreMountain(vehicle: Vehicle, reasons: MutableList<RecommendationReason>): Float {
        val has4WD = vehicle.attributes.any { 
            it.value.contains("4WD", ignoreCase = true) || 
            it.value.contains("AWD", ignoreCase = true) ||
            it.key.contains("DRIVETRAIN", ignoreCase = true)
        }
        
        val isSUV = vehicle.groupType.equals("SUV", ignoreCase = true)
        
        return when {
            has4WD && isSUV -> {
                reasons.add(
                    RecommendationReason(
                        factor = "Mountain Ready",
                        explanation = "4WD/AWD drivetrain perfect for mountain terrain",
                        impact = Impact.HIGH
                    )
                )
                1.0f
            }
            has4WD -> {
                reasons.add(
                    RecommendationReason(
                        factor = "All-Wheel Drive",
                        explanation = "4WD provides better traction on steep roads",
                       impact = Impact.HIGH
                    )
                )
                0.9f
            }
            isSUV -> 0.6f
            else -> 0.3f
        }
    }
    
    private fun scoreCity(vehicle: Vehicle, reasons: MutableList<RecommendationReason>): Float {
        val isElectric = vehicle.fuelType.equals("Electric", ignoreCase = true)
        val isCompact = vehicle.groupType.equals("COMPACT", ignoreCase = true) ||
                        vehicle.groupType.equals("SEDAN", ignoreCase = true)
        
        return when {
            isElectric && isCompact -> {
                reasons.add(
                    RecommendationReason(
                        factor = "City Perfect",
                        explanation = "Electric and compact, ideal for urban driving",
                        impact = Impact.HIGH
                    )
                )
                1.0f
            }
            isElectric -> 0.9f
            isCompact -> 0.8f
            else -> 0.6f
        }
    }
    
    private fun scoreHighway(vehicle: Vehicle, reasons: MutableList<RecommendationReason>): Float {
        val isAutomatic = vehicle.transmissionType.equals("Automatic", ignoreCase = true)
        val isComfortable = vehicle.groupType.equals("SEDAN", ignoreCase = true) ||
                           vehicle.isMoreLuxury
        
        return when {
            isAutomatic && isComfortable -> {
                reasons.add(
                    RecommendationReason(
                        factor = "Highway Cruiser",
                        explanation = "Automatic transmission and comfort for long drives",
                        impact = Impact.MEDIUM
                    )
                )
                1.0f
            }
            isAutomatic -> 0.9f
            else -> 0.7f
        }
    }
}
