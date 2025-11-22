package network.api

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException
import dto.AvailableVehiclesDto
import dto.BookingDto
import dto.CreateBookingDto
import dto.ProtectionPackageDto
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

    override suspend fun getAvailableProtectionPackages(bookingId: String): Result<ProtectionPackageDto, NetworkError> {
        TODO("Not yet implemented")
    }
}