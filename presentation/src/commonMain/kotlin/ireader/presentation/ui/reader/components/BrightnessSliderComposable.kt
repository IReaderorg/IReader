package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.i18n.DEFAULT
import ireader.i18n.localize

import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel


@Composable
fun BrightnessSliderComposable(
        modifier: Modifier = Modifier,
        viewModel: ReaderScreenViewModel,
        onChangeBrightness: (Float) -> Unit
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
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
                contentDescription = localize { xml -> xml.lessBrightness },
            )
            Slider(
                viewModel.brightness.value,
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
                enabled = !viewModel.autoBrightnessMode.value

            )
            AppIconButton(
                modifier = modifier.weight(1f),
                imageVector = Icons.Default.Brightness7,
                contentDescription = localizeHelper.localize { xml -> xml.moreBrightness }
            )
        }
    }
}
