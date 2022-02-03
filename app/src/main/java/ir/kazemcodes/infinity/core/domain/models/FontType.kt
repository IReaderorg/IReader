package ir.kazemcodes.infinity.core.domain.models

import androidx.compose.ui.text.font.FontFamily
import ir.kazemcodes.infinity.core.presentation.theme.poppins
import ir.kazemcodes.infinity.core.presentation.theme.sourceSansPro
import ir.kazemcodes.infinity.core.presentation.theme.supermercadoOne

sealed class FontType(val fontName: String, val fontFamily: FontFamily) {
    object Poppins : FontType("Poppins", poppins)
    object SourceSansPro : FontType("Source Sans Pro", sourceSansPro)
    object SupermercadoOne : FontType("Supermercado One", supermercadoOne)
}
