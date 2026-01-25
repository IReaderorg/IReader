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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.data.engines.TranslateEngine
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.models.theme.ReaderTheme
import ireader.domain.preferences.models.FontType
import ireader.domain.preferences.prefs.ReadingMode
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.UiText
import ireader.i18n.localize
import ireader.i18n.resources.*
import ireader.i18n.resources.alignment
import ireader.i18n.resources.auto_translate_next_chapter
import ireader.i18n.resources.auto_translate_next_chapter_summary
import ireader.i18n.resources.autoscroll
import ireader.i18n.resources.background_webview_mode
import ireader.i18n.resources.bilingual_layout
import ireader.i18n.resources.bionic_reading
import ireader.i18n.resources.bottom
import ireader.i18n.resources.bypass_bot_detection_invisibly_without
import ireader.i18n.resources.colors
import ireader.i18n.resources.content_padding
import ireader.i18n.resources.continues
import ireader.i18n.resources.custom_brightness
import ireader.i18n.resources.default_reading_mode_for_new_books
import ireader.i18n.resources.delete_custom_theme
import ireader.i18n.resources.disable
import ireader.i18n.resources.enable_bilingual_mode
import ireader.i18n.resources.font
import ireader.i18n.resources.font_size
import ireader.i18n.resources.font_weight
import ireader.i18n.resources.full
import ireader.i18n.resources.general
import ireader.i18n.resources.horizontal
import ireader.i18n.resources.immersive_mode
import ireader.i18n.resources.interval
import ireader.i18n.resources.left
import ireader.i18n.resources.letter
import ireader.i18n.resources.line
import ireader.i18n.resources.line_height
import ireader.i18n.resources.manage_glossary
import ireader.i18n.resources.margins
import ireader.i18n.resources.offset
import ireader.i18n.resources.origin_language
import ireader.i18n.resources.padding
import ireader.i18n.resources.page
import ireader.i18n.resources.paragraph
import ireader.i18n.resources.paragraph_distance
import ireader.i18n.resources.paragraph_indent
import ireader.i18n.resources.paragraph_translation_menu
import ireader.i18n.resources.partial
import ireader.i18n.resources.reader
import ireader.i18n.resources.reading_mode
import ireader.i18n.resources.right
import ireader.i18n.resources.save_custom_theme
import ireader.i18n.resources.screen_always_on
import ireader.i18n.resources.scrollIndicator
import ireader.i18n.resources.scroll_mode
import ireader.i18n.resources.scrollbar_mode
import ireader.i18n.resources.selectable_mode
import ireader.i18n.resources.show_reading_time
import ireader.i18n.resources.show_scrollbar
import ireader.i18n.resources.show_webView_during_fetching
import ireader.i18n.resources.target_language
import ireader.i18n.resources.text_align
import ireader.i18n.resources.text_align_center
import ireader.i18n.resources.text_align_justify
import ireader.i18n.resources.text_align_left
import ireader.i18n.resources.text_align_right
import ireader.i18n.resources.theme_was_deleted
import ireader.i18n.resources.theme_was_saved
import ireader.i18n.resources.top
import ireader.i18n.resources.translate_now
import ireader.i18n.resources.translating
import ireader.i18n.resources.translation_engine
import ireader.i18n.resources.translation_settings
import ireader.i18n.resources.tts_with_translated_text
import ireader.i18n.resources.use_translated_text_for_text
import ireader.i18n.resources.vertical
import ireader.i18n.resources.volume_key_navigation
import ireader.i18n.resources.width
import ireader.presentation.ui.component.components.Build
import ireader.presentation.ui.component.components.ChipChoicePreference
import ireader.presentation.ui.component.components.ChipPreference
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.PreferenceRow
import ireader.presentation.ui.component.components.SwitchPreference
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.theme.ReaderTheme
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
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
                trailing = (vm.autoScrollInterval.lazyValue / 1000).toInt().toString(),
                valueRange = 500.0F..10000.0F
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.autoScrollOffset,
                title = localizeHelper.localize(Res.string.offset),
                trailing = (vm.autoScrollOffset.lazyValue / 100).toInt().toString(),
                valueRange = 1.0F..8F
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
                trailing = vm.scrollIndicatorPadding.lazyValue.toString(),
                valueRange = 0F..32F
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.scrollIndicatorWith,
                title = localizeHelper.localize(Res.string.width),
                trailing = vm.scrollIndicatorWith.lazyValue.toString(),
                valueRange = 0F..32F
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
                trailing = vm.topMargin.lazyValue.toString(),
                valueRange = 0F..200F
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.bottomMargin,
                title = localizeHelper.localize(Res.string.bottom),
                trailing = vm.bottomMargin.lazyValue.toString(),
                valueRange = 0F..200F
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.leftMargin,
                title = localizeHelper.localize(Res.string.left),
                trailing = vm.leftMargin.lazyValue.toString(),
                valueRange = 0F..200F
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.rightMargin,
                title = localizeHelper.localize(Res.string.right),
                trailing = vm.rightMargin.lazyValue.toString(),
                valueRange = 0F..200F
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
                trailing = vm.topContentPadding.lazyValue.toString(),
                valueRange = 0F..32F
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.bottomContentPadding,
                title = localizeHelper.localize(Res.string.bottom),
                trailing = vm.bottomContentPadding.lazyValue.toString(),
                valueRange = 0F..32F
            ).Build()
        }
        item {
            Components.Slider(
                preferenceAsInt = vm.betweenLetterSpaces,
                title = localizeHelper.localize(Res.string.letter),
                trailing = vm.betweenLetterSpaces.lazyValue.toString(),
                valueRange = 0F..32F
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
                    is ireader.domain.usecases.translate.TranslationEngineSource.Plugin -> {
                        // Include plugin engines with their manifest ID hash as the engine ID
                        val pluginId = source.plugin.manifest.id.hashCode().toLong()
                        pluginId to "${source.plugin.manifest.name} (Plugin)"
                    }
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
                title = localizeHelper.localize(Res.string.manage_glossary)
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
                title = localizeHelper.localize(Res.string.default_reading_mode_for_new_books)
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
                    title = localizeHelper.localize(Res.string.background_webview_mode),
                    subtitle = localizeHelper.localize(Res.string.bypass_bot_detection_invisibly_without)
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
                preference = vm.showReadingTimeIndicator,
                title = localizeHelper.localize(Res.string.show_reading_time),
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
                title = localizeHelper.localize(Res.string.paragraph_translation_menu),
            )
        }
        item {
            Components.Header(
                "Performance"
            ).Build()
        }
        item {
            // Reduced animations toggle for older devices
            SwitchPreference(
                preference = vm.readerPreferences.reducedAnimations().get(),
                title = localizeHelper.localize(Res.string.reduced_animations),
                subtitle = localizeHelper.localize(Res.string.disable_animations_for_better_performance),
                onValueChange = { enabled ->
                    vm.readerPreferences.reducedAnimations().set(enabled)
                }
            )
        }
        item {
            Components.Header(
                "Content Filter"
            ).Build()
        }
        item {
            SwitchPreference(
                preference = vm.contentFilterEnabled,
                title = localizeHelper.localize(Res.string.enable_content_filter),
                subtitle = localizeHelper.localize(Res.string.remove_unwanted_text_patterns_from)
            )
        }
        if (vm.contentFilterEnabled.value) {
            item {
                ContentFilterPatternsEditor(
                    patterns = vm.contentFilterPatterns.value,
                    onPatternsChange = { vm.contentFilterPatterns.value = it },
                    contentFilterUseCase = vm.contentFilterUseCase
                )
            }
        }
        item {
            Components.Header(
                "Text-to-Speech Settings"
            ).Build()
        }
        item {
            SwitchPreference(
                preference = vm.useTTSWithTranslatedText,
                title = localizeHelper.localize(Res.string.tts_with_translated_text),
                subtitle = localizeHelper.localize(Res.string.use_translated_text_for_text)
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
                title = localizeHelper.localize(Res.string.enable_bilingual_mode)
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
                title = localizeHelper.localize(Res.string.bilingual_layout)
            )
        }
        item {
            Components.Header(
                "Actions"
            ).Build()
        }
        item {
            PreferenceRow(
                onClick = {
                    vm.enterCopyMode()
                },
                title = "Copy Quote",
                subtitle = "Enable text selection to copy quotes from this chapter"
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
    val fontPref = vm.font
    val currentSelectedId = remember(vm.selectedFontId.value, fontPref?.value) {
        val customFontId = vm.selectedFontId.value
        val googleFontName = fontPref?.value?.name
        
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


/**
 * Content Filter Patterns Editor
 * Allows users to add/edit regex patterns to remove unwanted text from chapters.
 */
@Composable
fun ContentFilterPatternsEditor(
    patterns: String,
    onPatternsChange: (String) -> Unit,
    contentFilterUseCase: ireader.domain.usecases.reader.ContentFilterUseCase,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var editedPatterns by remember { mutableStateOf(patterns) }
    var testText by remember { mutableStateOf("") }
    var testResult by remember { mutableStateOf<String?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }
    
    androidx.compose.runtime.LaunchedEffect(patterns) {
        editedPatterns = patterns
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val hasValidationError = validationError != null
        val errorText = validationError ?: ""
        androidx.compose.material3.OutlinedTextField(
            value = editedPatterns,
            onValueChange = { newValue: String ->
                editedPatterns = newValue
                val lines = newValue.split("\n").filter { line -> line.isNotBlank() }
                val errors = lines.mapNotNull { pattern ->
                    contentFilterUseCase.validatePattern(pattern.trim())?.let { err -> "$pattern: $err" }
                }
                validationError = if (errors.isNotEmpty()) errors.joinToString("\n") else null
            },
            label = { Text(localizeHelper.localize(Res.string.regex_patterns_one_per_line)) },
            placeholder = { Text(localizeHelper.localize(Res.string.use_arrow_keyschapternadchapternread_more_at)) },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            isError = hasValidationError,
            supportingText = if (hasValidationError) {
                { Text(errorText, color = MaterialTheme.colorScheme.error) }
            } else null,
            maxLines = 8
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    if (validationError == null) {
                        onPatternsChange(editedPatterns)
                    }
                },
                enabled = validationError == null && editedPatterns != patterns
            ) {
                Text(localizeHelper.localize(Res.string.save_patterns))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = localizeHelper.localize(Res.string.test_filter),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        androidx.compose.material3.OutlinedTextField(
            value = testText,
            onValueChange = { newValue: String -> testText = newValue },
            label = { Text(localizeHelper.localize(Res.string.sample_text)) },
            placeholder = { Text(localizeHelper.localize(Res.string.paste_text_to_test_the_filter)) },
            modifier = Modifier.fillMaxWidth().height(80.dp),
            maxLines = 3
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    testResult = contentFilterUseCase.testPatterns(testText, editedPatterns)
                },
                enabled = testText.isNotBlank() && editedPatterns.isNotBlank()
            ) {
                Text(localizeHelper.localize(Res.string.test))
            }
            
            val hasTestResult = testResult != null
            if (hasTestResult) {
                TextButton(onClick = { testResult = null }) {
                    Text(localizeHelper.localize(Res.string.clear_1))
                }
            }
        }
        
        val currentTestResult: String? = testResult
        if (currentTestResult != null) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = localizeHelper.localize(Res.string.result),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            val isEmpty = currentTestResult.isEmpty()
            val resultText: String = if (isEmpty) "(empty - all text removed)" else currentTestResult
            val resultColor = if (isEmpty) 
                MaterialTheme.colorScheme.error 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
            
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = resultText,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = resultColor
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = localizeHelper.localize(Res.string.tip_use_regex_patterns_to_remove_unwanted_textn) +
                   "• .* = any characters\n" +
                   "• (?:A|B) = A or B\n" +
                   "• \\\\[ and \\\\] = literal brackets\n\n" +
                   "Examples:\n" +
                   "• \"Use arrow keys.*chapter\" - navigation hints\n" +
                   "• \"(?:A|D|←|→).*chapter\" - keyboard shortcuts\n" +
                   "• \"Read more at.*\" - promotions\n" +
                   "• \"\\\\[TL:.*?\\\\]\" - translator notes",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
