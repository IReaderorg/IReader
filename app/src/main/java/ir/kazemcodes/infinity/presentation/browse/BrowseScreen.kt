package ir.kazemcodes.infinity.presentation.browse

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.base_feature.navigation.BookDetailKey
import ir.kazemcodes.infinity.base_feature.navigation.WebViewKey
import ir.kazemcodes.infinity.explore_feature.domain.util.isScrolledToTheEnd
import ir.kazemcodes.infinity.library_feature.util.mappingApiNameToAPi
import ir.kazemcodes.infinity.presentation.components.GridLayoutComposable
import ir.kazemcodes.infinity.presentation.components.LinearViewList
import ir.kazemcodes.infinity.presentation.library.DisplayMode
import ir.kazemcodes.infinity.presentation.library.components.RadioButtonWithTitleComposable


@ExperimentalFoundationApi
@Composable
fun BrowserScreen() {
    val viewModel = rememberService<BrowseViewModel>()
    val scrollState = rememberLazyListState()
    val backstack = LocalBackstack.current
    val state = viewModel.state.value
    val backStack = LocalBackstack.current
    val source = viewModel.getSource()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(source.name) },
                backgroundColor = MaterialTheme.colors.background,
                actions = {
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
                        expanded = viewModel.state.value.isMenuDropDownShown,
                        onDismissRequest = {
                            viewModel.onEvent(BrowseScreenEvents.ToggleMenuDropDown(false))
                        }
                    ) {
                        DropdownMenuItem(onClick = {
                            viewModel.onEvent(BrowseScreenEvents.UpdateLayoutType(
                                layoutType = LayoutType.CompactLayout))
                            viewModel.onEvent(BrowseScreenEvents.ToggleMenuDropDown(false))
                        }) {
                            RadioButtonWithTitleComposable(
                                text = DisplayMode.CompactModel.title,
                                selected = viewModel.state.value.layout == LayoutType.CompactLayout
                            )
                        }
                        DropdownMenuItem(onClick = {
                            viewModel.onEvent(BrowseScreenEvents.UpdateLayoutType(
                                layoutType = LayoutType.GridLayout))
                            viewModel.onEvent(BrowseScreenEvents.ToggleMenuDropDown(false))
                        }) {
                            RadioButtonWithTitleComposable(
                                text = DisplayMode.GridLayout.title,
                                selected = viewModel.state.value.layout == LayoutType.GridLayout,
                            )
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
            if (state.books.isNotEmpty()) {
                if (state.layout == LayoutType.CompactLayout) {
                    LinearViewList(books = state.books, onClick = { index ->
                        backstack.goTo(BookDetailKey(state.books[index], source = source))
                    }, scrollState = scrollState)
                } else if (state.layout == LayoutType.GridLayout) {
                    GridLayoutComposable(books = state.books, onClick = { index ->
                        backStack.goTo(
                            BookDetailKey(
                                state.books[index],
                                source = mappingApiNameToAPi(state.books[index].source ?: "")
                            )
                        )
                    }, scrollState = scrollState)
                }
            }
            if (scrollState.isScrolledToTheEnd() && !state.isLoading && state.error.isEmpty()) {
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


