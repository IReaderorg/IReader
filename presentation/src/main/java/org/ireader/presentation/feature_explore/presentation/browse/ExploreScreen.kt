package org.ireader.presentation.feature_explore.presentation.browse

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.ExperimentalPagingApi
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import org.ireader.core.R
import org.ireader.core.SearchListing
import org.ireader.domain.FetchType
import org.ireader.domain.models.layouts
import org.ireader.domain.view_models.explore.ExploreScreenEvents
import org.ireader.domain.view_models.explore.ExploreViewModel
import org.ireader.presentation.feature_library.presentation.components.LayoutComposable
import org.ireader.presentation.feature_library.presentation.components.RadioButtonWithTitleComposable
import org.ireader.presentation.presentation.EmptyScreenComposable
import org.ireader.presentation.presentation.components.handlePagingResult
import org.ireader.presentation.presentation.reusable_composable.*
import org.ireader.presentation.ui.BookDetailScreenSpec
import org.ireader.presentation.ui.WebViewScreenSpec
import tachiyomi.source.HttpSource


@OptIn(ExperimentalFoundationApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
@ExperimentalPagingApi
@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: ExploreViewModel,
    onFilterClick: () -> Unit,
) {
    val scrollState = rememberLazyListState()
    val state = viewModel.state.value
    val filterState = viewModel.filterState.value

    val source = viewModel.state.value.source
    val focusManager = LocalFocusManager.current

    val books = viewModel.books.collectAsLazyPagingItems()
    var isFilterMode by remember {
        mutableStateOf(false)
    }
    val gridState = rememberLazyGridState()
    val lazyListState = rememberLazyListState()
    val bottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    if (source != null) {

        ModalBottomSheetLayout(
            sheetState = bottomSheetState,
            sheetContent = {
                Box(modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                ) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            isFilterMode = true
                        },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        MidSizeTextComposable(text = "SortBy:")
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically) {
                            MidSizeTextComposable(text = filterState.sortBy)
                            TopAppBarActionButton(imageVector = Icons.Default.ArrowDownward,
                                title = "",
                                onClick = { })
                        }
                        DropdownMenu(
                            modifier = Modifier.background(MaterialTheme.colors.background),
                            expanded = isFilterMode,//viewModel.state.isMenuExpanded,
                            onDismissRequest = {
                                isFilterMode = false
                            },
                        ) {
                            source.getListings().filter { !it.name.contains("Search") }.forEach {
                                DropdownMenuItem(onClick = {
                                    isFilterMode = false
                                    viewModel.getBooks(listing = it, source = source)
                                    viewModel.filterState.value = filterState.copy(sortBy = it.name)
                                }) {
                                    MidSizeTextComposable(text = it.name)
                                }
                            }
                        }
                    }
                }
            },
            sheetBackgroundColor = MaterialTheme.colors.background,
            sheetShape = RoundedCornerShape(8.dp),
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            if (!state.isSearchModeEnable) {
                                TopAppBarTitle(title = source.name)
                            } else {
                                TopAppBarSearch(query = state.searchQuery,
                                    onValueChange = {
                                        viewModel.onEvent(ExploreScreenEvents.OnQueryChange(it))
                                    },
                                    onSearch = {
                                        viewModel.getBooks(
                                            query = state.searchQuery,
                                            listing = SearchListing(),
                                            source = source
                                        )
                                        focusManager.clearFocus()
                                    },
                                    isSearchModeEnable = state.searchQuery.isNotBlank())
                            }
                        },
                        backgroundColor = MaterialTheme.colors.background,
                        actions = {
                            if (state.isSearchModeEnable) {
                                TopAppBarActionButton(
                                    imageVector = Icons.Default.Close,
                                    title = "Close",
                                    onClick = {
                                        viewModel.onEvent(ExploreScreenEvents.ToggleSearchMode(false))
                                    },
                                )
                            } else if (source.getListings()
                                    .find { it.name.contains("Search", ignoreCase = true) } != null
                            ) {
                                TopAppBarActionButton(
                                    imageVector = Icons.Default.Search,
                                    title = "Search",
                                    onClick = {
                                        viewModel.onEvent(ExploreScreenEvents.ToggleSearchMode(true))
                                    },
                                )
                            }
                            TopAppBarActionButton(
                                imageVector = Icons.Default.Public,
                                title = "WebView",
                                onClick = {
                                    navController.navigate(WebViewScreenSpec.buildRoute(
                                        sourceId = source.id,
                                        fetchType = FetchType.LatestFetchType.index,
                                        url = (source as HttpSource).baseUrl
                                    )
                                    )
                                },
                            )
                            TopAppBarActionButton(
                                imageVector = Icons.Default.GridView,
                                title = "Menu",
                                onClick = {
                                    viewModel.onEvent(ExploreScreenEvents.ToggleMenuDropDown(true))
                                },
                            )
                            DropdownMenu(
                                modifier = Modifier.background(MaterialTheme.colors.background),
                                expanded = viewModel.state.value.isMenuDropDownShown,
                                onDismissRequest = {
                                    viewModel.onEvent(ExploreScreenEvents.ToggleMenuDropDown(false))
                                }
                            ) {
                                layouts.forEach { layout ->
                                    DropdownMenuItem(onClick = {
                                        viewModel.onEvent(ExploreScreenEvents.OnLayoutTypeChnage(
                                            layoutType = layout))
                                        viewModel.onEvent(ExploreScreenEvents.ToggleMenuDropDown(
                                            false))
                                    }) {
                                        RadioButtonWithTitleComposable(
                                            text = layout.title,
                                            selected = viewModel.state.value.layout == layout.layout,
                                            onClick = {
                                                viewModel.onEvent(ExploreScreenEvents.OnLayoutTypeChnage(
                                                    layoutType = layout))
                                                viewModel.onEvent(ExploreScreenEvents.ToggleMenuDropDown(
                                                    false))
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        navigationIcon = {
                            TopAppBarBackButton(navController = navController)

                        },
                    )
                },
                floatingActionButtonPosition = FabPosition.End,
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        text = { MidSizeTextComposable(text = stringResource(org.ireader.presentation.R.string.filter)) },
                        onClick = {
                            scope.launch {
                                bottomSheetState.show()
                            }
                        },
                        icon = { Icon(Icons.Filled.Add, "") }
                    )
                },

                ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val result = handlePagingResult(books = books,
                        onEmptyResult = {},
                        onErrorResult = { error ->
                            Column(
                                modifier = modifier
                                    .fillMaxSize()
                                    .align(Alignment.Center)
                                    .padding(bottom = 30.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                ErrorTextWithEmojis(
                                    error = error.toString(),
                                modifier = Modifier
                                    .padding(20.dp)
                            )
                            Row(Modifier
                                .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier
                                    .weight(.5f)
                                    .wrapContentSize(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    TopAppBarActionButton(imageVector = Icons.Default.Refresh,
                                        title = "Retry",
                                        onClick = { viewModel.getBooks(source = source) })
                                    SmallTextComposable(text = "Retry")
                                }
                                Column(Modifier
                                    .weight(.5f)
                                    .wrapContentSize(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    TopAppBarActionButton(imageVector = Icons.Default.Public,
                                        title = "Open in WebView",
                                        onClick = {
                                            navController.navigate(WebViewScreenSpec.buildRoute(
                                                sourceId = source.id,
                                                fetchType = FetchType.LatestFetchType.index,
                                                url = (source as HttpSource).baseUrl
                                            )
                                            )
                                        })
                                    SmallTextComposable(text = "Open in WebView")
                                }

                            }

                        }

                    })
                    if (result) {
                        LayoutComposable(
                            books = books,
                            layout = state.layout,
                            scrollState = scrollState,
                            source = source,
                            navController = navController,
                            isLocal = false,
                            gridState = gridState,
                            onBookTap = { book ->
                                navController.navigate(
                                    route = BookDetailScreenSpec.buildRoute(sourceId = book.sourceId,
                                        bookId = book.id)
                                )
                            }
                        )
                    }
                }


            }
        }
    } else {
        EmptyScreenComposable(navController, R.string.source_not_available)
    }
}

