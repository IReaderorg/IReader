package org.ireader.core_api.util

import android.text.format.DateUtils
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime



class DateTimeFormatter constructor(pattern: String) {
    internal val jtFormatter = java.time.format.DateTimeFormatter.ofPattern(pattern)
}

fun LocalDateTime.format(formatter: DateTimeFormatter): String {
    return toJavaLocalDateTime().format(formatter.jtFormatter)
}
