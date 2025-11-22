package viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import repositories.BookingRepository
import ui.state.SearchUiState
import utils.NetworkError
import utils.Result

class SearchViewModel(
    private val bookingRepository: BookingRepository,
    private val bookingFlowViewModel: BookingFlowViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun createBooking(destination: String) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            
            // In a real app, we would use the destination to set context
            // For now, we just create a booking session
            
            when (val result = bookingRepository.createBooking()) {
                is Result.Success -> {
                    _uiState.value = SearchUiState.Success(result.data.id)
                }
                is Result.Error -> {
                    val errorMessage = when(result.error) {
                        NetworkError.NO_INTERNET -> "No internet connection"
                        NetworkError.REQUEST_TIMEOUT -> "Request timed out"
                        NetworkError.SERVER_ERROR -> "Server error"
                        else -> "Something went wrong"
                    }
                    _uiState.value = SearchUiState.Error(errorMessage)
                }
            }
        }
    }
    
    fun ensureBookingCreated() {
        // Only create booking if it doesn't exist
        if (bookingFlowViewModel.bookingId.value == null) {
            viewModelScope.launch {
                when (val result = bookingRepository.createBooking()) {
                    is Result.Success -> {
                        bookingFlowViewModel.setBookingId(result.data.id)
                        println("🎫 Booking created: ${result.data.id}")
                    }
                    is Result.Error -> {
                        println("❌ Failed to create booking: ${result.error}")
                    }
                }
            }
        }
    }
    
    fun resetState() {
        _uiState.value = SearchUiState.Idle
    }
}
