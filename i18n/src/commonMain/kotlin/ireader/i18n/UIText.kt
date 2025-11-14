package ireader.i18n

import ireader.i18n.resources.Res
import ireader.i18n.resources.error_unknown


fun UiText.asString(localizeHelper: LocalizeHelper): String {
    return when (this) {
        is UiText.DynamicString -> text
        is UiText.StringResource -> localizeHelper.localize(resId)
        is UiText.MStringResource -> localizeHelper.localize(res)
        is UiText.ExceptionString -> {
            val eString = e.localizedMessage ?: localizeHelper.localize(Res.string.error_unknown)
            return eString.substring(0, eString.length.coerceAtMost(500))
        }
        else -> ""
    }
}

