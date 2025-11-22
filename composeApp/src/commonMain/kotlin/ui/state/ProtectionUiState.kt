package ui.state

import dto.ProtectionPackageDto

sealed interface ProtectionUiState {
    data object Loading : ProtectionUiState
    data class Success(val packages: List<ProtectionPackageDto>) : ProtectionUiState
    data class Error(val message: String) : ProtectionUiState
}
