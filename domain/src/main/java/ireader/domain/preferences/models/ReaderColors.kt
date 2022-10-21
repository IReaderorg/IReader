package ireader.domain.preferences.models

import androidx.compose.ui.graphics.Color

data class ReaderColors(
    val id: Long,
    val backgroundColor: Color,
    val onTextColor: Color,
    val isDefault: Boolean = false
)