package ireader.domain.utils.extensions

import android.content.Context
import ireader.domain.R
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant.Companion.fromEpochMilliseconds
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import kotlinx.datetime.toLocalDateTime
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun convertLongToTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.US)
    return format.format(date)
}

fun convertLongToTime(time: Long, format: String = "yyyy.MM.dd HH:mm"): String {
    val date = Date(time)
    val format = SimpleDateFormat(format, Locale.getDefault())
    return format.format(date)
}

fun currentTimeToLong(): Long {
    return Clock.System.now().toEpochMilliseconds()
}

fun convertDateToLong(date: String): Long? {
    val df = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.US)
    return df.parse(date)?.time
}

fun Long.toLocalDate() : LocalDateTime {
    return kotlinx.datetime.Instant
        .fromEpochMilliseconds(this)
        .toLocalDateTime((kotlinx.datetime.TimeZone.currentSystemDefault()))

}

/**
 * Get date as time key
 *
 * @param date desired date
 * @return date as time key
 */
fun Long.toDateKey(): Date {
    val cal = Calendar.getInstance()
    cal.time = Date(this)
    cal[Calendar.HOUR_OF_DAY] = 0
    cal[Calendar.MINUTE] = 0
    cal[Calendar.SECOND] = 0
    cal[Calendar.MILLISECOND] = 0
    return cal.time
}

/**
 * Convert epoch long to Calendar instance
 *
 * @return Calendar instance at supplied epoch time. Null if epoch was 0.
 */
fun Long.toCalendar(): Calendar? {
    if (this == 0L) {
        return null
    }
    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    return cal
}

/**
 * Convert local time millisecond value to Calendar instance in UTC
 *
 * @return UTC Calendar instance at supplied time. Null if time is 0.
 */
fun Long.toUtcCalendar(): Calendar? {
    if (this == 0L) {
        return null
    }
    val rawCalendar = Calendar.getInstance().apply {
        timeInMillis = this@toUtcCalendar
    }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(
            rawCalendar.get(Calendar.YEAR),
            rawCalendar.get(Calendar.MONTH),
            rawCalendar.get(Calendar.DAY_OF_MONTH),
            rawCalendar.get(Calendar.HOUR_OF_DAY),
            rawCalendar.get(Calendar.MINUTE),
            rawCalendar.get(Calendar.SECOND),
        )
    }
}

/**
 * Convert UTC time millisecond to Calendar instance in local time zone
 *
 * @return local Calendar instance at supplied UTC time. Null if time is 0.
 */
fun Long.toLocalCalendar(): Calendar? {
    if (this == 0L) {
        return null
    }
    val rawCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = this@toLocalCalendar
    }
    return Calendar.getInstance().apply {
        clear()
        set(
            rawCalendar.get(Calendar.YEAR),
            rawCalendar.get(Calendar.MONTH),
            rawCalendar.get(Calendar.DAY_OF_MONTH),
            rawCalendar.get(Calendar.HOUR_OF_DAY),
            rawCalendar.get(Calendar.MINUTE),
            rawCalendar.get(Calendar.SECOND),
        )
    }
}

private const val MILLISECONDS_IN_DAY = 86_400_000L

fun Date.toRelativeString(
    context: Context,
    range: Int = 7,
    dateFormat: DateFormat = DateFormat.getDateInstance(DateFormat.SHORT),
): String {
    if (range == 0) {
        return dateFormat.format(this)
    }
    val now = Date()
    val difference = now.timeWithOffset.floorNearest(MILLISECONDS_IN_DAY) - this.timeWithOffset.floorNearest(MILLISECONDS_IN_DAY)
    val days = difference.floorDiv(MILLISECONDS_IN_DAY).toInt()
    return when {
        difference < 0 -> context.getString(R.string.recently)
        difference < MILLISECONDS_IN_DAY -> context.getString(R.string.relative_time_today)
        difference < MILLISECONDS_IN_DAY.times(range) -> context.resources.getQuantityString(
            R.plurals.relative_time,
            days,
            days,
        )
        else -> dateFormat.format(this)
    }
}

private val Date.timeWithOffset: Long
    get() {
        return Calendar.getInstance().run {
            time = this@timeWithOffset
            val dstOffset = get(Calendar.DST_OFFSET)
            this@timeWithOffset.time + timeZone.rawOffset + dstOffset
        }
    }

fun Long.floorNearest(to: Long): Long {
    return this.floorDiv(to) * to
}
