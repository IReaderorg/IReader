package ir.kazemcodes.infinity.feature_detail.presentation.book_detail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarBackButton
import ir.kazemcodes.infinity.core.ui.WebViewScreenSpec
import ir.kazemcodes.infinity.core.utils.getUrlWithoutDomain
import ir.kazemcodes.infinity.feature_sources.sources.models.FetchType

@Composable
fun BookDetailTopAppBar(
    modifier: Modifier = Modifier,
    isWebViewEnable: Boolean,
    viewModel: BookDetailViewModel,
    navController : NavController
) {
    val state = viewModel.state
    TopAppBar(
        title = {},
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        backgroundColor = Color.Transparent,
        contentColor = MaterialTheme.colors.onBackground,
        elevation = 0.dp,
        actions = {
            if (isWebViewEnable) {
                IconButton(onClick = { viewModel.getWebViewData() }) {
                    Icon(
                        imageVector = Icons.Default.TrackChanges,
                        contentDescription = "Get from webview",
                        tint = MaterialTheme.colors.onBackground,
                    )
                }
            }

            IconButton(onClick = { viewModel.getRemoteChapterDetail(state.book) }) {
                Icon(
                    imageVector = Icons.Default.Autorenew,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colors.onBackground,
                )
            }
            IconButton(onClick = {
                navController.navigate(
                    WebViewScreenSpec.buildRoute(
                        url = viewModel.state.source.baseUrl + getUrlWithoutDomain(
                            viewModel.state.book.link),
                        sourceId = viewModel.state.source.sourceId,
                        fetchType = FetchType.Detail.index,
                        bookId = viewModel.state.book.id
                    )
                )
            }) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "WebView",
                    tint = MaterialTheme.colors.onBackground,
                )
            }


        },
        navigationIcon = {
            TopAppBarBackButton(navController = navController)

        }
    )
}