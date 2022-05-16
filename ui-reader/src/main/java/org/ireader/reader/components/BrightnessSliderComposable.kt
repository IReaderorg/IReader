package org.ireader.reader.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.ireader.common_resources.DEFAULT
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.CaptionTextComposable
import org.ireader.reader.viewmodel.ReaderScreenPreferencesState
import org.ireader.ui_reader.R

@Composable
fun BrightnessSliderComposable(
    modifier: Modifier = Modifier,
    viewModel: ReaderScreenPreferencesState,
    onToggleAutoBrightness: () -> Unit,
    onChangeBrightness: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(4F),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconButton(
                modifier = modifier.weight(1f),
                imageVector = Icons.Default.LightMode,
                contentDescription = stringResource(R.string.less_brightness),
            )
            Slider(
                viewModel.brightness,
                onValueChange = onChangeBrightness,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(8f),
                valueRange = DEFAULT.MIN_BRIGHTNESS..DEFAULT.MAX_BRIGHTNESS,
                steps = 0,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = .6f),
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .4f),
                ),
                enabled = !viewModel.autoBrightnessMode

            )
            AppIconButton(
                modifier = modifier.weight(1f),
                imageVector = Icons.Default.Brightness7,
                contentDescription = stringResource(id = R.string.more_brightness)
            )
        }
        OutlinedButton(
            onClick = onToggleAutoBrightness,
            modifier = Modifier
                .weight(1F)
                .padding(8.dp),
            border = BorderStroke(
                ButtonDefaults.OutlinedBorderSize,
                if (viewModel.autoBrightnessMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                backgroundColor = if (viewModel.autoBrightnessMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
            )
        ) {
            CaptionTextComposable(
                modifier = Modifier.align(Alignment.CenterVertically),
                color = if (viewModel.autoBrightnessMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                text = stringResource(R.string.auto),
                style = MaterialTheme.typography.labelSmall,
                maxLine = 1
            )
        }
    }
}
