package ireader.common.resources

import android.content.Context

fun UiText.asString(context: Context): String {
    return when (this) {
        is UiText.DynamicString -> text
        is UiText.StringResource -> context.getString(resId)
        is UiText.ExceptionString -> {
            val eString = e.localizedMessage ?: context.getString(R.string.error_unknown)
            return eString.substring(0, eString.length.coerceAtMost(500))
        }
        else -> ""
    }
}