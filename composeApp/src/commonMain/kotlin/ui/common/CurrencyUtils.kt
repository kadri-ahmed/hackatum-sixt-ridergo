package ui.common

import kotlin.math.round

fun getCurrencySymbol(currencyCode: String): String {
    return when (currencyCode.uppercase()) {
        "EUR", "EURO" -> "€"
        "USD" -> "$"
        "GBP" -> "£"
        "JPY" -> "¥"
        "CHF" -> "Fr."
        else -> currencyCode
    }
}

fun formatPrice(amount: Double): String {
    val rounded = round(amount * 100) / 100.0
    val str = rounded.toString()
    val parts = str.split(".")
    return if (parts.size > 1) {
        val decimals = parts[1]
        if (decimals.length == 1) {
            "${parts[0]}.${decimals}0"
        } else {
            str
        }
    } else {
        "$str.00"
    }
}
