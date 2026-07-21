package ireader.presentation.core

import androidx.compose.ui.graphics.Color
import ireader.domain.models.common.DomainColor

/**
 * Extension functions to convert between DomainColor (domain layer)
 * and Compose Color (presentation layer)
 * 
 * These extensions maintain clean architecture by keeping Compose
 * dependencies out of the domain layer.
 */

/**
 * Convert DomainColor to Compose Color
 * 
 * Note: DomainColor.Unspecified is converted to Color.Unspecified to maintain
 * proper comparison semantics in the presentation layer.
 */
fun DomainColor.toComposeColor(): Color {
    // Handle Unspecified case - DomainColor.Unspecified should map to Color.Unspecified
    if (this == DomainColor.Unspecified) {
        return Color.Unspecified
    }
    return Color(
        red = this.red,
        green = this.green,
        blue = this.blue,
        alpha = this.alpha
    )
}

/**
 * Convert Compose Color to DomainColor
 * 
 * Note: Color.Unspecified is converted to DomainColor.Unspecified to maintain
 * proper comparison semantics across layers.
 */
fun Color.toDomainColor(): DomainColor {
    // Handle Unspecified case - Color.Unspecified should map to DomainColor.Unspecified
    if (this == Color.Unspecified) {
        return DomainColor.Unspecified
    }
    return DomainColor(
        red = this.red,
        green = this.green,
        blue = this.blue,
        alpha = this.alpha
    )
}

/**
 * Convert list of DomainColors to Compose Colors
 */
fun List<DomainColor>.toComposeColors(): List<Color> {
    return map { it.toComposeColor() }
}

/**
 * Convert list of Compose Colors to DomainColors
 */
fun List<Color>.toDomainColors(): List<DomainColor> {
    return map { it.toDomainColor() }
}

/**
 * Convert DomainColorScheme to Compose ColorScheme
 */
fun ireader.domain.models.theme.DomainColorScheme.toComposeColorScheme(): androidx.compose.material3.ColorScheme {
    return androidx.compose.material3.ColorScheme(
        primary = this.primary.toComposeColor(),
        onPrimary = this.onPrimary.toComposeColor(),
        primaryContainer = this.primaryContainer.toComposeColor(),
        onPrimaryContainer = this.onPrimaryContainer.toComposeColor(),
        inversePrimary = this.inversePrimary.toComposeColor(),
        secondary = this.secondary.toComposeColor(),
        onSecondary = this.onSecondary.toComposeColor(),
        secondaryContainer = this.secondaryContainer.toComposeColor(),
        onSecondaryContainer = this.onSecondaryContainer.toComposeColor(),
        tertiary = this.tertiary.toComposeColor(),
        onTertiary = this.onTertiary.toComposeColor(),
        tertiaryContainer = this.tertiaryContainer.toComposeColor(),
        onTertiaryContainer = this.onTertiaryContainer.toComposeColor(),
        background = this.background.toComposeColor(),
        onBackground = this.onBackground.toComposeColor(),
        surface = this.surface.toComposeColor(),
        onSurface = this.onSurface.toComposeColor(),
        surfaceVariant = this.surfaceVariant.toComposeColor(),
        onSurfaceVariant = this.onSurfaceVariant.toComposeColor(),
        surfaceTint = this.surfaceTint.toComposeColor(),
        inverseSurface = this.inverseSurface.toComposeColor(),
        inverseOnSurface = this.inverseOnSurface.toComposeColor(),
        error = this.error.toComposeColor(),
        onError = this.onError.toComposeColor(),
        errorContainer = this.errorContainer.toComposeColor(),
        onErrorContainer = this.onErrorContainer.toComposeColor(),
        outline = this.outline.toComposeColor(),
        outlineVariant = this.outlineVariant.toComposeColor(),
        scrim = this.scrim.toComposeColor(),
        surfaceBright = this.surfaceBright.toComposeColor(),
        surfaceDim = this.surfaceDim.toComposeColor(),
        surfaceContainer = this.surfaceContainer.toComposeColor(),
        surfaceContainerHigh = this.surfaceContainerHigh.toComposeColor(),
        surfaceContainerHighest = this.surfaceContainerHighest.toComposeColor(),
        surfaceContainerLow = this.surfaceContainerLow.toComposeColor(),
        surfaceContainerLowest = this.surfaceContainerLowest.toComposeColor()
    )
}

/**
 * Convert Compose ColorScheme to DomainColorScheme
 */
fun androidx.compose.material3.ColorScheme.toDomainColorScheme(isDark: Boolean = false): ireader.domain.models.theme.DomainColorScheme {
    return ireader.domain.models.theme.DomainColorScheme(
        primary = this.primary.toDomainColor(),
        onPrimary = this.onPrimary.toDomainColor(),
        primaryContainer = this.primaryContainer.toDomainColor(),
        onPrimaryContainer = this.onPrimaryContainer.toDomainColor(),
        inversePrimary = this.inversePrimary.toDomainColor(),
        secondary = this.secondary.toDomainColor(),
        onSecondary = this.onSecondary.toDomainColor(),
        secondaryContainer = this.secondaryContainer.toDomainColor(),
        onSecondaryContainer = this.onSecondaryContainer.toDomainColor(),
        tertiary = this.tertiary.toDomainColor(),
        onTertiary = this.onTertiary.toDomainColor(),
        tertiaryContainer = this.tertiaryContainer.toDomainColor(),
        onTertiaryContainer = this.onTertiaryContainer.toDomainColor(),
        background = this.background.toDomainColor(),
        onBackground = this.onBackground.toDomainColor(),
        surface = this.surface.toDomainColor(),
        onSurface = this.onSurface.toDomainColor(),
        surfaceVariant = this.surfaceVariant.toDomainColor(),
        onSurfaceVariant = this.onSurfaceVariant.toDomainColor(),
        surfaceTint = this.surfaceTint.toDomainColor(),
        inverseSurface = this.inverseSurface.toDomainColor(),
        inverseOnSurface = this.inverseOnSurface.toDomainColor(),
        error = this.error.toDomainColor(),
        onError = this.onError.toDomainColor(),
        errorContainer = this.errorContainer.toDomainColor(),
        onErrorContainer = this.onErrorContainer.toDomainColor(),
        outline = this.outline.toDomainColor(),
        outlineVariant = this.outlineVariant.toDomainColor(),
        scrim = this.scrim.toDomainColor(),
        surfaceBright = this.surfaceBright.toDomainColor(),
        surfaceDim = this.surfaceDim.toDomainColor(),
        surfaceContainer = this.surfaceContainer.toDomainColor(),
        surfaceContainerHigh = this.surfaceContainerHigh.toDomainColor(),
        surfaceContainerHighest = this.surfaceContainerHighest.toDomainColor(),
        surfaceContainerLow = this.surfaceContainerLow.toDomainColor(),
        surfaceContainerLowest = this.surfaceContainerLowest.toDomainColor()
    )
}
