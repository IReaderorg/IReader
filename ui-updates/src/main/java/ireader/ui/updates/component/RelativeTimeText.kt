package ireader.ui.updates.component

import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import ireader.common.extensions.asRelativeTimeString

@Composable
fun RelativeTimeText(modifier: Modifier = Modifier, date: LocalDate) {
    Text(
        text = date.asRelativeTimeString(),
        modifier = modifier,
        color = MaterialTheme.colorScheme.onBackground
    )
}

// fun LocalDate.asRelativeTimeString(): String {
//    return DateUtils
//        .getRelativeTimeSpanString(
//            atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
//            System.currentTimeMillis(),
//            DateUtils.DAY_IN_MILLIS
//        )
//        .toString()
// }
