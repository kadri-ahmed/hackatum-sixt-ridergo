package repositories

import dto.BookingDto
import dto.CreateBookingDto
import utils.NetworkError
import utils.Result

/**
 * Repository interface for booking-related operations.
 * Abstracts data sources (API, database) from business logic.
 */
interface BookingRepository {
    /**
     * Creates a new booking.
     * @return Result containing the created booking ID or an error
     */
    suspend fun createBooking(): Result<CreateBookingDto, NetworkError>
    
    /**
     * Retrieves a booking by its ID.
     * @param bookingId The ID of the booking to retrieve
     * @return Result containing the booking or an error
     */
    suspend fun getBooking(bookingId: String): Result<BookingDto, NetworkError>
    
    /**
     * Assigns a vehicle to a booking.
     * @param bookingId The ID of the booking
     * @param vehicleId The ID of the vehicle to assign
     * @return Result containing the updated booking or an error
     */
    suspend fun assignVehicleToBooking(bookingId: String, vehicleId: String): Result<BookingDto, NetworkError>
    
    /**
     * Assigns a protection package to a booking.
     * @param bookingId The ID of the booking
     * @param packageId The ID of the protection package to assign
     * @return Result containing the updated booking or an error
     */
    suspend fun assignProtectionPackageToBooking(bookingId: String, packageId: String): Result<BookingDto, NetworkError>
    
    /**
     * Completes a booking.
     * @param bookingId The ID of the booking to complete
     * @return Result containing the completed booking or an error
     */
    suspend fun completeBooking(bookingId: String): Result<BookingDto, NetworkError>

    /**
     * Retrieves available protection packages for a booking.
     * @param bookingId The ID of the booking
     * @return Result containing the protection packages or an error
     */
    suspend fun getProtectionPackages(bookingId: String): Result<dto.ProtectionPackagesDto, NetworkError>
}
