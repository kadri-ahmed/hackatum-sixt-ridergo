package navigation

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    data object Landing : Screen()
    
    @Serializable
    data object Search : Screen()
    
    @Serializable
    data object VehicleList : Screen()
    
    @Serializable
    data object Protection : Screen()
    
    @Serializable
    data object BookingSummary : Screen()
    
    @Serializable
    data object Settings : Screen()
}
