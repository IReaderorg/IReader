package org.ireader.reader.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.common_resources.DEFAULT
import org.ireader.components.reusable_composable.CaptionTextComposable
import org.ireader.reader.viewmodel.ReaderScreenPreferencesState

@Composable
fun BrightnessSliderComposable(
    modifier: Modifier = Modifier,
    viewModel: ReaderScreenPreferencesState,
    onToggleAutoBrightness:() -> Unit,
    onChangeBrightness:(Float) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {

        Row(modifier = Modifier
            .fillMaxWidth()
            .weight(4F),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically) {
            Icon(modifier = modifier.weight(1f),
                imageVector = Icons.Default.LightMode,
                contentDescription = "less brightness")
            Slider(
                viewModel.brightness,
                onValueChange = onChangeBrightness,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(8f),
                valueRange = DEFAULT.MIN_BRIGHTNESS..DEFAULT.MAX_BRIGHTNESS,
                steps = 0,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colors.primary,
                    activeTrackColor = MaterialTheme.colors.primary.copy(alpha = .6f),
                    inactiveTrackColor = MaterialTheme.colors.onBackground.copy(alpha = .4f),
                ),
                enabled = !viewModel.autoBrightnessMode

            )
            Icon(modifier = modifier.weight(1f),
                imageVector = Icons.Default.Brightness7,
                contentDescription = "less brightness")
        }
        OutlinedButton(onClick = onToggleAutoBrightness,
            modifier = Modifier
                .weight(1F)
                .padding(8.dp),
            border = BorderStroke(
                ButtonDefaults.OutlinedBorderSize,
                if (viewModel.autoBrightnessMode) MaterialTheme.colors.primary else MaterialTheme.colors.onBackground
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                backgroundColor = if (viewModel.autoBrightnessMode) MaterialTheme.colors.primary else MaterialTheme.colors.background,
            )) {
            CaptionTextComposable(
                modifier = Modifier.align(Alignment.CenterVertically),
                color = if (viewModel.autoBrightnessMode) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onBackground,
                text = "Auto",
                style = MaterialTheme.typography.caption,
                maxLine = 1)
        }
    }
}