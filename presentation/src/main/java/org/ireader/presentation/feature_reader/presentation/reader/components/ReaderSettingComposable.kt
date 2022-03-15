package org.ireader.presentation.feature_reader.presentation.reader.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.HorizontalDistribute
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.VerticalDistribute
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
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier,
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start) {
                IndentChangerComposable(Modifier, viewModel = viewModel)
                FontSizeChangerComposable(viewModel = viewModel)
                ParagraphDistanceComposable(viewModel = viewModel)
                Row(modifier = Modifier,
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Indicator",
                        fontSize = 12.sp,
                        style = TextStyle(fontWeight = FontWeight.W400),
                        color = MaterialTheme.colors.onBackground
                    )
                    AppIconButton(imageVector = Icons.Default.SquareFoot,
                        title = "Change Indicator",
                        onClick = { viewModel.scrollIndicatorDialogShown = true })
                }
            }
            Column(modifier = Modifier,
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start) {
                ScrollModeSetting(viewModel)
                FontHeightChangerComposable(viewModel = viewModel)
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically) {
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


    }
}

@Composable
fun ScrollModeSetting(viewModel: ReaderScreenViewModel) {
    Row(modifier = Modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Scrolling Mode",
            fontSize = 12.sp,
            style = TextStyle(fontWeight = FontWeight.W400),
            color = MaterialTheme.colors.onBackground
        )
        AppIconButton(imageVector = if (viewModel.verticalScrolling) Icons.Default.HorizontalDistribute else Icons.Default.VerticalDistribute,
            title = if (viewModel.verticalScrolling) "Vertical Mode" else "Horizontal ",
            onClick = {
                viewModel.toggleScrollMode()
            })
    }
}