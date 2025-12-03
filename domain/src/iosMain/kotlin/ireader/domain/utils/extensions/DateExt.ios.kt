package ireader.domain.utils.extensions

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSRelativeDateTimeFormatter
import platform.Foundation.NSRelativeDateTimeFormatterUnitsStyleFull
import platform.Foundation.currentLocale
import kotlin.time.ExperimentalTime

/**
 * iOS implementation of relative time string formatting
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
actual fun LocalDate.asRelativeTimeString(
    range: ireader.domain.models.prefs.PreferenceValues.RelativeTime,
    dateFormat: String,
): String {
    val now = kotlin.time.Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val daysDiff = (today.toEpochDays() - this.toEpochDays()).toInt()
    
    val maxDays = when (range) {
        ireader.domain.models.prefs.PreferenceValues.RelativeTime.Off -> 0
        ireader.domain.models.prefs.PreferenceValues.RelativeTime.Day -> 1
        ireader.domain.models.prefs.PreferenceValues.RelativeTime.Week -> 7
        ireader.domain.models.prefs.PreferenceValues.RelativeTime.Seconds -> 0
        ireader.domain.models.prefs.PreferenceValues.RelativeTime.Minutes -> 0
        ireader.domain.models.prefs.PreferenceValues.RelativeTime.Hour -> 0
    }
    
    return if (daysDiff in 0..maxDays && maxDays > 0) {
        getRelativeString(daysDiff)
    } else {
        formatDateIos(dateFormat)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun LocalDate.getRelativeString(daysDiff: Int): String {
    return when (daysDiff) {
        0 -> "Today"
        1 -> "Yesterday"
        in 2..6 -> "$daysDiff days ago"
        7 -> "1 week ago"
        in 8..27 -> "${daysDiff / 7} weeks ago"
        28, 29, 30 -> "1 month ago"
        else -> formatDateIos("MMM d, yyyy")
    }
}


@OptIn(ExperimentalForeignApi::class)
private fun LocalDate.formatDateIos(format: String): String {
    val calendar = NSCalendar.currentCalendar
    val components = NSDateComponents().apply {
        setYear(this@formatDateIos.year.toLong())
        setMonth(this@formatDateIos.month.number.toLong())
        setDay(this@formatDateIos.day.toLong())
    }
    
    val nsDate = calendar.dateFromComponents(components) ?: return this.toString()
    
    val formatter = NSDateFormatter().apply {
        dateFormat = format.ifEmpty { "MMM d, yyyy" }
        locale = NSLocale.currentLocale
    }
    
    return formatter.stringFromDate(nsDate)
}

@OptIn(ExperimentalForeignApi::class)
fun LocalDate.format(pattern: String): String = formatDateIos(pattern)

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
fun LocalDate.toLocalizedRelativeString(): String {
    val now = kotlin.time.Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val daysDiff = (today.toEpochDays() - this.toEpochDays()).toInt()
    val secondsDiff = daysDiff * 24 * 60 * 60.0
    
    val formatter = NSRelativeDateTimeFormatter().apply {
        unitsStyle = NSRelativeDateTimeFormatterUnitsStyleFull
    }
    
    return formatter.localizedStringFromTimeInterval(-secondsDiff)
}
