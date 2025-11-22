package network.api

import dto.GroqChatRequest
import dto.GroqChatResponse
import utils.NetworkError
import utils.Result

interface GroqApi {
    suspend fun sendChatMessage(messages: List<dto.GroqMessage>, apiKey: String? = null): Result<GroqChatResponse, NetworkError>
}
