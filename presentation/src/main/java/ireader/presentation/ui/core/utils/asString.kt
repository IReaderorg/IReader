package ireader.presentation.ui.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ireader.i18n.UiText

@Composable
fun UiText.StringResource.asString(): String {
    return LocalContext.current.getString(resId)
}