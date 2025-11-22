package network.api

import config.getGroqApiKey
import config.getGroqModel
import dto.GroqChatRequest
import dto.GroqChatResponse
import dto.GroqMessage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.statement.bodyAsText
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException
import utils.NetworkError
import utils.Result

class GroqApiImpl(private val client: HttpClient) : GroqApi {

    private val GROQ_API_URL: String = "https://api.groq.com/openai/v1/chat/completions"

    override suspend fun sendChatMessage(messages: List<GroqMessage>): Result<GroqChatResponse, NetworkError> {
        return try {
            val apiKey = getGroqApiKey()
            if (apiKey.isEmpty()) {
                return Result.Error(NetworkError.UNAUTHORIZED)
            }

            val model = getGroqModel()
            val request = GroqChatRequest(
                messages = messages,
                model = model,
                temperature = 0.7,
                max_tokens = 1024
            )

            val response = client.post(GROQ_API_URL) {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $apiKey")
                }
                setBody(request)
            }

            when (response.status.value) {
                in 200..299 -> {
                    val chatResponse = response.body<GroqChatResponse>()
                    Result.Success(chatResponse)
                }
                401 -> Result.Error(NetworkError.UNAUTHORIZED)
                429 -> Result.Error(NetworkError.REQUEST_TIMEOUT) // Rate limit
                in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
                else -> {
                    println("DEBUG: Groq API failed with status: ${response.status.value}")
                    println("DEBUG: Response body: ${response.bodyAsText()}")
                    Result.Error(NetworkError.UNKNOWN)
                }
            }
        } catch (e: UnresolvedAddressException) {
            println("DEBUG: Groq API failed: No Internet")
            Result.Error(NetworkError.NO_INTERNET)
        } catch (e: SerializationException) {
            println("DEBUG: Groq API failed: Serialization Exception: ${e.message}")
            e.printStackTrace()
            Result.Error(NetworkError.SERIALIZATION)
        } catch (e: Exception) {
            println("DEBUG: Groq API failed: Unknown Exception: ${e.message}")
            e.printStackTrace()
            Result.Error(NetworkError.UNKNOWN)
        }
    }
}
