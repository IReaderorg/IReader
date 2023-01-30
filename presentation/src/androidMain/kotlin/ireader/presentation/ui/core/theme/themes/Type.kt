//package ireader.ui.core.theme.themes
//
//import androidx.compose.material3.Typography
//import androidx.compose.ui.text.ExperimentalTextApi
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.googlefonts.Font
//import androidx.compose.ui.text.googlefonts.GoogleFont
//import androidx.compose.ui.unit.sp
//import ireader.presentation.ui.core.R
//
//fun createTypography(fontFamily: FontFamily): Typography {
//    return androidx.compose.material3.Typography(
//        displayLarge = TextStyle(
//            fontFamily = fontFamily,
//            fontWeight = FontWeight.W400,
//            fontSize = 57.sp,
//            lineHeight = 64.sp,
//            letterSpacing = (-0.25).sp,
//        ),
//        displayMedium = TextStyle(
//            fontFamily = fontFamily,
//            fontWeight = FontWeight.W400,
//            fontSize = 45.sp,
//            lineHeight = 52.sp,
//            letterSpacing = 0.sp,
//        ),
//        displaySmall = TextStyle(
//            fontFamily = fontFamily,
//            fontWeight = FontWeight.W400,
//            fontSize = 36.sp,
//            lineHeight = 44.sp,
//            letterSpacing = 0.sp,
//        ),
//        headlineLarge = TextStyle(
//            fontFamily = fontFamily,
//            fontWeight = FontWeight.W400,
//            fontSize = 32.sp,
//            lineHeight = 40.sp,
//            letterSpacing = 0.sp,
//        ),
//        headlineMedium = TextStyle(
//            fontFamily = fontFamily,
//            fontWeight = FontWeight.W400,
//            fontSize = 28.sp,
//            lineHeight = 36.sp,
//            letterSpacing = 0.sp,
//        ),
//        headlineSmall = TextStyle(
//            fontFamily = fontFamily,
//            fontWeight = FontWeight.W400,
//            fontSize = 24.sp,
//            lineHeight = 32.sp,
//            letterSpacing = 0.sp,
//        ),
//        titleLarge = TextStyle(
//            fontFamily = fontFamily,
//            fontWeight = FontWeight.W400,
//            fontSize = 22.sp,
//            lineHeight = 28.sp,
//            letterSpacing = 0.sp,
//        ),
//        titleMedium = TextStyle(
//            fontFamily = fontFamily,
//            fontWeight = FontWeight.Medium,
//            fontSize = 16.sp,
//            lineHeight = 24.sp,
//            letterSpacing = 0.1.sp,
//        ),
//        titleSmall = TextStyle(
//            fontFamily = fontFamily,
//            fontWeight = FontWeight.Medium,
//            fontSize = 14.sp,
//            lineHeight = 20.sp,
//            letterSpacing = 0.1.sp,
//        ),
//        labelLarge = TextStyle(
//            fontFamily = fontFamily,
//            fontWeight = FontWeight.Medium,
//            fontSize = 14.sp,
//            lineHeight = 20.sp,
//            letterSpacing = 0.1.sp,
//        ),
//        bodyLarge = TextStyle(
//            fontFamily = fontFamily,
//            fontWeight = FontWeight.W400,
//            fontSize = 16.sp,
//            lineHeight = 24.sp,
//            letterSpacing = 0.5.sp,
//        ),
//        bodyMedium = TextStyle(
//            fontFamily = fontFamily,
//            fontWeight = FontWeight.W400,
//            fontSize = 14.sp,
//            lineHeight = 20.sp,
//            letterSpacing = 0.25.sp,
//        ),
//        bodySmall = TextStyle(
//            fontFamily = fontFamily,
//            fontWeight = FontWeight.W400,
//            fontSize = 12.sp,
//            lineHeight = 16.sp,
//            letterSpacing = 0.4.sp,
//        ),
//        labelMedium = TextStyle(
//            fontFamily = fontFamily,
//            fontWeight = FontWeight.Medium,
//            fontSize = 12.sp,
//            lineHeight = 16.sp,
//            letterSpacing = 0.5.sp,
//        ),
//        labelSmall = TextStyle(
//            fontFamily = fontFamily,
//            fontWeight = FontWeight.Medium,
//            fontSize = 11.sp,
//            lineHeight = 16.sp,
//            letterSpacing = 0.5.sp,
//        )
//    )
//}
//
//@ExperimentalTextApi
//fun createSingleGoogleFontFamily(
//    name: String,
//    provider: GoogleFont.Provider = GmsFontProvider,
//    weights: List<FontWeight>,
//): FontFamily = FontFamily(
//    weights.map { weight ->
//        Font(
//            googleFont = GoogleFont(name),
//            fontProvider = provider,
//            weight = weight,
//        )
//    }
//)
//
//@ExperimentalTextApi
//internal val GmsFontProvider: GoogleFont.Provider by lazy {
//    GoogleFont.Provider(
//        providerAuthority = "com.google.android.gms.fonts",
//        providerPackage = "com.google.android.gms",
//        certificates = R.array.com_google_android_gms_fonts_certs,
//    )
//}
