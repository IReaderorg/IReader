package ireader.presentation.core

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import ireader.domain.models.common.FontFamilyModel
/**
 * Android-specific implementation for converting FontFamilyModel to Compose FontFamily
 * This implementation supports Google Fonts with caching
 */

@OptIn(ExperimentalTextApi::class)
private val googleFontProvider: GoogleFont.Provider by lazy {
    // Using proper R class reference for font certificates
    // The resource is defined in presentation/src/androidMain/res/values/font_certs.xml
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = ireader.presentation.R.array.com_google_android_gms_fonts_certs
    )
}

// Cache for loaded font families to avoid recreating them
private val fontFamilyCache = mutableMapOf<String, FontFamily>()

@OptIn(ExperimentalTextApi::class)
actual fun FontFamilyModel.toComposeFontFamily(): FontFamily {
    return when (this) {
        is FontFamilyModel.Default -> FontFamily.Default
        is FontFamilyModel.SansSerif -> FontFamily.SansSerif
        is FontFamilyModel.Serif -> FontFamily.Serif
        is FontFamilyModel.Monospace -> FontFamily.Monospace
        is FontFamilyModel.Cursive -> FontFamily.Cursive
        is FontFamilyModel.Custom -> {
            // Check cache first - this ensures fonts are reused and not re-downloaded
            fontFamilyCache[name]?.let { 
                ireader.core.log.Log.debug("Using cached font: $name")
                return it 
            }
            
            try {
                ireader.core.log.Log.debug("Loading Google Font: $name")
                // Load font from Google Fonts with multiple weights for better rendering
                // Google Fonts API will cache these automatically on the device
                val fontFamily = FontFamily(
                    Font(
                        googleFont = GoogleFont(name),
                        fontProvider = googleFontProvider,
                        weight = FontWeight.Normal
                    ),
                    Font(
                        googleFont = GoogleFont(name),
                        fontProvider = googleFontProvider,
                        weight = FontWeight.Bold
                    ),
                    Font(
                        googleFont = GoogleFont(name),
                        fontProvider = googleFontProvider,
                        weight = FontWeight.Light
                    ),
                    Font(
                        googleFont = GoogleFont(name),
                        fontProvider = googleFontProvider,
                        weight = FontWeight.Medium
                    )
                )
                
                // Cache the font family in memory to avoid recreating FontFamily objects
                // The actual font files are cached by Google Play Services
                fontFamilyCache[name] = fontFamily
                ireader.core.log.Log.debug("Successfully loaded and cached font: $name")
                fontFamily
            } catch (e: Exception) {
                ireader.core.log.Log.error("Failed to load Google Font: $name", e)
                // Fallback to default if font loading fails
                // This can happen if Google Play Services is not available or font name is invalid
                FontFamily.Default
            }
        }
    }
}

/**
 * Clear the font family cache
 */
fun clearFontFamilyCache() {
    fontFamilyCache.clear()
}
