package ai.scorers

import ai.models.Impact
import ai.models.RecommendationReason
import ai.models.TripContext
import dto.Vehicle
import kotlin.math.min

class CapacityScorer {
    
    fun score(
        vehicle: Vehicle,
        context: TripContext,
        reasons: MutableList<RecommendationReason>
    ): Float {
        val passengerScore = scorePassengers(vehicle, context, reasons)
        val luggageScore = scoreLuggage(vehicle, context, reasons)
        
        // Average of both scores
        return (passengerScore + luggageScore) / 2f
    }
    
    private fun scorePassengers(
        vehicle: Vehicle,
        context: TripContext,
        reasons: MutableList<RecommendationReason>
    ): Float {
        val vehicleCapacity = vehicle.passengersCount
        val needed = context.passengers
        
        return when {
            vehicleCapacity >= needed + 2 -> {
                // Extra space
                reasons.add(
                    RecommendationReason(
                        factor = "Spacious",
                        explanation = "Extra room for $vehicleCapacity passengers (${needed} needed)",
                        impact = Impact.MEDIUM
                    )
                )
                1.0f
            }
            vehicleCapacity >= needed -> {
                // Perfect fit
                0.9f
            }
            vehicleCapacity >= needed - 1 -> {
                // Slightly tight
                0.6f
            }
            else -> {
                // Not enough
                0.2f
            }
        }
    }
    
    private fun scoreLuggage(
        vehicle: Vehicle,
        context: TripContext,
        reasons: MutableList<RecommendationReason>
    ): Float {
        val vehicleBags = vehicle.bagsCount
        val needed = context.luggage
        
        return when {
            needed == 0 -> 1.0f // No luggage needed, all good
            vehicleBags >= needed * 2 -> {
                reasons.add(
                    RecommendationReason(
                        factor = "Ample Storage",
                        explanation = "Plenty of trunk space for luggage",
                        impact = Impact.MEDIUM
                    )
                )
                1.0f
            }
            vehicleBags >= needed -> 0.9f
            vehicleBags >= needed - 1 -> 0.6f
            else -> 0.3f
        }
    }
}
