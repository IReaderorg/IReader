package ireader.presentation.ui.core.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * Available system fonts for the app UI
 */
enum class AppUiFont(val displayName: String, val fontFamily: FontFamily) {
    DEFAULT("Default (Roboto)", FontFamily.Default),
    SANS_SERIF("Sans Serif", FontFamily.SansSerif),
    SERIF("Serif", FontFamily.Serif),
    MONOSPACE("Monospace", FontFamily.Monospace),
    CURSIVE("Cursive", FontFamily.Cursive);
    
    companion object {
        fun fromString(value: String): AppUiFont {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: DEFAULT
        }
    }
}

/**
 * Get the FontFamily for a given font name
 */
fun getAppUiFontFamily(fontName: String): FontFamily {
    return AppUiFont.fromString(fontName).fontFamily
}
