package ireader.common.models.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb


import kotlinx.serialization.Serializable


data class ReaderTheme(
    val id: Long = 0,
    val backgroundColor: Int,
    val onTextColor: Int,
)

@kotlinx.serialization.Serializable
data class CustomTheme(

    val id: Long = 0,

    val materialColor: CustomColorScheme,

    val extraColors: CustomExtraColors,
    val dark: Boolean = false
)

@kotlinx.serialization.Serializable
data class CustomColorScheme(
    val primary: Int,
    val onPrimary: Int,
    val primaryContainer: Int,
    val onPrimaryContainer: Int,
    val inversePrimary: Int,
    val secondary: Int,
    val onSecondary: Int,
    val secondaryContainer: Int,
    val onSecondaryContainer: Int,
    val tertiary: Int,
    val onTertiary: Int,
    val tertiaryContainer: Int,
    val onTertiaryContainer: Int,
    val background: Int,
    val onBackground: Int,
    val surface: Int,
    val onSurface: Int,
    val surfaceVariant: Int,
    val onSurfaceVariant: Int,
    val surfaceTint: Int,
    val inverseSurface: Int,
    val inverseOnSurface: Int,
    val error: Int,
    val onError: Int,
    val errorContainer: Int,
    val onErrorContainer: Int,
    val outline: Int,
    val outlineVariant: Int,
    val scrim: Int,
)

private fun Int.toArgColor(): Color {
    return Color(this)
}

fun CustomColorScheme.toColorScheme(): ColorScheme {
    return ColorScheme(
        primary = this.primary.toArgColor(),
        primaryContainer = this.primaryContainer.toArgColor(),
        onPrimary = this.onPrimary.toArgColor(),
        secondary = this.secondary.toArgColor(),
        onSecondary = this.onSecondary.toArgColor(),
        background = this.background.toArgColor(),
        surface = this.surface.toArgColor(),
        onBackground = this.onBackground.toArgColor(),
        onSurface = this.onSurface.toArgColor(),
        error = this.error.toArgColor(),
        onError = this.onError.toArgColor(),
        surfaceTint = this.surfaceTint.toArgColor(),
        secondaryContainer = this.secondaryContainer.toArgColor(),
        errorContainer = this.errorContainer.toArgColor(),
        inverseOnSurface = this.inverseOnSurface.toArgColor(),
        inversePrimary = this.inversePrimary.toArgColor(),
        inverseSurface = this.inverseSurface.toArgColor(),
        onErrorContainer = this.onErrorContainer.toArgColor(),
        onPrimaryContainer = this.onPrimaryContainer.toArgColor(),
        onSecondaryContainer = this.onSecondaryContainer.toArgColor(),
        onSurfaceVariant = this.onSurfaceVariant.toArgColor(),
        onTertiary = this.onTertiary.toArgColor(),
        onTertiaryContainer = this.onTertiaryContainer.toArgColor(),
        outline = this.outline.toArgColor(),
        surfaceVariant = this.surfaceVariant.toArgColor(),
        tertiary = this.tertiary.toArgColor(),
        tertiaryContainer = this.tertiaryContainer.toArgColor(),
        outlineVariant = this.outlineVariant.toArgColor(),
        scrim = this.scrim.toArgColor()
    )
}

fun ColorScheme.toCustomColorScheme(): CustomColorScheme {
    return CustomColorScheme(
        primary = this.primary.toArgb(),
        primaryContainer = this.primaryContainer.toArgb(),
        onPrimary = this.onPrimary.toArgb(),
        secondary = this.secondary.toArgb(),
        onSecondary = this.onSecondary.toArgb(),
        background = this.background.toArgb(),
        surface = this.surface.toArgb(),
        onBackground = this.onBackground.toArgb(),
        onSurface = this.onSurface.toArgb(),
        error = this.error.toArgb(),
        onError = this.onError.toArgb(),
        surfaceTint = this.surfaceTint.toArgb(),
        secondaryContainer = this.secondaryContainer.toArgb(),
        errorContainer = this.errorContainer.toArgb(),
        inverseOnSurface = this.inverseOnSurface.toArgb(),
        inversePrimary = this.inversePrimary.toArgb(),
        inverseSurface = this.inverseSurface.toArgb(),
        onErrorContainer = this.onErrorContainer.toArgb(),
        onPrimaryContainer = this.onPrimaryContainer.toArgb(),
        onSecondaryContainer = this.onSecondaryContainer.toArgb(),
        onSurfaceVariant = this.onSurfaceVariant.toArgb(),
        onTertiary = this.onTertiary.toArgb(),
        onTertiaryContainer = this.onTertiaryContainer.toArgb(),
        outline = this.outline.toArgb(),
        surfaceVariant = this.surfaceVariant.toArgb(),
        tertiary = this.tertiary.toArgb(),
        tertiaryContainer = this.tertiaryContainer.toArgb(),
        scrim = 0,
        outlineVariant = 0
    )
}

fun ExtraColors.toCustomExtraColors(): CustomExtraColors {
    return CustomExtraColors(
        bars = this.bars.toArgb(),
        onBars = this.onBars.toArgb(),
        isBarLight = this.isBarLight
    )
}

fun CustomExtraColors.toExtraColor(): ExtraColors {
    return ExtraColors(
        bars = this.bars.toArgColor(),
        onBars = this.onBars.toArgColor(),
        isBarLight = this.isBarLight
    )
}

@Serializable
data class CustomExtraColors(
    val bars: Int = Color.Unspecified.toArgb(),
    val onBars: Int = Color.Unspecified.toArgb(),
    val isBarLight: Boolean = Color(bars).luminance() > 0.5,
)
