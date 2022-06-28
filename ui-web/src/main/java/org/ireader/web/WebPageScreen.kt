package org.ireader.web

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.web.AccompanistWebChromeClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.core.R
import org.ireader.core_api.source.CatalogSource
import org.ireader.core_api.source.HttpSource
import org.ireader.core_ui.ui.SnackBarListener
import org.ireader.domain.utils.setDefaultSettings

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@ExperimentalCoroutinesApi
@Composable
fun WebPageScreen(
    modifier: Modifier = Modifier,
    viewModel: WebViewPageModel,
    onPopBackStack: () -> Unit,
    onModalBottomSheetShow: () -> Unit,
    onModalBottomSheetHide: () -> Unit,
    source: CatalogSource?,
    snackBarHostState: SnackbarHostState,
    scaffoldPadding: PaddingValues
) {
    val userAgent = remember {
        (source as? HttpSource )?.getCoverRequest("")?.second?.headers?.get(HttpHeaders.UserAgent)
    }
    val webViewState = rememberWebViewState(url = viewModel.url)

    LaunchedEffect(key1 = webViewState.hashCode()) {
        viewModel.webViewState = webViewState
    }

    LaunchedEffect(key1 = webViewState.content.getCurrentUrl()) {
        webViewState.content.getCurrentUrl()?.let { viewModel.webUrl = it }
    }
    val accompanistState = AccompanistWebChromeClient()

    DisposableEffect(key1 = true) {
        onDispose {
            viewModel.webView?.destroy()
        }
    }
    SnackBarListener(vm = viewModel, host = snackBarHostState)
    val refreshState = rememberSwipeRefreshState(isRefreshing = viewModel.isLoading)
    Box(modifier = Modifier.padding(scaffoldPadding)) {
        SwipeRefresh(
            state = refreshState,
            onRefresh = {
                viewModel.webView?.reload()
                viewModel.toggleLoading(true)
            },
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = trigger,
                    scale = true,
                    backgroundColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primaryContainer,
                    elevation = 8.dp
                )
            }
        ) {
            if (webViewState.isLoading) {
                LinearProgressIndicator(
                    Modifier
                        .fillMaxWidth(),
                )
            }
            WebView(
                state = webViewState,
                onCreated = {
                    viewModel.webView = it
                    it.setDefaultSettings()
                    userAgent?.let { ua -> it.setUserAgent(ua) }
                },
                chromeClient = accompanistState
            )
        }
    }
}



@Composable
fun WebPageBottomLayout(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onBook: (Long) -> Unit,
    state: WebViewPageState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = {
                onCancel()
            }, modifier = Modifier.width(92.dp), shape = RoundedCornerShape(4.dp)) {
                MidSizeTextComposable(
                    text =stringResource(R.string.cancel),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(onClick = {
                onConfirm()
            }, modifier = Modifier.width(92.dp), shape = RoundedCornerShape(4.dp)) {
                MidSizeTextComposable(
                    text = stringResource(R.string.apply),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            if (state.stateBook != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Checkbox(checked = -1 in state.selectedBooks,
                            onCheckedChange = {
                                if (it) {
                                    state.selectedBooks.add(-1)
                                } else {
                                    state.selectedBooks.remove(-1)
                                }
                            })
                        MidSizeTextComposable(text = stringResource(R.string.add_as_new))

                    }
                }
            }

            items(state.availableBooks.size) { index ->
                val item = state.availableBooks[index]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Checkbox(checked = item.id in state.selectedBooks,
                            onCheckedChange = {
                                if (it) {
                                    state.selectedBooks.add(item.id)
                                } else {
                                    state.selectedBooks.remove(item.id)
                                }
                            })
                        MidSizeTextComposable(text = item.title)
                    }
                    AppIconButton(imageVector = Icons.Default.ArrowForward,
                        contentDescription = stringResource(R.string.view_book),
                        onClick = {
                            onBook(item.id)
                        })
                }
            }
        }

    }
}