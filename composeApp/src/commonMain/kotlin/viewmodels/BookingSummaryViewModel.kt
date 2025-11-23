package viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import dto.BookingStatus
import dto.SavedBooking
import repositories.BookingRepository
import repositories.SavedBookingRepository
import ui.state.BookingSummaryUiState
import utils.NetworkError
import utils.Result
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class BookingSummaryViewModel(
    private val bookingRepository: BookingRepository,
    private val bookingFlowViewModel: BookingFlowViewModel,
    private val savedBookingRepository: SavedBookingRepository,
    private val vehiclesRepository: repositories.VehiclesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingSummaryUiState>(BookingSummaryUiState.Loading)
    val uiState: StateFlow<BookingSummaryUiState> = _uiState.asStateFlow()

    fun loadBooking() {
        viewModelScope.launch {
            val bookingId = bookingFlowViewModel.bookingId.value ?: run {
                _uiState.value = BookingSummaryUiState.Error("No booking found")
                return@launch
            }
            
            // Check if booking ID is fake - if so, show error (can't load a fake booking)
            if (bookingFlowViewModel.isFakeBookingId(bookingId)) {
                _uiState.value = BookingSummaryUiState.Error("Invalid booking. Please resume from your saved bookings.")
                return@launch
            }
            
            _uiState.value = BookingSummaryUiState.Loading
            
            when (val result = bookingRepository.getBooking(bookingId)) {
                is Result.Success -> {
                    var booking = result.data
                    
                    // Fetch and populate selected addons
                    val selectedAddonIds = bookingFlowViewModel.selectedAddons.value
                    if (selectedAddonIds.isNotEmpty()) {
                        val addonsResult = vehiclesRepository.getAvailableAddons(bookingId)
                        if (addonsResult is Result.Success) {
                            val selectedAddonsList = addonsResult.data.addons
                                .flatMap { it.options }
                                .filter { selectedAddonIds.contains(it.chargeDetail.id) }
                            booking = booking.copy(addons = selectedAddonsList)
                        }
                    }
                    
                    _uiState.value = BookingSummaryUiState.Loaded(booking)
                }
                is Result.Error -> {
                    val errorMessage = mapError(result.error)
                    _uiState.value = BookingSummaryUiState.Error(errorMessage)
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun confirmBooking(onConfirmed: () -> Unit) {
        viewModelScope.launch {
            val bookingId = bookingFlowViewModel.bookingId.value ?: run {
                _uiState.value = BookingSummaryUiState.Error("No booking found")
                return@launch
            }
            
            // Check if booking ID is fake - if so, show error (can't complete a fake booking)
            if (bookingFlowViewModel.isFakeBookingId(bookingId)) {
                _uiState.value = BookingSummaryUiState.Error("Invalid booking. Please resume from your saved bookings to complete.")
                return@launch
            }
            
            _uiState.value = BookingSummaryUiState.Loading
            
            when (val result = bookingRepository.completeBooking(bookingId)) {
                is Result.Success -> {
                    _uiState.value = BookingSummaryUiState.Confirmed(result.data)
                    saveConfirmedBooking(result.data)
                    bookingFlowViewModel.clearBooking()
                }
                is Result.Error -> {
                    val errorMessage = mapError(result.error)
                    _uiState.value = BookingSummaryUiState.Error(errorMessage)
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun saveConfirmedBooking(booking: dto.BookingDto) {
        val vehicle = booking.selectedVehicle
        if (vehicle != null) {
            val vehiclePrice = vehicle.pricing.totalPrice.amount
            val protectionPrice = booking.protectionPackages?.price?.totalPrice?.amount 
                ?: booking.protectionPackages?.price?.displayPrice?.amount 
                ?: 0.0
                
            // Calculate addon price
            var addonsPrice = 0.0
            val selectedAddonIds = bookingFlowViewModel.selectedAddons.value
            if (selectedAddonIds.isNotEmpty()) {
                val result = vehiclesRepository.getAvailableAddons(booking.id)
                if (result is Result.Success) {
                    result.data.addons.forEach { category ->
                        category.options.forEach { option ->
                            if (selectedAddonIds.contains(option.chargeDetail.id)) {
                                addonsPrice += option.additionalInfo.price.totalPrice?.amount
                                    ?: option.additionalInfo.price.displayPrice.amount
                            }
                        }
                    }
                }
            }
            
            val totalAmount = vehiclePrice + protectionPrice + addonsPrice
            val currency = vehicle.pricing.totalPrice.currency
            
            // Check if we already have a saved booking for this ID
            val existingBooking = savedBookingRepository.getSavedBookings().first().find { it.bookingId == booking.id }
            val savedBookingId = existingBooking?.id ?: Clock.System.now().toEpochMilliseconds().toString()

            val savedBooking = SavedBooking(
                id = savedBookingId,
                bookingId = booking.id,
                vehicle = vehicle,
                protectionPackage = booking.protectionPackages,
                addonIds = selectedAddonIds,
                timestamp = Clock.System.now().toEpochMilliseconds(),
                totalPrice = totalAmount,
                currency = currency,
                status = BookingStatus.CONFIRMED
            )
            savedBookingRepository.saveBooking(savedBooking)
        }
    }
    
    @OptIn(ExperimentalTime::class)
    fun saveBookingForLater() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is BookingSummaryUiState.Loaded) {
                val booking = currentState.booking
                val vehicle = booking.selectedVehicle
                
                if (vehicle != null) {
                    val vehiclePrice = vehicle.pricing.totalPrice.amount
                    val protectionPrice = booking.protectionPackages?.price?.totalPrice?.amount 
                        ?: booking.protectionPackages?.price?.displayPrice?.amount 
                        ?: 0.0
                        
                    // Calculate addon price
                    var addonsPrice = 0.0
                    val selectedAddonIds = bookingFlowViewModel.selectedAddons.value
                    if (selectedAddonIds.isNotEmpty()) {
                        val result = vehiclesRepository.getAvailableAddons(booking.id)
                        if (result is Result.Success) {
                            result.data.addons.forEach { category ->
                                category.options.forEach { option ->
                                    if (selectedAddonIds.contains(option.chargeDetail.id)) {
                                        addonsPrice += option.additionalInfo.price.totalPrice?.amount
                                            ?: option.additionalInfo.price.displayPrice.amount
                                    }
                                }
                            }
                        }
                    }
                    
                    val totalAmount = vehiclePrice + protectionPrice + addonsPrice
                    val currency = vehicle.pricing.totalPrice.currency
                    
                    val savedBooking = SavedBooking(
                        id = Clock.System.now().toEpochMilliseconds().toString(),
                        bookingId = booking.id,
                        vehicle = vehicle,
                        protectionPackage = booking.protectionPackages,
                        addonIds = selectedAddonIds,
                        timestamp = Clock.System.now().toEpochMilliseconds(),
                        totalPrice = totalAmount,
                        currency = currency,
                        status = BookingStatus.DRAFT
                    )
                    savedBookingRepository.saveBooking(savedBooking)
                    bookingFlowViewModel.clearBooking()
                    _uiState.value = BookingSummaryUiState.Saved(booking)
                }
            }
        }
    }

    private fun mapError(error: NetworkError): String {
        return when(error) {
            NetworkError.NO_INTERNET -> "No internet connection"
            NetworkError.REQUEST_TIMEOUT -> "Request timed out"
            NetworkError.SERVER_ERROR -> "Server error"
            NetworkError.SERIALIZATION -> "Failed to process response"
            NetworkError.CONFLICT -> "Booking conflict - please try again"
            NetworkError.UNAUTHORIZED -> "Authorization error"
            else -> "Failed to complete booking. Please contact support."
        }
    }
}
