package ireader.presentation.core.theme

import java.text.DateFormat
import java.util.Date
import java.util.Locale

actual fun formatSystemTime(timestampMillis: Long, platformContext: Any?): String {
    return DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
        .format(Date(timestampMillis))
}
