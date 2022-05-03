package org.ireader.web

import android.webkit.WebView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Surface
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.web.WebContent
import com.google.accompanist.web.rememberWebViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import org.ireader.common_resources.UiEvent
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.domain.utils.setDefaultSettings

@OptIn(ExperimentalMaterialApi::class)
@ExperimentalCoroutinesApi
@Composable
fun WebPageScreen(
    modifier: Modifier = Modifier,
    viewModel: WebViewPageModel,
    onPopBackStack: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    var webView by remember {
        mutableStateOf<WebView?>(null)
    }

    DisposableEffect(key1 = true) {
        onDispose {
            webView?.destroy()
        }
    }
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
        mutableStateOf(viewModel.webUrl)
    }

    val webViewState = rememberWebViewState(url = webUrl.value)

    val refreshState = rememberSwipeRefreshState(isRefreshing = viewModel.isLoading)

    Scaffold(
        topBar = {
            WebPageTopBar(
                urlToRender = viewModel.url,
                onGo = {
                    webViewState.content = WebContent.Url(viewModel.url)
                    // webView.value?.loadUrl(viewModel.state.url)
                    viewModel.updateWebUrl(viewModel.url)
                },
                refresh = {
                    webView?.reload()
                },
                goBack = {
                    webView?.goBack()
                },
                goForward = {
                    webView?.goForward()
                },
                onValueChange = {
                    viewModel.updateUrl(it)
                },
                onPopBackStack = onPopBackStack
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

    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {

            SwipeRefresh(
                state = refreshState,
                onRefresh = {
                    webView?.reload()
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
                    LinearProgressIndicator(
                        Modifier
                            .fillMaxWidth()
                    )
                }

                WebView(
                    modifier = Modifier.fillMaxSize(),
                    state = webViewState,
                    captureBackPresses = false,
                    isLoading = {
                        viewModel.toggleLoading(it)
                    },
                    onCreated = {
                        webView = it
                        it.setDefaultSettings()
                    },
                    updateUrl = {
                        viewModel.updateUrl(it)
                    },
                )
            }
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
            BigSizeTextComposable(text = title)
        }
    }
}
