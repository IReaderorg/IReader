package org.ireader.presentation.feature_library.presentation


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import org.ireader.core.utils.Constants
import org.ireader.domain.view_models.library.LibraryViewModel
import org.ireader.presentation.feature_library.presentation.components.BottomTabComposable
import org.ireader.presentation.feature_library.presentation.components.LayoutComposable
import org.ireader.presentation.presentation.components.handlePagingResult
import org.ireader.presentation.presentation.reusable_composable.ErrorTextWithEmojis
import org.ireader.presentation.ui.BookDetailScreenSpec
import org.ireader.presentation.ui.ReaderScreenSpec


@ExperimentalPagerApi
@ExperimentalAnimationApi
@OptIn(ExperimentalMaterialApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: LibraryViewModel = hiltViewModel(),
) {


    val state = viewModel.state

    val coroutineScope = rememberCoroutineScope()

    val books = viewModel.book.collectAsLazyPagingItems()
    //val books = viewModel.books.collectAsLazyPagingItems()
    val pagerState = rememberPagerState()
    val bottomSheetState = rememberBottomSheetScaffoldState()

    val gridState = rememberLazyGridState()
    val lazyListState = rememberLazyListState()

    BottomSheetScaffold(
        sheetPeekHeight = (-1).dp,
        sheetContent = {
            if (bottomSheetState.bottomSheetState.isExpanded) {
                BottomTabComposable(
                    viewModel = viewModel,
                    pagerState = pagerState,
                    navController = navController,
                    scope = coroutineScope)
            }
        },
        scaffoldState = bottomSheetState
    ) {
        Column(modifier = Modifier
            .fillMaxSize()) {
            LibraryScreenTopBar(navController = navController,
                viewModel = viewModel,
                coroutineScope = coroutineScope,
                bottomSheetState = bottomSheetState)
            Box(modifier = Modifier
                .fillMaxSize()) {
                val result = handlePagingResult(books = books, onEmptyResult = {
                    ErrorTextWithEmojis(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .align(Alignment.Center),
                        error = "There is no book is Library, you can add books in the Explore screen"
                    )
                })
                if (result) {
                    AnimatedContent(books.loadState.refresh is LoadState.NotLoading) {
                        LayoutComposable(
                            books = if (!state.inSearchMode) books else books,
                            layout = state.layout,
                            navController = navController,
                            isLocal = true,
                            gridState = gridState,
                            scrollState = lazyListState,
                            goToLatestChapter = { book ->
                                navController.navigate(
                                    ReaderScreenSpec.buildRoute(
                                        bookId = book.id,
                                        sourceId = book.sourceId,
                                        chapterId = Constants.LAST_CHAPTER
                                    )
                                )
                            },
                            onBookTap = { book ->
                                navController.navigate(
                                    route = BookDetailScreenSpec.buildRoute(
                                        sourceId = book.sourceId,
                                        bookId = book.id)
                                )
                            }
                        )
                    }
                }
            }
        }


    }


}





