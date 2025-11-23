package recommendations

import dto.Deal
import dto.Vehicle
import dto.Pricing
import models.DestinationContext
import models.TripPurpose

/**
 * Recommendation reason explaining why a vehicle is recommended
 */
data class RecommendationReason(
    val title: String,
    val description: String,
    val priority: Int // Higher priority = more important reason
)

/**
 * Scored deal with recommendation reasons
 */
data class ScoredDeal(
    val deal: Deal,
    val score: Double,
    val reasons: List<RecommendationReason>
)

/**
 * Vehicle Recommendation Engine
 * 
 * Scores vehicles based on multiple factors:
 * - Price value (discount percentage, total cost)
 * - Vehicle features matching user needs (passengers, bags, fuel type)
 * - Context matching (trip purpose, destination type)
 * - Vehicle quality indicators (new car, luxury, recommended flag)
 * - User preferences (from past bookings and behavior)
 * - Availability and status
 */
class VehicleRecommendationEngine(
    private val destinationContext: DestinationContext? = null,
    private val userProfile: models.UserProfile? = null
) {
    
    /**
     * Score and rank deals based on multiple factors
     */
    fun scoreAndRankDeals(deals: List<Deal>): List<ScoredDeal> {
        return deals.map { deal ->
            val (score, reasons) = calculateScore(deal)
            ScoredDeal(deal, score, reasons)
        }
        .sortedByDescending { it.score }
    }
    
    /**
     * Calculate recommendation score for a deal
     * Returns score (0.0 to 100.0) and list of reasons
     */
    private fun calculateScore(deal: Deal): Pair<Double, List<RecommendationReason>> {
        var score = 0.0
        val reasons = mutableListOf<RecommendationReason>()
        
        // 1. Price Value Score (0-25 points)
        val priceScore = calculatePriceScore(deal.pricing)
        score += priceScore.first
        reasons.addAll(priceScore.second)
        
        // 2. Feature Matching Score (0-20 points)
        val featureScore = calculateFeatureMatchingScore(deal.vehicle)
        score += featureScore.first
        reasons.addAll(featureScore.second)
        
        // 3. Context Matching Score (0-15 points)
        val contextScore = calculateContextMatchingScore(deal)
        score += contextScore.first
        reasons.addAll(contextScore.second)
        
        // 4. User Preferences Score (0-20 points) - NEW!
        val preferencesScore = calculateUserPreferencesScore(deal)
        score += preferencesScore.first
        reasons.addAll(preferencesScore.second)
        
        // 5. Quality Indicators Score (0-10 points)
        val qualityScore = calculateQualityScore(deal.vehicle)
        score += qualityScore.first
        reasons.addAll(qualityScore.second)
        
        // 6. Deal Attractiveness Score (0-10 points)
        val dealScore = calculateDealAttractivenessScore(deal)
        score += dealScore.first
        reasons.addAll(dealScore.second)
        
        return Pair(score.coerceIn(0.0, 100.0), reasons.sortedByDescending { it.priority })
    }
    
    /**
     * Calculate score based on user's historical preferences
     * Privacy-respecting: Only uses anonymized aggregated data
     */
    private fun calculateUserPreferencesScore(deal: Deal): Pair<Double, List<RecommendationReason>> {
        var score = 0.0
        val reasons = mutableListOf<RecommendationReason>()
        
        val profile = userProfile ?: return Pair(score, reasons)
        
        // Brand preference (0-7 points)
        if (profile.preferredBrands.contains(deal.vehicle.brand)) {
            score += 7.0
            reasons.add(
                RecommendationReason(
                    title = "Your Preferred Brand",
                    description = "You've rented ${deal.vehicle.brand} vehicles before",
                    priority = 4
                )
            )
        }
        
        // Vehicle type preference (0-6 points)
        if (profile.preferredVehicleTypes.contains(deal.vehicle.groupType)) {
            score += 6.0
            reasons.add(
                RecommendationReason(
                    title = "Matches Your Style",
                    description = "You prefer ${deal.vehicle.groupType} vehicles",
                    priority = 3
                )
            )
        }
        
        // Fuel type preference (0-4 points)
        if (profile.preferredFuelTypes.contains(deal.vehicle.fuelType)) {
            score += 4.0
            reasons.add(
                RecommendationReason(
                    title = "Your Preferred Fuel Type",
                    description = "You often choose ${deal.vehicle.fuelType} vehicles",
                    priority = 2
                )
            )
        }
        
        // Budget range matching (0-3 points)
        profile.averageBudgetRange?.let { budgetRange ->
            val price = deal.pricing.totalPrice.amount
            if (price >= budgetRange.min && price <= budgetRange.max) {
                score += 3.0
                reasons.add(
                    RecommendationReason(
                        title = "Within Your Budget Range",
                        description = "Matches your typical spending",
                        priority = 3
                    )
                )
            }
        }
        
        return Pair(score, reasons)
    }
    
    /**
     * Calculate price value score based on discounts and total cost
     */
    private fun calculatePriceScore(pricing: Pricing): Pair<Double, List<RecommendationReason>> {
        var score = 0.0
        val reasons = mutableListOf<RecommendationReason>()
        
        // Discount percentage (0-15 points)
        val discountScore = (pricing.discountPercentage / 100.0) * 15.0
        score += discountScore
        if (pricing.discountPercentage > 0) {
            reasons.add(
                RecommendationReason(
                    title = "${pricing.discountPercentage}% Discount",
                    description = "Great savings on this vehicle",
                    priority = if (pricing.discountPercentage >= 20) 5 else 3
                )
            )
        }
        
        // Price competitiveness (0-15 points)
        // Lower total price = higher score (normalized)
        // This would ideally compare against average prices, but for now we'll use a simple heuristic
        val totalPrice = pricing.totalPrice.amount
        val priceScore = when {
            totalPrice < 50 -> 15.0
            totalPrice < 100 -> 12.0
            totalPrice < 150 -> 8.0
            totalPrice < 200 -> 5.0
            else -> 2.0
        }
        score += priceScore
        
        if (totalPrice < 100) {
            reasons.add(
                RecommendationReason(
                    title = "Great Value",
                    description = "Competitive pricing for this vehicle class",
                    priority = 4
                )
            )
        }
        
        return Pair(score, reasons)
    }
    
    /**
     * Calculate feature matching score based on user needs
     */
    private fun calculateFeatureMatchingScore(vehicle: Vehicle): Pair<Double, List<RecommendationReason>> {
        var score = 0.0
        val reasons = mutableListOf<RecommendationReason>()
        
        val travelerCount = destinationContext?.travelerCount ?: 1
        
        // Passenger capacity matching (0-10 points)
        val passengerMatch = when {
            vehicle.passengersCount >= travelerCount + 1 -> 10.0 // Extra space
            vehicle.passengersCount >= travelerCount -> 8.0 // Perfect match
            vehicle.passengersCount >= travelerCount - 1 -> 5.0 // Slightly tight
            else -> 0.0 // Insufficient
        }
        score += passengerMatch
        
        if (passengerMatch >= 8.0) {
            reasons.add(
                RecommendationReason(
                    title = "Perfect Capacity",
                    description = "Seats ${vehicle.passengersCount} passengers comfortably",
                    priority = 5
                )
            )
        }
        
        // Bag capacity (0-8 points)
        // Assume average need: 1 bag per traveler + 1 extra
        val expectedBags = (travelerCount + 1).coerceAtLeast(2)
        val bagMatch = when {
            vehicle.bagsCount >= expectedBags + 1 -> 8.0
            vehicle.bagsCount >= expectedBags -> 6.0
            vehicle.bagsCount >= expectedBags - 1 -> 4.0
            else -> 2.0
        }
        score += bagMatch
        
        if (bagMatch >= 6.0) {
            reasons.add(
                RecommendationReason(
                    title = "Spacious Storage",
                    description = "Fits ${vehicle.bagsCount} bags comfortably",
                    priority = 3
                )
            )
        }
        
        // Fuel efficiency (0-7 points)
        val fuelScore = when (vehicle.fuelType.lowercase()) {
            "electric", "hybrid" -> 7.0
            "diesel" -> 5.0
            "petrol" -> 3.0
            else -> 2.0
        }
        score += fuelScore
        
        if (fuelScore >= 5.0) {
            reasons.add(
                RecommendationReason(
                    title = "Eco-Friendly",
                    description = "${vehicle.fuelType.capitalize()} fuel for better efficiency",
                    priority = 2
                )
            )
        }
        
        return Pair(score, reasons)
    }
    
    /**
     * Calculate context matching score based on trip purpose and destination
     */
    private fun calculateContextMatchingScore(deal: Deal): Pair<Double, List<RecommendationReason>> {
        var score = 0.0
        val reasons = mutableListOf<RecommendationReason>()
        
        val purpose = destinationContext?.purpose ?: TripPurpose.UNKNOWN
        
        // Trip purpose matching (0-12 points)
        val purposeScore = when (purpose) {
            TripPurpose.FAMILY -> {
                when {
                    deal.vehicle.passengersCount >= 5 -> 12.0
                    deal.vehicle.passengersCount >= 4 -> 8.0
                    deal.vehicle.groupType.contains("SUV", ignoreCase = true) -> 6.0
                    else -> 2.0
                }
            }
            TripPurpose.BUSINESS -> {
                when {
                    deal.vehicle.isMoreLuxury -> 10.0
                    deal.vehicle.groupType.contains("Sedan", ignoreCase = true) -> 8.0
                    deal.vehicle.groupType.contains("Premium", ignoreCase = true) -> 8.0
                    else -> 4.0
                }
            }
            TripPurpose.VACATION -> {
                when {
                    deal.vehicle.groupType.contains("SUV", ignoreCase = true) -> 10.0
                    deal.vehicle.groupType.contains("Convertible", ignoreCase = true) -> 8.0
                    deal.vehicle.bagsCount >= 4 -> 6.0
                    else -> 4.0
                }
            }
            TripPurpose.UNKNOWN -> 5.0 // Neutral score
        }
        score += purposeScore
        
        if (purposeScore >= 8.0) {
            val purposeText = when (purpose) {
                TripPurpose.FAMILY -> "Perfect for family trips"
                TripPurpose.BUSINESS -> "Ideal for business travel"
                TripPurpose.VACATION -> "Great for vacation adventures"
                TripPurpose.UNKNOWN -> "Well-suited for your trip"
            }
            reasons.add(
                RecommendationReason(
                    title = purposeText,
                    description = "Matches your trip purpose",
                    priority = 4
                )
            )
        }
        
        // Vehicle type for terrain (0-8 points)
        // Check if vehicle has 4WD or AWD for mountain/rough terrain
        val has4WD = deal.vehicle.tyreType.contains("4WD", ignoreCase = true) ||
                     deal.vehicle.tyreType.contains("AWD", ignoreCase = true) ||
                     deal.vehicle.upsellReasons.any { 
                         it.title.contains("mountain", ignoreCase = true) ||
                         it.title.contains("4WD", ignoreCase = true) ||
                         it.title.contains("AWD", ignoreCase = true)
                     }
        
        if (has4WD) {
            score += 8.0
            reasons.add(
                RecommendationReason(
                    title = "All-Terrain Capable",
                    description = "4WD/AWD for challenging road conditions",
                    priority = 5
                )
            )
        }
        
        return Pair(score, reasons)
    }
    
    /**
     * Calculate quality indicators score
     */
    private fun calculateQualityScore(vehicle: Vehicle): Pair<Double, List<RecommendationReason>> {
        var score = 0.0
        val reasons = mutableListOf<RecommendationReason>()
        
        // New car bonus (0-5 points)
        if (vehicle.isNewCar) {
            score += 5.0
            reasons.add(
                RecommendationReason(
                    title = "Brand New",
                    description = "Latest model year",
                    priority = 3
                )
            )
        }
        
        // Recommended flag (0-5 points)
        if (vehicle.isRecommended) {
            score += 5.0
            reasons.add(
                RecommendationReason(
                    title = "Highly Rated",
                    description = "Top choice among customers",
                    priority = 4
                )
            )
        }
        
        // Luxury indicator (0-5 points)
        if (vehicle.isMoreLuxury) {
            score += 5.0
            reasons.add(
                RecommendationReason(
                    title = "Premium Experience",
                    description = "Luxury features included",
                    priority = 3
                )
            )
        }
        
        return Pair(score, reasons)
    }
    
    /**
     * Calculate deal attractiveness score
     */
    private fun calculateDealAttractivenessScore(deal: Deal): Pair<Double, List<RecommendationReason>> {
        var score = 0.0
        val reasons = mutableListOf<RecommendationReason>()
        
        // Exciting discount tag (0-5 points)
        if (deal.vehicle.isExcitingDiscount) {
            score += 5.0
            reasons.add(
                RecommendationReason(
                    title = "Special Offer",
                    description = "Limited-time exciting discount",
                    priority = 4
                )
            )
        }
        
        // Deal info and tags (0-3 points)
        if (deal.dealInfo.isNotBlank()) {
            score += 2.0
        }
        
        if (deal.tags.isNotEmpty()) {
            score += 1.0
            val tagText = deal.tags.take(2).joinToString(", ")
            reasons.add(
                RecommendationReason(
                    title = "Special Features",
                    description = tagText,
                    priority = 1
                )
            )
        }
        
        // Upsell reasons (0-2 points)
        if (deal.vehicle.upsellReasons.isNotEmpty()) {
            score += 2.0
        }
        
        return Pair(score, reasons)
    }
    
    /**
     * Get top N recommended deals
     */
    fun getTopRecommendations(deals: List<Deal>, count: Int = 3): List<ScoredDeal> {
        return scoreAndRankDeals(deals).take(count)
    }
    
    /**
     * Generate personalized recommendation message
     */
    fun generateRecommendationMessage(scoredDeals: List<ScoredDeal>): String {
        if (scoredDeals.isEmpty()) {
            return "We found some great options for you!"
        }
        
        val topDeal = scoredDeals.first()
        val topReason = topDeal.reasons.firstOrNull()
        
        val purposeContext = when (destinationContext?.purpose) {
            TripPurpose.FAMILY -> "family trip"
            TripPurpose.BUSINESS -> "business trip"
            TripPurpose.VACATION -> "vacation"
            TripPurpose.UNKNOWN -> "trip"
            null -> "trip"
        }
        
        return when {
            scoredDeals.size == 1 -> {
                "We found the perfect match for your $purposeContext!"
            }
            scoredDeals.size <= 3 -> {
                "We found ${scoredDeals.size} perfect matches for your $purposeContext!"
            }
            else -> {
                "We found ${scoredDeals.size} great options for your $purposeContext!"
            }
        }
    }
}
