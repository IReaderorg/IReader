package ireader.presentation.ui.settings.reader

import androidx.compose.runtime.Stable

@Stable
data class SettingState(
    val doh: Int = 0,
    val dialogState: Boolean = false,
)
