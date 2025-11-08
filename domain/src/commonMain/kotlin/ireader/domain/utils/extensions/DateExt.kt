package ireader.domain.utils.extensions


import kotlinx.datetime.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.ExperimentalTime

fun Date.toDateTimestampString(dateFormatter: DateFormat): String {
    val date = dateFormatter.format(this)
    val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(this)
    return "$date $time"
}

expect fun LocalDate.asRelativeTimeString(
    range: ireader.domain.models.prefs.PreferenceValues.RelativeTime = ireader.domain.models.prefs.PreferenceValues.RelativeTime.Day,
    dateFormat: String = "",
): String


class DateTimeFormatter constructor(pattern: String) {
    internal val jtFormatter = java.time.format.DateTimeFormatter.ofPattern(pattern)
}

fun LocalDateTime.format(formatter: DateTimeFormatter): String {
    return toJavaLocalDateTime().format(formatter.jtFormatter)
}
fun convertLongToTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.US)
    return format.format(date)
}


@OptIn(ExperimentalTime::class)
fun currentTimeToLong(): Long {
    return kotlin.time.Clock.System.now().toEpochMilliseconds()
}

@OptIn(ExperimentalTime::class)
fun Long.toLocalDate() : LocalDateTime {
    return kotlinx.datetime.Instant
        .fromEpochMilliseconds(this)
        .toLocalDateTime((kotlinx.datetime.TimeZone.currentSystemDefault()))

}
