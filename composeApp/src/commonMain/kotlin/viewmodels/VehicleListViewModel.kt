package viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import models.DestinationContext
import recommendations.VehicleRecommendationEngine
import repositories.VehiclesRepository
import ui.state.VehicleListUiState
import utils.NetworkError
import utils.Result

class VehicleListViewModel(
    private val vehiclesRepository: VehiclesRepository,
    private val bookingFlowViewModel: BookingFlowViewModel,
    private val destinationContext: DestinationContext? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<VehicleListUiState>(VehicleListUiState.Loading)
    val uiState: StateFlow<VehicleListUiState> = _uiState.asStateFlow()

    private val recommendationEngine = VehicleRecommendationEngine(destinationContext)

    // In a real app, we would get the bookingId from SavedStateHandle or Navigation args
    // For now, we'll accept it as a parameter or use a hardcoded one for testing if needed
    fun loadVehicles() {
        viewModelScope.launch {
            val bookingId = bookingFlowViewModel.bookingId.value ?: run {
                _uiState.value = VehicleListUiState.Error("No booking found")
                return@launch
            }
            _uiState.value = VehicleListUiState.Loading
            
            when (val result = vehiclesRepository.getAvailableVehicles(bookingId)) {
                is Result.Success -> {
                    if (result.data.deals.isEmpty()) {
                        _uiState.value = VehicleListUiState.Empty
                    } else {
                        // Score and rank deals using recommendation engine
                        val scoredDeals = recommendationEngine.scoreAndRankDeals(result.data.deals)
                        val recommendationMessage = recommendationEngine.generateRecommendationMessage(
                            scoredDeals.take(3)
                        )
                        
                        // Return deals sorted by score (best first)
                        val sortedDeals = scoredDeals.map { it.deal }
                        
                        _uiState.value = VehicleListUiState.Success(
                            deals = sortedDeals,
                            scoredDeals = scoredDeals,
                            recommendationMessage = recommendationMessage
                        )
                    }
                }
                is Result.Error -> {
                    val errorMessage = when(result.error) {
                        NetworkError.NO_INTERNET -> "No internet connection"
                        NetworkError.REQUEST_TIMEOUT -> "Request timed out"
                        NetworkError.SERVER_ERROR -> "Server error"
                        else -> "Failed to load vehicles"
                    }
                    _uiState.value = VehicleListUiState.Error(errorMessage)
                }
            }
        }
    }
}
