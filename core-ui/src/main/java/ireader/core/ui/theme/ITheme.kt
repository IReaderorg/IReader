package ireader.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import ireader.common.models.theme.ExtraColors
import ireader.domain.models.theme.Theme
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


val themes = listOf<Theme>(
    Theme(
        id = -1, materialColors = LightThemeColorsTachiyomi, extraColors = ExtraColors(
            bars = LightThemeColorsTachiyomi.surface,
            onBars = LightThemeColorsTachiyomi.onSurface
        ), isDark = false
    ),
    Theme(
        id = -2, materialColors = DarkThemeColorsTachiyomi, extraColors = ExtraColors(
            bars = DarkThemeColorsTachiyomi.surface,
            onBars = DarkThemeColorsTachiyomi.onSurface
        ), isDark = true
    ),
    Theme(id =-3, materialColors =lightColorScheme(
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
    ), extraColors =ExtraColors(
        bars = Color.White,
        onBars = Color.Black
    ), isDark = false),
    Theme(id =-4, materialColors =darkColorScheme(
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
    ), extraColors =ExtraColors(
        bars = Color(0xFF181818),
        onBars = Color.White
    ), isDark = true),
    Theme(id =-5, materialColors =MidNightLightColorScheme, extraColors =ExtraColors(
        bars = MidNightLightColorScheme.surface,
        onBars = MidNightLightColorScheme.onSurface
    ), isDark = false),
    Theme(id =-6, materialColors =MidNightDarkColorScheme, extraColors =ExtraColors(
        bars = MidNightDarkColorScheme.surface,
        onBars = MidNightDarkColorScheme.onSurface
    ), isDark = true),
    Theme(id =-7, materialColors =GreenAppleLightThemeColors, extraColors = ExtraColors(
        bars = GreenAppleLightThemeColors.surface,
        onBars = GreenAppleLightThemeColors.onSurface
    ), isDark = false),
    Theme(id =-8, materialColors =GreenAppleDarkThemeColors, extraColors =ExtraColors(
        bars = GreenAppleDarkThemeColors.surface,
        onBars = GreenAppleDarkThemeColors.onSurface
    ), isDark = true),
    Theme(id =-9, materialColors =LightThemeColorsStrawberries, extraColors =ExtraColors(
        bars = LightThemeColorsStrawberries.surface,
        onBars = LightThemeColorsStrawberries.onSurface
    ), isDark = false),
    Theme(id =-10, materialColors =DarkThemeColorsStrawberries, extraColors =ExtraColors(
        bars = DarkThemeColorsStrawberries.surface,
        onBars = DarkThemeColorsStrawberries.onSurface
    ), isDark = true),
    Theme(id =-11, materialColors =LightThemeColorsTako, extraColors =ExtraColors(
        bars = LightThemeColorsTako.surface,
        onBars = LightThemeColorsTako.onSurface
    ), isDark = false),
    Theme(id =-12, materialColors =DarkThemeColorsTako, extraColors =ExtraColors(
        bars = DarkThemeColorsTako.surface,
        onBars = DarkThemeColorsTako.onSurface
    ), isDark = true),
).toMutableList()
