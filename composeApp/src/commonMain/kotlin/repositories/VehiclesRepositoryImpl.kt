package repositories

import dto.AddonsDto
import dto.AvailableVehiclesDto
import dto.ProtectionPackagesDto
import network.api.SixtApi
import utils.NetworkError
import utils.Result

/**
 * Implementation of VehiclesRepository that uses SixtApi for data operations.
 * This layer can be extended to add caching, offline support, or database operations.
 */
class VehiclesRepositoryImpl(
    private val api: SixtApi
) : VehiclesRepository {
    
    override suspend fun getAvailableVehicles(bookingId: String): Result<AvailableVehiclesDto, NetworkError> {
        return api.getAvailableVehicles(bookingId)
    }
    
    override suspend fun getAvailableProtectionPackages(bookingId: String): Result<ProtectionPackagesDto, NetworkError> {
        return api.getAvailableProtectionPackages(bookingId)
    }

    override suspend fun getAvailableAddons(bookingId: String): Result<AddonsDto, NetworkError> {
        return api.getAvailableAddons(bookingId)
    }

    override suspend fun searchVehicles(query: String): Result<AvailableVehiclesDto, NetworkError> {
        // Mock search by fetching all and filtering
        return when (val result = api.getAvailableVehicles("mock_booking_id")) {
            is Result.Success -> {
                val filtered = result.data.deals.filter { deal ->
                    deal.vehicle.brand.contains(query, ignoreCase = true) ||
                    deal.vehicle.model.contains(query, ignoreCase = true)
                }
                Result.Success(result.data.copy(deals = filtered))
            }
            is Result.Error -> result
        }
    }
}
