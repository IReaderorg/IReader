package ireader.presentation.ui.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import ireader.domain.models.theme.ExtraColors
import ireader.domain.models.theme.Theme
import ireader.presentation.ui.core.theme.themes.DarkThemeColorsStrawberries
import ireader.presentation.ui.core.theme.themes.DarkThemeColorsTachiyomi
import ireader.presentation.ui.core.theme.themes.DarkThemeColorsTako
import ireader.presentation.ui.core.theme.themes.GreenAppleDarkThemeColors
import ireader.presentation.ui.core.theme.themes.GreenAppleLightThemeColors
import ireader.presentation.ui.core.theme.themes.LightThemeColorsStrawberries
import ireader.presentation.ui.core.theme.themes.LightThemeColorsTachiyomi
import ireader.presentation.ui.core.theme.themes.LightThemeColorsTako
import ireader.presentation.ui.core.theme.themes.MidNightDarkColorScheme
import ireader.presentation.ui.core.theme.themes.MidNightLightColorScheme
import ireader.presentation.ui.core.ui.Colour


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
        onPrimaryContainer = Colour.white_50,
        onPrimary = Colour.white_50,
        secondary = Colour.blue_accent,
        secondaryContainer = Colour.blue_600,
        onSecondaryContainer = Colour.white_50,
        onSecondary = Colour.white_50,
        background = Colour.white_50,
        surface = Colour.white_50,
        surfaceVariant = Color.LightGray.copy(alpha = 0.3f),
        onSurfaceVariant = Colour.black_900,
        onBackground = Colour.black_900,
        onSurface = Colour.black_900,
        error = Colour.red_600,
        errorContainer = Colour.red_200,
        onErrorContainer = Colour.black_900,
        onError = Colour.white_50,
    ), extraColors = ExtraColors(
        bars = Color.White,
        onBars = Color.Black
    ), isDark = false),
    Theme(id =-4, materialColors =darkColorScheme(
        primary = Colour.blue_accent,
        primaryContainer = Colour.blue_700,
        onPrimaryContainer = Colour.white_50,
        onPrimary = Colour.white_50,
        secondary = Colour.blue_accent,
        secondaryContainer = Colour.blue_700,
        onSecondaryContainer = Colour.white_50,
        onSecondary = Colour.white_50,
        background = Color(0xFF121212),
        surface = Color(0xFF000000),
        surfaceVariant = Color.DarkGray.copy(alpha = 0.3f),
        onSurfaceVariant = Color.LightGray,
        onBackground = Colour.white_50,
        onSurface = Colour.white_50,
        error = Colour.red_600,
        errorContainer = Colour.red_200,
        onErrorContainer = Colour.black_900,
        onError = Colour.white_50,
    ), extraColors = ExtraColors(
        bars = Color(0xFF181818),
        onBars = Color.White
    ), isDark = true),
    Theme(id =-5, materialColors =MidNightLightColorScheme, extraColors = ExtraColors(
        bars = MidNightLightColorScheme.surface,
        onBars = MidNightLightColorScheme.onSurface
    ), isDark = false),
    Theme(id =-6, materialColors =MidNightDarkColorScheme, extraColors = ExtraColors(
        bars = MidNightDarkColorScheme.surface,
        onBars = MidNightDarkColorScheme.onSurface
    ), isDark = true),
    Theme(id =-7, materialColors =GreenAppleLightThemeColors, extraColors = ExtraColors(
        bars = GreenAppleLightThemeColors.surface,
        onBars = GreenAppleLightThemeColors.onSurface
    ), isDark = false),
    Theme(id =-8, materialColors =GreenAppleDarkThemeColors, extraColors = ExtraColors(
        bars = GreenAppleDarkThemeColors.surface,
        onBars = GreenAppleDarkThemeColors.onSurface
    ), isDark = true),
    Theme(id =-9, materialColors =LightThemeColorsStrawberries, extraColors = ExtraColors(
        bars = LightThemeColorsStrawberries.surface,
        onBars = LightThemeColorsStrawberries.onSurface
    ), isDark = false),
    Theme(id =-10, materialColors =DarkThemeColorsStrawberries, extraColors = ExtraColors(
        bars = DarkThemeColorsStrawberries.surface,
        onBars = DarkThemeColorsStrawberries.onSurface
    ), isDark = true),
    Theme(id =-11, materialColors =LightThemeColorsTako, extraColors = ExtraColors(
        bars = LightThemeColorsTako.surface,
        onBars = LightThemeColorsTako.onSurface
    ), isDark = false),
    Theme(id =-12, materialColors =DarkThemeColorsTako, extraColors = ExtraColors(
        bars = DarkThemeColorsTako.surface,
        onBars = DarkThemeColorsTako.onSurface
    ), isDark = true),
).toMutableList()
