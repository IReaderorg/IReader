package org.ireader.presentation.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.launch
import org.ireader.core.R
import org.ireader.core.utils.UiText
import org.ireader.core_ui.theme.TransparentStatusBar
import org.ireader.domain.ui.NavigationArgs
import org.ireader.presentation.feature_reader.presentation.reader.ReadingScreen
import org.ireader.presentation.feature_reader.presentation.reader.reverse_swip_refresh.rememberSwipeRefreshState
import org.ireader.presentation.feature_reader.presentation.reader.viewmodel.ReaderScreenViewModel
import org.ireader.presentation.feature_ttl.TTSScreen
import org.ireader.presentation.feature_ttl.TTSService.Companion.NEXT_PAR
import org.ireader.presentation.feature_ttl.TTSService.Companion.PLAY_PAUSE
import org.ireader.presentation.feature_ttl.TTSService.Companion.PREV_PAR
import org.ireader.presentation.feature_ttl.TTSService.Companion.SKIP_NEXT
import org.ireader.presentation.feature_ttl.TTSService.Companion.SKIP_PREV
import org.ireader.presentation.presentation.EmptyScreenComposable

object ReaderScreenSpec : ScreenSpec {

    override val navHostRoute: String = "reader_screen_route/{bookId}/{chapterId}/{sourceId}"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.bookId,
        NavigationArgs.chapterId,
        NavigationArgs.sourceId,
    )

    fun buildRoute(
        bookId: Long,
        sourceId: Long,
        chapterId: Long,
    ): String {
        return "reader_screen_route/$bookId/$chapterId/$sourceId"
    }

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink {
            uriPattern =
                "https://www.ireader.org/reader_screen_route/{bookId}/{chapterId}/{sourceId}"
            NavigationArgs.bookId
            NavigationArgs.chapterId
            NavigationArgs.sourceId
        }
    )

    @OptIn(ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState,
    ) {
        val vm: ReaderScreenViewModel = hiltViewModel()
        val currentIndex = vm.currentChapterIndex
        val source = vm.ttsSource
        val chapters = vm.stateChapters
        val chapter = vm.stateChapter
        val scope = rememberCoroutineScope()
        val scrollState = rememberLazyListState()
        val drawerScrollState = rememberLazyListState()
        val swipeState = rememberSwipeRefreshState(isRefreshing = vm.ttsIsLoading)
        val context = LocalContext.current

        DisposableEffect(key1 = true) {
            onDispose {
                vm.uiFunc.apply {
                    vm.restoreSetting(context, scrollState)
                }
            }
        }



        if (source != null) {
            when {
                vm.voiceMode -> {

                    TTSScreen(
                        vm = vm,
                        onPrev = {
                            vm.runTTSService(context, SKIP_PREV)
                        },
                        onPlay = {
                            vm.runTTSService(context, PLAY_PAUSE)
                        },
                        onNext = {
                            vm.runTTSService(context, SKIP_NEXT)
                        },
                        onChapter = { ch ->
                            scope.launch {
                                vm.mainFunc.apply {
                                    vm.getChapter(ch.id,
                                        source = source) {
                                        vm.runTTSService(context)
                                    }
                                }

                            }
                            scope.launch {
                                scrollState.scrollToItem(0, 0)
                            }
                            vm.uiFunc.apply {
                                vm.updateChapterSliderIndex(vm.getCurrentIndexOfChapter(
                                    ch))
                            }
                        },
                        source = source,
                        navController = navController,
                        onPrevPar = {
                            vm.runTTSService(context, PREV_PAR)
                        },
                        onNextPar = {
                            vm.runTTSService(context, NEXT_PAR)
                        },
                        onValueChange = {
                            vm.ttsState.tts.stop()
                            vm.currentReadingParagraph = it.toInt()
                        },
                        onValueChangeFinished = {
                            if (vm.isPlaying) {
                                vm.runTTSService(context)
                            }
                        }
                    )
                }
                else -> {


                    TransparentStatusBar {

                        ReadingScreen(
                            navController = navController,
                            vm = vm,
                            scrollState = scrollState,
                            source = source,
                            onNext = {
                                if (currentIndex < chapters.lastIndex) {
                                    vm.uiFunc.apply {
                                        vm.mainFunc.apply {
                                            vm.updateChapterSliderIndex(currentIndex, true)
                                            scope.launch {
                                                vm.getChapter(vm.getCurrentChapterByIndex().id,
                                                    source = source)
                                                scrollState.scrollToItem(0, 0)
                                            }
                                        }
                                    }

                                } else {
                                    scope.launch {
                                        vm.showSnackBar(UiText.StringResource(R.string.this_is_last_chapter))

                                    }
                                }
                            },
                            onPrev = { scrollToEnd ->
                                if (currentIndex > 0) {
                                    vm.uiFunc.apply {
                                        vm.mainFunc.apply {
                                            vm.updateChapterSliderIndex(currentIndex, false)
                                            scope.launch {
                                                vm.getChapter(vm.getCurrentChapterByIndex().id,
                                                    source = source)
                                                when (scrollToEnd) {
                                                    true -> {
                                                        if (chapter != null) {
                                                            scrollState.scrollToItem(chapter.content.lastIndex,
                                                                Int.MAX_VALUE)
                                                        }
                                                    }
                                                    else -> {
                                                        scrollState.scrollToItem(0, 0)
                                                    }
                                                }

                                            }
                                        }
                                    }

                                } else {
                                    scope.launch {
                                        vm.showSnackBar(UiText.StringResource(org.ireader.core.R.string.this_is_first_chapter))
                                    }
                                }
                            },
                            onSliderFinished = {
                                scope.launch {
                                    vm.showSnackBar(UiText.DynamicString(chapters[vm.currentChapterIndex].title))
                                }
                                vm.uiFunc.apply {
                                    vm.mainFunc.apply {
                                        vm.updateChapterSliderIndex(currentIndex)
                                        scope.launch {
                                            vm.getChapter(chapters[vm.currentChapterIndex].id,
                                                source = source)
                                        }
                                    }
                                }
                                scope.launch {
                                    scrollState.animateScrollToItem(0, 0)
                                }
                            },
                            onSliderChange = {
                                vm.uiFunc.apply {
                                    vm.updateChapterSliderIndex(it.toInt())
                                }
                            },
                            swipeState = swipeState,
                            drawerScrollState = drawerScrollState,
                            onChapter = { ch ->
                                vm.uiFunc.apply {
                                    vm.mainFunc.apply {
                                        scope.launch {
                                            vm.getChapter(ch.id,
                                                source = source)
                                        }
                                        scope.launch {
                                            scrollState.scrollToItem(0, 0)
                                        }
                                        vm.updateChapterSliderIndex(vm.getCurrentIndexOfChapter(
                                            ch))
                                    }
                                }

                            }
                        )
                    }


                }
            }


        } else {
            EmptyScreenComposable(navController = navController,
                errorResId = org.ireader.presentation.R.string.something_is_wrong_with_this_book)
        }
    }
}