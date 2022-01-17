package ir.kazemcodes.infinity.feature_library.presentation

import androidx.compose.foundation.background
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
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.core.presentation.components.TitleText
import ir.kazemcodes.infinity.core.presentation.layouts.layouts
import ir.kazemcodes.infinity.core.presentation.reusable_composable.*
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.Constants
import ir.kazemcodes.infinity.feature_library.presentation.components.LayoutComposable
import ir.kazemcodes.infinity.feature_library.presentation.components.LibraryEvents
import ir.kazemcodes.infinity.feature_library.presentation.components.RadioButtonWithTitleComposable
import kotlinx.coroutines.launch


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
                    backgroundColor = MaterialTheme.colors.background,
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
                ModalBottomSheetLayout(sheetBackgroundColor = MaterialTheme.colors.background,
                    modifier = Modifier.height(500.dp),
                    sheetContent = {
                        /** There is Some issue here were sheet content is not need , not sure why**/
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                                .background(MaterialTheme.colors.background),
                            content = {}
                        )

                    }) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.background)
                            .padding(12.dp)
                    ) {
                        TitleText(text = "Display")
                        layouts.forEach { layout ->
                            RadioButtonWithTitleComposable(
                                text = layout.title,
                                selected = viewModel.state.value.layout == layout.layout,
                                onClick = {
                                    viewModel.onEvent(LibraryEvents.UpdateLayoutType(layout))
                                }
                            )
                        }
                    }
                }

            },
            scaffoldState = bottomSheetScaffoldState
        ) {

            Box(Modifier.fillMaxSize()) {
                Box(modifier = Modifier.padding(bottom = 50.dp)) {
                    if (books.loadState.refresh is LoadState.NotLoading) {
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



