package ireader.presentation.ui.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import ireader.domain.models.theme.ExtraColors

/**
 * All the application colors from both [MaterialTheme.colorScheme] and [ExtraColors] which can be
 * accessed through this class.
 */
@Stable
@Suppress("unused")
class AppColors(
    private val materialColors: ColorScheme,
    private val extraColors: ExtraColors,
) {

    val primary get() = materialColors.primary
    val primaryContainer get() = materialColors.primaryContainer
    val secondary get() = materialColors.secondary
    val secondaryContainer get() = materialColors.secondaryContainer
    val background get() = materialColors.background
    val surface get() = materialColors.surface
    val error get() = materialColors.error
    val onPrimary get() = materialColors.onPrimary
    val onSecondary get() = materialColors.onSecondary
    val onBackground get() = materialColors.onBackground
    val onSurface get() = materialColors.onSurface
    val onError get() = materialColors.onError

    val bars get() = extraColors.bars
    val onBars get() = extraColors.onBars
    val isBarLight get() = extraColors.isBarLight

    companion object {
        val current: AppColors
            @Composable
            @ReadOnlyComposable
            get() = LocalAppColors.current
    }
}

/**
 * Composable that provides the [LocalAppColors] composition to children composables with the
 * [materialColors] and [extraColors] provided.
 */
@Composable
fun AppColors(
    materialColors: ColorScheme,
    extraColors: ExtraColors,
    typography: Typography,
    shape: Shapes,
    content: @Composable () -> Unit,
) {
    val rememberedCustomColors = remember { extraColors }.apply { updateFrom(extraColors) }
    val rememberedAppColors = remember { AppColors(materialColors, rememberedCustomColors) }

    MaterialTheme(
        colorScheme = materialColors,
        typography = typography,
        shapes = shape,
    ) {
        CompositionLocalProvider(
            LocalAppColors provides rememberedAppColors,
            content = content
        )
    }
}

private val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("The AppColors composable must be called before usage")
}