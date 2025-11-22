package network.api

import models.AvailableVehiclesResponse
import utils.NetworkError
import utils.Result

interface SixtApi {
    suspend fun getAvailableVehicles(bookingId: String): Result<AvailableVehiclesResponse, NetworkError>
}