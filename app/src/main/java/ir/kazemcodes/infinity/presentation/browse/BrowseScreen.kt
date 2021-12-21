package ir.kazemcodes.infinity.presentation.browse

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.base_feature.navigation.BookDetailKey
import ir.kazemcodes.infinity.base_feature.navigation.WebViewKey
import ir.kazemcodes.infinity.presentation.components.LinearViewList
import timber.log.Timber


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
            TopAppBar(title = {Text("Explore")} , backgroundColor = MaterialTheme.colors.background, actions = {
                IconButton(onClick = { backStack.goTo(WebViewKey(source.baseUrl)) }) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "WebView",
                        tint = MaterialTheme.colors.onBackground,
                    )
                }

            },navigationIcon = {
                IconButton(onClick = { backStack.goBack()}) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "ArrowBack Icon",
                        tint = MaterialTheme.colors.onBackground,
                    )
                }

            })
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),

            ) {
            if (state.books.isNotEmpty()) {
                LinearViewList(books = state.books, onClick = { index ->
                    backstack.goTo(BookDetailKey(state.books[index], source = source))
                }, scrollState = scrollState)

            }

            if (scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == scrollState.layoutInfo.totalItemsCount - 1) {
                Timber.d("Scroll state reach the bottom")
                LaunchedEffect(key1 = true) {
                    viewModel.getBooks(source)
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


