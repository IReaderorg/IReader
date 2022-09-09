package ireader.common.models.theme

import androidx.compose.material3.ColorScheme

data class Theme(
    val id: Long,
    val materialColors: ColorScheme,
    val extraColors: ExtraColors,
    val isDark: Boolean = false,
)
