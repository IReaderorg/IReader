package org.ireader.core.utils

import java.text.SimpleDateFormat
import java.util.*

fun convertLongToTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.US)
    return format.format(date)
}

fun convertLongToTime(time: Long, format: String = "yyyy.MM.dd HH:mm"): String {
    val date = Date(time)
    val format = SimpleDateFormat(format, Locale.US)
    return format.format(date)
}

fun currentTimeToLong(): Long {
    return Calendar.getInstance().timeInMillis
}

fun convertDateToLong(date: String): Long {
    val df = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.US)
    return df.parse(date).time
}