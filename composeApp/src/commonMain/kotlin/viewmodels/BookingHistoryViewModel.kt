package viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import models.BookingHistoryEntry
import repositories.BookingHistoryRepository
import repositories.UserProfileRepository
// Date formatting uses simple string parsing for multiplatform compatibility

class BookingHistoryViewModel(
    private val bookingHistoryRepository: BookingHistoryRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {
    
    private val _history = MutableStateFlow<List<BookingHistoryEntry>>(emptyList())
    val history: StateFlow<List<BookingHistoryEntry>> = _history.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _privacyConsent = MutableStateFlow<models.PrivacyConsent>(models.PrivacyConsent.LOCAL_ONLY)
    val privacyConsent: StateFlow<models.PrivacyConsent> = _privacyConsent.asStateFlow()
    
    fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profile = userProfileRepository.getUserProfile()
                _privacyConsent.value = profile.privacyConsent
                
                // Only load history if user has consented
                if (profile.privacyConsent != models.PrivacyConsent.NONE) {
                    _history.value = bookingHistoryRepository.getBookingHistory()
                } else {
                    _history.value = emptyList()
                }
            } catch (e: Exception) {
                _history.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            bookingHistoryRepository.clearBookingHistory()
            _history.value = emptyList()
        }
    }
    
    fun formatDate(timestamp: String): String {
        return try {
            // Parse ISO 8601 timestamp: "2025-12-01T09:00:00+01:00" or "2025-12-01T09:00:00Z"
            val datePart = timestamp.substringBefore("T")
            val parts = datePart.split("-")
            
            if (parts.size == 3) {
                val year = parts[0]
                val monthNum = parts[1].toIntOrNull() ?: return datePart
                val day = parts[2]
                
                val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                val month = monthNames.getOrNull(monthNum - 1) ?: monthNum.toString()
                
                "$month $day, $year"
            } else {
                datePart
            }
        } catch (e: Exception) {
            // Fallback: just return the date part
            timestamp.substringBefore("T")
        }
    }
}
