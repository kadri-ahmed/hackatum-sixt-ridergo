package repositories

import dto.AvailableVehiclesDto
import dto.ProtectionPackagesDto
import utils.NetworkError
import utils.Result

/**
 * Repository interface for vehicle-related operations.
 * Abstracts data sources (API, database) from business logic.
 */
interface VehiclesRepository {
    /**
     * Retrieves available vehicles for a booking.
     * @param bookingId The ID of the booking
     * @return Result containing available vehicles or an error
     */
    suspend fun getAvailableVehicles(bookingId: String): Result<AvailableVehiclesDto, NetworkError>
    
    /**
     * Retrieves available protection packages for a booking.
     * @param bookingId The ID of the booking
     * @return Result containing protection packages or an error
     */
    suspend fun getAvailableProtectionPackages(bookingId: String): Result<ProtectionPackagesDto, NetworkError>
}
