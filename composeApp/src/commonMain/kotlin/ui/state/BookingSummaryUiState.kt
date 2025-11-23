package ui.state

import dto.BookingDto

sealed interface BookingSummaryUiState {
    data object Loading : BookingSummaryUiState
    data class Loaded(val booking: BookingDto) : BookingSummaryUiState
    data class Confirmed(val booking: BookingDto) : BookingSummaryUiState
    data class Saved(val booking: BookingDto) : BookingSummaryUiState
    data class Error(val message: String) : BookingSummaryUiState
}
