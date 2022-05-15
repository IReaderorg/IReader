package org.ireader.core_ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import org.ireader.core_ui.theme.themes.DarkThemeColorsStrawberries
import org.ireader.core_ui.theme.themes.DarkThemeColorsTachiyomi
import org.ireader.core_ui.theme.themes.DarkThemeColorsTako
import org.ireader.core_ui.theme.themes.GreenAppleDarkThemeColors
import org.ireader.core_ui.theme.themes.GreenAppleLightThemeColors
import org.ireader.core_ui.theme.themes.LightThemeColorsStrawberries
import org.ireader.core_ui.theme.themes.LightThemeColorsTachiyomi
import org.ireader.core_ui.theme.themes.LightThemeColorsTako
import org.ireader.core_ui.theme.themes.MidNightDarkColorScheme
import org.ireader.core_ui.theme.themes.MidNightLightColorScheme
import org.ireader.core_ui.ui.Colour

data class Theme(
    val id: Int,
    val materialColors: ColorScheme,
    val extraColors: ExtraColors,
)
data class BaseTheme(
    val id: Int,
    val lightColor: ColorScheme,
    val darkColor: ColorScheme,
    val lightExtraColors: ExtraColors,
    val darkExtraColors: ExtraColors,
)

fun BaseTheme.light():Theme {
    return Theme(
        id,
        lightColor,
        lightExtraColors
    )
}
fun BaseTheme.dark():Theme {
    return Theme(
        id,
        darkColor,
        darkExtraColors
    )
}



val themes = listOf<BaseTheme>(
    BaseTheme(
        id = 1,
        lightColor = LightThemeColorsTachiyomi,
        darkColor = DarkThemeColorsTachiyomi,
        darkExtraColors =ExtraColors(
            bars = DarkThemeColorsTachiyomi.surface,
            onBars = DarkThemeColorsTachiyomi.onSurface
        ),
        lightExtraColors = ExtraColors(
            bars = LightThemeColorsTachiyomi.surface,
            onBars = LightThemeColorsTachiyomi.onSurface
        )
    ),
    BaseTheme(
        id = 2,
        lightColor = lightColorScheme(
            primary = Colour.blue_accent,
            primaryContainer = Colour.blue_700,
            onPrimary = Colour.white_50,
            secondary = Colour.blue_accent,
            onSecondary = Colour.white_50,
            background = Colour.white_50,
            surface = Colour.white_50,
            onBackground = Colour.black_900,
            onSurface = Colour.black_900,
            error = Colour.red_600,
            onError = Colour.white_50,
        ),
        darkColor = darkColorScheme(
            primary = Colour.blue_accent,
            primaryContainer = Colour.blue_600,
            onPrimary = Colour.black_900,
            secondary = Colour.blue_accent,
            onSecondary = Colour.black_900,
            background = Color(0xFF121212),
            surface = Color(0xFF000000),
            onBackground = Colour.white_50,
            onSurface = Colour.white_50,
            error = Colour.red_600,
            onError = Colour.black_900,
        ),
        lightExtraColors = ExtraColors(
            bars = Color.White,
            onBars = Color.Black
        ),
        darkExtraColors = ExtraColors(
            bars = Color(0xFF181818),
            onBars = Color.White
        ),

    ),
    BaseTheme(
        id = 3,
        lightColor = MidNightLightColorScheme,
        darkColor = MidNightDarkColorScheme,
        darkExtraColors =ExtraColors(
            bars = MidNightDarkColorScheme.surface,
            onBars = MidNightDarkColorScheme.onSurface
        ),
        lightExtraColors = ExtraColors(
            bars = MidNightLightColorScheme.surface,
            onBars = MidNightLightColorScheme.onSurface
        )
    ),
    BaseTheme(
        id = 4,
        lightColor = GreenAppleLightThemeColors,
        darkColor = GreenAppleDarkThemeColors,
        darkExtraColors =ExtraColors(
            bars = GreenAppleDarkThemeColors.surface,
            onBars = GreenAppleDarkThemeColors.onSurface
        ),
        lightExtraColors = ExtraColors(
            bars = GreenAppleLightThemeColors.surface,
            onBars = GreenAppleLightThemeColors.onSurface
        )
    ),
    BaseTheme(
        id = 5,
        lightColor = LightThemeColorsStrawberries,
        darkColor = DarkThemeColorsStrawberries,
        darkExtraColors =ExtraColors(
            bars = DarkThemeColorsStrawberries.surface,
            onBars = DarkThemeColorsStrawberries.onSurface
        ),
        lightExtraColors = ExtraColors(
            bars = LightThemeColorsStrawberries.surface,
            onBars = LightThemeColorsStrawberries.onSurface
        )
    ),
    BaseTheme(
        id = 6,
        lightColor = LightThemeColorsTako,
        darkColor = DarkThemeColorsTako,
        darkExtraColors =ExtraColors(
            bars = DarkThemeColorsTako.surface,
            onBars = DarkThemeColorsTako.onSurface
        ),
        lightExtraColors = ExtraColors(
            bars = LightThemeColorsTako.surface,
            onBars = LightThemeColorsTako.onSurface
        )
    ),


)
