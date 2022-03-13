package org.ireader.presentation.feature_reader.presentation.reader.components

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
import androidx.compose.ui.platform.LocalContext
import org.ireader.core.utils.DEFAULT
import org.ireader.domain.view_models.reader.ReaderEvent
import org.ireader.domain.view_models.reader.ReaderScreenViewModel

@Composable
fun BrightnessSliderComposable(
    modifier: Modifier = Modifier,
    viewModel: ReaderScreenViewModel,
) {

    val context = LocalContext.current
    Column {
        Text(text = "Brightness", style = MaterialTheme.typography.caption)
        Row(modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically) {
            Icon(modifier = modifier.weight(1f),
                imageVector = Icons.Default.LightMode,
                contentDescription = "less brightness")
            Slider(
                viewModel.brightness,
                onValueChange = { viewModel.onEvent(ReaderEvent.ChangeBrightness(it, context)) },
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
            Icon(modifier = modifier.weight(1f),
                imageVector = Icons.Default.Brightness7,
                contentDescription = "less brightness")
        }
    }

}