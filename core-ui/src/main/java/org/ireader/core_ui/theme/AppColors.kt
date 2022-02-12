package org.ireader.core_ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.material.Colors as MaterialColors

/**
 * All the application colors from both [MaterialTheme.colors] and [ExtraColors] which can be
 * accessed through this class.
 */
@Stable
@Suppress("unused")
class AppColors(
    private val materialColors: MaterialColors,
    private val extraColors: ExtraColors,
) {

    val primary get() = materialColors.primary
    val primaryVariant get() = materialColors.primaryVariant
    val secondary get() = materialColors.secondary
    val secondaryVariant get() = materialColors.secondaryVariant
    val background get() = materialColors.background
    val surface get() = materialColors.surface
    val error get() = materialColors.error
    val onPrimary get() = materialColors.onPrimary
    val onSecondary get() = materialColors.onSecondary
    val onBackground get() = materialColors.onBackground
    val onSurface get() = materialColors.onSurface
    val onError get() = materialColors.onError
    val isLight get() = materialColors.isLight

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
    materialColors: MaterialColors,
    extraColors: ExtraColors,
    typography: Typography,
    shape: Shapes,
    content: @Composable () -> Unit,
) {
    val rememberedCustomColors = remember { extraColors }.apply { updateFrom(extraColors) }
    val rememberedAppColors = remember { AppColors(materialColors, rememberedCustomColors) }

    MaterialTheme(
        colors = materialColors,
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

/**
 * The extra colors of the application which are not included in [MaterialTheme.colors]. An instance
 * of this class can be retrieved through the composition local [AppColors.current].
 */
@Stable
class ExtraColors(
    bars: Color = Color.Unspecified,
    onBars: Color = Color.Unspecified,
    isBarLight: Boolean = bars.luminance() > 0.5,
) {
    var bars by mutableStateOf(bars, structuralEqualityPolicy())
        private set
    var onBars by mutableStateOf(onBars, structuralEqualityPolicy())
        private set
    var isBarLight by mutableStateOf(isBarLight, structuralEqualityPolicy())
        private set

    fun updateFrom(other: ExtraColors) {
        bars = other.bars
        onBars = other.onBars
        isBarLight = other.isBarLight
    }

}
