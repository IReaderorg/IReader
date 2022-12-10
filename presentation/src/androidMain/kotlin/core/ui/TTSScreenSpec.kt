package ireader.presentation.core.ui

import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import com.google.accompanist.pager.ExperimentalPagerApi
import ireader.core.log.Log
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.models.prefs.readerThemes
import ireader.presentation.R
import ireader.presentation.core.ui.util.NavigationArgs
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.CustomizeAnimateVisibility
import ireader.presentation.ui.component.NavigationBarTokens
import ireader.presentation.ui.component.ThemePreference
import ireader.presentation.ui.component.components.Build
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.component.ChipChoicePreference
import ireader.presentation.ui.component.components.component.SliderPreference
import ireader.presentation.ui.component.components.component.SwitchPreference
import ireader.presentation.ui.home.tts.MediaControllers
import ireader.presentation.ui.home.tts.TTLScreenPlay
import ireader.presentation.ui.home.tts.TTSScreen
import ireader.presentation.ui.home.tts.TTSTopBar
import ireader.presentation.ui.home.tts.TTSViewModel
import ireader.presentation.ui.reader.ReaderScreenDrawer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import java.math.RoundingMode

object TTSScreenSpec : ScreenSpec {
    override val navHostRoute: String = "tts_screen_route/{bookId}/{chapterId}/{sourceId}"

    fun buildRoute(
        bookId: Long,
        sourceId: Long,
        chapterId: Long,
    ): String {
        return "tts_screen_route/$bookId/$chapterId/$sourceId"
    }

    fun buildDeepLink(
        bookId: Long,
        sourceId: Long,
        chapterId: Long,
        readingParagraph: Long,
    ): String {
        return "https://www.ireader.org/tts_screen_route/$bookId/$chapterId/$sourceId/$readingParagraph"
    }

    override val deepLinks: List<NavDeepLink> = listOf(

        navDeepLink {
            uriPattern =
                "https://www.ireader.org/tts_screen_route/{bookId}/{chapterId}/{sourceId}/{readingParagraph}"
            NavigationArgs.bookId
            NavigationArgs.chapterId
            NavigationArgs.sourceId
            NavigationArgs.readingParagraph
        }
    )
    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.bookId,
        NavigationArgs.chapterId,
        NavigationArgs.sourceId,
        NavigationArgs.showModalSheet,
        NavigationArgs.haveDrawer,
        NavigationArgs.haveCustomizedBottomBar,

        )

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,)
    @Composable
    override fun TopBar(controller: Controller) {
        val vm: TTSViewModel = getViewModel(owner = controller.navBackStackEntry, parameters = {
            org.koin.core.parameter.parametersOf(
                TTSViewModel.createParam(controller)
            )
        })
        val scope = rememberCoroutineScope()
        CustomizeAnimateVisibility(visible = !vm.fullScreenMode) {
            TTSTopBar(
                onPopBackStack = {
                    controller.navController.popBackStack()
                },
                scrollBehavior = controller.scrollBehavior,
                onSetting = {
                    scope.launch {
                        controller.sheetState.show()
                    }
                },
                onContent = {
                    scope.launch {
                        controller.drawerState.animateTo(
                            DrawerValue.Open,
                            TweenSpec()
                        )
                    }
                },
                vm = vm,
            )
        }
    }

    @OptIn(
        ExperimentalMaterialApi::class, ExperimentalPagerApi::class,
        ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: TTSViewModel = getViewModel(owner = controller.navBackStackEntry, parameters = {
            org.koin.core.parameter.parametersOf(
                TTSViewModel.createParam(controller)
            )
        })
        val context = LocalContext.current
        val lazyState = rememberLazyListState()
        DisposableEffect(key1 = true ) {
            vm.initMedia(context)
            onDispose {
                vm.browser?.disconnect()
            }
        }
        TTSScreen(
            modifier = Modifier,
            vm = vm,
            onChapter = { ch ->
                vm.getLocalChapter(ch.id)
            },
            source = vm.ttsSource,
            onPopStack = {
                controller.navController.popBackStack()
            },
            lazyState = lazyState,
            bottomSheetState = controller.sheetState,
            drawerState = controller.drawerState,
            paddingValues = controller.scaffoldPadding,
            onPlay = {
                vm.play(context)
            }
        )

        LaunchedEffect(key1 = vm.ttsState.currentReadingParagraph) {
            try {
                if (vm.currentReadingParagraph != vm.prevPar && vm.ttsState.currentReadingParagraph < lazyState.layoutInfo.totalItemsCount) {
                    if(vm.currentReadingParagraph !in lazyState.layoutInfo.visibleItemsInfo.map { it.index }.dropLast(2) || vm.isTtsTrackerEnable.value) {
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun BottomAppBar(controller: Controller) {
        val vm: TTSViewModel  = getViewModel(owner = controller.navBackStackEntry, parameters = {
            org.koin.core.parameter.parametersOf(
                TTSViewModel.createParam(controller)
            )
        })
        val context = LocalContext.current
        val scrollableTabsHeight = LocalDensity.current.run {
            NavigationBarTokens.ContainerHeight + (if (controller.scrollBehavior.state.heightOffset == controller.scrollBehavior.state.heightOffsetLimit) controller.scrollBehavior.state.heightOffset * 2 else controller.scrollBehavior.state.heightOffsetLimit).toDp()
        }
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
                            modifier = Modifier.padding(controller.scaffoldPadding),
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun ModalDrawer(
        controller: Controller
    ) {
        val vm: TTSViewModel = getViewModel(owner = controller.navBackStackEntry, parameters = {
            org.koin.core.parameter.parametersOf(
                TTSViewModel.createParam(controller)
            )
        })
        val scope = rememberCoroutineScope()
        val drawerScrollState = rememberLazyListState()
        val chapter = vm.ttsChapter
        LaunchedEffect(key1 = controller.drawerState.targetValue) {
            if (chapter != null && controller.drawerState.targetValue == androidx.compose.material3.DrawerValue.Open && vm.ttsChapters.isNotEmpty()) {

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


    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun BottomModalSheet(
        controller: Controller
    ) {
        val vm: TTSViewModel = getViewModel(owner = controller.navBackStackEntry, parameters = {
            org.koin.core.parameter.parametersOf(
                TTSViewModel.createParam(controller)
            )
        })

        LaunchedEffect(key1 = controller.sheetState.hashCode()) {
            controller.sheetState.hide()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            ChipChoicePreference(
                preference = vm.voice,
                choices = vm.uiVoices.associate { voice ->
                    return@associate voice to voice.localDisplayName
                },
                title = stringResource(id = R.string.voices),
                onFailToFindElement = stringResource(id = R.string.system_default)
            )
            ChipChoicePreference(
                preference = vm.language,
                choices = vm.languages.associate { language ->
                    return@associate language.displayName to language.displayName
                },
                title = stringResource(id = R.string.languages),
                onFailToFindElement = stringResource(id = R.string.system_default)
            )
            ThemePreference(onBackgroundChange = {
                vm.theme.value = it
            }, themes = readerThemes, selected = {
                vm.theme.value == it
            })
            SwitchPreference(
                preference = vm.isTtsTrackerEnable,
                title = stringResource(id = R.string.tracker)
            )
            SwitchPreference(
                preference = vm.autoNext,
                title = stringResource(id = R.string.auto_next_chapter)
            )
            SliderPreference(
                title = stringResource(R.string.speech_rate),
                preferenceAsFloat = vm.speechRate,
                valueRange = .5F..3F,
                trailing = vm.speechRate.value.toBigDecimal().setScale(1, RoundingMode.FLOOR)
                    .toString()
            )
            SliderPreference(
                title = stringResource(R.string.pitch),
                preferenceAsFloat = vm.speechPitch,
                valueRange = .5F..2.1F,
                trailing = vm.speechPitch.value.toBigDecimal().setScale(1, RoundingMode.FLOOR)
                    .toString()
            )
            SwitchPreference(
                preference = vm.sleepModeUi,
                title = stringResource(id = R.string.enable_sleep_timer)
            )
            SliderPreference(
                title = stringResource(R.string.sleep),
                preferenceAsLong = vm.sleepTimeUi,
                valueRange = 0F..60F,
                trailing = "${vm.sleepTimeUi.value.toInt()} M",
                isEnable = vm.sleepModeUi.value
            )
            Components.Chip(
                preference = listOf(
                    stringResource(id = R.string.top_left),
                    stringResource(id = R.string.bottom_left),
                    stringResource(id = R.string.hide),
                ),
                title = stringResource(id = R.string.alignment),
                onValueChange = {
                    when (it) {
                        0 -> vm.ttsIconAlignments.value = PreferenceValues.PreferenceAlignment.TopLeft
                        1 -> vm.ttsIconAlignments.value = PreferenceValues.PreferenceAlignment.BottomLeft
                        2 -> vm.ttsIconAlignments.value = PreferenceValues.PreferenceAlignment.Hide
                    }
                },
                selected = vm.ttsIconAlignments.value.ordinal
            ).Build()
        }
    }
}
