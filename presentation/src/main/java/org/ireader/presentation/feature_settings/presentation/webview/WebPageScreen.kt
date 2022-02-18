package org.ireader.presentation.feature_settings.presentation.webview

import android.webkit.WebView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.web.WebContent
import com.google.accompanist.web.rememberWebViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.getHtml
import org.ireader.domain.models.source.HttpSource
import org.ireader.domain.view_models.settings.webview.WebViewPageModel
import org.ireader.infinity.core.data.network.utils.setDefaultSettings
import org.ireader.presentation.presentation.reusable_composable.TopAppBarTitle


@ExperimentalCoroutinesApi
@Composable
fun WebPageScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: WebViewPageModel = hiltViewModel(),
) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    val webView = remember {
        mutableStateOf<WebView?>(null)
    }

    val source = viewModel.state.source
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        event.uiText.asString(context)
                    )
                }
                else -> {}
            }
        }

    }
    val webUrl = remember {
        mutableStateOf(viewModel.state.webUrl)
    }
    val webViewState = rememberWebViewState(url = webUrl.value)


    val refreshState = rememberSwipeRefreshState(isRefreshing = viewModel.state.isLoading)


    Scaffold(
        topBar = {
            WebPageTopBar(
                navController = navController,
                urlToRender = viewModel.state.url,
                fetchType = viewModel.state.fetcher,
                source = source,
                onGo = {
                    webViewState.content = WebContent.Url(viewModel.state.url)
                    // webView.value?.loadUrl(viewModel.state.url)
                    viewModel.updateWebUrl(viewModel.state.url)
                },
                refresh = {
                    webView.value?.reload()

                },
                goBack = {
                    webView.value?.goBack()
                },
                goForward = {
                    webView.value?.goForward()
                },
                fetchBook = {
                    if (source != null) {
                        scope.launch {
                            webView.value?.setUserAgent(source.headers.get("User-Agent")
                                ?: HttpSource.DEFAULT_USER_AGENT)
                            viewModel.getBookDetailAndChapter(pageSource = webView.value?.getHtml()
                                ?: "",
                                url = webViewState.content.getCurrentUrl() ?: "",
                                source)
                        }
                    }
                },
                fetchBooks = {
                    if (source != null) {
                        scope.launch {
                            webView.value?.setUserAgent(source.headers.get("User-Agent")
                                ?: HttpSource.DEFAULT_USER_AGENT)
                            viewModel.getExploredBook(pageSource = webView.value?.getHtml() ?: "",
                                url = webViewState.content.getCurrentUrl() ?: "",
                                source)
                        }
                    }
                },
                fetchChapter = {
                    if (source != null) {
                        scope.launch {
                            webView.value?.setUserAgent(source.headers.get("User-Agent")
                                ?: HttpSource.DEFAULT_USER_AGENT)
                            viewModel.getContentFromWebView(pageSource = webView.value?.getHtml()
                                ?: "",
                                url = webViewState.content.getCurrentUrl() ?: "",
                                source)
                        }
                    }
                },
                onValueChange = {
                    viewModel.updateUrl(it)
                }
            )
        },
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(it) { data ->
                Snackbar(
                    actionColor = MaterialTheme.colors.primary,
                    snackbarData = data,
                    backgroundColor = MaterialTheme.colors.background,
                    contentColor = MaterialTheme.colors.onBackground,
                )
            }
        }

    ) {
        SwipeRefresh(
            state = refreshState,
            onRefresh = {
                webView.value?.reload()
                viewModel.toggleLoading(true)
            },
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = trigger,
                    scale = true,
                    backgroundColor = MaterialTheme.colors.background,
                    contentColor = MaterialTheme.colors.primaryVariant,
                    elevation = 8.dp
                )
            }
        ) {
            if (webViewState.isLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            WebView(
                modifier = Modifier.fillMaxSize(),
                state = webViewState,
                captureBackPresses = false,
                isLoading = {
                    viewModel.toggleLoading(it)
                },
                onCreated = {
                    webView.value = it
                    it.setDefaultSettings()
                    if (source != null) {
                        it.setUserAgent(source.headers.get("User-Agent")
                            ?: HttpSource.DEFAULT_USER_AGENT)
                    }
                },
                updateUrl = {
                    viewModel.updateUrl(it)
                },
            )
        }
    }

}


@Composable
fun ScrollableAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    background: Color = MaterialTheme.colors.primary,
    scrollUpState: Boolean,
) {
    val position by animateFloatAsState(if (scrollUpState) -150f else 0f)

    Surface(modifier = Modifier.graphicsLayer { translationY = (position) }, elevation = 8.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(color = background),
        )
        Row(modifier = modifier.padding(start = 12.dp)) {
            if (navigationIcon != null) {
                navigationIcon()
            }
            TopAppBarTitle(title = title)
        }
    }
}