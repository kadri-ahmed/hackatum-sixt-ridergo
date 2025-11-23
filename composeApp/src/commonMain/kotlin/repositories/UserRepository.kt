package repositories

import com.russhwolf.settings.Settings
import dto.UserProfile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface UserRepository {
    fun saveProfile(profile: UserProfile)
    fun getProfile(): UserProfile?
    fun clearProfile()
    fun hasProfile(): Boolean
}

class UserRepositoryImpl(
    private val settings: Settings
) : UserRepository {
    
    private val PROFILE_KEY = "user_profile"
    private val json = Json { ignoreUnknownKeys = true }

    override fun saveProfile(profile: UserProfile) {
        try {
            val profileString = json.encodeToString(profile)
            settings.putString(PROFILE_KEY, profileString)
        } catch (e: Exception) {
            println("Failed to save profile: $e")
        }
    }

    override fun getProfile(): UserProfile? {
        val profileString = settings.getString(PROFILE_KEY, "")
        if (profileString.isBlank()) return null
        
        return try {
            json.decodeFromString<UserProfile>(profileString)
        } catch (e: Exception) {
            println("Failed to load profile: $e")
            null
        }
    }

    override fun clearProfile() {
        settings.remove(PROFILE_KEY)
    }

    override fun hasProfile(): Boolean {
        return settings.getString(PROFILE_KEY, "").isNotBlank()
    }
}
