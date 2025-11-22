package ui.state

import dto.Deal

sealed interface VehicleListUiState {
    data object Loading : VehicleListUiState
    data class Success(val deals: List<Deal>) : VehicleListUiState
    data class Error(val message: String) : VehicleListUiState
    data object Empty : VehicleListUiState
}
