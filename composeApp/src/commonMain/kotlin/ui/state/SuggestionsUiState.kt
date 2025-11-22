package ui.state

import ai.models.VehicleRecommendation

sealed interface SuggestionsUiState {
    data object Initial : SuggestionsUiState
    data object Quiz : SuggestionsUiState
    data object Loading : SuggestionsUiState
    data class Success(
        val recommendations: List<VehicleRecommendation>,
        val currentIndex: Int = 0
    ) : SuggestionsUiState
    data class Error(val message: String) : SuggestionsUiState
}
