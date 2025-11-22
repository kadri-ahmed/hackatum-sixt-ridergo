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
}
