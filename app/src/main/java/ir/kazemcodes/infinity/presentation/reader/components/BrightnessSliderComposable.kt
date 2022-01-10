package ir.kazemcodes.infinity.presentation.reader.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.kazemcodes.infinity.presentation.book_detail.DEFAULT

@Composable
fun BrightnessSliderComposable(brightness : Float, onValueChange : (brightness : Float) -> Unit) {
    Slider(
        brightness,
        {
            onValueChange(it)

        },
        modifier = Modifier
            .fillMaxWidth(),
        valueRange = DEFAULT.MIN_BRIGHTNESS..DEFAULT.MAX_BRIGHTNESS,
        steps = 0,
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colors.primary,
            activeTrackColor = MaterialTheme.colors.onBackground.copy(alpha = .6f),
            inactiveTrackColor = MaterialTheme.colors.onBackground.copy(alpha = .6f),
        )

    )
}