package ireader.domain.utils.extensions

import android.text.format.DateUtils
import ireader.domain.models.prefs.PreferenceValues
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
actual fun LocalDate.asRelativeTimeString(
    range: PreferenceValues.RelativeTime,
    dateFormat: String
): String {
    val rangeFormat = when (range) {
        ireader.domain.models.prefs.PreferenceValues.RelativeTime.Seconds, -> DateUtils.SECOND_IN_MILLIS
        ireader.domain.models.prefs.PreferenceValues.RelativeTime.Minutes -> DateUtils.MINUTE_IN_MILLIS
        ireader.domain.models.prefs.PreferenceValues.RelativeTime.Hour -> DateUtils.HOUR_IN_MILLIS
        ireader.domain.models.prefs.PreferenceValues.RelativeTime.Day -> DateUtils.DAY_IN_MILLIS
        ireader.domain.models.prefs.PreferenceValues.RelativeTime.Week -> DateUtils.WEEK_IN_MILLIS
        ireader.domain.models.prefs.PreferenceValues.RelativeTime.Off -> null
    }
    return DateUtils
        .getRelativeTimeSpanString(
            atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            System.currentTimeMillis(),
            rangeFormat ?: DateUtils.DAY_IN_MILLIS
        )
        .toString()
}