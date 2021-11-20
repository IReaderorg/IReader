package ir.kazemcodes.infinity.base_feature.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = Colour.blue_200,
    primaryVariant = Colour.blue_600,
    onPrimary = Colour.black_900,
    secondary = Colour.light_blue_a_200,
    onSecondary = Colour.black_900,
    background = Colour.black_900,
    surface = Colour.white_50,
    onBackground = Colour.white_50,
    onSurface = Colour.white_50,
)

private val LightColorPalette = lightColors(
    primary = Colour.blue_500,
    primaryVariant = Colour.blue_700,
    onPrimary = Colour.white_50,
    secondary = Colour.light_blue_a_200,
    onSecondary = Colour.black_900,
    background = Colour.white_50,
    surface = Colour.white_50,
    onBackground = Colour.black_900,
    onSurface = Colour.black_900,
)

@Composable
fun InfinityTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}