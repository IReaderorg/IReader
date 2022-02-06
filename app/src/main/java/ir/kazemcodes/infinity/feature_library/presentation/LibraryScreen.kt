package ir.kazemcodes.infinity.feature_library.presentation


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import ir.kazemcodes.infinity.core.presentation.components.handlePagingResult
import ir.kazemcodes.infinity.core.presentation.reusable_composable.ErrorTextWithEmojis
import ir.kazemcodes.infinity.feature_library.presentation.components.BottomTabComposable
import ir.kazemcodes.infinity.feature_library.presentation.components.LayoutComposable


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
    val pagerState = rememberPagerState()
    val bottomSheetState = rememberBottomSheetScaffoldState()

    val gridState = rememberLazyGridState()
    val lazyListState = rememberLazyListState()

    LaunchedEffect(key1 = true) {
        viewModel.setExploreModeOffForInLibraryBooks()
    }

    BottomSheetScaffold(
        topBar = {
            LibraryScreenTopBar(navController = navController,
                viewModel = viewModel,
                coroutineScope = coroutineScope,
                bottomSheetState = bottomSheetState)
        },
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
                        scrollState = lazyListState
                    )
                }
            }
        }

    }


}





