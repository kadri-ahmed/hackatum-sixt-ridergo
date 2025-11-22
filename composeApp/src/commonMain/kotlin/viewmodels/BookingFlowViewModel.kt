package viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared ViewModel to hold the current booking flow state.
 * This allows passing booking ID between screens without navigation arguments.
 */
class BookingFlowViewModel : ViewModel() {
    private val _bookingId = MutableStateFlow<String?>(null)
    val bookingId: StateFlow<String?> = _bookingId.asStateFlow()
    
    fun setBookingId(id: String) {
        _bookingId.value = id
    }
    
    fun clearBooking() {
        _bookingId.value = null
    }
}
