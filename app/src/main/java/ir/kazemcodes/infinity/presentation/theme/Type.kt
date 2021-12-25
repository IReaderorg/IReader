package ir.kazemcodes.infinity.presentation.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.domain.models.FontType

// Set of Material typography styles to start with
val poppins = FontFamily(
    Font(R.font.poppin_semi_bold)
)
val sourceSansPro = FontFamily(
    Font(R.font.source_sans_pro_w200_extra_light, weight = FontWeight.ExtraLight),

    Font(R.font.source_sans_pro_lignt, weight = FontWeight.Light),
    Font(R.font.source_sans_pro_resgular, weight = FontWeight.Normal),

    Font(R.font.source_sans_pro_semi_bold, weight = FontWeight.SemiBold),

    Font(R.font.source_sans_pro_bold_700, weight = FontWeight.Bold),


    Font(R.font.source_sans_pro_900, weight = FontWeight.ExtraBold),
)

// Set of Material typography styles to start with
val Typography = Typography(
    defaultFontFamily = sourceSansPro,
)
val fonts = listOf<FontType>(
    FontType.Poppins,
    FontType.SourceSansPro,
)