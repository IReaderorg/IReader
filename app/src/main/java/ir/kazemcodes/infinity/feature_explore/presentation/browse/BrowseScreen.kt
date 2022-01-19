package ir.kazemcodes.infinity.feature_explore.presentation.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.core.presentation.layouts.layouts
import ir.kazemcodes.infinity.core.presentation.reusable_composable.*
import ir.kazemcodes.infinity.feature_activity.presentation.WebViewKey
import ir.kazemcodes.infinity.feature_library.presentation.components.LayoutComposable
import ir.kazemcodes.infinity.feature_library.presentation.components.RadioButtonWithTitleComposable
import ir.kazemcodes.infinity.feature_sources.sources.models.FetchType


@Composable
fun BrowserScreen() {
    val viewModel = rememberService<BrowseViewModel>()
    val scrollState = rememberLazyListState()
    val state = viewModel.state.value
    val backStack = LocalBackstack.current
    val source = viewModel.getSource()
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
                               viewModel.getBooks(state.searchQuery,ExploreType.Search)
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
                    } else if (source.supportSearch){
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
                            backStack.goTo(WebViewKey(source.baseUrl, sourceName = source.name, fetchType = FetchType.Latest.index))
                        },
                    )
                    TopAppBarActionButton(
                        imageVector = Icons.Default.Menu,
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
                    TopAppBarBackButton(backStack = backStack)

                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            if (books.loadState.refresh is LoadState.NotLoading) {
                LayoutComposable(
                    books = books,
                    layout = state.layout,
                    scrollState = scrollState,
                    source = source,
                    backStack = backStack,
                    isLocal = false
                )
            }
            if (books.loadState.source.refresh is LoadState.Error) {
                ErrorTextWithEmojis(error = state.error, modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .wrapContentSize(Alignment.Center)
                    .align(Alignment.Center))
            }
            if (books.loadState.refresh is LoadState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }


    }
}


