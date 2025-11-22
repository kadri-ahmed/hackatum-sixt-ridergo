package network.api

import dto.AvailableVehiclesDto
import dto.BookingDto
import dto.CreateBookingDto
import dto.ProtectionPackagesDto
import utils.NetworkError
import utils.Result

interface SixtApi {

    // Booking Management
    suspend fun createBooking(): Result<CreateBookingDto, NetworkError>
    suspend fun getBooking(bookingId: String): Result<BookingDto, NetworkError>
    suspend fun getAvailableVehicles(bookingId: String): Result<AvailableVehiclesDto, NetworkError>
    suspend fun getAvailableProtectionPackages(bookingId: String): Result<ProtectionPackagesDto, NetworkError>
    suspend fun assignVehicleToBooking(bookingId: String, vehicleId: String): Result<BookingDto, NetworkError>
    suspend fun assignProtectionPackageToBooking(bookingId: String, packageId: String): Result<BookingDto, NetworkError>
    suspend fun completeBooking(bookingId: String): Result<BookingDto, NetworkError>

    // Car Lock/Unlock API
    suspend fun lockCar(): Result<Unit, NetworkError>
    suspend fun unlockCar(): Result<Unit, NetworkError>
    suspend fun blinkCar(): Result<Unit, NetworkError>
}