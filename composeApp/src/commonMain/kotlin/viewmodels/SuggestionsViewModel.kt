package viewmodels

import ai.RecommendationEngine
import ai.models.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dto.Vehicle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import utils.Result
import repositories.VehiclesRepository
import ui.state.SuggestionsUiState

class SuggestionsViewModel(
    private val vehiclesRepository: VehiclesRepository,
    private val bookingFlowViewModel: BookingFlowViewModel
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<SuggestionsUiState>(SuggestionsUiState.Initial)
    val uiState: StateFlow<SuggestionsUiState> = _uiState.asStateFlow()
    
    private val _quizStep = MutableStateFlow(0)
    val quizStep: StateFlow<Int> = _quizStep.asStateFlow()
    
    private val _tripContext = MutableStateFlow(TripContext())
    val tripContext: StateFlow<TripContext> = _tripContext.asStateFlow()
    
    private val recommendationEngine = RecommendationEngine()
    private val likedVehicles = mutableListOf<Vehicle>()
    
    fun startQuiz() {
        _uiState.value = SuggestionsUiState.Quiz
        _quizStep.value = 0
    }
    
    fun answerQuestion(answer: QuizAnswer) {
        val current = _tripContext.value
        val updated = when (_quizStep.value) {
            0 -> current.copy(terrain = answer as TerrainType)
            1 -> current.copy(weather = answer as WeatherCondition)
            2 -> current.copy(passengers = answer as Int)
            3 -> current.copy(luggage = answer as Int)
            4 -> current.copy(tripPurpose = answer as TripPurpose)
            5 -> current.copy(duration = answer as Int)
            else -> current
        }
        _tripContext.value = updated
        
        if (_quizStep.value < 5) {
            _quizStep.value++
        } else {
            // Quiz complete, generate recommendations
            generateRecommendations()
        }
    }
    
    fun skipQuiz() {
        // Use default context
        generateRecommendations()
    }
    
    private fun generateRecommendations() {
        viewModelScope.launch {
            _uiState.value = SuggestionsUiState.Loading
            
            val bookingId = bookingFlowViewModel.bookingId.value
            if (bookingId == null) {
                _uiState.value = SuggestionsUiState.Error("No active booking found")
                return@launch
            }
            
            when (val result = vehiclesRepository.getAvailableVehicles(bookingId)) {
                is Result.Success -> {
                    // Extract all vehicles from all vehicle groups
                    val vehicles = result.data.deals.flatMap { vehicleGroup ->
                        vehicleGroup.deals.map { deal -> deal.vehicle }
                    }
                    val recommendations = recommendationEngine.scoreVehicles(
                        vehicles = vehicles,
                        context = _tripContext.value
                    )
                    _uiState.value = SuggestionsUiState.Success(
                        recommendations = recommendations.take(10) // Top 10
                    )
                }
                is Result.Error -> {
                    _uiState.value = SuggestionsUiState.Error("Failed to load vehicles")
                }
            }
        }
    }
    
    fun swipeRight(recommendation: VehicleRecommendation) {
        likedVehicles.add(recommendation.vehicle)
        moveToNextCard()
    }
    
    fun swipeLeft() {
        moveToNextCard()
    }
    
    private fun moveToNextCard() {
        val currentState = _uiState.value
        if (currentState is SuggestionsUiState.Success) {
            val nextIndex = currentState.currentIndex + 1
            if (nextIndex < currentState.recommendations.size) {
                _uiState.value = currentState.copy(currentIndex = nextIndex)
            } else {
                // All cards swiped
                _uiState.value = SuggestionsUiState.Initial
            }
        }
    }
    
    fun getLikedVehicles(): List<Vehicle> = likedVehicles.toList()
}

// Type alias for quiz answers
typealias QuizAnswer = Any
