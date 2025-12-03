package ireader.presentation.core

import androidx.compose.ui.text.font.FontFamily
import ireader.domain.models.common.FontFamilyModel

/**
 * iOS-specific implementation for converting FontFamilyModel to Compose FontFamily
 */
actual fun FontFamilyModel.toComposeFontFamily(): FontFamily {
    return when (this) {
        is FontFamilyModel.Default -> FontFamily.Default
        is FontFamilyModel.SansSerif -> FontFamily.SansSerif
        is FontFamilyModel.Serif -> FontFamily.Serif
        is FontFamilyModel.Monospace -> FontFamily.Monospace
        is FontFamilyModel.Cursive -> FontFamily.Cursive
        is FontFamilyModel.Custom -> {
            // iOS custom fonts would need to be bundled with the app
            // For now, fall back to default
            FontFamily.Default
        }
    }
}
