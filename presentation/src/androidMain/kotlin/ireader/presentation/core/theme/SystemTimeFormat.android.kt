package ireader.presentation.core.theme

import android.content.Context
import android.text.format.DateFormat as AndroidDateFormat
import java.text.DateFormat
import java.util.Date

actual fun formatSystemTime(timestampMillis: Long, platformContext: Any?): String {
    // AndroidDateFormat.getTimeFormat reads the system "Use 24-hour format" setting.
    val formatter = (platformContext as? Context)?.let { AndroidDateFormat.getTimeFormat(it) }
        ?: DateFormat.getTimeInstance(DateFormat.SHORT)
    return formatter.format(Date(timestampMillis))
}
