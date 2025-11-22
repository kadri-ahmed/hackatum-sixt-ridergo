package repositories

import dto.GroqMessage
import network.api.GroqApi
import utils.NetworkError
import utils.Result

interface ChatRepository {
    suspend fun sendMessage(messages: List<GroqMessage>): Result<String, NetworkError>
}
