package ireader.presentation.core

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import ireader.domain.models.theme.DomainColorScheme

/**
 * Extension functions to convert between DomainColorScheme (domain layer)
 * and Compose ColorScheme (presentation layer)
 * 
 * Note: toComposeColorScheme() is defined in DomainColorExtensions.kt
 */

/**
 * Convert Compose ColorScheme to DomainColorScheme
 */
fun ColorScheme.toDomainColorScheme(): DomainColorScheme {
    return DomainColorScheme(
        primary = primary.toDomainColor(),
        onPrimary = onPrimary.toDomainColor(),
        primaryContainer = primaryContainer.toDomainColor(),
        onPrimaryContainer = onPrimaryContainer.toDomainColor(),
        inversePrimary = inversePrimary.toDomainColor(),
        secondary = secondary.toDomainColor(),
        onSecondary = onSecondary.toDomainColor(),
        secondaryContainer = secondaryContainer.toDomainColor(),
        onSecondaryContainer = onSecondaryContainer.toDomainColor(),
        tertiary = tertiary.toDomainColor(),
        onTertiary = onTertiary.toDomainColor(),
        tertiaryContainer = tertiaryContainer.toDomainColor(),
        onTertiaryContainer = onTertiaryContainer.toDomainColor(),
        background = background.toDomainColor(),
        onBackground = onBackground.toDomainColor(),
        surface = surface.toDomainColor(),
        onSurface = onSurface.toDomainColor(),
        surfaceVariant = surfaceVariant.toDomainColor(),
        onSurfaceVariant = onSurfaceVariant.toDomainColor(),
        surfaceTint = surfaceTint.toDomainColor(),
        inverseSurface = inverseSurface.toDomainColor(),
        inverseOnSurface = inverseOnSurface.toDomainColor(),
        error = error.toDomainColor(),
        onError = onError.toDomainColor(),
        errorContainer = errorContainer.toDomainColor(),
        onErrorContainer = onErrorContainer.toDomainColor(),
        outline = outline.toDomainColor(),
        outlineVariant = outlineVariant.toDomainColor(),
        scrim = scrim.toDomainColor(),
        surfaceBright = surfaceBright.toDomainColor(),
        surfaceDim = surfaceDim.toDomainColor(),
        surfaceContainer = surfaceContainer.toDomainColor(),
        surfaceContainerHigh = surfaceContainerHigh.toDomainColor(),
        surfaceContainerHighest = surfaceContainerHighest.toDomainColor(),
        surfaceContainerLow = surfaceContainerLow.toDomainColor(),
        surfaceContainerLowest = surfaceContainerLowest.toDomainColor()
    )
}

/**
 * Helper to create light ColorScheme from DomainColorScheme
 */
fun domainLightColorScheme(
    primary: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(0xFF6750A4),
    onPrimary: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.White,
    // ... other parameters
): ColorScheme {
    return DomainColorScheme.lightColorScheme(
        primary = primary.toDomainColor(),
        onPrimary = onPrimary.toDomainColor()
        // ... other parameters
    ).toComposeColorScheme()
}

/**
 * Helper to create dark ColorScheme from DomainColorScheme
 */
fun domainDarkColorScheme(
    primary: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(0xFFD0BCFF),
    onPrimary: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(0xFF381E72),
    // ... other parameters
): ColorScheme {
    return DomainColorScheme.darkColorScheme(
        primary = primary.toDomainColor(),
        onPrimary = onPrimary.toDomainColor()
        // ... other parameters
    ).toComposeColorScheme()
}
