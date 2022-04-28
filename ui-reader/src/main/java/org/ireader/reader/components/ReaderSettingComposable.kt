package org.ireader.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.reader.viewmodel.Orientation
import org.ireader.reader.viewmodel.ReaderScreenPreferencesState

@Composable
fun ReaderSettingComposable(
    modifier: Modifier = Modifier,
    vm: ReaderScreenPreferencesState,
    onFontSelected: (Int) -> Unit,
    onToggleScrollMode: (Boolean) -> Unit,
    onToggleAutoScroll: (Boolean) -> Unit,
    onToggleOrientation: (Boolean) -> Unit,
    onToggleImmersiveMode: (Boolean) -> Unit,
    onToggleSelectedMode: (Boolean) -> Unit,
    onFontSizeIncrease: (Boolean) -> Unit,
    onParagraphIndentIncrease: (Boolean) -> Unit,
    onParagraphDistanceIncrease: (Boolean) -> Unit,
    onLineHeightIncrease: (Boolean) -> Unit,
    onAutoscrollIntervalIncrease: (Boolean) -> Unit,
    onAutoscrollOffsetIncrease: (Boolean) -> Unit,
    onScrollIndicatorPaddingIncrease: (Boolean) -> Unit,
    onScrollIndicatorWidthIncrease: (Boolean) -> Unit,
    onToggleAutoBrightness: () -> Unit,
    onChangeBrightness: (Float) -> Unit,
    onBackgroundChange: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp)
            .verticalScroll(scrollState)
    ) {
        BrightnessSliderComposable(
            viewModel = vm,
            onToggleAutoBrightness = onToggleAutoBrightness,
            onChangeBrightness = onChangeBrightness
        )
        Spacer(modifier = Modifier.height(16.dp))
        ReaderBackgroundComposable(viewModel = vm, onBackgroundChange = onBackgroundChange)
        Spacer(modifier = Modifier.height(16.dp))
        /** Font indent and font menu **/
        FontChip(
            state = vm,
            onFontSelected = onFontSelected
        )
        Spacer(modifier = Modifier.height(12.dp))
        SettingItemToggleComposable(
            text = "Scroll Mode",
            value = vm.verticalScrolling,
            onToggle = onToggleScrollMode
        )

        SettingItemToggleComposable(
            text = "Orientation",
            value = vm.orientation == Orientation.Landscape,
            onToggle = onToggleOrientation
        )
        SettingItemToggleComposable(
            text = "AutoScroll",
            value = vm.autoScrollMode,
            onToggle = onToggleAutoScroll
        )
        SettingItemToggleComposable(
            text = "Immersive mode",
            value = vm.immersiveMode,
            onToggle = onToggleImmersiveMode
        )
        SettingItemToggleComposable(
            text = "Selectable mode",
            value = vm.selectableMode,
            onToggle = onToggleSelectedMode
        )
        SettingItemComposable(
            text = "Font Size",
            value = vm.fontSize.toString(),
            onAdd = {
                onFontSizeIncrease(true)
            },
            onMinus = {
                onFontSizeIncrease(false)
            }
        )
        SettingItemComposable(
            text = "Paragraph Indent",
            value = vm.paragraphsIndent.toString(),
            onAdd = {
                onParagraphIndentIncrease(true)
            },
            onMinus = {
                onParagraphIndentIncrease(false)
            }
        )

        SettingItemComposable(
            text = "Paragraph Distance",
            value = vm.distanceBetweenParagraphs.toString(),
            onAdd = {
                onParagraphDistanceIncrease(true)
            },
            onMinus = {
                onParagraphDistanceIncrease(false)
            }
        )
        SettingItemComposable(
            text = "Line Height",
            value = vm.lineHeight.toString(),
            onAdd = {
                onLineHeightIncrease(true)
            },
            onMinus = {
                onLineHeightIncrease(false)
            }
        )
        SettingItemComposable(
            text = "Autoscroll Interval",
            value = "${vm.autoScrollInterval / 1000} second",
            onAdd = {
                onAutoscrollIntervalIncrease(true)
            },
            onMinus = {
                onAutoscrollIntervalIncrease(false)
            }
        )
        SettingItemComposable(
            text = "Autoscroll Offset",
            value = vm.autoScrollOffset.toString(),
            onAdd = {
                onAutoscrollOffsetIncrease(true)
            },
            onMinus = {
                onAutoscrollOffsetIncrease(false)
            }
        )
        SettingItemComposable(
            text = "ScrollIndicator Padding",
            value = vm.scrollIndicatorPadding.toString(),
            onAdd = {
                onScrollIndicatorPaddingIncrease(true)
            },
            onMinus = {
                onScrollIndicatorPaddingIncrease(false)
            }
        )
        SettingItemComposable(
            text = "ScrollIndicator Width",
            value = vm.scrollIndicatorWith.toString(),
            onAdd = {
                onScrollIndicatorWidthIncrease(true)
            },
            onMinus = {
                onScrollIndicatorWidthIncrease(false)
            }
        )
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Advance Setting",
                fontSize = 12.sp,
                style = TextStyle(fontWeight = FontWeight.W400),
                color = MaterialTheme.colors.onBackground
            )
            AppIconButton(
                imageVector = Icons.Default.Settings,
                title = "Advance Setting",
                onClick = { vm.scrollIndicatorDialogShown = true }
            )
        }
    }
}
