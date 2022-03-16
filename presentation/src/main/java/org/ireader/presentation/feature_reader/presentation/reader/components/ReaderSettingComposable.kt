package org.ireader.presentation.feature_reader.presentation.reader.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start) {
                IndentChangerComposable(Modifier, viewModel = viewModel)
                FontSizeChangerComposable(viewModel = viewModel)
                ParagraphDistanceComposable(viewModel = viewModel)
                Row(modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Advance Setting",
                        fontSize = 12.sp,
                        style = TextStyle(fontWeight = FontWeight.W400),
                        color = MaterialTheme.colors.onBackground
                    )
                    AppIconButton(imageVector = Icons.Default.Settings,
                        title = "Advance Setting",
                        onClick = { viewModel.scrollIndicatorDialogShown = true })
                }
            }
            Column(modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start) {
                ScrollModeSetting(viewModel)
                FontHeightChangerComposable(viewModel = viewModel)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
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
                AutoScrollSetting(viewModel = viewModel)
            }

        }


    }
}

@Composable
fun ScrollModeSetting(viewModel: ReaderScreenViewModel) {
    Row(modifier = Modifier.fillMaxWidth(),
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

@Composable
fun AutoScrollSetting(viewModel: ReaderScreenViewModel) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Auto Scroll Mode",
            fontSize = 12.sp,
            style = TextStyle(fontWeight = FontWeight.W400),
            color = MaterialTheme.colors.onBackground
        )
        AppIconButton(imageVector = if (viewModel.autpScrollMode) Icons.Default.CheckCircle else Icons.Default.Unpublished,
            title = if (viewModel.autpScrollMode) "Enable" else "Disable",
            onClick = {
                viewModel.toggleAutoScrollMode()
            })
    }
}