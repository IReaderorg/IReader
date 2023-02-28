package ireader.i18n

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
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
@Composable
actual fun UiText.asString(): String {
    val context = LocalContext.current
    return when (this) {
        is UiText.DynamicString -> text
        is UiText.StringResource -> context.getString(resId)
        is UiText.MStringResource -> StringDesc.Resource(res).toString(context)
        is UiText.ExceptionString -> {
            val eString = e.localizedMessage ?: context.getString(R.string.error_unknown)
            return eString.substring(0, eString.length.coerceAtMost(500))
        }
        else -> ""
    }
}