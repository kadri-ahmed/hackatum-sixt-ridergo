package network.api

import dto.AvailableVehiclesDto
import dto.BookingDto
import dto.CreateBookingDto
import dto.ProtectionPackageDto
import utils.NetworkError
import utils.Result

interface SixtApi {
    suspend fun createBooking(): Result<CreateBookingDto, NetworkError>
    suspend fun getBooking(bookingId: String): Result<BookingDto, NetworkError>
    suspend fun getAvailableVehicles(bookingId: String): Result<AvailableVehiclesDto, NetworkError>

    suspend fun getAvailableProtectionPackages(bookingId: String): Result<ProtectionPackageDto, NetworkError>


}