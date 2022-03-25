package org.ireader.core_ui.theme


import androidx.compose.ui.text.font.FontFamily


sealed class FontType(val fontName: String, val fontFamily: FontFamily) {
    object Poppins : FontType("Poppins", poppins)
    object SourceSansPro : FontType("Source Sans Pro", sourceSansPro)
    object SupermercadoOne : FontType("Supermercado One", supermercadoOne)
    object Comfortaa : FontType("Comfortaa", comfortaa)
}
