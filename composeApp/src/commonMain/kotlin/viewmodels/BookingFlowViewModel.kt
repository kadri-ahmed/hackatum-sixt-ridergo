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

    private val _selectedAddons = MutableStateFlow<Set<String>>(emptySet())
    val selectedAddons: StateFlow<Set<String>> = _selectedAddons.asStateFlow()

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
    
    fun toggleAddon(addonId: String) {
        val current = _selectedAddons.value.toMutableSet()
        if (current.contains(addonId)) {
            current.remove(addonId)
        } else {
            current.add(addonId)
        }
        _selectedAddons.value = current
        // Note: API integration for addons would go here if available
    }
    
    fun selectVehicle(vehicleId: String) {
        val currentBookingId = _bookingId.value
        if (currentBookingId != null) {
            viewModelScope.launch {
                bookingRepository.assignVehicleToBooking(currentBookingId, vehicleId)
            }
        }
    }
    
    private val _isModifying = MutableStateFlow(false)
    val isModifying: StateFlow<Boolean> = _isModifying.asStateFlow()

    fun setModifying(modifying: Boolean) {
        _isModifying.value = modifying
    }

    fun clearBooking() {
        _bookingId.value = null
        _selectedProtectionPackageId.value = null
        _selectedAddons.value = emptySet()
        _isModifying.value = false
    }

    suspend fun saveDraft(
        vehicle: dto.Vehicle, 
        pricing: dto.Pricing,
        protectionPackage: dto.ProtectionPackageDto?,
        savedBookingRepository: repositories.SavedBookingRepository
    ) {
        val currentBookingId = _bookingId.value
        val idToSave = if (currentBookingId != null && _isModifying.value) currentBookingId else kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString()
        
        val vehiclePrice = pricing.totalPrice.amount
        val protectionPrice = protectionPackage?.price?.totalPrice?.amount 
            ?: protectionPackage?.price?.displayPrice?.amount 
            ?: 0.0
        val totalAmount = vehiclePrice + protectionPrice
        
        val savedBooking = dto.SavedBooking(
            id = idToSave,
            bookingId = idToSave, // For drafts, bookingId can be same as ID or generated
            vehicle = dto.Deal(vehicle, pricing, ""), // Reconstruct Deal
            protectionPackage = protectionPackage,
            addonIds = _selectedAddons.value,
            timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            totalPrice = totalAmount,
            currency = pricing.totalPrice.currency,
            status = dto.BookingStatus.DRAFT
        )
        
        savedBookingRepository.saveBooking(savedBooking)
        clearBooking()
    }
}
