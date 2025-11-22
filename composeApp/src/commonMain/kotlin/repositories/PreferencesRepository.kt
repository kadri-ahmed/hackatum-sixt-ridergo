package repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import utils.ThemeMode

/**
 * Repository for managing user preferences.
 * Currently in-memory, should be replaced with DataStore for persistence.
 */
class PreferencesRepository {
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: Flow<ThemeMode> = _themeMode.asStateFlow()
    
    suspend fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        // TODO: Persist to DataStore
    }
    
    fun getThemeMode(): ThemeMode {
        return _themeMode.value
    }
}
