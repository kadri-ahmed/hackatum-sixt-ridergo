package repositories

import dto.BookingDto
import dto.CreateBookingDto
import network.api.SixtApi
import utils.NetworkError
import utils.Result

/**
 * Implementation of BookingRepository that uses SixtApi for data operations.
 * This layer can be extended to add caching, offline support, or database operations.
 */
class BookingRepositoryImpl(
    private val api: SixtApi
) : BookingRepository {
    
    override suspend fun createBooking(): Result<CreateBookingDto, NetworkError> {
        return api.createBooking()
    }
    
    override suspend fun getBooking(bookingId: String): Result<BookingDto, NetworkError> {
        return api.getBooking(bookingId)
    }
    
    override suspend fun assignVehicleToBooking(
        bookingId: String,
        vehicleId: String
    ): Result<BookingDto, NetworkError> {
        return api.assignVehicleToBooking(bookingId, vehicleId)
    }
    
    override suspend fun assignProtectionPackageToBooking(
        bookingId: String,
        packageId: String
    ): Result<BookingDto, NetworkError> {
        return api.assignProtectionPackageToBooking(bookingId, packageId)
    }
    
    override suspend fun completeBooking(bookingId: String): Result<BookingDto, NetworkError> {
        return api.completeBooking(bookingId)
    }
}
