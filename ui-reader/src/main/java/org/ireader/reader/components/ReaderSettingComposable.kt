package org.ireader.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import org.ireader.common_resources.UiText
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.core_ui.asString
import org.ireader.core_ui.ui.Colour.contentColor
import org.ireader.core_ui.ui.TextAlign
import org.ireader.reader.viewmodel.Orientation
import org.ireader.reader.viewmodel.ReaderScreenPreferencesState
import org.ireader.ui_reader.R

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
@Composable
fun ReaderSettingMainLayout(
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
    onBackgroundChange: (Int) -> Unit,
    onShowScrollIndicator: (Boolean) -> Unit,
    onTextAlign: (TextAlign) -> Unit
) {
    val pagerState = rememberPagerState()

    val tabs = listOf<TabItem>(
        TabItem(
            UiText.StringResource(R.string.reader)
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
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.width(100.dp),
                        text = UiText.StringResource(R.string.text_align).asString(),
                        fontSize = 12.sp,
                        style = TextStyle(fontWeight = FontWeight.W400),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIconButton(
                            imageVector = Icons.Default.FormatAlignLeft,
                            text = UiText.StringResource(R.string.text_align_left),
                            onClick = {
                                onTextAlign(TextAlign.Left)
                            },
                            tint = if (vm.textAlignment == TextAlign.Left) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                        AppIconButton(
                            imageVector = Icons.Default.FormatAlignCenter,
                            text = UiText.StringResource(R.string.text_align_center),
                            onClick = {
                                onTextAlign(TextAlign.Center)
                            },
                            tint = if (vm.textAlignment == TextAlign.Center) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                        AppIconButton(
                            imageVector = Icons.Default.FormatAlignJustify,
                            text = UiText.StringResource(R.string.text_align_justify),
                            onClick = {
                                onTextAlign(TextAlign.Justify)
                            },
                            tint = if (vm.textAlignment == TextAlign.Justify) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                        AppIconButton(
                            imageVector = Icons.Default.FormatAlignRight,
                            text = UiText.StringResource(R.string.text_align_right),
                            onClick = {
                                onTextAlign(TextAlign.Right)
                            },
                            tint = if (vm.textAlignment == TextAlign.Right) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )

                    }

                }
                SettingItemComposable(
                    text =UiText.StringResource(R.string.font_size),
                    value = UiText.DynamicString(vm.fontSize.toString()),
                    onAdd = {
                        onFontSizeIncrease(true)
                    },
                    onMinus = {
                        onFontSizeIncrease(false)
                    }
                )
                SettingItemComposable(
                    text = UiText.StringResource(R.string.paragraph_indent),
                    value = UiText.DynamicString(vm.paragraphsIndent.toString()),
                    onAdd = {
                        onParagraphIndentIncrease(true)
                    },
                    onMinus = {
                        onParagraphIndentIncrease(false)
                    }
                )

                SettingItemComposable(
                    text = UiText.StringResource(R.string.paragraph_distance),
                    value = UiText.DynamicString(vm.distanceBetweenParagraphs.toString()),
                    onAdd = {
                        onParagraphDistanceIncrease(true)
                    },
                    onMinus = {
                        onParagraphDistanceIncrease(false)
                    }
                )
                SettingItemComposable(
                    text = UiText.StringResource(R.string.line_height),
                    value = UiText.DynamicString(vm.lineHeight.toString()),
                    onAdd = {
                        onLineHeightIncrease(true)
                    },
                    onMinus = {
                        onLineHeightIncrease(false)
                    }
                )
                SettingItemComposable(
                    text = UiText.StringResource(R.string.autoscroll_interval),
                    value = UiText.DynamicString("${vm.autoScrollInterval / 1000} second"),
                    onAdd = {
                        onAutoscrollIntervalIncrease(true)
                    },
                    onMinus = {
                        onAutoscrollIntervalIncrease(false)
                    }
                )
                SettingItemComposable(
                    text = UiText.StringResource(R.string.autoscroll_offset),
                    value = UiText.DynamicString(vm.autoScrollOffset.toString()),
                    onAdd = {
                        onAutoscrollOffsetIncrease(true)
                    },
                    onMinus = {
                        onAutoscrollOffsetIncrease(false)
                    }
                )
                SettingItemComposable(
                    text = UiText.StringResource(R.string.scrollIndicator_padding),
                    value = UiText.DynamicString(vm.scrollIndicatorPadding.toString()),
                    onAdd = {
                        onScrollIndicatorPaddingIncrease(true)
                    },
                    onMinus = {
                        onScrollIndicatorPaddingIncrease(false)
                    }
                )
                SettingItemComposable(
                    text = UiText.StringResource(R.string.scrollIndicator_width),
                    value = UiText.DynamicString(vm.scrollIndicatorWith.toString()),
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
                        text = UiText.StringResource(R.string.advance_setting).asString(),
                        fontSize = 12.sp,
                        style = TextStyle(fontWeight = FontWeight.W400),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    AppIconButton(
                        imageVector = Icons.Default.Settings,
                        text = UiText.StringResource(R.string.advance_setting),
                        onClick = { vm.scrollIndicatorDialogShown = true }
                    )
                }
            }

        },
        TabItem(
            UiText.StringResource(R.string.general)
        ) {
            Column(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SettingItemToggleComposable(
                    text = UiText.StringResource(R.string.scroll_mode),
                    value = vm.verticalScrolling,
                    onToggle = onToggleScrollMode
                )
                SettingItemToggleComposable(
                    text = UiText.StringResource(R.string.show_scrollbar),
                    value = vm.showScrollIndicator,
                    onToggle = onShowScrollIndicator
                )

                SettingItemToggleComposable(
                    text = UiText.StringResource(R.string.orientation),
                    value = vm.orientation == Orientation.Landscape,
                    onToggle = onToggleOrientation
                )
                SettingItemToggleComposable(
                    text = UiText.StringResource(R.string.autoScroll),
                    value = vm.autoScrollMode,
                    onToggle = onToggleAutoScroll
                )
                SettingItemToggleComposable(
                    text = UiText.StringResource(R.string.immersive_mode),
                    value = vm.immersiveMode,
                    onToggle = onToggleImmersiveMode
                )
                SettingItemToggleComposable(
                    text = UiText.StringResource(R.string.selectable_mode),
                    value = vm.selectableMode,
                    onToggle = onToggleSelectedMode
                )
            }

        },

        )

    /** There is Some issue here were sheet content is not need , not sure why**/
    Column(modifier = Modifier.fillMaxSize()) {
        Tabs(libraryTabs = tabs, pagerState = pagerState)
        TabsContent(
            libraryTabs = tabs,
            pagerState = pagerState,
        )
    }
}

data class TabItem(
    val name: UiText,
    val screen: @Composable () -> Unit
)

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun Tabs(libraryTabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.contentColor,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                color = MaterialTheme.colorScheme.primary,

                )
        }
    ) {
        libraryTabs.forEachIndexed { index, tab ->
            Tab(
                text = { MidSizeTextComposable(text = tab.name) },
                selected = pagerState.currentPage == index,
                unselectedContentColor = MaterialTheme.colorScheme.onBackground,
                selectedContentColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
            )
        }
    }
}

@ExperimentalPagerApi
@Composable
fun TabsContent(
    libraryTabs: List<TabItem>,
    pagerState: PagerState,
) {
    HorizontalPager(
        count = libraryTabs.size,
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        libraryTabs[page].screen()
    }
}

