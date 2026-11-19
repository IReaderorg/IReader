package ireader.presentation.core.theme

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSLocale
import platform.Foundation.dateWithTimeIntervalSince1970

@OptIn(ExperimentalForeignApi::class)
actual fun formatSystemTime(timestampMillis: Long, platformContext: Any?): String {
    // Short time style follows the device's 12/24-hour setting and current locale.
    val formatter = NSDateFormatter().apply {
        dateStyle = NSDateFormatterNoStyle
        timeStyle = NSDateFormatterShortStyle
        locale = NSLocale.currentLocale
    }
    return formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(timestampMillis / 1000.0))
}
