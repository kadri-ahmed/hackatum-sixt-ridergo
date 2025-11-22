package repositories

import dto.GroqMessage
import network.api.GroqApi
import utils.NetworkError
import utils.Result

class ChatRepositoryImpl(private val groqApi: GroqApi) : ChatRepository {

    override suspend fun sendMessage(messages: List<GroqMessage>): Result<String, NetworkError> {
        return when (val result = groqApi.sendChatMessage(messages)) {
            is Result.Success -> {
                val response = result.data
                val assistantMessage = response.choices.firstOrNull()?.message?.content
                if (assistantMessage != null) {
                    Result.Success(assistantMessage)
                } else {
                    Result.Error(NetworkError.UNKNOWN)
                }
            }
            is Result.Error -> Result.Error(result.error)
        }
    }
}
