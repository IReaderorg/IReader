package ireader.presentation.ui.reader.components

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRow
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.models.theme.ReaderTheme
import ireader.domain.preferences.models.FontType
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReadingMode
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.UiText
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.components.Build
import ireader.presentation.ui.component.components.ChipChoicePreference
import ireader.presentation.ui.component.components.ChipPreference
import ireader.presentation.ui.component.components.ColorPreference
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.PreferenceRow
import ireader.presentation.ui.component.components.SwitchPreference
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.theme.ReaderTheme
import ireader.presentation.ui.core.ui.Colour.contentColor
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
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
    val pagerState = androidx.compose.foundation.pager.rememberPagerState()
    val localizeHelper = LocalLocalizeHelper.currentOrThrow

    val readerTab: TabItem = remember {
        TabItem(
            localizeHelper.localize(MR.strings.reader)
        ) {
            ReaderScreenTab(vm, onTextAlign)
        }
    }
    val generalTab: TabItem = remember {
        TabItem(
            localizeHelper.localize(MR.strings.general)
        ) {
            GeneralScreenTab(vm)
        }
    }


    val colorTabItem = remember {
        TabItem(localizeHelper.localize(MR.strings.colors)) {
            ColorScreenTab(vm, onChangeBrightness, onBackgroundChange)
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
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
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
            vm.font?.let { font ->
                ChipChoicePreference(
                        preference = font,
                        choices = vm.fonts.map { FontType(it, FontFamily.Default) }
                                .associate { fontType ->
                                    postion++
                                    return@associate fontType to fontType.name
                                },
                        title = localizeHelper.localize(MR.strings.font),
                        onFailToFindElement = vm.font?.value?.name ?: ""
                )
            }

        }
        item {
            PreferenceRow(
                title = localizeHelper.localize(MR.strings.text_align),
                action = {
                    LazyRow {
                        item {
                            AppIconButton(
                                imageVector = Icons.Default.FormatAlignLeft,
                                contentDescription = localize(MR.strings.text_align_left),
                                onClick = {
                                    onTextAlign(PreferenceValues.PreferenceTextAlignment.Left)
                                },
                                tint = if (vm.textAlignment.value == PreferenceValues.PreferenceTextAlignment.Left) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                            AppIconButton(
                                imageVector = Icons.Default.FormatAlignCenter,
                                contentDescription = localize(MR.strings.text_align_center),
                                onClick = {
                                    onTextAlign(PreferenceValues.PreferenceTextAlignment.Center)
                                },
                                tint = if (vm.textAlignment.value == PreferenceValues.PreferenceTextAlignment.Center) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                            AppIconButton(
                                imageVector = Icons.Default.FormatAlignJustify,
                                contentDescription = localize(MR.strings.text_align_justify),
                                onClick = {
                                    onTextAlign(PreferenceValues.PreferenceTextAlignment.Justify)
                                },
                                tint = if (vm.textAlignment.value == PreferenceValues.PreferenceTextAlignment.Justify) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                            AppIconButton(
                                imageVector = Icons.Default.FormatAlignRight,
                                contentDescription = localize(MR.strings.text_align_right),
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
                title = localizeHelper.localize(MR.strings.font_size),
                trailing = vm.fontSize.value.toInt().toString(),
                valueRange = 8.0F..32.0F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.textWeight,
                title = localizeHelper.localize(MR.strings.font_weight),
                trailing = vm.textWeight.value.toInt().toString(),
                valueRange = 1f..900F,
            ).Build()
        }
        item {
            Components.Header(
                localizeHelper.localize(MR.strings.paragraph)
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.paragraphsIndent,
                title = localizeHelper.localize(MR.strings.paragraph_indent),
                trailing = vm.paragraphsIndent.value.toInt().toString(),
                valueRange = 0.0F..32.0F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.distanceBetweenParagraphs,
                title = localizeHelper.localize(MR.strings.paragraph_distance),
                trailing = vm.distanceBetweenParagraphs.value.toInt().toString(),
                valueRange = 0.0F..8.0F,
            ).Build()
        }
        item {
            Components.Header(
                localizeHelper.localize(MR.strings.line)
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.lineHeight,
                title = localizeHelper.localize(MR.strings.line_height),
                trailing = vm.lineHeight.value.toInt().toString(),
                valueRange = 22.0F..48.0F,
            ).Build()
        }
        item {
            Components.Header(
                localizeHelper.localize(MR.strings.autoscroll)
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsLong = vm.autoScrollInterval,
                title = localizeHelper.localize(MR.strings.interval),
                trailing = (vm.autoScrollInterval.value / 1000).toInt().toString(),
                valueRange = 500.0F..10000.0F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.autoScrollOffset,
                title = localizeHelper.localize(MR.strings.offset),
                trailing = (vm.autoScrollOffset.value / 1000).toInt().toString(),
                valueRange = 500.0F..10000F,
            ).Build()

        }
        item {
            Components.Header(
                localizeHelper.localize(MR.strings.scrollIndicator)
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.scrollIndicatorPadding,
                title = localizeHelper.localize(MR.strings.padding),
                trailing = vm.scrollIndicatorPadding.value.toString(),
                valueRange = 0F..32F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.scrollIndicatorWith,
                title = localizeHelper.localize(MR.strings.width),
                trailing = vm.scrollIndicatorWith.value.toString(),
                valueRange = 0F..32F,
            ).Build()
        }
        item {
            Components.Chip(
                preference = listOf(
                    localizeHelper.localize(MR.strings.right),
                    localizeHelper.localize(MR.strings.left),
                ),
                title = localizeHelper.localize(MR.strings.alignment),
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
                localizeHelper.localize(MR.strings.margins)
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.topMargin,
                title = localizeHelper.localize(MR.strings.top),
                trailing = vm.topMargin.value.toString(),
                valueRange = 0F..200F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.bottomMargin,
                title = localizeHelper.localize(MR.strings.bottom),
                trailing = vm.bottomMargin.value.toString(),
                valueRange = 0F..200F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.leftMargin,
                title = localizeHelper.localize(MR.strings.left),
                trailing = vm.leftMargin.value.toString(),
                valueRange = 0F..200F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.rightMargin,
                title = localizeHelper.localize(MR.strings.right),
                trailing = vm.rightMargin.value.toString(),
                valueRange = 0F..200F,
            ).Build()
        }
        item {
            Components.Header(
                localizeHelper.localize(MR.strings.content_padding)
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.topContentPadding,
                title = localizeHelper.localize(MR.strings.top),
                trailing = vm.topContentPadding.value.toString(),
                valueRange = 0F..32F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.bottomContentPadding,
                title = localizeHelper.localize(MR.strings.bottom),
                trailing = vm.bottomContentPadding.value.toString(),
                valueRange = 0F..32F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.betweenLetterSpaces,
                title = localizeHelper.localize(MR.strings.letter),
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
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    LazyColumn(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ChipChoicePreference(
                preference = vm.translatorOriginLanguage,
                choices = vm.translationEnginesManager.get().supportedLanguages.associate { it.first to it.second },
                title = localize(
                    MR.strings.origin_language
                )
            )
        }
        item {
            ChipChoicePreference(
                preference = vm.translatorTargetLanguage,
                choices = vm.translationEnginesManager.get().supportedLanguages.associate { it.first to it.second },
                title = localize(
                    MR.strings.target_language
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
                    localizeHelper.localize(MR.strings.page),
                    localizeHelper.localize(MR.strings.continues),
                ),
                selected = vm.readingMode.value.ordinal,
                onValueChange = {
                    vm.readingMode.value = ReadingMode.valueOf(it)
                },
                title = localizeHelper.localize(MR.strings.scroll_mode)
            )
        }
        item {
            ChipPreference(
                preference = listOf(
                    localizeHelper.localize(MR.strings.horizontal),
                    localizeHelper.localize(MR.strings.vertical),
                ),
                selected = vm.verticalScrolling.value.isTrue(),
                onValueChange = {
                    vm.verticalScrolling.value = it == 1
                },
                title = localizeHelper.localize(MR.strings.reading_mode)
            )
        }
        item {
            ChipPreference(
                preference = listOf(
                    localizeHelper.localize(MR.strings.landscape),
                    localizeHelper.localize(MR.strings.portrait),
                ),
                selected = vm.orientation.value,
                onValueChange = {
                    when (it) {
                        0 ->
                            vm.orientation.value =
                                    AppPreferences.PreferenceKeys.Orientation.Landscape.ordinal
                        1 ->
                            vm.orientation.value =
                                    AppPreferences.PreferenceKeys.Orientation.Portrait.ordinal
                    }
                },
                title = localizeHelper.localize(MR.strings.orientation)
            )
        }
        item {
            ChipPreference(
                preference = listOf(
                    localizeHelper.localize(MR.strings.full),
                    localizeHelper.localize(MR.strings.partial),
                    localizeHelper.localize(MR.strings.disable),
                ),
                selected = vm.isScrollIndicatorDraggable.value.ordinal,
                onValueChange = {
                    vm.isScrollIndicatorDraggable.value =
                        PreferenceValues.ScrollbarSelectionMode.valueOf(it)
                },
                title = localizeHelper.localize(MR.strings.scrollbar_mode)
            )
        }
        item {
            SwitchPreference(
                preference = vm.autoScrollMode,
                title = localizeHelper.localize(MR.strings.autoScroll),
                onValueChange = { vm.autoScrollMode = it }
            )
        }
        item {
            SwitchPreference(
                preference = vm.immersiveMode,
                title = localizeHelper.localize(MR.strings.immersive_mode),
            )
        }
        item {
            SwitchPreference(
                preference = vm.bionicReadingMode,
                title = localizeHelper.localize(MR.strings.bionic_reading),
            )
        }
        item {
            SwitchPreference(
                preference = vm.webViewIntegration,
                title = localizeHelper.localize(MR.strings.show_webView_during_fetching),
            )
        }
        item {
            SwitchPreference(
                preference = vm.screenAlwaysOn,
                title = localizeHelper.localize(MR.strings.screen_always_on),
            )
        }
        item {
            SwitchPreference(
                preference = vm.selectableMode,
                title = localizeHelper.localize(MR.strings.selectable_mode),
            )
        }
        item {
            SwitchPreference(
                preference = vm.showScrollIndicator,
                title = localizeHelper.localize(MR.strings.show_scrollbar),
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
        onChangeBrightness: (Float) -> Unit,
        onBackgroundChange: (themeId: Long) -> Unit,
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
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
                title = localizeHelper.localize(MR.strings.custom_brightness),
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
                    title = localizeHelper.localize(MR.strings.background_color),
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
                    title = localizeHelper.localize(MR.strings.text_color),
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
                                vm.showSnackBar(UiText.MStringResource(MR.strings.theme_was_saved))
                            }
                        }) {
                            MidSizeTextComposable(text = localizeHelper.localize(MR.strings.save_custom_theme))
                        }
                    } else if (!vm.readerTheme.value.isDefault) {
                        TextButton(onClick = {
                            scope.launchIO {
                                vm.readerThemeRepository.delete(
                                    vm.readerTheme.value.ReaderTheme()
                                )
                                vm.showSnackBar(UiText.MStringResource(MR.strings.theme_was_deleted))
                            }
                        }) {
                            MidSizeTextComposable(text = localizeHelper.localize(MR.strings.delete_custom_theme))
                        }
                    }
                }
            }.Build()
        }
        item {
            Components.Dynamic {
                ColorPreference(
                    preference = vm.selectedScrollBarColor,
                    title = localizeHelper.localize(MR.strings.selected_scrollbar_color)
                )
            }.Build()
        }
        item {
            Components.Dynamic {
                ColorPreference(
                    preference = vm.unselectedScrollBarColor,
                    title = localizeHelper.localize(MR.strings.unselected_scrollbar_color)
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


@OptIn(ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@Composable
fun Tabs(libraryTabs: List<TabItem>, pagerState: androidx.compose.foundation.pager.PagerState) {
    val scope = rememberCoroutineScope()
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.contentColor,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabsContent(
    libraryTabs: List<TabItem>,
    pagerState: androidx.compose.foundation.pager.PagerState,
) {
    androidx.compose.foundation.pager.HorizontalPager(
        pageCount = libraryTabs.size,
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
