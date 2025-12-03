package ireader.domain.utils.extensions

import kotlinx.datetime.LocalDate

actual fun LocalDate.asRelativeTimeString(
    range: ireader.domain.models.prefs.PreferenceValues.RelativeTime,
    dateFormat: String,
): String {
    // TODO: Implement using NSDateFormatter with relative formatting
    return this.toString()
}
