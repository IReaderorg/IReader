package ireader.presentation.ui.component

import androidx.compose.runtime.Composable

@Composable
actual fun getContextWrapper(): Any? {
    // iOS doesn't have Android Context
    return null
}
