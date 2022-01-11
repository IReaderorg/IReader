package ir.kazemcodes.infinity.presentation.reader.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.kazemcodes.infinity.presentation.reader.ReaderEvent
import ir.kazemcodes.infinity.presentation.reader.ReaderScreenViewModel

@Composable
fun ReaderSettingComposable(modifier: Modifier = Modifier,viewModel: ReaderScreenViewModel) {

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        BrightnessSliderComposable(brightness = viewModel.state.value.brightness) {
            viewModel.onEvent(ReaderEvent.ChangeBrightness(it))
        }
        Spacer(modifier = Modifier.height(12.dp))
        ReaderBackgroundComposable(viewModel = viewModel)
        Spacer(modifier = Modifier.height(12.dp))
        FontMenuComposable(
            viewModel = viewModel
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier=modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            FontSizeChangerComposable( viewModel=viewModel)
            Divider(
                color = MaterialTheme.colors.onBackground.copy(alpha = .2f),
                modifier = Modifier
                    .height(20.dp)
                    .width(1.dp)
            )
            FontHeightChangerComposable(viewModel = viewModel)
        }
        ParagraphDistanceComposable(viewModel = viewModel)


    }
}