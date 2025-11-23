package viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared ViewModel to hold the current booking flow state.
 * This allows passing booking ID between screens without navigation arguments.
 */
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import repositories.BookingRepository

class BookingFlowViewModel(
    private val bookingRepository: BookingRepository
) : ViewModel() {
    private val _bookingId = MutableStateFlow<String?>(null)
    val bookingId: StateFlow<String?> = _bookingId.asStateFlow()
    
    private val _selectedProtectionPackageId = MutableStateFlow<String?>(null)
    val selectedProtectionPackageId: StateFlow<String?> = _selectedProtectionPackageId.asStateFlow()

    private val _homeVisitCount = MutableStateFlow(0)
    val homeVisitCount: StateFlow<Int> = _homeVisitCount.asStateFlow()

    fun incrementHomeVisitCount() {
        _homeVisitCount.value += 1
    }

    fun setBookingId(id: String) {
        _bookingId.value = id
    }
    
    fun setSelectedProtectionPackageId(id: String) {
        _selectedProtectionPackageId.value = id
        // Assign to booking
        val currentBookingId = _bookingId.value
        if (currentBookingId != null) {
            viewModelScope.launch {
                bookingRepository.assignProtectionPackageToBooking(currentBookingId, id)
            }
        }
    }
    
    fun selectVehicle(vehicleId: String) {
        val currentBookingId = _bookingId.value
        if (currentBookingId != null) {
            viewModelScope.launch {
                bookingRepository.assignVehicleToBooking(currentBookingId, vehicleId)
            }
        }
    }
    
    fun clearBooking() {
        _bookingId.value = null
        _selectedProtectionPackageId.value = null
    }
}
