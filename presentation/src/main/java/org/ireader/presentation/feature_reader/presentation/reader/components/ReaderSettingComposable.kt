package org.ireader.presentation.feature_reader.presentation.reader.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ireader.domain.view_models.reader.FontSizeEvent
import org.ireader.domain.view_models.reader.Orientation
import org.ireader.domain.view_models.reader.ReaderEvent
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
        Spacer(modifier = Modifier.height(16.dp))
        ReaderBackgroundComposable(viewModel = viewModel)
        Spacer(modifier = Modifier.height(16.dp))
        /** Font indent and font menu **/
        FontChip(
            viewModel = viewModel
        )
        Spacer(modifier = Modifier.height(12.dp))
        SettingItemToggleComposable(text = "Scroll Mode",
            value = viewModel.verticalScrolling,
            onToggle = { viewModel.toggleScrollMode() })

        SettingItemToggleComposable(text = "Orientation",
            value = viewModel.orientation == Orientation.Landscape,
            onToggle = { viewModel.saveOrientation(context) })
        SettingItemToggleComposable(text = "AutoScroll",
            value = viewModel.autpScrollMode,
            onToggle = { viewModel.toggleAutoScrollMode() })
        SettingItemComposable(text = "Font Size",
            value = viewModel.fontSize.toString(),
            onAdd = {
                viewModel.onEvent(ReaderEvent.ChangeFontSize(FontSizeEvent.Increase))
            },
            onMinus = {
                viewModel.onEvent(
                    ReaderEvent.ChangeFontSize(FontSizeEvent.Decrease))

            })
        SettingItemComposable(text = "Paragraph Indent",
            value = viewModel.paragraphsIndent.toString(),
            onAdd = {
                viewModel.saveParagraphIndent(true)

            },
            onMinus = {
                viewModel.saveParagraphIndent(false)
            })

        SettingItemComposable(text = "Paragraph Distance",
            value = viewModel.distanceBetweenParagraphs.toString(),
            onAdd = {
                viewModel.saveParagraphDistance(true)
            },
            onMinus = {
                viewModel.saveParagraphDistance(false)

            })
        SettingItemComposable(text = "Line Height",
            value = viewModel.lineHeight.toString(),
            onAdd = {
                viewModel.saveFontHeight(true)
            },
            onMinus = {
                viewModel.saveFontHeight(false)

            })




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
}

