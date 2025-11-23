package utils

/**
 * Multiplatform-compatible number formatting utilities
 */
object NumberFormatter {
    /**
     * Format a double to 2 decimal places
     * Works on all platforms (Android, iOS, etc.)
     */
    fun formatCurrency(amount: Double): String {
        // Simple approach: round to 2 decimal places and format
        val rounded = (amount * 100).toInt()
        val whole = rounded / 100
        val decimal = rounded % 100
        return "$whole.${decimal.toString().padStart(2, '0')}"
    }
}
