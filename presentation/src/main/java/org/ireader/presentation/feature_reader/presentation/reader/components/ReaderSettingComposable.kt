package org.ireader.presentation.feature_reader.presentation.reader.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ireader.domain.view_models.reader.ReaderScreenViewModel
import org.ireader.presentation.presentation.reusable_composable.AppIconButton

@Composable
fun ReaderSettingComposable(modifier: Modifier = Modifier, viewModel: ReaderScreenViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp)
            .verticalScroll(scrollState)
    ) {
        BrightnessSliderComposable(viewModel = viewModel)
        Spacer(modifier = Modifier.height(12.dp))
        ReaderBackgroundComposable(viewModel = viewModel)
        Spacer(modifier = Modifier.height(12.dp))
        /** Font indent and font menu **/
        FontMenuComposable(
            viewModel = viewModel
        )
        Spacer(modifier = Modifier.height(12.dp))
        IndentChangerComposable(viewModel = viewModel)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            FontSizeChangerComposable(viewModel = viewModel)
            Divider(
                color = MaterialTheme.colors.onBackground.copy(alpha = .2f),
                modifier = Modifier
                    .height(20.dp)
                    .width(1.dp)
            )
            FontHeightChangerComposable(viewModel = viewModel)
        }
        Row(modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            ParagraphDistanceComposable(viewModel = viewModel)
            Divider(
                color = MaterialTheme.colors.onBackground.copy(alpha = .2f),
                modifier = Modifier
                    .height(20.dp)
                    .width(1.dp)
            )
            Text(
                text = "Orientation",
                fontSize = 12.sp,
                style = TextStyle(fontWeight = FontWeight.W400),
                color = MaterialTheme.colors.onBackground
            )
            AppIconButton(imageVector = Icons.Default.FlipCameraAndroid,
                title = "Change Orientation",
                onClick = { viewModel.saveOrientation(context) })
        }


    }
}