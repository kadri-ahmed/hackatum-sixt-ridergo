package navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Search : Screen

    @Serializable
    data object VehicleList : Screen

    @Serializable
    data object Protection : Screen

    @Serializable
    data object BookingSummary : Screen
}
