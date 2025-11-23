package repositories

import models.PrivacyConsent
import models.UserProfile
import utils.Storage
import utils.getCurrentTimestamp

/**
 * Repository for managing user profile
 * All data stored locally, respects privacy settings
 */
interface UserProfileRepository {
    suspend fun getUserProfile(): UserProfile
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun updatePrivacyConsent(consent: PrivacyConsent)
    suspend fun clearUserProfile()
}

class UserProfileRepositoryImpl(
    private val storage: Storage
) : UserProfileRepository {
    
    private val USER_PROFILE_KEY = "user_profile"
    private val PRIVACY_CONSENT_KEY = "privacy_consent"
    
    override suspend fun getUserProfile(): UserProfile {
        val serialized = storage.getPreference(USER_PROFILE_KEY)
        
        if (serialized == null) {
            // Return default profile with LOCAL_ONLY consent
            val consent = getPrivacyConsent()
            return UserProfile(
                privacyConsent = consent,
                dataRetentionDays = 90
            )
        }
        
        return try {
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            json.decodeFromString<UserProfile>(serialized)
        } catch (e: Exception) {
            // Return default on error
            UserProfile(
                privacyConsent = getPrivacyConsent(),
                dataRetentionDays = 90
            )
        }
    }
    
    override suspend fun saveUserProfile(profile: UserProfile) {
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        val serialized = json.encodeToString(UserProfile.serializer(), profile)
        storage.savePreference(USER_PROFILE_KEY, serialized)
        
        // Also save privacy consent separately for quick access
        storage.savePreference(PRIVACY_CONSENT_KEY, profile.privacyConsent.name)
    }
    
    override suspend fun updatePrivacyConsent(consent: PrivacyConsent) {
        val currentProfile = getUserProfile()
        val updatedProfile = currentProfile.copy(
            privacyConsent = consent,
            lastUpdated = getCurrentTimestamp()
        )
        saveUserProfile(updatedProfile)
        
        // If consent is NONE, clear all data
        if (consent == PrivacyConsent.NONE) {
            clearUserProfile()
        }
    }
    
    override suspend fun clearUserProfile() {
        storage.savePreference(USER_PROFILE_KEY, "")
        storage.savePreference(PRIVACY_CONSENT_KEY, PrivacyConsent.NONE.name)
    }
    
    private fun getPrivacyConsent(): PrivacyConsent {
        val consentString = storage.getPreference(PRIVACY_CONSENT_KEY)
        return try {
            PrivacyConsent.valueOf(consentString ?: PrivacyConsent.LOCAL_ONLY.name)
        } catch (e: Exception) {
            PrivacyConsent.LOCAL_ONLY // Default: local storage only
        }
    }
    
    private fun getCurrentTimestamp(): String {
        return utils.getCurrentTimestamp()
    }
}
