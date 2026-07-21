package ireader.core.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

actual fun LocalDate.asRelativeTimeString(): String {
    // TODO: Implement using NSDateFormatter with relative formatting
    return this.toString()
}

actual class DateTimeFormatter actual constructor(pattern: String) {
    private val pattern: String = pattern
    
    fun format(dateTime: LocalDateTime): String {
        // TODO: Implement using NSDateFormatter
        return dateTime.toString()
    }
}

actual fun LocalDateTime.format(formatter: DateTimeFormatter): String {
    return formatter.format(this)
}
