package ireader.presentation.ui.reader.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRow
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import ireader.domain.data.engines.TranslateEngine
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.models.theme.ReaderTheme
import ireader.domain.preferences.models.FontType
import ireader.domain.preferences.prefs.ReadingMode
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.UiText
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
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
import ireader.presentation.ui.core.ui.PreferenceMutableState
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import ireader.presentation.core.toComposeColor
import ireader.presentation.core.toDomainColor
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
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
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
      4  // Updated to 4 tabs
    }
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }

    val readerTab: TabItem = remember {
        TabItem(
            localizeHelper.localize(Res.string.reader)
        ) {
            ReaderScreenTab(vm, onTextAlign)
        }
    }
    val generalTab: TabItem = remember {
        TabItem(
            localizeHelper.localize(Res.string.general)
        ) {
            GeneralScreenTab(vm)
        }
    }

    val colorTabItem = remember {
        TabItem(localizeHelper.localize(Res.string.colors)) {
            ColorScreenTab(vm, onChangeBrightness, onBackgroundChange)
        }
    }
    
    val fontsTabItem = remember {
        TabItem("Fonts") {
            FontPickerTab(vm)
        }
    }

    val tabs = remember {
        listOf<TabItem>(
            readerTab,
            generalTab,
            colorTabItem,
            fontsTabItem
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
fun ReaderScreenTab(
        vm: ReaderScreenViewModel,
        onTextAlign: (PreferenceValues.PreferenceTextAlignment) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    LazyColumn(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            val font = vm.font
            if (vm.fontsLoading) {
                // Show loading indicator while fonts are being fetched
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            } else {
                // Map font names to FontType with proper FontFamilyModel.Custom
                val fontChoices = vm.fonts.associate { fontName ->
                    val fontType = FontType(
                        name = fontName,
                        fontFamily = ireader.domain.models.common.FontFamilyModel.Custom(fontName)
                    )
                    fontType to fontName
                }
                
                ChipChoicePreference(
                    preference = font!!,
                    choices = fontChoices,
                    title = localizeHelper.localize(Res.string.font),
                    onFailToFindElement = vm.font!!.value.name
                )
            }
        }
        item {
            PreferenceRow(
                title = localizeHelper.localize(Res.string.text_align),
                action = {
                    LazyRow {
                        item {
                            AppIconButton(
                                imageVector = Icons.Default.FormatAlignLeft,
                                contentDescription = localize(Res.string.text_align_left),
                                onClick = {
                                    onTextAlign(PreferenceValues.PreferenceTextAlignment.Left)
                                },
                                tint = if (vm.textAlignment.value == PreferenceValues.PreferenceTextAlignment.Left) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                            AppIconButton(
                                imageVector = Icons.Default.FormatAlignCenter,
                                contentDescription = localize(Res.string.text_align_center),
                                onClick = {
                                    onTextAlign(PreferenceValues.PreferenceTextAlignment.Center)
                                },
                                tint = if (vm.textAlignment.value == PreferenceValues.PreferenceTextAlignment.Center) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                            AppIconButton(
                                imageVector = Icons.Default.FormatAlignJustify,
                                contentDescription = localize(Res.string.text_align_justify),
                                onClick = {
                                    onTextAlign(PreferenceValues.PreferenceTextAlignment.Justify)
                                },
                                tint = if (vm.textAlignment.value == PreferenceValues.PreferenceTextAlignment.Justify) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                            AppIconButton(
                                imageVector = Icons.Default.FormatAlignRight,
                                contentDescription = localize(Res.string.text_align_right),
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
                title = localizeHelper.localize(Res.string.font_size),
                trailingFormatter = { value -> "${value.toInt()} sp" },
                valueRange = 8.0F..180.0F,
                onValueChange = {
                    // Real-time update - no need to make transparent
                    // The change is immediately reflected in the reader
                }
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.textWeight,
                title = localizeHelper.localize(Res.string.font_weight),
                trailingFormatter = { value -> value.toInt().toString() },
                valueRange = 1f..900F,
                onValueChange = {
                    // Real-time update - no need to make transparent
                }
            ).Build()
        }
        item {
            Components.Header(
                localizeHelper.localize(Res.string.paragraph)
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.paragraphsIndent,
                title = localizeHelper.localize(Res.string.paragraph_indent),
                trailingFormatter = { value -> value.toInt().toString() },
                valueRange = 0.0F..100.0F,
                onValueChange = {
                    // Real-time update
                }
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.distanceBetweenParagraphs,
                title = localizeHelper.localize(Res.string.paragraph_distance),
                trailingFormatter = { value -> value.toInt().toString() },
                valueRange = 0.0F..10.0F,
                onValueChange = {
                    // Real-time update
                }
            ).Build()
        }
        item {
            Components.Header(
                localizeHelper.localize(Res.string.line)
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.lineHeight,
                title = localizeHelper.localize(Res.string.line_height),
                trailingFormatter = { value -> "${value.toInt()} sp" },
                valueRange = 22.0F..100.0F,
                onValueChange = {
                    // Real-time update with smooth transition
                }
            ).Build()
        }
        item {
            Components.Header(
                localizeHelper.localize(Res.string.autoscroll)
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsLong = vm.autoScrollInterval,
                title = localizeHelper.localize(Res.string.interval),
                trailing = (vm.autoScrollInterval.value / 1000).toInt().toString(),
                valueRange = 500.0F..10000.0F,onValueChange = {
                    vm.makeSettingTransparent()
                }
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.autoScrollOffset,
                title = localizeHelper.localize(Res.string.offset),
                trailing = (vm.autoScrollOffset.value / 100).toInt().toString(),
                valueRange = 1.0F..8F,onValueChange = {
                    vm.makeSettingTransparent()
                }
            ).Build()

        }
        item {
            Components.Header(
                localizeHelper.localize(Res.string.scrollIndicator)
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.scrollIndicatorPadding,
                title = localizeHelper.localize(Res.string.padding),
                trailing = vm.scrollIndicatorPadding.value.toString(),
                valueRange = 0F..32F,onValueChange = {
                    vm.makeSettingTransparent()
                }
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.scrollIndicatorWith,
                title = localizeHelper.localize(Res.string.width),
                trailing = vm.scrollIndicatorWith.value.toString(),
                valueRange = 0F..32F,onValueChange = {
                    vm.makeSettingTransparent()
                }
            ).Build()
        }
        item {
            Components.Chip(
                preference = listOf(
                    localizeHelper.localize(Res.string.right),
                    localizeHelper.localize(Res.string.left),
                ),
                title = localizeHelper.localize(Res.string.alignment),
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
                localizeHelper.localize(Res.string.margins)
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.topMargin,
                title = localizeHelper.localize(Res.string.top),
                trailing = vm.topMargin.value.toString(),
                valueRange = 0F..200F,onValueChange = {
                    vm.makeSettingTransparent()
                }
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.bottomMargin,
                title = localizeHelper.localize(Res.string.bottom),
                trailing = vm.bottomMargin.value.toString(),
                valueRange = 0F..200F,onValueChange = {
                    vm.makeSettingTransparent()
                }
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.leftMargin,
                title = localizeHelper.localize(Res.string.left),
                trailing = vm.leftMargin.value.toString(),
                valueRange = 0F..200F,onValueChange = {
                    vm.makeSettingTransparent()
                }
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.rightMargin,
                title = localizeHelper.localize(Res.string.right),
                trailing = vm.rightMargin.value.toString(),
                valueRange = 0F..200F,onValueChange = {
                    vm.makeSettingTransparent()
                }
            ).Build()
        }
        item {
            Components.Header(
                localizeHelper.localize(Res.string.content_padding)
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.topContentPadding,
                title = localizeHelper.localize(Res.string.top),
                trailing = vm.topContentPadding.value.toString(),
                valueRange = 0F..32F,onValueChange = {
                    vm.makeSettingTransparent()
                }
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.bottomContentPadding,
                title = localizeHelper.localize(Res.string.bottom),
                trailing = vm.bottomContentPadding.value.toString(),
                valueRange = 0F..32F,onValueChange = {
                    vm.makeSettingTransparent()
                }
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.betweenLetterSpaces,
                title = localizeHelper.localize(Res.string.letter),
                trailing = vm.betweenLetterSpaces.value.toString(),
                valueRange = 0F..32F,onValueChange = {
                    vm.makeSettingTransparent()
                }
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    LazyColumn(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Components.Header(
                localizeHelper.localize(Res.string.translation_settings)
            ).Build()
        }
        item {
            // Requirements: 6.6, 6.7 - Use stable API to get and set translation engine preference
            val engines = vm.translationEnginesManager.getAvailableEngines()
            val engineChoices = engines.mapNotNull { source ->
                when (source) {
                    is ireader.domain.usecases.translate.TranslationEngineSource.BuiltIn -> 
                        source.engine.id to source.engine.engineName
                    is ireader.domain.usecases.translate.TranslationEngineSource.Plugin -> 
                        null // Plugin engines would need different handling
                }
            }.toMap()
            
            ChipChoicePreference(
                preference = vm.translatorEngine,
                choices = engineChoices,
                title = localize(
                    Res.string.translation_engine
                )
            )
        }
        item {
            ChipChoicePreference(
                preference = vm.translatorOriginLanguage,
                choices = vm.translationEnginesManager.get().supportedLanguages.associate { it.first to it.second },
                title = localize(
                    Res.string.origin_language
                )
            )
        }
        item {
            ChipChoicePreference(
                preference = vm.translatorTargetLanguage,
                choices = vm.translationEnginesManager.get().supportedLanguages.associate { it.first to it.second },
                title = localize(
                    Res.string.target_language
                )
            )
        }
        item {
            SwitchPreference(
                preference = vm.autoTranslateNextChapter,
                title = localizeHelper.localize(Res.string.auto_translate_next_chapter),
                subtitle = localizeHelper.localize(Res.string.auto_translate_next_chapter_summary)
            )
        }
        
        item {
            TranslateButton(
                onClick = {
                    vm.translateCurrentChapter()
                },
                isTranslating = vm.translationViewModel.isTranslating,
                engine = vm.translationEnginesManager.get(),
                vm = vm
            )
        }
        
        // Glossary button
        item {
            PreferenceRow(
                onClick = {
                    vm.loadGlossary()
                    vm.translationViewModel.translationState.showGlossaryDialog = true
                },
                title = "Manage Glossary"
            )
        }
        
        item {
            Components.Header(
                "Reading Settings"
            ).Build()
        }
        item {
            ChipPreference(
                preference = listOf(
                    localizeHelper.localize(Res.string.page),
                    localizeHelper.localize(Res.string.continues),
                ),
                selected = vm.readingMode.value.ordinal,
                onValueChange = {
                    vm.readingMode.value = ReadingMode.valueOf(it)
                },
                title = localizeHelper.localize(Res.string.scroll_mode)
            )
        }
        item {
            ChipPreference(
                preference = listOf(
                    localizeHelper.localize(Res.string.page),
                    localizeHelper.localize(Res.string.continues),
                ),
                selected = vm.readerPreferences.defaultReadingMode().get().ordinal,
                onValueChange = {
                    vm.readerPreferences.defaultReadingMode().set(ReadingMode.valueOf(it))
                },
                title = "Default Reading Mode for New Books"
            )
        }
        item {
            ChipPreference(
                preference = listOf(
                    localizeHelper.localize(Res.string.horizontal),
                    localizeHelper.localize(Res.string.vertical),
                ),
                selected = vm.verticalScrolling.value.isTrue(),
                onValueChange = {
                    vm.verticalScrolling.value = it == 1
                },
                title = localizeHelper.localize(Res.string.reading_mode)
            )
        }
        // Orientation is a global app setting, moved to app settings
        // Keeping only chapter/book-specific settings here
        item {
            Components.Header(
                "Display Settings"
            ).Build()
        }
        item {
            ChipPreference(
                preference = listOf(
                    localizeHelper.localize(Res.string.full),
                    localizeHelper.localize(Res.string.partial),
                    localizeHelper.localize(Res.string.disable),
                ),
                selected = vm.isScrollIndicatorDraggable.value.ordinal,
                onValueChange = {
                    vm.isScrollIndicatorDraggable.value =
                        PreferenceValues.ScrollbarSelectionMode.valueOf(it)
                },
                title = localizeHelper.localize(Res.string.scrollbar_mode)
            )
        }
        item {
            SwitchPreference(
                preference = vm.autoScrollMode,
                title = localizeHelper.localize(Res.string.autoscroll),
                onValueChange = { vm.autoScrollMode = it }
            )
        }
        item {
            SwitchPreference(
                preference = vm.immersiveMode,
                title = localizeHelper.localize(Res.string.immersive_mode),
            )
        }
        item {
            SwitchPreference(
                preference = vm.bionicReadingMode,
                title = localizeHelper.localize(Res.string.bionic_reading),
            )
        }
        item {
            SwitchPreference(
                preference = vm.webViewIntegration,
                title = localizeHelper.localize(Res.string.show_webView_during_fetching),
            )
        }
        if (vm.webViewIntegration.value) {
            item {
                SwitchPreference(
                    preference = vm.webViewBackgroundMode,
                    title = "Background WebView Mode",
                    subtitle = "Bypass bot detection invisibly without showing WebView"
                )
            }
        }
        item {
            SwitchPreference(
                preference = vm.screenAlwaysOn,
                title = localizeHelper.localize(Res.string.screen_always_on),
            )
        }
        item {
            SwitchPreference(
                preference = vm.selectableMode,
                title = localizeHelper.localize(Res.string.selectable_mode),
            )
        }
        item {
            SwitchPreference(
                preference = vm.showScrollIndicator,
                title = localizeHelper.localize(Res.string.show_scrollbar),
            )
        }
        item {
            SwitchPreference(
                preference = vm.showReadingTime,
                title = "Show Reading Time",
                onValueChange = { vm.showReadingTime = it }
            )
        }
        item {
            SwitchPreference(
                preference = vm.volumeKeyNavigation,
                title = localizeHelper.localize(Res.string.volume_key_navigation),
            )
        }
        item {
            SwitchPreference(
                preference = vm.paragraphTranslationEnabled,
                title = "Paragraph Translation Menu",
            )
        }
        item {
            Components.Header(
                "Text-to-Speech Settings"
            ).Build()
        }
        item {
            SwitchPreference(
                preference = vm.useTTSWithTranslatedText,
                title = "TTS with Translated Text",
                subtitle = "Use translated text for text-to-speech when available"
            )
        }
        item {
            Components.Header(
                "Bilingual Mode"
            ).Build()
        }
        item {
            SwitchPreference(
                preference = vm.bilingualModeEnabled,
                title = "Enable Bilingual Mode"
            )
        }
        item {
            ChipPreference(
                preference = listOf(
                    "Side by Side",
                    "Paragraph by Paragraph",
                ),
                selected = vm.bilingualModeLayout.value,
                onValueChange = {
                    vm.switchBilingualLayout()
                },
                title = "Bilingual Layout"
            )
        }
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ColorScreenTab(
        vm: ReaderScreenViewModel,
        onChangeBrightness: (Float) -> Unit,
        onBackgroundChange: (themeId: Long) -> Unit,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                title = localizeHelper.localize(Res.string.custom_brightness),
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
        // Note: Background and text color preferences removed as they don't exist in the current ViewModel
        // These would need to be added to ReaderPreferences if color customization is needed
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
                                vm.showSnackBar(UiText.MStringResource(Res.string.theme_was_saved))
                            }
                        }) {
                            MidSizeTextComposable(text = localizeHelper.localize(Res.string.save_custom_theme))
                        }
                    } else if (!vm.readerTheme.value.isDefault) {
                        TextButton(onClick = {
                            scope.launchIO {
                                vm.readerThemeRepository.delete(
                                    vm.readerTheme.value.ReaderTheme()
                                )
                                vm.showSnackBar(UiText.MStringResource(Res.string.theme_was_deleted))
                            }
                        }) {
                            MidSizeTextComposable(text = localizeHelper.localize(Res.string.delete_custom_theme))
                        }
                    }
                }
            }.Build()
        }
        // Note: Scrollbar color preferences removed as they don't exist in the current ViewModel
        // These would need to be added to ReaderPreferences if scrollbar color customization is needed
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
    ) {
        libraryTabs.forEachIndexed { index, tab ->
            Tab(
                text = { MidSizeTextComposable(text = tab.name) },
                selected = pagerState.currentPage == index,
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
    HorizontalPager(
        modifier = Modifier.fillMaxSize(),
        state = pagerState,
        pageSpacing = 0.dp,
        userScrollEnabled = true,
        reverseLayout = false,
        contentPadding = PaddingValues(0.dp),
        pageSize = PageSize.Fill,
        key = null,

        pageContent =  { page ->
            libraryTabs[page].screen()
        }
    )
}

fun Boolean.isTrue(): Int {
    return if (this) {
        1
    } else {
        0
    }
}

@Composable
fun TranslateButton(
    onClick: () -> Unit,
    isTranslating: Boolean,
    engine: TranslateEngine,
    vm: ReaderScreenViewModel,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { if (!isTranslating) onClick() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isTranslating,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = if (isTranslating) 
                            localizeHelper.localize(Res.string.translating) 
                        else 
                            try {
                                localizeHelper.localize(Res.string.translate_now)
                            } catch (e: Exception) {
                                "Translate Now"
                            },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            // Show current engine
            Text(
                text = engine.engineName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            if (engine.requiresApiKey) {
                val apiStatus = when (engine.id) {
                    2L -> if (vm.openAIApiKey.value.isBlank()) "API key required" else "API key set"  
                    3L -> if (vm.deepSeekApiKey.value.isBlank()) "API key required" else "API key set"
                    else -> null
                }
                
                if (apiStatus != null) {
                    Text(
                        text = apiStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (apiStatus.contains("required")) 
                            MaterialTheme.colorScheme.error
                        else 
                            MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}



/**
 * Font Picker Tab for selecting custom fonts and Google Fonts
 */
@Composable
fun FontPickerTab(
    vm: ReaderScreenViewModel
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    // Convert Google Fonts list to CustomFont format for display
    val googleFonts = remember(vm.fonts) {
        vm.fonts.map { fontName ->
            ireader.domain.models.fonts.CustomFont(
                id = "google_${fontName.lowercase().replace(" ", "_")}",
                name = fontName,
                filePath = "", // Empty for Google Fonts
                isSystemFont = true,
                dateAdded = 0L
            )
        }
    }
    
    // Determine the currently selected font ID
    // Check both the selectedFontId (for custom fonts) and the font preference (for Google Fonts)
    val currentSelectedId = remember(vm.selectedFontId.value, vm.font!!.value) {
        val customFontId = vm.selectedFontId.value
        val googleFontName = vm.font.value.name
        
        when {
            customFontId.isNotEmpty() -> customFontId
            googleFontName != null -> "google_${googleFontName.lowercase().replace(" ", "_")}"
            else -> ""
        }
    }
    
    FontPicker(
        selectedFontId = currentSelectedId,
        customFonts = vm.customFonts,
        systemFonts = googleFonts, // Use Google Fonts as system fonts
        onFontSelected = { fontId ->
            // If it's a Google Font, download and apply it asynchronously
            if (fontId.startsWith("google_")) {
                val fontName = googleFonts.find { it.id == fontId }?.name
                if (fontName != null) {
                    vm.selectGoogleFont(fontName)
                }
            } else {
                // Custom font - clear Google Font preference and set custom font
                vm.platformUiPreferences.font().set(
                    ireader.domain.preferences.models.FontType(
                        name = "Default",
                        fontFamily = ireader.domain.models.common.FontFamilyModel.Default
                    )
                )
                vm.selectFont(fontId)
            }
        },
        onImportFont = { /* Import button removed */ },
        onDeleteFont = { fontId ->
            vm.deleteFont(fontId)
        },
        isLoading = vm.fontsLoading,
        modifier = Modifier.fillMaxSize()
    )
}
