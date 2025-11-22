package models

import kotlinx.serialization.Serializable

@Serializable
data class ReservationBlockDateTime(
    val date: String,
    val timeZone: String
)