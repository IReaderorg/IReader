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
import ireader.presentation.ui.core.theme.themes.OceanBlueLightThemeColors
import ireader.presentation.ui.core.theme.themes.OceanBlueDarkThemeColors
import ireader.presentation.ui.core.theme.themes.SunsetOrangeLightThemeColors
import ireader.presentation.ui.core.theme.themes.SunsetOrangeDarkThemeColors
import ireader.presentation.ui.core.theme.themes.LavenderPurpleLightThemeColors
import ireader.presentation.ui.core.theme.themes.LavenderPurpleDarkThemeColors
import ireader.presentation.ui.core.theme.themes.ForestGreenLightThemeColors
import ireader.presentation.ui.core.theme.themes.ForestGreenDarkThemeColors
import ireader.presentation.ui.core.theme.themes.MonochromeMinimalLightThemeColors
import ireader.presentation.ui.core.theme.themes.MonochromeMinimalDarkThemeColors
import ireader.presentation.ui.core.theme.themes.CherryBlossomLightThemeColors
import ireader.presentation.ui.core.theme.themes.CherryBlossomDarkThemeColors
import ireader.presentation.ui.core.theme.themes.MidnightSkyLightThemeColors
import ireader.presentation.ui.core.theme.themes.MidnightSkyDarkThemeColors
import ireader.presentation.ui.core.theme.themes.AutumnHarvestLightThemeColors
import ireader.presentation.ui.core.theme.themes.AutumnHarvestDarkThemeColors
import ireader.presentation.ui.core.theme.themes.EmeraldForestLightThemeColors
import ireader.presentation.ui.core.theme.themes.EmeraldForestDarkThemeColors
import ireader.presentation.ui.core.theme.themes.RoseGoldLightThemeColors
import ireader.presentation.ui.core.theme.themes.RoseGoldDarkThemeColors
import ireader.presentation.ui.core.ui.Colour


val themes = listOf<Theme>(
    Theme(
        id = -1, 
        materialColors = LightThemeColorsTachiyomi, 
        extraColors = ExtraColors(
            bars = LightThemeColorsTachiyomi.surface,
            onBars = LightThemeColorsTachiyomi.onSurface
        ), 
        isDark = false
    ),
    Theme(
        id = -2, 
        materialColors = DarkThemeColorsTachiyomi, 
        extraColors = ExtraColors(
            bars = DarkThemeColorsTachiyomi.surface,
            onBars = DarkThemeColorsTachiyomi.onSurface
        ), 
        isDark = true
    ),
    Theme(
        id = -3, 
        materialColors = lightColorScheme(
            primary = Colour.blue_accent,
            primaryContainer = Colour.blue_200,
            onPrimaryContainer = Color(0xFF001D35),
            onPrimary = Colour.white_50,
            secondary = Colour.blue_accent,
            secondaryContainer = Colour.blue_200,
            onSecondaryContainer = Color(0xFF001D35),
            onSecondary = Colour.white_50,
            background = Colour.white_50,
            surface = Colour.white_50,
            surfaceVariant = Color.LightGray.copy(alpha = 0.3f),
            onSurfaceVariant = Colour.black_900,
            onBackground = Colour.black_900,
            onSurface = Colour.black_900,
            error = Colour.red_600,
            errorContainer = Colour.red_200,
            onErrorContainer = Color(0xFF410002),
            onError = Colour.white_50,
        ), 
        extraColors = ExtraColors(
            bars = Color.White,
            onBars = Color.Black
        ), 
        isDark = false
    ),
    Theme(
        id = -4, 
        materialColors = darkColorScheme(
            primary = Colour.blue_200,
            primaryContainer = Colour.blue_700,
            onPrimaryContainer = Colour.blue_200,
            onPrimary = Color(0xFF003258),
            secondary = Colour.blue_200,
            secondaryContainer = Colour.blue_700,
            onSecondaryContainer = Colour.blue_200,
            onSecondary = Color(0xFF003258),
            background = Color(0xFF121212),
            surface = Color(0xFF000000),
            surfaceVariant = Color.DarkGray.copy(alpha = 0.3f),
            onSurfaceVariant = Color.LightGray,
            onBackground = Colour.white_50,
            onSurface = Colour.white_50,
            error = Color(0xFFFFB4AB),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),
            onError = Color(0xFF690005),
        ), 
        extraColors = ExtraColors(
            bars = Color(0xFF181818),
            onBars = Color.White
        ), 
        isDark = true
    ),
    Theme(
        id = -5, 
        materialColors = MidNightLightColorScheme, 
        extraColors = ExtraColors(
            bars = MidNightLightColorScheme.surface,
            onBars = MidNightLightColorScheme.onSurface
        ), 
        isDark = false
    ),
    Theme(
        id = -6, 
        materialColors = MidNightDarkColorScheme, 
        extraColors = ExtraColors(
            bars = MidNightDarkColorScheme.surface,
            onBars = MidNightDarkColorScheme.onSurface
        ), 
        isDark = true
    ),
    Theme(
        id = -7, 
        materialColors = GreenAppleLightThemeColors, 
        extraColors = ExtraColors(
            bars = GreenAppleLightThemeColors.surface,
            onBars = GreenAppleLightThemeColors.onSurface
        ), 
        isDark = false
    ),
    Theme(
        id = -8, 
        materialColors = GreenAppleDarkThemeColors, 
        extraColors = ExtraColors(
            bars = GreenAppleDarkThemeColors.surface,
            onBars = GreenAppleDarkThemeColors.onSurface
        ), 
        isDark = true
    ),
    Theme(
        id = -9, 
        materialColors = LightThemeColorsStrawberries, 
        extraColors = ExtraColors(
            bars = LightThemeColorsStrawberries.surface,
            onBars = LightThemeColorsStrawberries.onSurface
        ), 
        isDark = false
    ),
    Theme(
        id = -10, 
        materialColors = DarkThemeColorsStrawberries, 
        extraColors = ExtraColors(
            bars = DarkThemeColorsStrawberries.surface,
            onBars = DarkThemeColorsStrawberries.onSurface
        ), 
        isDark = true
    ),
    Theme(
        id = -11, 
        materialColors = LightThemeColorsTako, 
        extraColors = ExtraColors(
            bars = LightThemeColorsTako.surface,
            onBars = LightThemeColorsTako.onSurface
        ), 
        isDark = false
    ),
    Theme(
        id = -12, 
        materialColors = DarkThemeColorsTako, 
        extraColors = ExtraColors(
            bars = DarkThemeColorsTako.surface,
            onBars = DarkThemeColorsTako.onSurface
        ), 
        isDark = true
    ),
    Theme(
        id = -13, 
        materialColors = OceanBlueLightThemeColors, 
        extraColors = ExtraColors(
            bars = OceanBlueLightThemeColors.surface,
            onBars = OceanBlueLightThemeColors.onSurface
        ), 
        isDark = false
    ),
    Theme(
        id = -14, 
        materialColors = OceanBlueDarkThemeColors, 
        extraColors = ExtraColors(
            bars = OceanBlueDarkThemeColors.surface,
            onBars = OceanBlueDarkThemeColors.onSurface
        ), 
        isDark = true
    ),
    Theme(
        id = -15, 
        materialColors = SunsetOrangeLightThemeColors, 
        extraColors = ExtraColors(
            bars = SunsetOrangeLightThemeColors.surface,
            onBars = SunsetOrangeLightThemeColors.onSurface
        ), 
        isDark = false
    ),
    Theme(
        id = -16, 
        materialColors = SunsetOrangeDarkThemeColors, 
        extraColors = ExtraColors(
            bars = SunsetOrangeDarkThemeColors.surface,
            onBars = SunsetOrangeDarkThemeColors.onSurface
        ), 
        isDark = true
    ),
    Theme(
        id = -17, 
        materialColors = LavenderPurpleLightThemeColors, 
        extraColors = ExtraColors(
            bars = LavenderPurpleLightThemeColors.surface,
            onBars = LavenderPurpleLightThemeColors.onSurface
        ), 
        isDark = false
    ),
    Theme(
        id = -18, 
        materialColors = LavenderPurpleDarkThemeColors, 
        extraColors = ExtraColors(
            bars = LavenderPurpleDarkThemeColors.surface,
            onBars = LavenderPurpleDarkThemeColors.onSurface
        ), 
        isDark = true
    ),
    Theme(
        id = -19, 
        materialColors = ForestGreenLightThemeColors, 
        extraColors = ExtraColors(
            bars = ForestGreenLightThemeColors.surface,
            onBars = ForestGreenLightThemeColors.onSurface
        ), 
        isDark = false
    ),
    Theme(
        id = -20, 
        materialColors = ForestGreenDarkThemeColors, 
        extraColors = ExtraColors(
            bars = ForestGreenDarkThemeColors.surface,
            onBars = ForestGreenDarkThemeColors.onSurface
        ), 
        isDark = true
    ),
    Theme(
        id = -21, 
        materialColors = MonochromeMinimalLightThemeColors, 
        extraColors = ExtraColors(
            bars = MonochromeMinimalLightThemeColors.surface,
            onBars = MonochromeMinimalLightThemeColors.onSurface
        ), 
        isDark = false
    ),
    Theme(
        id = -22, 
        materialColors = MonochromeMinimalDarkThemeColors, 
        extraColors = ExtraColors(
            bars = MonochromeMinimalDarkThemeColors.surface,
            onBars = MonochromeMinimalDarkThemeColors.onSurface
        ), 
        isDark = true
    ),
    Theme(
        id = -23, 
        materialColors = CherryBlossomLightThemeColors, 
        extraColors = ExtraColors(
            bars = CherryBlossomLightThemeColors.surface,
            onBars = CherryBlossomLightThemeColors.onSurface
        ), 
        isDark = false
    ),
    Theme(
        id = -24, 
        materialColors = CherryBlossomDarkThemeColors, 
        extraColors = ExtraColors(
            bars = CherryBlossomDarkThemeColors.surface,
            onBars = CherryBlossomDarkThemeColors.onSurface
        ), 
        isDark = true
    ),
    Theme(
        id = -25, 
        materialColors = MidnightSkyLightThemeColors, 
        extraColors = ExtraColors(
            bars = MidnightSkyLightThemeColors.surface,
            onBars = MidnightSkyLightThemeColors.onSurface
        ), 
        isDark = false
    ),
    Theme(
        id = -26, 
        materialColors = MidnightSkyDarkThemeColors, 
        extraColors = ExtraColors(
            bars = MidnightSkyDarkThemeColors.surface,
            onBars = MidnightSkyDarkThemeColors.onSurface
        ), 
        isDark = true
    ),
    Theme(
        id = -27, 
        materialColors = AutumnHarvestLightThemeColors, 
        extraColors = ExtraColors(
            bars = AutumnHarvestLightThemeColors.surface,
            onBars = AutumnHarvestLightThemeColors.onSurface
        ), 
        isDark = false
    ),
    Theme(
        id = -28, 
        materialColors = AutumnHarvestDarkThemeColors, 
        extraColors = ExtraColors(
            bars = AutumnHarvestDarkThemeColors.surface,
            onBars = AutumnHarvestDarkThemeColors.onSurface
        ), 
        isDark = true
    ),
    Theme(
        id = -29, 
        materialColors = EmeraldForestLightThemeColors, 
        extraColors = ExtraColors(
            bars = EmeraldForestLightThemeColors.surface,
            onBars = EmeraldForestLightThemeColors.onSurface
        ), 
        isDark = false
    ),
    Theme(
        id = -30, 
        materialColors = EmeraldForestDarkThemeColors, 
        extraColors = ExtraColors(
            bars = EmeraldForestDarkThemeColors.surface,
            onBars = EmeraldForestDarkThemeColors.onSurface
        ), 
        isDark = true
    ),
    Theme(
        id = -31, 
        materialColors = RoseGoldLightThemeColors, 
        extraColors = ExtraColors(
            bars = RoseGoldLightThemeColors.surface,
            onBars = RoseGoldLightThemeColors.onSurface
        ), 
        isDark = false
    ),
    Theme(
        id = -32, 
        materialColors = RoseGoldDarkThemeColors, 
        extraColors = ExtraColors(
            bars = RoseGoldDarkThemeColors.surface,
            onBars = RoseGoldDarkThemeColors.onSurface
        ), 
        isDark = true
    ),
).toMutableList()
