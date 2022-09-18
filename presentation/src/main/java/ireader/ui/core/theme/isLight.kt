package ireader.ui.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

fun ColorScheme.isLight() = this.surface.luminance() > 0.5

// val ColorScheme.isLight: Boolean
//    get() = this.background.luminance() > 0.5

data class CustomSystemColor(
    val status: Color,
    val navigation: Color
)
