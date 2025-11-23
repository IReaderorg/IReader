package ireader.presentation.core

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import ireader.domain.models.common.FontFamilyModel

/**
 * Android-specific implementation for converting FontFamilyModel to Compose FontFamily
 * This implementation supports Google Fonts
 * 
 * Note: The proper GoogleFont.Provider with certificates is injected via DI in AppModule.
 * This is a fallback implementation that uses a hardcoded resource ID.
 */

@OptIn(ExperimentalTextApi::class)
private val googleFontProvider: GoogleFont.Provider by lazy {
    // Using hardcoded resource ID to avoid module dependency issues
    // The resource is defined in android/src/main/res/values/font_certs.xml
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = 0x7f030000 // Hardcoded R.array.com_google_android_gms_fonts_certs
    )
}

@OptIn(ExperimentalTextApi::class)
actual fun FontFamilyModel.toComposeFontFamily(): FontFamily {
    return when (this) {
        is FontFamilyModel.Default -> FontFamily.Default
        is FontFamilyModel.SansSerif -> FontFamily.SansSerif
        is FontFamilyModel.Serif -> FontFamily.Serif
        is FontFamilyModel.Monospace -> FontFamily.Monospace
        is FontFamilyModel.Cursive -> FontFamily.Cursive
        is FontFamilyModel.Custom -> {
            try {
                // Load font from Google Fonts
                FontFamily(
                    Font(
                        googleFont = GoogleFont(name),
                        fontProvider = googleFontProvider
                    )
                )
            } catch (e: Exception) {
                // Fallback to default if font loading fails
                FontFamily.Default
            }
        }
    }
}
