package ireader.core.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ireader.common.resources.UiText

@Composable
fun UiText.StringResource.asString(): String {
    return LocalContext.current.getString(resId)
}