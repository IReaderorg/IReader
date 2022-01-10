package ir.kazemcodes.infinity.presentation.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ir.kazemcodes.infinity.presentation.book_detail.DEFAULT

@Composable
fun BrightnessSliderComposable(modifier: Modifier = Modifier, brightness: Float, onValueChange: (brightness: Float) -> Unit) {

    Column() {
        Text(text = "Brightness", style = MaterialTheme.typography.caption)
        Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            Icon(modifier = modifier.weight(1f), imageVector = Icons.Default.LightMode, contentDescription = "less brightness" )
            Slider(
                brightness,
                onValueChange = { onValueChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(8f),
                valueRange = DEFAULT.MIN_BRIGHTNESS..DEFAULT.MAX_BRIGHTNESS,
                steps = 0,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colors.primary,
                    activeTrackColor = MaterialTheme.colors.onBackground.copy(alpha = .6f),
                    inactiveTrackColor = MaterialTheme.colors.onBackground.copy(alpha = .6f),
                )

            )
            Icon(modifier = modifier.weight(1f),imageVector = Icons.Default.Brightness7, contentDescription = "less brightness" )
        }
    }

}