package viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import repositories.VehiclesRepository
import ui.state.ProtectionUiState
import utils.NetworkError
import utils.Result

class ProtectionViewModel(
    private val vehiclesRepository: VehiclesRepository,
    private val bookingFlowViewModel: BookingFlowViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProtectionUiState>(ProtectionUiState.Loading)
    val uiState: StateFlow<ProtectionUiState> = _uiState.asStateFlow()

    val selectedPackageId: StateFlow<String?> = bookingFlowViewModel.selectedProtectionPackageId

    fun loadProtectionPackages() {
        viewModelScope.launch {
            val bookingId = bookingFlowViewModel.bookingId.value ?: run {
                _uiState.value = ProtectionUiState.Error("No booking found")
                return@launch
            }
            _uiState.value = ProtectionUiState.Loading
            
            when (val result = vehiclesRepository.getAvailableProtectionPackages(bookingId)) {
                is Result.Success -> {
                    _uiState.value = ProtectionUiState.Success(result.data.protectionPackages)
                }
                is Result.Error -> {
                    val errorMessage = when(result.error) {
                        NetworkError.NO_INTERNET -> "No internet connection"
                        NetworkError.REQUEST_TIMEOUT -> "Request timed out"
                        NetworkError.SERVER_ERROR -> "Server error"
                        else -> "Failed to load protection packages"
                    }
                    _uiState.value = ProtectionUiState.Error(errorMessage)
                }
            }
        }
    }
    fun selectPackage(packageId: String) {
        bookingFlowViewModel.setSelectedProtectionPackageId(packageId)
    }
}
