package dto

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val name: String,
    val age: String, // String to handle text input easily, convert later if needed
    val licenseType: String, // "Manual" or "Automatic"
    val travelPreference: String // "Business" or "Leisure"
)
