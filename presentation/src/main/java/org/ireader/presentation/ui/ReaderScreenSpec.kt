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
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.view_models.reader.ReaderScreenViewModel
import org.ireader.presentation.feature_reader.presentation.reader.ReadingScreen
import org.ireader.presentation.feature_reader.presentation.reader.reverse_swip_refresh.rememberSwipeRefreshState
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
        val viewModel: ReaderScreenViewModel = hiltViewModel()
        val currentIndex = viewModel.currentChapterIndex
        val source = viewModel.source
        val chapters = viewModel.stateChapters
        val coroutineScope = rememberCoroutineScope()
        val scrollState = rememberLazyListState()
        val swipeState = rememberSwipeRefreshState(isRefreshing = viewModel.isLoading)

        if (source != null) {
            ReadingScreen(
                navController = navController,
                vm = viewModel,
                scrollState = scrollState,
                source = source,
                onNext = {
                    if (currentIndex < chapters.lastIndex) {
                        viewModel.updateChapterSliderIndex(currentIndex + 1)
                        viewModel.getChapter(viewModel.getCurrentChapterByIndex().id,
                            source = source) {
                            coroutineScope.launch {
                                scrollState.animateScrollToItem(0, 0)
                            }
                        }
                    } else {
                        coroutineScope.launch {
                            viewModel.showSnackBar(UiText.StringResource(R.string.this_is_last_chapter))

                        }
                    }
                },
                onPrev = {
                    if (currentIndex > 0) {
                        viewModel.updateChapterSliderIndex(currentIndex - 1)
                        viewModel.getChapter(viewModel.getCurrentChapterByIndex().id,
                            source = source) {
                            coroutineScope.launch {
                                scrollState.animateScrollToItem(0, 0)
                            }
                        }

                    } else {
                        coroutineScope.launch {
                            viewModel.showSnackBar(UiText.StringResource(org.ireader.core.R.string.this_is_first_chapter))
                        }
                    }
                },
                onSliderFinished = {
                    coroutineScope.launch {
                        viewModel.showSnackBar(UiText.DynamicString(chapters[viewModel.currentChapterIndex].title))
                    }
                    viewModel.updateChapterSliderIndex(currentIndex)
                    viewModel.getChapter(chapters[viewModel.currentChapterIndex].id,
                        source = source)
                    coroutineScope.launch {
                        scrollState.animateScrollToItem(0, 0)
                    }
                },
                onSliderChange = {
                    viewModel.updateChapterSliderIndex(it.toInt())
                },
                swipeState = swipeState
            )
        } else {
            EmptyScreenComposable(navController = navController,
                errorResId = org.ireader.presentation.R.string.something_is_wrong_with_this_book)
        }
    }

}