package ir.kazemcodes.infinity.presentation.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import ir.kazemcodes.infinity.api_feature.network.InfinityInstance
import ir.kazemcodes.infinity.domain.network.models.ParsedHttpSource
import ir.kazemcodes.infinity.base_feature.navigation.BookDetailKey
import ir.kazemcodes.infinity.base_feature.navigation.WebViewKey
import ir.kazemcodes.infinity.presentation.components.LinearViewList
import timber.log.Timber


@Composable
fun BrowserScreen(
    viewModel: BrowseViewModel = hiltViewModel(),
    api: ParsedHttpSource
) {
    val scrollState = rememberLazyListState()
    val backstack = LocalBackstack.current
    val state = viewModel.state.value
    val backStack = LocalBackstack.current
    InfinityInstance.inDetailScreen = false

    LaunchedEffect(key1 = true) {
        if (api != viewModel.state.value.api) {

            viewModel.cleanState()
        }
        viewModel.changeApi(api = api)
        viewModel.getBooks(source = api)
    }

    Scaffold(
        topBar = {
            TopAppBar(backgroundColor = MaterialTheme.colors.background) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "WebView",
                    tint = MaterialTheme.colors.onBackground,
                    modifier = Modifier
                        .clickable {
                            backStack.goTo(WebViewKey(api.baseUrl))
                        }
                )
            }
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),

            ) {
            if (state.books.isNotEmpty()) {
                LinearViewList(books = state.books, onClick = { index ->
                    backstack.goTo(BookDetailKey(state.books[index], api = api))
                }, scrollState = scrollState)

            }

            if (scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == scrollState.layoutInfo.totalItemsCount - 1) {
                Timber.d("Scroll state reach the bottom")
                LaunchedEffect(key1 = true) {
                    viewModel.getBooks(api)
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


