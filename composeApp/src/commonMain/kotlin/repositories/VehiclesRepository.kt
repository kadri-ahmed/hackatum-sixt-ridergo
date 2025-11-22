package repositories

import network.api.SixtApi
import dto.Deal

class VehiclesRepository(private val api: SixtApi) {
    suspend fun getAvailableVehicles(bookingId: String): List<Deal> {
        return listOf()
    }
}