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
import org.ireader.presentation.feature_reader.presentation.reader.viewmodel.FontSizeEvent
import org.ireader.presentation.feature_reader.presentation.reader.viewmodel.Orientation
import org.ireader.presentation.feature_reader.presentation.reader.viewmodel.ReaderEvent
import org.ireader.presentation.feature_reader.presentation.reader.viewmodel.ReaderScreenViewModel
import org.ireader.presentation.presentation.reusable_composable.AppIconButton

@Composable
fun ReaderSettingComposable(modifier: Modifier = Modifier, viewModel: ReaderScreenViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val func = viewModel.prefFunc
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
            onToggle = {
                func.apply {
                    viewModel.toggleScrollMode()
                }
            })

        SettingItemToggleComposable(text = "Orientation",
            value = viewModel.orientation == Orientation.Landscape,
            onToggle = {
                func.apply {
                    viewModel.saveOrientation(context)
                }
            })
        SettingItemToggleComposable(text = "AutoScroll",
            value = viewModel.autoScrollMode,
            onToggle = {
                func.apply {
                    viewModel.toggleAutoScrollMode()
                }
            })
        SettingItemToggleComposable(text = "Immersive mode",
            value = viewModel.immersiveMode,
            onToggle = {
                func.apply {
                    viewModel.toggleImmersiveMode(context)
                }
            })
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
                func.apply {
                    viewModel.saveParagraphIndent(true)
                }

            },
            onMinus = {
                func.apply {
                    viewModel.saveParagraphIndent(false)
                }
            })

        SettingItemComposable(text = "Paragraph Distance",
            value = viewModel.distanceBetweenParagraphs.toString(),
            onAdd = {
                func.apply {
                    viewModel.saveParagraphDistance(true)
                }
            },
            onMinus = {
                func.apply {
                    viewModel.saveParagraphDistance(false)
                }
            })
        SettingItemComposable(text = "Line Height",
            value = viewModel.lineHeight.toString(),
            onAdd = {
                func.apply {
                    viewModel.saveFontHeight(true)
                }
            },
            onMinus = {
                func.apply {
                    viewModel.saveFontHeight(false)
                }


            })
        SettingItemComposable(text = "Autoscroll Interval",
            value = "${viewModel.autoScrollInterval / 1000} second",
            onAdd = {
                func.apply {
                    viewModel.setAutoScrollIntervalReader(true)
                }
            },
            onMinus = {
                func.apply {
                    viewModel.setAutoScrollIntervalReader(false)
                }

            })
        SettingItemComposable(text = "Autoscroll Offset",
            value = viewModel.autoScrollOffset.toString(),
            onAdd = {
                func.apply {
                    viewModel.setAutoScrollIntervalReader(true)
                }
            },
            onMinus = {
                func.apply {
                    viewModel.setAutoScrollIntervalReader(false)
                }


            })
        SettingItemComposable(text = "ScrollIndicator Padding",
            value = viewModel.scrollIndicatorPadding.toString(),
            onAdd = {
                func.apply {
                    viewModel.saveScrollIndicatorPadding(true)
                }
            },
            onMinus = {
                func.apply {
                    viewModel.saveScrollIndicatorPadding(false)
                }

            })
        SettingItemComposable(text = "ScrollIndicator Width",
            value = viewModel.scrollIndicatorWith.toString(),
            onAdd = {
                func.apply {
                    viewModel.saveScrollIndicatorWidth(true)
                }
            },
            onMinus = {
                func.apply {
                    viewModel.saveScrollIndicatorWidth(false)
                }

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

