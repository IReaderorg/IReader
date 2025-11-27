package ireader.domain.models.theme

import ireader.domain.models.common.DomainColor

/**
 * Domain-layer color scheme that doesn't depend on Compose Material3
 * 
 * This replaces androidx.compose.material3.ColorScheme in the domain layer
 * to maintain clean architecture principles.
 */
data class DomainColorScheme(
    val primary: DomainColor,
    val onPrimary: DomainColor,
    val primaryContainer: DomainColor,
    val onPrimaryContainer: DomainColor,
    val inversePrimary: DomainColor,
    val secondary: DomainColor,
    val onSecondary: DomainColor,
    val secondaryContainer: DomainColor,
    val onSecondaryContainer: DomainColor,
    val tertiary: DomainColor,
    val onTertiary: DomainColor,
    val tertiaryContainer: DomainColor,
    val onTertiaryContainer: DomainColor,
    val background: DomainColor,
    val onBackground: DomainColor,
    val surface: DomainColor,
    val onSurface: DomainColor,
    val surfaceVariant: DomainColor,
    val onSurfaceVariant: DomainColor,
    val surfaceTint: DomainColor,
    val inverseSurface: DomainColor,
    val inverseOnSurface: DomainColor,
    val error: DomainColor,
    val onError: DomainColor,
    val errorContainer: DomainColor,
    val onErrorContainer: DomainColor,
    val outline: DomainColor,
    val outlineVariant: DomainColor,
    val scrim: DomainColor,
    val surfaceBright: DomainColor = surface,
    val surfaceDim: DomainColor = surface,
    val surfaceContainer: DomainColor = surface,
    val surfaceContainerHigh: DomainColor = surface,
    val surfaceContainerHighest: DomainColor = surface,
    val surfaceContainerLow: DomainColor = surface,
    val surfaceContainerLowest: DomainColor = surface
) {
    companion object {
        /**
         * Create a light color scheme with default Material3 colors
         */
        fun lightColorScheme(
            primary: DomainColor = DomainColor.fromArgb(0xFF6750A4.toInt()),
            onPrimary: DomainColor = DomainColor.White,
            primaryContainer: DomainColor = DomainColor.fromArgb(0xFFEADDFF.toInt()),
            onPrimaryContainer: DomainColor = DomainColor.fromArgb(0xFF21005D.toInt()),
            inversePrimary: DomainColor = DomainColor.fromArgb(0xFFD0BCFF.toInt()),
            secondary: DomainColor = DomainColor.fromArgb(0xFF625B71.toInt()),
            onSecondary: DomainColor = DomainColor.White,
            secondaryContainer: DomainColor = DomainColor.fromArgb(0xFFE8DEF8.toInt()),
            onSecondaryContainer: DomainColor = DomainColor.fromArgb(0xFF1D192B.toInt()),
            tertiary: DomainColor = DomainColor.fromArgb(0xFF7D5260.toInt()),
            onTertiary: DomainColor = DomainColor.White,
            tertiaryContainer: DomainColor = DomainColor.fromArgb(0xFFFFD8E4.toInt()),
            onTertiaryContainer: DomainColor = DomainColor.fromArgb(0xFF31111D.toInt()),
            background: DomainColor = DomainColor.fromArgb(0xFFFFFBFE.toInt()),
            onBackground: DomainColor = DomainColor.fromArgb(0xFF1C1B1F.toInt()),
            surface: DomainColor = DomainColor.fromArgb(0xFFFFFBFE.toInt()),
            onSurface: DomainColor = DomainColor.fromArgb(0xFF1C1B1F.toInt()),
            surfaceVariant: DomainColor = DomainColor.fromArgb(0xFFE7E0EC.toInt()),
            onSurfaceVariant: DomainColor = DomainColor.fromArgb(0xFF49454F.toInt()),
            surfaceTint: DomainColor = primary,
            inverseSurface: DomainColor = DomainColor.fromArgb(0xFF313033.toInt()),
            inverseOnSurface: DomainColor = DomainColor.fromArgb(0xFFF4EFF4.toInt()),
            error: DomainColor = DomainColor.fromArgb(0xFFB3261E.toInt()),
            onError: DomainColor = DomainColor.White,
            errorContainer: DomainColor = DomainColor.fromArgb(0xFFF9DEDC.toInt()),
            onErrorContainer: DomainColor = DomainColor.fromArgb(0xFF410E0B.toInt()),
            outline: DomainColor = DomainColor.fromArgb(0xFF79747E.toInt()),
            outlineVariant: DomainColor = DomainColor.fromArgb(0xFFCAC4D0.toInt()),
            scrim: DomainColor = DomainColor.Black
        ): DomainColorScheme {
            return DomainColorScheme(
                primary, onPrimary, primaryContainer, onPrimaryContainer, inversePrimary,
                secondary, onSecondary, secondaryContainer, onSecondaryContainer,
                tertiary, onTertiary, tertiaryContainer, onTertiaryContainer,
                background, onBackground, surface, onSurface, surfaceVariant, onSurfaceVariant,
                surfaceTint, inverseSurface, inverseOnSurface,
                error, onError, errorContainer, onErrorContainer,
                outline, outlineVariant, scrim
            )
        }
        
        /**
         * Create a dark color scheme with default Material3 colors
         */
        fun darkColorScheme(
            primary: DomainColor = DomainColor.fromArgb(0xFFD0BCFF.toInt()),
            onPrimary: DomainColor = DomainColor.fromArgb(0xFF381E72.toInt()),
            primaryContainer: DomainColor = DomainColor.fromArgb(0xFF4F378B.toInt()),
            onPrimaryContainer: DomainColor = DomainColor.fromArgb(0xFFEADDFF.toInt()),
            inversePrimary: DomainColor = DomainColor.fromArgb(0xFF6750A4.toInt()),
            secondary: DomainColor = DomainColor.fromArgb(0xFFCCC2DC.toInt()),
            onSecondary: DomainColor = DomainColor.fromArgb(0xFF332D41.toInt()),
            secondaryContainer: DomainColor = DomainColor.fromArgb(0xFF4A4458.toInt()),
            onSecondaryContainer: DomainColor = DomainColor.fromArgb(0xFFE8DEF8.toInt()),
            tertiary: DomainColor = DomainColor.fromArgb(0xFFEFB8C8.toInt()),
            onTertiary: DomainColor = DomainColor.fromArgb(0xFF492532.toInt()),
            tertiaryContainer: DomainColor = DomainColor.fromArgb(0xFF633B48.toInt()),
            onTertiaryContainer: DomainColor = DomainColor.fromArgb(0xFFFFD8E4.toInt()),
            background: DomainColor = DomainColor.fromArgb(0xFF1C1B1F.toInt()),
            onBackground: DomainColor = DomainColor.fromArgb(0xFFE6E1E5.toInt()),
            surface: DomainColor = DomainColor.fromArgb(0xFF1C1B1F.toInt()),
            onSurface: DomainColor = DomainColor.fromArgb(0xFFE6E1E5.toInt()),
            surfaceVariant: DomainColor = DomainColor.fromArgb(0xFF49454F.toInt()),
            onSurfaceVariant: DomainColor = DomainColor.fromArgb(0xFFCAC4D0.toInt()),
            surfaceTint: DomainColor = primary,
            inverseSurface: DomainColor = DomainColor.fromArgb(0xFFE6E1E5.toInt()),
            inverseOnSurface: DomainColor = DomainColor.fromArgb(0xFF313033.toInt()),
            error: DomainColor = DomainColor.fromArgb(0xFFF2B8B5.toInt()),
            onError: DomainColor = DomainColor.fromArgb(0xFF601410.toInt()),
            errorContainer: DomainColor = DomainColor.fromArgb(0xFF8C1D18.toInt()),
            onErrorContainer: DomainColor = DomainColor.fromArgb(0xFFF9DEDC.toInt()),
            outline: DomainColor = DomainColor.fromArgb(0xFF938F99.toInt()),
            outlineVariant: DomainColor = DomainColor.fromArgb(0xFF49454F.toInt()),
            scrim: DomainColor = DomainColor.Black
        ): DomainColorScheme {
            return DomainColorScheme(
                primary, onPrimary, primaryContainer, onPrimaryContainer, inversePrimary,
                secondary, onSecondary, secondaryContainer, onSecondaryContainer,
                tertiary, onTertiary, tertiaryContainer, onTertiaryContainer,
                background, onBackground, surface, onSurface, surfaceVariant, onSurfaceVariant,
                surfaceTint, inverseSurface, inverseOnSurface,
                error, onError, errorContainer, onErrorContainer,
                outline, outlineVariant, scrim
            )
        }
    }
}
