package utils

import platform.Foundation.*

actual fun getCurrentTimestamp(): String {
    // Use iOS Foundation APIs for timestamp generation
    val formatter = NSISO8601DateFormatter()
    formatter.formatOptions = NSISO8601DateFormatWithInternetDateTime
    formatter.timeZone = NSTimeZone.localTimeZone
    return formatter.stringFromDate(NSDate()) ?: ""
}
