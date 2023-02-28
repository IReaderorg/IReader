package ireader.i18n

import androidx.compose.runtime.Composable
import ireader.i18n.resources.MR

@Composable
actual fun UiText.asString(): String {
    return when (this) {
        is UiText.DynamicString -> text
        is UiText.MStringResource -> localize(res)
        is UiText.ExceptionString -> {
            val eString = e.localizedMessage ?: localize(MR.strings.error_unknown)
            return eString.substring(0, eString.length.coerceAtMost(500))
        }
        else -> ""
    }
}