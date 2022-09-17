package ireader.domain.models.theme


import androidx.compose.material3.ColorScheme
import ireader.common.models.theme.ExtraColors

data class Theme(
    val id: Long,
    val materialColors: ColorScheme,
    val extraColors: ExtraColors,
    val isDark: Boolean = false,
)
