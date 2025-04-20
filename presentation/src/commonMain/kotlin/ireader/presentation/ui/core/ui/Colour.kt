package ireader.presentation.ui.core.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ireader.presentation.ui.core.theme.isLight


object Colour {
    // Blues
    val blue_200 = Color(0xFF90CAF9)
    val blue_400 = Color(0xFF42A5F5)
    val blue_500 = Color(0xFF2196F3)
    val blue_600 = Color(0xFF1E88E5)
    val blue_700 = Color(0xFF1976D2)
    val blue_accent = Color(0xFF448AFF)
    
    // Light Blues
    val light_blue_a_200 = Color(0xFF40C4FF)
    val light_blue_a_400 = Color(0xFF00B0FF)
    
    // Reds
    val red_200 = Color(0xFFEF9A9A)
    val red_400 = Color(0xFFEF5350)
    val red_600 = Color(0xFFE53935)
    
    // Greens
    val green_500 = Color(0xFF4CAF50)
    val green_700 = Color(0xFF388E3C)
    
    // Neutrals
    val white_50 = Color(0xFFFFFFFF)
    val black_800 = Color(0xFF121212)
    val black_900 = Color(0xFF000000)
    
    // Transparent
    val transparent = Color(0x00FFFFFF)

    val ColorScheme.topBarColor
        @Composable
        get() = if (isLight()) white_50 else black_900

    val ColorScheme.contentColor
        @Composable
        get() = if (isLight()) black_900 else white_50
    val ColorScheme.Transparent
        @Composable
        get() = if (isLight()) black_900.copy(alpha = .1f) else white_50

    val ColorScheme.iconColor
        @Composable
        get() = if (isLight()) blue_500 else blue_400

    val ColorScheme.scrollingThumbColor
        @Composable
        get() = if (isLight()) blue_accent else blue_accent
}
