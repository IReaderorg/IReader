package ireader.data.apptheme

import androidx.compose.material3.ColorScheme
import ireader.common.models.theme.*

val  appThemeMapper = {
        id: Long,
        isDark: Boolean,
        primary: Int,
        onPrimary: Int,
        primaryContainer: Int,
        onPrimaryContainer: Int,
        inversePrimary: Int,
        secondary: Int,
        onSecondary: Int,
        secondaryContainer: Int,
        onSecondaryContainer: Int,
        tertiary: Int,
        onTertiary: Int,
        tertiaryContainer: Int,
        onTertiaryContainer: Int,
        background: Int,
        onBackground: Int,
        surface: Int,
        onSurface: Int,
        surfaceVariant: Int,
        onSurfaceVariant: Int,
        surfaceTint: Int,
        inverseSurface: Int,
        inverseOnSurface: Int,
        error: Int,
        onError: Int,
        errorContainer: Int,
        onErrorContainer: Int,
        outline: Int,
        outlineVariant: Int,
        scrim: Int,
        bars: Int,
        onBars: Int,
        isBarLight: Boolean ->         Theme(
        id,
        materialColors = CustomColorScheme(
                primary,
                onPrimary,
                primaryContainer,
                onPrimaryContainer,
                inversePrimary,
                secondary,
                onSecondary,
                secondaryContainer,
                onSecondaryContainer,
                tertiary,
                onTertiary,
                tertiaryContainer,
                onTertiaryContainer,
                background,
                onBackground,
                surface,
                onSurface,
                surfaceVariant,
                onSurfaceVariant,
                surfaceTint,
                inverseSurface,
                inverseOnSurface,
                error,
                onError,
                errorContainer,
                onErrorContainer,
                outline,
                outlineVariant,
                scrim,
        ).toColorScheme(),
        extraColors = CustomExtraColors(
                bars,
                onBars,
                isBarLight,
        ).toExtraColor(),
        isDark,


        )
}

val readerMapper = {_id: Long, background_color: Int, on_textcolor: Int ->
        ReaderTheme(
                _id,
                background_color,
                on_textcolor
        )
}
