package config

import org.example.ridergo.BuildConfig

actual fun getGroqApiKey(): String {
    return BuildConfig.GROQ_API_KEY
}

actual fun getGroqModel(): String {
    return BuildConfig.GROQ_MODEL.ifEmpty { "llama-3.1-70b-versatile" }
}
