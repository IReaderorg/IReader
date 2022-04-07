package org.ireader.presentation.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.launch
import org.ireader.core.R
import org.ireader.core.utils.UiText
import org.ireader.core_ui.theme.TransparentStatusBar
import org.ireader.domain.feature_services.notification.Notifications
import org.ireader.domain.ui.NavigationArgs
import org.ireader.presentation.feature_reader.presentation.reader.ReadingScreen
import org.ireader.presentation.feature_reader.presentation.reader.reverse_swip_refresh.rememberSwipeRefreshState
import org.ireader.presentation.feature_reader.presentation.reader.viewmodel.ReaderScreenViewModel
import org.ireader.presentation.feature_ttl.TTSScreen
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
        val source = vm.source
        val chapters = vm.stateChapters
        val chapter = vm.stateChapter
        val scope = rememberCoroutineScope()
        val scrollState = rememberLazyListState()
        val drawerScrollState = rememberLazyListState()
        val swipeState = rememberSwipeRefreshState(isRefreshing = vm.isLoading)
        val context = LocalContext.current

        DisposableEffect(key1 = true) {
            onDispose {
                vm.speaker?.shutdown()
                vm.mediaSessionCompat(context).release()
                NotificationManagerCompat.from(context)
                    .cancel(Notifications.ID_TEXT_READER_PROGRESS)
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
                            if (vm.currentChapterIndex > 0) {
                                vm.apply {
                                    currentChapterIndex -= 1
                                    scope.launch {
                                        vm.getChapter(vm.getCurrentChapterByIndex().id,
                                            source = source)
                                        scrollState.animateScrollToItem(0, 0)
                                    }
                                }
                            } else {
                                scope.launch {
                                    vm.showSnackBar(UiText.StringResource(org.ireader.core.R.string.this_is_first_chapter))
                                }
                            }
                        },
                        onPlay = {
                            when {
                                vm.isPlaying -> {
                                    vm.speaker?.stop()
                                }
                                else -> {
                                    vm.textReaderManager.apply {
                                        vm.readText(context, vm.mediaSessionCompat(context))
                                    }
                                }
                            }
                            scope.launch {
                                vm.state.stateChapter?.let { chapter ->
                                    vm.state.book?.let { book ->
                                        val notification =
                                            vm.defaultNotificationHelper.basicPlayingTextReaderNotification(
                                                chapter,
                                                book,
                                                vm.isPlaying,
                                                vm.currentReadingParagraph,
                                                vm.mediaSessionCompat(context))
                                        NotificationManagerCompat.from(context)
                                            .notify(Notifications.ID_TEXT_READER_PROGRESS,
                                                notification.build())
                                    }
                                }
                            }
                        },
                        onNext = {
                            if (currentIndex < chapters.lastIndex) {
                                vm.apply {
                                    currentChapterIndex += 1
                                    scope.launch {
                                        vm.getChapter(vm.getCurrentChapterByIndex().id,
                                            source = source)
                                        scrollState.animateScrollToItem(0, 0)
                                    }

                                }

                            } else {
                                scope.launch {
                                    vm.showSnackBar(UiText.StringResource(R.string.this_is_last_chapter))

                                }
                            }
                        },
                        onChapter = { ch ->
                            scope.launch {
                                vm.mainFunc.apply {
                                    vm.getChapter(ch.id,
                                        source = source)
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
                            if (vm.currentReadingParagraph > 0) {
                                vm.speaker?.stop()
                                vm.currentReadingParagraph -= 1
                                if (vm.isPlaying) {
                                    vm.textReaderManager.apply {
                                        vm.readText(context, vm.mediaSessionCompat(context))
                                    }

                                }
                            }
                        },
                        onNextPar = {
                            vm.stateChapter?.let { chapter ->
                                if (vm.currentReadingParagraph < chapter.content.lastIndex) {
                                    vm.speaker?.stop()
                                    vm.currentReadingParagraph += 1
                                    if (vm.isPlaying) {
                                        vm.textReaderManager.apply {
                                            vm.readText(context, vm.mediaSessionCompat(context))
                                        }

                                    }
                                }
                            }
                        },
                        onValueChange = {
                            vm.speaker?.stop()
                            vm.currentReadingParagraph = it.toInt()
                        },
                        onValueChangeFinished = {
                            if (vm.isPlaying) {
                                vm.textReaderManager.apply {
                                    vm.readText(context, vm.mediaSessionCompat(context))
                                }
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
                                            vm.updateChapterSliderIndex(currentIndex + 1)
                                            scope.launch {
                                                vm.getChapter(vm.getCurrentChapterByIndex().id,
                                                    source = source)
                                                scrollState.animateScrollToItem(0, 0)
                                            }
                                        }
                                    }

                                } else {
                                    scope.launch {
                                        vm.showSnackBar(UiText.StringResource(R.string.this_is_last_chapter))

                                    }
                                }
                            },
                            onPrev = {
                                if (currentIndex > 0) {
                                    vm.uiFunc.apply {
                                        vm.mainFunc.apply {
                                            vm.updateChapterSliderIndex(currentIndex - 1)
                                            scope.launch {
                                                vm.getChapter(vm.getCurrentChapterByIndex().id,
                                                    source = source)
                                                scrollState.animateScrollToItem(0, 0)
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