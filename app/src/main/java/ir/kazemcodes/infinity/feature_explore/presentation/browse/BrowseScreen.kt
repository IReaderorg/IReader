package ir.kazemcodes.infinity.feature_explore.presentation.browse

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.ExperimentalPagingApi
import androidx.paging.compose.collectAsLazyPagingItems
import ir.kazemcodes.infinity.core.presentation.components.handlePagingResult
import ir.kazemcodes.infinity.core.presentation.layouts.layouts
import ir.kazemcodes.infinity.core.presentation.reusable_composable.*
import ir.kazemcodes.infinity.feature_activity.presentation.Screen
import ir.kazemcodes.infinity.feature_library.presentation.components.LayoutComposable
import ir.kazemcodes.infinity.feature_library.presentation.components.RadioButtonWithTitleComposable
import ir.kazemcodes.infinity.feature_sources.sources.models.FetchType


@OptIn(ExperimentalFoundationApi::class)
@ExperimentalPagingApi
@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val scrollState = rememberLazyListState()
    val state = viewModel.state.value

    val source = viewModel.state.value.source
    val focusManager = LocalFocusManager.current

    val books = viewModel.books.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!state.isSearchModeEnable) {
                        TopAppBarTitle(title = source.name)
                    } else {
                        TopAppBarSearch(query = state.searchQuery,
                            onValueChange = {
                                viewModel.onEvent(BrowseScreenEvents.UpdateSearchInput(it))
                            },
                            onSearch = {
                                viewModel.getBooks(
                                    query = state.searchQuery,
                                    type = ExploreType.Search, )
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
                            onClick = { viewModel.onEvent(BrowseScreenEvents.ToggleSearchMode()) },
                        )
                    } else if (source.supportSearch) {
                        TopAppBarActionButton(
                            imageVector = Icons.Default.Search,
                            title = "Search",
                            onClick = {
                                viewModel.onEvent(BrowseScreenEvents.ToggleSearchMode())

                            },
                        )
                    }
                    TopAppBarActionButton(
                        imageVector = Icons.Default.Language,
                        title = "WebView",
                        onClick = {
                            //TODO change bookId AND url
                            navController.navigate(
                                Screen.WebPage.passArgs(
                                    sourceId = source.sourceId,
                                    fetchType = FetchType.Latest.index,
                                    url = source.baseUrl
                                )
                            )
                        },
                    )
                    TopAppBarActionButton(
                        imageVector = Icons.Default.GridView,
                        title = "Menu",
                        onClick = {
                            viewModel.onEvent(BrowseScreenEvents.ToggleMenuDropDown(true))
                        },
                    )
                    DropdownMenu(
                        modifier = Modifier.background(MaterialTheme.colors.background),
                        expanded = viewModel.state.value.isMenuDropDownShown,
                        onDismissRequest = {
                            viewModel.onEvent(BrowseScreenEvents.ToggleMenuDropDown(false))
                        }
                    ) {
                        layouts.forEach { layout ->
                            DropdownMenuItem(onClick = {
                                viewModel.onEvent(BrowseScreenEvents.UpdateLayoutType(
                                    layoutType = layout))
                                viewModel.onEvent(BrowseScreenEvents.ToggleMenuDropDown(false))
                            }) {
                                RadioButtonWithTitleComposable(
                                    text = layout.title,
                                    selected = viewModel.state.value.layout == layout.layout,
                                    onClick = {
                                        viewModel.onEvent(BrowseScreenEvents.UpdateLayoutType(
                                            layoutType = layout))
                                        viewModel.onEvent(BrowseScreenEvents.ToggleMenuDropDown(
                                            false))
                                    }
                                )
                            }
                        }
                    }


                },
                navigationIcon = {
                    TopAppBarBackButton(navController = navController)

                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val result = handlePagingResult(books = books, onEmptyResult = {
                ErrorTextWithEmojis(error = "Sorry, the source failed to get any content.")
            })

            if (result) {
                LayoutComposable(
                    books = books,
                    layout = state.layout,
                    scrollState = scrollState,
                    source = source,
                    navController = navController,
                    isLocal = false
                )
            }
        }


    }
}

