package ireader.presentation.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * IReader Material Design 3 theme system following Mihon's patterns.
 * Provides comprehensive theming with color schemes, typography, and AMOLED support.
 */

enum class AppTheme {
    DEFAULT,
    MONET,
    GREEN_APPLE,
    STRAWBERRY,
    TAKO,
    TACHIYOMI,
    MIDNIGHT,
    OCEAN_BLUE,
    SUNSET_ORANGE,
    LAVENDER_PURPLE,
    FOREST_GREEN,
    MONOCHROME_MINIMAL,
    CHERRY_BLOSSOM,
    MIDNIGHT_SKY,
    AUTUMN_HARVEST,
    EMERALD_FOREST,
    ROSE_GOLD
}

@Composable
fun IReaderTheme(
    appTheme: AppTheme = AppTheme.DEFAULT,
    amoled: Boolean = false,
    content: @Composable () -> Unit,
) {
    BaseIReaderTheme(
        appTheme = appTheme,
        isAmoled = amoled,
        content = content,
    )
}

@Composable
private fun BaseIReaderTheme(
    appTheme: AppTheme,
    isAmoled: Boolean,
    content: @Composable () -> Unit,
) {
    val paddingValues = PaddingValues()
    
    androidx.compose.runtime.CompositionLocalProvider(
        LocalPaddingValues provides paddingValues
    ) {
        MaterialTheme(
            colorScheme = getThemeColorScheme(appTheme, isAmoled),
            typography = IReaderTypography,
            shapes = IReaderShapes,
            content = content,
        )
    }
}

@Composable
@ReadOnlyComposable
private fun getThemeColorScheme(
    appTheme: AppTheme,
    isAmoled: Boolean,
): ColorScheme {
    val colorScheme = when (appTheme) {
        AppTheme.DEFAULT -> if (isSystemInDarkTheme()) IReaderDarkColorScheme else IReaderLightColorScheme
        AppTheme.MONET -> if (isSystemInDarkTheme()) IReaderDarkColorScheme else IReaderLightColorScheme // TODO: Implement Monet
        AppTheme.GREEN_APPLE -> if (isSystemInDarkTheme()) GreenAppleDarkColorScheme else GreenAppleLightColorScheme
        AppTheme.STRAWBERRY -> if (isSystemInDarkTheme()) StrawberryDarkColorScheme else StrawberryLightColorScheme
        AppTheme.TAKO -> if (isSystemInDarkTheme()) TakoDarkColorScheme else TakoLightColorScheme
        AppTheme.TACHIYOMI -> if (isSystemInDarkTheme()) TachiyomiDarkColorScheme else TachiyomiLightColorScheme
        AppTheme.MIDNIGHT -> if (isSystemInDarkTheme()) MidnightDarkColorScheme else MidnightLightColorScheme
        AppTheme.OCEAN_BLUE -> if (isSystemInDarkTheme()) OceanBlueDarkColorScheme else OceanBlueLightColorScheme
        AppTheme.SUNSET_ORANGE -> if (isSystemInDarkTheme()) SunsetOrangeDarkColorScheme else SunsetOrangeLightColorScheme
        AppTheme.LAVENDER_PURPLE -> if (isSystemInDarkTheme()) LavenderPurpleDarkColorScheme else LavenderPurpleLightColorScheme
        AppTheme.FOREST_GREEN -> if (isSystemInDarkTheme()) ForestGreenDarkColorScheme else ForestGreenLightColorScheme
        AppTheme.MONOCHROME_MINIMAL -> if (isSystemInDarkTheme()) MonochromeMinimalDarkColorScheme else MonochromeMinimalLightColorScheme
        AppTheme.CHERRY_BLOSSOM -> if (isSystemInDarkTheme()) CherryBlossomDarkColorScheme else CherryBlossomLightColorScheme
        AppTheme.MIDNIGHT_SKY -> if (isSystemInDarkTheme()) MidnightSkyDarkColorScheme else MidnightSkyLightColorScheme
        AppTheme.AUTUMN_HARVEST -> if (isSystemInDarkTheme()) AutumnHarvestDarkColorScheme else AutumnHarvestLightColorScheme
        AppTheme.EMERALD_FOREST -> if (isSystemInDarkTheme()) EmeraldForestDarkColorScheme else EmeraldForestLightColorScheme
        AppTheme.ROSE_GOLD -> if (isSystemInDarkTheme()) RoseGoldDarkColorScheme else RoseGoldLightColorScheme
    }
    
    return if (isAmoled && isSystemInDarkTheme()) {
        colorScheme.copy(
            background = Color.Black,
            surface = Color.Black,
        )
    } else {
        colorScheme
    }
}

// Default IReader color schemes
private val IReaderLightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF001D35),
    secondary = Color(0xFF1976D2),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFBBDEFB),
    onSecondaryContainer = Color(0xFF001D35),
    tertiary = Color(0xFF7C4DFF),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE1BEE7),
    onTertiaryContainer = Color(0xFF2E0034),
    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF424242),
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFFBDBDBD),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2F2F2F),
    inverseOnSurface = Color(0xFFF0F0F0),
    inversePrimary = Color(0xFF90CAF9),
)

private val IReaderDarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF004881),
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF90CAF9),
    onSecondary = Color(0xFF003258),
    secondaryContainer = Color(0xFF004881),
    onSecondaryContainer = Color(0xFFBBDEFB),
    tertiary = Color(0xFFB39DDB),
    onTertiary = Color(0xFF4A148C),
    tertiaryContainer = Color(0xFF6A1B9A),
    onTertiaryContainer = Color(0xFFE1BEE7),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF424242),
    onSurfaceVariant = Color(0xFFBDBDBD),
    outline = Color(0xFF8E8E8E),
    outlineVariant = Color(0xFF424242),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = Color(0xFF2F2F2F),
    inversePrimary = Color(0xFF1976D2),
)