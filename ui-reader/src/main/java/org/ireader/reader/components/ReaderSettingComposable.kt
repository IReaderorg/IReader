package org.ireader.reader.components

import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import org.ireader.components.components.Components
import org.ireader.components.components.component.ChipChoicePreference
import org.ireader.components.components.component.ChipPreference
import org.ireader.components.components.component.ColorPreference
import org.ireader.components.components.component.PreferenceRow
import org.ireader.components.components.component.SwitchPreference
import org.ireader.components.components.setupUiComponent
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.core_ui.preferences.ReadingMode
import org.ireader.core_ui.theme.fonts
import org.ireader.core_ui.ui.Colour.contentColor
import org.ireader.core_ui.ui.PreferenceAlignment
import org.ireader.reader.viewmodel.ReaderScreenViewModel
import org.ireader.ui_reader.R

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
@Composable
fun ReaderSettingMainLayout(
    modifier: Modifier = Modifier,
    vm: ReaderScreenViewModel,
    onFontSelected: (Int) -> Unit,
    onToggleAutoBrightness: () -> Unit,
    onChangeBrightness: (Float) -> Unit,
    onBackgroundChange: (Int) -> Unit,
    onTextAlign: (PreferenceAlignment) -> Unit
) {
    val pagerState = rememberPagerState()
    val context = LocalContext.current
    val tabs = remember {
        listOf<TabItem>(
            TabItem(
                context.getString(R.string.reader)
            ) {
                val items = listOf<Components>(
                    Components.Slider(
                        preferenceAsInt = vm.fontSize,
                        title = stringResource(id = R.string.font_size),
                        trailing = vm.fontSize.value.toInt().toString(),
                        valueRange = 8.0F..32.0F,
                    ),
                    Components.Header(
                        stringResource(id = R.string.paragraph)
                    ),
                    Components.Slider(
                        preferenceAsInt = vm.paragraphsIndent,
                        title = stringResource(id = R.string.paragraph_indent),
                        trailing = vm.paragraphsIndent.value.toInt().toString(),
                        valueRange = 0.0F..32.0F,
                    ),
                    Components.Slider(
                        preferenceAsInt = vm.distanceBetweenParagraphs,
                        title = stringResource(id = R.string.paragraph_distance),
                        trailing = vm.distanceBetweenParagraphs.value.toInt().toString(),
                        valueRange = 0.0F..8.0F,
                    ),
                    Components.Header(
                        stringResource(id = R.string.line)
                    ),
                    Components.Slider(
                        preferenceAsInt = vm.lineHeight,
                        title = stringResource(id = R.string.line_height),
                        trailing = vm.lineHeight.value.toInt().toString(),
                        valueRange = 22.0F..48.0F,
                    ),
                    Components.Header(
                        stringResource(id = R.string.autoscroll)
                    ),
                    Components.Slider(
                        preferenceAsLong = vm.autoScrollInterval,
                        title = stringResource(id = R.string.interval),
                        trailing = (vm.autoScrollInterval.value / 1000).toInt().toString(),
                        valueRange = 500.0F..10000.0F,
                    ),
                    Components.Slider(
                        preferenceAsInt = vm.autoScrollOffset,
                        title = stringResource(id = R.string.offset),
                        trailing = (vm.autoScrollOffset.value / 1000).toInt().toString(),
                        valueRange = 500.0F..10000F,
                    ),
                    Components.Header(
                        stringResource(id = R.string.scrollIndicator)
                    ),
                    Components.Slider(
                        preferenceAsInt = vm.scrollIndicatorPadding,
                        title = stringResource(id = R.string.padding),
                        trailing = vm.scrollIndicatorPadding.value.toString(),
                        valueRange = 0F..32F,
                    ),
                    Components.Slider(
                        preferenceAsInt = vm.scrollIndicatorWith,
                        title = stringResource(id = R.string.width),
                        trailing = vm.scrollIndicatorWith.value.toString(),
                        valueRange = 0F..32F,
                    ),
                    Components.Chip(
                        preference = listOf(
                            stringResource(id = R.string.right),
                            stringResource(id = R.string.left),
                        ),
                        title = stringResource(id = R.string.alignment),
                        onValueChange = {
                            when (it) {
                                0 -> vm.scrollIndicatorAlignment.value = PreferenceAlignment.Right
                                1 -> vm.scrollIndicatorAlignment.value = PreferenceAlignment.Left
                            }
                        },
                        selected = vm.scrollIndicatorAlignment.value.ordinal
                    ),
                    Components.Header(
                        stringResource(id = R.string.margins)
                    ),
                    Components.Slider(
                        preferenceAsInt = vm.topMargin,
                        title = stringResource(id = R.string.top),
                        trailing = vm.topMargin.value.toString(),
                        valueRange = 0F..200F,
                    ),
                    Components.Slider(
                        preferenceAsInt = vm.bottomMargin,
                        title = stringResource(id = R.string.bottom),
                        trailing = vm.bottomMargin.value.toString(),
                        valueRange = 0F..200F,
                    ),Components.Slider(
                        preferenceAsInt = vm.leftMargin,
                        title = stringResource(id = R.string.left),
                        trailing = vm.leftMargin.value.toString(),
                        valueRange = 0F..200F,
                    ),Components.Slider(
                        preferenceAsInt = vm.rightMargin,
                        title = stringResource(id = R.string.right),
                        trailing = vm.rightMargin.value.toString(),
                        valueRange = 0F..200F,
                    ),Components.Header(
                        stringResource(id = R.string.content_padding)
                    ),Components.Slider(
                        preferenceAsInt = vm.topContentPadding,
                        title = stringResource(id = R.string.top),
                        trailing = vm.topContentPadding.value.toString(),
                        valueRange = 0F..32F,
                    )
                    ,Components.Slider(
                        preferenceAsInt = vm.bottomContentPadding,
                        title = stringResource(id = R.string.bottom),
                        trailing = vm.bottomContentPadding.value.toString(),
                        valueRange = 0F..32F,
                    ),
                )
                LazyColumn(
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        var postion = remember {
                            0
                        }
                        ChipChoicePreference(
                            preference = vm.font,
                            choices = fonts.associate { fontType ->
                                postion++
                                return@associate fontType to fontType.fontName
                            },
                            title = stringResource(id = R.string.font),
                            onFailToFindElement = vm.font.value.fontName
                        )
                    }
                    item {
                        PreferenceRow(
                            title = stringResource(id = R.string.text_align),
                            action = {
                                LazyRow {
                                    item {
                                        AppIconButton(
                                            imageVector = Icons.Default.FormatAlignLeft,
                                            contentDescription = stringResource(R.string.text_align_left),
                                            onClick = {
                                                onTextAlign(PreferenceAlignment.Left)
                                            },
                                            tint = if (vm.textAlignment.value == PreferenceAlignment.Left) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                        )
                                        AppIconButton(
                                            imageVector = Icons.Default.FormatAlignCenter,
                                            contentDescription = stringResource(R.string.text_align_center),
                                            onClick = {
                                                onTextAlign(PreferenceAlignment.Center)
                                            },
                                            tint = if (vm.textAlignment.value == PreferenceAlignment.Center) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                        )
                                        AppIconButton(
                                            imageVector = Icons.Default.FormatAlignJustify,
                                            contentDescription = stringResource(R.string.text_align_justify),
                                            onClick = {
                                                onTextAlign(PreferenceAlignment.Justify)
                                            },
                                            tint = if (vm.textAlignment.value == PreferenceAlignment.Justify) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                        )
                                        AppIconButton(
                                            imageVector = Icons.Default.FormatAlignRight,
                                            contentDescription = stringResource(R.string.text_align_right),
                                            onClick = {
                                                onTextAlign(PreferenceAlignment.Right)
                                            },
                                            tint = if (vm.textAlignment.value == PreferenceAlignment.Right) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        )
                    }
                    setupUiComponent(items)
                }
            },
            TabItem(
                context.getString(R.string.general)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        ChipPreference(
                            preference = listOf(
                                stringResource(id = R.string.page),
                                stringResource(id = R.string.continues),
                            ), selected = vm.readingMode.value.ordinal,
                            onValueChange = {
                                vm.readingMode.value = ReadingMode.valueOf(it)
                            },
                            title = stringResource(id = R.string.scroll_mode)
                        )
                    }
                    item {
                        ChipPreference(
                            preference = listOf(
                                stringResource(id = R.string.horizontal),
                                stringResource(id = R.string.vertical),
                            ), selected = vm.verticalScrolling.value.isTrue(),
                            onValueChange = {
                                vm.verticalScrolling.value = it == 1
                            },
                            title = stringResource(id = R.string.reading_mode)
                        )
                    }
                    item {
                        ChipPreference(
                            preference = listOf(
                                stringResource(id = R.string.landscape),
                                stringResource(id = R.string.portrait),
                            ),
                            selected = vm.orientation.value,
                            onValueChange = {
                                when (it) {
                                    0 -> vm.orientation.value = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                    1 -> vm.orientation.value = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                }
                            },
                            title = stringResource(id = R.string.orientation)
                        )
                    }

                    item {
                        SwitchPreference(
                            preference = vm.autoScrollMode,
                            title = stringResource(id = R.string.autoScroll),
                            onValueChange = { vm.autoScrollMode = it }
                        )
                    }
                    item {
                        SwitchPreference(
                            preference = vm.immersiveMode,
                            title = stringResource(id = R.string.immersive_mode),
                        )
                    }
                    item {
                        SwitchPreference(
                            preference = vm.selectableMode,
                            title = stringResource(id = R.string.selectable_mode),
                        )
                    }
                    item {
                        SwitchPreference(
                            preference = vm.showScrollIndicator,
                            title = stringResource(id = R.string.show_scrollbar),
                        )
                    }
                    item {
                        SwitchPreference(
                            preference = vm.isScrollIndicatorDraggable,
                            title = stringResource(id = R.string.scrollbar_draggable),
                        )
                    }
                }

            },
            TabItem(context.getString(R.string.colors)) {

                LazyColumn(
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    item {
                        SwitchPreference(
                            preference = vm.autoBrightnessMode,
                            title = stringResource(id = R.string.custom_brightness),
                        )
                    }
                    item {
                        BrightnessSliderComposable(
                            viewModel = vm,
                            onChangeBrightness = onChangeBrightness
                        )
                    }
                    item {

                        ReaderBackgroundComposable(
                            viewModel = vm,
                            onBackgroundChange = onBackgroundChange
                        )
                    }
                    item {

                        ColorPreference(
                            preference = vm.backgroundColor,
                            title = stringResource(id = R.string.background_color)
                        )
                    }
                    item {
                        ColorPreference(
                            preference = vm.textColor,
                            title = stringResource(id = R.string.text_color)
                        )

                    }
                    item {
                        ColorPreference(
                            preference = vm.selectedScrollBarColor,
                            title = stringResource(id = R.string.selected_scrollbar_color)
                        )

                    }
                    item {
                        ColorPreference(
                            preference = vm.unselectedScrollBarColor,
                            title = stringResource(id = R.string.unselected_scrollbar_color)
                        )

                    }
                }

            }

        )
    }

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

fun Boolean.isTrue(): Int {
    return if (this) {
        1
    } else {
        0
    }
}

