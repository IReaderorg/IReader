package org.ireader.core_ui.theme

import androidx.compose.ui.text.font.FontFamily

sealed class FontType(val fontName: String, val fontFamily: FontFamily) {
    object Poppins : FontType("Poppins", poppins)
    object SourceSansPro : FontType("Source Sans Pro", sourceSansPro)
    object SupermercadoOne : FontType("Supermercado One", supermercadoOne)
    object Comfortaa : FontType("Comfortaa", comfortaa)
    object PTSerif : FontType("PT Serif", pt_serif)
    object ArbutusSlab : FontType("Arbutus Slab", arbutus_slab)
    object Domine : FontType("Domine", domine)
    object Lora : FontType("Lora", lora)
    object Nunito : FontType("Nunito", nunito)
    object Noto : FontType("Noto", noto)
    object OpenSand : FontType("Noto",openSans)
    object RobotoSerif : FontType("Roboto Serif",roboto_serif)
}
