package network

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun createHttpClient(engine: HttpClientEngine): HttpClient {
    return HttpClient(engine) {
        install(Logging) {
            level = LogLevel.ALL
        }

        install(ContentNegotiation) { // for JSON parsing
            json(
                json = Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    isLenient = true
                }
            )
        }
    }
}