package viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import repositories.BookingRepository
import ui.state.BookingSummaryUiState
import utils.NetworkError
import utils.Result

class BookingSummaryViewModel(
    private val bookingRepository: BookingRepository,
    private val bookingFlowViewModel: BookingFlowViewModel,
    private val savedBookingRepository: repositories.SavedBookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingSummaryUiState>(BookingSummaryUiState.Loading)
    val uiState: StateFlow<BookingSummaryUiState> = _uiState.asStateFlow()

    fun loadBooking() {
        viewModelScope.launch {
            val bookingId = bookingFlowViewModel.bookingId.value ?: run {
                _uiState.value = BookingSummaryUiState.Error("No booking found")
                return@launch
            }
            _uiState.value = BookingSummaryUiState.Loading
            
            when (val result = bookingRepository.getBooking(bookingId)) {
                is Result.Success -> {
                    _uiState.value = BookingSummaryUiState.Success(result.data)
                }
                is Result.Error -> {
                    val errorMessage = mapError(result.error)
                    _uiState.value = BookingSummaryUiState.Error(errorMessage)
                }
            }
        }
    }

    fun confirmBooking(onConfirmed: () -> Unit) {
        viewModelScope.launch {
            val bookingId = bookingFlowViewModel.bookingId.value ?: run {
                _uiState.value = BookingSummaryUiState.Error("No booking found")
                return@launch
            }
            _uiState.value = BookingSummaryUiState.Loading
            
            when (val result = bookingRepository.completeBooking(bookingId)) {
                is Result.Success -> {
                    _uiState.value = BookingSummaryUiState.Success(result.data)
                    
                    // Save as confirmed booking
                    val booking = result.data
                    val vehicle = booking.selectedVehicle
                    if (vehicle != null) {
                        val vehiclePrice = vehicle.pricing.totalPrice.amount
                        val protectionPrice = booking.protectionPackages?.price?.totalPrice?.amount 
                            ?: booking.protectionPackages?.price?.displayPrice?.amount 
                            ?: 0.0
                        val totalAmount = vehiclePrice + protectionPrice
                        val currency = vehicle.pricing.totalPrice.currency
                        
                        val savedBooking = dto.SavedBooking(
                            id = kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString(),
                            bookingId = booking.id,
                            vehicle = vehicle,
                            protectionPackage = booking.protectionPackages,
                            addonIds = bookingFlowViewModel.selectedAddons.value,
                            timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                            totalPrice = totalAmount,
                            currency = currency,
                            status = dto.BookingStatus.CONFIRMED
                        )
                        savedBookingRepository.saveBooking(savedBooking)
                    }
                    
                    bookingFlowViewModel.clearBooking()
                    onConfirmed()
                }
                is Result.Error -> {
                    val errorMessage = mapError(result.error)
                    _uiState.value = BookingSummaryUiState.Error(errorMessage)
                }
            }
        }
    }
    
    fun saveBookingForLater() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is BookingSummaryUiState.Success) {
                val booking = currentState.booking
                val vehicle = booking.selectedVehicle
                
                if (vehicle != null) {
                    val vehiclePrice = vehicle.pricing.totalPrice.amount
                    val protectionPrice = booking.protectionPackages?.price?.totalPrice?.amount 
                        ?: booking.protectionPackages?.price?.displayPrice?.amount 
                        ?: 0.0
                    val totalAmount = vehiclePrice + protectionPrice
                    val currency = vehicle.pricing.totalPrice.currency
                    
                    val savedBooking = dto.SavedBooking(
                        id = kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString(),
                        bookingId = booking.id,
                        vehicle = vehicle,
                        protectionPackage = booking.protectionPackages,
                        addonIds = bookingFlowViewModel.selectedAddons.value,
                        timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                        totalPrice = totalAmount,
                        currency = currency,
                        status = dto.BookingStatus.DRAFT
                    )
                    savedBookingRepository.saveBooking(savedBooking)
                    bookingFlowViewModel.clearBooking()
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
