package utils

import com.russhwolf.settings.Settings

interface Storage {
    fun saveFavorite(id: String)
    fun removeFavorite(id: String)
    fun isFavorite(id: String): Boolean
    fun getFavorites(): List<String>
    fun savePreference(key: String, value: String)
    fun getPreference(key: String): String?
}

class StorageImpl(private val settings: Settings) : Storage {
    private val FAVORITES_KEY = "favorites"
    val LIVE_DEMO_KEY = "live_demo_enabled"
    val GROQ_API_KEY = "groq_api_key"

    override fun saveFavorite(id: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.add(id)
        settings.putString(FAVORITES_KEY, favorites.joinToString(","))
    }

    override fun removeFavorite(id: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.remove(id)
        settings.putString(FAVORITES_KEY, favorites.joinToString(","))
    }

    override fun isFavorite(id: String): Boolean {
        return getFavorites().contains(id)
    }

    override fun getFavorites(): List<String> {
        val favoritesString = settings.getString(FAVORITES_KEY, "")
        return if (favoritesString.isBlank()) emptyList() else favoritesString.split(",")
    }

    override fun savePreference(key: String, value: String) {
        settings.putString(key, value)
    }

    override fun getPreference(key: String): String? {
        return settings.getStringOrNull(key)
    }
}
