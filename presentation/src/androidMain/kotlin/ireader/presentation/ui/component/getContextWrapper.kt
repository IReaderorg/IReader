package ireader.presentation.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun getContextWrapper(): Any? {
    return LocalContext.current
}