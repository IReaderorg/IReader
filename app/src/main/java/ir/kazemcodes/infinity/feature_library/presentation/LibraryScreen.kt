package ir.kazemcodes.infinity.feature_library.presentation


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import ir.kazemcodes.infinity.core.presentation.components.handlePagingResult
import ir.kazemcodes.infinity.core.presentation.reusable_composable.*
import ir.kazemcodes.infinity.core.presentation.theme.Colour.topBarColor
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.feature_library.presentation.components.BottomTabComposable
import ir.kazemcodes.infinity.feature_library.presentation.components.LayoutComposable
import ir.kazemcodes.infinity.feature_library.presentation.components.LibraryEvents
import kotlinx.coroutines.launch


@ExperimentalPagerApi
@ExperimentalAnimationApi
@OptIn(ExperimentalMaterialApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: LibraryViewModel = hiltViewModel(),
) {


    val state = viewModel.state.value

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val books = viewModel.book.collectAsLazyPagingItems()
    val pagerState = rememberPagerState()
    val bottomSheetState = rememberBottomSheetScaffoldState()


    LaunchedEffect(key1 = true ) {
        viewModel.setExploreModeOffForInLibraryBooks()
    }


    BottomSheetScaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!state.inSearchMode) {
                        TopAppBarTitle(title = "Library")
                    } else {
                        TopAppBarSearch(query = state.searchQuery,
                            onValueChange = {
                                viewModel.onEvent(LibraryEvents.UpdateSearchInput(it))
                            },
                            onSearch = {
                                viewModel.searchBook(state.searchQuery)
                                focusManager.clearFocus()
                            },
                            isSearchModeEnable = state.searchQuery.isNotBlank())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.topBarColor,
                contentColor = MaterialTheme.colors.onBackground,
                elevation = Constants.DEFAULT_ELEVATION,
                actions = {
                    if (state.inSearchMode) {
                        TopAppBarActionButton(
                            imageVector = Icons.Default.Close,
                            title = "Close",
                            onClick = {
                                viewModel.onEvent(LibraryEvents.ToggleSearchMode(false))
                            },
                        )
                    }
                    TopAppBarActionButton(
                        imageVector = Icons.Default.Sort,
                        title = "Filter",
                        onClick = {
                            coroutineScope.launch {
                                if (bottomSheetState.bottomSheetState.isExpanded) {
                                    bottomSheetState.bottomSheetState.collapse()
                                } else {
                                    bottomSheetState.bottomSheetState.expand()
                                }
                            }
                        },
                    )
                    TopAppBarActionButton(
                        imageVector = Icons.Default.Search,
                        title = "Search",
                        onClick = {
                            viewModel.onEvent(LibraryEvents.ToggleSearchMode())

                        },
                    )


                },
                navigationIcon = if (state.inSearchMode) {
                    {
                        TopAppBarBackButton(navController = navController,
                            onClick = {
                                viewModel.onEvent(LibraryEvents.ToggleSearchMode(false))
                            })
                    }
                } else null

            )
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
                    )
                }
            }
        }

    }


}





