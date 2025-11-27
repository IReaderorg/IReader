package ireader.presentation.ui.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import ireader.domain.models.theme.ExtraColors
import ireader.domain.models.theme.Theme
import ireader.domain.models.common.DomainColor
import ireader.presentation.core.toDomainColor
import ireader.presentation.core.toDomainColorScheme
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
        materialColors = LightThemeColorsTachiyomi.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = LightThemeColorsTachiyomi.surface.toDomainColor(),
            onBars = LightThemeColorsTachiyomi.onSurface.toDomainColor()
        ), 
        isDark = false
    ),
    Theme(
        id = -2, 
        materialColors = DarkThemeColorsTachiyomi.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = DarkThemeColorsTachiyomi.surface.toDomainColor(),
            onBars = DarkThemeColorsTachiyomi.onSurface.toDomainColor()
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
        ).toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = Color.White.toDomainColor(),
            onBars = Color.Black.toDomainColor()
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
        ).toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = Color(0xFF181818).toDomainColor(),
            onBars = Color.White.toDomainColor()
        ), 
        isDark = true
    ),
    Theme(
        id = -5, 
        materialColors = MidNightLightColorScheme.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = MidNightLightColorScheme.surface.toDomainColor(),
            onBars = MidNightLightColorScheme.onSurface.toDomainColor()
        ), 
        isDark = false
    ),
    Theme(
        id = -6, 
        materialColors = MidNightDarkColorScheme.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = MidNightDarkColorScheme.surface.toDomainColor(),
            onBars = MidNightDarkColorScheme.onSurface.toDomainColor()
        ), 
        isDark = true
    ),
    Theme(
        id = -7, 
        materialColors = GreenAppleLightThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = GreenAppleLightThemeColors.surface.toDomainColor(),
            onBars = GreenAppleLightThemeColors.onSurface.toDomainColor()
        ), 
        isDark = false
    ),
    Theme(
        id = -8, 
        materialColors = GreenAppleDarkThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = GreenAppleDarkThemeColors.surface.toDomainColor(),
            onBars = GreenAppleDarkThemeColors.onSurface.toDomainColor()
        ), 
        isDark = true
    ),
    Theme(
        id = -9, 
        materialColors = LightThemeColorsStrawberries.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = LightThemeColorsStrawberries.surface.toDomainColor(),
            onBars = LightThemeColorsStrawberries.onSurface.toDomainColor()
        ), 
        isDark = false
    ),
    Theme(
        id = -10, 
        materialColors = DarkThemeColorsStrawberries.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = DarkThemeColorsStrawberries.surface.toDomainColor(),
            onBars = DarkThemeColorsStrawberries.onSurface.toDomainColor()
        ), 
        isDark = true
    ),
    Theme(
        id = -11, 
        materialColors = LightThemeColorsTako.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = LightThemeColorsTako.surface.toDomainColor(),
            onBars = LightThemeColorsTako.onSurface.toDomainColor()
        ), 
        isDark = false
    ),
    Theme(
        id = -12, 
        materialColors = DarkThemeColorsTako.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = DarkThemeColorsTako.surface.toDomainColor(),
            onBars = DarkThemeColorsTako.onSurface.toDomainColor()
        ), 
        isDark = true
    ),
    Theme(
        id = -13, 
        materialColors = OceanBlueLightThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = OceanBlueLightThemeColors.surface.toDomainColor(),
            onBars = OceanBlueLightThemeColors.onSurface.toDomainColor()
        ), 
        isDark = false
    ),
    Theme(
        id = -14, 
        materialColors = OceanBlueDarkThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = OceanBlueDarkThemeColors.surface.toDomainColor(),
            onBars = OceanBlueDarkThemeColors.onSurface.toDomainColor()
        ), 
        isDark = true
    ),
    Theme(
        id = -15, 
        materialColors = SunsetOrangeLightThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = SunsetOrangeLightThemeColors.surface.toDomainColor(),
            onBars = SunsetOrangeLightThemeColors.onSurface.toDomainColor()
        ), 
        isDark = false
    ),
    Theme(
        id = -16, 
        materialColors = SunsetOrangeDarkThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = SunsetOrangeDarkThemeColors.surface.toDomainColor(),
            onBars = SunsetOrangeDarkThemeColors.onSurface.toDomainColor()
        ), 
        isDark = true
    ),
    Theme(
        id = -17, 
        materialColors = LavenderPurpleLightThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = LavenderPurpleLightThemeColors.surface.toDomainColor(),
            onBars = LavenderPurpleLightThemeColors.onSurface.toDomainColor()
        ), 
        isDark = false
    ),
    Theme(
        id = -18, 
        materialColors = LavenderPurpleDarkThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = LavenderPurpleDarkThemeColors.surface.toDomainColor(),
            onBars = LavenderPurpleDarkThemeColors.onSurface.toDomainColor()
        ), 
        isDark = true
    ),
    Theme(
        id = -19, 
        materialColors = ForestGreenLightThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = ForestGreenLightThemeColors.surface.toDomainColor(),
            onBars = ForestGreenLightThemeColors.onSurface.toDomainColor()
        ), 
        isDark = false
    ),
    Theme(
        id = -20, 
        materialColors = ForestGreenDarkThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = ForestGreenDarkThemeColors.surface.toDomainColor(),
            onBars = ForestGreenDarkThemeColors.onSurface.toDomainColor()
        ), 
        isDark = true
    ),
    Theme(
        id = -21, 
        materialColors = MonochromeMinimalLightThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = MonochromeMinimalLightThemeColors.surface.toDomainColor(),
            onBars = MonochromeMinimalLightThemeColors.onSurface.toDomainColor()
        ), 
        isDark = false
    ),
    Theme(
        id = -22, 
        materialColors = MonochromeMinimalDarkThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = MonochromeMinimalDarkThemeColors.surface.toDomainColor(),
            onBars = MonochromeMinimalDarkThemeColors.onSurface.toDomainColor()
        ), 
        isDark = true
    ),
    Theme(
        id = -23, 
        materialColors = CherryBlossomLightThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = CherryBlossomLightThemeColors.surface.toDomainColor(),
            onBars = CherryBlossomLightThemeColors.onSurface.toDomainColor()
        ), 
        isDark = false
    ),
    Theme(
        id = -24, 
        materialColors = CherryBlossomDarkThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = CherryBlossomDarkThemeColors.surface.toDomainColor(),
            onBars = CherryBlossomDarkThemeColors.onSurface.toDomainColor()
        ), 
        isDark = true
    ),
    Theme(
        id = -25, 
        materialColors = MidnightSkyLightThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = MidnightSkyLightThemeColors.surface.toDomainColor(),
            onBars = MidnightSkyLightThemeColors.onSurface.toDomainColor()
        ), 
        isDark = false
    ),
    Theme(
        id = -26, 
        materialColors = MidnightSkyDarkThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = MidnightSkyDarkThemeColors.surface.toDomainColor(),
            onBars = MidnightSkyDarkThemeColors.onSurface.toDomainColor()
        ), 
        isDark = true
    ),
    Theme(
        id = -27, 
        materialColors = AutumnHarvestLightThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = AutumnHarvestLightThemeColors.surface.toDomainColor(),
            onBars = AutumnHarvestLightThemeColors.onSurface.toDomainColor()
        ), 
        isDark = false
    ),
    Theme(
        id = -28, 
        materialColors = AutumnHarvestDarkThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = AutumnHarvestDarkThemeColors.surface.toDomainColor(),
            onBars = AutumnHarvestDarkThemeColors.onSurface.toDomainColor()
        ), 
        isDark = true
    ),
    Theme(
        id = -29, 
        materialColors = EmeraldForestLightThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = EmeraldForestLightThemeColors.surface.toDomainColor(),
            onBars = EmeraldForestLightThemeColors.onSurface.toDomainColor()
        ), 
        isDark = false
    ),
    Theme(
        id = -30, 
        materialColors = EmeraldForestDarkThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = EmeraldForestDarkThemeColors.surface.toDomainColor(),
            onBars = EmeraldForestDarkThemeColors.onSurface.toDomainColor()
        ), 
        isDark = true
    ),
    Theme(
        id = -31, 
        materialColors = RoseGoldLightThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = RoseGoldLightThemeColors.surface.toDomainColor(),
            onBars = RoseGoldLightThemeColors.onSurface.toDomainColor()
        ), 
        isDark = false
    ),
    Theme(
        id = -32, 
        materialColors = RoseGoldDarkThemeColors.toDomainColorScheme(),
        extraColors = ExtraColors(
            bars = RoseGoldDarkThemeColors.surface.toDomainColor(),
            onBars = RoseGoldDarkThemeColors.onSurface.toDomainColor()
        ), 
        isDark = true
    ),
).toMutableList()
