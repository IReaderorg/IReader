package ireader.domain.preferences.models

import androidx.compose.ui.text.font.FontFamily

data class FontType(
    val name: String,
    val fontFamily: FontFamily
)
fun getDefaultFont(): FontType {
    return FontType("Roboto", FontFamily.Default)
}