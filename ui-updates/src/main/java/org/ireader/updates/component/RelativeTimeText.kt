package org.ireader.updates.component

import android.text.format.DateUtils
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

@Composable
fun RelativeTimeText(modifier: Modifier = Modifier, date: LocalDate) {
    Text(
        text = date.asRelativeTimeString(),
        modifier = modifier,
        color = MaterialTheme.colorScheme.onBackground
    )
}

fun LocalDate.asRelativeTimeString(): String {
    return DateUtils
        .getRelativeTimeSpanString(
            atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            System.currentTimeMillis(),
            DateUtils.DAY_IN_MILLIS
        )
        .toString()
}
