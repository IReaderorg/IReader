package ir.kazemcodes.infinity.presentation.browse

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.base_feature.navigation.WebViewKey
import ir.kazemcodes.infinity.explore_feature.domain.util.isScrolledToTheEnd
import ir.kazemcodes.infinity.presentation.layouts.layouts
import ir.kazemcodes.infinity.presentation.library.components.LayoutComposable
import ir.kazemcodes.infinity.presentation.library.components.RadioButtonWithTitleComposable


@ExperimentalFoundationApi
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
                        Text(
                            text = source.name,
                            color = MaterialTheme.colors.onBackground,
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Box {
                            if (state.searchQuery.isEmpty()) {
                                Text(
                                    text = "Search...",
                                    style = MaterialTheme.typography.subtitle1,
                                    color = MaterialTheme.colors.onBackground.copy(alpha = .7F)
                                )
                            }
                            BasicTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = state.searchQuery,
                                onValueChange = {
                                    viewModel.onEvent(BrowseScreenEvents.UpdateSearchInput(it))
                                },
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    viewModel.onEvent(BrowseScreenEvents.SearchBooks(state.searchQuery))
                                    focusManager.clearFocus()
                                }),
                                singleLine = true,
                                textStyle = TextStyle(color = MaterialTheme.colors.onBackground),
                            )
                        }
                    }


                },
                backgroundColor = MaterialTheme.colors.background,
                actions = {
                    if (state.isSearchModeEnable) {
                        IconButton(onClick = {
                            viewModel.onEvent(BrowseScreenEvents.ToggleSearchMode(false))
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Icon",
                                tint = MaterialTheme.colors.onBackground
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                viewModel.onEvent(BrowseScreenEvents.ToggleSearchMode())
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = MaterialTheme.colors.onBackground
                            )
                        }
                    }
                    IconButton(onClick = {
                        backStack.goTo(WebViewKey(source.baseUrl))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "WebView",
                            tint = MaterialTheme.colors.onBackground,
                        )
                    }

                    IconButton(onClick = {
                        viewModel.onEvent(BrowseScreenEvents.ToggleMenuDropDown(true))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colors.onBackground,
                        )
                    }
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
                    IconButton(onClick = { backStack.goBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "ArrowBack Icon",
                            tint = MaterialTheme.colors.onBackground,
                        )
                    }

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


