package models

import kotlinx.serialization.Serializable

@Serializable
data class Deal(
    val vehicle: Vehicle,
    val pricing: Pricing,
    val dealInfo: String,
    val tags: List<String> = emptyList(),
    val priceTag: String? = null
)