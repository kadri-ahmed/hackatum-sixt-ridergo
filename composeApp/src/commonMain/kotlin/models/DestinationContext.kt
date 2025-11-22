package models

import dto.LocationDto
import kotlinx.serialization.Serializable

@Serializable
data class DestinationContext(
    val location: LocationDto,
    val startDate: String,                                  // ISO 8601 recommended: "2025-12-01T09:00:00+01:00"
    val endDate: String,                                // ISO 8601
    val tripType: TripType = TripType.ROUND_TRIP,       // e.g., one-way/round-trip, useful for logic
    val travelerCount: Int = 1,                         // Single traveler as default
    val purpose: TripPurpose = TripPurpose.UNKNOWN      // e.g., "business", "vacation"

    // ---------- Extensions -----------------------
    //    Add fields below as new context becomes useful:
    //
    //    val luggageCount: Int? = null
    //
    //    val specialNeeds: SpecialNeedsContext? = null
    //
    //    val environment: EnvironmentContextDto? = null
    //
    //    val plannedRoutes: List<RoutePlanDto> = emptyList()
    //
    //    val preferences: UserPreferenceContext? = null
)

enum class TripType { ONE_WAY, ROUND_TRIP }
enum class TripPurpose { BUSINESS, VACATION, FAMILY, UNKNOWN }
