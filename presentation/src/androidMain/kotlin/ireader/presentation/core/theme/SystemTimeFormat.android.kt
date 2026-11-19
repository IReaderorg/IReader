package ireader.presentation.core.theme

import android.content.Context
import java.text.DateFormat
import java.util.Date

actual fun formatSystemTime(timestampMillis: Long, platformContext: Any?): String {
    // DateFormat.getTimeFormat reads the system "Use 24-hour format" setting.
    val formatter = (platformContext as? Context)?.let { DateFormat.getTimeFormat(it) }
        ?: DateFormat.getTimeInstance(DateFormat.SHORT)
    return formatter.format(Date(timestampMillis))
}
