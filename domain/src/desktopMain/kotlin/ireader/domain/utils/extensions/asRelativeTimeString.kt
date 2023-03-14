package ireader.domain.utils.extensions

import ireader.domain.models.prefs.PreferenceValues
import kotlinx.datetime.LocalDate

actual fun LocalDate.asRelativeTimeString(
    range: PreferenceValues.RelativeTime,
    dateFormat: String
): String {
    return ""
}