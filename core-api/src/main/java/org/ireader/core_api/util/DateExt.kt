package org.ireader.core_api.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime

class DateTimeFormatter constructor(pattern: String) {
    internal val jtFormatter = java.time.format.DateTimeFormatter.ofPattern(pattern)
}

fun LocalDateTime.format(formatter: DateTimeFormatter): String {
    return toJavaLocalDateTime().format(formatter.jtFormatter)
}
