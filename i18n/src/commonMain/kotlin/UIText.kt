package ireader.i18n

import androidx.compose.runtime.Composable
import ireader.i18n.resources.MR


fun UiText.asString(localizeHelper: LocalizeHelper): String {
    return when (this) {
        is UiText.DynamicString -> text
        is UiText.StringResource -> localizeHelper.localize(resId)
        is UiText.MStringResource -> localizeHelper.localize(res)
        is UiText.ExceptionString -> {
            val eString = e.localizedMessage ?: localizeHelper.localize(MR.strings.error_unknown)
            return eString.substring(0, eString.length.coerceAtMost(500))
        }
        else -> ""
    }
}

