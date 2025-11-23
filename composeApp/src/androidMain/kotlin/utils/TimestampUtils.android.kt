package utils

import java.time.Instant

actual fun getCurrentTimestamp(): String {
    return Instant.now().toString()
}
