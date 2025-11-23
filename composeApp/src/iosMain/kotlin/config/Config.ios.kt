package config

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle

@OptIn(ExperimentalForeignApi::class)
actual fun getGroqApiKey(): String {
    // Try to read from Info.plist first
    val infoPlist = NSBundle.mainBundle.infoDictionary
    val apiKey = infoPlist?.get("GROQ_API_KEY") as? String
    
    if (!apiKey.isNullOrEmpty()) {
        return apiKey
    }
    
    // Fallback: try to read from environment variable or return empty
    // In production, you should set this via Info.plist or build configuration
    return ""
}

@OptIn(ExperimentalForeignApi::class)
actual fun getGroqModel(): String {
    // Try to read from Info.plist first
    val infoPlist = NSBundle.mainBundle.infoDictionary
    val model = infoPlist?.get("GROQ_MODEL") as? String
    
    if (!model.isNullOrEmpty()) {
        return model
    }
    
    // Fallback to default model
    return "llama-3.1-70b-versatile"
}
