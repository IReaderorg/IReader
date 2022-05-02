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
import androidx.compose.material.MaterialTheme
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
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.core_ui.ui.Colour.contentColor
import org.ireader.core_ui.ui.TextAlign
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
    onBackgroundChange: (Int) -> Unit,

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

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
@Composable
fun ReaderSettingMainLayout(    modifier: Modifier = Modifier,
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
    onShowScrollIndicator : (Boolean) -> Unit,
    onTextAlign: (TextAlign) -> Unit
) {
    val pagerState = rememberPagerState()
    val tabs = listOf<TabItem>(
        TabItem(
            "Reader"
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
                        text = "Text Align",
                        fontSize = 12.sp,
                        style = TextStyle(fontWeight = FontWeight.W400),
                        color = MaterialTheme.colors.onBackground
                    )
                    Row(
                        modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIconButton(
                            imageVector = Icons.Default.FormatAlignLeft,
                            title = "ext Align Left",
                            onClick = {
                                onTextAlign(TextAlign.Left)
                            },
                            tint = if ( vm.textAlignment == TextAlign.Left) MaterialTheme.colors.primary else MaterialTheme.colors.onBackground
                        )
                        AppIconButton(
                            imageVector = Icons.Default.FormatAlignCenter,
                            title = "Text Align Center",
                            onClick = {
                                onTextAlign(TextAlign.Center)
                            },
                            tint = if ( vm.textAlignment == TextAlign.Center) MaterialTheme.colors.primary else MaterialTheme.colors.onBackground
                        )
                        AppIconButton(
                            imageVector = Icons.Default.FormatAlignJustify,
                            title = "Text Align Justify",
                            onClick = {
                                onTextAlign(TextAlign.Justify)
                            },
                            tint = if ( vm.textAlignment == TextAlign.Justify) MaterialTheme.colors.primary else MaterialTheme.colors.onBackground
                        )
                        AppIconButton(
                            imageVector = Icons.Default.FormatAlignRight,
                            title = "Text Align Right",
                            onClick = {
                                onTextAlign(TextAlign.Right)
                            },
                            tint = if ( vm.textAlignment == TextAlign.Right) MaterialTheme.colors.primary else MaterialTheme.colors.onBackground
                        )

                    }

                }
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

        },
        TabItem(
            "General"
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                SettingItemToggleComposable(
                    text = "Scroll Mode",
                    value = vm.verticalScrolling,
                    onToggle = onToggleScrollMode
                )
                SettingItemToggleComposable(
                    text = "Show Scrollbar",
                    value = vm.showScrollIndicator,
                    onToggle = onShowScrollIndicator
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
    val name: String,
    val screen: @Composable () -> Unit
)

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun Tabs(libraryTabs: List<TabItem>, pagerState: PagerState) {
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.contentColor,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                color = MaterialTheme.colors.primary,

                )
        }
    ) {
        libraryTabs.forEachIndexed { index, tab ->
            Tab(
                text = { MidSizeTextComposable(text = tab.name) },
                selected = pagerState.currentPage == index,
                unselectedContentColor = MaterialTheme.colors.onBackground,
                selectedContentColor = MaterialTheme.colors.primary,
                onClick = {
                          //TODO need to uncomment this on compose next update
                    //scope.launch { pagerState.animateScrollToPage(index) }
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

