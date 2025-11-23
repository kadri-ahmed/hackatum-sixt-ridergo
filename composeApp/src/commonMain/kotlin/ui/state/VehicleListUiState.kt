package ui.state

import dto.Deal
import recommendations.ScoredDeal

sealed interface VehicleListUiState {
    data object Loading : VehicleListUiState
    data class Success(
        val deals: List<Deal>,
        val scoredDeals: List<ScoredDeal>,
        val recommendationMessage: String
    ) : VehicleListUiState
    data class Error(val message: String) : VehicleListUiState
    data object Empty : VehicleListUiState
}
