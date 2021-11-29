package ir.kazemcodes.infinity.base_feature.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ir.kazemcodes.infinity.R

// Set of Material typography styles to start with
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    )
)
val poppins = FontFamily(
    Font(R.font.poppin_semi_bold)
)
val sourceSansPro = FontFamily(
    Font(R.font.source_sans_pro_resgular),
)