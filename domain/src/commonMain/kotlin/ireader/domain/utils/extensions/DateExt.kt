package ireader.domain.utils.extensions

import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Kotlin Multiplatform date/time utilities using kotlinx-datetime.
 * These replace Java date/time APIs for cross-platform compatibility.
 */

expect fun LocalDate.asRelativeTimeString(
    range: ireader.domain.models.prefs.PreferenceValues.RelativeTime = ireader.domain.models.prefs.PreferenceValues.RelativeTime.Day,
    dateFormat: String = "",
): String

/**
 * Get current time in milliseconds (epoch).
 * Replaces System.currentTimeMillis() and Calendar.getInstance().timeInMillis
 */
@OptIn(ExperimentalTime::class)
fun currentTimeToLong(): Long {
    return kotlin.time.Clock.System.now().toEpochMilliseconds()
}

/**
 * Convert epoch milliseconds to LocalDateTime.
 */
@OptIn(ExperimentalTime::class)
fun Long.toLocalDate(): LocalDateTime {
    return Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
}

/**
 * Convert epoch milliseconds to LocalDate (date only, no time).
 */
@OptIn(ExperimentalTime::class)
fun Long.toLocalDateOnly(): LocalDate {
    return Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
}

/**
 * Get current Instant.
 */
@OptIn(ExperimentalTime::class)
fun currentInstant(): Instant {
    return kotlin.time.Clock.System.now()
}

/**
 * Get today's date at start of day in milliseconds.
 */
@OptIn(ExperimentalTime::class)
fun todayStartMillis(): Long {
    val now = kotlin.time.Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return today.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

/**
 * Get yesterday's date at start of day in milliseconds.
 */
@OptIn(ExperimentalTime::class)
fun yesterdayStartMillis(): Long {
    val now = kotlin.time.Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    return yesterday.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

/**
 * Get date N days ago at start of day in milliseconds.
 */
@OptIn(ExperimentalTime::class)
fun daysAgoStartMillis(days: Int): Long {
    val now = kotlin.time.Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val targetDate = today.minus(days, DateTimeUnit.DAY)
    return targetDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

/**
 * Format epoch milliseconds to a simple date-time string.
 * Format: "yyyy.MM.dd HH:mm"
 * This is a platform-agnostic basic formatter.
 */
@OptIn(ExperimentalTime::class)
fun convertLongToTime(time: Long): String {
    val dateTime = Instant.fromEpochMilliseconds(time)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    
    val year = dateTime.year
    val month = dateTime.monthNumber.toString().padStart(2, '0')
    val day = dateTime.dayOfMonth.toString().padStart(2, '0')
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    
    return "$year.$month.$day $hour:$minute"
}

/**
 * Format LocalDateTime to ISO date string (yyyy-MM-dd).
 */
fun LocalDateTime.toIsoDateString(): String {
    val month = monthNumber.toString().padStart(2, '0')
    val day = dayOfMonth.toString().padStart(2, '0')
    return "$year-$month-$day"
}

/**
 * Format LocalDateTime to ISO date-time string.
 */
fun LocalDateTime.toIsoDateTimeString(): String {
    val month = monthNumber.toString().padStart(2, '0')
    val day = dayOfMonth.toString().padStart(2, '0')
    val h = hour.toString().padStart(2, '0')
    val m = minute.toString().padStart(2, '0')
    val s = second.toString().padStart(2, '0')
    return "$year-$month-${day}T$h:$m:$s"
}

/**
 * Check if a timestamp (in millis) is from today.
 */
@OptIn(ExperimentalTime::class)
fun Long.isToday(): Boolean {
    val todayStart = todayStartMillis()
    val tomorrowStart = todayStart + 24 * 60 * 60 * 1000
    return this in todayStart until tomorrowStart
}

/**
 * Check if a timestamp (in millis) is from yesterday.
 */
@OptIn(ExperimentalTime::class)
fun Long.isYesterday(): Boolean {
    val yesterdayStart = yesterdayStartMillis()
    val todayStart = todayStartMillis()
    return this in yesterdayStart until todayStart
}

/**
 * Check if a timestamp (in millis) is within the last N days.
 */
@OptIn(ExperimentalTime::class)
fun Long.isWithinDays(days: Int): Boolean {
    val startMillis = daysAgoStartMillis(days)
    val now = currentTimeToLong()
    return this in startMillis..now
}

/**
 * Format epoch milliseconds to "MMM dd, yyyy HH:mm" format.
 * Example: "Dec 03, 2025 14:30"
 */
fun Long.formatDateTime(): String {
    val dt = this.toLocalDate()
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val month = monthNames[dt.monthNumber - 1]
    val day = dt.dayOfMonth.toString().padStart(2, '0')
    val hour = dt.hour.toString().padStart(2, '0')
    val minute = dt.minute.toString().padStart(2, '0')
    return "$month $day, ${dt.year} $hour:$minute"
}

/**
 * Format epoch milliseconds to "MMM d, yyyy" format.
 * Example: "Dec 3, 2025"
 */
fun Long.formatDate(): String {
    val dt = this.toLocalDate()
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val month = monthNames[dt.monthNumber - 1]
    return "$month ${dt.dayOfMonth}, ${dt.year}"
}

/**
 * Format epoch milliseconds to "h:mm a" format (12-hour with AM/PM).
 * Example: "2:30 PM"
 */
fun Long.formatTime12Hour(): String {
    val dt = this.toLocalDate()
    val hour12 = if (dt.hour == 0) 12 else if (dt.hour > 12) dt.hour - 12 else dt.hour
    val amPm = if (dt.hour < 12) "AM" else "PM"
    val minute = dt.minute.toString().padStart(2, '0')
    return "$hour12:$minute $amPm"
}

/**
 * Format epoch milliseconds to "yyyyMMdd_HHmmss" format (for filenames).
 * Example: "20251203_143025"
 */
fun Long.formatForFilename(): String {
    val dt = this.toLocalDate()
    val month = dt.monthNumber.toString().padStart(2, '0')
    val day = dt.dayOfMonth.toString().padStart(2, '0')
    val hour = dt.hour.toString().padStart(2, '0')
    val minute = dt.minute.toString().padStart(2, '0')
    val second = dt.second.toString().padStart(2, '0')
    return "${dt.year}$month${day}_$hour$minute$second"
}

/**
 * Format epoch milliseconds to ISO 8601 date format "yyyy-MM-dd".
 * Example: "2025-12-03"
 */
fun Long.formatIsoDate(): String {
    val dt = this.toLocalDate()
    val month = dt.monthNumber.toString().padStart(2, '0')
    val day = dt.dayOfMonth.toString().padStart(2, '0')
    return "${dt.year}-$month-$day"
}

/**
 * Format epoch milliseconds to ISO 8601 datetime format.
 * Example: "2025-12-03T14:30:25Z"
 */
fun Long.formatIsoDateTime(): String {
    val dt = this.toLocalDate()
    val month = dt.monthNumber.toString().padStart(2, '0')
    val day = dt.dayOfMonth.toString().padStart(2, '0')
    val hour = dt.hour.toString().padStart(2, '0')
    val minute = dt.minute.toString().padStart(2, '0')
    val second = dt.second.toString().padStart(2, '0')
    return "${dt.year}-$month-${day}T$hour:$minute:${second}Z"
}

/**
 * Format a relative time string (e.g., "2 hours ago", "3 days ago").
 */
fun Long.formatRelativeTime(): String {
    val now = currentTimeToLong()
    val diff = now - this
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000} minutes ago"
        diff < 86400_000 -> "${diff / 3600_000} hours ago"
        diff < 604800_000 -> "${diff / 86400_000} days ago"
        else -> this.formatDate()
    }
}
