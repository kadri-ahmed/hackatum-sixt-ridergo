package dto

import kotlinx.serialization.Serializable

@Serializable
data class GroqChatRequest(
    val messages: List<GroqMessage>,
    val model: String = "llama-3.1-70b-versatile",
    val temperature: Double = 0.7,
    val max_tokens: Int = 1024
)

@Serializable
data class GroqMessage(
    val role: String,
    val content: String
)

@Serializable
data class GroqChatResponse(
    val id: String,
    val choices: List<GroqChoice>,
    val created: Long,
    val model: String,
    val usage: GroqUsage?
)

@Serializable
data class GroqChoice(
    val index: Int,
    val message: GroqMessage,
    val finish_reason: String?
)

@Serializable
data class GroqUsage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)
