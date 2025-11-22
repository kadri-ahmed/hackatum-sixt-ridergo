package models

import kotlinx.serialization.Serializable

@Serializable
data class AvailableVehiclesResponse(
    val reservationId: String,
    val deals: List<Deal>,
    val totalVehicles: Int,
    val reservationBlockDateTime: ReservationBlockDateTime? = null,
    val filter: Filter? = null,
    val quickFilters: List<QuickFilter>? = null,
    val terminalList: List<String>? = null,
    val isBundleSelected: Boolean? = null
)