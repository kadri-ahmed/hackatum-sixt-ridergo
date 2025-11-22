package network.api

import dto.AvailableVehiclesDto
import dto.BookingDto
import dto.CreateBookingDto
import dto.ProtectionPackagesDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.statement.bodyAsText
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import utils.NetworkError
import utils.Result

class SixtApiImpl(private val client: HttpClient) : SixtApi {

    val SIXT_API_URL: String = "https://hackatum25.sixt.io"

    override suspend fun getAvailableVehicles(bookingId: String): Result<AvailableVehiclesDto, NetworkError> {
        return try {
            val response = client.get(
                "$SIXT_API_URL/api/booking/$bookingId/vehicles"
            ) {
                contentType(ContentType.Application.Json)
            }
            
            when(response.status.value) {
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
        } catch (e: UnresolvedAddressException) {
            Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            Result.Error(NetworkError.SERIALIZATION)
        } catch (e: Exception) {
            Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun createBooking(): Result<CreateBookingDto, NetworkError> {
        return try {
            val response = client.post("$SIXT_API_URL/api/booking") {
                contentType(ContentType.Application.Json)
            }
            
            when(response.status.value) {
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
        } catch (e: UnresolvedAddressException) {
            Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            Result.Error(NetworkError.SERIALIZATION)
        } catch (e: Exception) {
            Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun getBooking(bookingId: String): Result<BookingDto, NetworkError> {
        return try {
            val response = client.get("$SIXT_API_URL/api/booking/$bookingId") {
                contentType(ContentType.Application.Json)
            }
            
            when(response.status.value) {
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
        } catch (e: UnresolvedAddressException) {
            Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            Result.Error(NetworkError.SERIALIZATION)
        } catch (e: Exception) {
            Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun getAvailableProtectionPackages(bookingId: String): Result<ProtectionPackagesDto, NetworkError> {
        return try {
            val response = client.get(
                "$SIXT_API_URL/api/booking/$bookingId/protections"
            ) {
                contentType(ContentType.Application.Json)
            }
            
            when(response.status.value) {
                in 200..299 -> {
                    val bodyText = response.bodyAsText()
                    println("DEBUG: Protection Response: $bodyText")
                    val availableProtectionPackages = Json { ignoreUnknownKeys = true }.decodeFromString<ProtectionPackagesDto>(bodyText)
                    Result.Success(availableProtectionPackages)
                }
                401 -> Result.Error(NetworkError.UNAUTHORIZED)
                409 -> Result.Error(NetworkError.CONFLICT)
                408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
                413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
                in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
                else -> Result.Error(NetworkError.UNKNOWN)
            }
        } catch (e: UnresolvedAddressException) {
            Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            Result.Error(NetworkError.SERIALIZATION)
        } catch (e: Exception) {
            Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun assignVehicleToBooking(
        bookingId: String,
        vehicleId: String
    ): Result<BookingDto, NetworkError> {
        return try {
            val response = client.post(
                "$SIXT_API_URL/api/booking/$bookingId/vehicles/$vehicleId"
            ) {
                contentType(ContentType.Application.Json)
            }
            
            println("DEBUG: Assign Vehicle Status: ${response.status.value}")

            when(response.status.value) {
                in 200..299 -> {
                    val bodyText = response.bodyAsText()
                    println("DEBUG: Assign Vehicle Response: $bodyText")
                    val updatedBooking = Json { ignoreUnknownKeys = true }.decodeFromString<BookingDto>(bodyText)
                    Result.Success(updatedBooking)
                }
                401 -> Result.Error(NetworkError.UNAUTHORIZED)
                409 -> Result.Error(NetworkError.CONFLICT)
                408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
                413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
                in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
                else -> {
                    println("DEBUG: Assign Vehicle Failed with status ${response.status.value}")
                    Result.Error(NetworkError.UNKNOWN)
                }
            }
        } catch (e: UnresolvedAddressException) {
            Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            println("DEBUG: Serialization Exception: $e")
            Result.Error(NetworkError.SERIALIZATION)
        } catch (e: Exception) {
            println("DEBUG: Unknown Exception: $e")
            e.printStackTrace()
            Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun assignProtectionPackageToBooking(
        bookingId: String,
        packageId: String
    ): Result<BookingDto, NetworkError> {
        return try {
            val response = client.post(
                "$SIXT_API_URL/api/booking/$bookingId/protections/$packageId"
            ) {
                contentType(ContentType.Application.Json)
            }
            
            println("DEBUG: Assign Protection Status: ${response.status.value}")
            
            when(response.status.value) {
                in 200..299 -> {
                    val bodyText = response.bodyAsText()
                    println("DEBUG: Assign Protection Response: $bodyText")
                    val updatedBooking = Json { ignoreUnknownKeys = true }.decodeFromString<BookingDto>(bodyText)
                    Result.Success(updatedBooking)
                }
                401 -> Result.Error(NetworkError.UNAUTHORIZED)
                409 -> Result.Error(NetworkError.CONFLICT)
                408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
                413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
                in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
                else -> {
                    println("DEBUG: Assign Protection Failed with status ${response.status.value}")
                    Result.Error(NetworkError.UNKNOWN)
                }
            }
        } catch (e: UnresolvedAddressException) {
            Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            println("DEBUG: Serialization Exception: $e")
            Result.Error(NetworkError.SERIALIZATION)
        } catch (e: Exception) {
            println("DEBUG: Unknown Exception: $e")
            e.printStackTrace()
            Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun completeBooking(bookingId: String): Result<BookingDto, NetworkError> {
        return try {
            val response = client.post(
                "$SIXT_API_URL/api/booking/$bookingId/complete"
            ) {
                contentType(ContentType.Application.Json)
            }
            
            println("DEBUG: Complete Booking Status: ${response.status.value}")
            
            when(response.status.value) {
                in 200..299 -> {
                    val bodyText = response.bodyAsText()
                    println("DEBUG: Complete Booking Response: $bodyText")
                    val completedBooking = Json { ignoreUnknownKeys = true }.decodeFromString<BookingDto>(bodyText)
                    Result.Success(completedBooking)
                }
                401 -> Result.Error(NetworkError.UNAUTHORIZED)
                409 -> Result.Error(NetworkError.CONFLICT)
                408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
                413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
                in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
                else -> {
                    val bodyText = response.bodyAsText()
                    println("DEBUG: Complete Booking Failed with status ${response.status.value}, body: $bodyText")
                    Result.Error(NetworkError.UNKNOWN)
                }
            }
        } catch (e: UnresolvedAddressException) {
            Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            println("DEBUG: Serialization Exception: $e")
            Result.Error(NetworkError.SERIALIZATION)
        } catch (e: Exception) {
            println("DEBUG: Unknown Exception: $e")
            e.printStackTrace()
            Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun lockCar(): Result<Unit, NetworkError> {
        return try {
            val response = client.post(
                "$SIXT_API_URL/api/car/lock"
            ) {
                contentType(ContentType.Application.Json)
            }
            
            when(response.status.value) {
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
        } catch (e: UnresolvedAddressException) {
            Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            Result.Error(NetworkError.SERIALIZATION)
        } catch (e: Exception) {
            Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun unlockCar(): Result<Unit, NetworkError> {
        return try {
            val response = client.post(
                "$SIXT_API_URL/api/car/unlock"
            ) {
                contentType(ContentType.Application.Json)
            }
            
            when(response.status.value) {
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
        } catch (e: UnresolvedAddressException) {
            Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            Result.Error(NetworkError.SERIALIZATION)
        } catch (e: Exception) {
            Result.Error(NetworkError.UNKNOWN)
        }
    }

    override suspend fun blinkCar(): Result<Unit, NetworkError> {
        return try {
            val response = client.post(
                "$SIXT_API_URL/api/car/blink"
            ) {
                contentType(ContentType.Application.Json)
            }
            
            when(response.status.value) {
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
        } catch (e: UnresolvedAddressException) {
            Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            Result.Error(NetworkError.SERIALIZATION)
        } catch (e: Exception) {
            Result.Error(NetworkError.UNKNOWN)
        }
    }
}