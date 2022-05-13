package org.ireader.core_ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.ireader.core_ui.R

// Set of Material typography styles to start with
val poppins = FontFamily(
    Font(R.font.poppins_regular, weight = FontWeight.Normal),
    Font(R.font.poppins_semibold, weight = FontWeight.SemiBold)
)
val sourceSansPro = FontFamily(
    listOf(
        Font(R.font.source_sans_pro_w200_extra_light, weight = FontWeight.ExtraLight),
        Font(R.font.source_sans_pro_lignt, weight = FontWeight.Light),
        Font(R.font.source_sans_pro_resgular, weight = FontWeight.Normal),
        Font(R.font.source_sans_pro_semi_bold, weight = FontWeight.SemiBold),
        Font(R.font.source_sans_pro_bold_700, weight = FontWeight.Bold),
        Font(R.font.source_sans_pro_900, weight = FontWeight.ExtraBold),
    )
)
val supermercadoOne = FontFamily(
    listOf(
        Font(R.font.supermercado_one_regular, weight = FontWeight.Normal),
    )
)
val pt_serif = FontFamily(
    listOf(
        Font(R.font.ptserif_regular, weight = FontWeight.Normal),
    )
)
val arbutus_slab = FontFamily(
    listOf(
        Font(R.font.arbutusslab_regular, weight = FontWeight.Normal),
    )
)
val domine = FontFamily(
    listOf(
        Font(R.font.domine_regular, weight = FontWeight.Normal),
    )
)
val lora = FontFamily(
    listOf(
        Font(R.font.lora_regular, weight = FontWeight.Normal),
    )
)
val nunito = FontFamily(
    listOf(
        Font(R.font.nunito_regular, weight = FontWeight.Normal),
    )
)
val noto = FontFamily(
    listOf(
        Font(R.font.noto_sans_regular, weight = FontWeight.Normal),
    )
)
val openSans = FontFamily(
    listOf(
        Font(R.font.opensans_regular, weight = FontWeight.Normal),
    )
)
val roboto_serif = FontFamily(
    listOf(
        Font(R.font.robotoserif_regular, weight = FontWeight.Normal),
    )
)
val comfortaa = FontFamily(
    listOf(
        Font(R.font.comfortaa_font_regular, weight = FontWeight.Normal),
        Font(R.font.comfortaa_font_regular, weight = FontWeight.Medium),
        Font(R.font.comfortaa_font_regular, weight = FontWeight.W400),
        Font(R.font.comfortaa_font_regular, weight = FontWeight.SemiBold),
    )
)

// Set of Material typography styles to start with
val Typography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(fontFamily = sourceSansPro, fontWeight = FontWeight.W700),
    displayMedium = TextStyle(fontFamily = sourceSansPro, fontWeight = FontWeight.W700),
    displaySmall = TextStyle(fontFamily = sourceSansPro, fontWeight = FontWeight.W700),
    headlineLarge = TextStyle(fontFamily = sourceSansPro, fontWeight = FontWeight.W700),
    headlineMedium = TextStyle(fontFamily = sourceSansPro, fontWeight = FontWeight.W700),
    headlineSmall = TextStyle(fontFamily = sourceSansPro, fontWeight = FontWeight.W700),
    titleLarge = TextStyle(fontFamily = sourceSansPro, fontWeight = FontWeight.W700),
    titleMedium = TextStyle(fontFamily = sourceSansPro, fontWeight = FontWeight.W700),
    titleSmall = TextStyle(fontFamily = sourceSansPro, fontWeight = FontWeight.W700),
    labelLarge = TextStyle(fontFamily = sourceSansPro, fontWeight = FontWeight.W600),
    labelMedium = TextStyle(fontFamily = sourceSansPro, fontWeight = FontWeight.W500),
    labelSmall = TextStyle(fontFamily = sourceSansPro, fontWeight = FontWeight.W500),
    bodyLarge = TextStyle(fontFamily = sourceSansPro, fontWeight = FontWeight.W500),
    bodyMedium = TextStyle(fontFamily = sourceSansPro, fontWeight = FontWeight.W500),
    bodySmall = TextStyle(fontFamily = sourceSansPro, fontWeight = FontWeight.W500),
)

val fonts = listOf<FontType>(
    FontType.Poppins,
    FontType.SourceSansPro,
    FontType.SupermercadoOne,
    FontType.Comfortaa,
    FontType.PTSerif,
    FontType.ArbutusSlab,
    FontType.Domine,
    FontType.Lora,
    FontType.Nunito,
    FontType.Noto,
    FontType.OpenSand,
    FontType.RobotoSerif,
)

val readerScreenBackgroundColors = listOf<BackgroundColor>(
    BackgroundColor.Black,
    BackgroundColor.White,
    BackgroundColor.GrayishBlack,
    BackgroundColor.CloudColor,
    BackgroundColor.Gray88,
    BackgroundColor.CornflowerBlue,
    BackgroundColor.NewCar,
    BackgroundColor.AntiqueWhite,
    BackgroundColor.LavenderRose,
    BackgroundColor.ArylideYellow,
    BackgroundColor.DesertSand,
    BackgroundColor.Lavender,
    BackgroundColor.PastelPink,
    BackgroundColor.PastelYellow,
)

sealed class BackgroundColor(val color: Color, val onTextColor: Color, val index: Int) {
    object Black : BackgroundColor(Color(0xff000000), Color(0xffffffff), 0)
    object White : BackgroundColor(Color(0xffffffff), Color(0xff000000), 1)
    object GrayishBlack : BackgroundColor(Color(0xff262626), Color(0xFFE9E9E9), 2)
    object CloudColor : BackgroundColor(Color(0xFF405A61), Color(0xFFFFFFFF), 3)
    object Gray88 : BackgroundColor(Color(248, 249, 250), Color(51, 51, 51), 4)
    object CornflowerBlue : BackgroundColor(Color(150, 173, 252), Color(0xff000000), 5)
    object NewCar : BackgroundColor(Color(219, 225, 241), Color(0xff000000), 6)
    object AntiqueWhite : BackgroundColor(Color(237, 221, 110), Color(0xff000000), 7)
    object LavenderRose : BackgroundColor(Color(168, 242, 154), Color(0xff000000), 8)
    object ArylideYellow : BackgroundColor(Color(233, 214, 107), Color(0xff000000), 9)
    object DesertSand : BackgroundColor(Color(237, 209, 176), Color(0xff000000), 10)
    object Lavender : BackgroundColor(Color(185, 135, 220), Color(0xff000000), 11)
    object PastelPink : BackgroundColor(Color(224, 166, 170), Color(0xff000000), 12)
    object PastelYellow : BackgroundColor(Color(248, 253, 137), Color(0xff000000), 13)
}
