package ir.kazemcodes.infinity.feature_library.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.core.presentation.reusable_composable.*
import ir.kazemcodes.infinity.core.presentation.theme.Colour.topBarColor
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.Constants
import ir.kazemcodes.infinity.feature_library.presentation.components.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@ExperimentalPagerApi
@ExperimentalAnimationApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LibraryScreen() {

    val backstack = LocalBackstack.current
    val viewModel = rememberService<LibraryViewModel>()

    val state = viewModel.state.value
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val books = viewModel.book.collectAsLazyPagingItems()
    val pagerState = rememberPagerState()


    Box(
        modifier = Modifier.fillMaxSize()
    ) {

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
                                    if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                                        bottomSheetScaffoldState.bottomSheetState.expand()
                                    } else {
                                        bottomSheetScaffoldState.bottomSheetState.collapse()
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
                            TopAppBarBackButton(backStack = backstack,
                                onClick = {
                                    viewModel.onEvent(LibraryEvents.ToggleSearchMode(false))
                                })
                        }
                    } else null

                )
            },
            sheetContent = {
                BottomTabComposable(viewModel = viewModel, pagerState = pagerState, scope = coroutineScope)
            },
            scaffoldState = bottomSheetScaffoldState
        ) {

            Box(Modifier.fillMaxSize()) {
                Box(modifier = Modifier.padding(bottom = 50.dp)) {
                    AnimatedContent(books.loadState.refresh is LoadState.NotLoading) {
                        LayoutComposable(
                            books = if (!state.inSearchMode) books else books,
                            layout = state.layout,
                            backStack = backstack,
                            isLocal = false
                        )
                    }
                }
                if (books.loadState.refresh is LoadState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                if (books.loadState.source.refresh is LoadState.NotLoading && books.loadState.append.endOfPaginationReached && books.itemCount < 1) {
                    ErrorTextWithEmojis(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .align(Alignment.Center),
                        error = "There is no book is Library"
                    )
                }
            }
        }
    }


}



@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun BottomTabComposable(modifier: Modifier = Modifier, viewModel: LibraryViewModel,pagerState: PagerState,scope: CoroutineScope) {
    val tabs = listOf(TabItem.Filter(viewModel = viewModel), TabItem.Sort(viewModel), TabItem.Display(viewModel = viewModel))

    ModalBottomSheetLayout(sheetBackgroundColor = MaterialTheme.colors.background,
        modifier = Modifier.height(500.dp),
        sheetContent = {
            /** There is Some issue here were sheet content is not need , not sure why**/
            Column(modifier = modifier.fillMaxSize()) {
                Tabs(tabs = tabs, pagerState = pagerState)
                TabsContent(tabs = tabs, pagerState = pagerState)

            }
        }, content = {
            Column(modifier = modifier.fillMaxSize()) {
                Tabs(tabs = tabs, pagerState = pagerState)
                TabsContent(tabs = tabs, pagerState = pagerState)
            }
        })

}





