package ireader.presentation.core.ui

import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.core.log.Log
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.models.prefs.readerThemes
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.core.IModalDrawer
import ireader.presentation.core.IModalSheets
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.CustomizeAnimateVisibility
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.NavigationBarTokens
import ireader.presentation.ui.component.ThemePreference
import ireader.presentation.ui.component.components.Build
import ireader.presentation.ui.component.components.ChipChoicePreference
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.SliderPreference
import ireader.presentation.ui.component.components.SwitchPreference
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.home.tts.MediaControllers
import ireader.presentation.ui.home.tts.TTLScreenPlay
import ireader.presentation.ui.home.tts.TTSScreen
import ireader.presentation.ui.home.tts.TTSTopBar
import ireader.presentation.ui.home.tts.TTSViewModel
import ireader.presentation.ui.reader.ReaderScreenDrawer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf
import java.math.RoundingMode

actual class TTSScreenSpec actual constructor(
    val bookId: Long,
    val chapterId: Long,
    val sourceId: Long,
    val readingParagraph: Int,
) : VoyagerScreen() {


    override val key: ScreenKey
        get() = "TTS_SCREEN#$chapterId"
//    fun buildDeepLink(
//        bookId: Long,
//        sourceId: Long,
//        chapterId: Long,
//        readingParagraph: Long,
//    ): String {
//        return "https://www.ireader.org/tts_screen_route/$bookId/$chapterId/$sourceId/$readingParagraph"
//    }
//
//    override val deepLinks: List<NavDeepLink> = listOf(
//
//        navDeepLink {
//            uriPattern =
//                "https://www.ireader.org/tts_screen_route/{bookId}/{chapterId}/{sourceId}/{readingParagraph}"
//            NavigationArgs.bookId
//            NavigationArgs.chapterId
//            NavigationArgs.sourceId
//            NavigationArgs.readingParagraph
//        }
//    )


    @OptIn(
        ExperimentalMaterialApi::class,
        ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val localizeHelper = LocalLocalizeHelper.currentOrThrow
        val vm: TTSViewModel =
            getIViewModel(parameters =
            { parametersOf(TTSViewModel.Param(sourceId,chapterId,bookId,readingParagraph))}
                )
        val context = LocalContext.current
        val lazyState = rememberLazyListState()
        DisposableEffect(key1 = true) {
            vm.initMedia(context)
            onDispose {
                vm.browser?.disconnect()
            }
        }
        val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        IModalDrawer(
            state = drawerState,
            sheetContent = {
                val scope = rememberCoroutineScope()
                val drawerScrollState = rememberLazyListState()
                val chapter = vm.ttsChapter
                LaunchedEffect(key1 =drawerState.targetValue) {
                    if (chapter != null && drawerState.targetValue == androidx.compose.material3.DrawerValue.Open && vm.ttsChapters.isNotEmpty()) {

                        val index = vm.ttsChapters.indexOfFirst { it.id == chapter.id }
                        if (index != -1) {
                            drawerScrollState.scrollToItem(index)
                        }
                    }
                }
                ReaderScreenDrawer(
                    modifier = Modifier.statusBarsPadding(),
                    onReverseIcon = {
                        if (vm.ttsChapter != null) {
                            vm.isDrawerAsc = !vm.isDrawerAsc
                        }
                    },
                    onChapter = { ch ->
                        vm.getLocalChapter(ch.id)
                    },
                    chapter = vm.ttsChapter,
                    chapters = vm.uiChapters.value,
                    drawerScrollState = drawerScrollState,
                    onMap = { drawer ->
                        scope.launch {
                            try {
                                val index =
                                    vm.ttsState.uiChapters.value.indexOfFirst { it.id == vm.ttsChapter?.id }
                                if (index != -1) {
                                    drawer.scrollToItem(
                                        index,
                                        -drawer.layoutInfo.viewportEndOffset / 2
                                    )
                                }
                            } catch (e: Throwable) {
                            }
                        }
                    },
                )
            }
        ) {
            IModalSheets(
                sheetContent = {
                    LaunchedEffect(key1 = sheetState.hashCode()) {
                        sheetState.hide()
                    }

                    Column(
                        modifier = it
                            .padding(16.dp)
                    ) {
                        ChipChoicePreference(
                            preference = vm.voice,
                            choices = vm.uiVoices.associate { voice ->
                                return@associate voice to voice.localDisplayName
                            },
                            title = localizeHelper.localize(MR.strings.voices),
                            onFailToFindElement = localizeHelper.localize(MR.strings.system_default)
                        )
                        ChipChoicePreference(
                            preference = vm.language,
                            choices = vm.languages.associate { language ->
                                return@associate language.displayName to language.displayName
                            },
                            title = localizeHelper.localize(MR.strings.languages),
                            onFailToFindElement = localizeHelper.localize(MR.strings.system_default)
                        )
                        ThemePreference(onBackgroundChange = {
                            vm.theme.value = it
                        }, themes = readerThemes, selected = {
                            vm.theme.value == it
                        })
                        SwitchPreference(
                            preference = vm.isTtsTrackerEnable,
                            title = localizeHelper.localize(MR.strings.tracker)
                        )
                        SwitchPreference(
                            preference = vm.autoNext,
                            title = localizeHelper.localize(MR.strings.auto_next_chapter)
                        )
                        SliderPreference(
                            title = localize(MR.strings.speech_rate),
                            preferenceAsFloat = vm.speechRate,
                            valueRange = .5F..3F,
                            trailing = vm.speechRate.value.toBigDecimal()
                                .setScale(1, RoundingMode.FLOOR)
                                .toString()
                        )
                        SliderPreference(
                            title = localize(MR.strings.pitch),
                            preferenceAsFloat = vm.speechPitch,
                            valueRange = .5F..2.1F,
                            trailing = vm.speechPitch.value.toBigDecimal()
                                .setScale(1, RoundingMode.FLOOR)
                                .toString()
                        )
                        SwitchPreference(
                            preference = vm.sleepModeUi,
                            title = localizeHelper.localize(MR.strings.enable_sleep_timer)
                        )
                        SliderPreference(
                            title = localize(MR.strings.sleep),
                            preferenceAsLong = vm.sleepTimeUi,
                            valueRange = 0F..60F,
                            trailing = "${vm.sleepTimeUi.value.toInt()} M",
                            isEnable = vm.sleepModeUi.value
                        )
                        Components.Chip(
                            preference = listOf(
                                localizeHelper.localize(MR.strings.top_left),
                                localizeHelper.localize(MR.strings.bottom_left),
                                localizeHelper.localize(MR.strings.hide),
                            ),
                            title = localizeHelper.localize(MR.strings.alignment),
                            onValueChange = {
                                when (it) {
                                    0 -> vm.ttsIconAlignments.value =
                                        PreferenceValues.PreferenceAlignment.TopLeft
                                    1 -> vm.ttsIconAlignments.value =
                                        PreferenceValues.PreferenceAlignment.BottomLeft
                                    2 -> vm.ttsIconAlignments.value =
                                        PreferenceValues.PreferenceAlignment.Hide
                                }
                            },
                            selected = vm.ttsIconAlignments.value.ordinal
                        ).Build()
                    }
                },
                bottomSheetState = sheetState,
            ) {
                IScaffold(
                    topBar = { scrollBehavior ->
                        val scope = rememberCoroutineScope()
                        CustomizeAnimateVisibility(visible = !vm.fullScreenMode) {
                            TTSTopBar(
                                onPopBackStack = {
                                    popBackStack(navigator)
                                },
                                scrollBehavior = scrollBehavior,
                                onSetting = {
                                    scope.launch {
                                        sheetState.show()
                                    }
                                },
                                onContent = {
                                    scope.launch {
                                      drawerState.animateTo(
                                            DrawerValue.Open,
                                            TweenSpec()
                                        )
                                    }
                                },
                                title = vm.ttsChapter?.name?:"",
                                    subtitle = vm.ttsBook?.title?:""
                            )
                        }
                    },
                    bottomBar = {
                        CustomizeAnimateVisibility(visible = !vm.fullScreenMode, goUp = false) {
                            androidx.compose.material3.BottomAppBar(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(NavigationBarTokens.ContainerHeight * 2),
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                content = {
                                    Column() {
                                        TTLScreenPlay(
                                            modifier = Modifier.padding(PaddingValues(0.dp)),
                                            vm = vm,
                                            onValueChange = {
                                                vm.controller?.transportControls?.seekTo(it.toLong())
                                            },
                                            onValueChangeFinished = {},
                                            content = vm.ttsContent?.value
                                        )
                                        MediaControllers(
                                            vm = vm,
                                            onPrev = {
                                                vm.controller?.transportControls?.skipToPrevious()
                                            },
                                            onPlay = {
                                                vm.play(context)
                                            },
                                            onNext = {
                                                vm.controller?.transportControls?.skipToNext()
                                            },
                                            onPrevPar = {
                                                vm.controller?.transportControls?.rewind()
                                            },
                                            onNextPar = {
                                                vm.controller?.transportControls?.fastForward()
                                            },
                                        )
                                    }
                                }
                            )
                        }
                    }
                ) { scaffoldPadding ->
                    TTSScreen(
                        modifier = Modifier,
                        vm = vm,
                        onChapter = { ch ->
                            vm.getLocalChapter(ch.id)
                        },
                        source = vm.ttsSource,
                        onPopStack = {
                            popBackStack(navigator)
                        },
                        lazyState = lazyState,
                        bottomSheetState = sheetState,
                        drawerState = drawerState,
                        paddingValues = scaffoldPadding,
                        onPlay = {
                            vm.play(context)
                        }
                    )

                }
            }

            LaunchedEffect(key1 = vm.ttsState.currentReadingParagraph) {
                try {
                    if (vm.currentReadingParagraph != vm.prevPar && vm.ttsState.currentReadingParagraph < lazyState.layoutInfo.totalItemsCount) {
                        if (vm.currentReadingParagraph !in lazyState.layoutInfo.visibleItemsInfo.map { it.index }
                                .dropLast(2) || vm.isTtsTrackerEnable.value) {
                            lazyState.scrollToItem(
                                vm.currentReadingParagraph
                            )
                        }

                        if (vm.isPlaying) {
                            delay(100)
                            vm.controller?.transportControls?.seekTo(vm.ttsState.currentReadingParagraph.toLong())
                        } else {
                            vm.controller?.transportControls?.seekTo(vm.ttsState.currentReadingParagraph.toLong())
                        }
                    }
                } catch (e: Throwable) {
                    Log.error(e, "")
                }
            }
        }
    }


}
