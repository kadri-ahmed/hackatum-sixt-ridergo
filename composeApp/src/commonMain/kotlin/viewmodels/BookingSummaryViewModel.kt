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
    private val bookingFlowViewModel: BookingFlowViewModel
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
                    onConfirmed()
                }
                is Result.Error -> {
                    val errorMessage = mapError(result.error)
                    _uiState.value = BookingSummaryUiState.Error(errorMessage)
                }
            }
        }
    }

    private fun mapError(error: NetworkError): String {
        return when(error) {
            NetworkError.NO_INTERNET -> "Connection error: Unable to reach the server. Please check your internet connection and try again."
            NetworkError.REQUEST_TIMEOUT -> "Request timed out: The server took too long to respond. Please try again."
            NetworkError.SERVER_ERROR -> "Server error: The server encountered an error. Please try again later."
            NetworkError.SERIALIZATION -> "Failed to process response: There was an error processing the booking data."
            NetworkError.CONFLICT -> "Booking conflict: This booking may have already been completed. Please refresh and try again."
            NetworkError.UNAUTHORIZED -> "Authorization error: Your session may have expired. Please try again."
            NetworkError.BAD_REQUEST -> "Invalid request: Please check your booking details and try again."
            else -> "Failed to complete booking: An unexpected error occurred. Please try again or contact support."
        }
    }
}
