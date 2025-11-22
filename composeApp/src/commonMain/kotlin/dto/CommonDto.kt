package dto

import kotlinx.serialization.Serializable

@Serializable
data class Price(
    val currency: String,
    val amount: Double,
    val suffix: String
)

@Serializable
data class PriceInfo(
    val discountPercentage: Int = 0,
    val displayPrice: Price,
    val listPrice: Price? = null,
    val totalPrice: Price? = null
)
