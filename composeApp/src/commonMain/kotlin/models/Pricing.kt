package models

import kotlinx.serialization.Serializable

@Serializable
data class Pricing(
    val discountPercentage: Int,
    val displayPrice: Price,
    val totalPrice: Price
)

@Serializable
data class Price(
    val currency: String,
    val amount: Double,
    val prefix: String = "",
    val suffix: String = ""
)