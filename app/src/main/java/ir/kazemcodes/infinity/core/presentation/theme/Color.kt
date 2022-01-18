package ir.kazemcodes.infinity.core.presentation.theme

import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

//val Purple200 = Color(0xFFBB86FC)
//val Purple500 = Color(0xFF6200EE)
//val Purple700 = Color(0xFF3700B3)
//val Teal200 = Color(0xFF03DAC5)

object Colour {
    val blue_200 = Color(0xFF90CAF9)
    val blue_500 = Color(0xFF2196f3)
    val blue_600 = Color(0xFF2196f3)
    val blue_700 = Color(0xFF2196f3)


    val light_blue_a_200 = Color(0xFF40c4ff)

    val light_blue_a_400 = Color(0xFF00b0ff)


    val red_200 = Color(0xFFcf6679)

    val red_600 = Color(0xFFb00020)

    val white_50 = Color(0xFFffffff)

    val black_800 = Color(0xFF121212)

    val black_900 = Color(0xFF000000)

    val transparent = Color(0x00FFFFFF)

    val Colors.topBarColor
        @Composable
        get() = if (isLight) white_50 else black_900

    val Colors.contentColor
        @Composable
        get() = if (isLight) black_900 else white_50
    val Colors.Transparent
        @Composable
        get() = if (isLight) black_900.copy(alpha = .1f) else white_50

    val Colors.iconColor
        @Composable
        get() = if (isLight) blue_500 else blue_500


}