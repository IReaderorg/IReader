package ireader.domain.models.theme


import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * The extra colors of the application which are not included in [MaterialTheme.colorScheme]. An instance
 * of this class can be retrieved through the composition local [AppColors.current].
 */
@Stable
data class ExtraColors(
    val bars: Color = Color.Unspecified,
    val onBars: Color = Color.Unspecified,
) {
    // Calculate isBarLight dynamically based on current bars color
    val isBarLight: Boolean
        get() = bars.luminance() > 0.5
}
