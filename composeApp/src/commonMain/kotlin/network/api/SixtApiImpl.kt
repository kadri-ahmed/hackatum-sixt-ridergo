package network.api

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.call.receive
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException
import models.AvailableVehiclesResponse
import utils.NetworkError
import utils.Result

class SixtApiImpl(private val client: HttpClient) : SixtApi {

    val SIXT_API_URL: String = "https://hackatum25.sixt.io/"

    override suspend fun getAvailableVehicles(bookingId: String): Result<AvailableVehiclesResponse, NetworkError> {
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
                val availableVehicles = response.body<AvailableVehiclesResponse>()
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
}