package network.api

import dto.AvailableVehiclesDto
import dto.BookingDto
import dto.CreateBookingDto
import dto.ProtectionPackagesDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.network.*
import kotlinx.serialization.SerializationException
import utils.NetworkError
import utils.Result

class SixtApiImpl(private val client: HttpClient) : SixtApi {

    val SIXT_API_URL: String = "https://hackatum25.sixt.io"

    override suspend fun getAvailableVehicles(bookingId: String): Result<AvailableVehiclesDto, NetworkError> {
        val response = try {
            client.get(
                "$SIXT_API_URL/api/booking/$bookingId/vehicles"
            ) {
                contentType(ContentType.Application.Json)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }
        return when(response.status.value) {
            in 200..299 -> {
                val availableVehicles = response.body<AvailableVehiclesDto>()
                Result.Success(availableVehicles)
            }
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun createBooking(): Result<CreateBookingDto, NetworkError> {
        val response = try {
            client.post("$SIXT_API_URL/api/booking") {
                contentType(ContentType.Application.Json)
            }
        }
        catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }
        return when(response.status.value) {
            in 200..299 -> {
                val createdBooking = response.body<CreateBookingDto>()
                Result.Success(createdBooking)
            }
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun getBooking(bookingId: String): Result<BookingDto, NetworkError> {
        val response = try {
            client.get("$SIXT_API_URL/api/booking/$bookingId") {
                contentType(ContentType.Application.Json)
            }
        }
        catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }
        return when(response.status.value) {
            in 200..299 -> {
                val booking = response.body<BookingDto>()
                Result.Success(booking)
            }
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun getAvailableProtectionPackages(bookingId: String): Result<ProtectionPackagesDto, NetworkError> {
        val response = try {
            client.get(
                "$SIXT_API_URL/api/booking/$bookingId/vehicles"
            ) {
                contentType(ContentType.Application.Json)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }
        return when(response.status.value) {
            in 200..299 -> {
                val availableProtectionPackages = response.body<ProtectionPackagesDto>()
                Result.Success(availableProtectionPackages)
            }
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun assignVehicleToBooking(
        bookingId: String,
        vehicleId: String
    ): Result<BookingDto, NetworkError> {
        val response = try {
            client.post(
                "$SIXT_API_URL/api/booking/$bookingId/vehicles/$vehicleId"
            ) {
                contentType(ContentType.Application.Json)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }
        return when(response.status.value) {
            in 200..299 -> {
                val updatedBooking = response.body<BookingDto>()
                Result.Success(updatedBooking)
            }
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun assignProtectionPackageToBooking(
        bookingId: String,
        packageId: String
    ): Result<BookingDto, NetworkError> {
        val response = try {
            client.post(
                "$SIXT_API_URL/api/booking/$bookingId/protections/$packageId"
            ) {
                contentType(ContentType.Application.Json)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }
        return when(response.status.value) {
            in 200..299 -> {
                val updatedBooking = response.body<BookingDto>()
                Result.Success(updatedBooking)
            }
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun completeBooking(bookingId: String): Result<BookingDto, NetworkError> {
        val response = try {
            client.post(
                "$SIXT_API_URL/api/booking/$bookingId/complete"
            ) {
                contentType(ContentType.Application.Json)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }
        return when(response.status.value) {
            in 200..299 -> {
                val completedBooking = response.body<BookingDto>()
                Result.Success(completedBooking)
            }
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun lockCar(): Result<Unit, NetworkError> {
        val response = try {
            client.post(
                "$SIXT_API_URL/api/car/lock"
            ) {
                contentType(ContentType.Application.Json)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }
        return when(response.status.value) {
            in 200..299 -> {
                val carLockSuccess = response.body<Unit>()
                Result.Success(carLockSuccess)
            }
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun unlockCar(): Result<Unit, NetworkError> {
        val response = try {
            client.post(
                "$SIXT_API_URL/api/car/unlock"
            ) {
                contentType(ContentType.Application.Json)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }
        return when(response.status.value) {
            in 200..299 -> {
                val carLockSuccess = response.body<Unit>()
                Result.Success(carLockSuccess)
            }
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun blinkCar(): Result<Unit, NetworkError> {
        val response = try {
            client.post(
                "$SIXT_API_URL/api/car/blink"
            ) {
                contentType(ContentType.Application.Json)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }
        return when(response.status.value) {
            in 200..299 -> {
                val carLockSuccess = response.body<Unit>()
                Result.Success(carLockSuccess)
            }
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }
}