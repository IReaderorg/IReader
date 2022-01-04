package ir.kazemcodes.infinity.presentation.browse

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.base_feature.navigation.WebViewKey
import ir.kazemcodes.infinity.presentation.layouts.layouts
import ir.kazemcodes.infinity.presentation.library.components.LayoutComposable
import ir.kazemcodes.infinity.presentation.library.components.RadioButtonWithTitleComposable
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarActionButton
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarBackButton
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarSearch
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarTitle
import ir.kazemcodes.infinity.util.isScrolledToTheEnd




@Composable
fun BrowserScreen() {
    val viewModel = rememberService<BrowseViewModel>()
    val scrollState = rememberLazyListState()
    val state = viewModel.state.value
    val backStack = LocalBackstack.current
    val source = viewModel.getSource()
    val focusManager = LocalFocusManager.current


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
                                viewModel.onEvent(BrowseScreenEvents.SearchBooks(state.searchQuery))
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
                    } else {
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
                            backStack.goTo(WebViewKey(source.baseUrl))

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
            if (state.searchedBook.books.isNotEmpty() && state.isSearchModeEnable) {
                LayoutComposable(
                    books = state.searchedBook.books,
                    layout = state.layout,
                    scrollState = scrollState,
                    source = source
                )
            }
            if (state.books.isNotEmpty() && !state.isSearchModeEnable) {
                LayoutComposable(
                    books = state.books,
                    layout = state.layout,
                    scrollState = scrollState,
                    source = source
                )
            }
            if (scrollState.isScrolledToTheEnd() && !state.isLoading && state.error.isEmpty() && !state.isSearchModeEnable) {
                viewModel.onEvent(BrowseScreenEvents.GetBooks(source = source))
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


