package ireader.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import ireader.common.models.theme.BaseTheme
import ireader.common.models.theme.ExtraColors
import ireader.common.models.theme.Theme
import ireader.core.ui.theme.themes.DarkThemeColorsStrawberries
import ireader.core.ui.theme.themes.DarkThemeColorsTachiyomi
import ireader.core.ui.theme.themes.DarkThemeColorsTako
import ireader.core.ui.theme.themes.GreenAppleDarkThemeColors
import ireader.core.ui.theme.themes.GreenAppleLightThemeColors
import ireader.core.ui.theme.themes.LightThemeColorsStrawberries
import ireader.core.ui.theme.themes.LightThemeColorsTachiyomi
import ireader.core.ui.theme.themes.LightThemeColorsTako
import ireader.core.ui.theme.themes.MidNightDarkColorScheme
import ireader.core.ui.theme.themes.MidNightLightColorScheme
import ireader.core.ui.ui.Colour

fun BaseTheme.light(): Theme {
    return Theme(
        id,
        lightColor,
        lightExtraColors,
        default = this.default
    )
}

fun BaseTheme.dark(): Theme {
    return Theme(
        id,
        darkColor,
        darkExtraColors,
        default = this.default
    )
}

val themes = listOf<BaseTheme>(
    BaseTheme(
        id = -1,
        lightColor = LightThemeColorsTachiyomi,
        darkColor = DarkThemeColorsTachiyomi,
        darkExtraColors = ExtraColors(
            bars = DarkThemeColorsTachiyomi.surface,
            onBars = DarkThemeColorsTachiyomi.onSurface
        ),
        lightExtraColors = ExtraColors(
            bars = LightThemeColorsTachiyomi.surface,
            onBars = LightThemeColorsTachiyomi.onSurface
        ),
        default = true
    ),
    BaseTheme(
        id = -2,
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
        default = true

    ),
    BaseTheme(
        id = -3,
        lightColor = MidNightLightColorScheme,
        darkColor = MidNightDarkColorScheme,
        darkExtraColors = ExtraColors(
            bars = MidNightDarkColorScheme.surface,
            onBars = MidNightDarkColorScheme.onSurface
        ),
        lightExtraColors = ExtraColors(
            bars = MidNightLightColorScheme.surface,
            onBars = MidNightLightColorScheme.onSurface
        ),
        default = true
    ),
    BaseTheme(
        id = -4,
        lightColor = GreenAppleLightThemeColors,
        darkColor = GreenAppleDarkThemeColors,
        darkExtraColors = ExtraColors(
            bars = GreenAppleDarkThemeColors.surface,
            onBars = GreenAppleDarkThemeColors.onSurface
        ),
        lightExtraColors = ExtraColors(
            bars = GreenAppleLightThemeColors.surface,
            onBars = GreenAppleLightThemeColors.onSurface
        ),
        default = true
    ),
    BaseTheme(
        id = -5,
        lightColor = LightThemeColorsStrawberries,
        darkColor = DarkThemeColorsStrawberries,
        darkExtraColors = ExtraColors(
            bars = DarkThemeColorsStrawberries.surface,
            onBars = DarkThemeColorsStrawberries.onSurface
        ),
        lightExtraColors = ExtraColors(
            bars = LightThemeColorsStrawberries.surface,
            onBars = LightThemeColorsStrawberries.onSurface
        ),
        default = true
    ),
    BaseTheme(
        id = -6,
        lightColor = LightThemeColorsTako,
        darkColor = DarkThemeColorsTako,
        darkExtraColors = ExtraColors(
            bars = DarkThemeColorsTako.surface,
            onBars = DarkThemeColorsTako.onSurface
        ),
        lightExtraColors = ExtraColors(
            bars = LightThemeColorsTako.surface,
            onBars = LightThemeColorsTako.onSurface
        ),
        default = true
    ),
).toMutableList()
