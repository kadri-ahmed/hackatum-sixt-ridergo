package repositories

import dto.AddonsDto
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
    
    /**
     * Retrieves available addons for a booking.
     * @param bookingId The ID of the booking
     * @return Result containing addons or an error
     */
    suspend fun getAvailableAddons(bookingId: String): Result<AddonsDto, NetworkError>

    /**
     * Searches for vehicles matching the query.
     * @param query The search query (e.g. brand, model)
     * @param bookingId The ID of the booking to search vehicles for
     * @return Result containing matching vehicles
     */
    suspend fun searchVehicles(query: String, bookingId: String): Result<AvailableVehiclesDto, NetworkError>
}
