package ireader.presentation.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import ireader.domain.models.theme.ExtraColors
import ireader.presentation.ui.core.theme.AppColors
import ireader.presentation.ui.core.theme.LocalAppColors
import ireader.presentation.core.toDomainColor

@Composable
fun WithCoverBasedTheme(
    coverBasedColorScheme: ColorScheme?,
    content: @Composable () -> Unit
) {
    if (coverBasedColorScheme != null) {
        val coverBasedExtraColors = ExtraColors(
            bars = coverBasedColorScheme.primary.toDomainColor(),
            onBars = coverBasedColorScheme.onPrimary.toDomainColor()
        )
        val coverBasedAppColors = remember(coverBasedColorScheme, coverBasedExtraColors) {
            AppColors(
                materialColors = coverBasedColorScheme,
                extraColors = coverBasedExtraColors
            )
        }
        CompositionLocalProvider(
            LocalAppColors provides coverBasedAppColors
        ) {
            MaterialTheme(
                colorScheme = coverBasedColorScheme,
                typography = MaterialTheme.typography,
                shapes = MaterialTheme.shapes,
                content = content
            )
        }
    } else {
        content()
    }
}
