package ai.scorers

import ai.models.Impact
import ai.models.RecommendationReason
import ai.models.TripPurpose
import dto.Vehicle

class PurposeScorer {
    
    fun score(
        vehicle: Vehicle,
        purpose: TripPurpose,
        reasons: MutableList<RecommendationReason>
    ): Float {
        val score = when (purpose) {
            TripPurpose.BUSINESS -> scoreBusiness(vehicle, reasons)
            TripPurpose.LEISURE -> scoreLeisure(vehicle, reasons)
            TripPurpose.FAMILY -> scoreFamily(vehicle, reasons)
            TripPurpose.ADVENTURE -> scoreAdventure(vehicle, reasons)
        }
        
        return score.coerceIn(0f, 1f)
    }
    
    private fun scoreBusiness(vehicle: Vehicle, reasons: MutableList<RecommendationReason>): Float {
        val isLuxury = vehicle.isMoreLuxury || vehicle.vehicleCost.value > 30000
        val isProfessional = vehicle.groupType.equals("SEDAN", ignoreCase = true) ||
                            vehicle.isNewCar
        
        return when {
            isLuxury && isProfessional -> {
                reasons.add(
                    RecommendationReason(
                        factor = "Business Class",
                        explanation = "Luxury and professional for business meetings",
                        impact = Impact.HIGH
                    )
                )
                1.0f
            }
            isLuxury -> 0.9f
            isProfessional -> 0.8f
            else -> 0.6f
        }
    }
    
    private fun scoreLeisure(vehicle: Vehicle, reasons: MutableList<RecommendationReason>): Float {
        val isComfortable = vehicle.passengersCount >= 4
        val isExciting = vehicle.isExcitingDiscount || vehicle.isRecommended
        
        return when {
            isComfortable && isExciting -> {
                reasons.add(
                    RecommendationReason(
                        factor = "Leisure Perfect",
                        explanation = "Comfortable and exciting for vacation",
                        impact = Impact.MEDIUM
                    )
                )
                1.0f
            }
            isComfortable -> 0.8f
            else -> 0.7f
        }
    }
    
    private fun scoreFamily(vehicle: Vehicle, reasons: MutableList<RecommendationReason>): Float {
        val isSpacious = vehicle.passengersCount >= 5
        val hasTrunkSpace = vehicle.bagsCount >= 3
        val isSafe = vehicle.isNewCar // Newer cars have better safety
        
        return when {
            isSpacious && hasTrunkSpace && isSafe -> {
                reasons.add(
                    RecommendationReason(
                        factor = "Family Friendly",
                        explanation = "Spacious, safe, and practical for family trips",
                        impact = Impact.HIGH
                    )
                )
                1.0f
            }
            isSpacious && hasTrunkSpace -> 0.9f
            isSpacious -> 0.7f
            else -> 0.5f
        }
    }
    
    private fun scoreAdventure(vehicle: Vehicle, reasons: MutableList<RecommendationReason>): Float {
        val isSUV = vehicle.groupType.equals("SUV", ignoreCase = true)
        val has4WD = vehicle.attributes.any {
            it.value.contains("4WD", ignoreCase = true) ||
            it.value.contains("AWD", ignoreCase = true)
        }
        val isRugged = isSUV || has4WD
        
        return when {
            isSUV && has4WD -> {
                reasons.add(
                    RecommendationReason(
                        factor = "Adventure Ready",
                        explanation = "Rugged SUV with 4WD for outdoor exploration",
                        impact = Impact.HIGH
                    )
                )
                1.0f
            }
            isRugged -> 0.8f
            else -> 0.5f
        }
    }
}
