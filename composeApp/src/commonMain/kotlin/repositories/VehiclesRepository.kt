package repositories

import network.api.SixtApi
import models.Deal

class VehiclesRepository(private val api: SixtApi) {
    suspend fun getAvailableVehicles(bookingId: String): List<Deal> {
        return listOf()
    }
}