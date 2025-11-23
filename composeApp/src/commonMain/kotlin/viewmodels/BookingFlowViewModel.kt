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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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
    
    /**
     * Checks if a booking ID is fake (not a real API booking ID).
     * Returns true if the ID appears to be fake, false if it's likely a real ID.
     */
    fun isFakeBookingId(bookingId: String?): Boolean {
        if (bookingId == null) return true
        // Fake IDs: start with "chat_booking_" or are just numeric timestamps
        return bookingId.startsWith("chat_booking_") || bookingId.matches(Regex("^\\d+$"))
    }
    
    /**
     * Ensures we have a real booking ID. If the provided ID is fake, creates a new real booking via API.
     * This should only be used when we can migrate the booking state (e.g., when resuming).
     * For operations like completion, use isFakeBookingId to check and show an error instead.
     */
    suspend fun ensureRealBookingId(bookingId: String?): String? {
        if (bookingId == null || isFakeBookingId(bookingId)) {
            // Create a new booking if no ID provided or if it's fake
            return when (val result = bookingRepository.createBooking()) {
                is utils.Result.Success -> result.data.id
                is utils.Result.Error -> null
            }
        }
        
        // Already a real ID
        return bookingId
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

    @OptIn(ExperimentalTime::class)
    suspend fun saveDraft(
        vehicle: dto.Vehicle, 
        pricing: dto.Pricing,
        protectionPackage: dto.ProtectionPackageDto?,
        availableAddons: List<dto.AddonCategory>,
        savedBookingRepository: repositories.SavedBookingRepository
    ): utils.Result<Unit, utils.NetworkError> {
        val currentBookingId = _bookingId.value
        
        // Always ensure we have a real booking ID
        val realBookingId = if (currentBookingId != null && _isModifying.value) {
            // Use existing booking ID if modifying
            currentBookingId
        } else {
            // Create a new real booking
            when (val createResult = bookingRepository.createBooking()) {
                is utils.Result.Success -> {
                    val newBookingId = createResult.data.id
                    // Assign vehicle to the new booking
                    val vehicleResult = bookingRepository.assignVehicleToBooking(newBookingId, vehicle.id)
                    if (vehicleResult is utils.Result.Success) {
                        // Assign protection package if present
                        if (protectionPackage != null) {
                            bookingRepository.assignProtectionPackageToBooking(newBookingId, protectionPackage.id)
                        }
                        newBookingId
                    } else {
                        return utils.Result.Error(utils.NetworkError.UNKNOWN)
                    }
                }
                is utils.Result.Error -> {
                    return createResult
                }
            }
        }
        
        val vehiclePrice = pricing.totalPrice.amount
        val protectionPrice = protectionPackage?.price?.totalPrice?.amount 
            ?: protectionPackage?.price?.displayPrice?.amount 
            ?: 0.0
            
        // Calculate addon price
        val selectedAddonIds = _selectedAddons.value
        var addonsPrice = 0.0
        availableAddons.forEach { category ->
            category.options.forEach { option ->
                if (selectedAddonIds.contains(option.chargeDetail.id)) {
                    addonsPrice += option.additionalInfo.price.totalPrice?.amount
                        ?: option.additionalInfo.price.displayPrice.amount
                }
            }
        }
            
        val totalAmount = vehiclePrice + protectionPrice + addonsPrice
        
        val savedBooking = dto.SavedBooking(
            id = Clock.System.now().toEpochMilliseconds().toString(),
            bookingId = realBookingId, // Always use real booking ID
            vehicle = dto.Deal(vehicle, pricing, ""), // Reconstruct Deal
            protectionPackage = protectionPackage,
            addonIds = selectedAddonIds,
            timestamp = Clock.System.now().toEpochMilliseconds(),
            totalPrice = totalAmount,
            currency = pricing.totalPrice.currency,
            status = dto.BookingStatus.DRAFT
        )
        
        savedBookingRepository.saveBooking(savedBooking)
        clearBooking()
        return utils.Result.Success(Unit)
    }
}
