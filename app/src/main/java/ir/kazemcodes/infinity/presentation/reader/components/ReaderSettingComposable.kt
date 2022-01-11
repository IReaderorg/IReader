package ir.kazemcodes.infinity.presentation.reader.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.kazemcodes.infinity.presentation.reader.ReaderEvent
import ir.kazemcodes.infinity.presentation.reader.ReaderScreenViewModel

@Composable
fun ReaderSettingComposable(modifier: Modifier = Modifier,viewModel: ReaderScreenViewModel) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
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
        Row( modifier = modifier.fillMaxSize()) {
            FontSizeChangerComposable(modifier = modifier.fillMaxWidth(.5f), viewModel=viewModel)
            FontHeightChangerComposable(modifier.fillMaxWidth(.5f),viewModel = viewModel)
        }


    }
}