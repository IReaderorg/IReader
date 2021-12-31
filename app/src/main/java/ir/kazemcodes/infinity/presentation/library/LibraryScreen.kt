package ir.kazemcodes.infinity.presentation.library

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.presentation.book_detail.Constants
import ir.kazemcodes.infinity.presentation.components.TitleText
import ir.kazemcodes.infinity.presentation.layouts.layouts
import ir.kazemcodes.infinity.presentation.library.components.LayoutComposable
import ir.kazemcodes.infinity.presentation.library.components.LibraryEvents
import ir.kazemcodes.infinity.presentation.library.components.RadioButtonWithTitleComposable
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarActionButton
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarBackButton
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarSearch
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarTitle
import kotlinx.coroutines.launch


@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun LibraryScreen(
) {
    val viewModel = rememberService<LibraryViewModel>()
    val backStack = LocalBackstack.current
    val state = viewModel.state.value
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
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
                                    viewModel.onEvent(LibraryEvents.SearchBooks(state.searchQuery))
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
                                onClick = { viewModel.onEvent(LibraryEvents.ToggleSearchMode(false)) },
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
                            TopAppBarBackButton(backStack = backStack,onClick = {viewModel.onEvent(LibraryEvents.ToggleSearchMode(false))})
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
                                onClick = { viewModel.onEvent(LibraryEvents.UpdateLayoutType(layout)) }
                            )
                        }
                    }
                }

            },
            scaffoldState = bottomSheetScaffoldState
        ) {
            if (state.books.isNotEmpty()) {
                Box(modifier = Modifier.padding(bottom = 50.dp)) {
                    LayoutComposable(
                        books = if (state.searchedBook.isEmpty()) state.books else state.searchedBook,
                        layout = state.layout,
                    )
                }


            }

            if (state.error.isNotBlank()) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colors.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .align(Alignment.Center)
                )
            }
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }


}

