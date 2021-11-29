package ir.kazemcodes.infinity.explore_feature.presentation.screen.browse_screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import ir.kazemcodes.infinity.base_feature.navigation.BookDetailKey
import ir.kazemcodes.infinity.explore_feature.presentation.screen.components.LinearViewList
import timber.log.Timber


@ExperimentalMaterialApi
@Composable
fun BrowseScreen(
    viewModel: BrowseViewModel = hiltViewModel(),
) {
    val scrollState = rememberLazyListState()
    val backstack = LocalBackstack.current
    val state = viewModel.state.value


    Box(
        modifier = Modifier.fillMaxSize(),

    ) {
        if (state.books.isNotEmpty()) {
            LinearViewList(books = state.books, onClick = { index ->
                backstack.goTo(BookDetailKey(state.books[index]))
            } , scrollState = scrollState)

        }

        if (scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == scrollState.layoutInfo.totalItemsCount - 1) {
            Timber.d("Scroll state reach the bottom")
            LaunchedEffect(key1 = true ) {
                viewModel.getBooks("https://readwebnovels.net/page/${viewModel.currentPage.value}/")
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


