package ireader.presentation.ui.reader.components

import android.content.Context
import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.models.theme.ReaderTheme
import ireader.domain.preferences.models.FontType
import ireader.domain.preferences.prefs.ReadingMode
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.UiText
import ireader.presentation.R
import ireader.presentation.ui.component.components.Build
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.component.ChipChoicePreference
import ireader.presentation.ui.component.components.component.ChipPreference
import ireader.presentation.ui.component.components.component.ColorPreference
import ireader.presentation.ui.component.components.component.PreferenceRow
import ireader.presentation.ui.component.components.component.SwitchPreference
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.theme.ReaderTheme
import ireader.presentation.ui.core.ui.Colour.contentColor
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
@Composable
fun ReaderSettingMainLayout(
    modifier: Modifier = Modifier,
    vm: ReaderScreenViewModel,
    onFontSelected: (Int) -> Unit,
    onToggleAutoBrightness: () -> Unit,
    onChangeBrightness: (Float) -> Unit,
    onBackgroundChange: (themeId: Long) -> Unit,
    onTextAlign: (PreferenceValues.PreferenceTextAlignment) -> Unit
) {
    val pagerState = rememberPagerState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val readerTab: TabItem = remember {
        TabItem(
            context.getString(R.string.reader)
        ) {
            ReaderScreenTab(vm, onTextAlign)
        }
    }
    val generalTab: TabItem = remember {
        TabItem(
            context.getString(R.string.general)
        ) {
            GeneralScreenTab(vm)
        }
    }


    val colorTabItem = remember {
        TabItem(context.getString(R.string.colors)) {
            ColorScreenTab(vm, context, onChangeBrightness, onBackgroundChange)
        }
    }

    val tabs = remember {
        listOf<TabItem>(
            readerTab,
            generalTab,
            colorTabItem
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


@Composable
private fun ReaderScreenTab(
    vm: ReaderScreenViewModel,
    onTextAlign: (PreferenceValues.PreferenceTextAlignment) -> Unit
) {
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
                choices = vm.fonts.map { FontType(it, FontFamily.Default) }
                    .associate { fontType ->
                        postion++
                        return@associate fontType to fontType.name
                    },
                title = stringResource(id = R.string.font),
                onFailToFindElement = vm.font.value.name
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
                                    onTextAlign(PreferenceValues.PreferenceTextAlignment.Left)
                                },
                                tint = if (vm.textAlignment.value == PreferenceValues.PreferenceTextAlignment.Left) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                            AppIconButton(
                                imageVector = Icons.Default.FormatAlignCenter,
                                contentDescription = stringResource(R.string.text_align_center),
                                onClick = {
                                    onTextAlign(PreferenceValues.PreferenceTextAlignment.Center)
                                },
                                tint = if (vm.textAlignment.value == PreferenceValues.PreferenceTextAlignment.Center) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                            AppIconButton(
                                imageVector = Icons.Default.FormatAlignJustify,
                                contentDescription = stringResource(R.string.text_align_justify),
                                onClick = {
                                    onTextAlign(PreferenceValues.PreferenceTextAlignment.Justify)
                                },
                                tint = if (vm.textAlignment.value == PreferenceValues.PreferenceTextAlignment.Justify) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                            AppIconButton(
                                imageVector = Icons.Default.FormatAlignRight,
                                contentDescription = stringResource(R.string.text_align_right),
                                onClick = {
                                    onTextAlign(PreferenceValues.PreferenceTextAlignment.Right)
                                },
                                tint = if (vm.textAlignment.value == PreferenceValues.PreferenceTextAlignment.Right) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            )
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.fontSize,
                title = stringResource(id = R.string.font_size),
                trailing = vm.fontSize.value.toInt().toString(),
                valueRange = 8.0F..32.0F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.textWeight,
                title = stringResource(id = R.string.font_weight),
                trailing = vm.textWeight.value.toInt().toString(),
                valueRange = 1f..900F,
            ).Build()
        }
        item {
            Components.Header(
                stringResource(id = R.string.paragraph)
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.paragraphsIndent,
                title = stringResource(id = R.string.paragraph_indent),
                trailing = vm.paragraphsIndent.value.toInt().toString(),
                valueRange = 0.0F..32.0F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.distanceBetweenParagraphs,
                title = stringResource(id = R.string.paragraph_distance),
                trailing = vm.distanceBetweenParagraphs.value.toInt().toString(),
                valueRange = 0.0F..8.0F,
            ).Build()
        }
        item {
            Components.Header(
                stringResource(id = R.string.line)
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.lineHeight,
                title = stringResource(id = R.string.line_height),
                trailing = vm.lineHeight.value.toInt().toString(),
                valueRange = 22.0F..48.0F,
            ).Build()
        }
        item {
            Components.Header(
                stringResource(id = R.string.autoscroll)
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsLong = vm.autoScrollInterval,
                title = stringResource(id = R.string.interval),
                trailing = (vm.autoScrollInterval.value / 1000).toInt().toString(),
                valueRange = 500.0F..10000.0F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.autoScrollOffset,
                title = stringResource(id = R.string.offset),
                trailing = (vm.autoScrollOffset.value / 1000).toInt().toString(),
                valueRange = 500.0F..10000F,
            ).Build()

        }
        item {
            Components.Header(
                stringResource(id = R.string.scrollIndicator)
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.scrollIndicatorPadding,
                title = stringResource(id = R.string.padding),
                trailing = vm.scrollIndicatorPadding.value.toString(),
                valueRange = 0F..32F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.scrollIndicatorWith,
                title = stringResource(id = R.string.width),
                trailing = vm.scrollIndicatorWith.value.toString(),
                valueRange = 0F..32F,
            ).Build()
        }
        item {
            Components.Chip(
                preference = listOf(
                    stringResource(id = R.string.right),
                    stringResource(id = R.string.left),
                ),
                title = stringResource(id = R.string.alignment),
                onValueChange = {
                    when (it) {
                        0 -> vm.scrollIndicatorAlignment.value =
                            PreferenceValues.PreferenceTextAlignment.Right
                        1 -> vm.scrollIndicatorAlignment.value =
                            PreferenceValues.PreferenceTextAlignment.Left
                    }
                },
                selected = vm.scrollIndicatorAlignment.value.ordinal
            ).Build()
        }
        item {
            Components.Header(
                stringResource(id = R.string.margins)
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.topMargin,
                title = stringResource(id = R.string.top),
                trailing = vm.topMargin.value.toString(),
                valueRange = 0F..200F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.bottomMargin,
                title = stringResource(id = R.string.bottom),
                trailing = vm.bottomMargin.value.toString(),
                valueRange = 0F..200F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.leftMargin,
                title = stringResource(id = R.string.left),
                trailing = vm.leftMargin.value.toString(),
                valueRange = 0F..200F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.rightMargin,
                title = stringResource(id = R.string.right),
                trailing = vm.rightMargin.value.toString(),
                valueRange = 0F..200F,
            ).Build()
        }
        item {
            Components.Header(
                stringResource(id = R.string.content_padding)
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.topContentPadding,
                title = stringResource(id = R.string.top),
                trailing = vm.topContentPadding.value.toString(),
                valueRange = 0F..32F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.bottomContentPadding,
                title = stringResource(id = R.string.bottom),
                trailing = vm.bottomContentPadding.value.toString(),
                valueRange = 0F..32F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.betweenLetterSpaces,
                title = stringResource(id = R.string.letter),
                trailing = vm.betweenLetterSpaces.value.toString(),
                valueRange = 0F..32F,
            ).Build()
        }
        item {
            Components.Space.Build()
        }


    }
}


@Composable
fun GeneralScreenTab(
    vm: ReaderScreenViewModel,
) {
    val scope = rememberCoroutineScope()
    LazyColumn(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ChipChoicePreference(
                preference = vm.translatorOriginLanguage,
                choices = vm.translationEnginesManager.get().supportedLanguages.associate { it.first to it.second },
                title = stringResource(
                    id = R.string.origin_language
                )
            )
        }
        item {
            ChipChoicePreference(
                preference = vm.translatorTargetLanguage,
                choices = vm.translationEnginesManager.get().supportedLanguages.associate { it.first to it.second },
                title = stringResource(
                    id = R.string.target_language
                )
            )
        }
        item {
            PreferenceRow(title = "translate", onClick = {
                scope.launch {
                    vm.translate()
                }
            })
        }
        item {
            ChipPreference(
                preference = listOf(
                    stringResource(id = R.string.page),
                    stringResource(id = R.string.continues),
                ),
                selected = vm.readingMode.value.ordinal,
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
                ),
                selected = vm.verticalScrolling.value.isTrue(),
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
                        0 ->
                            vm.orientation.value =
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        1 ->
                            vm.orientation.value =
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                },
                title = stringResource(id = R.string.orientation)
            )
        }
        item {
            ChipPreference(
                preference = listOf(
                    stringResource(id = R.string.full),
                    stringResource(id = R.string.partial),
                    stringResource(id = R.string.disable),
                ),
                selected = vm.isScrollIndicatorDraggable.value.ordinal,
                onValueChange = {
                    vm.isScrollIndicatorDraggable.value =
                        PreferenceValues.ScrollbarSelectionMode.valueOf(it)
                },
                title = stringResource(id = R.string.scrollbar_mode)
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
                preference = vm.webViewIntegration,
                title = stringResource(id = R.string.show_webView_during_fetching),
            )
        }
        item {
            SwitchPreference(
                preference = vm.screenAlwaysOn,
                title = stringResource(id = R.string.screen_always_on),
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
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun ColorScreenTab(
    vm: ReaderScreenViewModel,
    context: Context,
    onChangeBrightness: (Float) -> Unit,
    onBackgroundChange: (themeId: Long) -> Unit,
) {
    val scope = rememberCoroutineScope()
    LazyColumn(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
    ) {
        item {
            Components.Dynamic {
                Spacer(modifier = Modifier.height(16.dp))
            }.Build()
        }
        item {
            Components.Switch(
                preference = vm.autoBrightnessMode,
                title = context.getString(R.string.custom_brightness),
            ).Build()
        }
        item {
            Components.Dynamic {
                BrightnessSliderComposable(
                    viewModel = vm,
                    onChangeBrightness = onChangeBrightness
                )
            }.Build()
        }
        item {
            Components.Dynamic {
                ReaderBackgroundComposable(
                    viewModel = vm,
                    onBackgroundChange = { id ->
                        onBackgroundChange(id)
                        vm.readerThemeSavable = false
                    },
                    themes = vm.readerColors,
                )
            }.Build()
        }
        item {
            Components.Dynamic {
                ColorPreference(
                    preference = vm.backgroundColor,
                    title = stringResource(id = R.string.background_color),
                    onChangeColor = {
                        vm.readerThemeSavable = true
                    }
                )
            }.Build()
        }
        item {
            Components.Dynamic {
                ColorPreference(
                    preference = vm.textColor,
                    title = stringResource(id = R.string.text_color),
                    onChangeColor = {
                        vm.readerThemeSavable = true
                    }
                )
            }.Build()
        }
        item {
            Components.Dynamic {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (vm.readerThemeSavable) {
                        TextButton(onClick = {
                            vm.readerThemeSavable = false
                            scope.launchIO {
                                vm.readerThemeRepository.insert(
                                    ReaderTheme(
                                        backgroundColor = vm.backgroundColor.value.toArgb(),
                                        onTextColor = vm.textColor.value.toArgb(),
                                    )
                                )
                                vm.showSnackBar(UiText.StringResource(R.string.theme_was_saved))
                            }
                        }) {
                            MidSizeTextComposable(text = stringResource(id = R.string.save_custom_theme))
                        }
                    } else if (!vm.readerTheme.value.isDefault) {
                        TextButton(onClick = {
                            scope.launchIO {
                                vm.readerThemeRepository.delete(
                                    vm.readerTheme.value.ReaderTheme()
                                )
                                vm.showSnackBar(UiText.StringResource(R.string.theme_was_deleted))
                            }
                        }) {
                            MidSizeTextComposable(text = stringResource(id = R.string.delete_custom_theme))
                        }
                    }
                }
            }.Build()
        }
        item {
            Components.Dynamic {
                ColorPreference(
                    preference = vm.selectedScrollBarColor,
                    title = stringResource(id = R.string.selected_scrollbar_color)
                )
            }.Build()
        }
        item {
            Components.Dynamic {
                ColorPreference(
                    preference = vm.unselectedScrollBarColor,
                    title = stringResource(id = R.string.unselected_scrollbar_color)
                )
            }.Build()
        }
        item {
            Components.Space.Build()
        }

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
