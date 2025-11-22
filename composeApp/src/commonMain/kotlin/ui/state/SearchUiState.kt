package ui.state

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val bookingId: String) : SearchUiState
    data class SearchResults(val vehicles: List<dto.Deal>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}
