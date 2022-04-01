package org.ireader.presentation.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.launch
import org.ireader.core.R
import org.ireader.core.utils.UiText
import org.ireader.core_ui.theme.TransparentStatusBar
import org.ireader.domain.ui.NavigationArgs
import org.ireader.presentation.feature_reader.presentation.reader.ReadingScreen
import org.ireader.presentation.feature_reader.presentation.reader.reverse_swip_refresh.rememberSwipeRefreshState
import org.ireader.presentation.feature_reader.presentation.reader.viewmodel.ReaderScreenViewModel
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
        val scope = rememberCoroutineScope()
        val scrollState = rememberLazyListState()
        val drawerScrollState = rememberLazyListState()
        val swipeState = rememberSwipeRefreshState(isRefreshing = vm.isLoading)

        TransparentStatusBar {


            if (source != null) {
                ReadingScreen(
                    navController = navController,
                    vm = vm,
                    scrollState = scrollState,
                    source = source,
                    onNext = {
                        if (currentIndex < chapters.lastIndex) {
                            vm.updateChapterSliderIndex(currentIndex + 1)
                            scope.launch {
                                vm.getChapter(vm.getCurrentChapterByIndex().id,
                                    source = source)
                                scrollState.animateScrollToItem(0, 0)
                            }

                        } else {
                            scope.launch {
                                vm.showSnackBar(UiText.StringResource(R.string.this_is_last_chapter))

                            }
                        }
                    },
                    onPrev = {
                        if (currentIndex > 0) {
                            vm.updateChapterSliderIndex(currentIndex - 1)
                            scope.launch {
                                vm.getChapter(vm.getCurrentChapterByIndex().id,
                                    source = source)
                                scrollState.animateScrollToItem(0, 0)
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
                        vm.updateChapterSliderIndex(currentIndex)
                        scope.launch {
                            vm.getChapter(chapters[vm.currentChapterIndex].id,
                                source = source)
                        }
                        scope.launch {
                            scrollState.animateScrollToItem(0, 0)
                        }
                    },
                    onSliderChange = {
                        vm.updateChapterSliderIndex(it.toInt())
                    },
                    swipeState = swipeState,
                    drawerScrollState = drawerScrollState,
                    onChapter = { ch ->
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
                )
            } else {
                EmptyScreenComposable(navController = navController,
                    errorResId = org.ireader.presentation.R.string.something_is_wrong_with_this_book)
            }
        }
    }
}