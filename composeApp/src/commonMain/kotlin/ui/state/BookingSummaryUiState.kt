package ui.state

import dto.BookingDto

sealed interface BookingSummaryUiState {
    data object Loading : BookingSummaryUiState
    data class Success(val booking: BookingDto) : BookingSummaryUiState
    data class Error(val message: String) : BookingSummaryUiState
}
