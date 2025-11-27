package ireader.domain.models.theme

import ireader.domain.models.common.DomainColor
import ireader.domain.plugins.ThemeColorScheme
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

/**
 * Convert CustomColorScheme to DomainColorScheme
 */
fun CustomColorScheme.toDomainColorScheme(): DomainColorScheme {
    return DomainColorScheme(
        primary = DomainColor.fromArgb(this.primary),
        onPrimary = DomainColor.fromArgb(this.onPrimary),
        primaryContainer = DomainColor.fromArgb(this.primaryContainer),
        onPrimaryContainer = DomainColor.fromArgb(this.onPrimaryContainer),
        inversePrimary = DomainColor.fromArgb(this.inversePrimary),
        secondary = DomainColor.fromArgb(this.secondary),
        onSecondary = DomainColor.fromArgb(this.onSecondary),
        secondaryContainer = DomainColor.fromArgb(this.secondaryContainer),
        onSecondaryContainer = DomainColor.fromArgb(this.onSecondaryContainer),
        tertiary = DomainColor.fromArgb(this.tertiary),
        onTertiary = DomainColor.fromArgb(this.onTertiary),
        tertiaryContainer = DomainColor.fromArgb(this.tertiaryContainer),
        onTertiaryContainer = DomainColor.fromArgb(this.onTertiaryContainer),
        background = DomainColor.fromArgb(this.background),
        onBackground = DomainColor.fromArgb(this.onBackground),
        surface = DomainColor.fromArgb(this.surface),
        onSurface = DomainColor.fromArgb(this.onSurface),
        surfaceVariant = DomainColor.fromArgb(this.surfaceVariant),
        onSurfaceVariant = DomainColor.fromArgb(this.onSurfaceVariant),
        surfaceTint = DomainColor.fromArgb(this.surfaceTint),
        inverseSurface = DomainColor.fromArgb(this.inverseSurface),
        inverseOnSurface = DomainColor.fromArgb(this.inverseOnSurface),
        error = DomainColor.fromArgb(this.error),
        onError = DomainColor.fromArgb(this.onError),
        errorContainer = DomainColor.fromArgb(this.errorContainer),
        onErrorContainer = DomainColor.fromArgb(this.onErrorContainer),
        outline = DomainColor.fromArgb(this.outline),
        outlineVariant = DomainColor.fromArgb(this.outlineVariant),
        scrim = DomainColor.fromArgb(this.scrim)
    )
}/**
 * Convert CustomColorScheme to DomainColorScheme
 */
fun ThemeColorScheme.toDomainColorScheme(): DomainColorScheme {
    return DomainColorScheme(
        primary = DomainColor.fromArgb(this.primary.toInt()),
        onPrimary = DomainColor.fromArgb(this.onPrimary.toInt()),
        primaryContainer = DomainColor.fromArgb(this.primaryContainer.toInt()),
        onPrimaryContainer = DomainColor.fromArgb(this.onPrimaryContainer.toInt()),
        inversePrimary = DomainColor.fromArgb(this.inversePrimary.toInt()),
        secondary = DomainColor.fromArgb(this.secondary.toInt()),
        onSecondary = DomainColor.fromArgb(this.onSecondary.toInt()),
        secondaryContainer = DomainColor.fromArgb(this.secondaryContainer.toInt()),
        onSecondaryContainer = DomainColor.fromArgb(this.onSecondaryContainer.toInt()),
        tertiary = DomainColor.fromArgb(this.tertiary.toInt()),
        onTertiary = DomainColor.fromArgb(this.onTertiary.toInt()),
        tertiaryContainer = DomainColor.fromArgb(this.tertiaryContainer.toInt()),
        onTertiaryContainer = DomainColor.fromArgb(this.onTertiaryContainer.toInt()),
        background = DomainColor.fromArgb(this.background.toInt()),
        onBackground = DomainColor.fromArgb(this.onBackground.toInt()),
        surface = DomainColor.fromArgb(this.surface.toInt()),
        onSurface = DomainColor.fromArgb(this.onSurface.toInt()),
        surfaceVariant = DomainColor.fromArgb(this.surfaceVariant.toInt()),
        onSurfaceVariant = DomainColor.fromArgb(this.onSurfaceVariant.toInt()),
        surfaceTint = DomainColor.fromArgb(this.surfaceVariant.toInt()),
        inverseSurface = DomainColor.fromArgb(this.inverseSurface.toInt()),
        inverseOnSurface = DomainColor.fromArgb(this.inverseOnSurface.toInt()),
        error = DomainColor.fromArgb(this.error.toInt()),
        onError = DomainColor.fromArgb(this.onError.toInt()),
        errorContainer = DomainColor.fromArgb(this.errorContainer.toInt()),
        onErrorContainer = DomainColor.fromArgb(this.onErrorContainer.toInt()),
        outline = DomainColor.fromArgb(this.outline.toInt()),
        outlineVariant = DomainColor.fromArgb(this.outlineVariant.toInt()),
        scrim = DomainColor.fromArgb(this.scrim.toInt())
    )
}

/**
 * Convert DomainColorScheme to CustomColorScheme
 */
fun DomainColorScheme.toCustomColorScheme(): CustomColorScheme {
    return CustomColorScheme(
        primary = this.primary.toArgb(),
        onPrimary = this.onPrimary.toArgb(),
        primaryContainer = this.primaryContainer.toArgb(),
        onPrimaryContainer = this.onPrimaryContainer.toArgb(),
        inversePrimary = this.inversePrimary.toArgb(),
        secondary = this.secondary.toArgb(),
        onSecondary = this.onSecondary.toArgb(),
        secondaryContainer = this.secondaryContainer.toArgb(),
        onSecondaryContainer = this.onSecondaryContainer.toArgb(),
        tertiary = this.tertiary.toArgb(),
        onTertiary = this.onTertiary.toArgb(),
        tertiaryContainer = this.tertiaryContainer.toArgb(),
        onTertiaryContainer = this.onTertiaryContainer.toArgb(),
        background = this.background.toArgb(),
        onBackground = this.onBackground.toArgb(),
        surface = this.surface.toArgb(),
        onSurface = this.onSurface.toArgb(),
        surfaceVariant = this.surfaceVariant.toArgb(),
        onSurfaceVariant = this.onSurfaceVariant.toArgb(),
        surfaceTint = this.surfaceTint.toArgb(),
        inverseSurface = this.inverseSurface.toArgb(),
        inverseOnSurface = this.inverseOnSurface.toArgb(),
        error = this.error.toArgb(),
        onError = this.onError.toArgb(),
        errorContainer = this.errorContainer.toArgb(),
        onErrorContainer = this.onErrorContainer.toArgb(),
        outline = this.outline.toArgb(),
        outlineVariant = this.outlineVariant.toArgb(),
        scrim = this.scrim.toArgb()
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
        bars = DomainColor.fromArgb(this.bars),
        onBars = DomainColor.fromArgb(this.onBars),
    )
}

@Serializable
data class CustomExtraColors(
    val bars: Int = 0,
    val onBars: Int = 0,
    val isBarLight: Boolean = false,
)
