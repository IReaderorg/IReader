package ireader.common.models.theme


import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * The extra colors of the application which are not included in [MaterialTheme.colorScheme]. An instance
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
