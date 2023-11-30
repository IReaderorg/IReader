package ireader.presentation.ui.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

fun ColorScheme.isLight(): Boolean {
    val isLight =  kotlin.runCatching {
        this.surface.luminance() > 0.5
    }.getOrDefault(false)
    return isLight
}

// val ColorScheme.isLight: Boolean
//    get() = this.background.luminance() > 0.5

data class CustomSystemColor(
    val status: Color,
    val navigation: Color
)
