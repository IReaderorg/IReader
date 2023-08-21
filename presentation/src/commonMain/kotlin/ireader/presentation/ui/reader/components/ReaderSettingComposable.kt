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
            localizeHelper.localize { xml -> xml.reader }
        ) {
            ReaderScreenTab(vm, onTextAlign)
        }
    }
    val generalTab: TabItem = remember {
        TabItem(
            localizeHelper.localize() { xml ->
                xml.general
            }
        ) {
            GeneralScreenTab(vm)
        }
    }


    val colorTabItem = remember {
        TabItem(localizeHelper.localize { xml -> xml.colors }) {
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
                    title = localizeHelper.localize() { xml ->
                        xml.font
                    },
                    onFailToFindElement = vm.font?.value?.name ?: ""
                )
            }

        }
        item {
            PreferenceRow(
                title = localizeHelper.localize { xml -> xml.textAlign },
                action = {
                    LazyRow {
                        item {
                            AppIconButton(
                                imageVector = Icons.Default.FormatAlignLeft,
                                contentDescription = localize { xml -> xml.textAlignLeft },
                                onClick = {
                                    onTextAlign(PreferenceValues.PreferenceTextAlignment.Left)
                                },
                                tint = if (vm.textAlignment.value == PreferenceValues.PreferenceTextAlignment.Left) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                            AppIconButton(
                                imageVector = Icons.Default.FormatAlignCenter,
                                contentDescription = localize { xml -> xml.textAlignCenter },
                                onClick = {
                                    onTextAlign(PreferenceValues.PreferenceTextAlignment.Center)
                                },
                                tint = if (vm.textAlignment.value == PreferenceValues.PreferenceTextAlignment.Center) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                            AppIconButton(
                                imageVector = Icons.Default.FormatAlignJustify,
                                contentDescription = localize { xml -> xml.textAlignJustify },
                                onClick = {
                                    onTextAlign(PreferenceValues.PreferenceTextAlignment.Justify)
                                },
                                tint = if (vm.textAlignment.value == PreferenceValues.PreferenceTextAlignment.Justify) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                            AppIconButton(
                                imageVector = Icons.Default.FormatAlignRight,
                                contentDescription = localize { xml -> xml.textAlignRight },
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
                title = localizeHelper.localize() { xml ->
                    xml.fontSize
                },
                trailing = vm.fontSize.value.toInt().toString(),
                valueRange = 8.0F..32.0F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.textWeight,
                title = localizeHelper.localize { xml -> xml.fontWeight },
                trailing = vm.textWeight.value.toInt().toString(),
                valueRange = 1f..900F,
            ).Build()
        }
        item {
            Components.Header(
                localizeHelper.localize() { xml ->
                    xml.paragraph
                }
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.paragraphsIndent,
                title = localizeHelper.localize { xml -> xml.paragraphIndent },
                trailing = vm.paragraphsIndent.value.toInt().toString(),
                valueRange = 0.0F..32.0F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.distanceBetweenParagraphs,
                title = localizeHelper.localize { xml -> xml.paragraphDistance },
                trailing = vm.distanceBetweenParagraphs.value.toInt().toString(),
                valueRange = 0.0F..8.0F,
            ).Build()
        }
        item {
            Components.Header(
                localizeHelper.localize { xml -> xml.line }
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.lineHeight,
                title = localizeHelper.localize { xml -> xml.lineHeight },
                trailing = vm.lineHeight.value.toInt().toString(),
                valueRange = 22.0F..48.0F,
            ).Build()
        }
        item {
            Components.Header(
                localizeHelper.localize { xml -> xml.autoscroll }
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsLong = vm.autoScrollInterval,
                title = localizeHelper.localize { xml -> xml.interval },
                trailing = (vm.autoScrollInterval.value / 1000).toInt().toString(),
                valueRange = 500.0F..10000.0F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.autoScrollOffset,
                title = localizeHelper.localize { xml -> xml.offset },
                trailing = (vm.autoScrollOffset.value / 1000).toInt().toString(),
                valueRange = 500.0F..10000F,
            ).Build()

        }
        item {
            Components.Header(
                localizeHelper.localize { xml -> xml.scrollIndicator }
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.scrollIndicatorPadding,
                title = localizeHelper.localize { xml -> xml.padding },
                trailing = vm.scrollIndicatorPadding.value.toString(),
                valueRange = 0F..32F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.scrollIndicatorWith,
                title = localizeHelper.localize { xml -> xml.width },
                trailing = vm.scrollIndicatorWith.value.toString(),
                valueRange = 0F..32F,
            ).Build()
        }
        item {
            Components.Chip(
                preference = listOf(
                    localizeHelper.localize { xml -> xml.right },
                    localizeHelper.localize { xml -> xml.left },
                ),
                title = localizeHelper.localize { xml -> xml.alignment },
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
                localizeHelper.localize { xml -> xml.margins }
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.topMargin,
                title = localizeHelper.localize { xml -> xml.top },
                trailing = vm.topMargin.value.toString(),
                valueRange = 0F..200F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.bottomMargin,
                title = localizeHelper.localize { xml -> xml.bottom },
                trailing = vm.bottomMargin.value.toString(),
                valueRange = 0F..200F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.leftMargin,
                title = localizeHelper.localize { xml -> xml.left },
                trailing = vm.leftMargin.value.toString(),
                valueRange = 0F..200F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.rightMargin,
                title = localizeHelper.localize { xml -> xml.right },
                trailing = vm.rightMargin.value.toString(),
                valueRange = 0F..200F,
            ).Build()
        }
        item {
            Components.Header(
                localizeHelper.localize { xml -> xml.contentPadding }
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.topContentPadding,
                title = localizeHelper.localize { xml -> xml.top },
                trailing = vm.topContentPadding.value.toString(),
                valueRange = 0F..32F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.bottomContentPadding,
                title = localizeHelper.localize { xml -> xml.bottom },
                trailing = vm.bottomContentPadding.value.toString(),
                valueRange = 0F..32F,
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.betweenLetterSpaces,
                title = localizeHelper.localize { xml -> xml.letter },
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
                title = localize { it.originLanguage }
            )
        }
        item {
            ChipChoicePreference(
                preference = vm.translatorTargetLanguage,
                choices = vm.translationEnginesManager.get().supportedLanguages.associate { it.first to it.second },
                title = localize { it.targetLanguage }
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
                    localizeHelper.localize { xml -> xml.page },
                    localizeHelper.localize { xml -> xml.continues },
                ),
                selected = vm.readingMode.value.ordinal,
                onValueChange = {
                    vm.readingMode.value = ReadingMode.valueOf(it)
                },
                title = localizeHelper.localize { xml -> xml.scrollMode }
            )
        }
        item {
            ChipPreference(
                preference = listOf(
                    localizeHelper.localize { xml -> xml.horizontal },
                    localizeHelper.localize { xml -> xml.vertical },
                ),
                selected = vm.verticalScrolling.value.isTrue(),
                onValueChange = {
                    vm.verticalScrolling.value = it == 1
                },
                title = localizeHelper.localize { xml -> xml.readingMode }
            )
        }
        item {
            ChipPreference(
                preference = listOf(
                    localizeHelper.localize { xml -> xml.landscape },
                    localizeHelper.localize { xml -> xml.portrait },
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
                title = localizeHelper.localize { xml -> xml.orientation }
            )
        }
        item {
            ChipPreference(
                preference = listOf(
                    localizeHelper.localize { xml -> xml.full },
                    localizeHelper.localize { xml -> xml.partial },
                    localizeHelper.localize { xml -> xml.disable },
                ),
                selected = vm.isScrollIndicatorDraggable.value.ordinal,
                onValueChange = {
                    vm.isScrollIndicatorDraggable.value =
                        PreferenceValues.ScrollbarSelectionMode.valueOf(it)
                },
                title = localizeHelper.localize { xml -> xml.scrollMode }
            )
        }
        item {
            SwitchPreference(
                preference = vm.autoScrollMode,
                title = localizeHelper.localize { xml -> xml.autoScroll },
                onValueChange = { vm.autoScrollMode = it }
            )
        }
        item {
            SwitchPreference(
                preference = vm.immersiveMode,
                title = localizeHelper.localize { xml -> xml.immersiveMode },
            )
        }
        item {
            SwitchPreference(
                preference = vm.bionicReadingMode,
                title = localizeHelper.localize { xml -> xml.bionicReading },
            )
        }
        item {
            SwitchPreference(
                preference = vm.webViewIntegration,
                title = localizeHelper.localize { xml -> xml.showWebViewDuringFetching },
            )
        }
        item {
            SwitchPreference(
                preference = vm.screenAlwaysOn,
                title = localizeHelper.localize { xml -> xml.screenAlwaysOn },
            )
        }
        item {
            SwitchPreference(
                preference = vm.selectableMode,
                title = localizeHelper.localize { xml -> xml.selectableMode },
            )
        }
        item {
            SwitchPreference(
                preference = vm.showScrollIndicator,
                title = localizeHelper.localize { xml -> xml.showScrollbar },
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
                title = localizeHelper.localize { xml -> xml.customBrightness },
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
                    title = localizeHelper.localize { xml -> xml.backgroundColor },
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
                    title = localizeHelper.localize { xml -> xml.textColor },
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
                                vm.showSnackBar(UiText.MStringResource { it.themeWasSaved })
                            }
                        }) {
                            MidSizeTextComposable(text = localizeHelper.localize { xml -> xml.saveCustomTheme })
                        }
                    } else if (!vm.readerTheme.value.isDefault) {
                        TextButton(onClick = {
                            scope.launchIO {
                                vm.readerThemeRepository.delete(
                                    vm.readerTheme.value.ReaderTheme()
                                )
                                vm.showSnackBar(UiText.MStringResource { it.themeWasDeleted })
                            }
                        }) {
                            MidSizeTextComposable(text = localizeHelper.localize { xml -> xml.deleteCustomTheme })
                        }
                    }
                }
            }.Build()
        }
        item {
            Components.Dynamic {
                ColorPreference(
                    preference = vm.selectedScrollBarColor,
                    title = localizeHelper.localize { xml -> xml.selectedScrollbarColor }
                )
            }.Build()
        }
        item {
            Components.Dynamic {
                ColorPreference(
                    preference = vm.unselectedScrollBarColor,
                    title = localizeHelper.localize { xml -> xml.unselectedScrollbarColor }
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
