package ireader.presentation.core

import androidx.compose.ui.text.font.FontFamily
import ireader.domain.models.common.FontFamilyModel

/**
 * Desktop-specific implementation for converting FontFamilyModel to Compose FontFamily
 * Desktop doesn't support Google Fonts API, so we use system fonts
 */
actual fun FontFamilyModel.toComposeFontFamily(): FontFamily {
    return when (this) {
        is FontFamilyModel.Default -> FontFamily.Default
        is FontFamilyModel.SansSerif -> FontFamily.SansSerif
        is FontFamilyModel.Serif -> FontFamily.Serif
        is FontFamilyModel.Monospace -> FontFamily.Monospace
        is FontFamilyModel.Cursive -> FontFamily.Cursive
        is FontFamilyModel.Custom -> {
            // Desktop: fallback to default for now
            // Could be extended to load local font files
            FontFamily.Default
        }
    }
}
